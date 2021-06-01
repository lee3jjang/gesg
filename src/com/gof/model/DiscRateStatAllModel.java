package com.gof.model;

import java.util.ArrayList;
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

import com.gof.comparator.DiscRateHisComparator;
import com.gof.comparator.IrCurveHisComparator;
import com.gof.entity.DiscRateHis;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LinearRegResult;
import com.gof.entity.RAcctYieldHis;
import com.gof.entity.RCurveHis;
import com.gof.entity.RExtIrHis;
import com.gof.entity.UserDiscRateAsstRevnCumRate;
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
public class DiscRateStatAllModel implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger(DiscRateStatAllModel.class);
	
	private List<String> matCdList;
	private int[] termSet ;
	private Map<String, List<IrCurveHis>> indepVariableMap = new HashMap<String, List<IrCurveHis>>();
	private List<String> baseYymmList = new ArrayList<String>();
	
//	private Map<String,List<DiscRateHis>> discRateMap = new HashMap<String, List<DiscRateHis>>();
	private List<DiscRateHis> discRateHis = new ArrayList<DiscRateHis>();
	
	public DiscRateStatAllModel() {
		
	}
	
//	public DiscRateStatAllModel(Map<String, List<IrCurveHis>> indepVariable, Map<String, List<DiscRateHis>> discRateHis, int[] termSet, List<String> matCdList) {
//		this.termSet = termSet;
//		this.indepVariableMap = indepVariable;
//		this.discRateMap = discRateHis;
//		this.matCdList = matCdList;
//	}
	
	public DiscRateStatAllModel(Map<String, List<IrCurveHis>> indepVariable, List<DiscRateHis> discRateHis, int[] termSet, List<String> matCdList) {
		this.termSet = termSet;
		this.indepVariableMap = indepVariable;
		this.discRateHis = discRateHis;
		this.matCdList = matCdList;
		
	}
	
	public List<LinearRegResult> getExtirReg() {
		return getRegressionResult(getDependantBuilder("EXT_IR"));
	}
	
	/**
	 *  생성자에 입력한 시계열 데이터를 활용하여 자산운용수익률과 국고채와의 회귀분석 결과 산출 
	 *  
	 *  @return   회귀분석결과
	*/
	public List<LinearRegResult> getAssetYieldReg() {
		return getRegressionResult(getDependantBuilder("ASSET_YIELD"));
	}
	
	/**
	 *  생성자에 입력한 시계열 데이터를 활용하여 외부기준금리와 국고채와의 회귀분석 결과 산출 
	 *  
	 *  @return   회귀분석결과
	*/
//	public List<LinearRegResult> getBaseDiscRate() {
//		return getRegressionResult(getDependantBuilder("BASE_DISC"));
//		
//	}
	
	public LinearRegResult getBaseDiscRate() {
		return getRegressionResult(getDependantBuilder("BASE_DISC")).get(0);
		
	}
	
	private List<LinearRegResult> getRegressionResult(ListVector.NamedBuilder dependVariable) {
		List<LinearRegResult> rst = new ArrayList<>();
		LinearRegResult temp;
		
		SEXP regRst = getRegression(dependVariable);
		
//		logger.info("Sexp : {}", regRst);
//		logger.info("Sexp1 : {}", regRst.getElementAsSEXP(0).length());
		
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
	
		

	public List<String> getBaseYymmList() {
		if(baseYymmList.size()==0) {
			for(DiscRateHis aa : discRateHis) {
				baseYymmList.add(aa.getBaseYymm());
			}
		}
		return baseYymmList;
	}

	public void setBaseYymmList(List<String> baseYymmList) {
		this.baseYymmList = baseYymmList;
	}

	private SEXP getRegression(ListVector.NamedBuilder dependVariable) {
		System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.F2jBLAS");
		System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.F2jLAPACK");

		List<String> scriptString = ScriptUtil.getScriptContents();

				
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
			
//			String script = "Gs.int.lm.run(int.indep, int.dep)";
			
			
			SEXP swRst = (SEXP) engine.eval(script);
			
			return swRst;
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return null;
	}
	
	private ListVector.NamedBuilder getIndiVariableBuilder(){
		StringVector.Builder baseDateBuilder   = new StringVector.Builder();
		DoubleArrayVector.Builder ktb3YBuilder = new DoubleArrayVector.Builder();		//첫번째 입력된 변수
		DoubleArrayVector.Builder ktb5YBuilder = new DoubleArrayVector.Builder();		//두번째 입력된 변수

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(indepVariableMap.size()));
		
		
		int i = 0;
		String firstName =matCdList.get(0);
		String secondName ="";
//			secondName =matCdList.size() > 1 ? matCdList.get(1) : "NA"  ;
		if(matCdList.size()==2) {
			secondName = matCdList.get(1) ;
		}
//		logger.info("matCdList : {},{}", matCdList, secondName);
		
		
		double firstRate =0.0;
		double secontRate =0.0;
		
		/*for(String zz : getBaseYymmList()) {
			firstRate =0.0;
			secontRate =0.0;
			for(IrCurveHis aa : indepVariableMap.get(zz)) {
				
				if(matCdList.contains(aa.getMatCd())){
					if(matCdList.get(0).equals(aa.getMatCd())) {
						firstRate = aa.getIntRate();
					}
					else {
						secontRate = aa.getIntRate();
					}
				}
			}
			logger.info("rate  : {},{},{},{}", zz, firstRate, secontRate);
			
			baseDateBuilder.add(zz);
			ktb3YBuilder.add(firstRate);
			ktb5YBuilder.add(secontRate);
		}*/
		
		for(Map.Entry<String, List<IrCurveHis>> entry : indepVariableMap.entrySet()) {
			firstRate =0.0;
			secontRate =0.0;
			for(IrCurveHis aa : entry.getValue()) {
				if(matCdList.contains(aa.getMatCd())){
					if(matCdList.get(0).equals(aa.getMatCd())) {
						firstRate = aa.getIntRate();
					}
					else {
						secontRate = aa.getIntRate();
					}
				}
			}
//			logger.info("rate  : {},{},{},{}", entry.getKey(), firstRate, secontRate);
			
			baseDateBuilder.add(entry.getKey());
			ktb3YBuilder.add(firstRate);
			ktb5YBuilder.add(secontRate);
		}
		
		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add(firstName,  ktb3YBuilder.build());
		if(matCdList.size()==2) {
			dfProc.add(secondName, ktb5YBuilder.build());
		}
		
		return dfProc;
	}
	
/*	private ListVector.NamedBuilder getDependantBuilder(boolean isAssetYield){
		StringVector.Builder baseDateBuilder = new StringVector.Builder();
		
		DoubleArrayVector.Builder indiPensonBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(discRateMap.size()));
		int i = 0;

		double depVal =0.0;
		String depName ="";
		for (Map.Entry<String, List<DiscRateHis>> entry : discRateMap.entrySet()) {
			depVal =0.0;
			for(DiscRateHis aa : entry.getValue()) {
				if(isAssetYield) {
					depName ="ASSET_YIELD";
					depVal = aa.getMgtAsstYield();
				}
				else {
					depName ="BASE_DISC";
					depVal = aa.getBaseDiscRate();
				}
			}
			
			baseDateBuilder.add(entry.getKey());
			indiPensonBuilder.add(depVal);
		}
		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add(depName, indiPensonBuilder.build());
		
		return dfProc;
	}*/
	
	
	private ListVector.NamedBuilder getDependantBuilder(String dependType){
		StringVector.Builder baseDateBuilder = new StringVector.Builder();
		
		DoubleArrayVector.Builder dependVariBuilder = new DoubleArrayVector.Builder();

		ListVector.NamedBuilder dfProc = new ListVector.NamedBuilder();
		dfProc.setAttribute(Symbols.CLASS, StringVector.valueOf("data.frame"));
		dfProc.setAttribute(Symbols.ROW_NAMES, new RowNamesVector(discRateHis.size()));
		int i = 0;

		double depVal =0.0;
		String depName ="BASE_DISC";
		
		for(DiscRateHis aa : discRateHis) {
//			if(aa.getBaseDiscRate() >0.0) {
			
				depVal =0.0;
				if(dependType.equals("ASSET_YIELD")) {
					depName =dependType;
					depVal = aa.getMgtAsstYield();
				}
				else if(dependType.equals("EXT_IR")) {
					depName =dependType;
					depVal = aa.getExBaseIr();
				}
				else {
					depVal = aa.getBaseDiscRate();
	//				depVal = aa.getApplDiscRate();
				}
//			logger.info("zz :{},{}", aa.getBaseYymm(), depVal);
			
			baseDateBuilder.add(aa.getBaseYymm());
			dependVariBuilder.add(depVal);
//			}
		}
		dfProc.add("BASE_YYMM", baseDateBuilder.build());
		dfProc.add(depName, dependVariBuilder.build());
		
		return dfProc;
	}
	
	
	
}
