package com.gof.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.entity.BizDiscountRateSce;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.DcntSce;
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SmithWilsonResult;
import com.gof.util.ScriptUtil;


/**
 *  <p> Smith Wilson 모형        
 *  <p> Hull and White 1 Factor, 2 Factor, CIR, Vacicek 등으로 산출한 금리기간 구조의 보외법을 적용하여 전제금리기간 구조 산출함.  
 *  <p>  Script 를 실행하기 위한  Input Data 생성 및 관리 , R Script 실행, Output Converting 작업을 수행함.</p>
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class SmithWilsonModel {
	private final static Logger logger = LoggerFactory.getLogger(SmithWilsonModel.class);
	
	private final static ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<ScriptEngine>();
	
	private double[] intRate;
	private double[] matOfYear;
	private double  ufr;
	private double  ufrt ;
	private double  minAlpha = 0.001;
	private double  maxAlpha = 1;
	private double  tolerance = 0.001;
	private double  llp	=20;
//	private String  baseDate;	
	private String  dateApplyType ="F";
	private String  compound = "annu";
	private double  projectionYear = 100;
	private String sceNo;
	private String bssd;
	private String irCurveId;
	private List<IrCurveHis> irCurveHisList = new ArrayList<IrCurveHis>();
	private SEXP smithWilsonSEXP;
	
	
	public SmithWilsonModel() {
	
	}
	
	public SmithWilsonModel(double[] intRate, double[] matOfYear, double ufr, double ufrt) {
		this.intRate = intRate;
		this.matOfYear = matOfYear;
		this.ufr = ufr;
		this.ufrt = ufrt;
	}
	
	public SmithWilsonModel(List<IrCurveHis> curveHisList, double ufr, double ufrt) {
		this();
		this.irCurveHisList = curveHisList;
		this.intRate = new double[curveHisList.size()];
		this.matOfYear = new double[curveHisList.size()];
		for (int i = 0; i < matOfYear.length; i++) {
			intRate[i] = curveHisList.get(i).getIntRate();
			matOfYear[i] = Double.valueOf(curveHisList.get(i).getMatCd().split("M")[1]) / 12;
		}
		this.ufr = ufr;
		this.ufrt = ufrt;
		init();
	}
	
	public SmithWilsonModel(List<IrCurveHis> curveHisList, double volAdj, double ufr, double ufrt) {
		this();
		this.irCurveHisList = curveHisList;
		this.intRate = new double[curveHisList.size()];
		this.matOfYear = new double[curveHisList.size()];
		
		for (int i = 0; i < matOfYear.length; i++) {
			intRate[i] = curveHisList.get(i).getIntRate() + volAdj;
			matOfYear[i] = Double.valueOf(curveHisList.get(i).getMatCd().split("M")[1]) / 12;
		}
		
		this.ufr = ufr;
		this.ufrt = ufrt;
		init();
	}
	
	public SmithWilsonModel(List<IrCurveHis> curveHisList, List<BizLiqPremium> liqList, double ufr, double ufrt) {
		this();
		this.irCurveHisList = curveHisList;
		this.intRate = new double[curveHisList.size()];
		this.matOfYear = new double[curveHisList.size()];
		
		Map<String, BizLiqPremium> lpMap = liqList.stream().collect(Collectors.toMap(s -> s.getMatCd(), Function.identity()));
		double tempLp =0.0;
		for (int i = 0; i < matOfYear.length; i++) {
			if (lpMap.containsKey(curveHisList.get(i).getMatCd())) {
				tempLp = lpMap.get(curveHisList.get(i).getMatCd()).getLiqPrem();
			}

			if (curveHisList.get(i).getMatCd().equals("M0240")) {
				tempLp = 0.0;
			}
			intRate[i] = curveHisList.get(i).getIntRate() + tempLp;
			matOfYear[i] = Double.valueOf(curveHisList.get(i).getMatCd().split("M")[1]) / 12;
		}
		this.ufr = ufr;
		this.ufrt = ufrt;
		init();
	}
	
	private void init() {
		if( irCurveHisList.isEmpty()){
			logger.error("There no Ir Curve His for SmithWilson");
			System.exit(1);
		}
		
		IrCurveHis temp = irCurveHisList.get(0);
		
		this.sceNo = "0";
		if(temp ==null) {
			
		}
		else {
			this.bssd= temp.getBaseYymm();
			this.irCurveId = temp.getIrCurveId();
			this.sceNo = temp.getSceNo()==null ? "0": temp.getSceNo();
		}
	}
	
//	public SmithWilsonModel(List<IrCurveHis> curveHisList, List<BizLiqPremiumUd> liqList, double ufr, double ufrt) {
//		this();
//		this.intRate = new double[curveHisList.size()];
//		this.matOfYear = new double[curveHisList.size()];
//		
//		Map<String, BizLiqPremiumUd> lpMap = liqList.stream().collect(Collectors.toMap(s -> s.getMatCd(), Function.identity()));
//		double tempLp =0.0;
//		for (int i = 0; i < matOfYear.length; i++) {
//			if (lpMap.containsKey(curveHisList.get(i).getMatCd())) {
////				tempLp = lpMap.get(curveHisList.get(i).getMatCd()).getLiqPrem();
//				tempLp = lpMap.get(curveHisList.get(i).getMatCd()).getApplyLiqPrem();
//			}
//			if (curveHisList.get(i).getMatCd().equals("M0240")) {
//				tempLp = 0.0;
//			}
//			intRate[i] = curveHisList.get(i).getIntRate() + tempLp;
//			matOfYear[i] = Double.valueOf(curveHisList.get(i).getMatCd().split("M")[1]) / 12;
//		}
//		
//		this.ufr = ufr;
//		this.ufrt = ufrt;
//	}
	
	public SmithWilsonModel(List<IrCurveHis> curveHisList) {
		this();
		
		this.intRate = new double[curveHisList.size()];
		this.matOfYear = new double[curveHisList.size()];
		for (int i = 0; i < matOfYear.length; i++) {
			intRate[i] = curveHisList.get(i).getIntRate();
			matOfYear[i] = Double.valueOf(curveHisList.get(i).getMatCd().split("M")[1]) / 12;
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		logger.info("Curve currencny : {}", curveHisList.get(0).toString());
		param.put("CUR_CD",curveHisList.get(0).getIrCurve().getCurCd());
		
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, param);
		this.ufr  = swParam.get(0) ==null? 0.045: swParam.get(0).getUfr();
		this.ufrt = swParam.get(0) ==null? 60   : swParam.get(0).getUfrT();

	}
	
	
	public SmithWilsonModel(String bssd, String curveId, String sceNo, List<DcntSce> curveSceList) {
		this();

		this.bssd= bssd;
		this.irCurveId = curveId;
		this.sceNo = sceNo;
		this.intRate = new double[curveSceList.size()];
		this.matOfYear = new double[curveSceList.size()];
		for (int i = 0; i < matOfYear.length; i++) {
			intRate[i] = curveSceList.get(i).getRiskAdjRfRate();
			matOfYear[i] = Double.valueOf(curveSceList.get(i).getMatCd().split("M")[1]) / 12;
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
//		logger.info("Curve currencny : {}", curveSceList.get(0).toString());
		String curCd = curveSceList.get(0).getIrCurveId().split("_")[1];
		param.put("CUR_CD",curCd);
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, param);
		this.ufr  = swParam.get(0) ==null? 0.045: swParam.get(0).getUfr();
		this.ufrt = swParam.get(0) ==null? 60   : swParam.get(0).getUfrT();

	}
	
	public SEXP getSmithWilsonSEXP() {
		return getSmithWilsonSEXP(true);
	}
	
	/**
	 *  R Script 를 이용한 Smith Wilson 의 결과 산출
	 *  @param isFwdGen		: forward rate 산출 여부 
	 *  @return SEXP (Renjin 모듈의 산출 결과의 Data Type)  
	*/
	public SEXP getSmithWilsonSEXP(boolean isFwdGen) {
		if(smithWilsonSEXP ==null) {
//			logger.info("Run Smith Wilson ....");
			System.setProperty("com.github.fommil.netlib.BLAS","com.github.fommil.netlib.F2jBLAS");
			System.setProperty("com.github.fommil.netlib.LAPACK","com.github.fommil.netlib.F2jLAPACK");
			
			List<String> scriptString = ScriptUtil.getScriptContents();
			
			ScriptEngine engine = ENGINE.get();
			if(engine==null) {
				RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
				engine = factory.getScriptEngine();
				ENGINE.set(engine);
			}
			
			try {
				for(String aa : scriptString) {
					engine.eval( aa);
				}
				
				engine.put("int", intRate);
				engine.put("mat", matOfYear);
				engine.put("ufr", ufr);
				engine.put("ufr.t", ufrt);
				engine.put("proj.y", projectionYear);
				
				engine.put("isFwdCurve", isFwdGen);
				engine.put("compound", compound);
				
//				engine.put("min.alpha", minAlpha);
//				engine.put("max.alpha" ,maxAlpha);
//				engine.put("tol", tolerance);
//				engine.put("llp" ,llp);
//				engine.put("base.date", baseDate);
//				engine.put("isTerm", dateApplyType);
//				engine.put("l2", 1/12);
				
				String script = "SW.run(int, mat, ufr, ufr.t, proj.y = proj.y, ts.proj.tf= isFwdCurve, int.type = compound)";
				
				
				smithWilsonSEXP = (SEXP)engine.eval(script);
				return smithWilsonSEXP;
			} catch (Exception e) {
				logger.error("Renjin Error : {}", e);
			}
		}
		return smithWilsonSEXP;
	}
	
	
	public List<SmithWilsonResult> getSmithWilsionResult(){
		return getSmithWilsionResult(true);
	}
	
	public List<SmithWilsonResult> getSmithWilsionResult(boolean isFwdGen){
		List<SmithWilsonResult> rstList = new ArrayList<SmithWilsonResult>();
		SmithWilsonResult temp;
		
		//smith wilsion 의 spot term structure...
		SEXP modelRst = getSmithWilsonSEXP(isFwdGen).getElementAsSEXP(0);
//		logger.info("in the sw Model : {}", modelRst);
		int cnt = modelRst.getElementAsSEXP(0).length();
		
		for(int k =0; k < cnt; k++) {
			temp = new  SmithWilsonResult();
			temp.setBaseYymm(bssd);
			temp.setIrCurveId(irCurveId);
			temp.setSceNo(sceNo);
			
			temp.setTimeFactor(    modelRst.getElementAsSEXP(0).getElementAsSEXP(k).asReal());
			temp.setMonthNum(      modelRst.getElementAsSEXP(1).getElementAsSEXP(k).asInt());
			temp.setSpotCont(      modelRst.getElementAsSEXP(2).getElementAsSEXP(k).asReal());
			temp.setSpotAnnual(    modelRst.getElementAsSEXP(3).getElementAsSEXP(k).asReal());
			temp.setDiscountFactor(modelRst.getElementAsSEXP(4).getElementAsSEXP(k).asReal());
			temp.setFwdCont(       modelRst.getElementAsSEXP(5).getElementAsSEXP(k).asReal());
			temp.setFwdAnnual(     modelRst.getElementAsSEXP(6).getElementAsSEXP(k).asReal());
			temp.setFwdMonthNum(1);
			
			temp.setMatCd("M" + String.format("%04d", temp.getMonthNum()));
			
			rstList.add(k, temp);
		}
		return rstList;
	}
	
	public List<IrCurveHis> getIrCurveHisList(String bssd){
		List<IrCurveHis> rstList = new ArrayList<IrCurveHis>();
		IrCurveHis temp;

		SEXP modelRst = getSmithWilsonSEXP(false).getElementAsSEXP(0);
		int cnt = modelRst.getElementAsSEXP(0).length();
		
		for(int k =0; k < cnt; k++) {
			temp = new  IrCurveHis();
			
			temp.setBaseDate(bssd);
			temp.setMatCd("M" + String.format("%04d", modelRst.getElementAsSEXP(1).getElementAsSEXP(k).asInt()));
			temp.setSceNo("0");
			temp.setIntRate(    modelRst.getElementAsSEXP(3).getElementAsSEXP(k).asReal());
			temp.setForwardNum(0);
			
			
			
			rstList.add(k, temp);
		}
		return rstList;
	}
		
	public List<SmithWilsonResult> getSwForwardRateAtBucket(String matCd){
		int maturityTerm = Integer.parseInt(matCd.split("M")[1]);
		return getSwForwardRateAtBucket(maturityTerm);
	}
	
	
	/**
	 *  Smith Wilson 을 적용한 후   , 만기 Bucket 의 월별 미래 추정결과
	 *  @param maturityTerm  만기Bucket 
	 *  @return  특정 만기의 미래 금리 추정치   
	*/
	public List<SmithWilsonResult> getSwForwardRateAtBucket(int maturityTerm){
		List<SmithWilsonResult> rstList = new ArrayList<SmithWilsonResult>();
		SmithWilsonResult temp;
		
		//Smithwilsion 의 Forward TermStructure ...
		SEXP modelRst = getSmithWilsonSEXP().getElementAsSEXP(1);					// sw dataFrame 의 2번째 데이터인 forward term structure..					
		
		int cnt = modelRst.length();												//가로 : time, month, forward term 1200 개 ==> 1202
		int cnt1 = modelRst.getElementAsSEXP(0).length();							//세로 : maturity term : 1200개 
		
		int matNum =0; 
		
		for(int j =2; j < cnt; j++) {
			temp = new  SmithWilsonResult();
			
			temp.setTimeFactor(modelRst.getElementAsSEXP(0).getElementAsSEXP(maturityTerm-1).asReal());
			temp.setMonthNum(modelRst.getElementAsSEXP(1).getElementAsSEXP(maturityTerm-1).asInt());
			
			temp.setFwdMonthNum(j-1);
			temp.setSpotAnnual(modelRst.getElementAsSEXP(j).getElementAsSEXP(maturityTerm-1).asReal());
			rstList.add(temp);
		}
		return rstList;
	}
	
	/**
	 *  Smith Wilson 을 적용한 후 , 입력한 미래시점 월수 시점의 금리 기간 구조  
	 *  @param forwardTerm   미래시점 월수
	 *  @return  미래 금리기간구조  
	*/
	public List<SmithWilsonResult> getSwForwardTermStructure(int forwardTerm){
		List<SmithWilsonResult> rstList = new ArrayList<SmithWilsonResult>();
		SmithWilsonResult temp;
		
		//Smithwilsion 의 Forward TermStructure ...
		SEXP modelRst = getSmithWilsonSEXP().getElementAsSEXP(1);					// sw dataFrame 의 2번째 데이터인 fowrader term structure..					
		
		int cnt = modelRst.length();												//가로 : time, month, forward term 1200 개 ==> 1202
		int cnt1 = modelRst.getElementAsSEXP(0).length();							//세로 : maturity term : 1200개 
		
		int matNum =0; 
		
		if(forwardTerm < 1) {
			forwardTerm = 1;
		}
		
		for(int j =0; j < cnt1; j++) {
			temp = new  SmithWilsonResult();
			
			temp.setTimeFactor(modelRst.getElementAsSEXP(0).getElementAsSEXP(j).asReal());
			temp.setMonthNum(modelRst.getElementAsSEXP(1).getElementAsSEXP(j).asInt());
			
			temp.setFwdMonthNum(forwardTerm);
			temp.setSpotAnnual(modelRst.getElementAsSEXP(1 + forwardTerm).getElementAsSEXP(j).asReal());
			rstList.add(temp);
		}
		return rstList;
	}

	public double getSmithWilsonAlphaValue() {
		SEXP modelRst = getSmithWilsonSEXP();
		return modelRst.getElementAsSEXP(0).asReal();
	}
	
	private SEXP getSmithWilsonAlpha() {
			System.setProperty("com.github.fommil.netlib.BLAS","com.github.fommil.netlib.F2jBLAS");
			System.setProperty("com.github.fommil.netlib.LAPACK","com.github.fommil.netlib.F2jLAPACK");
			
			List<String> scriptString = ScriptUtil.getScriptContents();
			
			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
			ScriptEngine engine = factory.getScriptEngine();
			try {
				for(String aa : scriptString) {
					engine.eval( aa);
				}
				
				engine.put("int", intRate);
				engine.put("mat", matOfYear);
				engine.put("ufrc", ufr);
				engine.put("ufr.t", ufrt);
				
//				engine.put("min.alpha", minAlpha);
//				engine.put("max.alpha" ,maxAlpha);
//				engine.put("tol", tolerance);
//				engine.put("llp" ,llp);
//				engine.put("base.date", baseDate);
//				engine.put("isTerm", dateApplyType);
//				engine.put("compound", compound);
//				engine.put("l2", 1/12);
//				engine.put("proj.y", projectionYear);
//				engine.put("isFwdCurve", true);
				
				String script = "SW.alpha.find(int, mat, ufrc, ufr.t)";
				
				SEXP rst = (SEXP)engine.eval(script);
	//			logger.error("Renjin aaa : {},{}", matOfYear, rst);
				return rst;
			} catch (Exception e) {
				logger.error("Renjin Error : {}", e);
			}
			return null;
		}
	
	public List<IrCurveHis> convertToIrCurveHis(boolean isFwdGen){
		List<SmithWilsonResult> swRst = getSmithWilsionResult(isFwdGen);
		
		return swRst.stream().map(s -> s.convertToIrCurveHis()).collect(Collectors.toList());
	}
	
	
	public List<BizDiscountRateSce> convertToIrCurveSce(boolean isFwdGen, String bizDv){
		List<SmithWilsonResult> swRst = getSmithWilsionResult(isFwdGen);
		
		return swRst.stream().map(s -> s.convertToIrCurveSce(bizDv)).collect(Collectors.toList());
	}
	
//	public List<BottomupDcnt> convertToBottomuDcnt(boolean isFwdGen){
//		List<SmithWilsonResult> swRst = getSmithWilsionResult(isFwdGen);
//		
//		return swRst.stream().map(s -> s.convertToIrCurveHis()).collect(Collectors.toList());
//	}
//	public List<DcntSce> convertToDcntSce(boolean isFwdGen){
//		List<SmithWilsonResult> swRst = getSmithWilsionResult(isFwdGen);
//		
//		return swRst.stream().map(s -> s.convertToIrCurveHis()).collect(Collectors.toList());
//	}
}
