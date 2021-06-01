package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.SegLgdDao;
import com.gof.entity.SegLgd;
import com.gof.entity.SegLgdUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;

/**
 *  <p> 세그먼트별 부도시 손실률(LGD) 산출 모형 
 *  <p> 감독원이 제시하는 부도시 손실률 산출
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job53_SegLgd {
	private final static Logger logger = LoggerFactory.getLogger("LGD");

	public static void writeSegLgd(String bssd, Properties prop) {
		
		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();

		for (SegLgd aa : getSegLgd(bssd)) {
//			logger.info("IndiCrdGrdPd is inserted : {},{}", aa.toString(), session.contains(aa));
			session.saveOrUpdate(aa);
		}

		session.getTransaction().commit();
		logger.info(" {} Individual Pd is inserted" );
	}

	public static List<SegLgd> getSegLgd(String bssd) {
		List<SegLgd> rstList = new ArrayList<SegLgd>();
		SegLgd tempLgd;
		

		List<SegLgdUd> lgdList = SegLgdDao.getSegLgdUd(bssd);

		Map<String, Double> volMap =  getVolMap(bssd);
//		volMap.entrySet().stream().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()));
		
		for (SegLgdUd aa : lgdList) {
			tempLgd = new SegLgd();

			tempLgd.setBaseYymm(bssd);
			tempLgd.setLgdCalcTypCd("07");
			tempLgd.setSegId(aa.getSegId());
			tempLgd.setLgd(aa.getLgd());
			tempLgd.setVol(Math.max(0.0001, volMap.getOrDefault(aa.getSegId(), 0.0)));

			tempLgd.setLastModifiedBy("ESG_53");
			tempLgd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempLgd);
			
		}
		
		logger.info("Job53( Segment LGD Calculation) creates  {} results.  They are inserted into EAS_SEG_LGD Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Segment LGD Result : {}", s.toString()));
		return rstList;
	}
	
	private static Map<String, Double> getVolMap(String bssd) {
		List<SegLgd> lgdList = SegLgdDao.getSegLgdLessThan(bssd);
		Map<String, List<SegLgd>> rstMap = lgdList.stream().collect(Collectors.groupingBy(s -> s.getSegId(), Collectors.toList()));
		
		return FinUtils.getVolMap(rstMap);
	}

}
