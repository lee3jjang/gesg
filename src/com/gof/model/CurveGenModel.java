package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.maven.scm.command.add.AddScmResult;
import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiqPremium;
import com.gof.entity.SmithWilsonParam;
import com.gof.model.LiquidPremiumModel;
import com.gof.model.SmithWilsonModel;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;


public class CurveGenModel {
	private final static Logger logger = LoggerFactory.getLogger(CurveGenModel.class);

	public static List<BottomupDcnt> createTermStructure(String bssd, IrCurve curveMst, List<IrCurveHis> irCurveHis,  double volAdj, double ufr, double ufrt) {
		List<IrCurveHis> irCurveHisSpread = addSpread(curveMst, irCurveHis, volAdj);
		return gen(bssd, curveMst, irCurveHis, irCurveHisSpread, ufr, ufrt);

	}
	
	
	public static List<BottomupDcnt> createTermStructure(String bssd, IrCurve curveMst,  List<IrCurveHis> irCurveHis,  List<BizLiqPremium> lipPrem, double ufr, double ufrt) {
		List<IrCurveHis> irCurveHisSpread = addSpread(curveMst, irCurveHis, lipPrem);
		
		return gen(bssd, curveMst, irCurveHis, irCurveHisSpread, ufr, ufrt);
	}


	private static List<BottomupDcnt> gen(String bssd, IrCurve curveMst,  List<IrCurveHis> irCurveHis, List<IrCurveHis> irCurveHisSpread,  double ufr, double ufrt) {
		
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		BottomupDcnt temp;
		if(irCurveHis.isEmpty()) {
			logger.error("No IrCurveHis for generation BottomupDcnt");
			System.exit(0);
		}
		
		SmithWilsonModel rf      = new SmithWilsonModel(irCurveHis, ufr, ufrt);
		SmithWilsonModel rfAddLq = new SmithWilsonModel(irCurveHisSpread,   ufr, ufrt);
		
		SEXP rfRst      = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix: 
		SEXP rfAdjRst   = rfAddLq.getSmithWilsonSEXP(false).getElementAsSEXP(0);

		
		double lq=0.0;
		for(int i =0; i< 1200; i++) {
			lq = rfAdjRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal() - rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal();
			
			temp = new BottomupDcnt();
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(curveMst.getIrCurveId());
			temp.setMatCd("M" + String.format("%04d", i+1));
			
			temp.setRfRate(rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			temp.setLiqPrem(lq);
			temp.setRiskAdjRfRate(rfAdjRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			
			temp.setRiskAdjRfFwdRate(rfAdjRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			
			temp.setVol(0.0);
			bottomupRst.add(temp);
			
		}

		bottomupRst.stream().forEach(s->logger.debug("Smith Wilson Term Structure Result : {}", s.toString()));
		
		return bottomupRst;
		
	}
	
	private  static List<IrCurveHis> addSpread(IrCurve curveMst, List<IrCurveHis> irCurveHis,  List<BizLiqPremium> lipPrem) {
		List<IrCurveHis> curveRst = new ArrayList<IrCurveHis>();
		
		Map<String, Double> lpMap = lipPrem.stream().collect(Collectors.toMap(BizLiqPremium::getMatCd, BizLiqPremium::getApplyLiqPrem));
		
		double spread =0.0;
		double prevSpread =0.0;
		IrCurveHis temp;
		for(IrCurveHis aa : irCurveHis) {
			spread = lpMap.getOrDefault(aa.getMatCd(), prevSpread);
			
			temp = new IrCurveHis();
			temp.setBaseDate(aa.getBaseDate());
			temp.setIrCurveId(curveMst.getIrCurveId());
			temp.setMatCd(aa.getMatCd());
			temp.setIntRate(aa.getIntRate() + spread);
			
			temp.setSceNo("0");
			temp.setIrCurve(curveMst);
			
			prevSpread = spread;
			curveRst.add(temp);
		}
		return curveRst;
	}
	
	
	private  static List<IrCurveHis> addSpread(IrCurve curveMst, List<IrCurveHis> irCurveHis,  double spread) {
		List<IrCurveHis> curveRst = new ArrayList<IrCurveHis>();
		IrCurveHis temp;
		for(IrCurveHis aa : irCurveHis) {
			temp = new IrCurveHis();
			temp.setBaseDate(aa.getBaseDate());
			temp.setIrCurveId(curveMst.getIrCurveId());
			temp.setMatCd(aa.getMatCd());
			temp.setIntRate(aa.getIntRate() + spread);
			temp.setSceNo("0");
			temp.setIrCurve(curveMst);
			
			curveRst.add(temp);
		}
		return curveRst;
	}

}
