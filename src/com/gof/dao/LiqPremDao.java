package com.gof.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizLiqPremium;
import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateHis;
import com.gof.entity.HisDiscRate;
import com.gof.entity.HisLiqPremium;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 * <p> 공시이율 {@link DiscRate } 를 DataBase 에서 추출하는 기능을 수행하는 Class 임         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class LiqPremDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();


	
	public static int  getMaxSeq(String bssd, String bizDiv){
		String query = "select  a from HisLiqPremium a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
		+ "				and a.applyBizDv   = :bizDiv	"
		;
		
		return session.createQuery(query,  HisLiqPremium.class)
		   .setParameter("bssd",bssd)
		   .setParameter("bizDiv",bizDiv)
		   .getResultStream().mapToInt(s ->s.getSeq()).max().orElse(0);
		
	}
	
	public static List<BizLiqPremium>  getBizLiquidPremium(String bssd, String bizDiv){
		String query = "select  a from  BizLiqPremium a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
		+ "				and a.applyBizDv   = :bizDiv	"
		;
		
		return session.createQuery(query,  BizLiqPremium.class)
		   .setParameter("bssd",bssd)
		   .setParameter("bizDiv",bizDiv)
		   .getResultList();
		
	}
}
