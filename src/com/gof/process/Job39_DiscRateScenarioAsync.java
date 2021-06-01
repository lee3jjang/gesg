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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.entity.DiscRateSce;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.model.BizDiscRateModel;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;

/**
 *  <p> 무위험 금리의 시나리오를  내부기준의 공시이율 추정 모형에 적용하여 공시이율 시나리오를 생성함.
 *  <p> 공시이율 시나리오도 KICS 또는 QIS 기준으로 산출할 수 있으나 공시이율 시나리오의 데이터 량을 고려하여 1가지 방법으로만 공시이율 시나리오를 산출함.
 *  <p> 무위험 금리 시나리오의 금리구조에 대해 다음의 작업을 반복하여 시나리오를 생성함. ( 무위험 금리 시나리오 갯수 만큼 생성하며, 현재 1,000 개를 산출함) 
 *  <p>    1. 예측 Driver 로 국고채 선정  
 *  <p>    2. 국고채와 공시이율 산출의 주요 Factor 인 자산운용 수익률, 외부지표금리간 인과관계를 통계적으로 분석함. 
 *  <p>	     2.1 자산운용 수익률과 국고채의 시계열 데이터를 통해 통계모형 생성 
 *  <p>	     2.2 외부 지표금리와  국고채의 시계열 데이터를 통해 통계모형 생성
 *  <p>    3. 예측 Driver 인 국고채의 시나리오 ({@link Job14_EsgScenario} 를 통계모형에 적용하여 미래 시점의 자산운용수익률 , 외부지표금리 예측
 *  <p>    4. 예측된 자산운용수익률, 외부지표금리를 이용하여 공시기준이율과 공시이율을 산출함. ( 미래에 적용할 조정률은 현재 조정률이 변동 없다고 가정함.)
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Job39_DiscRateScenarioAsync {
	private final static Logger logger = LoggerFactory.getLogger(Job39_DiscRateScenarioAsync.class);
	
	public static List<DiscRateSce> getDiscRateScenarioAsync(String bssd, String bizDv, String sceNo,  Map<String, Double> pastCurveMap, ExecutorService exe) {
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		Map<String, Double> mergedCurveMapMap = new HashMap<String,  Double>();
		
		Map<String, Map<String, Double>> curveHistoryRateByMaturity = new HashMap<String, Map<String, Double>>();
		
		
		for(EBaseMatCd aa : EBaseMatCd.values()) {
//			미래시점의 금리를 기준년월로 정렬 (forward 로 산출함)
			matCdRateMap = getDriverCurveMap(bssd, sceNo, isRiskFree);
			
			mergedCurveMapMap.putAll(pastCurveMap);
			mergedCurveMapMap.putAll(FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, aa.name()));

			curveHistoryRateByMaturity.put(aa.name(), mergedCurveMapMap);
		}

		
		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2305"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRateSce>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateSceByStat(stat, bssd, sceNo,curveHistoryRateByMaturity), exe))
				.collect(Collectors.toList());

		List<DiscRateSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		if(Integer.parseInt(sceNo)%100 == 0) {
			logger.info("Biz Applied Disc Rate for SceNo {} is calculated. Results of SceNo {} are inserted into EAS_DISC_RATE_SCE table", sceNo, rst.size());
		}
		return rst;
	}

	private static List<DiscRateSce> getDiscRateSceByStat(BizDiscRateStat stat, String bssd, String sceNo, Map<String, Map<String, Double>> curveHistoryRateByMaturity) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
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
			
			temp = new DiscRateSce();
			
			temp.setBaseYymm(bssd);
			temp.setIntRateCd(stat.getIntRateCd());
			temp.setDiscRateCalcTyp(stat.getApplyBizDv());
			temp.setSceNo(sceNo);
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

	private static Map<String, Double> getDriverCurveMap(String bssd,  String sceNo, boolean isRiskFree){
		if(isRiskFree) {
			List<IrSce>  curveList = IrCurveHisDao.getIrCurveSce(bssd, "A100", sceNo);
//			logger.info("curveSize : {}", curveList.size());
			return curveList.stream().collect(Collectors.toMap(s->s.getMatCd(),  s->s.getRfIr()));
			
		}
		else {
			List<DcntSce> dcntRateList = BottomupDcntDao.getTermStructureScenario(bssd, "RF_KRW_BU", sceNo);
//			logger.info("curveSize : {},{}", dcntRateList.size(), sceNo);
			return dcntRateList.stream().collect(Collectors.toMap(s->s.getMatCd(), s-> s.getRiskAdjRfRate()))	;
		}
	}
}
