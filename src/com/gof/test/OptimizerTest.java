package com.gof.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.TopDownDao;
import com.gof.entity.AssetYield;
import com.gof.entity.IrCurveHis;
import com.gof.enums.EBaseMatCd;
import com.gof.util.FinUtils;

public class OptimizerTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	public static void main(String[] args) {
//		aaa();
//		bbb();
//		ccc();
//		ddd(); 
		zzz();		EBaseMatCd.names().forEach(s -> logger.info("AAA : {}", s));
	}
	
	private static void aaa() {
		String bssd = "201801";
		TopDownDao.getAssetCashFlow(bssd).stream().forEach(s ->logger.info("aaa : {}", s));
	}
	
	private static void bbb() {
		String bssd = "201712";
		DaoUtil.getEntityStream(AssetYield.class, new HashMap<String, Object>()).forEach(s ->logger.info("aaa : {}", s));
	}
	
	private static void ccc() {
		String bssd = "201712";
		TopDownDao.getAssetYield(bssd).stream().forEach(s ->logger.info("aaa : {}", s));
	}
	
	private static void ddd() {
		String bssd = "201712";
		TopDownDao.getMatYieldMap(bssd).entrySet().stream()
		.filter(s -> s.getKey().compareTo("M0012") < 1)
		.forEach(s ->logger.info("aaa : {}", s.getKey()));
		
	}
	private static void zzz() {
		String bssd = "201612";
		/* IrCurveHisDao.getIrCurveListTermStructure(bssd, FinUtils.addMonth(bssd, -12), "A100")
														 .entrySet()
//		 .values()
														 .stream()
//														 .flatMap(s -> s.stream())
		
//					.filter(s -> s.getKey().compareTo("M0012") < 1)
					.forEach(s ->logger.info("aaa : {},{}", s.getKey(), s.getValue().size()));
//		.forEach(s ->logger.info("aaa : {}", s.getMatCd()));
		*/
		Map<String, List<IrCurveHis>> curveMap = IrCurveHisDao.getIrCurveListTermStructure(bssd, FinUtils.addMonth(bssd, -12), "A100");
		 for(Map.Entry<String, List<IrCurveHis>> entry : curveMap.entrySet()) {
			 for(IrCurveHis aa : entry.getValue()) {
				 logger.info("aaa : {}, {},{},{}", curveMap.get("M0003").size(), entry.getKey(), aa.getBaseDate(), aa.getMatCd(), aa.getIntRate());
			 }
		 }
	}
	private static void kkk() {
	}
}
