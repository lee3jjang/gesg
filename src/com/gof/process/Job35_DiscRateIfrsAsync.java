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
