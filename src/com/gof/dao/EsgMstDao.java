package com.gof.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.EsgMst;
import com.gof.entity.EsgMeta;
import com.gof.enums.EBoolean;
import com.gof.util.HibernateUtil;

/**
 *  <p> ESG �������� �����ϴ� ���庯�� (�ݸ�/�ֽ�/ȯ��) ������ Master ������ ������.
 *  <p> ESG ������ ����Ǳ� ���ؼ� �ʿ��� �Ű����� ������ �Բ� �����Ͽ� EsgMst �� ������.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class EsgMstDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from EsgMst a where 1=1 ";

	public static List<EsgMst> getEntities() {
		return session.createQuery(baseQuery, EsgMst.class).getResultList();
	}


	/*public static List<EsgMst> getAllEsgMstWithParam(String bssd) {
		List<EsgMst> rst = new ArrayList<EsgMst>();
		
		session.enableFilter("paramApplyEqBaseYymm").setParameter("baseYymm", bssd);
		
		String q = baseQuery
				+ " and a.useYn = :param"
				;
		rst = session.createQuery(q, EsgMst.class)
				.setParameter("param", EBoolean.Y)
				.getResultList();
		
		return rst;
		
	}	*/
	
	/** 
	*  <p> ESG ���� ���� �� ESG ������ ������ �Ű������� ������. 
	*  @param bssd 	   ���س��
	*  @param useYn   ��뿩��
	*  @return		   ESG ���� �� ESG ������ ������ �Ű�����                 
	*/ 
	public static List<EsgMst> getEsgMstWithParam(String bssd, EBoolean useYn) {
		EsgMst rst ; 
		
		session.enableFilter("paramApplyEqBaseYymm").setParameter("baseYymm", bssd);
		
		String q = " select a from EsgMst a "
				+ "   where 1=1 " 
				+ "   and a.useYn = :param"
				;
		
		return session.createQuery(q, EsgMst.class)
				.setParameter("param", useYn)
				.getResultList();
		
		
		
	}
	
	
	
	
//	public static List<EsgMst> getEsgMstWithBizAppliedParam(String bssd, EBoolean useYn) {
//		EsgMst rst ; 
//		
//		session.enableFilter("bizApplyParamEqBaseYymm").setParameter("baseYymm", bssd);
//		
//		String q = " select a from EsgMst a "
//				+ "   where 1=1 " 
//				+ "   and a.useYn = :param"
//				;
//		
//		return session.createQuery(q, EsgMst.class)
//				.setParameter("param", useYn)
//				.getResultList();
//	}
	
	public static List<EsgMst> getEsgMst(EBoolean useYn) {
		String q = " select a from EsgMst a "
				+ "   where 1=1 " 
				+ "   and a.useYn = :param"
				;
		
		return session.createQuery(q, EsgMst.class)
				.setParameter("param", useYn)
				.getResultList();
		
	}
	
	
	public static List<EsgMeta> getEsgMeta(String groupId) {
		String q = " select a from EsgMeta a "
				+ "   where 1=1 "
				+ "	  and a.groupId = :groupId	" 
				+ "   and a.useYn = :param"
				;
		
		return session.createQuery(q, EsgMeta.class)
				.setParameter("groupId", groupId)
				.setParameter("param", EBoolean.Y)
				.getResultList();
	}

}
