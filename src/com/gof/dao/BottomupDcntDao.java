package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscountRateUd;
import com.gof.entity.BizLiqPremiumUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.DcntSce;
import com.gof.interfaces.IIntRate;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> BottomUp ������ ������{@link BottomupDcnt} �� DataBase ���� �����ϴ� ����� �����ϴ� Class ��         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class BottomupDcntDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	/** 
	*  BottomUp �������� �ݸ��Ⱓ������ ������   
	*  @param bssd 	   ���س��
	*  @param irCurveId  ������ �ݸ�� ID 
	*  @return        Bottom Up �������� �Ⱓ����   
	*/ 
	
	public static List<BottomupDcnt> getTermStructure(String bssd, String irCurveId){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BottomupDcnt a "
				+ "		where 1=1"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.baseYymm  = :bssd"
				)
		;
		
		Query<BottomupDcnt> q = session.createQuery(builder.toString(), BottomupDcnt.class);
		q.setParameter("bssd", bssd);
		q.setParameter("irCurveId", irCurveId);
		;
		
		return q.getResultList();
	}
	
	
	public static List<DcntSce> getTermStructureScenario(String bssd, String irCurveId, String sceNo){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from DcntSce a "
				+ "		where 1=1"
				+ "		and a.baseYymm  = :bssd"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.sceNo  = :sceNo"
				)
		;
		
		Query<DcntSce> q = session.createQuery(builder.toString(), DcntSce.class);
		q.setParameter("bssd", bssd);
		q.setParameter("irCurveId", irCurveId);
		q.setParameter("sceNo", sceNo);
		;
		
		return q.getResultList();
	}
	
	public static List<IIntRate> getPrecedingData1(String bssd, int monNum){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BottomupDcnt a "
				+ "		where 1=1"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.baseYymm >= :stDate"
				+ "		and a.baseYymm <  :endDate"
				)
		;
		
		Query<IIntRate> q = session.createQuery(builder.toString(), IIntRate.class);
//		q.setParameter("stDate", FinUtils.addMonth(bssd, -1* monNum));
		q.setParameter("stDate", FinUtils.addMonth(bssd,  monNum));
		q.setParameter("endDate", bssd);
		q.setParameter("irCurveId", "RF_KRW_BU");
		;
		
		return q.getResultList();
	}
	
	/** 
	*  BottomUp ��������  ���� �̷� ������ ����
	*  <p> �������� ���� ���� �� �������� �̵� ����� �ʿ��� �� ����ϴ� Method ��   
	*  @param bssd 	   ���س��
	*  @param monNum  ���س�� ���� ���� 
	*  @return        Bottom Up ������  
	*/ 
	
	public static List<BottomupDcnt> getPrecedingData(String bssd, int monNum){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BottomupDcnt a "
				+ "		where 1=1"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.baseYymm >= :stDate"
				+ "		and a.baseYymm <=  :endDate"
				)
		;
		
		Query<BottomupDcnt> q = session.createQuery(builder.toString(), BottomupDcnt.class);
//		q.setParameter("stDate", FinUtils.addMonth(bssd, -1* monNum));
		q.setParameter("stDate", FinUtils.addMonth(bssd,  monNum));
		q.setParameter("endDate", bssd);
		q.setParameter("irCurveId", "RF_KRW_BU");
		;
		
		return q.getResultList();
	}
	
	
	public static String  getApplyDateLiqPremiumUd(String bssd){
		StringBuilder builder = new StringBuilder();
		builder.append("select max(a.applyStartYymm) from BizLiqPremiumUd a "
				+ "		where 1=1"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.applyStartYymm <= :bssd"
				+ "		and a.applyEndYymm >=  :bssd"
				)
		;
		Query<String> q = session.createQuery(builder.toString(), String.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", "I");
		;
		
		logger.info("Applied Date for User Input Liq premium : {}", q.getSingleResult());
		return q.getSingleResult();
	}
	
	public static String  getApplyDateLiqPremiumUd(String bssd, String irCurveId){
		StringBuilder builder = new StringBuilder();
		builder.append("select max(a.applyStartYymm) from BizLiqPremiumUd a "
				+ "		where 1=1"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.applyStartYymm <= :bssd"
				+ "		and a.applyEndYymm >=  :bssd"
				)
		;
		Query<String> q = session.createQuery(builder.toString(), String.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("irCurveId", irCurveId);
		;
		
		logger.info("Applied Date for User Input Liq premium : {}", q.getSingleResult());
		return q.getSingleResult();
	}
	
	public static List<BizLiqPremiumUd> getLiqPremiumUd(String bssd){
		
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizLiqPremiumUd a "
				+ "		where 1=1"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.applyStartYymm = :stDate"
				)
		;
		
		Query<BizLiqPremiumUd> q = session.createQuery(builder.toString(), BizLiqPremiumUd.class);
		
		q.setParameter("stDate", getApplyDateLiqPremiumUd(bssd));
		q.setParameter("bizDv", "I");
		
		
		return q.getResultList();
	}
	
	public static List<BizLiqPremiumUd> getLiqPremiumUdByCurveId(String bssd, String irCurveId){
		
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizLiqPremiumUd a "
				+ "		where 1=1"
				+ "		and a.irCurveId = :irCurveId"
				+ "		and a.applyStartYymm = :stDate"
				)
		;
		
		Query<BizLiqPremiumUd> q = session.createQuery(builder.toString(), BizLiqPremiumUd.class);
		
		q.setParameter("stDate", getApplyDateLiqPremiumUd(bssd, irCurveId));
		q.setParameter("irCurveId", irCurveId);
		
		return q.getResultList();
	}

	public static List<BizDiscountRateUd> getBizDiscountRateUd(String bssd, String curCd, String bizDv){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRateUd a "
				+ "		where 1=1"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.baseYymm = :bssd"
				+ "		and a.irCurveId = :irCurveId"
				)
		;
		
		Query<BizDiscountRateUd> q = session.createQuery(builder.toString(), BizDiscountRateUd.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irCurveId", "RF_"+curCd +"_KICS");
		
		
		return q.getResultList();
	}

	
	public static List<BizDiscountRateUd> getBizDiscountRateUd(String bssd, String irCurveId){
		StringBuilder builder = new StringBuilder();
		builder.append("select a from BizDiscountRateUd a "
				+ "		where 1=1"
				+ "		and a.baseYymm = :bssd"
//				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irCurveId = :irCurveId"
				)
		;
		
		Query<BizDiscountRateUd> q = session.createQuery(builder.toString(), BizDiscountRateUd.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("irCurveId", irCurveId);
		
		return q.getResultList();
	}
}
