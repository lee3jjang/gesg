package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.TransitionMatrix;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> 전이행렬 정보를 추출하는 DAO
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
	*  <p> 전이행렬 중 부도등급으로 전이하는 확률(부도율)을 추출함.
	*  @param bssd 	   기준년월
	*  @return		  부도율                  
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
