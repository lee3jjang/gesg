package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BizDiscRateAdjUd;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscRateStatUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateStats;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.model.BizDiscRateModel;
import com.gof.model.SmithWilsonModel;
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

public class Job32_BizDiscRateStatInternal {
	private final static Logger logger = LoggerFactory.getLogger(Job32_BizDiscRateStatInternal.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	/*public static List<BizDiscRateStat> getBaseDiscRateStatInternal(String bssd) {
		List<BizDiscRateStat> rst = BizDiscRateModel.getBaseDiscRateStat(bssd, "I");
		logger.info("Biz Applied Disc Rate Stat for IFRS Model is calculated. {} Results are inserted into EAS_BIZ_APLY_DISC_RATE_STAT table", rst.size());
		return rst;
 
	}*/
	
	public static List<BizDiscRateStat> getBaseDiscRateStat(String bssd) {
		List<BizDiscRateStat> rstList = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat rst;
		
		String isRiskFree    = ParamUtil.getParamMap().getOrDefault("discInternalDriverIsRiskFree", "Y");
		Map<String, Map<String, Double>> rfRateMap = getPastDriverCurveMap(bssd, -36, isRiskFree.equals("Y"));
//		rfRateMap.entrySet().forEach(entry -> logger.info("qqq : {},{},{}", entry.getKey(), entry.getValue()));
		
		
//		����ڰ� �Է��� ������ ��� ����
		Map<String, BizDiscRateStatUd> userInternalStatMap = DiscRateStatsDao.getUserDiscRateStat(bssd).stream()
																		 .filter(s->s.getApplyBizDv().equals("I"))
//																		 .filter(s->s.getIndpVariable().equals("BASE_DISC"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		����ڰ� ������ �����ϱ� ���� �Է��� ��쿡 �����. 
		Map<String, BizDiscRateAdjUd> userInternalAdjtMap = DiscRateStatsDao.getUserDiscRateAdj(bssd).stream()
																		 .filter(s->s.getApplBizDv().equals("I"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		���� �����͸� �̿��Ͽ� ������ ������
		List<DiscRateStats> internalStatList = DiscRateStatsDao.getDiscRateStatsForIfrs(bssd);
		

		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());
		Map<String, DiscRateCalcSetting> settingMap = settingList.stream().collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
												
//		���̽��� ���� �̷� ���� ���� (���� 36���� ������) 
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, 0);
		Map<String, DiscRateHis> discRateHisMap = discRateHis.stream().collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));

//		�̷��� �ȵ���ִ� �͵��� ������.
		List<DiscRateStats> internalStatListFitered = internalStatList.stream().filter(s-> discRateHisMap.containsKey(s.getIntRateCd()))
																			   .collect(Collectors.toList());
		
//      �������� ���������� ���� ���������� ������ Fitting �ϵ��� ������� ������. 		
		for(DiscRateStats aa : internalStatList) {
			double avgRate =0.0;
			int cnt =0;
			if(settingMap.containsKey(aa.getIntRateCd()) && settingMap.get(aa.getIntRateCd()).isCalculable()) {
				for(int j= 0; j < aa.getAvgNum(); j++) {
					avgRate = avgRate + rfRateMap.get(FinUtils.addMonth(bssd, -1 * j)).getOrDefault(aa.getIndiVariableMatCd(), new Double(0.0));
					cnt =cnt+1;
//					logger.info("kkk : {},{},{},{}", aa.getIntRateCd(), aa.getIndiVariableMatCd(), FinUtils.addMonth(bssd, -1 * j), rfRateMap.get(FinUtils.addMonth(bssd, -1 * j)).getOrDefault(aa.getIndiVariableMatCd(), new Double(0.0)));
				}
				if(cnt ==0) {
					avgRate =0.0;
				}
				avgRate = avgRate / cnt ; 
			}
			
				
//			����ڰ� �Է��� �������� ������ �ݿ��ϰ�, ����ڰ� �Է��� �������� �ִ� ��� �߰������� ������.
			if(userInternalStatMap.containsKey(aa.getIntRateCd())) {
				rst = userInternalStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userInternalAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userInternalAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}
				rstList.add(rst);
			}
			else {
				
//				��������� ������ ����
				double adj   = discRateHis.stream().filter(s-> bssd.equals(s.getBaseYymm()))
							.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
							.findFirst().orElse(new DiscRateHis())
							.getDiscRateAdjRate();
				
//				����ڰ� �Է��� �������� �ִ� ��� �ݿ���.
				if(userInternalAdjtMap.containsKey(aa.getIntRateCd())) {
					adj = userInternalAdjtMap.get(aa.getIntRateCd()).getApplAdjRate();
				}
				
				rst = new BizDiscRateStat();
				
				rst.setBaseYymm(bssd);
				rst.setApplyBizDv("I");
				
				rst.setIntRateCd(aa.getIntRateCd());
//				rst.setDepnVariable(aa.getDepnVariable());
				rst.setIndpVariable(aa.getIndpVariable());
				
				rst.setAdjRate(adj);
				
				rst.setRegrCoef(aa.getRegrCoef());
				if(discRateHisMap.containsKey(aa.getIntRateCd())) {
					rst.setRegrConstant(discRateHisMap.get(aa.getIntRateCd()).getBaseDiscRate() - avgRate * aa.getRegrCoef());
				}
				else {
					rst.setRegrConstant(aa.getRegrCoef());
				}
				
				rst.setRemark("");
				rst.setAvgMonNum(aa.getAvgNum());
				rst.setVol(avgRate);
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {},{}, {},{}, {}", rst.getIntRateCd(), avgRate, rst.getRegrCoef(), rst.getRegrConstant(), discRateHisMap.get(aa.getIntRateCd()).getBaseDiscRate());
				rstList.add(rst);
			}
		}
		logger.info("Biz Applied Disc Rate Stat for IFRS is calculated. {} Results are inserted into EAS_BIZ_APLY_DISC_RATE_STAT table", rstList.size());
		return  rstList;
	}
	
	private static Map<String, Map<String, Double>> getPastDriverCurveMap(String bssd,  int monthNum, boolean isRiskFree){
		Map<String, Map<String, Double>> rstMap = new HashMap<String, Map<String, Double>>();
		for(int k= monthNum; k < 1; k++) {
			rstMap.put(FinUtils.addMonth(bssd, k), getDriverCurveMap(FinUtils.addMonth(bssd, k), isRiskFree));
		}
		return rstMap;
	}
	
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
				if(dcntRateList.size() ==0) {
					logger.warn("Size of BottomUp Discount Rate for RF_KRW_BU at {} is zero. You should run Job22 for BottomUp Discount Rate", bssd );
				}
				
				return dcntRateList.stream().collect(Collectors.toMap(s->s.getMatCd(), s-> s.getRiskAdjRfRate()))	;
		}
	}
}
