package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscRateAdjUd;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscRateStatUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateSce;
import com.gof.entity.DiscRateStats;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

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

public class BizDiscRateModel {
	private final static Logger logger = LoggerFactory.getLogger(BizDiscRateModel.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	�������� ���� �ݸ� �Ⱓ ������ �ݿ��Ͽ� �������� �ּ������� ������.
	public static List<BizDiscRate> getBizDiscRateAsync(String bssd, String bizDv,ExecutorService exe) {
		return getDiscRateAsync(bssd, bizDv, exe).stream().map(s->s.convertTo()).collect(Collectors.toList());
		
	}
	
//	�������� ���� �ݸ� �Ⱓ ������ �ݿ��Ͽ� �������� �ּ������� ������.
	public static List<DiscRate> getDiscRateAsync(String bssd, String bizDv, ExecutorService exe) {
		String isRiskFreeString    = ParamUtil.getParamMap().getOrDefault("discIntenalDriverIsRiskFree", "N");
		boolean	isRiskFree = isRiskFreeString.equals("Y");
		
		Map<String, Map<String, Double>> curveHistoryRateByMaturity = new HashMap<String, Map<String, Double>>();

//		Refactoring : DB ȣ���� �ּ�ȭ�� ���� �޼��� ���� ����
		Map<String, Map<String, Double>> termStructureByDate = getAllDriverCurveMap(bssd, -36, isRiskFree);
		for(EBaseMatCd aa : EBaseMatCd.values()) {
//			curveHistoryRateByMaturity.put(aa.name(), getAllDriverCurveMapByMatCd(bssd, -36, aa.name(), isRiskFree));
			curveHistoryRateByMaturity.put(aa.name(), getAllDriverCurveMapByMatCd1(bssd, -36, aa.name(), termStructureByDate));
		}
		
		List<String> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
//														.filter(s ->s.isCalculable())
														.map(s ->s.getIntRateCd())
														.collect(Collectors.toList());

		List<BizDiscRateStat> bizStatList = DiscRateStatsDao.getBizDiscRateStat(bssd).stream()
															.filter(s-> s.getApplyBizDv().equals(bizDv))
															.filter(s-> discSetting.contains(s.getIntRateCd()))
//															.filter(s-> s.getIntRateCd().equals("2305"))
															.collect(Collectors.toList());

		
		List<CompletableFuture<List<DiscRate>>> sceJobFutures =  bizStatList.stream()
				.map(stat -> CompletableFuture.supplyAsync(() -> getDiscRateByStat(stat, bssd, curveHistoryRateByMaturity), exe))
				.collect(Collectors.toList());

		List<DiscRate> rst = sceJobFutures.stream().map(CompletableFuture::join).flatMap(s ->s.stream()).collect(Collectors.toList());
		
		logger.info("Disc Rate for Model {} is calculated. {} Results are inserted into EAS_DISC_RATE table", bizDv, rst.size());
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
	
	
	// �����ڵ庰 ���� �� �̷� Time Series �� �ʿ���. ..Map<���ؿ�, ����������>
	// ���� �ݸ��� Term Structure ���� �����ϰ�, �̷� �ݸ��� ���� �ݸ��Ⱓ�������� Forwarding �� �� ������.
	
	private static Map<String, Double> getAllDriverCurveMapByMatCd(String bssd,  int monthNum, String matCd, boolean isRiskFree){
		
		Map<String, Double> rstMap = new HashMap<String,  Double>();
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		
		matCdRateMap = getDriverCurveMap(bssd, isRiskFree);
		
//		�̷������� �ݸ� (forward �� ������)
		rstMap = FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, matCd); 
		
//		���� ���⺰ �ݸ��� ���ڷ� ����
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			matCdRateMap = getDriverCurveMap(prevBssd, isRiskFree);
			rstMap.put(prevBssd, matCdRateMap.getOrDefault(matCd, new Double(0.0)));
		}
		return rstMap;
	}
	
	private static Map<String, Double> getAllDriverCurveMapByMatCd1(String bssd,  int monthNum, String matCd, Map<String, Map<String, Double>> rstInput){
		
//		Map<String, Map<String, Double>> rstInput = getAllDriverCurveMap(bssd, monthNum, isRiskFree);
		
		Map<String, Double> rstMap = new HashMap<String,  Double>();
		Map<String, Double> matCdRateMap = new HashMap<String,  Double>();
		
		matCdRateMap = rstInput.get(bssd);
		
//		�̷������� �ݸ� (forward �� ������)
		rstMap = FinUtils.getForwardRateByMaturityZZ(bssd, matCdRateMap, matCd); 
		
//		���� ���⺰ �ݸ��� ���ڷ� ����
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			matCdRateMap = rstInput.get(prevBssd);
			rstMap.put(prevBssd, matCdRateMap.getOrDefault(matCd, new Double(0.0)));
		}
		return rstMap;
	}

	private static Map<String, Map<String, Double>> getAllDriverCurveMap(String bssd,  int monthNum, boolean isRiskFree){
		Map<String, Map<String, Double>> rst = new HashMap<String, Map<String, Double>>();
		for(int k= monthNum; k < 1; k++) {
			String prevBssd = FinUtils.addMonth(bssd, k);
			rst.put(prevBssd, getDriverCurveMap(prevBssd, isRiskFree));
		}
		return rst;
	}
	
	
//	�־��� ���ڷ� ������ Map<����, ���� ������ �����> 
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
