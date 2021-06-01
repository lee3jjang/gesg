package com.gof.test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.EsgParamDao;
import com.gof.entity.BizEsgParam;
import com.gof.util.ScriptUtil;

public class RenjinTest2 {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private final static ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<ScriptEngine>();
	
	public static void main(String[] args) {
		int core = Runtime.getRuntime().availableProcessors();
		Double[] input = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,10.0,11.0, 12.0, 13.0, 14.0, 15.0};
		Stream<Double> in =Stream.of(input);
		
		System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		
		List<String> scriptString = ScriptUtil.getScriptContents();
					
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		ScriptEngine engine = factory.getScriptEngine();
		
		try {
			for (String aa : scriptString) {
				engine.eval(aa);
			}
//			engine.put("int", intRate);
//			engine.put("mat", matOfYear);
//			engine.put("ufr", ufr);
//			engine.put("ufr.t", ufrt);
			engine.put("params", getParamBuilder().build());
			logger.info("hw : {},{},{},{}", getParamBuilder().length());
//			
//			engine.put("bse.ym", baseYymm);							//시드 넘버의 역할을 수행함.
//			engine.put("num.of.scen", sceNum);
//			engine.put("int.type", "annu");
			
//			String scriptHW = "Hw1f.simulation.run(bse.ym, int, mat, params, num.of.scen = num.of.scen, int.type= int.type, ufr=ufr, ufr.t=ufr.t)";
			String scriptHW = "Hw1f.param.adj(params,100)";
			
			SEXP hwRst = (SEXP) engine.eval(scriptHW);
//			logger.info("hw1 : {},{},{},{}", intRate, matOfYear, baseYymm, sceNum);
			logger.info("aaa : {}", hwRst);
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}

	}
	
	private static ListVector.NamedBuilder getParamBuilder(){
		String bssd ="201712";
		String id ="HW_1_VOL_SURFACE";
		
		List<BizEsgParam> bizParamHis = EsgParamDao.getBizEsgParam(bssd, "I", id);
		
		
		StringVector.Builder paramTypeBuilder = new StringVector.Builder();
		DoubleArrayVector.Builder matYearBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder valBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
//		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(paramHisList.size()));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(bizParamHis.size()));
		
		
/*		for (ParamApply aa : paramHisList) {
			paramTypeBuilder.add(aa.getParamTypCd());
			matYearBuilder.add(Double.valueOf(aa.getMatCd().split("M")[1]));
			valBuilder.add(aa.getApplParamVal());
		}*/
		
		for (BizEsgParam aa : bizParamHis) {
//			logger.info("paramHis : {},{}", aa.getParamTypCd(),Double.valueOf(aa.getMatCd().split("M")[1]) /12 );
			paramTypeBuilder.add(aa.getParamTypCd());
			matYearBuilder.add(Double.valueOf(aa.getMatCd().split("M")[1]));
			valBuilder.add(aa.getApplParamVal());
			
//			if(aa.getParamTypCd().contains("SIGMA")) {
//				valBuilder.add(aa.getApplParamVal()* 0.5);
//			}
//			else {
//				valBuilder.add(aa.getApplParamVal());
//			}
		}
//		logger.info("paramHis : {},{},{}", paramTypeBuilder.length(), matYearBuilder.length(), valBuilder.length());
		
		dfProc.add("PARAM_TYP_CD", paramTypeBuilder.build());
		dfProc.add("MAT_CD", matYearBuilder.build());
		dfProc.add("APPL_PARAM_VAL", valBuilder.build());
		
		logger.info("aaa size : {},{}",bizParamHis.size(), dfProc.getAttribute(Symbol.get("MAT_CD")));
		
		return dfProc;
	}
	
}
