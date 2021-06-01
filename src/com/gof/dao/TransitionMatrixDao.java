package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.TransitionMatrix;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> ������� ������ �����ϴ� DAO
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class TransitionMatrixDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	private static String baseQuery = "select a from TransitionMatrix a where 1=1 ";

	
	public static List<TransitionMatrix> getEntities() {
		return session.createQuery(baseQuery, TransitionMatrix.class).getResultList();
	}
	
	public static List<TransitionMatrix> getTM(String bssd) {
		 String tmType = ParamUtil.getParamMap().getOrDefault("tmType", "STM1");
		 String sql = "select a from TransitionMatrix a "
	    			+ "			where a.tmType = :param "
	    			+ "			and a.baseYyyy = :time" 
	    			;
	    
	    List<TransitionMatrix> rst = session.createQuery(sql, TransitionMatrix.class)
	    							.setParameter("param", tmType)
	    							.setParameter("time",bssd.substring(0,4))
	    							.getResultList();

	    
		return rst;
	}
	
	/** 
	*  <p> ������� �� �ε�������� �����ϴ� Ȯ��(�ε���)�� ������.
	*  @param bssd 	   ���س��
	*  @return		  �ε���                  
	*/
	
	public static List<TransitionMatrix> getDefaultRate(String bssd) {
		String tmType = ParamUtil.getParamMap().getOrDefault("tmType", "STM1");
		 String sql = "select a from TransitionMatrix a "
	    			+ "			where a.tmType = :param "
	    			+ "			and a.toGrade  = :param2 "
	    			+ "			and a.baseYyyy = :time" 
	    			;
	    
	    List<TransitionMatrix> rst = session.createQuery(sql, TransitionMatrix.class)
	    							.setParameter("param", tmType)
	    							.setParameter("param2", "D")
	    							.setParameter("time",bssd.substring(0,4))
	    							.getResultList();
		return rst;
	}
}
