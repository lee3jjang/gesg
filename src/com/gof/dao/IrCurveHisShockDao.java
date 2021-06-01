package com.gof.dao;

import java.util.List;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.IrCurveHis;
import com.gof.entity.IrCurveWeek;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;



/**
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class IrCurveHisShockDao {
	private final static Logger log = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	
    //TODO:
	public static List<IrCurveHis> getIrCurveListTermStructureForShock(String bssd, String stBssd, String irCurveId){
		
		String query =" select a from IrCurveHis a " 
					+ "where a.irCurveId =:irCurveId "			
					+ "and a.baseDate >= :stBssd "
					+ "and a.baseDate <= :bssd "
					//+ "and a.matCd in (:matCdList)"
					+ "order by a.baseDate, a.matCd "
					;
		
		return session.createQuery(query, IrCurveHis.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("stBssd", stBssd)
				.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
//				.setParameterList("matCdList", EBaseMatCdShock.names())
				.getResultList()
//				.collect(Collectors.groupingBy(s ->s.getBaseDate(), TreeMap::new, Collectors.toList()))
//				.collect(Collectors.groupingBy(s ->s.getMatCd(), TreeMap::new, Collectors.toList()))
				;
	}	
	
    //TODO:
	public static List<IrCurveHis> getIrCurveListTermStructureForShock(String bssd, String stBssd, String irCurveId, List<String> tenorList){
		
		String query =" select a from IrCurveHis a " 
					+ "where a.irCurveId =:irCurveId "			
					+ "and a.baseDate >= :stBssd "
					+ "and a.baseDate <= :bssd "
					+ "and a.matCd in (:matCdList)"
					+ "order by a.baseDate, a.matCd "
					;
		
		return session.createQuery(query, IrCurveHis.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("stBssd", stBssd)
				.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				.setParameterList("matCdList", tenorList)
				.getResultList()
				;
	}
	
	public static List<IrCurveWeek> getIrCurveWeekListTermStructureForShock(String bssd, String stBssd, String irCurveId, List<String> tenorList){
		
		String query =" select a from IrCurveWeek a " 
					+ "where a.irCurveId =:irCurveId "			
					+ "and a.baseDate >= :stBssd "
					+ "and a.baseDate <= :bssd "
					+ "and a.matCd in (:matCdList)"
					+ "order by a.baseDate, a.matCd "
					;
		
		return session.createQuery(query, IrCurveWeek.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("stBssd", stBssd)
				.setParameter("bssd", FinUtils.toEndOfMonth(bssd))
				.setParameterList("matCdList", tenorList)
				.getResultList()
				;
	}
	
	public static String getMaxBaseDate (String bssd, String irCurveId) {
		String query = "select max(a.baseDate) "
					 + "from IrCurveHis a "
					 + "where 1=1 "
					 + "and a.irCurveId = :irCurveId "
					 + "and a.baseDate <= :bssd	"
					 ;
		Object maxDate =  session.createQuery(query)
				 				 .setParameter("irCurveId", irCurveId)			
								 .setParameter("bssd", FinUtils.toEndOfMonth(bssd))
								 .uniqueResult();
		if(maxDate==null) {
			log.warn("IR Curve History Data is not found {} at {}!!!" , irCurveId, FinUtils.toEndOfMonth(bssd));
			return bssd;
		}
		return maxDate.toString();
	}
	
	public static List<IrCurveHis> getIrCurveHis(String bssd, String irCurveId, List<String> tenorList){
		String query = "select a from IrCurveHis a "
					 + "where 1=1 "
					 + "and a.irCurveId =:irCurveId "
					 + "and a.baseDate  = :bssd	"
					 + "and a.matCd in (:matCdList)"
					 + "order by a.matCd"
					 ;
		
		List<IrCurveHis> curveRst =  session.createQuery(query, IrCurveHis.class)
				.setParameter("irCurveId", irCurveId)
				.setParameter("bssd", getMaxBaseDate(bssd, irCurveId))
				.setParameterList("matCdList", tenorList)
				.getResultList();		

		return curveRst;
	}	
}
