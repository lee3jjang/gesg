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
import com.gof.entity.DiscRateWght;
import com.gof.entity.IrCurveHis;
import com.gof.util.ParamUtil;


/**
 *  <p> Smith Wilson 모형        
 *  <p> Hull and White 1 Factor, 2 Factor, CIR, Vacicek 등으로 산출한 금리기간 구조의 보외법을 적용하여 전제금리기간 구조 산출함.  
 *  <p>  Script 를 실행하기 위한  Input Data 생성 및 관리 , R Script 실행, Output Converting 작업을 수행함.</p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class DiscRateModelNew {
	private final static Logger logger = LoggerFactory.getLogger(DiscRateModelNew.class);
	
	private String bssd;
	private String calcType;
	private DiscRateCalcSetting setting;
	private DiscRateHis	discRateHis;
	private List<DiscRateHis>	discRateHisList;
	private DiscRateWght	discRateWght;
	private List<BizDiscRateStat> statList;
	private String discRateCurrentAdjYn ;
	private int discRateAdjAvgMon ;
	
	private String sceNo ="";
	private double scaleFactor =1.0;
	private double scaleConstant =0.0;
	private double scaleAsset 	 =0.0;
	private double scaleExtIr 	 =0.0;
	private double adj		 	 =0.0;
	private double extWgt	 	 =0.0;
	
	private Map<String, List<IrCurveHis>> indiVariableSwRstMap = new HashMap<String, List<IrCurveHis>>();
	
	public DiscRateModelNew() {
	}
	public DiscRateModelNew(String bssd, String calcType, DiscRateCalcSetting setting) {
		this.bssd = bssd;
		this.calcType =calcType;
		this.setting = setting;
		
		this.discRateCurrentAdjYn =  ParamUtil.getParamMap().getOrDefault("discRateCurrentAdjYn", "N");
		this.discRateAdjAvgMon =  Integer.parseInt(ParamUtil.getParamMap().getOrDefault("discRateAdjAvgMon", "-12"));

		this.discRateHis = DiscRateSettingDao.getDiscRateHis(bssd, setting.getIntRateCd());
		this.discRateHisList = DiscRateSettingDao.getDiscRateHis(bssd, discRateAdjAvgMon, setting.getIntRateCd());
		
		this.discRateWght = DiscRateSettingDao.getDiscRateWeight(bssd, setting.getIntRateCd());
		
		this.statList = DiscRateStatsDao.getBizDiscRateStat(bssd, setting.getIntRateCd(), calcType);
	}
	
	public DiscRateModelNew(String bssd, String calcType, DiscRateCalcSetting setting, String sceNo) {
		this(bssd, calcType, setting);
		this.sceNo = sceNo;
		
		
	}
	public DiscRateModelNew(String bssd, String calcType, DiscRateCalcSetting setting, Map<String, List<IrCurveHis>> fwdMap) {
		this(bssd, calcType, setting);
		this.indiVariableSwRstMap = fwdMap; 
	}
	
	public DiscRateModelNew(String bssd, String calcType, DiscRateCalcSetting setting, String sceNo, Map<String, List<IrCurveHis>> fwdMap) {
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
		
//		adjustInternalScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
		if(discRateHis != null && discRateHis.getExBaseIrWght() > 0.0) {
			extWgt = discRateHis.getExBaseIrWght();
		}else {
			extWgt = discRateWght ==null ? 0.0 : discRateWght.getExtrIrWght();
		}
		
		for (int j = 0; j < 1200; j++) {
			final int k = j  ;
			for (BizDiscRateStat stat : statList) {
				if(stat.getApplyBizDv().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
//					logger.info("rfRate : {},{},{}", k, stat, rfRate);
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() ;
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() ;
					}else {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() ;
						extWgt =0.0;
					}
					
					adj = stat.getAdjRate();
					 
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
		
		if(discRateHis != null && discRateHis.getExBaseIrWght() > 0.0) {
			extWgt = discRateHis.getExBaseIrWght();
		}else {
			extWgt = discRateWght ==null ? 0.0 : discRateWght.getExtrIrWght();
		}
		
		
		for (int j = 0; j < 1200; j++) {
//			final int k = j + 1 ;
			final int k = j  ;
			for (BizDiscRateStat stat : statList) {
				if(stat.getApplyBizDv().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);					
					
					if (stat.getDepnVariable().equals("ASSET_YIELD")) {
						assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant() ; 
					} else if (stat.getDepnVariable().equals("EXT_IR")) {
						extIr = stat.getRegrCoef() * rfRate + stat.getRegrConstant() ; 
					}
					adj = stat.getAdjRate();
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
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (BizDiscRateStat stat : statList) {
				if(stat.getApplyBizDv().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);

					assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant();
					adj = stat.getAdjRate();
				}
				
			}
			
			temp = new DiscRate();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));
			
			temp.setExBaseIr(0.0);
			temp.setExBaseIrWght(0.0);


			temp.setMgtYield(assetYield);
//			temp.setBaseDiscRate(assetYield * scaleFactor );   						   
			temp.setBaseDiscRate(0.0);
			temp.setAdjRate(adj);

			temp.setDiscRate(assetYield* adj);
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
		
//		adjustKicsScale();
		logger.debug("Disclosure Rate History Data for IntRateCd {} : {},{}", setting.getIntRateCd(), discRateHis.toString());
		
//		getIndiVariableMap().get("KTB5Y").stream().filter(s -> s.getFwdMonthNum()<= 0).forEach(s -> logger.info("sss : {},{},{}",s.getFwdMonthNum(), s.getSpotAnnual(), s.toString()));
		
		
		
		for (int j = 0; j < 1200; j++) {
			final int k = j + 1 ;
			for (BizDiscRateStat stat : statList) {
				if(stat.getApplyBizDv().equals(calcType)) {
					rfRate = getAvgRfRate(stat, k);
					
					assetYield = stat.getRegrCoef() * rfRate + stat.getRegrConstant();
					adj = stat.getAdjRate();
				}
			}
			
			temp = new DiscRateSce();

			temp.setBaseYymm(bssd);
			temp.setDiscRateCalcTyp(calcType);
			temp.setSceNo(sceNo);
			temp.setIntRateCd(setting.getIntRateCd());
			temp.setMatCd("M" + String.format("%04d", j + 1));

			temp.setExBaseIr(0.0);
			temp.setExBaseIrWght(0.0);

			temp.setMgtYield(assetYield);
			temp.setBaseDiscRate(0.0);

			temp.setAdjRate(adj);
			temp.setDiscRate(assetYield* adj);
			temp.setVol(rfRate);
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			discRateRst.add(temp);
		}

		return discRateRst;
	}
	
	private double getAvgRfRate (BizDiscRateStat stat, int forwardNum ) {
//		indiVariableSwRstMap.get(stat.getIndiVariableMatCd()).stream()
//												.filter(s -> s.getForwardNum() >= forwardNum )
//												.limit(stat.getAvgNum().longValue())
//												.forEach(s -> logger.info("aaa : {}, {}", s.toString()));
		
		return indiVariableSwRstMap.get(stat.getIndiVariableMatCd()).stream()
									.filter(s -> s.getForwardNum()<= forwardNum )
									.sorted(new IrCurveHisFwdComparator())
									.limit(stat.getAvgMonNum().longValue())
									.collect(Collectors.averagingDouble(s ->s.getIntRate()))
		;
		
	}
	
	
}
