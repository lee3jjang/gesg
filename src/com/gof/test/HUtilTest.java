package com.gof.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.EsgMstDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SmithWilsonDao;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.enums.EBaseMatCd;
import com.gof.enums.EBoolean;
import com.gof.interfaces.IIntRate;
import com.gof.model.SmithWilsonModel;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

public class HUtilTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
			
	public static void main(String[] args) {
		int core = Runtime.getRuntime().availableProcessors();
		String bssd ="201712";
//		logger.info("aa : {} ", EsgMstDao.getEsgMst(EBoolean.Y));
		
//		IrCurveHisDao.getKTBIrCurveHis("201712")
		
//		logger.info("Core : {}", IrCurveHisDao.getKTBIrCurveHis("201712"));
//		
//		logger.info("Core : {}",FinUtils.spanFullBucket("201712", IrCurveHisDao.getKTBIrCurveHis("201712")));
//		;
//		
//		logger.info("Core : {}", EsgMstDao.getEsgMstWithBizAppliedParam(bssd, EBoolean.Y));
		;

//		IrCurveHisDao.getIrCurveByCrdGrdCd("000").stream().filter(s -> "KRW".equals(s.getCurCd())).forEach(s -> logger.info("ir : {}", s.getIrCurveId()));
//		IrCurveHisDao.getIrCurveByCrdGrdCd("000").stream().forEach(s -> logger.info("ir : {}", s.toString()));
		
		logger.info("Core : {},{}", core, "20171231".substring(0,6));
//		
		logger.info("aa : {}", getEomCurveMap(bssd, -12));
		IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, -1), "A100").forEach(s -> logger.info("aaa : {}", s));
		
//		List<? extends IIntRate> rst = new ArrayList<>();
//		
//		rst = BottomupDcntDao.getPrecedingData(bssd, -1);
//		rst = IrCurveHisDao.getIrCurveHis(bssd, "A100");
////		rst.addAll(BottomupDcntDao.getPrecedingData1(bssd, -2));
//		
////		rst.addAll(IrCurveHisDao.getIrCurveHis(bssd, "A100"));
//		rst.forEach(s -> logger.info("zz :{}, {}", s.getIntRate(), s.getMatCd()));
//		
//		logger.info("aa : {}", BottomupDcntDao.getPrecedingData1(bssd, -2));
		
		
		SmithWilsonModel swModel = new SmithWilsonModel(IrCurveHisDao.getIrCurveHis(bssd, "A100"), 0.045, 60);
//		swModel.getSmithWilsionResult(false).forEach(s -> logger.info("sw : {}", s));
		swModel.convertToIrCurveHis(false).forEach(s -> logger.info("sw : {}", s));
		
//		logger.info("fwd : {}" , FinUtils.getForwardRateByMaturity(bssd, curveList, "M0001");
	}
	
	private static Map<String, List<IIntRate >> getEomCurveMap(String bssd, int monthNum, boolean isRiskFree){
		if(isRiskFree) {
			List<IrCurveHis> curveList = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, monthNum), "A100");		
			Map<String, String> eomDate = IrCurveHisDao.getEomMap(bssd, "A100");
			
			Map<String, List<IrCurveHis>> eomTermStructure 
					= curveList.stream().filter(s -> eomDate.containsValue(s.getBaseDate()))
													 .collect(Collectors.groupingBy(s->s.getBaseDate(), Collectors.toList()));
			
			List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
			Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
			 
			double ufr =  swParamMap.get("KRW").getUfr();
			double ufrt =  swParamMap.get("KRW").getUfrT();
			
			Map<String, List<IIntRate>> eomfilteredTermStructure = new HashMap<String, List<IIntRate>>();
			
			for(Map.Entry<String, List<IrCurveHis>> entry : eomTermStructure.entrySet()) {
				SmithWilsonModel swModel = new SmithWilsonModel(entry.getValue(), ufr, ufrt);
				
				eomfilteredTermStructure.put(entry.getKey().substring(0,6)
											, swModel.getIrCurveHisList(entry.getKey().substring(0,6))
												.stream()
												.filter(s -> EBaseMatCd.isContain(s.getMatCd()))
												.map(s -> s.addForwardTerm(bssd))
												.collect(Collectors.toList()));
			}
			
			return eomfilteredTermStructure;
		}
		else {
			List<BottomupDcnt> dcntRateList = BottomupDcntDao.getPrecedingData(bssd, monthNum);
			
			return dcntRateList.stream().filter(s -> EBaseMatCd.isContain(s.getMatCd()))
					.collect(Collectors.groupingBy(s->s.getBaseYymm(), Collectors.toList()))	;
		}
	}
	
	
//	private static Map<String, List<BottomupDcnt>> getEomCurveMap(String bssd, int monthNum, boolean isRiskFree){
//		List<BottomupDcnt> dcntRateList = BottomupDcntDao.getPrecedingData(bssd, monthNum);
//		return dcntRateList.stream().filter(s -> EBaseMatCd.isContain(s.getMatCd()))
//						   .collect(Collectors.groupingBy(s->s.getBaseYymm(), Collectors.toList()))	;
//	}
	
	private static Map<String, List<IIntRate>> getEomCurveMap(String bssd, int monthNum){
		List<IrCurveHis> curveList = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, monthNum), "A100");		
		Map<String, String> eomDate = IrCurveHisDao.getEomMap(bssd, "A100");
		
		Map<String, List<IrCurveHis>> eomTermStructure 
				= curveList.stream().filter(s -> eomDate.containsValue(s.getBaseDate()))
												 .collect(Collectors.groupingBy(s->s.getBaseDate(), Collectors.toList()));
		
		List<SmithWilsonParam> swParamList =SmithWilsonDao.getParamList();
		Map<String, SmithWilsonParam> swParamMap = swParamList.stream().collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
		 
		double ufr =  swParamMap.get("KRW").getUfr();
		double ufrt =  swParamMap.get("KRW").getUfrT();
		
		Map<String, List<IIntRate>> eomfilteredTermStructure = new HashMap<String, List<IIntRate>>();
		
		for(Map.Entry<String, List<IrCurveHis>> entry : eomTermStructure.entrySet()) {
			SmithWilsonModel swModel = new SmithWilsonModel(entry.getValue(), ufr, ufrt);
			
			eomfilteredTermStructure.put(entry.getKey().substring(0,6)
										, swModel.getIrCurveHisList(entry.getKey().substring(0,6))
											.stream()
											.filter(s -> EBaseMatCd.isContain(s.getMatCd()))
											.map(s -> s.addForwardTerm(bssd))
											.collect(Collectors.toList()));
		}
		
		return eomfilteredTermStructure;
	}
}
