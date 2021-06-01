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
 *  <p> IFRS 17 �� TopDown ����п� ���� ������ ���� ������ �����ϰ� �����ϴ� Class ��.         
 *  <p> TopDwon �������� ��ä�� �����帧�� ������ Ư��(����, �ݾ��� ���� ������)�� ������ �ڻ��� ���� ��Ʈ�������� �����ϰ� ��Ʈ�������� �����ϴ� �����ڻ��� ���ͷ��� ���� �ſ����� ���� ���� ���� �����ϴ� �������� �ǹ���.
 *  <p> �̸� ���� ������ �ܰ踦 ���� Top Down �������� ������
 *  <p>    1. ��ä �����帧�� ���ϰ� ������ �ڻ� �����帧�� �����ϱ� ���� CF ��Ī ������ ���� 
 *  <p>	     1.1 �����帧 ��Ī�� ó���� ���� ����  �� ���� ���⼳�� : ���� ���� 20��, �б� ���� ������ CF ��Ī ����
 *  <p>	     1.2 ��ä �����帧�� ���ϰ� ������ �ڻ� �����帧 ������ ������ �������⺰ �������� ����ȭ ������ ���� ������.
 *  <p>    2. ���� �ڻ��� ���ͷ� ���� �� �ڻ걺�� ���ͷ� ����    
 *  <p>      2.1 ���� �ڻ��� ���� ������� ���ͷ� ����
 *  <p>      2.2 ���� �ڻ��� �ſ뵵�� �ݿ��� �ſ����� ���� : �ڻ걺�� ��������, �ſ��޺��� ����ȭ�Ͽ� �ſ����� ������. 
 *  <p>      2.3 �������⺰ CF ���� ������ �ݿ��Ͽ� �ڻ걺��, ���⺰ ���ͷ� ����
 *  <p>    3. ���� ��Ʈ������ ���� �� ���ͷ� ����  ( ���� ��Ʈ������ ���� ����� �Ǵ� �ڻ걺 ������ �����)
 *  <p>    	 3.1 �����ä�� �����ϰ� �����Ⱓ���� �����帧�� �߻��ϴ� Fixed Income �� �ڻ걺���� ���� ( ����ä, ȸ��ä, �ؿ�ä��, ����ä��) 
 *  <p>    	 3.2 �ֽ�(������ �����帧 �̹߻�: �������� �Ұ���), �������� �� ��ü���� ( �����帧�� �����ڻ��� ������ ���� �������� ũ�� �߻� : �������� �ݿ� �ʿ�) ���� �ڻ걺 ������ ������
 *  <p>      3.3 ������ �ڻ걺�� �������⺰ ��������Ͽ� ���� ��Ʈ������ ���� �� ���ͷ� ����
 *  <p>    4. Top Down ������ ���� : ������ �������� �̿��� ������ ���ͷ��� ������/���ܹ� ����(Smith wilsion)
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job29_TopDown {
	
	private final static Logger logger = LoggerFactory.getLogger(Job29_TopDown.class);
		
		/** 
		*  ���⺰ �ſ��޺� �������带 �̿��Ͽ� ���� �ڻ��� �ſ����� ���� ó��      
		*  @param bssd 	   ���س��
		*  @return        �ſ����� ���� ���ͷ�   
		*/ 
		
	public static List<AssetYield> updateCreditAdjustment(String bssd ){
//			�ڻ� ���ͷ��� ����ũ ������ ������ �ڿ� Ȯ���� ������ ������. 
//			������ ������ ����ũ �������� �켱������ ����Ǿ�� �ϹǷ� �������� �����帧 ������ Ȱ����.
		
//		String bfBssd = FinUtils.addMonth(bssd, -1);
		String bfBssd = FinUtils.addMonth(bssd, 0);
		
		List<AssetYield> assetYield = TopDownDao.getAssetYield(bfBssd);
		Map<String, Double> loanAssetYieldMap = TopDownDao.getLoanAssetYieldMap(bfBssd);
		Map<String, Double> loanRsMap = TopDownDao.getLoanResidualSpreadMap(bfBssd);
		
		Map<String, List<BizCrdSpreadUd>> userSpreadMap = TopDownDao.getCreditSpreadUd(bssd).stream()
						.collect(Collectors.groupingBy(s -> ECreditGrade.getECreditGradeFromLegacy(s.getCrdGrdCd()).getAlias(), Collectors.toList()));
		
		Map<String, List<CreditSpread>> rawSpreadMap = TopDownDao.getCreditSpread(bssd).stream()
								.collect(Collectors.groupingBy(s -> ECreditGrade.getECreditGradeFromLegacy(s.getCrdGrdCd()).getAlias(), Collectors.toList()));
		
		// <�ſ���, <����,�������� Map>>, ���� : M0003... �ſ��� : AAA, AA....
		Map<String, Map<String, Double>> crdSpreadMap = new HashMap<String, Map<String, Double>>();  
		Map<String, Double> tempMap = new LinkedHashMap<String, Double>();
		
		//�ſ��޺�, �������⺰ �������� ����
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
		
//		���� Exposure �� �ſ����� ����
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
//					�ڻ� Exposure �� �ſ���, ���⿡ �ش��ϴ� �ſ��޺� ���⺰ �ſ뽺������ ���� ( �������ġ)
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
	*  ��ä �����帧�� ����, �ݾ� ���ϰ� ������ �ڻ� �����帧�� �����ϱ� ���� ���⺰ ������ ����   
	*  ���⺰ �ڻ�/��ä �����帧 ���̰� �ּҰ� �Ǵ� ���� ������ ����ȭ ����� �̿��Ͽ� ������.
	*  @param bssd 	   ���س��
	*  @return        ���⺰ �����帧 ��Ī ������   
	*/ 
	public static List<CashFlowMatchAdj> calcCashFlowMatchRatio(String bssd) {
//		�ڻ� ���ͷ��� ����ũ ������ ������ �ڿ� Ȯ���� ������ ������. 
//		������ ������ ����ũ �������� �켱������ ����Ǿ�� �ϹǷ� �������� �ڻ���ͷ��� Ȱ����.
		String bfBssd = FinUtils.addMonth(bssd, -1);
		
		List<CashFlowMatchAdj> rst = new ArrayList<CashFlowMatchAdj>();
		CashFlowMatchAdj tempRst;
		
//		�����帧 Fetch from DataBase : �����帧�� �б⺰�� ����Ǿ� ����.
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
		
//		M0000~ M0240 ���� �б⺰ 81 ���� �����帧 ���ؼ� weight �� ������. �� ������ �����帧�� ���ؼ��� �������� ����.
//		M0240 ������ �����帧�� �������Ⱑ M0240 ���� ū ��ǰ�� �����帧���� ������.
		
		double[][] assetCfArray = new double[81][maxMatCdSize];
		double[]    liabCfArray = new double[81];

//		�迭 �ʱ�ȭ
		for(int i=0; i<81; i++) {
			for(int j =0 ; j< maxMatCdSize; j++) {
				assetCfArray[i][j] =0.0;
			}
			liabCfArray[i] =0.0;
		}
		
//		��ä �����帧 ����		
		double tempAmt =0.0;
		
		for(Map.Entry<String, List<LiabCf>> entry : liabCfMap.entrySet()) {
			tempAmt =0.0;
			for(LiabCf aa : entry.getValue()) {
				tempAmt = tempAmt + aa.getCfAmt();
				totalLiabCf =totalLiabCf + aa.getCfAmt();
			}
			liabCfArray[Integer.parseInt(entry.getKey().split("M")[1])/3] = tempAmt;
		}
		
		
		
//      �����帧 ���⺰, �������⺰ �ڻ������帧 ����
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
		
//		����ȭ ����� ���� Matrix ����
		SimpleMatrix liabCfMatrix = new SimpleMatrix(81, 1, true, liabCfArray);
		SimpleMatrix assetCfMatrix = new SimpleMatrix(assetCfArray);
		

//		MOEA ����ȭ ���� ���� �� ����
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
		
//		MOEA ����ȭ �� �� �ּ��� �� ���� 
		int objIndex =0;
		double objSum =totalAssetCf;
		for(int i =0; i<rstOpt.size(); i++) {
			if( rstOpt.get(i).getObjective(0) < objSum) {
				objSum   = rstOpt.get(i).getObjective(0) ;
				objIndex = i;
			}
			logger.info(" Objectives : {},{},{},{},{}", i, rstOpt.get(i).getObjective(0) , rstOpt.get(i).getObjective(0)/totalLiabCf , assetCfMatrix.elementSum(),liabCfMatrix.elementSum());
		}
		
//		Output ����
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
	*  �������⺰ CF ��Ī �������� �����Ͽ� �ڻ걺��  ���ͷ� ����    
	*  @param bssd 	   ���س��
	*  @return        ���� ��Ʈ������ ���ͷ�   
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
			
//			��������, �ڻ�����, �ſ���, ������������ Key �� �����Ǿ� ����. ������(,)�� �迭�� �����ϰ�, 4���� (index �δ� 3��) �迭���� ������.
			String matCd = entry.getKey().split(",")[3];
			
			weight = weightMap.getOrDefault(matCd,  0.0);			//�������⺰ CF ��Ī ������ ���� (�б⺰ �����ڵ�)
			
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
	*  ���� ��Ʈ������ ���� �ڻ걺���� ���� ��Ʈ������ ���� �� ���ͷ� ����    
	*  @param bssd 	   ���س��
	*  @return        ���� ��Ʈ������ ���ͷ�   
	*/ 
	
	public static List<RefPortYield> createRefPortfolioYield(String bssd ){
		List<RefPortYield> rst = new ArrayList<RefPortYield>();
		RefPortYield tempRst;
		
//		�ڻ걺 ���ͷ��� �����Ͽ� �������⺰�� �׷�����.
		Map<String, List<AssetClassYield>> yieldMap = TopDownDao.getAssetClassYield(bssd).stream()
															.collect(Collectors.groupingBy(s ->ETopDownMatCd.getBaseMatCd(s.getMatCd()), Collectors.toList()));
		
		double weightSumYield;
		double sumAmt;
		
		for(Map.Entry<String, List<AssetClassYield>> entry : yieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
//			�������⺰ �׷��ε� �ڻ걺 ���ͷ��� ��αݾ� �����Ͽ� �����. 				
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
	*  ���� ��Ʈ������ ���ͷ��� ������� TopDown ��������  ������ 
	*  ������ �ڻ� ���ͷ��� �⺻���� ����/���ܹ����� Smith-Wilsion ���� ������.   
	*  @param bssd 	   ���س��
	*  @return        TopDown ������    
	*/ 
	public static List<TopDownDcnt> createTopDown(String bssd ){
		List<TopDownDcnt> rst = new ArrayList<TopDownDcnt>();
		TopDownDcnt tempRst;
		
		List<RefPortYield> yieldList = TopDownDao.getRefPortYield(bssd);
		if(yieldList.size()==0) {
			return rst;
		}
//		Smith Wilson ������ ���� ���� ó��
		double[] yield  = new double[yieldList.size()] ;
		double[] matYear = new double[yieldList.size()];
		int k =0;
		for(RefPortYield aa : yieldList) {
			yield[k] = aa.getAssetYield();
			matYear[k]= Double.parseDouble(aa.getMatCd().split("M")[1]) / 12.0 ;
			k =k+1;
		}
		
//		Smith Wilson ���� �Ű����� ����
		Map<String, Object> param = new HashMap<>();
		param.put("curCd", "KRW");
		List<SmithWilsonParam> swparam = DaoUtil.getEntities(SmithWilsonParam.class, param);
		
//		Smith Wilson ��� ����		
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
		
//		EBaseMatCd �� ������ CF ���� ������ Map �� ������
		Map<String, Double> weightMap = matchList.stream().collect(Collectors.toMap(s->s.getMatCd(), s ->s.getWeightAdjCoef()));
		
		double weightSumYield;
		double weight=0.0;
		double sumAmt;
		List<AssetClassYield> assetClassYiledList = new ArrayList<AssetClassYield>();
		AssetClassYield tempAssetClassYield;
		
		for(Map.Entry<String, List<AssetYield>> entry : yieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
			weight = weightMap.getOrDefault(entry.getKey().split(",")[3], 0.0);			//�������⺰ CF ��Ī ������ ����
			
			for(AssetYield aa : entry.getValue()) {
				weightSumYield = weightSumYield + weight * aa.getBookBal() * (aa.getAsstYield() ==0? 0.035: aa.getAsstYield());          //TODO : �����ڻ� rs �ݿ��� ����
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
		
//		�ڻ걺 ���ͷ��� �����Ͽ� �������⺰�� �׷�����.
		Map<String, List<AssetClassYield>> assetClastyieldMap = assetClassYiledList.stream().filter(s -> !s.getAssetClassTypCd().equals("04"))
				.collect(Collectors.groupingBy(s ->ETopDownMatCd.getBaseMatCd(s.getMatCd()), Collectors.toList()));
		
		
		for(Map.Entry<String, List<AssetClassYield>> entry : assetClastyieldMap.entrySet()) {
			weightSumYield =0.0;
			sumAmt =0.0;
			
//			�������⺰ �׷��ε� �ڻ걺 ���ͷ��� ��αݾ� �����Ͽ� �����. 				
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
		
//		Smith Wilson ������ ���� ���� ó��
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
		
//		Smith Wilson ��� ����		
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
