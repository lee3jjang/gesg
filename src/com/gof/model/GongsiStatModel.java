package com.gof.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.gof.entity.IrCurveHis;
import com.gof.entity.LinearRegResult;
import com.gof.entity.RAcctYieldHis;
import com.gof.entity.RCurveHis;
import com.gof.entity.RExtIrHis;
import com.gof.entity.UserDiscRateExBaseIr;
import com.gof.interfaces.Rrunnable;
import com.gof.util.ScriptUtil;

/**
 *  <p> 회귀분석  모형	</p>        
 *  <p> R Script 를 실행하기 위한  Input Data 생성 및 관리 , R Script 실행, Output Converting 작업을 수행함.</p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class GongsiStatModel implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger("HullWhite");

	private List<RCurveHis> indepVariableList = new ArrayList<RCurveHis>();
	private List<RExtIrHis> extIrList	 = new ArrayList<RExtIrHis>();
	private List<RAcctYieldHis> assetYieldList = new ArrayList<RAcctYieldHis>();
	
	public GongsiStatModel() {
		
	}
	
	public GongsiStatModel(List<RCurveHis> indepVariable, List<RAcctYieldHis> assetYield, List<RExtIrHis> extIrHis) {
		
		this.indepVariableList = indepVariable;
		this.assetYieldList = assetYield;
		this.extIrList   = extIrHis;
		
	}
	
	/**
	 *  생성자에 입력한 시계열 데이터를 활용하여 자산운용수익률과 국고채와의 회귀분석 결과 산출 
	 *  
	 *  @return   회귀분석결과
	*/
	public List<LinearRegResult> getAssetYieldReg() {
		return getRegressionResult(getAssetYieldBuilder());
		
	}
	
	/**
	 *  생성자에 입력한 시계열 데이터를 활용하여 외부기준금리와 국고채와의 회귀분석 결과 산출 
	 *  
	 *  @return   회귀분석결과
	*/
	public List<LinearRegResult> getExtIrReg() {
		return getRegressionResult(getExtIrBuilder());
		
	}
	
	private List<LinearRegResult> getRegressionResult(ListVector.NamedBuilder dependVariable) {
		List<LinearRegResult> rst = new ArrayList<>();
		LinearRegResult temp;
		
		SEXP regRst = getRegression(dependVariable);
		
		logger.info("Sexp : {}", regRst);
		logger.info("Sexp1 : {}", regRst.getElementAsSEXP(0).length());
		
		for( int i =0; i< regRst.getElementAsSEXP(0).length(); i++) {
			temp = new LinearRegResult();
			temp.setBaseYymm(     regRst.getElementAsSEXP(0).getElementAsSEXP(i).asString());
			temp.setDepVariable(  regRst.getElementAsSEXP(1).getElementAsSEXP(i).asString());
			temp.setIndepVariable(regRst.getElementAsSEXP(2).getElementAsSEXP(i).asString());
			temp.setAvgMonNum(regRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
			
			temp.setRegConstant(regRst.getElementAsSEXP(4).getElementAsSEXP(i).asReal());
			temp.setRegCoef(regRst.getElementAsSEXP(5).getElementAsSEXP(i).asReal());
			temp.setRegRsqr(regRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			
			rst.add(temp);
		}
		return rst;
	}
	
		

	private SEXP getRegression(ListVector.NamedBuilder dependVariable) {
		System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");

		List<String> scriptString = ScriptUtil.getScriptContents();
		int[] termSet = {1, 3}; 
				
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		ScriptEngine engine = factory.getScriptEngine();

		try {
			for (String aa : scriptString) {
				engine.eval(aa);
			}
			engine.put("int.indep", getIndiVariableBuilder().build());
			engine.put("int.dep",   dependVariable.build());
			engine.put("ma.term.set", termSet);
			
			String script = "Gs.int.lm.run(int.indep, int.dep, ma.term.set=ma.term.set)";
//			engine.eval("print(int.dep)");
//			engine.eval("print(int.indep)");
			SEXP swRst = (SEXP) engine.eval(script);
			
			return swRst;
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return null;
	}
	
	private ListVector.NamedBuilder getIndiVariableBuilder(){
		StringVector.Builder baseDateBuilder   = new StringVector.Builder();
		DoubleArrayVector.Builder ktb3YBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder ktb5YBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(indepVariableList.size()));
		
		
		int i = 0;
		boolean isKtb3Y =false;
		boolean isKtb5Y =false;
		for (RCurveHis aa : indepVariableList) {
			baseDateBuilder.add(aa.getBaseYymm());
			ktb3YBuilder.add(aa.getKtb3Y());
			ktb5YBuilder.add(aa.getKtb5Y());
		}

		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add("M0036", ktb3YBuilder.build());
		dfProc.add("M0060", ktb5YBuilder.build());
		
		
		return dfProc;
	}
	
	private ListVector.NamedBuilder getAssetYieldBuilder(){
		StringVector.Builder baseDateBuilder = new StringVector.Builder();
		DoubleArrayVector.Builder longInsBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder indiPensonBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(assetYieldList.size()));
		int i = 0;

		for (RAcctYieldHis aa : assetYieldList) {
//			logger.info("paramHis : {},{}", aa.getParamTypCd(),Double.valueOf(aa.getMatCd().split("M")[1]) /12 );
			baseDateBuilder.add(aa.getBaseYymm());
			longInsBuilder.add(aa.getLongIns());
			indiPensonBuilder.add(aa.getIndiPenson());
		}

		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add("8300_장기무배당", longInsBuilder.build());
		dfProc.add("8100_개인연금", indiPensonBuilder.build());
		
		return dfProc;
	}
	
	private ListVector.NamedBuilder getExtIrBuilder(){
		StringVector.Builder baseDateBuilder = new StringVector.Builder();
		DoubleArrayVector.Builder ktb5YBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder corp3YBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder mnsb1YBuilder = new DoubleArrayVector.Builder();
		DoubleArrayVector.Builder cd91DBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(extIrList.size()));
		int i = 0;

		for (RExtIrHis aa : extIrList) {
//			logger.info("paramHis : {},{}", aa.getParamTypCd(),Double.valueOf(aa.getMatCd().split("M")[1]) /12 );
			baseDateBuilder.add(aa.getBaseYymm());
			ktb5YBuilder.add(aa.getKtb5Y());
			corp3YBuilder.add(aa.getCorp3Y());
			mnsb1YBuilder.add(aa.getMnsb1Y());
			cd91DBuilder.add(aa.getCd91D());
		}

		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add("KTB_Y5", ktb5YBuilder.build());
		dfProc.add("CORP_Y3", corp3YBuilder.build());
		dfProc.add("MNSB_Y1", mnsb1YBuilder.build());
		dfProc.add("CD_91", cd91DBuilder.build());
		
		return dfProc;
	}
	
	
}
