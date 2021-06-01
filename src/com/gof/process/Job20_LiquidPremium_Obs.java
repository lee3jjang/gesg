package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.IrCurveHisDao;
import com.gof.entity.IrCurve;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiqPremium;
import com.gof.model.LiquidPremiumModel;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;


/**
 *  <p> 유동성 프리미엄 산출 모형          
 *  <p> BottomUp 기준의 할인율 산출시 보험부채의 비유동성 특성을 반영하여 무위험 금리에 가산한 유동성 스프레드를 산출함.
 *  <p>    1. 보험부채의 비유동성 특성을 반영할 Proxy 대상 선정 (산금채) 
 *  <p>    2. Proxy 상품(산금채)와 무위험 금리의 과거 스프레드 추출 
 *  <p>	     2.1  시장에서 관측된 스프레드는 유동성 프리미엄 뿐만 아니라 채권의 개별리스크  및 시장의 Noise 가 포함되어 있음.  
 *  <p>	     2.2  개별요인으로 인한 스프레드는 장기적으로 0 으로 수렴하므로 시장 관측된 스프레드의 장기 평균을 적용함.
 *  <p>    3. Proxy 상품의 스프레드의 36개월 이동 평균을 이용하여 만기별 유동성 프리미엄 이력을 산출함. 
 *  <p>	   4. 유동성 프리미엄의 기본특성 ( 미관측된 기간 즉, LLP 이후 기간의 유동성 프리미엄은 0이어야 하고, 최단 만기에서 ( 이론적인 초단기임)도 유동성 프리미엄은 0 임) 을 이용하여 
 *  <p>    4.1  관측된 과거 유동성 프리미엄을 Curve Fitting 함.
 *  <p>    5. 관측된 유동성 프리미엄을 Curve Fitting 으로 보정한 최종적인 유동성 프리미엄 산출
 *  <p>    Observe version  에서는  fitting  과정  4, 5  step 을 생략함
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job20_LiquidPremium_Obs {
	
	private final static Logger logger = LoggerFactory.getLogger("LiquidPremium");
	
	public static List<LiqPremium> createLiquidPremium(String bssd, String lqModel, List<IrCurve> curveList) {
		List<LiqPremium> rstList = new ArrayList<LiqPremium>();
		
		for(IrCurve aa : curveList) {
			if(aa.getCurCd().equals("KRW")) {
				rstList.addAll(createLiquidPremium(bssd, aa.getIrCurveId(), lqModel));
			}
		}
		return rstList;
	}
	
	public static List<LiqPremium> createLiquidPremium(String bssd, String irCurveId, String lqModel) {
		List<IrCurveHis> spreadList = new ArrayList<IrCurveHis>();
		IrCurveHis spreadTemp ;
		
		int avgMonNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("lqAvgNum", "-36"));
		String lqKtbIrCruveId = ParamUtil.getParamMap().getOrDefault("lqKtbIrCruveId", "A100");
		String lqKdbIrCurveId = ParamUtil.getParamMap().getOrDefault("lqKdbIrCruveId", "E110");
		
		String stBssd = FinUtils.addMonth(bssd, avgMonNum);
		List<IrCurveHis> ktbList = IrCurveHisDao.getCurveHisBetween(bssd, stBssd, lqKtbIrCruveId);
		List<IrCurveHis> kdbList = IrCurveHisDao.getCurveHisBetween(bssd, stBssd, lqKdbIrCurveId);
		
		if(ktbList.size()==0 || kdbList.size()==0) {
			return new ArrayList<LiqPremium>();
		}
		
		Map<String, Double> ktbMap = ktbList.stream().collect(Collectors.toMap(s -> s.getBaseDate() + "#" +s.getMatCd() , s ->s.getIntRate()));
		Map<String, Double> kdbMap = kdbList.stream().collect(Collectors.toMap(s -> s.getBaseDate() + "#" +s.getMatCd() , s ->s.getIntRate()));

		double ktbRate =1.0;
		double kdbRate =1.0;
		
		for(Map.Entry<String, Double> entry: ktbMap.entrySet()) {
			if(kdbMap.containsKey(entry.getKey())) {
				ktbRate = entry.getValue();
				kdbRate = kdbMap.get(entry.getKey());
				spreadTemp = new IrCurveHis(entry.getKey().split("#")[0], entry.getKey().split("#")[1], ktbRate==0? 1: kdbRate/ktbRate );
				spreadList.add(spreadTemp);
			}
		}

		Map<String, List<IrCurveHis>> spreadMap  = spreadList.stream().collect(Collectors.groupingBy(s ->s.getMatCd(), Collectors.toList()));
		
//		spreadMap.get("M0036").stream().forEach(s -> logger.info("Spread : {},{}", s.getBaseDate(), s.getIntRate()));
		
		
		
		List<LiqPremium> liqCurveList = new ArrayList<LiqPremium>();
		int cnt =0;
		double sumRate =0.0;
		double curRate =0.0;
		String maxBssd = "";
		
		for(Map.Entry<String, List<IrCurveHis>> entry : spreadMap.entrySet()) {
			sumRate=0.0;
			cnt = 0 ;
			
			for(IrCurveHis aa : entry.getValue()) {
				cnt = cnt+1;
//				만기별로 과거일자의 스프레드 비율을 누적
				sumRate= sumRate + aa.getIntRate();		
				if(aa.getBaseDate().compareTo(maxBssd) > 0) {
					maxBssd = aa.getBaseDate();
				}
			}	
			curRate = ktbMap.getOrDefault( maxBssd + "#" +entry.getKey() , 1.0);
			
//			만기별로 과거일자의 스프레드 비율의 평균을 산출하고 현재 스프레드에 반영함.
//			logger.info("Sum : {},{},{},{},{}", entry.getKey(), sumRate, cnt, curRate, curRate * (sumRate/cnt -1));
			
			liqCurveList.add(build(bssd, irCurveId, lqModel, entry.getKey(), curRate * (sumRate/cnt -1) ));
		}
		
		
		logger.info("Job20( Liquid Premium Calculation_Observe) creates  {} results.  They are inserted into EAS_LIQ_PREM Table", liqCurveList.size());
		liqCurveList.stream().forEach(s->logger.debug("Liquidity Premium Result : {}", s.toString()));
		return liqCurveList;
	}
	
	private static LiqPremium build(String bssd, String irCurveId, String modelId, String matCd, double liqPrem ) {
		LiqPremium temp = new LiqPremium();
		
		temp.setBaseYymm(bssd);
		temp.setIrCurveId(irCurveId);
		temp.setModelId(modelId);
		temp.setMatCd(matCd);
		temp.setLiqPrem(liqPrem);
		temp.setVol(0.0);
		temp.setLastModifiedBy("ESG");
		temp.setLastUpdateDate(LocalDateTime.now());
		return temp;
		
	}
}
