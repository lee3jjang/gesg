package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.HisInflation;
import com.gof.entity.HisLiqPremium;
import com.gof.entity.Inflation;
import com.gof.entity.InflationUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> 인플레이션 정보를 추출하는 DAO
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class InflationDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from Inflation a where 1=1 ";

	public static List<Inflation> getEntities() {
		return session.createQuery(baseQuery, Inflation.class).getResultList();
	}
	

	/** 
	*  <p> 사용자가 입력한 인플레이션 지수 정보 추출 
	*  <p> 주로 전년동월 대비 인플레이션 상승률 산출을 위해 과거 1년치 데이터 추출함
	*  @param bssd 	   기준년월
	*  @param monNum  기준년월 이전 과거 월수 
	*  @return		  사용자 입력 인플레이션 지수                 
	*/ 

	public static List<InflationUd> getPrecedingInflationUd(String bssd, int monNum) {
		String sql = "select a "
				+ "			from InflationUd a "
    			+ "			where 1=1"
    			+ "			and a.settingYymm  =  :settingYymm"
    			+ "			and a.baseYymm  >=  :stTime"
    			+ "			and a.baseYymm  <=  :endTime"
		;
		 
		String settingYymm = getMaxSettingYymm(bssd);
		
	    List<InflationUd> rst = session.createQuery(sql, InflationUd.class)
						    		.setParameter("settingYymm",settingYymm)
						    		.setParameter("stTime",FinUtils.addMonth(bssd, monNum))
	    							.setParameter("endTime",bssd)
	    							.getResultList();
		return rst;

	}
	

	/** 
	*  <p> 기준년월 이전의 과거 인플레이션 정보 추출 
	*   
	*  @param bssd 	   기준년월
	*  @param monNum  기준년월 이전 과거 월수 
	*  @return		  인플레이션 지수                 
	*/ 
	public static List<Inflation> getPrecedingInflation(String bssd, int monNum) {
		 String sql = "select a from Inflation a "
	    			+ "			where 1=1"
	    			+ "			and a.baseYymm  >  :stTime"
	    			+ "			and a.baseYymm  <=   :endTime"
	    			;
		 
	    List<Inflation> rst = session.createQuery(sql, Inflation.class)
	    							.setParameter("stTime",FinUtils.addMonth(bssd.substring(0,6), monNum))
	    							.setParameter("endTime",bssd.substring(0,6))
	    							.getResultList();
		return rst;
	}
	
	
	public static int  getMaxSeq(String bssd, String bizDiv, String inflationId){
		String query = "select  a from HisInflation a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
		+ "				and a.applyBizDv   = :bizDiv	"
		+ "				and a.inflationId   = :inflationId	"
		;
		
		return session.createQuery(query,  HisInflation.class)
		   .setParameter("bssd",bssd)
		   .setParameter("bizDiv",bizDiv)
		   .setParameter("inflationId",inflationId)
		   .getResultStream().mapToInt(s ->s.getSeq()).max().orElse(0);
		
	}
	
	public static int  getMaxSeq(String bssd, String bizDiv){
		String query = "select  a from HisInflation a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
		+ "				and a.applyBizDv   = :bizDiv	"
		;
		
		return session.createQuery(query,  HisInflation.class)
		   .setParameter("bssd",bssd)
		   .setParameter("bizDiv",bizDiv)
		   .getResultStream().mapToInt(s ->s.getSeq()).max().orElse(0);
		
	}
	
	public static String  getMaxSettingYymm(String bssd){
		String query = "select max(settingYymm) from InflationUd a "
		+ "				where 1=1 "
		+ "				and a.settingYymm <= :bssd "
		;
		
		return session.createQuery(query,  String.class)
		   .setParameter("bssd",bssd)
		   .getSingleResult();
		
	}
}
