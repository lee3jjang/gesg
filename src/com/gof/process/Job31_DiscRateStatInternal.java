package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.comparator.DiscRateHisComparator;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateStats;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LinearRegResult;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.model.DiscRateStatAllModel;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;
/**
 *  <p> 내부기준의 공시이율 추정 모형         
 *  <p>    1. 예측 Driver 로 국고채 선정  
 *  <p>    2. 국고채와 공시이율 산출의 주요 Factor 인 자산운용 수익률, 외부지표금리간 인과관계를 통계적으로 분석함. 
 *  <p>	     2.1 자산운용 수익률과 국고채의 시계열 데이터를 통해 통계모형 생성 
 *  <p>	     2.2 외부 지표금리와  국고채의 시계열 데이터를 통해 통계모형 생성
 *  <p>    3. 예측 Driver 인 국고채의 시나리오 ({@link Job14_EsgScenario} 를 통계모형에 적용하여 미래 시점의 자산운용수익률 , 외부지표금리 예측
 *  <p>    4. 예측된 자산운용수익률, 외부지표금리를 이용하여 공시기준이율과 공시이율을 산출함. ( 미래에 적용할 조정률은 현재 조정률이 변동 없다고 가정함.)
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job31_DiscRateStatInternal {
	private final static Logger logger = LoggerFactory.getLogger(Job31_DiscRateStatInternal.class);
	
	
	public static List<DiscRateStats> getBaseDiscRateStat(String bssd) {
			
		
		List<DiscRateStats> rstList = new ArrayList<DiscRateStats>();
		DiscRateStats rst; 
		
		
		String matList    = ParamUtil.getParamMap().getOrDefault("discInternalModelMaturityList", "M0060");
		String avgTermSet = ParamUtil.getParamMap().getOrDefault("discInternalModelAveMonth", "24");
		
//		matList ="M0060,M0120";
//		avgTermSet ="24, 24";
		List<String> matCdList = Arrays.asList(matList.split(","));
		
		int[] termSet =new int[avgTermSet.split(",").length];
		int k=0;
		for(String zz : avgTermSet.split(",")) {
			termSet[k] = Integer.parseInt(zz.trim());
			k++;
		}
		
		/*int[] termSet1 =new int[matCdList.size()];
		int avgTerm =0;
		for(String zz : avgTermSet.split(",")) {
			avgTerm = 
			termSet[k] = Integer.parseInt(zz.trim());
			k++;
		}*/
		
		Map<String, List<IrCurveHis>> rfCurve = getPastCurveMap(bssd, -60, true, matCdList);
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, -60);   				//과거 5년 데이터로 통계분석

//		201908 수정 : 2개 이상인 경우만 통계모형 산출 대상임.
//		List<String> discRateHisIntRate = discRateHis.stream().map(s ->s.getIntRateCd()).collect(Collectors.toList());
		
		List<String> discRateHisIntRate = new ArrayList<>();
		Map<String, Long> discRateHisMap = discRateHis.stream().collect(Collectors.groupingBy(s->s.getIntRateCd(), Collectors.counting()));
		for(Map.Entry<String, Long> zz : discRateHisMap.entrySet()) {
			if(zz.getValue()>1) {
				discRateHisIntRate.add(zz.getKey());
			}
		}
		
		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());

//		산출해야하는 공시이율 코드에 대해서 공시이율 이력 데이터가 없는 것을 제외함.:20190701
		List<DiscRateCalcSetting> calcSettingList = settingList.stream().filter(s -> s.isCalculable())
																		.filter(s -> discRateHisIntRate.contains(s.getIntRateCd()))
																		.collect(Collectors.toList());
		
//		calcSettingList.forEach(s-> logger.info("setting :  {},{}", s.getIntRateCd()));

		List<DiscRateCalcSetting> nonCalcSettingList = settingList.stream().filter(s -> !s.isCalculable()).collect(Collectors.toList());

		for(DiscRateCalcSetting aa : nonCalcSettingList) {
			double nonCalcDiscRate = discRateHis.stream()
					.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
					.filter(s -> bssd.equals(s.getBaseYymm()))
					.map(s-> s.getApplDiscRate())
					.findFirst().orElse(new Double(0.0));
			
			String indiVari = EBaseMatCd.getBaseMatCdEnum(matCdList.get(0)).getKTBCode();
			
			rst = new DiscRateStats();
			
			rst.setApplStYymm(bssd);
			rst.setDiscRateCalcTyp("I");
			
			rst.setIntRateCd(aa.getIntRateCd());
			rst.setDepnVariable("BASE_DISC");
			rst.setIndpVariable(indiVari);
			
			rst.setApplEdYymm(bssd);
			rst.setRegrCoef(0.0);
			rst.setRegrConstant(nonCalcDiscRate);
			rst.setRemark("");
			rst.setAvgNum(termSet[0] * 1.0);
			rst.setLastModifiedBy("ESG");
			rst.setLastUpdateDate(LocalDateTime.now());
			
//			logger.info("RST : {}", rst);
			rstList.add(rst);
		}
		
		
		for(DiscRateCalcSetting aa : calcSettingList) {

//			System.out.println(aa.getIntRateCd());
			List<DiscRateHis> assetYieldList = discRateHis.stream()
														.filter(s -> s.getIntRateCd().equals(aa.getIntRateCd()))
														.filter(s -> rfCurve.keySet().contains(s.getBaseYymm()))
														.filter(s -> s.getBaseDiscRate()> 0.0)
														.collect(Collectors.toList());
			
			// SJ : calcSettingList에는 있으나 discRateHis에는 없는 IntRateCd가 존재하여 continue 처리함
//			if(assetYieldList.size() == 0) {
//				System.out.println("건너뛰기");
//				continue;
//			}
			
			assetYieldList.sort(new DiscRateHisComparator());
			
			
			DiscRateStatAllModel model = new DiscRateStatAllModel(rfCurve, assetYieldList,  termSet, matCdList);
			LinearRegResult modelrst = model.getBaseDiscRate();
			String indiVari = EBaseMatCd.getBaseMatCdEnum(modelrst.getIndepVariable()).getKTBCode();
			
				
			rst = new DiscRateStats();
			
			rst.setApplStYymm(bssd);
			rst.setDiscRateCalcTyp("I");
			
			rst.setIntRateCd(aa.getIntRateCd());
			rst.setDepnVariable("BASE_DISC");
			rst.setIndpVariable(indiVari);
			
			rst.setApplEdYymm(bssd);
			rst.setRegrCoef(modelrst.getRegCoef());
			rst.setRegrConstant(modelrst.getRegConstant());
			rst.setRemark(modelrst.getRegRsqr().toString());
			rst.setAvgNum(modelrst.getAvgMonNum());
			rst.setLastModifiedBy("ESG");
			rst.setLastUpdateDate(LocalDateTime.now());
			
//			logger.info("RST : {},{},{},{}", rst.getIntRateCd(), rst.getAvgNum(), rst.getRegrCoef(), rst.getRegrConstant());
			rstList.add(rst);
		}
		return  rstList;
		
	}

	
	/**
	 * 과거 월말 금리 데이터 추출
	 * 
	 * 
	*/
	
	
	private static Map<String, List<IrCurveHis >> getPastCurveMap(String bssd, int monthNum, boolean isRiskFree, List<String> matCdList){
			List<IrCurveHis> curveList = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, monthNum), "A100");		
			Map<String, String> eomDate = IrCurveHisDao.getEomMap(bssd, "A100");
			
			Map<String, List<IrCurveHis>> eomTermStructure 
					= curveList.stream().filter(s -> eomDate.containsValue(s.getBaseDate()))
													 .collect(Collectors.groupingBy(s->s.getBaseYymm(), Collectors.toList()));
			
			List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
			Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
			 
			double ufr =  swParamMap.get("KRW").getUfr();
			double ufrt =  swParamMap.get("KRW").getUfrT();
			
			Map<String, List<IrCurveHis>> eomfilteredTermStructure = new HashMap<String, List<IrCurveHis>>();
			
			for(Map.Entry<String, List<IrCurveHis>> entry : eomTermStructure.entrySet()) {
				SmithWilsonModel swModel = new SmithWilsonModel(entry.getValue(), ufr, ufrt);
				
				eomfilteredTermStructure.put(entry.getKey().substring(0,6)
											, swModel.getIrCurveHisList(entry.getKey().substring(0,6))
												.stream()
//												.filter(s -> EBaseMatCd.isContain(s.getMatCd()))
												.filter(s -> matCdList.contains(s.getMatCd()))
												.map(s -> s.addForwardTerm(bssd))
												.collect(Collectors.toList()));
			}
			
			return eomfilteredTermStructure;
	}
	
//	public static Map<String, List<IrCurveHis >> getPastCurveMap(String bssd, int monthNum, boolean isRiskFree, List<String> matCdList){
//		
//		Map<String, List<IrCurveHis>> eomfilteredTermStructure = new HashMap<String, List<IrCurveHis>>();
//		String tempBssd;
//		for(int i= monthNum ; i< 0; i++) {
//			tempBssd =  FinUtils.addMonth(bssd, i);
//			eomfilteredTermStructure.put(FinUtils.addMonth(bssd, i), getIrCurveWithSmithWilson(bssd));
//		}
//		return eomfilteredTermStructure;
//	}
/*	private static List<IrCurveHis> getIrCurveWithSmithWilson(String bssd){
		List<IrCurveHis> curveList = IrCurveHisDao.getIrCurveHis( bssd, "A100");		
			
		List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
		Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
			 
		double ufr =  swParamMap.get("KRW").getUfr();
		double ufrt =  swParamMap.get("KRW").getUfrT();
			
		SmithWilsonModel swModel = new SmithWilsonModel(curveList, ufr, ufrt);
			
		return swModel.getIrCurveHisList(bssd);
	}*/
}
