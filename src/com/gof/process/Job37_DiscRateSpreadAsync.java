package com.gof.process;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscRate;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateSce;
import com.gof.model.BizDiscRateModel;
import com.gof.model.BizDiscRateModelNew;

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
public class Job37_DiscRateSpreadAsync {
	private final static Logger logger = LoggerFactory.getLogger(Job37_DiscRateSpreadAsync.class);
	
	
	
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, ExecutorService exe) {
		return BizDiscRateModel.getDiscRateAsync(bssd, "S",exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
	public static List<DiscRate> getDiscRateAsync(String bssd, ExecutorService exe) {
		return BizDiscRateModel.getDiscRateAsync(bssd, "S",exe);
		
	}
	
	
	public static List<DiscRate> getDiscRateAsync_Alt(String bssd, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateAsync(bssd, "S","RF_KRW_KICS", false, exe);
		
	}
	
	public static List<DiscRateSce> getDiscRateAsync_Alt(String bssd, String sceNo, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateAsync(bssd, "S", "RF_KRW_KICS", sceNo, true, exe);
		
	}
}
