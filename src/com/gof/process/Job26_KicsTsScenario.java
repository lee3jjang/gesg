package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.DcntSce;
import com.gof.entity.IrCurveHis;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;

/**
*  <p> BottomUp 할인율의 금리 기간 구조를 바탕으로 금리 시나리오를 생성함.
*  <p> 1. 무위험 금리를 이용하여 생성한 시나리오 ({@link Job14_EsgScenario}
*  <p> 2. 생성한 시나리오 별로 유동성 프리미엄을 적용 
*  <p> 3. 유동성 프리미엄이 반영된 시나리오에 Smith Wilson ({@link SmithWilsonModel} 보외/보간법을 적용하여 시나리오 생성함.
*  
* @author takion77@gofconsulting.co.kr 
* @version 1.0
*/
public class Job26_KicsTsScenario {
	
	private final static Logger logger = LoggerFactory.getLogger("BottomUp");

	public static List<DcntSce> createKicsTsScenario(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, double volAdj) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
			
		if(curveRst.isEmpty()) {
			logger.warn("Risk Free Scenario Data Error :   Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
		if(curveRst.size()!=1200) {
			logger.warn("Risk Free Sceanrio Data Warn :  Data of {} is not 1200 Months at {} ", irCurveId, bssd );
		}
		double liqPrem =0.0;
		double fwdRate =0.0;
		Map<String, IrCurveHis> curveMap = curveRst.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
	
		List<IrCurveHis> curveVolAdjRst = new ArrayList<>();
		for(IrCurveHis aa : curveRst) {
			IrCurveHis tempCurve = new IrCurveHis(aa.getBaseDate(), aa);
			if(aa.getMatCd().compareTo("M0240") < 1) {
				tempCurve.setIntRate(aa.getIntRate() + volAdj);
			}
			curveVolAdjRst.add(tempCurve);
		}
		
		Map<String, IrCurveHis> curveVolAdjMap = curveVolAdjRst.stream().collect(Collectors.toMap(s->s.getMatCd(), Function.identity()));
		
		Map<String, Double> fwdMap = FinUtils.getForwardRateForPV(curveMap);
		Map<String, Double> fwdVolAdjMap = FinUtils.getForwardRateForPV(curveVolAdjMap);
		
		for (IrCurveHis aa : curveRst) {
		
			fwdRate = fwdVolAdjMap.get(aa.getMatCd());
			
			temp = new DcntSce();
			
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
			temp.setMatCd(aa.getMatCd());
			
			temp.setRfRate(aa.getIntRate());
			if(aa.getMatCd().compareTo("M0240") < 1) {
				temp.setLiqPrem(volAdj);
			}
			else {
				temp.setLiqPrem(0.0);
			}
			
			temp.setRefYield(0.0);
			temp.setCrdSpread(0.0);
			
			temp.setRiskAdjRfRate(aa.getIntRate() + temp.getLiqPrem());
			temp.setRiskAdjRfFwdRate(fwdRate);
			
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			bottomUpSce.add(temp);
		}
		return bottomUpSce;
	}

	public static List<DcntSce> createKicsTsSmithWilson(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, double volAdj, double ufr, double ufrt) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
//		logger.info("IR Scenario Thread and Batch No : {}, {}", sceNo, Thread.currentThread().getName());
//		volAdj = volAdj + 0.01;
		if(curveRst.isEmpty()) {
			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
			
		SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
		SmithWilsonModel rfAddLq = new SmithWilsonModel(curveRst, volAdj, ufr, ufrt);

		SEXP rst = rf.getSmithWilsonSEXP(false);
		SEXP rstAddLp = rfAddLq.getSmithWilsonSEXP(false);

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
	
	public static List<DcntSce> createKicsTsSmithWilsonAddVolAdj(String bssd, String sceNo, String irCurveId, List<IrCurveHis> curveRst, double volAdj, double ufr, double ufrt) {
		List<DcntSce> bottomUpSce = new ArrayList<DcntSce>();
		DcntSce temp;
//		logger.info("IR Scenario Thread and Batch No : {}, {}", sceNo, Thread.currentThread().getName());
//		volAdj = volAdj + 0.01;
		if(curveRst.isEmpty()) {
			logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", irCurveId, bssd );
			return new ArrayList<DcntSce>();
		}
			
		SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
		SmithWilsonModel rfAddLq = new SmithWilsonModel(curveRst, volAdj, ufr, ufrt);

		SEXP rst = rf.getSmithWilsonSEXP(false);
		SEXP rstAddLp = rfAddLq.getSmithWilsonSEXP(false);

		for(int i =0; i< 1200; i++) {
			temp = new DcntSce();
			
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
			temp.setMatCd("M" + String.format("%04d", i+1));
			
			temp.setRfRate(  rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			temp.setLiqPrem( volAdj);
			
			temp.setRefYield(0.0);
			temp.setCrdSpread(0.0);
			
			temp.setRiskAdjRfRate( rst.getElementAsSEXP(0).getElementAsSEXP(3).getElementAsSEXP(i).asReal() + volAdj);
			temp.setRiskAdjRfFwdRate(rstAddLp.getElementAsSEXP(0).getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			bottomUpSce.add(temp);
		}

		return bottomUpSce;
	}
}
