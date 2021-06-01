package com.gof.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.SegLgd;
import com.gof.entity.SegLgdUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> LGD 세그먼트 정보 추출
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class SegLgdDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from SegLgd a where 1=1 ";

	public static List<SegLgd> getEntities() {
		return session.createQuery(baseQuery, SegLgd.class).getResultList();
	}
	public static List<SegLgdUd> getSegLgdUd(String bssd) {
		String maxBaseYymm = "select max(a.applStYymm) "
				+ "			from SegLgdUd a "
    			+ "			where 1=1"
    			+ "			and a.applStYymm  <=  :time"
    			+ "			and a.applEdYymm  >=  :time"
    			
		;
		
		Object maxYymm= session.createQuery(maxBaseYymm)
								.setParameter("time",bssd.substring(0,6))
								.uniqueResult();

		
		String sql = " select a from SegLgdUd a "
	    			+ "			where 1=1"
	    			+ "			and a.applStYymm  =  :maxTime"
	    			;
		
		if(maxYymm == null) {
			return new ArrayList<SegLgdUd>() ;
		}
		else {
			List<SegLgdUd> rst = session.createQuery(sql, SegLgdUd.class)
					.setParameter("maxTime",maxYymm==null? bssd: maxYymm.toString())
					.getResultList();
			return rst;
		}

	}

	public static List<SegLgd> getSegLgd(String bssd) {
		 return getSegLgd(bssd, "07");
	}

	/** 
	*  <p> 감독원 기준의 과거 36개월치 LGD 세그먼트 정보 추출
	*  @param bssd 	   기준년월
	*  @return		   LGD 세그먼트 정보 이력                 
	*/
	public static List<SegLgd> getSegLgdLessThan(String bssd) {
		 return getSegLgdLessThan(bssd, "07");
	}

	public static List<SegLgd> getSegLgd(String bssd, String lgdCaclType) {
		 String sql = "select a from SegLgd a "
	    			+ "			where 1=1"
	    			+ "			and a.lgdCalcTypCd = :param "
	    			+ "			and a.baseYymm  =  :time"
	    			;
		 
	    List<SegLgd> rst = session.createQuery(sql, SegLgd.class)
	    							.setParameter("param", lgdCaclType)
	    							.setParameter("time",bssd.substring(0,6))
	    							.getResultList();
		return rst;
	}
	
	/** 
	*  <p> 과거 36개월치 LGD 세그먼트 정보 추출
	*  @param bssd 	   기준년월
	*  @param lgdCaclType	LGD 산출 유형
	*  @return		   LGD 세그먼트 정보 이력                 
	*/ 
	public static List<SegLgd> getSegLgdLessThan(String bssd, String lgdCaclType) {
		 String sql = "select a from SegLgd a "
	    			+ "			where 1=1"
	    			+ "			and a.lgdCalcTypCd = :param "
	    			+ "			and a.baseYymm  >=  :stTime"
	    			+ "			and a.baseYymm  <   :endTime"
	    			;
		 
	    List<SegLgd> rst = session.createQuery(sql, SegLgd.class)
	    							.setParameter("param", lgdCaclType)
	    							.setParameter("stTime",FinUtils.addMonth(bssd.substring(0,6), -36))
	    							.setParameter("endTime",bssd.substring(0,6))
	    							.getResultList();
		return rst;
	}
}
