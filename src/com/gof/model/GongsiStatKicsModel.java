package com.gof.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class GongsiStatKicsModel implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger(GongsiStatKicsModel.class);

	private List<RAcctYieldHis> assetYieldList = new ArrayList<RAcctYieldHis>();
	private List<RExtIrHis> extIrList = new ArrayList<RExtIrHis>();
	private List<IrCurveHis> indepVariableList = new ArrayList<IrCurveHis>();
	
	private String indiVariable;
	private double[] indiVal;
	private double[] pensionVal;
	private double[] longInsVal;
	
	public GongsiStatKicsModel() {
		
	}
	public GongsiStatKicsModel(List<IrCurveHis> indepVariableList, List<RAcctYieldHis> assetYield ) {
		this.indepVariableList = indepVariableList;
		this.assetYieldList    = assetYield;
		this.indiVal =new double[assetYield.size()];
		this.pensionVal =new double[assetYield.size()];
		this.longInsVal =new double[assetYield.size()];
	}
	
	public GongsiStatKicsModel(List<IrCurveHis> indepVariableList, List<RAcctYieldHis> assetYield, List<RExtIrHis> extIrHis) {
		this.indepVariableList = indepVariableList;
		this.assetYieldList    = assetYield;
		this.extIrList         = extIrHis;
		this.indiVal =new double[assetYield.size()];
		this.pensionVal =new double[assetYield.size()];
		this.longInsVal =new double[assetYield.size()];
	}
	/**
	 *  생성자에 입력한 시계열 데이터를 활용하여 자산운용수익률과 국고채와의 회귀분석 결과 산출 
	 *  
	 *  @return   회귀분석결과
	*/
	public List<LinearRegResult> getAssetYieldReg(String bssd) {
		List<LinearRegResult> rst = new ArrayList<>();
		setInputVariable();
		
		rst.add(getLinearRegResult(bssd, "8100_개인연금", getRegression(pensionVal)));
		rst.add(getLinearRegResult(bssd, "8300_장기무배당", getRegression(longInsVal)));
		
		return rst;
	}
	
//	public List<LinearRegResult> getExtIrReg(String bssd) {
//		List<LinearRegResult> rst = new ArrayList<>();
//		setInputVariable();
//		
//		rst.add(getLinearRegResult(bssd, "KTB_Y5", getRegression(pensionVal)));
//		rst.add(getLinearRegResult(bssd, "CORP_Y3", getRegression(longInsVal)));
//		rst.add(getLinearRegResult(bssd, "MNSB_Y1", getRegression(longInsVal)));
//		rst.add(getLinearRegResult(bssd, "CD_91",   getRegression(longInsVal)));
//		
//		return rst;
//	}
	
	private LinearRegResult getLinearRegResult(String bssd, String depVari, SEXP regRst) {
		LinearRegResult temp = new LinearRegResult();
		
		
		temp.setBaseYymm(bssd);
		temp.setDepVariable(depVari);
		
		temp.setIndepVariable("KTB1M");
		temp.setAvgMonNum(1.0);
		
		temp.setRegConstant(regRst.getElementAsSEXP(0).asReal());
		temp.setRegCoef(regRst.getElementAsSEXP(1).asReal());
		temp.setRegRsqr(0.0);
		
		return temp;
	}

	private SEXP getRegression(double[] depVal) {
		System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");

		List<String> scriptString = ScriptUtil.getScriptContents();
		
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		ScriptEngine engine = factory.getScriptEngine();
		
		 
		try {
			for (String aa : scriptString) {
				engine.eval(aa);
			}
			engine.put("indiVal", indiVal);
			engine.put("depVal",   depVal);
//			String script = "lm(indiVal ~ depVal)";
			String script = "lm(depVal ~ indiVal)";
			SEXP swRst = (SEXP) engine.eval(script);
			
			logger.info("lm : {},{}", swRst);
			return swRst.getElementAsSEXP(0);		//Coefficeint ( intercept, slope)
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return null;
	}
	
	private void setInputVariable() {
		Map<String, Double> indiMap = indepVariableList.stream().collect(Collectors.toMap(s->s.getBaseYymm(),	s->s.getIntRate()));
		int k=0;
		
		indiVariable = indepVariableList.get(0).getMatCd();
		for(int j=0; j< assetYieldList.size(); j++)	{
			if(indiMap.containsKey(assetYieldList.get(j).getBaseYymm())){
				k=k+1 ;
				indiVal[k] = indiMap.get(assetYieldList.get(j).getBaseYymm());
				pensionVal[k] = assetYieldList.get(j).getIndiPenson();
				longInsVal[k]=assetYieldList.get(j).getLongIns();
//				logger.info("zzzzzzzzzzzz : {},{},{}", assetYieldList.get(j).getBaseYymm(), assetYieldList.get(j).getLongIns(), indiMap.get(assetYieldList.get(j).getBaseYymm()));
			}
		}
//		Arrays.stream(indiVal).forEach(s -> logger.info("aaa : {}", s));
//		Arrays.stream(pensionVal).forEach(s -> logger.info("bbb : {}", s));
//		Arrays.stream(longInsVal).forEach(s -> logger.info("ccc : {}", s));
	}
}
