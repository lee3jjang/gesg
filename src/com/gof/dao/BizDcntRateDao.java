package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateSce;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> BottomUp 할인율 데이터{@link BottomupDcnt} 를 DataBase 에서 추출하는 기능을 수행하는 Class 임         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class BizDcntRateDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	
	public static List<BizDiscountRate> getTermStructure(String bssd, String irCurveId){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRate a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
//				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				
				)
		;
		
		Query<BizDiscountRate> q = session.createQuery(builder.toString(), BizDiscountRate.class);
		q.setParameter("bssd", bssd);
//		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		;
		
		return q.getResultList();
	}
	
	public static List<BizDiscountRateSce> getTermStructureBySceNo(String bssd, String bizDv, String irCurveId,  String sceNo){
		if(irCurveId.equals("RF_KRW_KICS")) {
			bizDv ="K";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRateSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.sceNo  = :sceNo"
				)
		;
		
		Query<BizDiscountRateSce> q = session.createQuery(builder.toString(), BizDiscountRateSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		q.setParameter("sceNo", sceNo);
		;
		
		return q.getResultList();
	}

	
	public static List<BizDiscountRateSce> getTermStructureAllScenario(String bssd, String bizDv, String irCurveId){
		if(irCurveId.equals("RF_KRW_KICS")) {
			bizDv ="K";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRateSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
//				+ "		and a.sceNo  = :sceNo"
				)
		;
		
		Query<BizDiscountRateSce> q = session.createQuery(builder.toString(), BizDiscountRateSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
//		q.setParameter("sceNo", sceNo);
		;
		
		return q.getResultList();
	}
	
	public static List<BizDiscountRate> getTimeSeries(String bssd, String bizDv, String irCurveId, String matCd, int monNum){
		if(irCurveId.equals("RF_KRW_KICS")) {
			bizDv ="K";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRate a "
				+ "		where 1=1"
				+ "		and a.baseYymm  < :bssd"
				+ "     and a.baseYymm  >=  :stBssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.matCd = :matCd "
				
				)
		;
		
		Query<BizDiscountRate> q = session.createQuery(builder.toString(), BizDiscountRate.class);
		q.setParameter("bssd", bssd);
		q.setParameter("stBssd", FinUtils.addMonth(bssd,  monNum));
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		q.setParameter("matCd", matCd);
		;
		
		return q.getResultList();
	}
	
	public static List<BizDiscountRate> getTimeSeries(String bssd, String bizDv, String irCurveId, int monNum){
		if(irCurveId.equals("RF_KRW_KICS")) {
			bizDv ="K";
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRate a "
				+ "		where 1=1"
				+ "		and a.baseYymm  < :bssd"
				+ "     and a.baseYymm  >=  :stBssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
//				+ "		and a.matCd = :matCd "
				
				)
		;
		
		Query<BizDiscountRate> q = session.createQuery(builder.toString(), BizDiscountRate.class);
		q.setParameter("bssd", bssd);
		q.setParameter("stBssd", FinUtils.addMonth(bssd,  monNum));
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
//		q.setParameter("matCd", matCd);
		;
		
		return q.getResultList();
	}
	
//	public static List<BizDiscountRateSce> getTimeSeriesScenarioByMatCd(String bssd, String bizDv, String irCurveId,  String sceNo,String matCd, int monNum){
//			StringBuilder builder = new StringBuilder();
//			builder.append("select a from BizDiscountRateSce a "
//					+ "		where 1=1"
//					+ "		and a.baseYymm  <= :bssd"
//					+ "     and a.baseYymm  >=  :stBssd"
//					+ "		and a.applyBizDv = :bizDv"
//					+ "		and a.irCurveId = :irCurveId"
//					+ "		and a.sceNo  = :sceNo"
//					+ "		and a.matCd = :matCd "
//					)
//			;
//			
//			Query<BizDiscountRateSce> q = session.createQuery(builder.toString(), BizDiscountRateSce.class);
//			q.setParameter("bssd", bssd);
//			q.setParameter("stBssd", FinUtils.addMonth(bssd,  monNum));
//			q.setParameter("bizDv", bizDv);
//			q.setParameter("irCurveId", irCurveId);
//			q.setParameter("sceNo", sceNo);
//			q.setParameter("matCd", matCd);
//			
//			
//			return q.getResultList();
//		}

//	public static List<BizDiscountRateSce> getTimeSeriesScenarioAll(String bssd, String bizDv, String irCurveId,  String sceNo, int monNum){
//		StringBuilder builder = new StringBuilder();
//		builder.append("select a from BizDiscountRateSce a "
//				+ "		where 1=1"
//				+ "		and a.baseYymm  <= :bssd"
//				+ "     and a.baseYymm  >=  :stBssd"
//				+ "		and a.applyBizDv = :bizDv"
//				+ "		and a.irCurveId = :irCurveId"
//				+ "		and a.sceNo  = :sceNo"
////				+ "		and a.matCd = :matCd "
//				)
//		;
//		
//		Query<BizDiscountRateSce> q = session.createQuery(builder.toString(), BizDiscountRateSce.class);
//		q.setParameter("bssd", bssd);
//		q.setParameter("stBssd", FinUtils.addMonth(bssd,  monNum));
//		q.setParameter("bizDv", bizDv);
//		q.setParameter("irCurveId", irCurveId);
//		q.setParameter("sceNo", sceNo);
////		q.setParameter("matCd", matCd);
//		
//		
//		return q.getResultList();
//	}
}
