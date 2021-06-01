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
 *  <p> IFRS 17 �� BottomUp ����п� ���� ������ ���� ������ ����         
 *  <p> ���忡�� �����Ǵ� ������ �ݸ��� ������� ���� ��ä�� �������� ������ �ݿ��Ͽ� �����ä�� ������ ������ ������.
 *  <p>    1. ������ �ݸ��� ���� : ����ä  
 *  <p>    2. ������ �����̾� ���� ��� ���� : Covered Bond Method ���� ( ���ä�� Covered Bond �� ������) 
 *  <p>	     2.1 ���ä�� ���κ����� ����Ǿ� ������ ä�ǰ� ������ Ư���� ������ ���ä�� �ݸ��� ����ä �ݸ��� ���̴� �������� Ư������ ���� ���̷� �ν���. 
 *  <p>    3. ������ �����̾��� �����ϴ� ���� ( {@link LiquidPremiumModel}) �� ���� ����� ������ �������� ����
 *  <p>    4. ���ؿ��� ������ ����ݸ� + ������ �������� �����Ͽ� �Ⱓ���� ����
 *  <p>    5. Smith-Wilson �����( {@link SmithWilsonModel} ���� ����/ ���ܸ� �����Ͽ� ��ü ������ ������ ������.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job25_KicsTermStructure {
	
	private final static Logger logger = LoggerFactory.getLogger(Job25_KicsTermStructure.class);
	
	public static List<BottomupDcnt> createKicsTermStructureNew(String bssd, List<IrCurve> curveMst, Set<String> irCurveTenor, Map<String, Double> volAdjMap) {
		List<BottomupDcnt> bottomupRst = new ArrayList<BottomupDcnt>();
		BottomupDcnt temp;
		
//		Kics Curve ��� ����
		curveMst.stream().forEach(s ->logger.info("Load Curve Mst for {} : {} with Reference Curve {} ",s.getCurCd(), s.getIrCurveId(), s.getRefCurveId()  ));
		
//		Smith Wilson �Ű����� ����
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
//		swParamMap.entrySet().forEach(s-> logger.info("aaa:  {},{}", s.getKey(), s.getValue().getUfr()));
		
		for(IrCurve zz : curveMst) {
//			Ref Curve �� Bucket �� �ݸ� ����
//			List<IrCurveHis> curveRst = IrCurveHisDao.getIrCurveHis(bssd, zz.getRefCurveId());
			
//			20210510 :  Ref Curve �� Bucket �� �ݸ� ���� & �ý��� Tenor  ���͸�!!!
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
		
//		Kics Curve ��� ����
		curveMst.stream().forEach(s ->logger.info("Load Curve Mst for {} : {} with Reference Curve {} ",s.getCurCd(), s.getIrCurveId(), s.getRefCurveId()  ));
		
//		Smith Wilson �Ű����� ����
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		Map<String, SmithWilsonParam> swParamMap = swParam.stream().collect(Collectors.toMap(s ->s.getCurCd(), Function.identity()));
		
		for(IrCurve zz : curveMst) {
//			Ref Curve �� Bucket �� �ݸ� ����
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
