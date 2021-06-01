package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ejml.simple.SimpleMatrix;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.TopDownDao;
import com.gof.entity.AssetCf;
import com.gof.entity.AssetClassYield;
import com.gof.entity.AssetYield;
import com.gof.entity.BizCrdSpreadUd;
import com.gof.entity.CashFlowMatchAdj;
import com.gof.entity.CreditSpread;
import com.gof.entity.LiabCf;
import com.gof.entity.RefPortYield;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SmithWilsonResult;
import com.gof.entity.TopDownDcnt;
import com.gof.enums.ECreditGrade;
import com.gof.enums.ETopDownMatCd;
import com.gof.model.SmithWilsonModel;
import com.gof.model.WeightFinderWithDiffMin;
import com.gof.util.FinUtils;

/**
 *  <p> IFRS 17 의 TopDown 방법론에 의한 할인율 산출 모형을 관리하고 실행하는 Class 임.         
 *  <p> TopDwon 할인율은 부채의 현금흐름과 유사한 특성(시점, 금액의 패턴 유사한)을 가지는 자산의 참조 포트폴리오를 구성하고 포트폴리오를 구성하는 개별자산의 수익률로 부터 신용위험 조정 등을 통해 산출하는 할인율을 의미함.
 *  <p> 이를 위해 다음의 단계를 거쳐 Top Down 할인율을 산출함
 *  <p>    1. 부채 현금흐름의 패턴과 유사한 자산 현금흐름을 조정하기 위한 CF 매칭 조정률 산출 
 *  <p>	     1.1 현금흐름 매칭을 처리할 만기 구간  및 최장 만기설정 : 최장 만기 20년, 분기 구간 단위로 CF 매칭 조정
 *  <p>	     1.2 부채 현금흐름의 패턴과 유사한 자산 현금흐름 패턴을 가지는 잔존만기별 조정률을 최적화 엔진을 통해 산출함.
 *  <p>    2. 개별 자산의 수익률 산출 및 자산군별 수익률 산출    
 *  <p>      2.1 개별 자산의 현행 시장관측 수익률 산출
 *  <p>      2.2 개별 자산의 신용도를 반영한 신용위험 조정 : 자산군별 잔존만기, 신용등급별로 세분화하여 신용위험 조정함. 
 *  <p>      2.3 잔존만기별 CF 조정 비중을 반영하여 자산군별, 만기별 수익률 산출
 *  <p>    3. 참조 포트폴리오 구성 및 수익률 산출  ( 참조 포트폴리오 구성 대상이 되는 자산군 선정이 선행됨)
 *  <p>    	 3.1 보험부채와 유사하게 일정기간동안 현금흐름이 발생하는 Fixed Income 을 자산군으로 선정 ( 국고채, 회사채, 해외채권, 대출채권) 
 *  <p>    	 3.2 주식(일정한 현금흐름 미발생: 패턴조정 불가능), 수익증권 및 대체투자 ( 현금흐름이 기초자산의 성과에 따라 변동성이 크게 발생 : 시장위험 반영 필요) 등은 자산군 선정시 제외함
 *  <p>      3.3 선정된 자산군을 잔존만기별 가중평균하여 참조 포트폴리오 구성 및 수익률 산출
 *  <p>    4. Top Down 할인율 산출 : 선정된 잔존만기 이외의 만기의 수익률은 보간법/보외법 적용(Smith wilsion)
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job29_TopDown {
	
	private final static Logger logger = LoggerFactory.getLogger(Job29_TopDown.class);
		
		/** 
		*  만기별 신용등급별 스프레드를 이용하여 개별 자산의 신용위험 조정 처리      
		*  @param bssd 	   기준년월
		*  @return        신용위험 조정 수익률   
		*/ 
		
	public static List<AssetYield> updateCreditAdjustment(String bssd ){
//			자산 수익률은 리스크 엔진이 구동된 뒤에 확인이 가능한 정보임. 
//			경제적 가정은 리스크 엔진보다 우선적으로 수행되어야 하므로 직적월의 현금흐름 정보를 활용함.
		
//		String bfBssd = FinUtils.addMonth(bssd, -1);
		String bfBssd = FinUtils.addMonth(bssd, 0);
		
		List<AssetYield> assetYield = TopDownDao.getAssetYield(bfBssd);
		Map<String, Double> loanAssetYieldMap = TopDownDao.getLoanAssetYieldMap(bfBssd);
		Map<String, Double> loanRsMap = TopDownDao.getLoanResidualSpreadMap(bfBssd);
		
		Map<String, List<BizCrdSpreadUd>> userSpreadMap = TopDownDao.getCreditSpreadUd(bssd).stream()
						.collect(Collectors.groupingBy(s -> ECreditGrade.getECreditGradeFromLegacy(s.getCrdGrdCd()).getAlias(), Collectors.toList()));
		
		Map<String, List<CreditSpread>> rawSpreadMap = TopDownDao.getCreditSpread(bssd).stream()
								.collect(Collectors.groupingBy(s -> ECreditGrade.getECreditGradeFromLegacy(s.getCrdGrdCd()).getAlias(), Collectors.toList()));
		
		// <신용등급, <만기,스프레드 Map>>, 만기 : M0003... 신용등급 : AAA, AA....
		Map<String, Map<String, Double>> crdSpreadMap = new HashMap<String, Map<String, Double>>();  
		Map<String, Double> tempMap = new LinkedHashMap<String, Double>();
		
		//신용등급별, 잔존만기별 스프레드 세팅
		for(ECreditGrade crd : ECreditGrade.values()) {
			if(userSpreadMap.containsKey(crd.getAlias())) {
				tempMap = userSpreadMap.get(crd.getAlias()).stream()
								.collect(Collectors.toMap(s -> s.getMatCd(), s -> s.getApplyCrdSpread(), (s,t) ->t, LinkedHashMap::new));
			}
			else if(rawSpreadMap.containsKey(crd.getAlias())){
				tempMap = rawSpreadMap.get(crd.getAlias()).stream()
								.collect(Collectors.toMap(s -> s.getMatCd(), s -> s.getCrdSpread(), (s,t) ->t, LinkedHashMap::new));
			}
			crdSpreadMap.put(crd.getAlias(), tempMap);
		}
		
//		for(Map.Entry<String, List<CreditSpread>> entry : rawSpreadMap.entrySet()) {
//			tempMap = entry.getValue().stream().collect(Collectors.toMap(s -> s.getMatCd(), s -> s.getCrdSpread(), (s,t) ->t, LinkedHashMap::new));
//			crdSpreadMap.put(entry.getKey(), tempMap);
//		}
		
		String exposureId ="";
		String tempMatCd;
		String tempCrdGrd;
		double tempSpread =0.0;
		double tempYield =0.0;
		double tempRs =0.0;
		
//		개별 Exposure 의 신용위험 조정
		for( AssetYield aa : assetYield) {
			if(aa.getAssetClassTypCd().equals("04")) {
				tempYield = loanAssetYieldMap.getOrDefault(aa.getExpoId(), new Double(0.0));
				tempRs 	  = loanRsMap.getOrDefault(aa.getExpoId(), new Double(0.0));
				
				if(tempYield<0) {
					aa.setAsstYield(0.0);
					aa.setResidualSpread(0.0);
					aa.setCreditSpread( 0.0);
				}
				else {
					aa.setAsstYield(tempYield);
					aa.setResidualSpread(tempRs);
					aa.setCreditSpread(  tempRs);
//					aa.setCreditSpread(  0.0);
				}
			}
			else {
				exposureId = aa.getExpoId();
				tempMatCd  = "M0003";
				tempCrdGrd = ECreditGrade.getECreditGrade(aa.getCrdGrdCd()).getAlias();
				if(tempCrdGrd.equals("NR")){
//					if(!aa.getAssetClassTypCd().equals("04") && tempCrdGrd.equals("NR")){	
//					tempCrdGrd ="BBB+";
					tempCrdGrd ="RF";
				}
				
				if("RF".equals(aa.getCrdGrdCd())) {
					tempSpread =0.0;
				}
				else if(crdSpreadMap.containsKey(tempCrdGrd)) {
//					자산 Exposure 의 신용등급, 만기에 해당하는 신용등급별 만기별 신용스프레드 추출 ( 시장관측치)
					for(String zz : crdSpreadMap.get(tempCrdGrd).keySet()) {
						if(zz.compareTo(ETopDownMatCd.getBaseMatCd(aa.getMatCd())) <= 0) {
							tempMatCd  = zz;
							break;
						}
					}
					
					tempSpread = crdSpreadMap.get(tempCrdGrd).getOrDefault(tempMatCd,0.0);
				}
				else {
					tempSpread =0.0;
				}

				aa.setCreditSpread(tempSpread);
			}
			aa.setLastUpdateDate(LocalDateTime.now());
		}
		return assetYield;
	}

	/** 
	*  부채 현금흐름의 시점, 금액 패턴과 유사한 자산 현금흐름을 생성하기 위한 만기별 조정률 산출   
	*  만기별 자산/부채 현금흐름 차이가 최소가 되는 비중 조합을 최적화 모듈을 이용하여 산출함.
	*  @param bssd 	   기준년월
	*  @return        만기별 현금흐름 매칭 조정률   
	*/ 
	public static List<CashFlowMatchAdj> calcCashFlowMatchRatio(String bssd) {
//		자산 수익률은 리스크 엔진이 구동된 뒤에 확인이 가능한 정보임. 
//		경제적 가정은 리스크 엔진보다 우선적으로 수행되어야 하므로 직적월의 자산수익률을 활용함.
		String bfBssd = FinUtils.addMonth(bssd, -1);
		
		List<CashFlowMatchAdj> rst = new ArrayList<CashFlowMatchAdj>();
		CashFlowMatchAdj tempRst;
		
//		현금흐름 Fetch from DataBase : 현금흐름은 분기별로 저장되어 있음.
		List<LiabCf> liabCfList   = TopDownDao.getLiabilityCashFlow(bfBssd);
		List<AssetCf> assetCfList = TopDownDao.getAssetCashFlow(bfBssd);
		
		Map<String, List<LiabCf>>  liabCfMap  = liabCfList.stream().collect(Collectors.groupingBy(s-> s.getCfMatCd(), Collectors.toList()));
		
		Map<String, Map<String, List<AssetCf>>> assetCfMap = assetCfList.stream()
												.collect(Collectors.groupingBy(s-> s.getCfMatCd(), Collectors.groupingBy(t -> t.getMatCd() , Collectors.toList())));
		
//		assetCfMap.entrySet().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()));
		
		if(assetCfMap.size()==0) {
			return rst;
		}
		logger.info("solve matrix0 : {},{},{}",liabCfList.size(), assetCfList.size());
		
		int tempCfMatMonNum = 0;
		int tempMatMonNum = 0;
		int maxMatCdSize =0 ;
		
		for(AssetCf aa : assetCfList) {
			tempMatMonNum = Integer.parseInt(aa.getMatCd().split("M")[1]) / 3 + 1;
//			tempMatMonNum = Integer.parseInt(aa.getMatCd().split("M")[1])  + 1;
			if( maxMatCdSize < tempMatMonNum) {
				maxMatCdSize = tempMatMonNum;
			}
		}
		
		double totalAssetCf =0.0;
		double totalLiabCf =0.0;
		
//		M0000~ M0240 까지 분기별 81 개의 현금흐름 대해서 weight 를 조정함. 그 이후의 현금흐름에 대해서는 조정하지 않음.
//		M0240 만기의 현금흐름은 잔존만기가 M0240 보다 큰 상품의 현금흐름으로 구성됨.
		
		double[][] assetCfArray = new double[81][maxMatCdSize];
		double[]    liabCfArray = new double[81];

//		배열 초기화
		for(int i=0; i<81; i++) {
			for(int j =0 ; j< maxMatCdSize; j++) {
				assetCfArray[i][j] =0.0;
			}
			liabCfArray[i] =0.0;
		}
		
//		부채 현금흐름 세팅		
		double tempAmt =0.0;
		
		for(Map.Entry<String, List<LiabCf>> entry : liabCfMap.entrySet()) {
			tempAmt =0.0;
			for(LiabCf aa : entry.getValue()) {
				tempAmt = tempAmt + aa.getCfAmt();
				totalLiabCf =totalLiabCf + aa.getCfAmt();
			}
			liabCfArray[Integer.parseInt(entry.getKey().split("M")[1])/3] = tempAmt;
		}
		
		
		
//      현금흐름 만기별, 잔존만기별 자산현금흐름 세팅
		for(Map.Entry<String, Map<String,List<AssetCf>>> entry : assetCfMap.entrySet()) {
			for(Map.Entry<String, List<AssetCf>> subEntry : entry.getValue().entrySet()) {
				tempAmt =0.0;
				for(AssetCf aa : subEntry.getValue()) {
					tempAmt = tempAmt + aa.getCfAmt();
					totalAssetCf =totalAssetCf + aa.getCfAmt();
				}
//				logger.info("Sorted Asset Cf : {}, {},{}", entry.getKey(), Integer.parseInt(entry.getKey().split("M")[1])/3);
//				logger.info("Sorted Asset Cf : {}, {},{}", entry.getKey(), subEntry.getKey(), subEntry.getValue());
				
				assetCfArray[Integer.parseInt(entry.getKey().split("M")[1])/3][Integer.parseInt(subEntry.getKey().split("M")[1])/3] = tempAmt;
			}
		}
		
//		최적화 모듈을 위한 Matrix 설정
		SimpleMatrix liabCfMatrix = new SimpleMatrix(81, 1, true, liabCfArray);
		SimpleMatrix assetCfMatrix = new SimpleMatrix(assetCfArray);
		

//		MOEA 최적화 문제 설정 및 실행
		NondominatedPopulation rstOpt = new Executor()
//					.withProblemClass(WeightFinderWithDiffMin.class, maxMatCdSize, assetCfMatrix, liabCfMatrix.scale(totalAssetCf/totalLiabCf), 2.0, 0.5, 3.0)
					.withProblemClass(WeightFinderWithDiffMin.class, maxMatCdSize, assetCfMatrix, liabCfMatrix, 4.0, 0.1, 5.0)
//					.withProblemClass(WeightFinderWithDiffMin.class, maxMatCdSize, assetCfMatrix, liabCfMatrix, 3.0)
				   .withAlgorithm("NSGAII")	
				   .withEpsilon(1.0)
				   .withProperty("populationSize", 5)
				   .withMaxEvaluations(1000000)
				   .distributeOn(3)
				   .run();

		logger.info("Rst Size : {}", rstOpt.size());
		for(int i =0; i<rstOpt.size(); i++) {
			for(int j=0; j<maxMatCdSize; j++) {
				logger.info(" Optimal Weight for TopDown  : {},{},{}", i, j, rstOpt.get(i).getVariable(j));
			}
		}
		
//		MOEA 최적화 해 중 최소의 해 선정 
		int objIndex =0;
		double objSum =totalAssetCf;
		for(int i =0; i<rstOpt.size(); i++) {
			if( rstOpt.get(i).getObjective(0) < objSum) {
				objSum   = rstOpt.get(i).getObjective(0) ;
				objIndex = i;
			}
			logger.info(" Objectives : {},{},{},{},{}", i, rstOpt.get(i).getObjective(0) , rstOpt.get(i).getObjective(0)/totalLiabCf , assetCfMatrix.elementSum(),liabCfMatrix.elementSum());
		}
		
//		Output 세팅
		String tempMatCd ="";
		for(int j=0; j<maxMatCdSize; j++) {
			tempRst = new CashFlowMatchAdj();
			tempRst.setBaseYymm(bssd);
			tempRst.setMatCd("M" + String.format("%04d", j*3));
			tempRst.setVol(0.0);
			tempRst.setLastModifiedBy("ESG");
			tempRst.setLastUpdateDate(LocalDateTime.now());
			tempRst.setWeightAdjCoef(Double.parseDouble(rstOpt.get(objIndex).getVariable(j).toString()));
			
			rst.add(tempRst);
		}
		return rst;
	}
	
	
	/** 
	*  잔존만기별 CF 매칭 비중으로 가중하여 자산군별  수익률 산출    
	*  @param bssd 	   기준년월
	*  @return        참조 포트폴리오 수익률   
	*/ 
	
	public static List<AssetClassYield> createAssetClassYield(String bssd ){
		List<AssetClassYield> rst = new ArrayList<AssetClassYield>();
		AssetClassYield tempRst;
		
		List<AssetYield> assetYield   = TopDownDao.getAssetYield(bssd);
		Map<String, List<AssetYield>> yieldMap = assetYield.stream().collect(Collectors.groupingBy(s ->s.toGroupingIdString(), Collectors.toList()));
		
		Map<String, Double> weightMap = TopDownDao.getCfMatchAdj(bssd).stream().collect(Collectors.toMap(s->s.getMatCd(), s ->s.getWeightAdjCoef()));
//		weightMap.entrySet().forEach(s -> logger.info("zzz : {},{}", s.getKey(), s.getValue()));
		
		double weightSumYield;
		double weight=0.0;
		double sumAmt;
		double tempYield=0.0;
		
		for(Map.Entry<String, List<AssetYield>> entry : yieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
//			기준일자, 자산유형, 신용등급, 만기유형으로 Key 가 구성되어 있음. 구분자(,)로 배열을 생성하고, 4번재 (index 로는 3인) 배열값을 가져옴.
			String matCd = entry.getKey().split(",")[3];
			
			weight = weightMap.getOrDefault(matCd,  0.0);			//잔존만기별 CF 매칭 조정율 추출 (분기별 만기코드)
			
			for(AssetYield aa : entry.getValue()) {
				tempYield = aa.getAsstYield()==null? 0.0 : aa.getAsstYield() - aa.getCreditSpread();
				if(tempYield < 0) {
					logger.info("negative yield : {},{}", aa.getExpoId(), tempYield, aa.getAsstYield(), aa.getCreditSpread());
				}
				weightSumYield = weightSumYield + weight * aa.getBookBal() * tempYield;  
				sumAmt = sumAmt + weight * aa.getBookBal(); 
			}
			
//			logger.info("sumAmt, weigth : {},{}", sumAmt, weightSumYield);
			
			tempRst = new AssetClassYield();
			
			tempRst.setBaseYymm(bssd);
			tempRst.setAssetClassTypCd(entry.getKey().split(",")[1]);			
			tempRst.setCrdGrdCd("ALL");
			tempRst.setMatCd(entry.getKey().split(",")[3]);
			tempRst.setAssetYield(sumAmt==0 ? 0.0: weightSumYield / sumAmt);
			tempRst.setBookAmt(sumAmt);
			tempRst.setLastModifiedBy("ESG");
			tempRst.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(tempRst);
		}
		return rst;
	}
	
	/** 
	*  참조 포트폴리오 구성 자산군으로 참조 포트폴리오 구성 및 수익률 산출    
	*  @param bssd 	   기준년월
	*  @return        참조 포트폴리오 수익률   
	*/ 
	
	public static List<RefPortYield> createRefPortfolioYield(String bssd ){
		List<RefPortYield> rst = new ArrayList<RefPortYield>();
		RefPortYield tempRst;
		
//		자산군 수익률을 추출하여 잔존만기별로 그룹핑함.
		Map<String, List<AssetClassYield>> yieldMap = TopDownDao.getAssetClassYield(bssd).stream()
															.collect(Collectors.groupingBy(s ->ETopDownMatCd.getBaseMatCd(s.getMatCd()), Collectors.toList()));
		
		double weightSumYield;
		double sumAmt;
		
		for(Map.Entry<String, List<AssetClassYield>> entry : yieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
//			잔존만기별 그룹핑된 자산군 수익률을 장부금액 가중하여 평균함. 				
			for(AssetClassYield aa : entry.getValue()) {
				weightSumYield = weightSumYield + aa.getBookAmt() * aa.getAssetYield(); 
				sumAmt = sumAmt +  aa.getBookAmt(); 
			}
			
			tempRst = new RefPortYield();
			
			tempRst.setBaseYymm(bssd);
			tempRst.setAsstClassTypCd("ASSET");
			tempRst.setMatCd(entry.getKey());
			tempRst.setAssetYield(sumAmt==0 ? 0.0: weightSumYield / sumAmt);
			tempRst.setLastModifiedBy("ESG");
			tempRst.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(tempRst);
		}
		return rst;
	}
	
	/** 
	*  참조 포트폴리오 수익률을 기반으로 TopDown 할인율을  산출함 
	*  관측된 자산 수익률을 기본으로 보간/보외법으로 Smith-Wilsion 법을 적용함.   
	*  @param bssd 	   기준년월
	*  @return        TopDown 할인율    
	*/ 
	public static List<TopDownDcnt> createTopDown(String bssd ){
		List<TopDownDcnt> rst = new ArrayList<TopDownDcnt>();
		TopDownDcnt tempRst;
		
		List<RefPortYield> yieldList = TopDownDao.getRefPortYield(bssd);
		if(yieldList.size()==0) {
			return rst;
		}
//		Smith Wilson 적용을 위한 사전 처리
		double[] yield  = new double[yieldList.size()] ;
		double[] matYear = new double[yieldList.size()];
		int k =0;
		for(RefPortYield aa : yieldList) {
			yield[k] = aa.getAssetYield();
			matYear[k]= Double.parseDouble(aa.getMatCd().split("M")[1]) / 12.0 ;
			k =k+1;
		}
		
//		Smith Wilson 적용 매개변수 추출
		Map<String, Object> param = new HashMap<>();
		param.put("curCd", "KRW");
		List<SmithWilsonParam> swparam = DaoUtil.getEntities(SmithWilsonParam.class, param);
		
//		Smith Wilson 결과 산출		
		SmithWilsonModel swModel = new SmithWilsonModel(yield, matYear, swparam.size()==1? swparam.get(0).getUfr(): 0.045, swparam.size()==1?swparam.get(0).getLlp(): 60);
		
		for(SmithWilsonResult aa :  swModel.getSmithWilsionResult()) {
			tempRst = new TopDownDcnt();
			tempRst.setBaseYymm(bssd);
			tempRst.setIrCurveId("RF_KRW_TD");
			tempRst.setMatCd("M"+ String.format("%04d",aa.getMonthNum()));
			tempRst.setCrdSpread(0.0);
			tempRst.setRefYield(0.0);
			tempRst.setRiskAdjRfRate(aa.getSpotAnnual());
			tempRst.setRiskAdjRfFwdRate(aa.getFwdAnnual());
			tempRst.setVol(0.0);
			tempRst.setLastModifiedBy("ESG");
			tempRst.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(tempRst);
		}
		return rst;
	}
	
	/*public static List<TopDownDcnt> createTopDownAll(String bssd ){
		List<TopDownDcnt> rst = new ArrayList<TopDownDcnt>();
		TopDownDcnt tempRst;
		
		List<CashFlowMatchAdj> matchList = calcCashFlowMatchRatio(bssd);
		List<AssetYield>       yieldList = updateCreditAdjustment(bssd);
		
		Map<String, List<AssetYield>> yieldMap = yieldList.stream().collect(Collectors.groupingBy(s ->s.toGroupingIdString(), Collectors.toList()));
		
//		EBaseMatCd 로 설정된 CF 조정 비중을 Map 을 추출함
		Map<String, Double> weightMap = matchList.stream().collect(Collectors.toMap(s->s.getMatCd(), s ->s.getWeightAdjCoef()));
		
		double weightSumYield;
		double weight=0.0;
		double sumAmt;
		List<AssetClassYield> assetClassYiledList = new ArrayList<AssetClassYield>();
		AssetClassYield tempAssetClassYield;
		
		for(Map.Entry<String, List<AssetYield>> entry : yieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
			weight = weightMap.getOrDefault(entry.getKey().split(",")[3], 0.0);			//잔존만기별 CF 매칭 조정율 추출
			
			for(AssetYield aa : entry.getValue()) {
				weightSumYield = weightSumYield + weight * aa.getBookBal() * (aa.getAsstYield() ==0? 0.035: aa.getAsstYield());          //TODO : 대출자산 rs 반영후 수정
				sumAmt = sumAmt + weight * aa.getBookBal(); 
			}
			
			tempAssetClassYield = new AssetClassYield();
			
			tempAssetClassYield.setBaseYymm(bssd);
			tempAssetClassYield.setAssetClassTypCd(entry.getKey().split(",")[1]);
			tempAssetClassYield.setCrdGrdCd("ALL");
			tempAssetClassYield.setMatCd(entry.getKey().split(",")[3]);
			tempAssetClassYield.setAssetYield(sumAmt==0 ? 0.0: weightSumYield / sumAmt);
			tempAssetClassYield.setBookAmt(sumAmt);
			tempAssetClassYield.setLastModifiedBy("ESG");
			tempAssetClassYield.setLastUpdateDate(LocalDateTime.now());
			
			assetClassYiledList.add(tempAssetClassYield);
		}
		
		
		matchList.clear();
		yieldList.clear();
		
		List<RefPortYield> refPortYieldList = new ArrayList<RefPortYield>();
		RefPortYield tempRefPortYieldList;
		
//		자산군 수익률을 추출하여 잔존만기별로 그룹핑함.
		Map<String, List<AssetClassYield>> assetClastyieldMap = assetClassYiledList.stream().filter(s -> !s.getAssetClassTypCd().equals("04"))
				.collect(Collectors.groupingBy(s ->ETopDownMatCd.getBaseMatCd(s.getMatCd()), Collectors.toList()));
		
		
		for(Map.Entry<String, List<AssetClassYield>> entry : assetClastyieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
//			잔존만기별 그룹핑된 자산군 수익률을 장부금액 가중하여 평균함. 				
			for(AssetClassYield aa : entry.getValue()) {
				weightSumYield = weightSumYield + aa.getBookAmt() * aa.getAssetYield(); 
				sumAmt = sumAmt +  aa.getBookAmt(); 
			}
			
			tempRefPortYieldList = new RefPortYield();
			
			tempRefPortYieldList.setBaseYymm(bssd);
			tempRefPortYieldList.setAsstClassTypCd("ASSET");
			tempRefPortYieldList.setMatCd(entry.getKey());
			tempRefPortYieldList.setAssetYield(sumAmt==0 ? 0.0: weightSumYield / sumAmt);
			tempRefPortYieldList.setLastModifiedBy("ESG");
			tempRefPortYieldList.setLastUpdateDate(LocalDateTime.now());
			
			refPortYieldList.add(tempRefPortYieldList);
		}
		
//		Smith Wilson 적용을 위한 사전 처리
		double[] yield  = new double[refPortYieldList.size()] ;
		double[] matYear = new double[refPortYieldList.size()];
		int k =0;
		for(RefPortYield aa : refPortYieldList) {
			yield[k] = aa.getAssetYield();
			matYear[k]= Double.parseDouble(aa.getMatCd().split("M")[1]) /12 ;
			k =k+1;
		}
		Map<String, Object> param = new HashMap<>();
		param.put("curCd", "KRW");
		List<SmithWilsonParam> swparam = DaoUtil.getEntities(SmithWilsonParam.class, param);
		
//		Smith Wilson 결과 산출		
		SmithWilsonModel swModel = new SmithWilsonModel(yield, matYear, swparam.size()==1? swparam.get(0).getUfr(): 0.045, swparam.size()==1?swparam.get(0).getLlp(): 60);
		
		for(SmithWilsonResult aa :  swModel.getSmithWilsionResult()) {
			tempRst = new TopDownDcnt();
			tempRst.setBaseYymm(bssd);
			tempRst.setIrCurveId("RF_KRW_TD");
			tempRst.setMatCd("M"+ String.format("%04d",aa.getMonthNum()));
			tempRst.setCrdSpread(0.0);
			tempRst.setRefYield(0.0);
			tempRst.setRiskAdjRfRate(aa.getSpotAnnual());
			tempRst.setRiskAdjRfFwdRate(aa.getFwdAnnual());
			tempRst.setVol(0.0);
			tempRst.setLastModifiedBy("ESG");
			tempRst.setLastUpdateDate(LocalDateTime.now());
			
			rst.add(tempRst);
		}
		return rst;
	}*/
}
