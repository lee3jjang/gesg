package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateStats;
import com.gof.entity.DiscRateStatsAssetYield;
import com.gof.entity.DiscRateStatsExtIr;
import com.gof.entity.DiscRateWght;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LinearRegResult;
import com.gof.entity.RAcctYieldHis;
import com.gof.entity.RCurveHis;
import com.gof.entity.RExtIrHis;
import com.gof.entity.UserDiscRateAsstRevnCumRate;
import com.gof.entity.UserDiscRateExBaseIr;
import com.gof.model.GongsiStatKicsModel;
import com.gof.model.GongsiStatModel;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> 공시이율 추정을 위한 통계분석 모형임         
 *  <p> 공시이율 산출시 기준이 되는 공시기준이율은 자산운용수익률과 외부 지표금리의 가중평균으로 산출됨 ( 조정률이 반영되어 실제 공시이율이 산출됨)
 *  <p> 이는 미래시점의 공시이율을 추정하기 위해서는 미래시점의 자산운용수익률, 외부 지표금리를 예측해야한다는 것을 의미함. 
 *  
 *  <p> 미래 시점의 공시이율 추정을 위해 
 *  <p>    1. 예측 Driver 로 국고채 선정  
 *  <p>    2. 국고채와 공시이율 산출의 주요 Factor 인 자산운용 수익률, 외부지표금리간 인과관계를 통계적으로 분석함. 
 *  <p>	     2.1 자산운용 수익률과 국고채의 시계열 데이터를 통해 통계모형 생성 
 *  <p>	     2.2 외부 지표금리와  국고채의 시계열 데이터를 통해 통계모형 생성
 *  <p>    3. 예측 Driver 인 국고채의 시나리오 ({@link Job14_EsgScenario} 를 통계모형에 적용하여 미래 시점의 자산운용수익률 , 외부지표금리 예측
 *  <p>    4. 예측된 자산운용수익률, 외부지표금리를 이용하여 공시기준이율과 공시이율을 산출함. ( 미래에 적용할 조정률은 현재 조정률이 변동 없다고 가정함.)
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Job31_AccreteRateStats {
	private final static Logger logger = LoggerFactory.getLogger(Job31_AccreteRateStats.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	

	public static List<DiscRateStats> getExtIrStatForIntCode(String bssd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		
		List<DiscRateWght> discWght = DaoUtil.getEntities(DiscRateWght.class, param);
		List<DiscRateStatsExtIr> extIrStatList = DaoUtil.getEntities(DiscRateStatsExtIr.class, param);
		
		Map<String, DiscRateStatsExtIr> extIrStatMap = extIrStatList.stream().collect(Collectors.toMap(s->s.getExtIrCd(), Function.identity()));
		
		List<DiscRateStats> rstList = new ArrayList<DiscRateStats>();
		DiscRateStats temp; 
		
		double coef =0.0;
		double cont =0.0;
		double rsqr =0.0;
		
		for(DiscRateWght aa : discWght) {
			temp = new DiscRateStats();
			
			temp.setApplStYymm(bssd);
			temp.setDiscRateCalcTyp("I");
			temp.setIntRateCd(aa.getIntRateCd());
			temp.setDepnVariable("EXT_IR");
			temp.setIndpVariable(extIrStatMap.get("KTB_Y5").getIndpVariable());
			temp.setApplEdYymm(aa.getBaseYymm());
			
			coef = aa.getKtbY5Wght()  * extIrStatMap.get("KTB_Y5").getRegrCoef() + aa.getCorpY3Wght() * extIrStatMap.get("CORP_Y3").getRegrCoef()
				 + aa.getMnsbY1Wght() * extIrStatMap.get("MNSB_Y1").getRegrCoef() + aa.getCd91Wght()  * extIrStatMap.get("CD_91").getRegrCoef();
			
			cont = aa.getKtbY5Wght()  * extIrStatMap.get("KTB_Y5").getRegrConstant() + aa.getCorpY3Wght() * extIrStatMap.get("CORP_Y3").getRegrConstant()
					 + aa.getMnsbY1Wght() * extIrStatMap.get("MNSB_Y1").getRegrConstant() + aa.getCd91Wght()  * extIrStatMap.get("CD_91").getRegrConstant();
			
			temp.setRegrCoef(coef);
			temp.setRegrConstant(cont);
			temp.setRemark("0.0");
			temp.setAvgNum(extIrStatMap.get("KTB_Y5").getAvgMonNum());
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(temp);
		}
		return rstList;
	}
	
	public static List<DiscRateStats> getAseetYieldStatForIntCode(String bssd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		
		List<DiscRateCalcSetting> calcSettingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());
		List<DiscRateStatsAssetYield> assetYieldStatList = DaoUtil.getEntities(DiscRateStatsAssetYield.class, param);
		Map<String, DiscRateStatsAssetYield> assetYieldStatMap = assetYieldStatList.stream()
																	.filter(s ->s.getDiscRateCalcTyp().equals("I"))
																	.collect(Collectors.toMap(s->s.getAcctDvCd(), Function.identity()));

//		assetYieldStatList.forEach(s ->logger.info("aaa : {}", s.getAcctDvCd()));
		List<DiscRateStats> rstList = new ArrayList<DiscRateStats>();
		DiscRateStats temp; 
		
		double coef =0.0;
		double cont =0.0;
		double rsqr =0.0;
//		logger.info("Asset Yield : {}", assetYieldStatMap);
		for(DiscRateCalcSetting aa : calcSettingList) {
			if(aa.getAcctDvCd()!=null) {
				temp = new DiscRateStats();
				
				temp.setApplStYymm(bssd);
				temp.setDiscRateCalcTyp("I");
				temp.setIntRateCd(aa.getIntRateCd());
				temp.setDepnVariable("ASSET_YIELD");
				temp.setIndpVariable(assetYieldStatMap.get(aa.getAcctDvCd()).getIndpVariable());
				
				temp.setApplEdYymm(bssd);
				temp.setRegrCoef(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrCoef());
				temp.setRegrConstant(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrConstant());
				temp.setRemark(assetYieldStatMap.get(aa.getAcctDvCd()).getRemark());
				temp.setAvgNum(assetYieldStatMap.get(aa.getAcctDvCd()).getAvgMonNum());
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(temp);
			}
		}
		return rstList;
	}
	
	public static List<DiscRateStats> getKicsAseetYieldStatForIntCode(String bssd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		
		List<DiscRateCalcSetting> calcSettingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());
		List<DiscRateStatsAssetYield> assetYieldStatList = DaoUtil.getEntities(DiscRateStatsAssetYield.class, param);
		Map<String, DiscRateStatsAssetYield> assetYieldStatMap = assetYieldStatList.stream()
																	.filter(s ->s.getDiscRateCalcTyp().equals("K"))
																	.collect(Collectors.toMap(s->s.getAcctDvCd(), Function.identity()));

		
		List<DiscRateStats> rstList = new ArrayList<DiscRateStats>();
		DiscRateStats temp; 
		
		double coef =0.0;
		double cont =0.0;
		double rsqr =0.0;
//		logger.info("Asset Yield : {}", assetYieldStatMap);
		
		for(DiscRateCalcSetting aa : calcSettingList) {
			if(aa.getAcctDvCd()!=null) {
				temp = new DiscRateStats();
				
				temp.setApplStYymm(bssd);
				temp.setDiscRateCalcTyp("K");
				temp.setIntRateCd(aa.getIntRateCd());
				temp.setDepnVariable("ASSET_YIELD");
				temp.setIndpVariable(assetYieldStatMap.get(aa.getAcctDvCd()).getIndpVariable());
				
				temp.setApplEdYymm(bssd);
				temp.setRegrCoef(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrCoef());
				temp.setRegrConstant(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrConstant());
				temp.setRemark(assetYieldStatMap.get(aa.getAcctDvCd()).getRemark());
				temp.setAvgNum(assetYieldStatMap.get(aa.getAcctDvCd()).getAvgMonNum());
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(temp);
			}
		}
		return rstList;
	}
	
	public static List<DiscRateStats> getQisAseetYieldStatForIntCode(String bssd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		
		List<DiscRateCalcSetting> calcSettingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());
		List<DiscRateStatsAssetYield> assetYieldStatList = DaoUtil.getEntities(DiscRateStatsAssetYield.class, param);
		Map<String, DiscRateStatsAssetYield> assetYieldStatMap = assetYieldStatList.stream()
																	.filter(s ->s.getDiscRateCalcTyp().equals("K"))
																	.collect(Collectors.toMap(s->s.getAcctDvCd(), Function.identity()));

		
		List<DiscRateStats> rstList = new ArrayList<DiscRateStats>();
		DiscRateStats temp; 
		
		double coef =0.0;
		double cont =0.0;
		double rsqr =0.0;
//		logger.info("Asset Yield : {}", assetYieldStatMap);
		
		for(DiscRateCalcSetting aa : calcSettingList) {
			if(aa.getAcctDvCd()!=null) {
				temp = new DiscRateStats();
				
				temp.setApplStYymm(bssd);
				temp.setDiscRateCalcTyp("Q");
				temp.setIntRateCd(aa.getIntRateCd());
				temp.setDepnVariable("ASSET_YIELD");
				temp.setIndpVariable(assetYieldStatMap.get(aa.getAcctDvCd()).getIndpVariable());
				
				temp.setApplEdYymm(bssd);
				temp.setRegrCoef(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrCoef());
				temp.setRegrConstant(assetYieldStatMap.get(aa.getAcctDvCd()).getRegrConstant());
				temp.setRemark(assetYieldStatMap.get(aa.getAcctDvCd()).getRemark());
				temp.setAvgNum(assetYieldStatMap.get(aa.getAcctDvCd()).getAvgMonNum());
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(temp);
			}
		}
		return rstList;
	}
	
	public static List<DiscRateStatsAssetYield> getStatAssetYieldForKics(String bssd) {
		List<DiscRateStatsAssetYield> rstList = new ArrayList<DiscRateStatsAssetYield>();
		DiscRateStatsAssetYield temp;
		
		String discRateDriverMatCd = ParamUtil.getParamMap().getOrDefault("kicsDiscRateDriverMatCd", "M0001");
		
		GongsiStatKicsModel model = new GongsiStatKicsModel(getKicsIndiVariable(bssd, discRateDriverMatCd, -60), getAssetYieldInput(bssd));
//		GongsiStatKicsModel model = new GongsiStatKicsModel(getKicsIndiVariable(bssd, discRateDriverMatCd, -60), getAssetRevenueInput(bssd));
		
		List<LinearRegResult> lmRst =  model.getAssetYieldReg(bssd);
		
		for(LinearRegResult aa : lmRst) {
			
			temp = new DiscRateStatsAssetYield();
			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp("K");
			temp.setAcctDvCd(aa.getDepVariable());
			temp.setIndpVariable(aa.getIndepVariable());
			temp.setAvgMonNum(aa.getAvgMonNum());
			temp.setRegrConstant(aa.getRegConstant());
			temp.setRegrCoef(aa.getRegCoef());
			temp.setRemark(aa.getRegRsqr().toString());
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(temp);
		}
//		rstList.forEach(s ->logger.info("aaa : {}", s.toString()));
		return rstList;
	}
	
	
	public static List<DiscRateStatsAssetYield> getStatAssetYieldForInternal(String bssd) {
		List<DiscRateStatsAssetYield> rstList = new ArrayList<DiscRateStatsAssetYield>();
		DiscRateStatsAssetYield temp;
		
		String ifrsDiscRateDriverMatCd = ParamUtil.getParamMap().getOrDefault("ifrsDiscRateDriverMatCd", "M0036, M0060");
		
		GongsiStatModel model = new GongsiStatModel(getRfCurveInput(bssd), getAssetYieldInput(bssd), getExtIrInput(bssd));
		List<LinearRegResult> lmRst =  model.getAssetYieldReg();
		
		for(LinearRegResult aa : lmRst) {
//			logger.info("aaaa : {},{},{}", aa.getBaseYymm(), aa.getDepVariable(), aa.getIndepVariable());
			
			temp = new DiscRateStatsAssetYield();
			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp("I");
			temp.setAcctDvCd(aa.getDepVariable());
			temp.setIndpVariable(aa.getIndepVariable());
			temp.setAvgMonNum(aa.getAvgMonNum());
			temp.setRegrConstant(aa.getRegConstant());
			temp.setRegrCoef(aa.getRegCoef());
			temp.setRemark(aa.getRegRsqr().toString());
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(temp);
		}
		return rstList;
	}
	
	public static List<DiscRateStatsExtIr> getStatExtIrForIntenal(String bssd) {
		List<DiscRateStatsExtIr> rstList = new ArrayList<DiscRateStatsExtIr>();
		DiscRateStatsExtIr temp;
		
		String ifrsDiscRateDriverMatCd = ParamUtil.getParamMap().getOrDefault("ifrsDiscRateDriverMatCd", "M0036, M0060");

		GongsiStatModel model = new GongsiStatModel(getRfCurveInput(bssd), getAssetYieldInput(bssd), getExtIrInput(bssd));
		List<LinearRegResult> lmRst =  model.getExtIrReg();
		
		
		for(LinearRegResult aa : lmRst) {
//			logger.info("zzz :  {}", aa.getBaseYymm());
			
			temp = new DiscRateStatsExtIr();
			
			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp("I");
			temp.setExtIrCd(aa.getDepVariable());
			temp.setIndpVariable(aa.getIndepVariable());
			temp.setAvgMonNum(aa.getAvgMonNum());
			temp.setRegrConstant(aa.getRegConstant());
			temp.setRegrCoef(aa.getRegCoef());
			temp.setRemark(aa.getRegRsqr().toString());
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
//			logger.info("Stats : {},{}", aa.getDepVariable(), aa.getRegCoef());
			rstList.add(temp);
		}
		
//		logger.info("LM :  {}", lmRst.size());
		return rstList;		
	}
	

	private static List<RCurveHis> getRfCurveInput(String bssd) {
		String ifrsDiscRateDriverMatCd = ParamUtil.getParamMap().getOrDefault("ifrsDiscRateDriverMatCd", "M0036, M0060");
		ifrsDiscRateDriverMatCd =  "M0060";
//		IrCurveHisDao.getKTBMaturityHis(bssd, ifrsDiscRateDriverMatCd);
		return RCurveHis.convertFrom(IrCurveHisDao.getKTBMaturityHis(bssd, ifrsDiscRateDriverMatCd));
		
//		return RCurveHis.convertFrom(
//		IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, -12), "A100")
//		.stream().filter(s -> s.getMatCd().equals("M0060"))
//		.collect(Collectors.toList())
//		)
//		;
		
//		return RCurveHis.convertFrom(IrCurveHisDao.getKTBMaturityHis(bssd, "M0036", "M0060"));
	}


	private static List<RAcctYieldHis> getAssetYieldInput(String bssd) {
		return RAcctYieldHis.convertFrom(DaoUtil.getEntities(UserDiscRateAsstRevnCumRate.class, new HashMap<>()), false);
	}
	
	private static List<RExtIrHis> getExtIrInput(String bssd) {
		return RExtIrHis.convertFrom(DaoUtil.getEntities(UserDiscRateExBaseIr.class, new HashMap<>()));
	
	}

	private static List<IrCurveHis> getKicsIndiVariable(String bssd, String matCd, int monthNum){
		List<IrCurveHis> tempList = new ArrayList<IrCurveHis>();

		for(Map.Entry<String, List<IrCurveHis>> entry : getEomCurveMap(bssd, monthNum).entrySet()) {
			SmithWilsonModel swModel = new SmithWilsonModel(entry.getValue(), 0.045, 60);
			tempList.add(swModel.getIrCurveHisList(entry.getKey()).stream().filter(s->s.getMatCd().equals(matCd)).findFirst().orElse(new IrCurveHis())); 
		}
		return tempList;
	}
	
	private static Map<String, List<IrCurveHis>> getEomCurveMap(String bssd, int monthNum){
		String lqKtbIrCruveId = ParamUtil.getParamMap().getOrDefault("lqKtbIrCruveId", "A100"); 
		List<IrCurveHis> curveList = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, monthNum), lqKtbIrCruveId);		
		Map<String, String> eomDate = curveList.stream().collect(Collectors.toMap(s ->s.getBaseYymm(), s-> s.getBaseDate(), (s,u)-> u));
		
		return  curveList.stream()
						 .filter(s -> eomDate.containsValue(s.getBaseDate()))
						 .collect(Collectors.groupingBy(s->s.getBaseDate(), Collectors.toList()))
						;
	}
	
}
