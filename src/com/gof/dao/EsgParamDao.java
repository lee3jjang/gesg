package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizEsgParam;
import com.gof.entity.BizEsgParamUd;
import com.gof.entity.EsgMst;
import com.gof.entity.HisEsgParam;
import com.gof.entity.ParamCalcHis;
import com.gof.enums.EBoolean;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> 금리모형의 매개변수 정보를 추출함.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class EsgParamDao {
	private final static Logger logger = LoggerFactory.getLogger(EsgParamDao.class);
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	private static String baseQuery = "select a from ParamCalcHis a where 1=1 ";
//	public static List<ParamCalcHis> getEntities() {
//		return session.createQuery(baseQuery, ParamCalcHis.class).getResultList();
//	}

	
	public static List<ParamCalcHis> getParamCalHis(String bssd , int monthNum, String paramType, String matCd) {
		String q = "select a from ParamCalcHis a "
				+ " where 1=1" 
				+ " and a.baseYymm between :stDate and :endDate "
				+ " and a.paramTypCd =  :paramType"
				+ " and a.paramCalcCd = :paramCalcCd"
				+ " and a.matCd = :matCd" ;
		
		return session.createQuery(q, ParamCalcHis.class)
		.setParameter("stDate", FinUtils.addMonth(bssd, monthNum))
		.setParameter("endDate", bssd)
		.setParameter("paramType", paramType)
		.setParameter("matCd", matCd)
		.setParameter("paramCalcCd", "FULL_LOCAL_CALIB")
		.list()
		;

	}

	public static List<BizEsgParamUd> getBizEsgParamUd(String bssd ) {
		String irModelId ="HW_1_VOL_SURFACE";							//Default Value
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
		if(!esgMstList.isEmpty()) {
			irModelId = esgMstList.get(0).getIrModelId();
		}
		
		
		String q = "select a from BizEsgParamUd a "
				+ " where 1=1" 
				+ " and a.applyStartYymm = :bssd "
				+ " and a.applyBizDv 	= :bizDv "
				+ " and a.irModelId 	= :irModelId "
				;
		
		return session.createQuery(q, BizEsgParamUd.class)
				.setParameter("bssd", getAppliedDate(bssd))
				.setParameter("bizDv", "I")
				.setParameter("irModelId", irModelId)
				.list()
		;

	}
	
	public static List<BizEsgParamUd> getBizEsgParamUdByBiz(String bssd, String bizDv ) {
		String irModelId ="HW_1_VOL_SURFACE";							//Default Value
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
		if(!esgMstList.isEmpty()) {
			irModelId = esgMstList.get(0).getIrModelId();
		}
		
		
		String q = "select a from BizEsgParamUd a "
				+ " where 1=1" 
				+ " and a.applyStartYymm = :bssd "
				+ " and a.applyBizDv 	= :bizDv "
				+ " and a.irModelId 	= :irModelId "
				;
		
		return session.createQuery(q, BizEsgParamUd.class)
				.setParameter("bssd", getAppliedDateByBiz(bssd, bizDv))
				.setParameter("bizDv", bizDv)
				.setParameter("irModelId", irModelId)
				.list()
		;

	}

//	public static List<BizEsgParam> getBizEsgParam(String bssd, String irModelId ) {
//		String q = "select a from BizEsgParam a "
//				+ " where 1=1" 
//				+ " and a.baseYymm = :bssd "
//				+ " and a.irModelId 	= :irModelId "
//				;
//		
//		return session.createQuery(q, BizEsgParam.class)
//				.setParameter("bssd", bssd)
//				.setParameter("irModelId", irModelId)
//				.list()
//		;
//
//	}
	
	public static List<BizEsgParam> getBizEsgParam(String bssd, String bizDv, String irModelId ) {
		String q = "select a from BizEsgParam a "
				+ " where 1=1" 
				+ " and a.baseYymm = :bssd "
				+ " and a.applyBizDv = :applyBizDv "
				+ " and a.irModelId 	= :irModelId "
				;
		
		return session.createQuery(q, BizEsgParam.class)
				.setParameter("bssd", bssd)
				.setParameter("applyBizDv", bizDv)
				.setParameter("irModelId", irModelId)
				.list()
		;

	}
	
	private static String getAppliedDate(String bssd) {
		String irModelId ="HW_1_VOL_SURFACE";							//Default Value
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
		if(!esgMstList.isEmpty()) {
			irModelId = esgMstList.get(0).getIrModelId();
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("select max(a.applyStartYymm) from BizEsgParamUd a "
				+ "		where 1=1"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irModelId  = :irModelId"
				+ "		and a.applyStartYymm <= :bssd"
				+ "		and a.applyEndYymm >=  :bssd"
				)
		;
		Query<String> q = session.createQuery(builder.toString(), String.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", "I");
		q.setParameter("irModelId", irModelId);
		;
		
		logger.info("apply Date for Biz Applied Parameter : {}", q.getSingleResult());
		return q.getSingleResult();
	}
	
	
	private static String getAppliedDateByBiz(String bssd, String bizDv) {
		String irModelId ="HW_1_VOL_SURFACE";							//Default Value
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);
		if(!esgMstList.isEmpty()) {
			irModelId = esgMstList.get(0).getIrModelId();
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("select max(a.applyStartYymm) from BizEsgParamUd a "
				+ "		where 1=1"
				+ "		and a.applyBizDv = :bizDv"
				+ "		and a.irModelId  = :irModelId"
				+ "		and a.applyStartYymm <= :bssd"
				+ "		and a.applyEndYymm >=  :bssd"
				)
		;
		Query<String> q = session.createQuery(builder.toString(), String.class);
		
		q.setParameter("bssd", bssd);
		q.setParameter("bizDv", bizDv);
		q.setParameter("irModelId", irModelId);
		;
		
		logger.info("apply Date for Biz Applied Parameter : {}", q.getSingleResult());
		return q.getSingleResult();
	}
	public static int  getMaxSeq(String bssd){
		
		String query = "select  a from HisEsgParam a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
//		+ "				and a.applyBizDv   = :bizDiv	"
		;
		
		return session.createQuery(query,  HisEsgParam.class)
		   .setParameter("bssd",bssd)
//		   .setParameter("bizDiv",bizDiv)
		   .getResultStream().mapToInt(s ->s.getSeq()).max().orElse(0);
		
	}
}
