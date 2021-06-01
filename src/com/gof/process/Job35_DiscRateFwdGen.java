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
 *  <p> ���� ����(IFRS)��  �������� ���� ����
 *  <p> ���λ�������� �����Ͽ�, �ڻ������ͷ�, �ܺ���ǥ�ݸ�, ���ñ����������� �������� ������ҿ� ����ä �ݸ�����  ��� �м� ����� ������.
 *  <p>    1. ���� Driver �� ����ä 1����, 3�⹰, 5�⹰ ���� �������� ���������� ���� 
 *  <p>    2. �ڻ������ͷ�, �ܺ���ǥ�ݸ�, ���ñ����������� �������� ���Ӻ����� ����    
 *  <p>    3. ���� Driver ��  ���Ӻ������� ���� ������ ����( ���� �̵���� 12m, 24m, 36m ���� �����Ͽ� ������� �ִ��� ������ ������.
 *  <p>    4. �������� ������ �ݸ� ������ �����Ͽ� �������� �ּ�����ġ�� ������.
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
