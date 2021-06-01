package com.gof.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.EsgMstDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SwaptionVolDao;
import com.gof.entity.EsgMst;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.entity.IrShockSce;
import com.gof.entity.ParamCalcHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SmithWilsonResult;
import com.gof.entity.SwaptionVol;
import com.gof.enums.EBoolean;
import com.gof.model.DynamicNelsonSiegel;
import com.gof.model.HullWhite2Factor;
import com.gof.model.HullWhite2FactorParameter;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;

public class SmithWilsionTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	
	
	public static void main(String[] args) {
//		aaa();
//		forwardTermStructureResult();
		forwardBucketResult();
	}
	
	
	
	private static void aaa() {
		String bssd = "201712";
		List<IrCurveHis> curveHisList = IrCurveHisDao.getKTBIrCurveHis(bssd);
		
		SmithWilsonModel sw = new SmithWilsonModel(curveHisList);
		
		for(SmithWilsonResult aa : sw.getSmithWilsionResult()) {
			logger.info("SmithWilson result  : {}", aa.toString());
			
		}
		

	}
	
	private static void forwardTermStructureResult() {
		String bssd = "201712";
		List<IrCurveHis> curveHisList = IrCurveHisDao.getKTBIrCurveHis(bssd);
		
		SmithWilsonModel sw = new SmithWilsonModel(curveHisList);
		
		for(SmithWilsonResult aa : sw.getSwForwardTermStructure(0)) {
			logger.info("SmithWilson result  : {}", aa.toString());
			
		}
		

	}
	private static void forwardBucketResult() {
		String bssd = "201712";
		List<IrCurveHis> curveHisList = IrCurveHisDao.getKTBIrCurveHis(bssd);
		
		SmithWilsonModel sw = new SmithWilsonModel(curveHisList);
		
		for(SmithWilsonResult aa : sw.getSwForwardRateAtBucket(60)){
			logger.info("SmithWilson result  : {}", aa.toString());
			
		}
		

	}
}
