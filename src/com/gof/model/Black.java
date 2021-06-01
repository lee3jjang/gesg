package com.gof.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;

import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizEsgParam;
import com.gof.entity.MvSce;
import com.gof.interfaces.Rrunnable;
import com.gof.util.ScriptUtil;

/**
 *  Controller Class for Black Model.        
 *  <p> R Script �� �����ϱ� ����  Input Data ���� �� ���� , R Script ����, Output Converting �۾��� ������.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Black implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger("HullWhite");

	private String baseYymm;
	private double[] intRate;
	private double[] matOfYear;
	private double ufr;
	private double ufrt;
//	private List<ParamApply> paramHisList = new ArrayList<ParamApply>();
	private List<BizEsgParam> bizParamHisList = new ArrayList<BizEsgParam>();
	
	public Black() {
	}

	

	/**
	 *  R Script �� ������ ����� �ݸ� �ó������� ���·� ��ȯ�ϴ� ����� ������.
	 *   
	 * @param bssd 	  : ���س��
	 * @param mvId	   : ���庯�� ID 
	 * @param modelId   : ���庯�� �ó������� �����ϱ� ���� ����
	 * @param sceNum	   : �ó����� ���� ����
	 * @param batchNo	   : �ó����� ���� ��ġ�� ���� ( �ó������� �κ������� �����ϱ� ���� �Ű�������)
     * 
	 * @return List			   :�ֽ� �ó����� 
	 */
	public List<MvSce> getBlackkScenario(String bssd, String mvId, String modelId, int sceNum, int batchNo){
		List<MvSce> irScenarioList = new ArrayList<MvSce>();
		MvSce tempSce;
		int sceNo ;
		SEXP hwRst = getModelResult(sceNum);
		
		for(int k =0; k< hwRst.getElementAsSEXP(0).length(); k++) {
			sceNo = batchNo * 100 + hwRst.getElementAsSEXP(2).getElementAsSEXP(k).asInt() ;
					
			tempSce = new MvSce();
			tempSce.setBaseDate(bssd);
			tempSce.setMvId(mvId);
			
			tempSce.setModelId(modelId);
			tempSce.setSceNo( String.valueOf(sceNo));
			tempSce.setMvValue(hwRst.getElementAsSEXP(6).getElementAsSEXP(k).asReal());
			
			tempSce.setLastModifiedBy("ESG");
			tempSce.setLastUpdateDate(LocalDateTime.now());
			
			irScenarioList.add(tempSce);
		}
		return irScenarioList;
	}
	
	/**
	 *  R Script �� �̿��� Hull White 1 Factor Model ����
	 *  @return SEXP (Renjin ����� ���� ����� Data Type)  
	*/
	private SEXP getModelResult(int sceNum) {
		System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");
		
		List<String> scriptString = ScriptUtil.getScriptContents();
		
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		ScriptEngine engine = factory.getScriptEngine();

		try {
			for (String aa : scriptString) {
				engine.eval(aa);
			}
			engine.put("int", intRate);
			engine.put("mat", matOfYear);
			engine.put("ufr", ufr);
			engine.put("ufr.t", ufrt);
			engine.put("params", getParamBuilder().build());
			engine.put("bse.ym", baseYymm);
			engine.put("num.of.scen", sceNum);
			engine.put("int.type", "annu");
			
			String scriptHW = "Hw1f.simulation.run(bse.ym, int, mat, params, num.of.scen = num.of.scen, int.type= int.type, ufr=ufr, ufr.t=ufr.t)";
			SEXP hwRst = (SEXP) engine.eval(scriptHW);

			return hwRst;
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return null;
	}
	
	private ListVector.NamedBuilder getParamBuilder(){
		StringVector.Builder paramTypeBuilder = new StringVector.Builder();
		DoubleArrayVector.Builder matYearBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder valBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
//		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(paramHisList.size()));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(bizParamHisList.size()));
		

		/*for (ParamApply aa : paramHisList) {
//			logger.info("paramHis : {},{}", aa.getParamTypCd(),Double.valueOf(aa.getMatCd().split("M")[1]) /12 );
			paramTypeBuilder.add(aa.getParamTypCd());
			matYearBuilder.add(Double.valueOf(aa.getMatCd().split("M")[1]));
			valBuilder.add(aa.getApplParamVal());
		}*/
		for (BizEsgParam aa : bizParamHisList) {
//			logger.info("paramHis : {},{}", aa.getParamTypCd(),Double.valueOf(aa.getMatCd().split("M")[1]) /12 );
			paramTypeBuilder.add(aa.getParamTypCd());
			matYearBuilder.add(Double.valueOf(aa.getMatCd().split("M")[1]));
			valBuilder.add(aa.getApplParamVal());
		}
		dfProc.add("PARAM_TYP_CD", paramTypeBuilder.build());
		dfProc.add("MAT_CD", matYearBuilder.build());
		dfProc.add("PARAM_VAL", valBuilder.build());
		
		return dfProc;
	}
}
