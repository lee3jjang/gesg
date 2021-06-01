package com.gof.process;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BizDcntRateDao;
import com.gof.entity.BizDiscFwdRateSce;
import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateSce;
import com.gof.model.BizDiscFwdRateModel;
import com.gof.model.BizDiscFwdSceRateModel;
import com.gof.util.ParamUtil;

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
public class Job35_DiscRateFwdGen {
	private final static Logger logger = LoggerFactory.getLogger(Job35_DiscRateFwdGen.class);
	
	
	
	public static List<BizDiscFwdRateSce> getDiscFwdRateAsync(String bssd, String bizDv, String irCurveId, ExecutorService exe) {
		
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discInternalDriverIsRiskFree", "Y");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		if(bizDv.equals("I")) {
			isRiskFree = isRiskFreeString.equals("Y");
		}else {
			isRiskFree = isRiskFreeString.equals("N");
		}
		return BizDiscFwdRateModel.getDiscFwdRateAsync(bssd, bizDv, irCurveId, isRiskFree, exe);
		
	}
	
	public static List<BizDiscFwdRateSce> getDiscFwdRateAsync(String bssd, String bizDv, ExecutorService exe) {
		
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discInternalDriverIsRiskFree", "Y");
		String irCurveId ="";
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		if(bizDv.equals("I")) {
			irCurveId ="RF_KRW_BU";
		}else {
			irCurveId ="RF_KRW_KICS";
			isRiskFree = isRiskFreeString.equals("N");
		}
		return BizDiscFwdRateModel.getDiscFwdRateAsync(bssd, bizDv, irCurveId, isRiskFree, exe);
		
	}
	public static List<BizDiscFwdRateSce> getDiscFwdRateAsync(String bssd, String bizDv, String sceNo, List<BizDiscountRate> pastRate, ExecutorService exe) {
		
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discInternalDriverIsRiskFree", "Y");
		String irCurveId ="";
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		if(bizDv.equals("I")) {
			irCurveId ="RF_KRW_BU";
		}else {
			irCurveId ="RF_KRW_KICS";
			isRiskFree = isRiskFreeString.equals("N");
		}
		
		List<BizDiscountRateSce> dcntRateList = BizDcntRateDao.getTermStructureBySceNo(bssd, bizDv, irCurveId, sceNo);
//		logger.info("Job35 : {},{},{},{},{}", dcntRateList.size(), pastRate.size());
		
		return BizDiscFwdSceRateModel.getDiscFwdRateSceAsync(bssd, bizDv, irCurveId, isRiskFree, sceNo, dcntRateList, pastRate, exe);
//		return BizDiscFwdSceRateModel.getDiscFwdRateSceAsync(bssd, bizDv, irCurveId, isRiskFree, sceNo, exe);
		
	}
}
