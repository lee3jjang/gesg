package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.model.BizDiscRateModel;
import com.gof.model.SmithWilsonModel;
import com.gof.util.ParamUtil;
/**
 *  <p> 현재의 공시기준이율과 국고채 금리간의 스프레드가 향후에도 유지된다는 가정하에서 공시이율 최선추정치 산출하는 모형임         
 *  <p>    1. 예측 Driver 로 국고채 선정  
 *  <p>    2. 국고채와 공시기준이율의 현재 스프레드 산출 
 *  <p>	     2.1 자산운용 수익률과 국고채의 통계모형에서 상수항임 
 *  <p>	     2.2 계수항은 1.0 으로 설정함.
 *  <p>      2.3.조정률은 현재의 조정륭을 적용함.
 *  <p>    3.다만 사용자가 통계모형 및 조정률을 입력한 경우 사용자 입력값이 우선함. 
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job34_BizDiscRateStatSpread {
	private final static Logger logger = LoggerFactory.getLogger(Job34_BizDiscRateStatSpread.class);
	
	/*public static List<BizDiscRateStat> getBaseDiscRateStatSpread(String bssd) {
		List<BizDiscRateStat> rst = BizDiscRateModel.getBaseDiscRateStat(bssd, "S");
		logger.info("Biz Applied Disc Rate Stat for Spread Model is calculated. {} Results are inserted into EAS_BIZ_APLY_DISC_RATE_STAT table", rst.size());
		return rst;
	}*/
	
	
	public static List<BizDiscRateStat> getBaseDiscRateStat(String bssd) {
		List<BizDiscRateStat> rstList = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat rst; 
		
		String isRiskFree    = ParamUtil.getParamMap().getOrDefault("discSpreadDriverIsRiskFree", "N");
		
		String matList    = ParamUtil.getParamMap().getOrDefault("discSpreadModelMaturityList", "M0001");
		
		List<String> matCdList = Arrays.asList(matList.split(","));
		String matCd = matList.split(",")[0];
		
		String indiVari = EBaseMatCd.getBaseMatCdEnum(matCdList.get(0)).getKTBCode();
		
		
//		사용자가 입력한 통계모형 결과 추출
		Map<String, BizDiscRateStatUd> userSpreadStatMap = DiscRateStatsDao.getUserDiscRateStat(bssd).stream()
																		 .filter(s->s.getApplyBizDv().equals("S"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		사용자가 조정률 조정하기 위해 입력한 경우에 사용함. 
		Map<String, BizDiscRateAdjUd> userSpreadAdjtMap = DiscRateStatsDao.getUserDiscRateAdj(bssd).stream()
																		 .filter(s->s.getApplBizDv().equals("S"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
		Map<String, Double> rfCurveMap = getDriverCurveMap(bssd, isRiskFree.equals("Y"));
		
//		공시이율 코드 정보 추출		
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, 0);
		List<String> discRateHisIntRate = discRateHis.stream().map(s ->s.getIntRateCd()).collect(Collectors.toList());
		
		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());

//		산출해야하는 공시이율 코드에 대해서 공시이율 이력 데이터가 없는 것을 제외함.
		List<DiscRateCalcSetting> calcSettingList = settingList.stream().filter(s -> s.isCalculable())
																		.filter(s->discRateHisIntRate.contains(s.getIntRateCd()))
																		.collect(Collectors.toList());
		
		List<DiscRateCalcSetting> nonCalcSettingList = settingList.stream().filter(s -> !s.isCalculable()).collect(Collectors.toList());
		
		
//		공시이율 중 금리모형에 연동하지 않는 유형은 현재 공시이율을 추선추정치로 설정함.
		for(DiscRateCalcSetting aa : nonCalcSettingList) {
			double nonCalcDiscRate = discRateHis.stream().filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
									.map(s-> s.getApplDiscRate()).findFirst().orElse(new Double(0.0));
			
			
			if(userSpreadStatMap.containsKey(aa.getIntRateCd())) {
				rst = userSpreadStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userSpreadAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userSpreadAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}
				rstList.add(rst);
			}
			else {
				double adj =   discRateHis.stream().filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
						.map(s-> s.getDiscRateAdjRate()).findFirst().orElse(new Double(1.0));
				
				if(userSpreadAdjtMap.containsKey(aa.getIntRateCd())) {
					adj = userSpreadAdjtMap.get(aa.getIntRateCd()).getApplAdjRate();
				}
				
				rst = new BizDiscRateStat();
				
				rst.setBaseYymm(bssd);
				rst.setApplyBizDv("S");
				
				rst.setIntRateCd(aa.getIntRateCd());
//				rst.setDepnVariable("BASE_DISC");
				rst.setIndpVariable(indiVari);
				
				rst.setAdjRate(adj);
				rst.setRegrCoef(0.0);
				rst.setRegrConstant(nonCalcDiscRate);
				rst.setRemark("");
				rst.setAvgMonNum(1.0);
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {}", rst);
				rstList.add(rst);
				
			}
		}
		
		for(DiscRateCalcSetting aa : calcSettingList) {
			if(userSpreadStatMap.containsKey(aa.getIntRateCd())) {
				rst = userSpreadStatMap.get(aa.getIntRateCd()).convert(bssd);
				if(userSpreadAdjtMap.containsKey(aa.getIntRateCd())) {
					rst.setAdjRate(userSpreadAdjtMap.get(aa.getIntRateCd()).getApplAdjRate());
				}
				rstList.add(rst);
			}
			else {			
				double rfRate = rfCurveMap.get(matCd);
				
				double calcBaseDiscRate = discRateHis.stream().filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
													 .map(s-> s.getBaseDiscRate()).findFirst().orElse(new Double(0.0));
				double adj =   discRateHis.stream().filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
													.map(s-> s.getDiscRateAdjRate()).findFirst().orElse(new Double(1.0));
				
				if(userSpreadAdjtMap.containsKey(aa.getIntRateCd())) {
					adj = userSpreadAdjtMap.get(aa.getIntRateCd()).getApplAdjRate();
				}
				
				rst = new BizDiscRateStat();
				
				rst.setBaseYymm(bssd);
				rst.setApplyBizDv("S");
				
				rst.setIntRateCd(aa.getIntRateCd());
//				rst.setDepnVariable("BASE_DISC");
				rst.setIndpVariable(indiVari);
				
				rst.setAdjRate(adj);
				rst.setRegrCoef(1.0);
				rst.setRegrConstant(calcBaseDiscRate - rfRate);
				rst.setRemark(rfRate +"_" + calcBaseDiscRate);
				rst.setAvgMonNum(1.0);
				rst.setVol(rfRate);
				rst.setLastModifiedBy("ESG");
				rst.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("RST : {}", rst);
				rstList.add(rst);
			}
		}
		logger.info("Biz Applied Disc Rate Stat for Spread Model is calculated. {} Results are inserted into EAS_BIZ_APLY_DISC_RATE_STAT table", rstList.size());
		return  rstList;
	}
	
	
	/**
	 * 현재 월말기준의 금리 데이터 추출
	 * 무위험인 경우는 A100 에서 금리를 추출하면, 조정 무위험인 경우는 할인율 금리를 추출함.
	 * 
	*/
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
