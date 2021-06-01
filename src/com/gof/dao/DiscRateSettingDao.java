package com.gof.dao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateHis;
import com.gof.entity.DiscRateWght;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;


/**
 *  <p> 공시이율 산출방안을 정의한 정보 {@link DiscRateCalcSetting } 를 DataBase 에서 추출하는 기능을 수행하는 Class 임                 
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class DiscRateSettingDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

//	private static String baseQuery = "select a from DiscRateCalcSetting a where 1=1 ";
//
//	public static List<DiscRateCalcSetting> getEntities() {
//		return session.createQuery(baseQuery, DiscRateCalcSetting.class).getResultList();
//	}

	/** 
	*  <p> 공시이율 산출방안을 정의한 설정정보{@link DiscRateCalcSetting }를 추출함. 
	*  @return		 공시이율 산출 설정 정보 {@link DiscRateCalcSetting }                
	*/
	public static List<DiscRateCalcSetting> getDiscRateSettings(){
		String sql = "select a from DiscRateCalcSetting a "
					+ "		where 1=1 "
//	    			+ " 	and a.discRateType in ('01','02') "
	    			;
		 		
	    List<DiscRateCalcSetting> rst = session.createQuery(sql, DiscRateCalcSetting.class).getResultList();
	    
		return rst;
	}
	
	public static List<DiscRateCalcSetting> getDiscRateSettings(String intRateCd){
		 String sql = "select a from DiscRateCalcSetting a "
	    			+ "			where 1=1"
	    			+ "			and intRateCd = :param"
	    			;
		 		
	    List<DiscRateCalcSetting> rst = session.createQuery(sql, DiscRateCalcSetting.class)
	    							.setParameter("param", intRateCd)
	    							.getResultList();

	    
		return rst;
	}
	
	
	public static DiscRateHis getDiscRateHis(String bssd, String intRateCd){
		 String sql = "select a from DiscRateHis a "
	    			+ "			where 1=1"
	    			+ "			and a.baseYymm = :bssd"
	    			+ "			and intRateCd = :param"
	    			;
		 		
//	    List<DiscRateHis> rst = session.createQuery(sql, DiscRateHis.class)
//	    							.setParameter("bssd", bssd)
//	    							.setParameter("param", intRateCd)
//	    							.getResultList();
		 DiscRateHis rst = session.createQuery(sql, DiscRateHis.class)
					.setParameter("bssd", bssd)
					.setParameter("param", intRateCd)
					.uniqueResult()
					;
	    
		return rst;
	}
	
	public static List<DiscRateHis> getDiscRateHis(String bssd, int monthNum, String intRateCd){
		 String sql = "select a from DiscRateHis a "
	    			+ "			where 1=1"
	    			+ "			and a.baseYymm <= :bssd"
	    			+ "			and a.baseYymm >= :stBssd"
	    			+ "			and intRateCd = :param"
	    			;
		 		
//	    List<DiscRateHis> rst = session.createQuery(sql, DiscRateHis.class)
//	    							.setParameter("bssd", bssd)
//	    							.setParameter("param", intRateCd)
//	    							.getResultList();
		 
		 List<DiscRateHis> rst = session.createQuery(sql, DiscRateHis.class)
									.setParameter("bssd", bssd)
									.setParameter("stBssd", FinUtils.addMonth(bssd, monthNum))
									.setParameter("param", intRateCd)
									.getResultList()
									;
	    
		return rst;
	}
	
	public static DiscRateWght getDiscRateWeight(String bssd, String intRateCd){
		 String sql = "select a from DiscRateWght a "
	    			+ "			where 1=1"
	    			+ "			and a.baseYymm = :bssd"
	    			+ "			and intRateCd = :param"
	    			;
		 		
		 DiscRateWght rst = session.createQuery(sql, DiscRateWght.class)
					.setParameter("bssd", bssd)
					.setParameter("param", intRateCd)
					.uniqueResult()
					;
	    
		return rst;
	}
	
	/** 
	*  <p> 공시이율 산출방안을 정의한 설정정보{@link DiscRateCalcSetting }를 추출함. 
	*  <p> 이때 산출방안과 관련된 공시이율 과거 이력, 외부기준금리 가중치 (alpha, beta), 공시이율 산출시 적용할 매개변수 등의 정보도 함께 추출하여 Entity Class 에 설정함.   
	*  @param bssd 	   기준년월
	*  @return		 공시이율 산출 설정 정보 {@link DiscRateCalcSetting }                
	*/ 
	public static List<DiscRateCalcSetting> getDiscRateSettingsWithOtherInfo(String bssd){
		Map<String, Object> discParam = new HashMap<String, Object>();
//		discParam.put("intRateCd", "0305");
		

		Map<String, Map<String, Object>> filter = new HashMap<String, Map<String,Object>>();
		filter.put("discRateHisEqBaseYymm", new HashMap<String, Object>(){ {put("baseYymm", bssd);}});
		filter.put("discRateWghtEqBaseYymm", new HashMap<String, Object>(){ {put("baseYymm", bssd);}});
		filter.put("discRateStatEqApplStYymm", new HashMap<String, Object>(){ {put("applStYymm", bssd);}});			
		
		List<DiscRateCalcSetting> discSetting = DaoUtil.getEntities(DiscRateCalcSetting.class, discParam, filter);
		
		return discSetting;
		
	}
	public static List<String> getDiscRateExternalIntRateUd(String bssd){
		String query = " SELECT  A.KTB_Y5_IR, A.CORP_Y3_IR, A.MNSB_Y1_IR, A.CD_91_IR"
				+ "			FROM ESG.EAS_USER_DISC_RATE_EX_BASE_IR A"
				+ "		    WHERE 1=1 "
				+ "			AND  A.BASE_YYMM =:bssd "
				;
		
		List<Object[]> temp = session.createNativeQuery(query )
									.setParameter("bssd",bssd)
									.getResultList()
									;
		return Arrays.stream(temp.get(0)).map(s ->s.toString()).collect(Collectors.toList());
		
	}
	
	public static List<String> getDiscRateCumAssetYieldUd(String bssd){
		String query = " SELECT  A.ACCT_DV_CD "
				+ "			FROM ESG.EAS_USER_ASST_REVN_CUM_RATE A"
				+ "		    WHERE 1=1 "
				+ "			AND  A.BASE_YYMM =:bssd "
				;
		
		List<Object> cfTemp =  session.createNativeQuery(query )
									.setParameter("bssd",FinUtils.addMonth(bssd, -2))
									.getResultList();
		return cfTemp.stream().map(s -> s.toString()).collect(Collectors.toList());
	}
	public static List<String> getDiscRateAssetYieldUd(String bssd){
		String query = " SELECT  A.ACCT_DV_CD "
				+ "			FROM ESG.EAS_USER_ASST_REVN_RATE A"
				+ "		    WHERE 1=1 "
				+ "			AND  A.BASE_YYMM =:bssd "
				;
		
		List<Object> cfTemp =  session.createNativeQuery(query )
									.setParameter("bssd",FinUtils.addMonth(bssd, -2))
									.getResultList();
		return cfTemp.stream().map(s -> s.toString()).collect(Collectors.toList());
		
//		List<String> cfTemp =  session.createNativeQuery(query, String.class)
//				.setParameter("bssd",FinUtils.addMonth(bssd, -2))
//				.getResultList();
//		return cfTemp;
	}
	
	

}
