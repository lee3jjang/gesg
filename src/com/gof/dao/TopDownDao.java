package com.gof.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.AssetCf;
import com.gof.entity.AssetClassYield;
import com.gof.entity.AssetYield;
import com.gof.entity.BizCrdSpreadUd;
import com.gof.entity.CashFlowMatchAdj;
import com.gof.entity.CreditSpread;
import com.gof.entity.LiabCf;
import com.gof.entity.RefPortYield;
import com.gof.entity.TopDownDcnt;
import com.gof.enums.ETopDownMatCd;
import com.gof.util.HibernateUtil;

/**
 * <p> TopDown 할인율 산출과 관련된 데이터{@link TopDownDcnt} 를 DataBase 에서 추출하는 기능을 수행하는 Class 임         
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class TopDownDao {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
	public static List<AssetCf> getRawAssetCashFlow(String bssd, int divideMon){
		List<AssetCf> rst = new ArrayList<AssetCf>();
		String query = " SELECT BASE_YYMM, 'M'||LPAD(MAT_YEAR*:divideMon, 4,0) , 'M'||LPAD(CF_MAT_YEAR*:divideMon, 4,0), ROUND(SUM(CF_AMT),0)  "
				+ "		 FROM ( SELECT :bssd AS BASE_YYMM "
				+ "	  				  , ROUND(MAX(MONTHS_BETWEEN(BUCKET_END_DATE, AS_OF_DATE)/:divideMon) OVER(PARTITION BY FCONT_ID),0)  AS MAT_YEAR	"
				+ "   	              , ROUND(    MONTHS_BETWEEN(BUCKET_END_DATE, AS_OF_DATE)/:divideMon  							  ,0)  AS CF_MAT_YEAR "
				+ "			          , A.VALUE    																	            AS CF_AMT"
//				+ "				FROM QCM.KICS_AL_RSLT_A_BASE a"
				+ "				FROM QCM.KICS_AL_RSLT_A_CF a"
				+ "			    WHERE SCEN_NUM = 1"
				+ "				AND   FE_CD ='9370'"
				+ "             AND   PROC_ID ='KICS_A_PREPAY' "
				+ "             AND   SUBSTR(FCONT_ID,1,2) IN ( 'BD', 'UJ') "
				+ "			    AND   TO_CHAR(AS_OF_DATE,'YYYYMM') =:bssd "
				+ "			   ) "
				+ "		WHERE MAT_YEAR >=0 "
				+ "     AND   CF_MAT_YEAR >=0  "
				+ "		AND   CF_AMT > 0 "
				+ "		GROUP BY BASE_YYMM, MAT_YEAR, CF_MAT_YEAR"
				;
		
		List<Object[]> cfTemp =  session.createNativeQuery(query)
									.setParameter("bssd",bssd)
									.setParameter("divideMon", divideMon)
									.getResultList();
		;
		
		for(Object[] aa :cfTemp) {
			rst.add( new AssetCf(bssd, aa[1].toString(),aa[2].toString(),  Double.parseDouble(aa[3].toString()), 0.0, "ESG", LocalDateTime.now()));
		}
		return rst;
	}

	//	TODO : 부채 CF 원천 테이블 확인
		public static List<LiabCf> getRawLiabilityCashFlow(String bssd, int divideMon){
			List<LiabCf> rst = new ArrayList<LiabCf>();
			
			String query = " SELECT BASE_YYMM, 'M'||LPAD(CF_MAT_YEAR*:divideMon, 4,0), ROUND(SUM(CF_AMT),0) "
					+ "		 FROM ( SELECT :bssd 												    AS BASE_YYMM "
					+ "   	              , ROUND(MONTHS_BETWEEN(BUCKET_END_DATE, AS_OF_DATE)/:divideMon,0)  AS CF_MAT_YEAR "
					+ "			          , A.VALUE    							           		    AS CF_AMT"
					+ "				FROM QCM.Q_AL_RSLT_L_QIS a"				//QIS 용
//					+ "				FROM QCM.Q_AL_RSLT_L	 a"				//정상적인 상황
					+ "			    WHERE 1=1"
					+ "				AND   SCEN_NUM = 1"
					+ "				AND   CONT_TP = 1 "						//원수 계약만 적용
//					+ "				AND   MOV_STEP_SEQ = 4 "				// 4 : 재량 변경 적용 후
					+ "				AND   MOV_STEP_SEQ = 1 "				// 1 : 시점변경 적용 후 
					+ "				AND   FE_CD ='9372' "
					+ "			    AND   TO_CHAR(AS_OF_DATE,'YYYYMM') =:bssd "
					+ "				)"
					+ "		WHERE CF_MAT_YEAR >= 0 "
					+ "		AND   CF_AMT > 0 "
					+ "		GROUP BY BASE_YYMM, CF_MAT_YEAR"
			;
			
			List<Object[]> cfTemp =  session.createNativeQuery(query)
										.setParameter("bssd", bssd)
										.setParameter("divideMon", divideMon)
										.getResultList();
			;
			
			for(Object[] aa :cfTemp) {
				rst.add( new LiabCf(bssd, aa[1].toString(), Double.parseDouble(aa[2].toString()), 0.0, "ESG", LocalDateTime.now()));
			}
			
			return rst;
		}

	/** 
	*  자산 현금흐름 추출할 DAO    
	*  @param bssd 	   기준년월
	*  @return        자산 현금흐름   
	*/ 
	
	public static List<AssetCf> getAssetCashFlow(String bssd){
		List<AssetCf> rst = new ArrayList<AssetCf>();
		String query = " SELECT  a FROM AssetCf a "
				+ "		WHERE a.baseYymm = :bssd"
//				+ "     and a.matCd <= :maxMatCd"					//잔존만기가 최장만기까지 추출함.
				+ "		AND a.cfMatCd <= :maxCfMatCd"				//최장 만기까지의 현금흐름만 추출함. ==> 잔존만기는 모두 추출하게됨		
				+ "     ORDER BY a.cfMatCd, a.matCd "
				;
		return session.createQuery(query, AssetCf.class)
					 .setParameter("bssd", bssd)
//					 .setParameter("maxMatCd", "M0240")				//잔존만기가 최장 만기까지 추출함.
					 .setParameter("maxCfMatCd", "M0240")			//최장 만기까지의 현금흐름만 추출함.==> 잔존만기는 모두 추출하게됨
					 .getResultList();
	}
	
	/** 
	*  부채 현금흐름 추출할 DAO    
	*  @param bssd 	   기준년월
	*  @return        부채 현금흐름   
	*/ 
	public static List<LiabCf> getLiabilityCashFlow(String bssd){
		String query = " SELECT  a FROM LiabCf a "
				+ "		WHERE a.baseYymm = :bssd "
				+ "		AND a.cfMatCd <= :maxCfMatCd"
				+ "     ORDER BY a.cfMatCd "
				;
		return session.createQuery(query, LiabCf.class)
								.setParameter("bssd", bssd)
								.setParameter("maxCfMatCd", "M0240")
								.getResultList();
	}
	
	
	public static List<CashFlowMatchAdj> getCfMatchAdj(String bssd){
		String query = " SELECT a FROM CashFlowMatchAdj a "
				+ "		 WHERE a.baseYymm =:bssd"
				;
		
		return session.createQuery(query, CashFlowMatchAdj.class)
					.setParameter("bssd", bssd)
					.getResultList();
	}
	
	public static Map<String, Double> getLoanAssetYieldMap(String bssd){
		Map<String, Double> rst = new HashMap<>();
		String query = " SELECT substr(FCONT_ID, 5), EIR "
				+"		FROM QCM.KICS_EIR "
				+ "		WHERE 1=1"
				+ "		AND SUBSTR(TO_CHAR(AS_OF_DATE,'YYYYMMDD'),1,6) = :bssd"
				;
		
		List<Object[]> eirList =  session.createNativeQuery(query)
				.setParameter("bssd", bssd)
				.getResultList();
		
		for(Object[] aa :eirList) {
			BigDecimal temp = aa[1]==null? new BigDecimal(0.0):(BigDecimal)aa[1]; 
			rst.put(aa[0].toString(), temp.doubleValue());
		}

		return rst;				

	}
	public static Map<String, Double> getLoanResidualSpreadMap(String bssd){
		Map<String, Double> rst = new HashMap<>();
		String query = " SELECT substr(FCONT_ID, 5), RS_ISSUE "
				+"		FROM QCM.KICS_EIR "
				+ "		WHERE 1=1"
				+ "		AND SUBSTR(TO_CHAR(AS_OF_DATE,'YYYYMMDD'),1,6) = :bssd"
				;
		
		List<Object[]> eirList =  session.createNativeQuery(query)
				.setParameter("bssd", bssd)
				.getResultList();
		
		for(Object[] aa :eirList) {
			BigDecimal temp = aa[1]==null? new BigDecimal(0.0):(BigDecimal)aa[1]; 
			rst.put(aa[0].toString(), temp.doubleValue());
		}

		return rst;				

	}
	
	public static List<AssetYield> getAssetYield(String bssd){
		List<AssetYield> rst = new ArrayList<AssetYield>();
		
		String query = " SELECT a FROM AssetYield a "
				+ "		 WHERE a.baseYymm =:bssd"
//				+ "		 and a.expoId NOT LIKE 'UJ%'"                               //TODO : 
//				+ "      and   a.expoId ='BDUS12189LAE11000000001376283100020BD510700015107' "
				;
		
		rst  = session.createQuery(query, AssetYield.class)
					.setParameter("bssd", bssd)
					.getResultList();
		
		return rst;
	}	
	
	public static List<AssetClassYield> getAssetClassYield(String bssd){
		String query = " SELECT  a FROM AssetClassYield a " 
				+ "	     WHERE 1=1"
				+ "		 AND a.baseYymm = :bssd"
//				+ "		 AND a.assetClassTypCd <> '04' " 			// 04: 대출자산 제외  TODO : 대출 자산 최초 RS 산출 반영 후 제거 
				+ "      ORDER BY a.matCd"
				;
		
		return  session.createQuery(query, AssetClassYield.class)
						.setParameter("bssd", bssd)
						.getResultList();
	}
	
	public static List<RefPortYield> getRefPortYield(String bssd){
		String query = " SELECT  a FROM RefPortYield a " 
				+ "	     WHERE 1=1"
				+ "		 AND a.baseYymm = :bssd"
				+ "      ORDER BY a.matCd"
				;
		
		return  session.createQuery(query, RefPortYield.class)
						.setParameter("bssd", bssd)
						.getResultList();
	}
	
	public static Map<String, Double> getMatYieldMap(String bssd){
		String query = " SELECT  MAT_CD , ROUND(SUM(BOOK_BAL*asst_yield)/SUM(BOOK_BAL),4)"
				+ "	 	 FROM ESG.EAS_ASST_YIELD "
				+ "	     WHERE 1=1"
				+ "		 AND BASE_YYMM = :bssd"
				+ "		 AND ASSET_CLASS_TYP_CD <> '04'"
				+ "		 GROUP BY MAT_CD "
				;
		
		List<Object[]> yieldTemp =  session.createNativeQuery(query)
										.setParameter("bssd", bssd)
										.getResultList();
		Map<String, Double> rst = new HashMap<String, Double>();
		
		for(Object[] aa : yieldTemp) {
			rst.put(aa[0].toString(), Double.parseDouble(aa[1].toString()));
		}
		return rst;	
	}
	
	public static List<CreditSpread> getCreditSpread(String bssd){
		String query = " SELECT a FROM CreditSpread a "
				+ "		 WHERE a.baseYymm =:bssd"
				+ "      AND a.matCd in (:matList)"
				;
		
		return session.createQuery(query, CreditSpread.class)
					.setParameter("bssd", bssd)
					.setParameterList("matList", ETopDownMatCd.names())
					.getResultList()
//					.stream()
//					.collect(Collectors.toMap(s -> s.getMatCd(), s ->s.getCrdSpread()))
					;
	}
	
	public static List<BizCrdSpreadUd> getCreditSpreadUd(String bssd){
		String maxBaseYymm = "select max(a.applStYymm) "
				+ "			from BizCrdSpreadUd a "
    			+ "			where 1=1"
    			+ "			and a.applyBizDv =:bizDv "
    			+ "			and a.applStYymm  <=  :time"
    			+ "			and a.applEdYymm  >=  :time"
		;
		
		Object maxYymm= session.createQuery(maxBaseYymm)
								.setParameter("time",bssd.substring(0,6))
								.setParameter("bizDv", "I")
								.uniqueResult();
		
			String query = " SELECT a FROM BizCrdSpreadUd a "
					+ "		 WHERE a.applStYymm =:bssd"
					+ "      AND a.applyBizDv = :bizDv "
					+ "      AND a.matCd in (:matList)"
					;
			
			return session.createQuery(query, BizCrdSpreadUd.class)
					.setParameter("bssd",  maxYymm==null? bssd: maxYymm.toString())
					.setParameter("bizDv", "I")
					.setParameterList("matList", ETopDownMatCd.names())
					.getResultList()
					;
	}
}
