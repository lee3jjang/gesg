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
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.interfaces.Rrunnable;
import com.gof.util.ScriptUtil;

/**
 *  Controller Class for Vacicek Model.        
 *  <p> R Script �� �����ϱ� ����  Input Data ���� �� ���� , R Script ����, Output Converting �۾��� ������.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Vasicek implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger(Vasicek.class);

	private String baseYymm;
	private double shortRate =0.01;
	private double revSpeed =0.02;
	private double revLevel =0.02;
	private double sigma =0.01;
	private List<BizEsgParam> bizParamHisList = new ArrayList<BizEsgParam>();
	
	public Vasicek() {
	}
	
	/**
	 *  ��������, �ݸ� ������ �Ű������� �Ű������� ������ ������
	 * @param  shortRate     : ShortRate( ���������ݸ�)  
	 * @param  param     : �ݸ����� �Ű����� 
 	 */
	public Vasicek(double shortRate, List<BizEsgParam> param) {
		this.bizParamHisList = param;
		this.shortRate = shortRate;
		for(BizEsgParam aa : param) {
			if(aa.getParamTypCd().toUpperCase().equals("REV_SPEED")) {
				revSpeed = aa.getApplParamVal();
			}
			else if(aa.getParamTypCd().toUpperCase().equals("REV_LEVEL")) {
				revLevel = aa.getApplParamVal();
			}
			else if(aa.getParamTypCd().toUpperCase().equals("SIGMA")) {
				sigma = aa.getApplParamVal();
			}
		}
		param.forEach(s -> logger.info("vasicek : {},{} ", s.getParamTypCd(), s.getApplParamVal()));
		logger.info("vasicek : {},{}, {},{}", shortRate, sigma, revSpeed, revLevel);
	}
	/**
	 *  ��������, �ݸ� ������ �Ű������� �Ű������� ������ ������
	 *   
	 * @param  baseDate  : ��������
	 * @param  shortRate     : ShortRate( ���������ݸ�)
	 * @param  param     : �ݸ����� �Ű����� 
 	 */
	public Vasicek(String baseYymm, double shortRate , List<BizEsgParam> param) {
		this(shortRate, param);
		this.baseYymm = baseYymm;
	}

	/**
	 *  R Script �� ������ ����� �ݸ� �ó������� ���·� ��ȯ�ϴ� ����� ������.
	 *   
	 * @param  bssd 	  : ���س��
	 * @param  irCurveId : �ݸ�� ID 
	 * @param  modelId   : �ݸ� �ó������� �����ϱ� ���� ����
	 * @param  sceNum	   : �ó����� ���� ����
	 * @param  batchNo	   : �ó����� ���� ��ġ�� ���� ( �ó������� �κ������� �����ϱ� ���� �Ű�������)
     * 
	 * @return List			   :�ݸ� �ó����� 
	 */
	public List<IrSce> getVasicekScenario(String bssd, String irCurveId, String modelId, int sceNum, int batchNo){
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		IrSce tempSce;
		int sceNo ;
		SEXP vasicekRst = getModelResult(sceNum).getElementAsSEXP(0);
		
//		logger.info("vasicek : {},{}" , shortRate, getModelResult(sceNum));
		
		for(int k =0; k< vasicekRst.getElementAsSEXP(0).length(); k++) {
			sceNo = ( batchNo -1) * sceNum + vasicekRst.getElementAsSEXP(2).getElementAsSEXP(k).asInt() ;
					
			tempSce = new IrSce();
			tempSce.setBaseDate(bssd);
			tempSce.setIrCurveId(irCurveId);
			
			tempSce.setIrModelId(modelId);
			tempSce.setSceNo( String.valueOf(sceNo));
			tempSce.setMatCd("M"+ String.format("%04d", vasicekRst.getElementAsSEXP(4).getElementAsSEXP(k).asInt()));
			tempSce.setRfIr(vasicekRst.getElementAsSEXP(6).getElementAsSEXP(k).asReal());
			
			tempSce.setLastModifiedBy("ESG");
			tempSce.setLastUpdateDate(LocalDateTime.now());
			
			irScenarioList.add(tempSce);
		}
		return irScenarioList;
	}
	
	/**
	 *  R Script �� �̿��� ESG �ݸ����� ����
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

			engine.put("shortRate", shortRate);
			engine.put("revSpeed", revSpeed);
			engine.put("revLevel", revLevel);
			engine.put("sigma", sigma);
			engine.put("baseYymm", baseYymm);
			engine.put("sceNum", sceNum);
			engine.put("intType", "annu");
			
			String scriptHW = "Vasi.run(bse.ym=baseYymm, r0=shortRate, rev_speed=revSpeed, rev_level=revLevel, sigma=sigma, num.of.scen = sceNum, int.type= intType)";
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
