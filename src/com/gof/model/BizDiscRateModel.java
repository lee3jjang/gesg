package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscRateAdjUd;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscRateStatUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateSce;
import com.gof.entity.DiscRateStats;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> KIcS 기준의  공시이율 추정 모형
 *  <p> KIcS 에서 제시하는 방법론으로 공시기준이율 산출시 외부 지표금리는 배제하고 자산운용 수익률 요인만 고려함.
 *  <p>    1. 자산운용 수익률과 공시이율과의 비율의 3년 평균으로 조정률 결정
 *  <p>    2. 자산운용 수익률의 미래 추정치는 조정 무위험 금리의 1M Forward 에서 투자관리비용을 차감하여 결정함.  
 *  <p>	     2.1 별도의 통계모형을 적용하지 않으므로 독립변수는 KTB1M, 상수항에 투자관리비용, 계수항에 1.0 을 설정함. 
 *  <p>    3. 만일 사용자가 지정한 통계모형이 있으면 우선적으로 적용함.  
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class BizDiscRateModel {
	private final static Logger logger = LoggerFactory.getLogger(BizDiscRateModel.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	통계모형에 현행 금리 기간 구조를 반영하여 공시이율 최선추정을 산출함.
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, String bizDv,ExecutorService exe) {
		return getDiscRateAsync(bssd, bizDv, exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
//	통계모형에 현행 금리 기간 구조를 반영하여 공시이율 최선추정을 산출함.
	public static List<DiscRate> getDiscRateAsync(String bssd, String bizDv, ExecutorService exe) {
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		Map<String, Map<String, Double>> curveHistoryRateByMaturity = new HashMap<String, Map<String, Double>>();

//		Refactoring : DB 호출을 최소화를 위해 메서드 구조 개선
		Map<String, Map<String, Double>> termStructureByDate = getAllDriverCurveMap(bssd, -36, isRiskFree);
		for(EBaseMatCd aa : EBaseMatCd.values()) {
//			curveHistoryRateByMaturity.put(aa.name(), getAllDriverCurveMapByMatCd(bssd, -36, aa.name(), isRiskFree));
			curveHistoryRateByMaturity.put(aa.name(), getAllDriverCurveMapByMatCd1(bssd, -36, aa.name(), termStructureByDate));
		}
		
		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
//														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2305"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRate>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateByStat(stat, bssd, curveHistoryRateByMaturity), exe))
				.collect(Collectors.toList());

		List<DiscRate> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		logger.info("Disc Rate for Model {} is calculated. {} Results are inserted into EAS_DISC_RATE table", bizDv, rst.size());
		return rst;
	}
	
	
	private static List<DiscRate> getDiscRateByStat(BizDiscRateStat stat, String bssd, Map<String, Map<String, Double>> curveHistoryRateByMaturity) {
		List<DiscRate> rstList = new ArrayList<DiscRate>();
		DiscRate temp;
		int k = stat.getAvgMonNum().intValue();
		
		Map<String, Double> curveHistoryMap = curveHistoryRateByMaturity.get(stat.getIndiVariableMatCd());
		
		for(int i =0; i< 1200; i++) {
			int cnt =0;
			double intRate =0.0;
			
			for(int j= i + 1 - k; j < i+1; j++) {
				
				String fwdBssd = FinUtils.addMonth(bssd, j);
				cnt =cnt +1;
				intRate = intRate + curveHistoryMap.getOrDefault(fwdBssd, new Double(0.0));
//				logger.info("asdfasdf : {},{},{},{}", fwdBssd, curveHistoryMap.getOrDefault(fwdBssd, new Double(0.0)), intRate);
			}
			intRate = intRate/ cnt;
			
			temp = new DiscRate();
			
			temp.setBaseYymm(bssd);
			temp.setIntRateCd(stat.getIntRateCd());
			temp.setDiscRateCalcTyp(stat.getApplyBizDv());
			temp.setMatCd("M" +String.format("%04d", i+1));
			temp.setBaseDiscRate(stat.getRegrCoef() * intRate + stat.getRegrConstant());
			temp.setAdjRate(stat.getAdjRate());
			
			temp.setDiscRate(temp.getBaseDiscRate() * temp.getAdjRate());
			
			temp.setVol(intRate);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(temp);
			
		}
		
//		rstList.stream().forEach(s->logger.debug("Accrete  Rate for IFRS17 Result : {}", s.toString()));
		return rstList;
	}
	
	
	// 만기코드별 과거 및 미래 Time Series 가 필요함. ..Map<기준월, 조정무위험>
	// 과거 금리는 Term Structure 에서 추출하고, 미래 금리는 현재 금리기간구조에서 Forwarding 한 후 추출함.
	
	private static Map<String, Double> getAllDriverCurveMapByMatCd(String bssd,  int monthNum, String matCd, boolean isRiskFree){
		
		Map<String, Double> rstMap = new HashMap<String,  Double>();
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		
		matCdRateMap = getDriverCurveMap(bssd, isRiskFree);
		
//		미래시점의 금리 (forward 로 산출함)
		rstMap = FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, matCd); 
		
//		과거 만기별 금리를 일자로 정렬
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			matCdRateMap = getDriverCurveMap(prevBssd, isRiskFree);
			rstMap.put(prevBssd, matCdRateMap.getOrDefault(matCd, new Double(0.0)));
		}
		return rstMap;
	}
	
	private static Map<String, Double> getAllDriverCurveMapByMatCd1(String bssd,  int monthNum, String matCd, Map<String, Map<String, Double>> rstInput){
		
//		Map<String, Map<String, Double>> rstInput = getAllDriverCurveMap(bssd, monthNum, isRiskFree);
		
		Map<String, Double> rstMap = new HashMap<String,  Double>();
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		
		matCdRateMap = rstInput.get(bssd);
		
//		미래시점의 금리 (forward 로 산출함)
		rstMap = FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, matCd); 
		
//		과거 만기별 금리를 일자로 정렬
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			matCdRateMap = rstInput.get(prevBssd);
			rstMap.put(prevBssd, matCdRateMap.getOrDefault(matCd, new Double(0.0)));
		}
		return rstMap;
	}

	private static Map<String, Map<String, Double>> getAllDriverCurveMap(String bssd,  int monthNum, boolean isRiskFree){
		Map<String, Map<String, Double>> rst = new HashMap<String, Map<String, Double>>();
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			rst.put(prevBssd, getDriverCurveMap(prevBssd, isRiskFree));
		}
		return rst;
	}
	
	
//	주어진 일자로 산출한 Map<만기, 조정 무위험 위험률> 
	private static Map<String, Double> getDriverCurveMap(String bssd,  boolean isRiskFree){
		if(isRiskFree) {
			List<IrCurveHis> curveList = IrCurveHisDao.getIrCurveHis( bssd, "A100");		
				
			List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
			Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
				 
			double ufr =  swParamMap.get("KRW").getUfr();
			double ufrt =  swParamMap.get("KRW").getUfrT();
				
			SmithWilsonModel swModel = new SmithWilsonModel(curveList, ufr, ufrt);
				
			return swModel.getSmithWilsionResult(false).stream().collect(Collectors.toMap(s->s.getMatCd(), s->s.getSpotAnnual()));
		}
		else {
				List<BottomupDcnt> dcntRateList = BottomupDcntDao.getTermStructure(bssd, "RF_KRW_BU");
				
				return dcntRateList.stream().collect(Collectors.toMap(s->s.getMatCd(), s-> s.getRiskAdjRfRate()))	;
		}
	}
	
	
}
