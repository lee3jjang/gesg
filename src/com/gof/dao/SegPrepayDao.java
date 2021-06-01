package com.gof.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizSegPrepayUd;
import com.gof.entity.SegLgd;
import com.gof.entity.SegLgdUd;
import com.gof.entity.SegPrepay;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> LGD 세그먼트 정보 추출
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class SegPrepayDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<BizSegPrepayUd> getSegPrepayUd(String bssd) {
		String maxBaseYymmQuery = "select max(a.applyStartYymm) "
				+ "			from BizSegPrepayUd a "
    			+ "			where 1=1"
    			+ "			and a.applyStartYymm  <=  :time"
    			+ "			and a.applyEndYymm  >=  :time"
    			
		;
		
		String maxYymm= session.createQuery(maxBaseYymmQuery, String.class)
								.setParameter("time",bssd.substring(0,6))
								.uniqueResult();

		
		String sql = " select a from BizSegPrepayUd a "
	    			+ "			where 1=1 "
	    			+ "			and a.applyBizDv = :bizDv"
	    			+ "			and a.applyStartYymm  =  :maxTime"
	    			;
		
		if(maxYymm == null) {
			return new ArrayList<BizSegPrepayUd>() ;
		}
		else {
			List<BizSegPrepayUd> rst = session.createQuery(sql, BizSegPrepayUd.class)
					.setParameter("maxTime",maxYymm)
					.setParameter("bizDv","K")
					.getResultList();
			return rst;
		}

	}

	public static List<SegPrepay> getSegPrepay(String bssd) {
				
		String sql = " select a from SegPrepay a "
	    			+ "			where 1=1 "
	    			+ "			and a.baseYymm 	 = :bssd"
	    			+ "			and a.segTypCd  =  :segType"
	    			;
		
		
		List<SegPrepay> rst = session.createQuery(sql, SegPrepay.class)
					.setParameter("bssd",bssd)
					.setParameter("segType","5")
					.getResultList();
		
		return rst;
		

	}
}
