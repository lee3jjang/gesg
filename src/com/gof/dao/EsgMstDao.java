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
 *  <p> ESG 엔진에서 관리하는 시장변수 (금리/주식/환율) 모형의 Master 정보를 추출함.
 *  <p> ESG 엔진이 수행되기 위해서 필요한 매개변수 정보를 함께 추출하여 EsgMst 에 설정함.
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
	*  <p> ESG 모형 정보 와 ESG 모형에 적용할 매개변수를 추출함. 
	*  @param bssd 	   기준년월
	*  @param useYn   사용여부
	*  @return		   ESG 모형 및 ESG 모형에 적용할 매개변수                 
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
