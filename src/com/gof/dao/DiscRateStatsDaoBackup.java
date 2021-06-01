package com.gof.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscRateAdjUd;
import com.gof.entity.BizDiscRateStat;
import com.gof.entity.BizDiscRateStatUd;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateStats;
import com.gof.entity.DiscRateStatsUd;
import com.gof.entity.InvestManageCostUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> 공시이율 통계분석 정보 를 DataBase 에서 추출하는 기능을 수행하는 Class 임
 *  <p> 공시이율 통계분석 정보가 독립적으로 사용되는 경우에 사용함.( 매개변수의 모니터링, 사용자 입력 정보 검증 등) 
 *  <p> 미래 공시이율 추정시 적용되는 통계분석 정보는 공시이율 과거 이력, 외부금리 가중치 등과 함께 통합적으로 적용되므로 {@link DiscRateSettingDao}에서 추출 정보를 관리함.
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 * @see  DiscRateSettingDao
 */

public class DiscRateStatsDaoBackup {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	private static String baseQuery = "select a from DiscRateStats a where 1=1 ";
	
	
	/** 
	*  <p> 사용자가 입력한 공시이율 통계분석 결과를 추출함. 
	*  <p> 입력한 기준년월에 유효한 공시이율 통계분석 결과중 가장 최신의 분석 결과를 추출함
	*  <p> 추출한 정보를 검증하고 미래 공시이율 추정시 적용할 매개변수로 변환하는 작업에 활용함.    
	*  @param bssd 	   기준년월
	*  @return		   사용자 입력 공시이율 통계분석                 
	*/ 
	
	public static List<DiscRateStats> getDiscRateStats(String bssd){
		String query = " SELECT a from DiscRateStats   a "
				+ "				where 1=1 "
				+ "				and a.applStYymm = :bssd "
				;
		
		return  session.createQuery(query,  DiscRateStats.class)
					   .setParameter("bssd",bssd)
					   .getResultList()
					   ;
	}
	
	public static List<DiscRateStats> getDiscRateStatsForIfrs(String bssd){
		return getDiscRateStats(bssd).stream().filter(s ->s.getDiscRateCalcTyp().equals("I")).collect(Collectors.toList());
//		return getDiscRateStats(bssd, intRateCd, "I");
	}
	
	public static List<DiscRateStats> getDiscRateStatsForIfrs(String bssd, String intRateCd){
		return getDiscRateStats(bssd).stream().filter(s ->s.getDiscRateCalcTyp().equals("I"))
											  .filter(s ->s.getIntRateCd().equals(intRateCd))
											  .collect(Collectors.toList());
//		return getDiscRateStats(bssd, intRateCd, "I");
	}
	
	public static List<DiscRateStats> getDiscRateStats(String bssd, String intRateCd, String calcType){
		return getDiscRateStats(bssd).stream()
									 .filter(s ->s.getDiscRateCalcTyp().equals(calcType))
									 .filter(s ->s.getIntRateCd().equals(intRateCd))
									 .collect(Collectors.toList());
		
//		String query = " SELECT a from DiscRateStats   a "
//				+ "				where 1=1 "
//				+ "				and a.applStYymm = :bssd "
//				+ "				and a.intRateCd = :intRateCd "
//				+ "				and a.discRateCalcTyp = :calcType "
//				;
//		
//		return  session.createQuery(query,  DiscRateStats.class)
//					   .setParameter("bssd",bssd)
//					   .setParameter("intRateCd",intRateCd)
//					   .setParameter("calcType",calcType)
//					   .getResultList()
//					   ;
	}
	

	public static List<BizDiscRateStatUd> getUserDiscRateStat(String bssd){
		String maxBssdquery = " SELECT MAX(APPL_ST_YYMM) "  
				+ "				from ESG.EAS_USER_DISC_RATE_STATS   a "
				+ "				where 1=1 "
				+ "				and a.APPL_ST_YYMM <= :bssd	"
				+ "				and a.APPL_ED_YYMM >= :bssd	"
				;
		
		Object maxBssd =  session.createNativeQuery(maxBssdquery)
				.setParameter("bssd", bssd)
				.uniqueResult()
				;
		
		String query = " SELECT a from BizDiscRateStatUd   a "
				+ "				where 1=1 "
				+ "				and a.applyStartYymm = :maxBssd "
				;
		
		List<BizDiscRateStatUd> rst  =  session.createQuery(query,  BizDiscRateStatUd.class)
											 .setParameter("maxBssd", maxBssd==null? bssd: maxBssd.toString())
											 .getResultList()
											 ;
		
		return rst; 
	}
	
	public static List<BizDiscRateStatUd> getUserDiscRateStat(String bssd, String intRateCd, String calcType){
		List<BizDiscRateStatUd> rst =getUserDiscRateStat(bssd);
		return rst.stream().filter(s -> intRateCd.equals(s.getIntRateCd()))
				    .filter(s -> calcType.equals(s.getApplyBizDv()))
				    .collect(Collectors.toList());		
		
	}
	
	public static List<BizDiscRateAdjUd> getUserDiscRateAdj(String bssd){
		String maxBssdquery = " SELECT MAX(APPL_ST_YYMM) "  
				+ "				from ESG.EAS_USER_DISC_RATE_ADJ   a "
				+ "				where 1=1 "
				+ "				and a.APPL_ST_YYMM <= :bssd	"
				+ "				and a.APPL_ED_YYMM >= :bssd	"
				;
		
		Object maxBssd =  session.createNativeQuery(maxBssdquery)
				.setParameter("bssd", bssd)
				.uniqueResult()
				;
		
		String query = " SELECT a from BizDiscRateAdjUd   a "
				+ "				where 1=1 "
				+ "				and a.applStYymm = :bssd "
				;
		
		return  session.createQuery(query,  BizDiscRateAdjUd.class)
					   .setParameter("bssd", maxBssd==null? bssd: maxBssd.toString())
					   .getResultList()
					   ;
	}
	
	public static List<BizDiscRateAdjUd> getUserDiscRateAdj(String bssd, String intRateCd, String calcType){
		String maxBssdquery = " SELECT MAX(APPL_ST_YYMM) "  
				+ "				from ESG.EAS_USER_DISC_RATE_ADJ   a "
				+ "				where 1=1 "
				+ "				and a.APPL_ST_YYMM <= :bssd	"
				+ "				and a.APPL_ED_YYMM >= :bssd	"
				;
		
		Object maxBssd =  session.createNativeQuery(maxBssdquery)
				.setParameter("bssd", bssd)
				.uniqueResult()
				;
		
		String query = " SELECT a from BizDiscRateAdjUd   a "
				+ "				where 1=1 "
				+ "				and a.applStYymm = :bssd "
				+ "				and a.intRateCd = :intRateCd "
				+ "				and a.applBizDv = :calcType "
				;
		
		return  session.createQuery(query,  BizDiscRateAdjUd.class)
					   .setParameter("bssd",maxBssd==null? bssd: maxBssd.toString())
					   .setParameter("intRateCd",intRateCd)
					   .setParameter("calcType",calcType)
					   .getResultList()
					   ;
	}
	
	public static Double getUserDiscRateAdjValue(String bssd, String intRateCd, String calcType){
		String maxBssdquery = " SELECT MAX(APPL_ST_YYMM) "  
				+ "				from ESG.EAS_USER_DISC_RATE_ADJ   a "
				+ "				where 1=1 "
				+ "				and a.APPL_ST_YYMM <= :bssd	"
				+ "				and a.APPL_ED_YYMM >= :bssd	"
				;
		
		Object maxBssd =  session.createNativeQuery(maxBssdquery)
				.setParameter("bssd", bssd)
				.uniqueResult()
				;
		
		String query = " SELECT a.applAdjRate from BizDiscRateAdjUd   a "
				+ "				where 1=1 "
				+ "				and a.applStYymm = :bssd "
				+ "				and a.intRateCd = :intRateCd "
				+ "				and a.applBizDv = :calcType "
				;
		
		return  session.createQuery(query,  Double.class)
					   .setParameter("bssd",maxBssd==null? bssd: maxBssd.toString())
					   .setParameter("intRateCd",intRateCd)
					   .setParameter("calcType",calcType)
					   .uniqueResult()
					   ;
	}
	public static List<BizDiscRateStat> getBizDiscRateStat(String bssd){
		String query = " SELECT a from BizDiscRateStat   a "
				+ "				where 1=1 "
				+ "				and a.baseYymm   = :bssd "
				;
		
		return  session.createQuery(query,  BizDiscRateStat.class)
					   .setParameter("bssd",bssd)
					   .getResultList()
					   ;
	}
	
	public static List<BizDiscRateStat> getBizDiscRateStat(String bssd, String intRateCd, String bizDv){
		String query = " SELECT a from BizDiscRateStat   a "
				+ "				where 1=1 "
				+ "				and a.baseYymm   = :bssd "
				+ "				and a.intRateCd  = :intRateCd "
				+ "				and a.applyBizDv = :calcType "
				;
		
		return  session.createQuery(query,  BizDiscRateStat.class)
					   .setParameter("bssd",bssd)
					   .setParameter("intRateCd",intRateCd)
					   .setParameter("calcType",bizDv)
					   .getResultList()
					   ;
	}
	
	public static List<InvestManageCostUd> getUserInvMgtCost(String bssd, int monNum){
		String query = " SELECT a from InvestManageCostUd   a "
				+ "				where 1=1 "
				+ "				and a.baseYymm between :stBssd and  :bssd "
				;
		
		return  session.createQuery(query,  InvestManageCostUd.class)
					   .setParameter("bssd",bssd)
					   .setParameter("stBssd",FinUtils.addMonth(bssd, monNum))
					   .getResultList()
					   ;
	}
}
