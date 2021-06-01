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

public class BizDiscRateModelNew {
	private final static Logger logger = LoggerFactory.getLogger(BizDiscRateModelNew.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	통계모형에 현행 금리 기간 구조를 반영하여 공시이율 최선추정을 산출함.
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, String bizDv, String irCurveId, boolean isRiskFree, ExecutorService exe) {
		return getDiscRateAsync(bssd, bizDv, irCurveId,isRiskFree, exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
//	통계모형에 현행 금리 기간 구조를 반영하여 공시이율 최선추정을 산출함.
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
