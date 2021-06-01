package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BizDiscFwdRateSceDao;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.entity.BizDiscFwdRateSce;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateSce;
import com.gof.util.HibernateUtil;

/**
 *  <p> KIcS ������  �������� ���� ����
 *  <p> KIcS ���� �����ϴ� ��������� ���ñ������� ����� �ܺ� ��ǥ�ݸ��� �����ϰ� �ڻ��� ���ͷ� ���θ� �����.
 *  <p>    1. �ڻ��� ���ͷ��� ������������ ������ 3�� ������� ������ ����
 *  <p>    2. �ڻ��� ���ͷ��� �̷� ����ġ�� ���� ������ �ݸ��� 1M Forward ���� ���ڰ�������� �����Ͽ� ������.  
 *  <p>	     2.1 ������ �������� �������� �����Ƿ� ���������� KTB1M, ����׿� ���ڰ������, ����׿� 1.0 �� ������. 
 *  <p>    3. ���� ����ڰ� ������ �������� ������ �켱������ ������.  
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class BizDiscRateModelNew {
	private final static Logger logger = LoggerFactory.getLogger(BizDiscRateModelNew.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	�������� ���� �ݸ� �Ⱓ ������ �ݿ��Ͽ� �������� �ּ������� ������.
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, String bizDv, String irCurveId, boolean isRiskFree, ExecutorService exe) {
		return getDiscRateAsync(bssd, bizDv, irCurveId,isRiskFree, exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
//	�������� ���� �ݸ� �Ⱓ ������ �ݿ��Ͽ� �������� �ּ������� ������.
	public static List<DiscRate> getDiscRateAsync(String bssd, String bizDv,  String irCurveId, boolean isRiskFree, ExecutorService exe) {
//		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
//		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		List<BizDiscFwdRateSce> fwdRateList = BizDiscFwdRateSceDao.getForwardRates(bssd, bizDv, irCurveId, "0");

		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2302"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRate>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateByStat(stat, bssd, fwdRateList), exe))
				.collect(Collectors.toList());

		List<DiscRate> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		logger.info("Disc Rate for Model {} is calculated. {} Results are inserted into EAS_DISC_RATE_ table", bizDv, rst.size());
		return rst;
	}
	public static List<DiscRateSce> getDiscRateSceAsync(String bssd, String bizDv,  String irCurveId,  boolean isRiskFree, ExecutorService exe) {
//		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
//		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		List<BizDiscFwdRateSce> fwdRateList = BizDiscFwdRateSceDao.getForwardRatesAll(bssd, bizDv, irCurveId);

		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2302"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRateSce>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateSceByStat(stat, bssd, fwdRateList), exe))
				.collect(Collectors.toList());

		List<DiscRateSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		logger.info("Disc Rate for Model {} is calculated. {} Results are inserted into EAS_DISC_RATE_ table", bizDv, rst.size());
		return rst;
	}
	
	public static List<DiscRateSce> getDiscRateAsync(String bssd, String bizDv,  String irCurveId, String sceNo, boolean isRiskFree, ExecutorService exe) {
//		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
//		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		List<BizDiscFwdRateSce> fwdRateList = BizDiscFwdRateSceDao.getForwardRates(bssd, bizDv, irCurveId, sceNo);

		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2302"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRateSce>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateSceByStat(stat, bssd, sceNo, fwdRateList), exe))
				.collect(Collectors.toList());

		List<DiscRateSce> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		logger.info("Disc Rate Sce {} for Model {} is calculated. {} Results are inserted into EAS_DISC_RATE_SCE table", sceNo, bizDv, rst.size());
		return rst;
	}
	
	
	private static List<DiscRate> getDiscRateByStat(BizDiscRateStat stat, String bssd, List<BizDiscFwdRateSce> fwdRateList) {
		List<DiscRate> rstList = new ArrayList<DiscRate>();
		DiscRate temp;
		double intRate =0.0;
		String fwdMatCd ="";
		
		Map<String, Double> fwdMap  = fwdRateList.stream().filter(s ->s.getMatCd().equals(stat.getIndiVariableMatCd()))
												.collect(Collectors.toMap(s->"M"+String.format("%04d", Integer.valueOf(s.getFwdNo())), s->s.getAvgFwdRate()));
		
		
		for(int i =0; i< 1200; i++) {
			fwdMatCd = "M" +String.format("%04d", i+1);
			intRate = fwdMap.getOrDefault(fwdMatCd, new Double(0.0));
			
//				logger.info("asdfasdf : {},{},{},{}", fwdMatCd, intRate);
			
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
	
	private static List<DiscRateSce> getDiscRateSceByStat(BizDiscRateStat stat, String bssd, String sceNo, List<BizDiscFwdRateSce> fwdRateList) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		double intRate =0.0;
		String fwdMatCd ="";
		
		Map<String, Double> fwdMap  = fwdRateList.stream().filter(s ->s.getMatCd().equals(stat.getIndiVariableMatCd()))
												.collect(Collectors.toMap(s->"M"+String.format("%04d", Integer.valueOf(s.getFwdNo())), s->s.getAvgFwdRate()));
		
		
		for(int i =0; i< 1200; i++) {
			fwdMatCd = "M" +String.format("%04d", i+1);
			intRate = fwdMap.getOrDefault(fwdMatCd, new Double(0.0));
			
//				logger.info("asdfasdf : {},{},{},{}", fwdMatCd, intRate);
			
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
	private static List<DiscRateSce> getDiscRateSceByStat(BizDiscRateStat stat, String bssd, List<BizDiscFwdRateSce> fwdRateList) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		double intRate =0.0;
		String fwdMatCd ="";
		
		Map<String, List<BizDiscFwdRateSce>> fwdListBySce = fwdRateList.stream().collect(Collectors.groupingBy(s->s.getSceNo(), Collectors.toList()));
		
		for(Map.Entry<String, List<BizDiscFwdRateSce>> entry : fwdListBySce.entrySet()) {
			Map<String, Double> fwdMap  = entry.getValue().stream().filter(s ->s.getMatCd().equals(stat.getIndiVariableMatCd()))
						.collect(Collectors.toMap(s->"M"+String.format("%04d", Integer.valueOf(s.getFwdNo())), s->s.getAvgFwdRate()));
			for(int i =0; i< 1200; i++) {
				fwdMatCd = "M" +String.format("%04d", i+1);
				intRate = fwdMap.getOrDefault(fwdMatCd, new Double(0.0));
				
//				logger.info("asdfasdf : {},{},{},{}", fwdMatCd, intRate);
				
				temp = new DiscRateSce();
				
				temp.setBaseYymm(bssd);
				temp.setIntRateCd(stat.getIntRateCd());
				temp.setDiscRateCalcTyp(stat.getApplyBizDv());
				temp.setSceNo(entry.getKey());
				temp.setMatCd(fwdMatCd);
				temp.setBaseDiscRate(stat.getRegrCoef() * intRate + stat.getRegrConstant());
				temp.setAdjRate(stat.getAdjRate());
				
				temp.setDiscRate(temp.getBaseDiscRate() * temp.getAdjRate());
				
				temp.setVol(intRate);
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(temp);
			}
		}
		
//		rstList.stream().forEach(s->logger.debug("Accrete  Rate for IFRS17 Result : {}", s.toString()));
		return rstList;
	}
}
