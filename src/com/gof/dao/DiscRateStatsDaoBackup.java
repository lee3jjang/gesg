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
 *  <p> �������� ���м� ���� �� DataBase ���� �����ϴ� ����� �����ϴ� Class ��
 *  <p> �������� ���м� ������ ���������� ���Ǵ� ��쿡 �����.( �Ű������� ����͸�, ����� �Է� ���� ���� ��) 
 *  <p> �̷� �������� ������ ����Ǵ� ���м� ������ �������� ���� �̷�, �ܺαݸ� ����ġ ��� �Բ� ���������� ����ǹǷ� {@link DiscRateSettingDao}���� ���� ������ ������.
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
	*  <p> ����ڰ� �Է��� �������� ���м� ����� ������. 
	*  <p> �Է��� ���س���� ��ȿ�� �������� ���м� ����� ���� �ֽ��� �м� ����� ������
	*  <p> ������ ������ �����ϰ� �̷� �������� ������ ������ �Ű������� ��ȯ�ϴ� �۾��� Ȱ����.    
	*  @param bssd 	   ���س��
	*  @return		   ����� �Է� �������� ���м�                 
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
