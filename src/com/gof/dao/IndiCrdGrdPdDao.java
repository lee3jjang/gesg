package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IndiCrdGrdCumPd;
import com.gof.entity.IndiCrdGrdPd;
import com.gof.entity.IndiCrdGrdPdUd;
import com.gof.util.HibernateUtil;

/**
 *  <p> ���� �ſ�ҷ��� ������ �����ϴ� DAO
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class IndiCrdGrdPdDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from IndiCrdGrdPd a where 1=1 ";

	public static List<IndiCrdGrdPd> getEntities() {
		return session.createQuery(baseQuery, IndiCrdGrdPd.class).getResultList();
	}

	
	/** 
	*  <p> KCB �� ���νſ�ҷ��� ���� ����
	*  @param bssd 	   ���س��
	*  @return		  ���� �ſ�ҷ���                 
	*/ 
	public static List<IndiCrdGrdPd> getKcbPd(String bssd) {
		 String sql = "select a from IndiCrdGrdPd a "
	    			+ "			where 1=1"
	    			+ "			and a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm  =  :time"
	    			;
		 
	    List<IndiCrdGrdPd> rst = session.createQuery(sql, IndiCrdGrdPd.class)
	    							.setParameter("param", "07")
	    							.setParameter("time",bssd.substring(0,6))
	    							.getResultList();
		return rst;

	}

	/** 
	*  <p> NICE �� ���νſ�ҷ��� ���� ����
	*  @param bssd 	   ���س��
	*  @return		  ���� �ſ�ҷ���                 
	*/ 
	public static List<IndiCrdGrdPd> getNicePd(String bssd) {
		 String sql = "select a from IndiCrdGrdPd a "
	    			+ "			where 1=1"
	    			+ "			and a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm  =  :time"
	    			;
	    List<IndiCrdGrdPd> rst = session.createQuery(sql, IndiCrdGrdPd.class)
	    							.setParameter("param", "03")
	    							.setParameter("time",bssd.substring(0,6))
	    							.getResultList();
		return rst;
	}
	
	/** 
	*  <p> �Է��� �ſ��� ����� ���νſ�ҷ��� ���� ����
	*  @param bssd 	   ���س��
	*  @param agencyCd ���� �ſ��򰡱��
	*  @return		    ���� �ſ�ҷ���                 
	*/ 
	public static List<IndiCrdGrdPd> getAgencyPd(String bssd, String agencyCd) {
		 String sql = "select a from IndiCrdGrdPd a "
	    			+ "			where 1=1"
	    			+ "			and a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm  =  :time"
	    			;
	    List<IndiCrdGrdPd> rst = session.createQuery(sql, IndiCrdGrdPd.class)
	    							.setParameter("param", agencyCd)
	    							.setParameter("time",bssd.substring(0,6))
	    							.getResultList();
	    
		return rst;
	}
	/** 
	*  <p> �Է��� �ſ��� ����� ���� ���� �ſ�ҷ��� ���� ����
	*  @param bssd 	   ���س��
	*  @param agencyCd ���� �ſ��򰡱��
	*  @return		    ���� ���� �ſ�ҷ���                 
	*/ 
	public static List<IndiCrdGrdCumPd> getAgencyCumPd(String bssd, String agencyCd) {
		 String sql = "select a from IndiCrdGrdCumPd a "
	    			+ "			where 1=1"
	    			+ "			and a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm  =  :bssd"
	    			;
	    List<IndiCrdGrdCumPd> rst = session.createQuery(sql, IndiCrdGrdCumPd.class)
	    							.setParameter("param", agencyCd)
	    							.setParameter("bssd",bssd)
	    							.getResultList();
	    
		return rst;
	}
	
	/** 
	*  <p> ����ڰ� �Է��� ���� �ſ�ҷ��� ���� ����
	*  <p> �������� ������ ���� �ſ�ҷ����� ���� �ܺο��� ���Ϸ� �Է��� ������ �ǹ���.
	*  @param bssd 	   ���س��
	*  @return		    ���� �ſ�ҷ���                 
	*/ 
	public static List<IndiCrdGrdPdUd> getIndiPdUd(String bssd) {
		
		String maxBaseYymm = "select max(a.baseYymm) "
				+ "			from IndiCrdGrdPdUd a "
    			+ "			where 1=1"
    			+ "			and a.baseYymm  <=  :time"			
		;
		Object maxYymm= session.createQuery(maxBaseYymm)
								.setParameter("time",bssd.substring(0,6))
								.uniqueResult();

			
		 String sql = "select a from IndiCrdGrdPdUd a "
	    			+ "			where 1=1"
	    			+ "			and a.baseYymm  =  :maxTime"
	    			;
		 
	    List<IndiCrdGrdPdUd> rst = session.createQuery(sql, IndiCrdGrdPdUd.class)
	    							.setParameter("maxTime", maxYymm==null? bssd: maxYymm.toString())
	    							.getResultList();
	    
		return rst;
	}
	
}
