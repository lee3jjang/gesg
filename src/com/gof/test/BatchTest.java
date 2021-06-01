package com.gof.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SwaptionVolDao;
import com.gof.dao.TopDownDao;
import com.gof.entity.AssetCf;
import com.gof.entity.CreditSpread;
import com.gof.entity.EsgMeta;
import com.gof.entity.EsgMst;
import com.gof.entity.InvestManageCostUd;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiabCf;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SwaptionVol;
import com.gof.enums.EBoolean;
import com.gof.enums.ECreditGrade;
import com.gof.enums.ETopDownMatCd;
import com.gof.process.Job29_TopDown;
import com.gof.util.HibernateUtil;
import com.gof.util.LoggingOutputStream;

public class BatchTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	public static void main(String[] args) {
		String bssd ="201712";
		IrCurveHisDao.getIrCurveByGenMethod("5").forEach(s -> logger.info("aaa : {}",s.getIrCurveId()));
		
		Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
		Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
		swParamMap.entrySet().forEach(entry -> logger.info("sm : {},{}", entry.getKey(), entry.getValue().getUfr()));
		
				
		System.setErr(new PrintStream(new LoggingOutputStream(org.apache.log4j.Logger.getLogger("outLog"), Level.ERROR), true));
		logger.info("aaa : {}" , ETopDownMatCd.getBaseMatCd("M0030"));
		
//		List<AssetCf> assetCfList = TopDownDao.getAssetCashFlow(bssd);
//		Map<String, Map<String, List<AssetCf>>> assetCfMap = assetCfList.stream()
//												.collect(Collectors.groupingBy(s-> s.getCfMatCd(), Collectors.groupingBy(t -> t.getMatCd() , Collectors.toList())));
//		Map<String, List<AssetCf>> zz = assetCfMap.get("M0240");
//		for(Map.Entry<String, List<AssetCf>> entry : zz.entrySet()) {
//			logger.info("zzz : {},{}", entry.getKey(), entry.getValue());
//		}
		
		
//		Map<String, List<CreditSpread>> rawSpreadMap = TopDownDao.getCreditSpread(bssd).stream()
//				.collect(Collectors.groupingBy(s -> ECreditGrade.getECreditGradeFromLegacy(s.getCrdGrdCd()).getAlias(), Collectors.toList()));
//		rawSpreadMap.entrySet().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()))		;
		
//		assetCfMap.entrySet().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()));
//		assetCfMap.keySet().forEach(s -> logger.info("aaa : {},{}", s))		;
		
		
		
		
//		Job29_TopDown.createAssetClassYield(bssd)
//		.stream()
////		.filter(s -> s.gete)
//		.forEach(s -> logger.info("aaa: {},{},{}", s.getMatCd(), s.getAssetClassTypCd(), s.getAssetYield()));
		
		/*Map<String, Double> zzz  = TopDownDao.getLoanAssetYieldMap(bssd);
		logger.info("size : {}", zzz.size());
		
		for(Map.Entry<String, Double> entry : TopDownDao.getLoanAssetYieldMap(bssd).entrySet()) {
			logger.info("aa :{},{}", entry.getKey(), entry.getValue());
		}*/
		
		List<SwaptionVol> volRst1 = new ArrayList<>();
//		volRst1.add(new SwaptionVol());
		
		logger.info("aaa : {}", volRst1.get(0).getSwapTenor());
		
		
		List<SwaptionVol> volRst = SwaptionVolDao.getSwaptionVol(bssd);
		
		
		
		volRst.forEach(s -> logger.info("Swaption Vol : {}", s));
		
		List<IrCurveHis> curveRst = IrCurveHisDao.getKTBIrCurveHis(bssd);
		curveRst.forEach(s -> logger.info("Swaption Vol : {}", s));
		
		double[] intRate = new double[curveRst.size()];
		double[] matOfYear = new double[curveRst.size()];
		
		for (int i = 0; i < matOfYear.length; i++) {
			intRate[i]   = curveRst.get(i).getIntRate();
			matOfYear[i] = Double.valueOf(curveRst.get(i).getMatCd().split("M")[1]) / 12;
		}
		
		for (int i = 0; i < matOfYear.length; i++) {
			logger.info("int mat : {},{}", intRate[i], matOfYear[i]);
		}
		
		List<InvestManageCostUd> investCost = DaoUtil.getEntities(InvestManageCostUd.class, new HashMap<String, Object>());
		investCost.stream().filter(s-> s.getBaseYymm().equals("201712"))
		.forEach(s -> logger.info("aaa : {},{}", s.getInvCostRateByAccount()));
		
		List<InvestManageCostUd> investCostList = DaoUtil.getEntities(InvestManageCostUd.class, new HashMap<String, Object>());
		Map<String, Double> investCostMap = investCostList.stream().filter(s->s.getBaseYymm().equals(bssd)).findFirst().orElse(new InvestManageCostUd()).getInvCostRateByAccount();
		investCostMap.entrySet().forEach(s -> logger.info("zz : {},{}", s.getKey(), s.getValue()));
		
		IntStream.rangeClosed(1,10).forEach(s -> logger.info("aa : {}", s));
		
		logger.info("aaa : {}", "201712" + String.format("%02d", 1));  	
//		session.setHibernateFlushMode(FlushMode.MANUAL);
		
		logger.info("flush 1: {} ,{},{},{},{},{}"
				, session.getSessionFactory().getSessionFactoryOptions().getJdbcBatchSize()
				, session.getSessionFactory().getSessionFactoryOptions().getBatchFetchStyle().name()
				, session.getFlushMode().name()
		);
//		aaa();
//		bbb();
		List<IrCurveHis> irCurveHisList = IrCurveHisDao.getIrCurveHis("20171229", "A100");
		irCurveHisList.forEach(s -> logger.info("aa : {}", s));

	}
	
	private static void aaa() {
		List<EsgMeta> rst = new ArrayList<EsgMeta>();
		EsgMeta temp;
		session.setFlushMode(FlushMode.MANUAL);
		session.beginTransaction();
		
		session.createQuery("delete EsgMeta a where a.groupId=:param")
		.setParameter("param","TEST")
		.executeUpdate();
		
		for(int i=0 ; i< 100 ; i++) {
			temp = new EsgMeta();
			
			temp.setGroupId("TEST");
			temp.setParamKey("TEST" + i);
			temp.setParamValue("zzz");
			temp.setUseYn(EBoolean.Y);
			rst.add(temp);
		}
		
		int sceCnt =1;
		int flushSize =50;
		
		for (EsgMeta bb :rst) {
			session.save(bb);
			if (sceCnt % flushSize == 0) {
				session.flush();
				session.clear();
				logger.info("IR Scenario for {}  is processed {}/{} in Job 15 {}", sceCnt);
			}
			sceCnt = sceCnt + 1;
		}
		
		session.getTransaction().commit();
	}
	
	private static void bbb() {
		List<EsgMst> rst = new ArrayList<EsgMst>();
		EsgMst temp;
		logger.info("flush 1: {} ,{}", session.getFlushMode(), session.getSessionFactory().getSessionFactoryOptions().getJdbcBatchSize());
//		session.setHibernateFlushMode(FlushMode.MANUAL);
		session.beginTransaction();
		
		logger.info("flush Start: {} ", session.getFlushMode());
		
		session.createQuery("delete EsgMst a where a.irModelId like :param")
		.setParameter("param","TEST%")
		.executeUpdate();
		
		
		for(int i=0 ; i< 1000 ; i++) {
			temp = new EsgMst();
			
			temp.setIrModelId("TEST" +i);
			temp.setParamApplCd("AAA");
			temp.setUseYn(EBoolean.Y);
			rst.add(temp);
		}
		
		int sceCnt =1;
		int flushSize =50;
		
		for (EsgMst bb :rst) {
//			logger.info("esg : {}", bb.getIrModelId());
			session.save(bb);
			if (sceCnt % flushSize == 0) {
				session.flush();
				session.clear();
				logger.info("IR Scenario for {}  is processed {}/{} in Job 15 {}", sceCnt);
			}
			sceCnt = sceCnt + 1;
		}
		
		session.getTransaction().commit();
	}

}
