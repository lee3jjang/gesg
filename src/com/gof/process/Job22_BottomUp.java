package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiqPremium;
import com.gof.entity.SmithWilsonParam;
import com.gof.model.CurveGenModel;
import com.gof.model.LiquidPremiumModel;
import com.gof.model.SmithWilsonModel;
import com.gof.util.DoubleStaticstics;
import com.gof.util.FinUtils;
/**
 *  <p> IFRS 17 의 BottomUp 방법론에 의한 할인율 산출 모형의 실행         
 *  <p> 시장에서 관측되는 무위험 금리를 기반으로 보험 부채의 비유동성 측면을 반영하여 보험부채에 적용할 할인율 산출함.
 *  <p>    1. 무위험 금리의 선정 : 국고채  
 *  <p>    2. 유동성 프리미엄 산출 방안 설정 : Covered Bond Method 적용 ( 산금채를 Covered Bond 로 선정함) 
 *  <p>	     2.1 산금채는 정부보증이 적용되어 무위험 채권과 동일한 특성을 가지면 산금채의 금리와 국고채 금리의 차이는 유동성의 특성으로 인한 차이로 인식함. 
 *  <p>    3. 유동성 프리미엄을 산출하는 모형 ( {@link LiquidPremiumModel}) 을 통해 산출된 유동성 스프레드 산출
 *  <p>    4. 기준월의 무위험 시장금리 + 유동성 스프레를 적용하여 기간구조 생성
 *  <p>    5. Smith-Wilson 방법론( {@link SmithWilsonModel} 으로 보간/ 보외를 적용하여 전체 구간의 할인율 산출함.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job22_BottomUp {
	
	private final static Logger logger = LoggerFactory.getLogger("BottomUp");
	
//	private static  List<LiqPremium> lpRst = new ArrayList<LiqPremium>();
//	private static  List<SmithWilsonParam> swParam = new ArrayList<SmithWilsonParam>();
//	private static  Map<String, SmithWilsonParam> swParamMap = new HashMap<String, SmithWilsonParam>();
//	private static  Map<String, Double> volMap = new HashMap<String, Double>();
	
	
	public static List<BottomupDcnt> createBottomUpCurveNew(String bssd, String bizDv, List<IrCurve> curveMst, Set<String> irCurveTenor) {
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		param.put("applyBizDv", bizDv);
		
		List<BizLiqPremium> lpRst = DaoUtil.getEntities(BizLiqPremium.class, param);
		
//		Smith Wilson 매개변수 추출
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
		curveMst.forEach(s-> logger.info("curvMast : {},{}",s.getIrCurveId(), s.getCurCd() ));
		
		for(IrCurve zz : curveMst) {
//			20210510 :  Ref Curve 의 Bucket 별 금리 추출 & 시스템 Tenor  필터링!!!
			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId())
												.stream().filter(s->irCurveTenor.contains(s.getMatCd())).collect(Collectors.toList());
			
			logger.info("Curve His Data :  His Data of {} ,  {} ", zz.getIrCurveId(),zz.getRefCurveId(),  bssd );
			
			List<BizLiqPremium> filteredLpRst = lpRst.stream().filter(s->s.getIrCurveId().equals(zz.getIrCurveId())).collect(Collectors.toList());
			
			if(curveRst.isEmpty()) {
				logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", zz.getIrCurveId(), bssd );
				continue;
			}
			
			double ufr  = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfr() :0.045;
			double ufrt = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfrT(): 60  ;
			
			logger.info("ufr :  {},{}",zz.getIrCurveId(),  ufr);
			bottomupRst.addAll(CurveGenModel.createTermStructure(bssd, zz,curveRst, filteredLpRst, ufr, ufrt));
			
//			if(zz.getCurCd().equals("KRW")) {
//				bottomupRst.addAll(CurveGenModel.createTermStructure(bssd, zz,curveRst, filteredLpRst, ufr, ufrt));
//			}
//			else {
//				//20210510 : 외국통화에 대해서는 유동성 프리미엄 0 설정!!!!!
//				bottomupRst.addAll(CurveGenModel.createTermStructure(bssd, zz,curveRst, 0.00, ufr, ufrt));
//			}
			
		}	
		
		logger.info("Job 22 (IFRS17 Bottom Up Dcnt  ) create {} result.  They are inserted into EAS_BOTTOM_DCNT Table ", bottomupRst.size());
		return bottomupRst;
	}

	
	public static List<BottomupDcnt> createBottomUpCurve(String bssd, List<IrCurve> curveMst) {
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		BottomupDcnt temp;
		
//		Bottom Up Curve 대상 추출
		curveMst.stream().forEach(s ->logger.info("Load Curve Mst for {} : {} with Reference Curve {} ",s.getCurCd(), s.getIrCurveId(), s.getRefCurveId()  ));
		
//		유동성 프리미엄 정보 추출 : KRW 는 적용, 다른 통화는 적용여부 확인 필요 : TODO
//		String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
//		List<BizLiqPremium> lpRst = Job21_BizLiquidPremium.getBizLiqPremium(bssd, lqModelId);
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		param.put("applyBizDv", "I");
		List<BizLiqPremium> lpRst = DaoUtil.getEntities(LiqPremium.class, param);
		
//		Smith Wilson 매개변수 추출
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
//		Bottom Up Curve 의 과거 정보로 부터 Vol 을 생성함.
		Map<String, Double> volMap = getBottomupVolMap(bssd);
		
		for(IrCurve zz : curveMst) {
//			Ref Curve 의 Bucket 별 금리 추출
			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId());
			if(curveRst.isEmpty()) {
				logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", zz.getIrCurveId(), bssd );
				continue;
			}
			
			double ufr  = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfr() :0.045;
			double ufrt = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfrT(): 60  ;
			
			
			SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
//			SmithWilsonModel rfAddLq = new SmithWilsonModel(curveRst, lpRst, ufr, ufrt);
			
			SEXP rfRst      = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix: 
//			SEXP rfAdjRst   = rfAddLq.getSmithWilsonSEXP(false).getElementAsSEXP(0);
	
//			20190411 수정버전
			double lq=0.0;
			double riskFreeRate =0.0;
			String tempMatCd ="";
			String fwdMatCd ="";
			
			Map<String, Double> lpRstMap = lpRst.stream().collect(Collectors.toMap(s -> s.getMatCd(),  s->s.getLiqPrem()));
			
			Map<String, Double> spotMap = new HashMap<String, Double>();
			for(int i =0; i< 1200; i++) {
				tempMatCd = "M" + String.format("%04d", i+1);
				lq = lpRstMap.getOrDefault(tempMatCd, 0.0);
				riskFreeRate = rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal();
				spotMap.put(tempMatCd, riskFreeRate + lq);
			}
			
			Map<String, Double> fwdMap = FinUtils.getForwardRateByMaturityMatCd(bssd, spotMap, "M0001");
//			logger.info("fwdMap : {}", fwdMap.size());
//			fwdMap.entrySet().stream().forEach(s ->logger.info("aaa : {},{}", s.getKey(), s.getValue()));
			
			for(int i =0; i< 1200; i++) {
				tempMatCd = "M" + String.format("%04d", i+1);
				fwdMatCd = "M" + String.format("%04d", i);
				
				temp = new BottomupDcnt();
				temp.setBaseYymm(bssd);
				temp.setIrCurveId(zz.getIrCurveId());
				temp.setMatCd(tempMatCd);
				
				temp.setRfRate(rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
				temp.setLiqPrem(lpRstMap.getOrDefault(tempMatCd, 0.0));

				temp.setRiskAdjRfRate( spotMap.get(tempMatCd));
				temp.setRiskAdjRfFwdRate(i==0? spotMap.get(tempMatCd): fwdMap.get(fwdMatCd));
				
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				temp.setVol(volMap.get("M" + String.format("%04d", i+1)));
//				temp.setVol(volMapMap.getOrDefault("M" + String.format("%04d", i+1), 0.0));
//				temp.setVol(0.0);
				
//				logger.info("Bottom Up Ir Rate is generated.  : {} " ,  temp.toString());
				bottomupRst.add(temp);
				
			}
//			20190411  수정 이전 버전 : liq 산출 프로세스와 동일한 방식으로 할인율 산출
			/*for(int i =0; i< 1200; i++) {
				lq = rfAdjRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal() - rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal();
				
				
				
				temp = new BottomupDcnt();
				temp.setBaseYymm(bssd);
				temp.setIrCurveId(zz.getIrCurveId());
				temp.setMatCd("M" + String.format("%04d", i+1));
				
				temp.setRfRate(rfRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
//				temp.setLiqPrem(i<=240? lq: 0.0);
				temp.setLiqPrem(lq);
				temp.setRiskAdjRfRate(rfAdjRst.getElementAsSEXP(3).getElementAsSEXP(i).asReal());
				
				temp.setRiskAdjRfFwdRate(rfAdjRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
				
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				
				temp.setVol(volMap.get("M" + String.format("%04d", i+1)));
//				temp.setVol(volMapMap.getOrDefault("M" + String.format("%04d", i+1), 0.0));
//				temp.setVol(0.0);
				
//				logger.info("Bottom Up Ir Rate is generated.  : {} " ,  temp.toString());
				bottomupRst.add(temp);
				
			}*/
		}
		logger.info("Job22( Bottom Up Ir Rate Calculation) creates  {} results.  They are inserted into EAS_BOTTOMUP_DCNT Table", bottomupRst.size());
		bottomupRst.stream().forEach(s->logger.debug("Bottom Up Result : {}", s.toString()));
		
		return bottomupRst;
	}
	
	
	
	
	public static List<BottomupDcnt> createBottomUpCurve(String bssd) {
		List<IrCurve> curveMst = IrCurveHisDao.getBottomUpIrCurve();
		return createBottomUpCurve(bssd, curveMst);
	}
	
	public static Map<String, Double> getBottomupVolMap(String bssd){
			
			Map<String, List<BottomupDcnt>> rstMap = BottomupDcntDao.getPrecedingData(bssd, 24).stream().collect(Collectors.groupingBy(s -> s.getMatCd(), Collectors.toList()));
			Map<String, Double> rstVol = new HashMap<>();
			
	//		double powSum =0.0;
	//		double sum =0.0;
	//		double vol =0.0;
	//		int cnt ;
			for(Map.Entry<String, List<BottomupDcnt>> entry : rstMap.entrySet()) {
	//			powSum =0.0;
	//			sum =0.0;
	//			cnt =0;
	//			vol =0.0;
	//			logger.info("Vol Cnt : {}", entry.getValue().size());
	//			for(BottomupDcnt aa : entry.getValue()){
	//				powSum = powSum + aa.getRiskAdjRfRate()* aa.getRiskAdjRfRate();
	//				sum = sum + aa.getRiskAdjRfRate();
	//				cnt = cnt +1;
	//			}
	//			if(cnt >0) {
	//				vol= Math.sqrt(powSum/cnt - sum*sum/(cnt*cnt));
	//			}
	//			rstVol.put(entry.getKey(), vol);
				
				rstVol.put(entry.getKey(), entry.getValue().stream().map(s ->s.getRiskAdjRfRate()).collect(DoubleStaticstics.collector()).getStd());
			}
			
	//		rstVol.entrySet().stream().forEach(s -> logger.info("Vol Map of BottomUp  : {},{},{}", s.getKey(), s.getValue()));
	
			return rstVol;
					
		}

	
	/*private List<LiqPremium> getLpRst(String bssd){
		if(lpRst.isEmpty()) {
//			유동성 프리미엄 정보 추출 : KRW 는 적용, 다른 통화는 적용여부 확인 필요 : TODO
			String lqModelId = ParamUtil.getParamMap().getOrDefault("lqModelId", "COVERED_BOND_KDB");
			
			Map<String, Object> lqParam = new HashMap<String, Object>();
			lqParam.put("baseYymm", bssd);	
			lqParam.put("modelId", lqModelId);
			
			List<LiqPremium> lpCalcRst = DaoUtil.getEntities(LiqPremium.class, lqParam);
			
			List<BizLiqPremiumUd> lpUserRst = BottomupDcntDao.getLiqPremiumUd(bssd);
			
			if(lpUserRst.isEmpty()) {
				lpRst.addAll(lpCalcRst);
				logger.info("Job22 ( Bottom Up Discount Rate) : Applied Liquidity Premium is calculated data in EAS_LIQ_PREM ") ;
			}
			else{
				for(BizLiqPremiumUd aa : lpUserRst) {
					lpRst.add(aa.convertToLiqPreminum(bssd));
				}
				logger.info("Job22 ( Bottom Up Discount Rate) : Applied Liquidity Premium is User Input Data in EAS_USER_LIQ_PREM") ; 
			}
		}
		
		return lpRst;
	}
	
	private Map<String, SmithWilsonParam> getSwParamMap(){
		if(swParamMap.isEmpty()) {
			List<SmithWilsonParam> swParamList = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
			swParamMap	 = swParamList.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		}
		return swParamMap;
		
	}
	
	private Map<String, Double> getVolMap(String bssd){
		if(volMap.isEmpty()) {
			Map<String, List<BottomupDcnt>> rstMap = BottomupDcntDao.getPrecedingData(bssd, 24).stream().collect(Collectors.groupingBy(s -> s.getMatCd(), Collectors.toList()));
			Map<String, Double> rstVol = new HashMap<>();
			
			for(Map.Entry<String, List<BottomupDcnt>> entry : rstMap.entrySet()) {
				rstVol.put(entry.getKey(), entry.getValue().stream().map(s ->s.getRiskAdjRfRate()).collect(DoubleStaticstics.collector()).getStd());
			}
		}
		return volMap;
				
	}*/
}
