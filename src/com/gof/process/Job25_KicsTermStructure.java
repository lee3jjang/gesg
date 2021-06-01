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

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.model.CurveGenModel;
import com.gof.model.LiquidPremiumModel;
import com.gof.model.SmithWilsonModel;
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
public class Job25_KicsTermStructure {
	
	private final static Logger logger = LoggerFactory.getLogger(Job25_KicsTermStructure.class);
	
	public static List<BottomupDcnt> createKicsTermStructureNew(String bssd, List<IrCurve> curveMst, Set<String> irCurveTenor, Map<String, Double> volAdjMap) {
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		BottomupDcnt temp;
		
//		Kics Curve 대상 추출
		curveMst.stream().forEach(s ->logger.info("Load Curve Mst for {} : {} with Reference Curve {} ",s.getCurCd(), s.getIrCurveId(), s.getRefCurveId()  ));
		
//		Smith Wilson 매개변수 추출
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
//		swParamMap.entrySet().forEach(s-> logger.info("aaa:  {},{}", s.getKey(), s.getValue().getUfr()));
		
		for(IrCurve zz : curveMst) {
//			Ref Curve 의 Bucket 별 금리 추출
//			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId());
			
//			20210510 :  Ref Curve 의 Bucket 별 금리 추출 & 시스템 Tenor  필터링!!!
			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId())
												.stream().filter(s->irCurveTenor.contains(s.getMatCd())).collect(Collectors.toList());
			
			if(curveRst.isEmpty()) {
				logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", zz.getIrCurveId(), bssd );
				continue;
			}
			
			double ufr  = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfr() : 0.045;
			double ufrt = swParamMap.containsKey(zz.getCurCd()) ?  swParamMap.get(zz.getCurCd()).getUfrT(): 60; ;
			double volAdj = volAdjMap.getOrDefault(zz.getCurCd(), 0.0);
			
			logger.info("Param ufr, urft, volAdj :  {},{}, {} ",zz.getIrCurveId(),  ufr, ufrt, volAdj);
			
			bottomupRst.addAll(CurveGenModel.createTermStructure(bssd, zz,curveRst, volAdj, ufr, ufrt));
			
		}
		logger.info("Job25( Kics Ir Rate Calculation) creates  {} results.  They are inserted into EAS_BOTTOMUP_DCNT Table", bottomupRst.size());
		bottomupRst.stream().forEach(s->logger.debug("Kics Term Structure Result : {}", s.toString()));
		
		return bottomupRst;
	}
	
	
	public static List<BottomupDcnt> createKicsTermStructure(String bssd, List<IrCurve> curveMst, Map<String, Double> volAdjMap) {
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		BottomupDcnt temp;
		
//		Kics Curve 대상 추출
		curveMst.stream().forEach(s ->logger.info("Load Curve Mst for {} : {} with Reference Curve {} ",s.getCurCd(), s.getIrCurveId(), s.getRefCurveId()  ));
		
//		Smith Wilson 매개변수 추출
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
		for(IrCurve zz : curveMst) {
//			Ref Curve 의 Bucket 별 금리 추출
			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId());
			if(curveRst.isEmpty()) {
				logger.warn("Curve His Data Error :  His Data of {} is not found at {} ", zz.getIrCurveId(), bssd );
				continue;
			}
			
			double ufr  = swParamMap.containsKey(zz.getCurCd()) ? swParamMap.get(zz.getCurCd()).getUfr() : 0.045 ;
			double ufrt = swParamMap.containsKey(zz.getCurCd()) ? swParamMap.get(zz.getCurCd()).getUfrT(): 60;
			double volAdj = volAdjMap.getOrDefault(zz.getCurCd(), 0.0);
			
			SmithWilsonModel rf      = new SmithWilsonModel(curveRst, ufr, ufrt);
			SmithWilsonModel rfAddLq = new SmithWilsonModel(curveRst, volAdj, ufr, ufrt);
			
			SEXP rfRst      = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix: 
			SEXP rfAdjRst   = rfAddLq.getSmithWilsonSEXP(false).getElementAsSEXP(0);
	
			double lq=0.0;
			for(int i =0; i< 1200; i++) {
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
				
				temp.setVol(0.0);
				bottomupRst.add(temp);
				
			}
		}
		logger.info("Job25( Kics Ir Rate Calculation) creates  {} results.  They are inserted into EAS_BOTTOMUP_DCNT Table", bottomupRst.size());
		bottomupRst.stream().forEach(s->logger.debug("Kics Term Structure Result : {}", s.toString()));
		
		return bottomupRst;
	}
	

}
