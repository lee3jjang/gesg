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
 *  <p> 내부 기준(IFRS)의  공시이율 추정 모형
 *  <p> 내부산출기준을 적용하여, 자산운용이익률, 외부지표금리, 공시기준이율등의 공시이율 구성요소와 국고채 금리간읜  통계 분석 결과를 적용함.
 *  <p>    1. 예측 Driver 로 국고채 1개월, 3년물, 5년물 등을 통계모형의 독립변수로 지정 
 *  <p>    2. 자산운용이익률, 외부지표금리, 공시기준이율등을 통계모형의 종속변수로 지정    
 *  <p>    3. 예측 Driver 와  종속변수간의 최적 통계모형 산출( 과거 이동평균 12m, 24m, 36m 등을 적용하여 설명력이 최대인 모형을 산출함.
 *  <p>    4. 통계모형과 현재의 금리 수준을 적용하여 공시이율 최선추정치를 산출함.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job35_DiscRateIfrsAsync {
	private final static Logger logger = LoggerFactory.getLogger(Job35_DiscRateIfrsAsync.class);
	
	
	
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, ExecutorService exe) {
		return BizDiscRateModel.getDiscRateAsync(bssd, "I",exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
	public static List<DiscRate> getDiscRateAsync(String bssd, ExecutorService exe) {
		return BizDiscRateModel.getDiscRateAsync(bssd, "I",exe);
		
		
	}
	
	public static List<DiscRate> getDiscRateAsync_Alt(String bssd, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateAsync(bssd, "I", "RF_KRW_BU", true, exe);
		
	}
	public static List<DiscRateSce> getDiscRateAsync_Alt(String bssd, String sceNo, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateAsync(bssd, "I", "RF_KRW_BU", sceNo, true, exe);
		
	}
	public static List<DiscRateSce> getDiscRateSceAsync_Alt(String bssd, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateSceAsync(bssd, "I", "RF_KRW_BU",  true, exe);
		
	}
	
	public static List<DiscRateSce> getDiscRateAsync_Alt(String bssd, String bizDv, String irCurveId, String sceNo, ExecutorService exe) {
		return BizDiscRateModelNew.getDiscRateAsync(bssd, bizDv, irCurveId, sceNo, true, exe);
		
	}
}
