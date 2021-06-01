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
 *  <p> QIS ������  �������� ���� ����
 *  <p> QIS �� ������ ������� ����������� �񱳰��ɼ� Ȯ���� ���� �ڻ�����ͷ� ������ �����ϰ� ����ä �ݸ��� ������������ �������� ��� �м� ����� ������.
 *  <p>    1. ���� Driver �� ����ä 3�⹰�� 3���� ���, 3�� ����� ����
 *  <p>    2. ������ ����    
 *  <p>	     2.1 0.26 * 3M ��� ����ä 3�� +0.5 * 3Y ��� ����ä 3��  + 1.51% 
 *  <p>    3. ���� Driver �� ����ä�� �ó����� ({@link Job14_EsgScenario} �� �������� �����Ͽ� �������� ������. 
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
