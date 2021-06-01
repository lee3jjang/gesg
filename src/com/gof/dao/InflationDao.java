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
 *  <p> ���÷��̼� ������ �����ϴ� DAO
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
	*  <p> ����ڰ� �Է��� ���÷��̼� ���� ���� ���� 
	*  <p> �ַ� ���⵿�� ��� ���÷��̼� ��·� ������ ���� ���� 1��ġ ������ ������
	*  @param bssd 	   ���س��
	*  @param monNum  ���س�� ���� ���� ���� 
	*  @return		  ����� �Է� ���÷��̼� ����                 
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
	*  <p> ���س�� ������ ���� ���÷��̼� ���� ���� 
	*   
	*  @param bssd 	   ���س��
	*  @param monNum  ���س�� ���� ���� ���� 
	*  @return		  ���÷��̼� ����                 
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
