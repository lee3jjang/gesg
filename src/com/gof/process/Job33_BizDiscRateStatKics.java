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

import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.entity.BizDiscRateAdjUd;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscRateStatUd;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.InvestManageCostUd;
import com.gof.enums.EBaseMatCd;
import com.gof.model.BizDiscRateModel;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> KIcS 기준의  공시이율 추정 모형
 *  <p> KIcS 에서 제시하는 방법론으로 공시기준이율 산출시 외부 지표금리는 배제하고 자산운용 수익률 요인만 고려함.
 *  <p>    1. 자산운용 수익률과 공시이율과의 비율의 1년 평균으로 조정률 결정
 *  <p>    2. 자산운용 수익률의 미래 추정치는 조정 무위험 금리의 1M Forward 에서 투자관리비용을 차감하여 결정함.  
 *  <p>	     2.1 별도의 통계모형을 적용하지 않으므로 독립변수는 KTB1M, 상수항에 투자관리비용, 계수항에 1.0 을 설정함. 
 *  <p>    3. 만일 사용자가 지정한 통계모형이 있으면 우선적으로 적용함.  
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Job33_BizDiscRateStatKics {
	private final static Logger logger = LoggerFactory.getLogger(Job33_BizDiscRateStatKics.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	
	public static List<BizDiscRateStat> getBaseDiscRateStat(String bssd) {
		List<BizDiscRateStat> rstList = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat rst; 
		
//		String isRiskFree    = ParamUtil.getParamMap().getOrDefault("discKicsDriverIsRiskFree", "N");
		
		String matList    = ParamUtil.getParamMap().getOrDefault("discKicsModelMaturityList", "M0001");
		List<String> matCdList = Arrays.asList(matList.split(","));
		
		String indiVari = EBaseMatCd.getBaseMatCdEnum(matCdList.get(0)).getKTBCode();
		
		
//		사용자가 입력한 통계모형 결과 추출
		Map<String, BizDiscRateStatUd> userKicsStatMap = DiscRateStatsDao.getUserDiscRateStat(bssd).stream()
																		 .filter(s->s.getApplyBizDv().equals("K"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		사용자가 조정률 조정하기 위해 입력한 경우에 사용함. 
		Map<String, BizDiscRateAdjUd> userKicsAdjtMap = DiscRateStatsDao.getUserDiscRateAdj(bssd).stream()
																		 .filter(s->s.getApplBizDv().equals("K"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));

//		투자관리 비용 입수
//		List<InvestManageCostUd> investCostList = DaoUtil.getEntities(InvestManageCostUd.class, new HashMap<String, Object>());
//		Map<String, Double> investCostMap = investCostList.stream()
//														  .filter(s->s.getBaseYymm().equals(bssd))
//														  .findFirst().orElse(new InvestManageCostUd())
//														  .getInvCostRateByAccount();
		
//		20190513 수정 : Current User Defined InvestCost before bssd
		InvestManageCostUd investCostUd = DiscRateStatsDao.getCurrentUserInvMgtCost(bssd);
		Map<String, Double> investCostMap = investCostUd.getInvCostRateByAccount();					  //투자 비용율은 3년 평균을 사용자가 계산하여 입력한 것임. ( 입력된 값을 그대로 사용함)
		
		List<InvestManageCostUd> assetYieldList = DiscRateStatsDao.getUserInvMgtCostList(bssd, -12);  //201907수정 : 과거 3년-> 1년  공시이율 조정률 평균을 산출하기 위한 데이터임.
		
//		공시이율 코드 정보 추출
		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());

		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, -12);   					//201907수정 : 과거 3년-> 1년  공시이율 조정률 평균을 산출하기 위한 데이터임.
//		discRateHis.forEach(s-> logger.info("aaa : {},{},{}", s.getIntRateCd(), s.getBaseYymm(), s.getApplDiscRate()));
		
		List<String> discRateHisIntRate = discRateHis.stream().map(s ->s.getIntRateCd()).collect(Collectors.toList());
		
//		산출해야하는 공시이율 코드에 대해서 공시이율 이력 데이터가 없는 것을 제외함.
		List<DiscRateCalcSetting> calcSettingList = settingList.stream().filter(s -> s.isCalculable())
																		.filter(s->discRateHisIntRate.contains(s.getIntRateCd()))
																		.collect(Collectors.toList());
		
		List<DiscRateCalcSetting> nonCalcSettingList = settingList.stream().filter(s -> !s.isCalculable())
																			.collect(Collectors.toList());

		
//      공시이율 중 금리모형에 연동하지 않는 유형은 현재 공시이율을 추선추정치로 설정함. 		
		for(DiscRateCalcSetting aa : nonCalcSettingList) {
			
			if(userKicsStatMap.containsKey(aa.getIntRateCd())) {
				rst = userKicsStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userKicsAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userKicsAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}

				rstList.add(rst);
			}
			else {
//				현재시점의 조정률 적용
				double adj   = discRateHis.stream().filter(s-> bssd.equals(s.getBaseYymm()))
						.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
						.findFirst().orElse(new DiscRateHis())
						.getDiscRateAdjRate();
				
				if(userKicsAdjtMap.containsKey(aa.getIntRateCd())) {
					adj = userKicsAdjtMap.get(aa.getIntRateCd()).getApplAdjRate();
				}
				rst = new BizDiscRateStat();
				
				double nonCalcDiscRate = discRateHis.stream()
						.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
						.filter(s -> bssd.equals(s.getBaseYymm()))
						.map(s-> s.getApplDiscRate())
						.findFirst().orElse(new Double(0.0));
				
//				System.out.println("nonCalc: "+ nonCalcDiscRate +" ___ adj : " + adj);
				
				rst.setBaseYymm(bssd);
				rst.setApplyBizDv("K");
				
				rst.setIntRateCd(aa.getIntRateCd());
//				rst.setDepnVariable("BASE_DISC");
				rst.setIndpVariable(indiVari);
				
				rst.setAdjRate(adj);
				rst.setRegrCoef(0.0);
//				rst.setRegrConstant(-1.0 * invCost);
//				rst.setRegrConstant(0.0);
				rst.setRegrConstant(nonCalcDiscRate);
				rst.setRemark("");
				
				
				rst.setAvgMonNum(1.0);
				rst.setVol(0.0);
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {}", rst);
				rstList.add(rst);
			}
		}
		
		
		for(DiscRateCalcSetting aa : calcSettingList) {
//			logger.info("intRateCd : {}", aa.getIntRateCd());
			if(userKicsStatMap.containsKey(aa.getIntRateCd())) {
				rst = userKicsStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userKicsAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userKicsAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}
				rstList.add(rst);
			}
			else {
				Map<String, Double> assetYieldMap = assetYieldList.stream().collect(Collectors.toMap(s->s.getBaseYymm(), s -> s.getLongMgtAsstYield()));  
				if(aa.getAcctDvCd().contains("8100")) {
					assetYieldMap = assetYieldList.stream().collect(Collectors.toMap(s->s.getBaseYymm(), s -> s.getPensMgtAsstYield()));
				}
				
				double avgAdj =0.0;
				int cnt=0;
				double invCost = investCostMap.getOrDefault(aa.getAcctDvCd(), new Double(0.0));
				double asstYield =0.0;
				
				List<DiscRateHis> filteredList   = discRateHis.stream().filter(s -> s.getIntRateCd().equals(aa.getIntRateCd())).collect(Collectors.toList());
				for(DiscRateHis zz : filteredList) {
//					if(zz.getMgtAsstYield()!=0) {
//						avgAdj = avgAdj + zz.getApplDiscRate() / zz.getMgtAsstYield();
//						cnt =cnt+1;
//					}
//					201907 수정 !!!
					if(assetYieldMap.containsKey(zz.getBaseYymm())) {
						asstYield = assetYieldMap.get(zz.getBaseYymm());
						if(asstYield!=0) {
							avgAdj = avgAdj + zz.getApplDiscRate() / asstYield ;
							cnt =cnt+1;
						}
						else {
						}
					}
				}
	
//				logger.info("filter Size : {} ,{}", filteredList.size(), cnt);
				if(cnt !=0) {
					avgAdj = avgAdj / cnt ;
				}
				else {
					avgAdj =1.0;
				}
				
				if(userKicsAdjtMap.containsKey(aa.getIntRateCd())) {
					avgAdj = userKicsAdjtMap.get(aa.getIntRateCd()).getApplAdjRate();
				}
				
				rst = new BizDiscRateStat();
				
				rst.setBaseYymm(bssd);
				rst.setApplyBizDv("K");
				
				rst.setIntRateCd(aa.getIntRateCd());
//				rst.setDepnVariable("BASE_DISC");
				rst.setIndpVariable(indiVari);
				
				rst.setAdjRate(avgAdj);
				rst.setRegrCoef(1.0);
				rst.setRegrConstant(-1.0* invCost);
				rst.setRemark("");
				rst.setAvgMonNum(1.0);
				rst.setVol(0.0);
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {}", rst);
				rstList.add(rst);
			}
		}
		logger.info("Biz Applied Disc Rate Stat for Kics is calculated. {} Results are inserted into EAS_BIZ_APLY_DISC_RATE_STAT table", rstList.size());
		return  rstList;
	}
	// 20200529 :ADD
	public static List<BizDiscRateStat> getBaseDiscRateStat1(String bssd) {
		List<BizDiscRateStat> rstList = new ArrayList<BizDiscRateStat>();
		List<Object[]> tempList = DiscRateDao.getKicsAvgDiscRateStat(bssd);
//		List<Object[]> tempList =  DiscRateDao.getKicsAvgDiscRateStat(bssd);
		logger.info("job33 _bssd: {},{}", bssd);
		logger.info("job33 : {},{}", tempList.size());
//		logger.info("job33 : {},{}", tempList.size());
		for(Object[] obj : tempList){
				BizDiscRateStat rst = new BizDiscRateStat(); 
				logger.info("job33_obj : {},{},{}",obj.length,  obj[1].toString(), obj);
//				rst.setBaseYymm(bssd);	
				rst.setBaseYymm(obj[0].toString());	
				rst.setApplyBizDv("K");
				
				rst.setIntRateCd(obj[1].toString());
				rst.setDepnVariable(" ");
				rst.setIndpVariable(" ");
				
				rst.setAvgMonNum(0.0);
				
				rst.setRegrConstant(Double.valueOf(obj[2].toString()));
				/**2020.06.03 - ST.LEE 컬럼변경 - 이상진주임요청 사항*/
				//rst.setRegrCoef(1.0);
				//rst.setAdjRate(Double.valueOf(obj[3].toString()));
				/**2020.09.11 - ST.LEE 컬럼변경 - 이상진주임요청 사항*/
				//rst.setRegrCoef(Double.valueOf(obj[3].toString()));
				rst.setAdjRate(Double.valueOf(obj[3].toString()));
				/**2020.09.11 - ST.LEE 컬럼변경 - 이상진주임요청 사항*/
				//rst.setAdjRate(1.0);
				rst.setRegrCoef(Double.valueOf(obj[4].toString()));
				
				rst.setVol(0.0);
				rst.setRemark("");
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {}", rst);
				rstList.add(rst);
		}
		
		return  rstList;
	}
}
