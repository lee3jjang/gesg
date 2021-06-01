package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscFwdRateSce;
import com.gof.entity.BottomupDcnt;
import com.gof.util.HibernateUtil;

/**
 *  <p> BottomUp 할인율 데이터{@link BottomupDcnt} 를 DataBase 에서 추출하는 기능을 수행하는 Class 임         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class BizDiscFwdRateSceDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	
	public static List<BizDiscFwdRateSce> getForwardRatesByMat(String bssd, String bizDv, String irCurveId, String matCd, String sceNo){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscFwdRateSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.matCd  = :matCd"
				+ "		and a.sceNo = :sceNo"
				
				)
		;
		
		Query<BizDiscFwdRateSce> q = session.createQuery(builder.toString(), BizDiscFwdRateSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		q.setParameter("matCd", matCd);
		q.setParameter("sceNo", sceNo);
		;
		
		return q.getResultList();
	}
	public static List<BizDiscFwdRateSce> getForwardRates(String bssd, String bizDv, String irCurveId, String sceNo){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscFwdRateSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.sceNo = :sceNo"
				
				)
		;
		
		Query<BizDiscFwdRateSce> q = session.createQuery(builder.toString(), BizDiscFwdRateSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		q.setParameter("sceNo", sceNo);
		;
		
		return q.getResultList();
	}

	public static List<BizDiscFwdRateSce> getForwardRatesAll(String bssd, String bizDv, String irCurveId){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscFwdRateSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				)
		;
		
		Query<BizDiscFwdRateSce> q = session.createQuery(builder.toString(), BizDiscFwdRateSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", irCurveId);
		;
		
		return q.getResultList();
	}
}
