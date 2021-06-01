package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.CorpCrdGrdPd;
import com.gof.entity.CorpCumPd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> DataBase 로 부터 기업 신용등급 {@link CorpCrdGrdPd} 정보를 추출하는 기능을 수행함. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class CorpCrdGrdPdDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from CorpCrdGrdPd a where 1=1 ";

	public static List<CorpCrdGrdPd> getEntities() {
		return session.createQuery(baseQuery, CorpCrdGrdPd.class).getResultList();
	}
	/** 
	*  기준년월 이전 36월간의 기업신용등급별 부도율의 내역을 추출함. 
	*  @param bssd 	   기준년월
	*  @param monNum  기준년월 이전 과거 월수	
	*  @return        기업 신용등급별 부도율    
	*/ 
	public static List<CorpCrdGrdPd> getPrecedingCorpPd(String bssd, int monNum) {
		 String sql = "select a from CorpCrdGrdPd a "
	    			+ "			where a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm <  :time"
	    			+ "			and a.baseYymm >= :stTime"
	    			
	    			;
	    List<CorpCrdGrdPd> rst = session.createQuery(sql, CorpCrdGrdPd.class)
	    							.setParameter("param", "01")
	    							.setParameter("time",bssd.substring(0,6))
	    							.setParameter("stTime",FinUtils.addMonth(bssd,  monNum))
	    							.getResultList();

	    
		return rst;
	}

	public static List<CorpCrdGrdPd> getCorpPd(String bssd) {
		 String sql = "select a from CorpCrdGrdPd a "
	    			+ "			where a.crdEvalAgncyCd = :param "
	    			+ "			and a.baseYymm =  :bssd"
	    			;
	    List<CorpCrdGrdPd> rst = session.createQuery(sql, CorpCrdGrdPd.class)
	    							.setParameter("param", "01")
	    							.setParameter("bssd",bssd)
	    							.getResultList();
	    
		return rst;
	}
	
	
	public static List<CorpCumPd> getCorpCumPd(String bssd) {
		 String sql = "select a from CorpCumPd a "
	    			+ "			where a.agencyCode = :param "
	    			+ "			and a.baseYymm =  :bssd"
	    			;
		 
	    List<CorpCumPd> rst = session.createQuery(sql,CorpCumPd.class)
	    							.setParameter("param", "01")
	    							.setParameter("bssd",bssd)
	    							.getResultList();
	    
		return rst;
	}
}
