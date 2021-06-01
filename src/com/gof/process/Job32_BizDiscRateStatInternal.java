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
		
		
//		사용자가 입력한 통계모형 결과 추출
		Map<String, BizDiscRateStatUd> userInternalStatMap = DiscRateStatsDao.getUserDiscRateStat(bssd).stream()
																		 .filter(s->s.getApplyBizDv().equals("I"))
//																		 .filter(s->s.getIndpVariable().equals("BASE_DISC"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		사용자가 조정률 조정하기 위해 입력한 경우에 사용함. 
		Map<String, BizDiscRateAdjUd> userInternalAdjtMap = DiscRateStatsDao.getUserDiscRateAdj(bssd).stream()
																		 .filter(s->s.getApplBizDv().equals("I"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		과거 데이터를 이용하여 산출한 통계모형
		List<DiscRateStats> internalStatList = DiscRateStatsDao.getDiscRateStatsForIfrs(bssd);
		

		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());
		Map<String, DiscRateCalcSetting> settingMap = settingList.stream().collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
												
//		공이시율 과거 이력 정보 추출 (과거 36개월 추출함) 
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, 0);
		Map<String, DiscRateHis> discRateHisMap = discRateHis.stream().collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));

//		이력이 안들어있는 것들은 제외함.
		List<DiscRateStats> internalStatListFitered = internalStatList.stream().filter(s-> discRateHisMap.containsKey(s.getIntRateCd()))
																			   .collect(Collectors.toList());
		
//      공시이율 통계모형에서 현행 공시이율의 수준을 Fitting 하도록 상수항을 조정함. 		
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
			
				
//			사용자가 입력한 통계모형이 있으면 반영하고, 사용자가 입력한 조정률이 있는 경우 추가적으로 조정함.
			if(userInternalStatMap.containsKey(aa.getIntRateCd())) {
				rst = userInternalStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userInternalAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userInternalAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}
				rstList.add(rst);
			}
			else {
				
//				현재시점의 조정률 적용
				double adj   = discRateHis.stream().filter(s-> bssd.equals(s.getBaseYymm()))
							.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
							.findFirst().orElse(new DiscRateHis())
							.getDiscRateAdjRate();
				
//				사용자가 입력한 조정률이 있는 경우 반영함.
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
