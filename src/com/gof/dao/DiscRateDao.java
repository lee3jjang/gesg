package com.gof.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.DiscRate;
import com.gof.entity.DiscRateHis;
import com.gof.entity.HisDiscRate;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 * <p> 공시이율 {@link DiscRate } 를 DataBase 에서 추출하는 기능을 수행하는 Class 임         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class DiscRateDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();

	
	public static List<DiscRateHis> getDiscRateHis(String bssd, int monthNum){
		String query = " SELECT a from DiscRateHis   a "
				+ "				where 1=1 "
				+ "				and a.baseYymm between :stBssd and :bssd "
				+ "				order by a.baseYymm"
				;
		
		return  session.createQuery(query,  DiscRateHis.class)
					   .setParameter("bssd",bssd)
					   .setParameter("stBssd",FinUtils.addMonth(bssd, monthNum))
					   .getResultList()
					   ;
		
	}
	
	public static List<DiscRate> getDiscRateByCalcType(String bssd, String calcType){
		String query = " select a from DiscRate a"
				+ "		 where a.baseYymm = :bssd"
				+ "		 and a.discRateCalcTyp =:calcType"
				;
		
		
		return session.createQuery(query, DiscRate.class)
					.setParameter("bssd", bssd)
					.setParameter("calcType", calcType)
					.getResultList();
	}
	
	public static List<DiscRate> getDiscRate(String bssd){
		String query = " select a from DiscRate a"
				+ "		 where a.baseYymm = :bssd"
				;
		
		
		return session.createQuery(query, DiscRate.class)
					.setParameter("bssd", bssd)
					.getResultList();
	}
	
	/** 
	*  <p> KICS 에서 제시한 공시이율 산출방안은 공시이율과 자산운용수익률 비율의 과거 36개월 평균치를 적용할 것을 요구함.
	*  <p> 입력한 기준년월 기준으로  모든 이율코드에 대해서 공시이율 비율 = 공시이율/자산운용수익률 의 36개월 평균값을 산출하여   
	*  @param bssd 	   기준년월
	*  @return		 ( 이율코드, 평균공시이율 비율) 을 (key, value) 의 Map 으로 산출함.                 
	*/ 
	
	public static Map<String, Double> getKicsAvgAdjust(String bssd){
		Map<String, Double> rstMap = new HashMap<String, Double>();

		String query = " SELECT a.INT_RATE_CD, AVG(a.APPL_DISC_RATE / DECODE(a.MGT_ASST_YIELD,  0, a.EX_BASE_IR+a.DISC_RATE_SPREAD, a.MGT_ASST_YIELD)) " 
				+ "				from ESG.EAS_DISC_RATE_HIS   a "
				+ "				where 1=1 "
				+ "				and a.BASE_YYMM <= :bssd	"
				+ "				and a.BASE_YYMM >= :stBssd "
//				+ "				and a.MGT_ASST_YIELD <> 0"
				+ "				group by a.INT_RATE_CD"
				;
		
		
		List<Object[]> rstTemp =  session.createNativeQuery(query)
				.setParameter("bssd", bssd)
				.setParameter("stBssd", FinUtils.addMonth(bssd, -36))
				.getResultList();
		
		for(Object[] aa :rstTemp) {
			rstMap.put(aa[0].toString(), Double.parseDouble(aa[1].toString()));
		}
//		logger.info("KicsAvgAdj : {}" ,rstMap);
		
		return rstMap;
	}

	/** 
	*  <p> KICS 에서 제시한 공시이율 산출방안은 공시이율과 자산운용수익률 비율의 과거 36개월 평균치를 적용할 것을 요구함.
	*  <p> 입력한 이율코드, 기준년월 기준으로 36개월간의 공시이율, 운용수익률 데이터를 추출함.   
	*  @param bssd 	   기준년월
	*  @param intRateCd		이율코드 
	*  @return			공시이율 비율 = 공시이율/자산운용수익률 의 36개월 평균값.                
	*/ 
	
	public static Double getKicsAvgAdjust(String bssd, String intRateCd){

		String query = " SELECT AVG(a.APPL_DISC_RATE / DECODE(a.MGT_ASST_YIELD,  0, a.EX_BASE_IR+a.DISC_RATE_SPREAD, a.MGT_ASST_YIELD)) " 
//		String query = " SELECT AVG(a.APPL_DISC_RATE / a.MGT_ASST_YIELD) "		
				+ "				from ESG.EAS_DISC_RATE_HIS   a "
				+ "				where 1=1 "
				+ "				and a.INT_RATE_CD =:param	"
				+ "				and a.BASE_YYMM <= :bssd	"
				+ "				and a.BASE_YYMM >= :stBssd "
//				+ "				and a.MGT_ASST_YIELD <> 0"
				+ "				group by a.INT_RATE_CD"
				;
		
		
		Object rstTemp =  session.createNativeQuery(query)
				.setParameter("bssd", bssd)
				.setParameter("stBssd", FinUtils.addMonth(bssd, -36))
				.setParameter("param", intRateCd)
				.uniqueResult()
				;
		return Double.parseDouble(rstTemp.toString());
	}
	
	public static int  getMaxSeq(String bssd, String bizDiv){
		String query = "select  a from HisDiscRate a "
		+ "				where 1=1 "
		+ "				and a.baseYymm = :bssd	"
		+ "				and a.applyBizDv   = :bizDiv	"
		;
		
		return session.createQuery(query,  HisDiscRate.class)
		   .setParameter("bssd",bssd)
		   .setParameter("bizDiv",bizDiv)
		   .getResultStream().mapToInt(s ->s.getSeq()).max().orElse(0);
		
	}
//	20200529 :Add
	public static List<Object[]> getKicsAvgDiscRateStat(String bssd){

			/**		
 			String query = " SELECT MAX(BASE_YYMM), INT_RATE_CD, AVG(LONG_INV_COST_RATE), ROUND(AVG(alpha),5) "
					+ " FROM ("
					+ "        SELECT A.BASE_YYMM, A.INT_RATE_CD, B.LONG_MGT_ASST_YIELD, A.APPL_DISC_RATE, B.LONG_INV_COST_RATE, A.APPL_DISC_RATE/B.LONG_MGT_ASST_YIELD as alpha"
					+ " 	        FROM "
					+ " 	            (SELECT BASE_YYMM,INT_RATE_CD,APPL_DISC_RATE "
					+ " 	               FROM EAS_USER_DISC_RATE_HIS "
					+ " 	              WHERE BASE_YYMM BETWEEN :stBssd AND :bssd) A "
					+ " 	        LEFT OUTER JOIN "
					+ " 	            (SELECT BASE_YYMM, LONG_MGT_ASST_YIELD, LONG_INV_COST_RATE "
					+ " 	               FROM EAS_USER_INV_MGT_COST "
					+ " 	              WHERE BASE_YYMM BETWEEN :stBssd AND :bssd) B "
					+ " 	          ON A.BASE_YYMM =B.BASE_YYMM "
					+ " 	        ) "
					+ " 	GROUP BY INT_RATE_CD "
					+ " 	ORDER BY 2  "
 			*/
/**2020.08.25 - ST.LEE - 이상진 주임 요청사항 EAS_USER_DISC_RATE_HIS 기준일자 1개월 뒤로 지연  */
/*		String query = " SELECT MAX(BASE_YYMM), INT_RATE_CD, AVG(LONG_INV_COST_RATE), ROUND(AVG(alpha),5) "
					+ " FROM ("
					+ "        SELECT A.BASE_YYMM, A.INT_RATE_CD, B.LONG_MGT_ASST_YIELD, A.APPL_DISC_RATE, B.LONG_INV_COST_RATE, A.APPL_DISC_RATE/B.LONG_MGT_ASST_YIELD as alpha"
					+ " 	        FROM "
					+ " 	            (SELECT  TO_CHAR(ADD_MONTHS(TO_DATE(BASE_YYMM,'YYYYMM'),1), 'YYYYMM')  AS BASE_YYMM "
					+ "						,INT_RATE_CD,APPL_DISC_RATE "
					+ " 	               FROM EAS_USER_DISC_RATE_HIS "
					+ " 	              WHERE BASE_YYMM BETWEEN TO_CHAR(ADD_MONTHS(TO_DATE(:stBssd,'YYYYMM'),-1), 'YYYYMM')"
					+ "                                                     AND TO_CHAR(ADD_MONTHS(TO_DATE(:bssd,'YYYYMM'),-1), 'YYYYMM') ) A "
					+ " 	        LEFT OUTER JOIN "
					+ " 	            (SELECT BASE_YYMM, LONG_MGT_ASST_YIELD, LONG_INV_COST_RATE "
					+ " 	               FROM EAS_USER_INV_MGT_COST "
					+ " 	              WHERE BASE_YYMM BETWEEN :stBssd AND :bssd) B "
					+ " 	          ON A.BASE_YYMM =B.BASE_YYMM "
					+ " 	        ) "
					+ " 	GROUP BY INT_RATE_CD "
					+ " 	ORDER BY 2  "
		;*/
		
/**2020.09.11 - ST.LEE - 이상진 주임 요청사항 EAS_USER_DISC_RATE_HIS 컬럼 추가및  SELECT절 변경 */
				String query = "  SELECT MAX(BASE_YYMM), INT_RATE_CD " + 
						"                  ,CASE WHEN INT_RATE_CD IN ('0101','0201','1101','3101','3103' ) THEN AVG(APPL_DISC_RATE) ELSE -1 *AVG(LONG_INV_COST_RATE) END REGR_CONSTANT" + 
						"                  ,CASE WHEN INT_RATE_CD IN ('0101','0201','1101','3101','3103' ) THEN 1 ELSE ROUND(AVG(APPL_DISC_RATE/LONG_MGT_ASST_YIELD), 5) END ADJ_RATE" + 
						"                  ,CASE WHEN INT_RATE_CD IN ('0101','0201','1101','3101','3103' ) THEN 0 ELSE 1 END REGR_COEF" + 
						"					  FROM (" + 
						"					         SELECT A.BASE_YYMM, A.INT_RATE_CD, B.LONG_MGT_ASST_YIELD, A.APPL_DISC_RATE, B.LONG_INV_COST_RATE, A.APPL_DISC_RATE/B.LONG_MGT_ASST_YIELD as alpha" + 
						"					  	        FROM " + 
						"					  	            (SELECT  TO_CHAR(ADD_MONTHS(TO_DATE(BASE_YYMM,'YYYYMM'),1), 'YYYYMM')  AS BASE_YYMM " + 
						"					 						,INT_RATE_CD,APPL_DISC_RATE " + 
						"					  	               FROM esg.EAS_USER_DISC_RATE_HIS " + 
						"					  	              WHERE BASE_YYMM BETWEEN TO_CHAR(ADD_MONTHS(TO_DATE(:stBssd,'YYYYMM'),-1), 'YYYYMM')" + 
						"					                                                      AND TO_CHAR(ADD_MONTHS(TO_DATE(:bssd,'YYYYMM'),-1), 'YYYYMM') ) A " + 
						"					  	        LEFT OUTER JOIN " + 
						"					  	            (SELECT BASE_YYMM, LONG_MGT_ASST_YIELD, LONG_INV_COST_RATE " + 
						"					  	               FROM esg.EAS_USER_INV_MGT_COST " + 
						"					  	              WHERE BASE_YYMM BETWEEN :stBssd AND :bssd) B " + 
						"					  	          ON A.BASE_YYMM =B.BASE_YYMM " + 
						"					  	        ) " + 
						"					  	GROUP BY INT_RATE_CD " + 
						"					  	ORDER BY 2  "  
		;		
		
		List<Object[]> rstTemp =  session.createNativeQuery(query)
				.setParameter("bssd", bssd)
				.setParameter("stBssd", FinUtils.addMonth(bssd, -11))			//20200529: add 201801 ~ 201812
				.getResultList()
				;
		return rstTemp;
	}

}
