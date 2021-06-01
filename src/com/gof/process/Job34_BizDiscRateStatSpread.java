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
 *  <p> ������ ���ñ��������� ����ä �ݸ����� �������尡 ���Ŀ��� �����ȴٴ� �����Ͽ��� �������� �ּ�����ġ �����ϴ� ������         
 *  <p>    1. ���� Driver �� ����ä ����  
 *  <p>    2. ����ä�� ���ñ��������� ���� �������� ���� 
 *  <p>	     2.1 �ڻ��� ���ͷ��� ����ä�� ���������� ������� 
 *  <p>	     2.2 ������� 1.0 ���� ������.
 *  <p>      2.3.�������� ������ �������� ������.
 *  <p>    3.�ٸ� ����ڰ� ������ �� �������� �Է��� ��� ����� �Է°��� �켱��. 
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
		
		
//		����ڰ� �Է��� ������ ��� ����
		Map<String, BizDiscRateStatUd> userSpreadStatMap = DiscRateStatsDao.getUserDiscRateStat(bssd).stream()
																		 .filter(s->s.getApplyBizDv().equals("S"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
//		����ڰ� ������ �����ϱ� ���� �Է��� ��쿡 �����. 
		Map<String, BizDiscRateAdjUd> userSpreadAdjtMap = DiscRateStatsDao.getUserDiscRateAdj(bssd).stream()
																		 .filter(s->s.getApplBizDv().equals("S"))
																		 .collect(Collectors.toMap(s->s.getIntRateCd(), Function.identity()));
		
		Map<String, Double> rfCurveMap = getDriverCurveMap(bssd, isRiskFree.equals("Y"));
		
//		�������� �ڵ� ���� ����		
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, 0);
		List<String> discRateHisIntRate = discRateHis.stream().map(s ->s.getIntRateCd()).collect(Collectors.toList());
		
		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());

//		�����ؾ��ϴ� �������� �ڵ忡 ���ؼ� �������� �̷� �����Ͱ� ���� ���� ������.
		List<DiscRateCalcSetting> calcSettingList = settingList.stream().filter(s -> s.isCalculable())
																		.filter(s->discRateHisIntRate.contains(s.getIntRateCd()))
																		.collect(Collectors.toList());
		
		List<DiscRateCalcSetting> nonCalcSettingList = settingList.stream().filter(s -> !s.isCalculable()).collect(Collectors.toList());
		
		
//		�������� �� �ݸ������� �������� �ʴ� ������ ���� ���������� �߼�����ġ�� ������.
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
	 * ���� ���������� �ݸ� ������ ����
	 * �������� ���� A100 ���� �ݸ��� �����ϸ�, ���� �������� ���� ������ �ݸ��� ������.
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
