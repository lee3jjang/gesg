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
 *  <p> ���α����� �������� ���� ����         
 *  <p>    1. ���� Driver �� ����ä ����  
 *  <p>    2. ����ä�� �������� ������ �ֿ� Factor �� �ڻ��� ���ͷ�, �ܺ���ǥ�ݸ��� �ΰ����踦 ��������� �м���. 
 *  <p>	     2.1 �ڻ��� ���ͷ��� ����ä�� �ð迭 �����͸� ���� ������ ���� 
 *  <p>	     2.2 �ܺ� ��ǥ�ݸ���  ����ä�� �ð迭 �����͸� ���� ������ ����
 *  <p>    3. ���� Driver �� ����ä�� �ó����� ({@link Job14_EsgScenario} �� �������� �����Ͽ� �̷� ������ �ڻ�����ͷ� , �ܺ���ǥ�ݸ� ����
 *  <p>    4. ������ �ڻ�����ͷ�, �ܺ���ǥ�ݸ��� �̿��Ͽ� ���ñ��������� ���������� ������. ( �̷��� ������ �������� ���� �������� ���� ���ٰ� ������.)
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
		List<DiscRateHis> discRateHis = DiscRateDao.getDiscRateHis(bssd, -60);   				//���� 5�� �����ͷ� ���м�

//		201908 ���� : 2�� �̻��� ��츸 ������ ���� �����.
//		List<String> discRateHisIntRate = discRateHis.stream().map(s ->s.getIntRateCd()).collect(Collectors.toList());
		
		List<String> discRateHisIntRate = new ArrayList<>();
		Map<String, Long> discRateHisMap = discRateHis.stream().collect(Collectors.groupingBy(s->s.getIntRateCd(), Collectors.counting()));
		for(Map.Entry<String, Long> zz : discRateHisMap.entrySet()) {
			if(zz.getValue()>1) {
				discRateHisIntRate.add(zz.getKey());
			}
		}
		
		List<DiscRateCalcSetting> settingList = DaoUtil.getEntities(DiscRateCalcSetting.class, new HashMap<String, Object>());

//		�����ؾ��ϴ� �������� �ڵ忡 ���ؼ� �������� �̷� �����Ͱ� ���� ���� ������.:20190701
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
			
			// SJ : calcSettingList���� ������ discRateHis���� ���� IntRateCd�� �����Ͽ� continue ó����
//			if(assetYieldList.size() == 0) {
//				System.out.println("�ǳʶٱ�");
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
	 * ���� ���� �ݸ� ������ ����
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
