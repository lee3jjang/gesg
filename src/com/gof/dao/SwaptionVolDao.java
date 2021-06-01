package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.SwaptionVol;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;


/**
 *  <p> Swaption Vol 의 이력  정보를 추출함.
 *  <p> 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class SwaptionVolDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	
	/** 
	*  <p> 기준년월 이전의 과거 Swpation 변동성 추출  
	*  @param bssd 	   기준년월
	*  @param	monthNum 기준년월 이전 월수
	*  @return		   변동성 이력                 
	*/ 
	public static List<SwaptionVol> getPrecedingSwaptionVol(String bssd, int monthNum){
		String q = " select a from SwaptionVol a" 
				+ "  where a.baseYymm between :stDate and :endDate "
				;
		return session.createQuery(q, SwaptionVol.class)
						.setParameter("stDate", FinUtils.addMonth(bssd, monthNum))
						.setParameter("endDate", bssd)
						.list()
						;
	}
	
	public static List<SwaptionVol> getSwaptionVol(String bssd){
		String q = " select a from SwaptionVol a" 
				+ "  where a.baseYymm = :endDate "
				+ "  order by a.swapTenor, a.swaptionMaturity"
				;
		return session.createQuery(q, SwaptionVol.class)
						.setParameter("endDate", bssd)
						.list()
						;
	}
	
}
