package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DiscRate;
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> QIS 기준의  공시이율 추정 모형
 *  <p> QIS 시 제시한 방법론은 개별보험사의 비교가능성 확보를 위해 자산운용수익률 요인은 제외하고 국고채 금리와 공시이율간의 직접적인 통계 분석 결과를 적용함.
 *  <p>    1. 예측 Driver 로 국고채 3년물의 3개월 평균, 3년 평균을 적용
 *  <p>    2. 통계모형 제시    
 *  <p>	     2.1 0.26 * 3M 평균 국고채 3년 +0.5 * 3Y 평균 국고채 3년  + 1.51% 
 *  <p>    3. 예측 Driver 인 국고채의 시나리오 ({@link Job14_EsgScenario} 를 통계모형에 적용하여 공시이율 산출함. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job36_DiscRateAsyncDeprecate {
	private final static Logger logger = LoggerFactory.getLogger(Job36_DiscRateAsyncDeprecate.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, ExecutorService exe) {
		return getDiscRateAsync(bssd, exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
	public static List<DiscRate> getDiscRateAsync(String bssd, ExecutorService exe) {
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		Map<String, Map<String, Double>> curveHistoryRateByMaturity = new HashMap<String, Map<String, Double>>();
		
		for(EBaseMatCd aa : EBaseMatCd.values()) {
			curveHistoryRateByMaturity.put(aa.name(), getAllDriverCurveMapByMatCd(bssd, -36, aa.name(), isRiskFree));
		}
		
		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
//															.filter(s-> s.getApplyBizDv().equals("I"))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2305"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRate>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateByStat(stat, bssd, curveHistoryRateByMaturity), exe))
				.collect(Collectors.toList());

		List<DiscRate> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		
		return rst;
	}
	
	private static List<DiscRate> getDiscRate(String bssd) {
		
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
//		List<DiscRateCalcSetting> discSetting
		
		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
													.filter(s ->s.isCalculable())
													.map(s ->s.getIntRateCd())
													.collect(Collectors.toList());
		
		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals("I"))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2305"))
															.collect(Collectors.toList());
		
		
		List<DiscRate> rst = new ArrayList<>();
		for(BizDiscRateStat aa : bizStatList) {
			rst.addAll(getDiscRateByStat(aa, bssd, isRiskFree));
		}
		
		
		logger.info("DcntSce for Kics : {}", rst.size());
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
	
	private static List<DiscRate> getDiscRateByStat(BizDiscRateStat stat, String bssd, boolean isRiskFree) {
		List<DiscRate> rstList = new ArrayList<DiscRate>();
		DiscRate temp;
		
		logger.info("stat : {},{},{}", stat.getIntRateCd(), stat.getRegrCoef(), stat.getRegrConstant());
//		List<IrCurveHis> curveList = IrCurveHisDao.getIrCurveHis( bssd, "A100");
//		List<BottomupDcnt> bottomUpList = BottomupDcntDao.getTermStructure(bssd, "RF_KRW_BU");
//		Map<String, Map<String, Double>> curveHistoryRateByMaturity = new HashMap<String, Map<String, Double>>();
		
		
		Map<String, Double> curveHistoryMap = getAllDriverCurveMapByMatCd(bssd, -36, stat.getIndiVariableMatCd(), isRiskFree);
		
//		int k = Integer.parseInt(stat.getAvgMonNum().toString());
		int k = stat.getAvgMonNum().intValue();
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
			temp.setDiscRateCalcTyp("I");
			temp.setMatCd("M" +String.format("%04d", i+1));
			temp.setBaseDiscRate(stat.getRegrCoef() * intRate + stat.getRegrConstant());
			temp.setAdjRate(stat.getAdjRate());
			
			temp.setDiscRate(temp.getBaseDiscRate() * temp.getAdjRate());
			
			
			temp.setVol(intRate);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(temp);
			
		}
		logger.info("Job36(Accrete  Rate for IFRS17 Calculation) creates  {} results.  They are inserted into EAS_DISC_RATE Table", rstList.size());
//		rstList.stream().forEach(s->logger.debug("Accrete  Rate for IFRS17 Result : {}", s.toString()));
		return rstList;
	}
	
	
//	만기코드별 날짜의 금리...
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
