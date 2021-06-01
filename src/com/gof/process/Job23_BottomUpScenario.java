package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizLiqPremium;
import com.gof.entity.DcntSce;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiqPremium;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;

/**
*  <p> BottomUp �������� �ݸ� �Ⱓ ������ �������� �ݸ� �ó������� ������.
*  <p> 1. ������ �ݸ��� �̿��Ͽ� ������ �ó����� ({@link Job14_EsgScenario}
*  <p> 2. ������ �ó����� ���� ������ �����̾��� ���� 
*  <p> 3. ������ �����̾��� �ݿ��� �ó������� Smith Wilson ({@link SmithWilsonModel} ����/�������� �����Ͽ� �ó����� ������.
*  
* @author takion77@gofconsulting.co.kr 
* @version 1.0
*/
public class Job23_BottomUpScenario {
	
	private final static Logger logger = LoggerFactory.getLogger("BottomUp");

	public static List<DcntSce> createBottomUpScenario(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, List<BizLiqPremium> lpRst) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
			
		if(curveRst.isEmpty()) {
			logger.warn("Risk Free Scenario Data Error :   Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
		if(curveRst.size()!=1200) {
			logger.warn("Risk Free Sceanrio Data Warn :  Data of {} is not 1200 but {} Months at {} ", irCurveId, bssd, curveRst.size() );
		}
		double liqPrem =0.0;
		double fwdRate =0.0;
		
		Map<String, IrCurveHis> curveMap = curveRst.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
//		Map<String, Double> fwdMap = FinUtils.getForwardRateForPV(curveMap);
		
		Map<String, BizLiqPremium> lpMap = lpRst.stream().collect(Collectors.toMap(s -> s.getMatCd(), Function.identity()));
		
		List<IrCurveHis> curveVolAdjRst = new ArrayList<>();
		for(IrCurveHis aa : curveRst) {
			IrCurveHis tempCurve = new IrCurveHis(aa.getBaseDate(), aa);
			
			if(lpMap.containsKey(aa.getMatCd())) {
				tempCurve.setIntRate(aa.getIntRate() + lpMap.get(aa.getMatCd()).getLiqPrem());
			}
			curveVolAdjRst.add(tempCurve);
		}
		
		Map<String, IrCurveHis> curveVolAdjMap = curveVolAdjRst.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
		Map<String, Double> fwdVolAdjMap = FinUtils.getForwardRateForPV(curveVolAdjMap);
		
		for (IrCurveHis aa : curveRst) {
		
			liqPrem = lpMap.getOrDefault(aa.getMatCd(), new BizLiqPremium(0.0)).getLiqPrem();
//			fwdRate = FinUitl.getForwardRateForPV(curveMap, aa.getMatCd(), 1);
			fwdRate = fwdVolAdjMap.get(aa.getMatCd());
			
			temp = new DcntSce();
			
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
//			temp.setMatCd("M" + String.format("%04d", i+1));
			temp.setMatCd(aa.getMatCd());
			
			temp.setRfRate(aa.getIntRate());
			temp.setLiqPrem(liqPrem);
			
			temp.setRefYield(0.0);
			temp.setCrdSpread(0.0);
			
			temp.setRiskAdjRfRate(aa.getIntRate() + liqPrem);
			temp.setRiskAdjRfFwdRate(fwdRate);
			
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			bottomUpSce.add(temp);
		}
		return bottomUpSce;
	}

	public static List<DcntSce> createBottomUpScenarioSmithWilson(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, List<BizLiqPremium> lpList, double ufr, double ufrt) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
		String llp = ParamUtil.getParamMap().getOrDefault("llp", "M0240");
		
//		logger.info("Thread : {},{}", sceNo, Thread.currentThread().getName());
		if(curveRst.isEmpty()) {
			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
		
		SmithWilsonModel aa = new SmithWilsonModel(curveRst, ufr, ufrt);
		SmithWilsonModel bb = new SmithWilsonModel(curveRst, lpList, ufr, ufrt);
	
		
		SEXP rst = aa.getSmithWilsonSEXP(false);
		SEXP rstAddLp = bb.getSmithWilsonSEXP(false);

		SEXP rfSpot = rst.getElementAsSEXP(0).getElementAsSEXP(3);
		SEXP rfAddSpot = rstAddLp.getElementAsSEXP(0).getElementAsSEXP(3);
		
		
		for(int i =0; i< 1200; i++) {
			temp = new DcntSce();
			
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
			temp.setMatCd("M" + String.format("%04d", i+1));
			
			temp.setRfRate(       rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			temp.setLiqPrem( rstAddLp.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal() - rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal() );
			
			temp.setRefYield(0.0);
			temp.setCrdSpread(0.0);
			
			temp.setRiskAdjRfRate(   rstAddLp.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			temp.setRiskAdjRfFwdRate(rstAddLp.getElementAsSEXP(0).getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			bottomUpSce.add(temp);
		}

		return bottomUpSce;
	}
	
//	Ris Free Curve ==> SmithWilson ==> Add LiqPremium 
	public static List<DcntSce> createBottomUpScenarioAddLp(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, List<BizLiqPremium> lpList, double ufr, double ufrt) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
		String llp = ParamUtil.getParamMap().getOrDefault("llp", "M0240");
		String tempMatCd ="";
		String fwdMatCd ="";
		
//		logger.info("Thread : {},{}", sceNo, Thread.currentThread().getName());
		if(curveRst.isEmpty()) {
			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
		
		Map<String, Double> lpMap = lpList.stream().collect(Collectors.toMap(s->s.getMatCd(), s ->s.getApplyLiqPrem()));
		Map<String, Double> spotMap = new HashMap<String, Double>();

		
		
		SmithWilsonModel aa = new SmithWilsonModel(curveRst, ufr, ufrt);
		SmithWilsonModel bb = new SmithWilsonModel(curveRst, lpList, ufr, ufrt);
	
		SEXP rst = aa.getSmithWilsonSEXP(false);
		SEXP rstAddLp = bb.getSmithWilsonSEXP(false);

		SEXP rfSpot = rst.getElementAsSEXP(0).getElementAsSEXP(3);
		SEXP rfAddSpot = rstAddLp.getElementAsSEXP(0).getElementAsSEXP(3);
		
		double lq =0.0;
		double riskFreeRate =0.0;
		
		for(int i =0; i< 1200; i++) {
			tempMatCd = "M" + String.format("%04d", i+1);
			lq = lpMap.getOrDefault(tempMatCd, 0.0);
			riskFreeRate = rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal();
			
			spotMap.put(tempMatCd, riskFreeRate + lq);
		}
		Map<String, Double> fwdMap = FinUtils.getForwardRateByMaturityMatCd(bssd, spotMap, "M0001");
		
//		logger.info("fwdMap : {}, {}, {}", spotMap.size(),fwdMap.size(), fwdMap.get("M0001"));
		
		String matCd ="";
		double intRate =0.0;
		double lqRate =0.0;
		for(int i =0; i< 1200; i++) {
			matCd ="M" + String.format("%04d", i+1);
			intRate = rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal();
			lqRate = lpMap.getOrDefault(matCd, 0.0);
			
			tempMatCd = "M" + String.format("%04d", i);
			
			temp = new DcntSce();
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
			temp.setMatCd(matCd);
			
			temp.setRfRate(  intRate);
			temp.setLiqPrem( lqRate);
			
			temp.setRefYield(0.0);
			temp.setCrdSpread(0.0);
			
			temp.setRiskAdjRfRate(intRate +  lqRate);
//			temp.setRiskAdjRfFwdRate(rstAddLp.getElementAsSEXP(0).getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			temp.setRiskAdjRfFwdRate(i==0? intRate +  lqRate: fwdMap.getOrDefault(tempMatCd, intRate +  lqRate));
			
			
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			bottomUpSce.add(temp);
		}

		return bottomUpSce;
	}
}
