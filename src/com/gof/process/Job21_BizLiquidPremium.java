package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.DaoUtil;
import com.gof.entity.BizLiqPremium;
import com.gof.entity.BizLiqPremiumUd;
import com.gof.entity.IrCurve;
import com.gof.entity.LiqPremium;

/**
 *  <p> 유동성 프리미엄 산출 모형          
 *  <p> 경험통계로 산출한 유동성 프리미엄 또는 사용 승인받은 유동성 프리미엄(사용자입력)을   목적별 통계로 저장함.          
 *  <p> 이때    유동성 프리미엄 산출 Tenor 를 본 시스템에서 사용하는 Tenor ( irCurveTenor 에 저장되어 있음) 와 일치시킴           
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job21_BizLiquidPremium {
	
	private final static Logger logger = LoggerFactory.getLogger("LiquidPremium");
	
	public static List<BizLiqPremium> createBizLiqPremium(String bssd, IrCurve curveMst, String modelId, Set<String> irCurveTenor) {
		List<BizLiqPremium> rstList = new ArrayList<BizLiqPremium>();
		BizLiqPremium tempLiq;
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		param.put("irCurveId", curveMst.getIrCurveId());
		param.put("modelId", modelId);
		
		List<LiqPremium> liqPremList = DaoUtil.getEntities(LiqPremium.class, param);

		List<BizLiqPremiumUd> lpUserRst = BottomupDcntDao.getLiqPremiumUdByCurveId(bssd, curveMst.getIrCurveId())
											.stream()
//											.filter(s->irCurveTenor.contains(s.getMatCd()))
											.collect(Collectors.toList());
		
		if(lpUserRst.isEmpty()) {
			logger.info("liq clac :  {}", curveMst.getIrCurveId());
			for (LiqPremium aa : liqPremList) {
				if(irCurveTenor.contains(aa.getMatCd())) {
					tempLiq = new BizLiqPremium();
					
					tempLiq.setBaseYymm(aa.getBaseYymm());
					tempLiq.setApplyBizDv(curveMst.getApplBizDv());
					tempLiq.setIrCurveId(curveMst.getIrCurveId());
					tempLiq.setMatCd(aa.getMatCd());
					
					tempLiq.setApplyLiqPrem(aa.getLiqPrem());
					tempLiq.setLiqPrem(aa.getLiqPrem());
					tempLiq.setVol(0.0);
					
					tempLiq.setLastModifiedBy("ESG");
					tempLiq.setLastUpdateDate(LocalDateTime.now());
					
					rstList.add(tempLiq);
				}
			}
		}
		else{
			logger.info("liq ud :  {}", curveMst.getIrCurveId());
			for(BizLiqPremiumUd aa : lpUserRst) {
				rstList.add(aa.convertToBizLiqPremium(bssd));
			}
		}
		
		logger.info("Job 21 (IFRS17 Applied Liqudity Premium ) create {} result.  They are inserted into EAS_BIZ_APPL_LIQ_PREM Table ", rstList.size());
		return rstList;
	}
	
	public static List<BizLiqPremium> createBizLiqPremium(String bssd, String modelId, Set<String> irCurveTenor) {
		List<BizLiqPremium> rstList = new ArrayList<BizLiqPremium>();
		BizLiqPremium tempLiq;
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		param.put("modelId", modelId);
		
		List<LiqPremium> liqPremList = DaoUtil.getEntities(LiqPremium.class, param);

		List<BizLiqPremiumUd> lpUserRst = BottomupDcntDao.getLiqPremiumUd(bssd)
											.stream()
											.filter(s->irCurveTenor.contains(s.getMatCd()))
											.collect(Collectors.toList());
		
		if(lpUserRst.isEmpty()) {
			for (LiqPremium aa : liqPremList) {
				if(irCurveTenor.contains(aa.getMatCd())) {
					tempLiq = new BizLiqPremium();
					
					tempLiq.setBaseYymm(aa.getBaseYymm());
					tempLiq.setApplyBizDv("I");
					tempLiq.setMatCd(aa.getMatCd());
					
					tempLiq.setApplyLiqPrem(aa.getLiqPrem());
					tempLiq.setLiqPrem(aa.getLiqPrem());
					tempLiq.setVol(0.0);
					
					tempLiq.setLastModifiedBy("ESG");
					tempLiq.setLastUpdateDate(LocalDateTime.now());
					
					rstList.add(tempLiq);
				}
			}
		}
		else{
			for(BizLiqPremiumUd aa : lpUserRst) {
				rstList.add(aa.convertToBizLiqPremium(bssd));
			}
		}
		
		logger.info("Job 21 (IFRS17 Applied Liqudity Premium ) create {} result.  They are inserted into EAS_BIZ_APPL_LIQ_PREM Table ", rstList.size());
		return rstList;
	}
	
	/*public static List<LiqPremium> createLiquidPremium(String bssd) {
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
		
		List<IrCurveHis> liqCurveList = new ArrayList<IrCurveHis>();
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
			liqCurveList.add(new IrCurveHis(bssd, entry.getKey(), curRate * (sumRate/cnt -1) ));
		}
		
		LiquidPremiumModel lpModel = new LiquidPremiumModel(liqCurveList, 0, 20);
		List<LiqPremium> rst = lpModel.getLiqPremium(bssd);
		
		logger.info("Job21( Liquid Premium Calculation) creates  {} results.  They are inserted into EAS_LIQ_PREM Table", rst.size());
		rst.stream().forEach(s->logger.debug("Liquidity Premium Result : {}", s.toString()));
		return rst;
		
		
	}*/
}
