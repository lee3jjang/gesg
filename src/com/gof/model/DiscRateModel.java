package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.comparator.IrCurveHisFwdComparator;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.DiscRateStatsDao;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateSce;
import com.gof.entity.DiscRateStats;
import com.gof.entity.DiscRateWght;
import com.gof.entity.IrCurveHis;


/**
 *  <p> Smith Wilson 모형        
 *  <p> Hull and White 1 Factor, 2 Factor, CIR, Vacicek 등으로 산출한 금리기간 구조의 보외법을 적용하여 전제금리기간 구조 산출함.  
 *  <p>  Script 를 실행하기 위한  Input Data 생성 및 관리 , R Script 실행, Output Converting 작업을 수행함.</p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class DiscRateModel {
	private final static Logger logger = LoggerFactory.getLogger(DiscRateModel.class);
	
	private String bssd;
	private String calcType;
	private DiscRateCalcSetting setting;
	private DiscRateHis	discRateHis;
	private List<DiscRateHis>	discRateHisList;
	private DiscRateWght	discRateWght;
	private List<DiscRateStats> statList;
	
	private String sceNo ="";
	private double scaleFactor =1.0;
	private double scaleConstant =0.0;
	private double scaleAsset 	 =0.0;
	private double scaleExtIr 	 =0.0;
	private double adj		 	 =0.0;
	private double extWgt	 	 =0.0;
	
	private Map<String, List<IrCurveHis>> indiVariableSwRstMap = new HashMap<String, List<IrCurveHis>>();
	
	public DiscRateModel() {
	}
	public DiscRateModel(String bssd, String calcType, DiscRateCalcSetting setting) {
		this.bssd = bssd;
		this.calcType =calcType;
		this.setting = setting;

		this.discRateHis = DiscRateSettingDao.getDiscRateHis(bssd, setting.getIntRateCd());
		this.discRateHisList = DiscRateSettingDao.getDiscRateHis(bssd, -12, setting.getIntRateCd());
		
		this.discRateWght = DiscRateSettingDao.getDiscRateWeight(bssd, setting.getIntRateCd());
		
		this.statList = DiscRateStatsDao.getDiscRateStats(bssd, setting.getIntRateCd(), calcType);
		
		
	}
	
	public DiscRateModel(String bssd, String calcType, DiscRateCalcSetting setting, String sceNo) {
		this(bssd, calcType, setting);
		this.sceNo = sceNo;
		
		
	}
	public DiscRateModel(String bssd, String calcType, DiscRateCalcSetting setting, Map<String, List<IrCurveHis>> fwdMap) {
		this(bssd, calcType, setting);
		this.indiVariableSwRstMap = fwdMap; 
	}
	
	public DiscRateModel(String bssd, String calcType, DiscRateCalcSetting setting, String sceNo, Map<String, List<IrCurveHis>> fwdMap) {
		this(bssd, calcType, setting);
		this.sceNo = sceNo;
		this.indiVariableSwRstMap = fwdMap;
	}
	
	public  List<DiscRate> getDiscRateUserGiven(String calcType) {
		List<DiscRate> discRateRst = new ArrayList<DiscRate>();
		DiscRate temp;
		
		if(discRateHis!= null && discRateHis.getApplDiscRate() > 0.0) {
			for(int j =1; j<= 1200; j++) {
				temp = new DiscRate();

				temp.setBaseYymm(bssd);
				temp.setDiscRateCalcTyp(calcType);
				temp.setIntRateCd(setting.getIntRateCd());
				temp.setMatCd("M" + String.format("%04d", j ));
				
				temp.setMgtYield(discRateHis.getMgtAsstYield());
				temp.setExBaseIr(discRateHis.getExBaseIr() );
				temp.setExBaseIrWght(discRateHis.getExBaseIrWght());
				temp.setBaseDiscRate(discRateHis.getBaseDiscRate());
				temp.setAdjRate(discRateHis.getDiscRateAdjRate());
				temp.setDiscRate(discRateHis.getApplDiscRate());
				
				temp.setVol(0.0);
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				discRateRst.add(temp);
			}
		}
		return discRateRst;
	}
	
	public  List<DiscRateSce> getDiscRateScenarioUserGiven(String calcType) {
		List<DiscRateSce> discRateRst = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		
		if(discRateHis!= null && discRateHis.getApplDiscRate() > 0.0) {
			for(int j =1; j<= 1200; j++) {
				temp = new DiscRateSce();
				
				temp.setBaseYymm(bssd);
				temp.setDiscRateCalcTyp(calcType);
				temp.setIntRateCd(setting.getIntRateCd());
				temp.setSceNo(sceNo);
				temp.setMatCd("M" + String.format("%04d", j ));
				
				temp.setMgtYield(discRateHis.getMgtAsstYield());
				temp.setExBaseIr(discRateHis.getExBaseIr() );
				temp.setExBaseIrWght(discRateHis.getExBaseIrWght());
				temp.setBaseDiscRate(discRateHis.getBaseDiscRate());
				temp.setAdjRate(discRateHis.getDiscRateAdjRate());
				temp.setDiscRate(discRateHis.getApplDiscRate());
				
				temp.setVol(0.0);
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				discRateRst.add(temp);
			}
		}
		return discRateRst;
	}
	
	public  List<DiscRate> getInternalDiscRate() {
		List<DiscRate> discRateRst = new ArrayList<DiscRate>();
		DiscRate temp;
		
		double extIr = 0.0;
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRate>();
		}
		
		adjustInternalScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
					
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() + scaleExtIr; 
					}
				}
			}
			
			temp = new DiscRate();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(assetYield);
			temp.setExBaseIr(extIr );

			temp.setExBaseIrWght(extWgt);
			
//			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt + discRateHis.getDiscRateSpread());   //현재 추정값 fitting with add Term
			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt );   						          //현재 추정값 fitting with add Term
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}

	
	
	public  List<DiscRateSce> getInternalDiscRateScenario() {
		List<DiscRateSce> discRateRst = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		
		double extIr = 0.0;
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRateSce>();
		}
		
		adjustInternalScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);					
//					rfRate = getAvgRfRate(stat, k - stat.getAvgNum().intValue());            //????			
					
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() + scaleExtIr; 
					}
				}
			}
			
			temp = new DiscRateSce();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setSceNo(sceNo);
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(assetYield);
			temp.setExBaseIr(extIr );

			temp.setExBaseIrWght(extWgt);
			
//			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt + discRateHis.getDiscRateSpread());   //현재 추정값 fitting with add Term
			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt );   						          //현재 추정값 fitting with add Term
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}
	
	
	public  List<DiscRate> getKicsDiscRate() {
		List<DiscRate> discRateRst = new ArrayList<DiscRate>();
		DiscRate temp;
		
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRate>();
		}
		
		adjustKicsScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
//					rfRate = getKicsAvgRfRate(stat, k);
					
					
					
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					}
				}
			}
			
			temp = new DiscRate();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(assetYield);
			temp.setExBaseIr(0.0);

			temp.setExBaseIrWght(0.0);
			
			temp.setBaseDiscRate(assetYield * scaleFactor );   						   
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG_"+scaleAsset);
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}
	
	public  List<DiscRateSce> getKicsDiscRateScenario() {
		List<DiscRateSce> discRateRst = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRateSce>();
		}
		
		adjustKicsScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
					
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					}
				}
			}
			
			temp = new DiscRateSce();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setSceNo(sceNo);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(assetYield);
			temp.setExBaseIr(0.0);

			temp.setExBaseIrWght(0.0);
			
			temp.setBaseDiscRate(assetYield * scaleFactor );   						   
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}
	
	public  List<DiscRate> getQisDiscRate() {
		List<DiscRate> discRateRst = new ArrayList<DiscRate>();
		DiscRate temp;
		
		double extIr = 0.0;
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRate>();
		}
		
		adjustInternalScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
					
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() + scaleExtIr; 
					}
				}
			}
			
			temp = new DiscRate();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(0.0);
			temp.setExBaseIr(0.0 );

			temp.setExBaseIrWght(0.0);
			
//			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt + discRateHis.getDiscRateSpread());   //현재 추정값 fitting with add Term
			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt );   						          //현재 추정값 fitting with add Term
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}

	
	
	public  List<DiscRateSce> getQisRateScenario() {
		List<DiscRateSce> discRateRst = new ArrayList<DiscRateSce>();
		DiscRateSce temp;
		
		double extIr = 0.0;
		double assetYield = 0.0;
		double rfRate = 0.0;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<DiscRateSce>();
		}
		
		adjustInternalScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (DiscRateStats stat : statList) {
				if(stat.getDiscRateCalcTyp().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k - stat.getAvgNum().intValue());			//???				
//					rfRate = getAvgRfRate(stat, k);
//					logger.info("Rate : {},{},{},{}", j,stat.getDepnVariable(), stat.getIndpVariable(),rfRate);
					
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() +scaleAsset; 
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() + scaleExtIr; 
					}
				}
			}
			
			temp = new DiscRateSce();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setSceNo(sceNo);
			temp.setMatCd("M" + String.format("%04d", j + 1));
			temp.setMgtYield(assetYield);
			temp.setExBaseIr(extIr );

			temp.setExBaseIrWght(extWgt);
			
//			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt + discRateHis.getDiscRateSpread());   //현재 추정값 fitting with add Term
			temp.setBaseDiscRate(assetYield * (1 - extWgt) + extIr * extWgt );   						          //현재 추정값 fitting with add Term
			temp.setAdjRate(adj);

			temp.setDiscRate(temp.getBaseDiscRate() * adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}
	
	public  List<BizDiscRateStat> getIfrsBizDiscRateStatCalc() {
			List<BizDiscRateStat> discRateRst = new ArrayList<BizDiscRateStat>();
			BizDiscRateStat temp;
			
			double scale =0.0;
			double regCon =0.0;
			double regCoef =0.0;
			double rfRate = 0.0;
			
	
	//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
			if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
				logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
				return new ArrayList<BizDiscRateStat>();
			}
			
			logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
			
			adjustInternalScale();
			
			Map<String, List<DiscRateStats>> statsMap = statList.stream().collect(Collectors.groupingBy(s -> s.getIndpVariable(), Collectors.toList()));
			for(Map.Entry<String, List<DiscRateStats>> entry : statsMap.entrySet()) {
				for (DiscRateStats stat : entry.getValue()) {
					rfRate = getAvgRfRate(stat,  1);
					if(stat.getDepnVariable().toLowerCase().contains("asset")) {
						scale = scale + scaleAsset;
						regCoef = regCoef + stat.getRegrCoef();
						regCon = regCon + stat.getRegrConstant();
					}else {
						scale = scale + scaleExtIr;
						regCoef = regCoef + stat.getRegrCoef();
						regCon = regCon + stat.getRegrConstant();
					}
				}
				temp = new BizDiscRateStat();
				
				temp.setBaseYymm(bssd);
				temp.setApplyBizDv("I");
				temp.setIntRateCd(setting.getIntRateCd());
				temp.setIndpVariable(entry.getKey());
				temp.setAvgMonNum(1.0);
				
				temp.setRegrConstant(scaleFactor * (regCon + scale));
				temp.setRegrCoef(scaleFactor * regCoef);
				temp.setAdjRate(discRateHis.getDiscRateAdjRate());
				temp.setVol(rfRate);
				temp.setRemark(scaleFactor +"_" + scaleAsset +"_" +regCoef+"_" + regCon);
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				discRateRst.add(temp);
			}	
			return discRateRst;
	}
	
	public  List<BizDiscRateStat> getIfrsBizDiscRateStatGiven(String calcType) {
		List<BizDiscRateStat> discRateRst = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat temp;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<BizDiscRateStat>();
		}
		
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		temp = new BizDiscRateStat();
			
		temp.setBaseYymm(bssd);
		temp.setApplyBizDv(calcType);
		temp.setIntRateCd(setting.getIntRateCd());
		temp.setIndpVariable("KTB1M");
		temp.setAvgMonNum(1.0);
		
		temp.setRegrConstant(discRateHis.getApplDiscRate());
		temp.setRegrCoef(1.0);
		temp.setAdjRate(1.0);
		temp.setVol(0.0);
		temp.setRemark(":" + discRateHis.getDiscRateAdjRate());
		temp.setLastModifiedBy("ESG");
		temp.setLastUpdateDate(LocalDateTime.now());
		
		discRateRst.add(temp);

		return discRateRst;
	}
	public  List<BizDiscRateStat> getKicsBizDiscRateStatCalc() {
		List<BizDiscRateStat> discRateRst = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat temp;
		
		double assetYield = 0.0;
		double rfRate = 0.0;
		

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<BizDiscRateStat>();
		}
		
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		scale 조정
		adjustKicsScale();
		
		for (DiscRateStats stat : statList) {
			rfRate = getAvgRfRate(stat,  1);
			
			temp = new BizDiscRateStat();
			
			temp.setBaseYymm(bssd);
			temp.setApplyBizDv(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setIndpVariable(stat.getIndpVariable());
			temp.setAvgMonNum(stat.getAvgNum());
			
			temp.setRegrConstant(scaleFactor * (stat.getRegrConstant() + scaleAsset));
			temp.setRegrCoef(scaleFactor * stat.getRegrCoef());
			temp.setAdjRate(discRateHis.getDiscRateAdjRate());
			temp.setVol(rfRate);
			
			temp.setRemark(scaleFactor +"_" + scaleAsset +"_" +stat.getRegrCoef()+"_" + stat.getRegrConstant());
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
			
		}

		return discRateRst;
	}
	
	public  List<BizDiscRateStat> getKicsBizDiscRateStatGiven() {
		List<BizDiscRateStat> discRateRst = new ArrayList<BizDiscRateStat>();
		BizDiscRateStat temp;

//		공시이율 이력 데이터 부재시 공시이율 추정시 생성 불가!!!!		
		if (discRateHis==null || discRateHis.getApplDiscRate() == null) {
			logger.warn("Disclosure Rate History Data is null for {}. Check record and column APPL_DISC_RATE", setting.getIntRateCd());
			return new ArrayList<BizDiscRateStat>();
		}
		
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		temp = new BizDiscRateStat();
			
		temp.setBaseYymm(bssd);
		temp.setApplyBizDv(calcType);
		temp.setIntRateCd(setting.getIntRateCd());
		temp.setIndpVariable("KTB1M");
		temp.setDepnVariable("ASSET_YIELD");
		temp.setAvgMonNum(1.0);
		
		temp.setRegrConstant(discRateHis.getApplDiscRate());
		temp.setRegrCoef(1.0);
		temp.setAdjRate(1.0);
		temp.setVol(0.0);
		temp.setRemark(":" + discRateHis.getDiscRateAdjRate());
		temp.setLastModifiedBy("ESG");
		temp.setLastUpdateDate(LocalDateTime.now());
		
		discRateRst.add(temp);

		return discRateRst;
	}
	
	//	입력한 지표와 계산한 지표의 Gap 을 조정하기 위한 sacle 조정임
	//	외부지표금리 가중치는 개별로 입력한 것을 우선으로 처리함.
	//	조정률은 월별로 입력한 값을 사용하고, 조정률이 존재하지 않는 경우 역산하여 산출함.
	//	통계 분석 결과로 추정한 자산수익률, 외부지표금리와 현재값을 일치시키는 scale spread 를 산출함.
	private void adjustInternalScale() {
			double initExtIr = 0.0;
			double initAssetYield = 0.0;
			double initRfRate =0.0;
//			int monNum = 60;
			
			
			if(discRateHis != null && discRateHis.getExBaseIrWght() > 0.0) {
				extWgt = discRateHis.getExBaseIrWght();
			}else {
				extWgt = discRateWght ==null ? 0.0 : discRateWght.getExtrIrWght();
			}
			
			if(discRateHis != null && discRateHis.getDiscRateAdjRate() > 0.0) {
				adj =discRateHis.getDiscRateAdjRate() ;
			}else {
				adj = discRateHis.getBaseDiscRate()!=0.0 ? discRateHis.getApplDiscRate() / discRateHis.getBaseDiscRate(): 1.0;	
			}
								
			if(scaleAsset == 0.0) {
				for (DiscRateStats stat : statList) {
					initRfRate = getAvgRfRate(stat,  0);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						initAssetYield = stat.getRegrCoef() * initRfRate + stat.getRegrConstant();
						scaleAsset = discRateHis.getMgtAsstYield() - initAssetYield;
						
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						initExtIr = stat.getRegrCoef() * initRfRate + stat.getRegrConstant();
						scaleExtIr = discRateHis.getExBaseIr() - initExtIr;
					}
				}
				
				scaleConstant = (discRateHis.getMgtAsstYield()  * (1 - extWgt) + discRateHis.getExBaseIr() * extWgt + discRateHis.getDiscRateSpread()) - discRateHis.getBaseDiscRate() ;
//				scaleFactor = discRateHis.getBaseDiscRate() / (discRateHis.getMgtAsstYield()  * (1 - extWgt) + discRateHis.getExBaseIr() * extWgt + discRateHis.getDiscRateSpread());
				scaleFactor = discRateHis.getBaseDiscRate() / (discRateHis.getMgtAsstYield()  * (1 - extWgt) + discRateHis.getExBaseIr() * extWgt );
				
	//			logger.info("Adjust Info Adj, ExtWght for {} : {}, {}", discRateHis.getIntRateCd(), adj, extWgt);
	//			logger.info("Adjust Sacle : {}, {}, {},{},{},{},{},{}", scaleAsset, scaleExtIr, initRfRate, initAssetYield, initExtIr);
			}
			
	}
	private void adjustKicsScale() {
		double initExtIr = 0.0;
		double initAssetYield = 0.0;
		double initRfRate =0.0;
//		int monNum = 60;
		double tempAdj=0.0;
		
		if(discRateHis != null && discRateHis.getDiscRateAdjRate() > 0.0) {
			adj =discRateHis.getDiscRateAdjRate() ;
		}else {
			adj = discRateHis.getBaseDiscRate()!=0.0 ? discRateHis.getApplDiscRate() / discRateHis.getBaseDiscRate(): 1.0;	
		}
						
//		조정률 과거 평균 적용
		for(DiscRateHis aa : discRateHisList) {
			tempAdj = tempAdj + aa.getDiscRateAdjRate();
		}
		adj = discRateHisList.isEmpty()?1.0: tempAdj / discRateHisList.size();
		
		if(scaleAsset == 0.0) {
			for (DiscRateStats stat : statList) {
				
				initRfRate = getAvgRfRate(stat,  1);
				if (stat.getDepnVariable().equals("ASSET_YIELD")) {
					initAssetYield = stat.getRegrCoef() * initRfRate + stat.getRegrConstant();
					scaleAsset = discRateHis.getMgtAsstYield() - initAssetYield;
					
				} 
			}
			scaleFactor = discRateHis.getBaseDiscRate() / discRateHis.getMgtAsstYield();
			
//			logger.info("Adjust Sacle : {}, {}, {},{},{},{},{},{}", scaleAsset, initAssetYield, initRfRate);
		}
		
	}
	
	private void adjustQisScale() {
		double initExtIr = 0.0;
		double initAssetYield = 0.0;
		double initRfRate =0.0;
		int monNum = 60;
		
		
		if(discRateHis != null && discRateHis.getDiscRateAdjRate() > 0.0) {
			adj =discRateHis.getDiscRateAdjRate() ;
		}else {
			adj = discRateHis.getBaseDiscRate()!=0.0 ? discRateHis.getApplDiscRate() / discRateHis.getBaseDiscRate(): 1.0;	
		}
		extWgt = 0.5;
							
		if(scaleAsset == 0.0) {
			for (DiscRateStats stat : statList) {
				/*if(stat.getIndpVariable().trim().startsWith("KTB") && stat.getIndpVariable().endsWith("Y")) {
					monNum = Integer.parseInt(stat.getIndpVariable().split("KTB")[1].replace("Y", "")) * 12;
					 
					initRfRate = getAvgRfRate(stat, -1* stat.getAvgNum().intValue());
				}*/
				initRfRate = getAvgRfRate(stat, -1* stat.getAvgNum().intValue());
				
				if (stat.getDepnVariable().equals("ASSET_YIELD")) {
					initAssetYield = stat.getRegrCoef() * initRfRate + stat.getRegrConstant();

					
				} else if (stat.getDepnVariable().equals("EXT_IR")) {
					initExtIr = stat.getRegrCoef() * initRfRate + stat.getRegrConstant();

				}
			}
			
			scaleConstant = (initAssetYield + initExtIr) / 2  - discRateHis.getBaseDiscRate() ;
			
//			logger.info("Adjust Info Adj, ExtWght for {} : {}, {}", discRateHis.getIntRateCd(), adj, extWgt);
//			logger.info("Adjust Sacle : {}, {}, {},{},{},{},{},{}", scaleAsset, scaleExtIr, initRfRate, initAssetYield, initExtIr);
		}
		
	}
	
	private double getAvgRfRate (DiscRateStats stat, int forwardNum ) {
//		indiVariableSwRstMap.get(stat.getIndiVariableMatCd()).stream()
//												.filter(s -> s.getForwardNum() >= forwardNum )
//												.limit(stat.getAvgNum().longValue())
//												.forEach(s -> logger.info("aaa : {}, {}", s.toString()));
		
//		indiVariableSwRstMap.get(stat.getIndiVariableMatCd()).stream()
//							.filter(s -> s.getForwardNum()<= forwardNum )
//							.sorted(new IrCurveHisFwdComparator())
//							.limit(stat.getAvgNum().longValue())
//							.forEach(s -> logger.info("aaa : {}, {}", s.toString()));
		
		return indiVariableSwRstMap.get(stat.getIndiVariableMatCd()).stream()
									.filter(s -> s.getForwardNum()<= forwardNum )
									.sorted(new IrCurveHisFwdComparator())
									.limit(stat.getAvgNum().longValue())
									.collect(Collectors.averagingDouble(s ->s.getIntRate()))
		;
		
	}
	
//	private double getKicsAvgRfRate (DiscRateStats stat, int forwardNum ) {
//		logger.info("aa : {},{}", stat.getIndpVariable(), indiVariableSwRstMap.entrySet());
//		return indiVariableSwRstMap.get(stat.getIndpVariable()).stream()
//									.filter(s -> s.getForwardNum()<= forwardNum )
//									.sorted(new IrCurveHisFwdComparator())
//									.limit(stat.getAvgNum().longValue())
//									.collect(Collectors.averagingDouble(s ->s.getIntRate()))
//		;
//		
//	}
}
