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

import com.gof.dao.IndiCrdGrdPdDao;
import com.gof.entity.IndiCrdGrdCumPd;
import com.gof.entity.IndiCrdGrdPd;
import com.gof.entity.IndiCrdGrdPdUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;

/**
 *  <p> 개인 신용등급의 불량률  산출 모형 
 *  <p> 개인 신용평점을 제시하는 KCB, NICE 및 감독원이 제시하는 불량률을 이용하여  1년 부도율, 누적 부도율, 선도부도율을 산출함.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job52_IndividualPd {
	private final static Logger logger = LoggerFactory.getLogger("CumPD");

	public static void writeIndividualPd(String bssd, Properties prop) {
		
		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();

		for (IndiCrdGrdPd aa : getIndividualPd(bssd)) {
//			logger.info("IndiCrdGrdPd is inserted : {},{}", aa.toString(), session.contains(aa));
			session.saveOrUpdate(aa);
		}

		session.getTransaction().commit();
		logger.info(" {} Individual Pd is inserted" );
	}

	public static List<IndiCrdGrdPd> getIndividualPd(String bssd) {
		List<IndiCrdGrdPd> rstList = new ArrayList<IndiCrdGrdPd>();
		IndiCrdGrdPd tempKcb;
		IndiCrdGrdPd tempNice;
		IndiCrdGrdPd tempAvg;

		List<IndiCrdGrdPdUd> indiPdUd = IndiCrdGrdPdDao.getIndiPdUd(bssd);

		Map<String, Double> niceVolMap = getVolMap(bssd, "03");
		Map<String, Double> avgVolMap =  getVolMap(bssd, "07");
		Map<String, Double> kcbVolMap =  getVolMap(bssd, "08");
		
		for (IndiCrdGrdPdUd aa : indiPdUd) {
			
			tempNice = new IndiCrdGrdPd();
			tempNice.setBaseYymm(bssd);
			tempNice.setCrdEvalAgncyCd("03");
			tempNice.setCbGrdCd(aa.getCbGrdCd());
			tempNice.setPd(aa.getNicePd());
			tempNice.setVol(Math.max(aa.getNicePd()/10, niceVolMap.getOrDefault(aa.getCbGrdCd(), 0.0)));
			tempNice.setLastModifiedBy("ESG_52");
			tempNice.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempNice);

			tempKcb = new IndiCrdGrdPd();
			tempKcb.setBaseYymm(bssd);
			tempKcb.setCrdEvalAgncyCd("08");
			tempKcb.setCbGrdCd(aa.getCbGrdCd());
			tempKcb.setPd(aa.getKcbPd());
			tempKcb.setVol(Math.max(aa.getKcbPd()/10, kcbVolMap.getOrDefault(aa.getCbGrdCd(), 0.0)));
			tempKcb.setLastModifiedBy("JOB_52");
			tempKcb.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempKcb);
			
			tempAvg = new IndiCrdGrdPd();

			tempAvg.setBaseYymm(bssd);
			tempAvg.setCrdEvalAgncyCd("07");
			tempAvg.setCbGrdCd(aa.getCbGrdCd());
			tempAvg.setPd((aa.getKcbPd() + aa.getNicePd())/2.0);
			tempAvg.setVol(Math.max(aa.getKcbPd()/10, kcbVolMap.getOrDefault(aa.getCbGrdCd(), 0.0)));
			tempAvg.setLastModifiedBy("JOB_52");
			tempAvg.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempAvg);
		}
		logger.info("Job52( Individual PD Calculation) creates  {} results.  They are inserted into EAS_INDI_CRD_GRD_PD Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("IndividualPD Result : {}", s.toString()));
		
		return rstList;
	}
	
	public static void writeIndividualCumPd(String bssd, Properties prop) {
		Session session = HibernateUtil.getSessionFactory(prop).openSession();
		session.beginTransaction();
		for (IndiCrdGrdCumPd aa : getIndividualCumPdOld(bssd)) {
//			logger.info("aaa : {}", aa.toString());
			session.saveOrUpdate(aa);
		}
//		getIndividualCumPd(bssd).stream().forEach(s ->session.saveOrUpdate(s));
		session.getTransaction().commit();
	}
	
	public static List<IndiCrdGrdCumPd> getIndividualCumPd(String bssd) {
		List<IndiCrdGrdCumPd> rstList = new ArrayList<IndiCrdGrdCumPd>();
		String indiCumPdMethod = ParamUtil.getParamMap().getOrDefault("indiCumPdMethod", "01");
//		indiCumPdMethod = "04";
		
		List<IndiCrdGrdPdUd> indiPdUd = IndiCrdGrdPdDao.getIndiPdUd(bssd);
		
		for(IndiCrdGrdPdUd aa : indiPdUd) {
			rstList.addAll(aa.getCumPd(bssd, "03", indiCumPdMethod));
			rstList.addAll(aa.getCumPd(bssd, "08", indiCumPdMethod));
			rstList.addAll(aa.getCumPd(bssd, "07", indiCumPdMethod));
		}
		return rstList;
	}
	
	public static List<IndiCrdGrdCumPd> getIndividualCumPdOld(String bssd) {
		List<IndiCrdGrdCumPd> rstList = new ArrayList<IndiCrdGrdCumPd>();
		IndiCrdGrdCumPd tempKcb;
		IndiCrdGrdCumPd tempNice;
		IndiCrdGrdCumPd tempAvg;
		
		List<IndiCrdGrdPdUd> indiPdUd = IndiCrdGrdPdDao.getIndiPdUd(bssd);
		
		for (IndiCrdGrdPdUd aa : indiPdUd) {
			double kcbPrevCumPd=0.0;
			double kcbFwdPd=0.0;
			double kcbCurrCumPd=0.0;
			
			double nicePrevCumPd=0.0;
			double niceFwdPd=0.0;
			double niceCurrCumPd=0.0;
			
			double avgPrevCumPd=0.0;
			double avgFwdPd=0.0;
			double avgCurrCumPd=0.0;
			
			for( int k=1 ; k<= 30; k++) {			//감독원 데이터가 30Y 증감율임.
//				NICE 등급 불량률
				niceCurrCumPd = Math.min(aa.getNicePd() * aa.getFwdChangeRate(k), 1.0);
				niceFwdPd     = nicePrevCumPd==1? 0.0: (niceCurrCumPd - nicePrevCumPd) /(1-nicePrevCumPd);
				
				tempNice = new IndiCrdGrdCumPd();
				tempNice.setBaseYymm(bssd);
				tempNice.setCrdEvalAgncyCd("03");
				tempNice.setCbGrdCd(aa.getCbGrdCd());
				tempNice.setMatCd("M"+ String.format("%04d", k*12));
				tempNice.setCumPd(niceCurrCumPd);
				tempNice.setFwdPd(niceFwdPd);
				tempNice.setCumPdChgRate(nicePrevCumPd == 0? 0.0 :niceCurrCumPd/nicePrevCumPd -1);
				tempNice.setVol(0.0);
				tempNice.setLastModifiedBy("ESG_52");
				tempNice.setLastUpdateDate(LocalDateTime.now());
				rstList.add(tempNice);
				nicePrevCumPd =  niceCurrCumPd;

//				KCB 등급 불량률
				kcbCurrCumPd = Math.min( aa.getKcbPd() * aa.getFwdChangeRate(k), 1.0);
				kcbFwdPd= kcbPrevCumPd==1? 0.0: (kcbCurrCumPd - kcbPrevCumPd) /(1-kcbPrevCumPd);
				
				tempKcb = new IndiCrdGrdCumPd();
				tempKcb.setBaseYymm(bssd);
				tempKcb.setCrdEvalAgncyCd("08");
				tempKcb.setCbGrdCd(aa.getCbGrdCd());
				tempKcb.setMatCd("M"+ String.format("%04d", k*12));
				tempKcb.setCumPd(kcbCurrCumPd);
				tempKcb.setFwdPd(kcbFwdPd);
				tempKcb.setCumPdChgRate(kcbPrevCumPd == 0? 0.0 :kcbCurrCumPd/kcbPrevCumPd -1);
				tempKcb.setVol(0.0);
				tempKcb.setLastModifiedBy("ESG_52");
				tempKcb.setLastUpdateDate(LocalDateTime.now());
				rstList.add(tempKcb);
				kcbPrevCumPd =  kcbCurrCumPd;
				
				
//				평균 불량률
				avgCurrCumPd = Math.min((aa.getKcbPd()+ aa.getNicePd()) /2.0 * aa.getFwdChangeRate(k), 1.0);
				avgFwdPd= avgPrevCumPd==1? 0.0: (avgCurrCumPd - avgPrevCumPd) /(1-avgPrevCumPd);
				tempAvg = new IndiCrdGrdCumPd();
				
				tempAvg.setBaseYymm(bssd);
				tempAvg.setCrdEvalAgncyCd("07");
				tempAvg.setCbGrdCd(aa.getCbGrdCd());
				tempAvg.setMatCd("M"+ String.format("%04d", k*12));
				tempAvg.setCumPd(avgCurrCumPd);
				tempAvg.setFwdPd(avgFwdPd);
				tempAvg.setCumPdChgRate(avgPrevCumPd == 0? 0 :avgCurrCumPd/avgPrevCumPd -1);
				tempAvg.setVol(0.0);
				tempAvg.setLastModifiedBy("ESG_52");
				tempAvg.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempAvg);
				avgPrevCumPd =  avgCurrCumPd;
			}
		}
		
		logger.info("Job52( Individual Cumulative PD Calculation) creates  {} results.  They are inserted into EAS_INDI_CRD_GRD_CUM_PD Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Individual Cumulative PD Result : {}", s.toString()));
		return rstList;
	}
	
	
	private static List<IndiCrdGrdCumPd> getIndividualCumPdFromFwd(String bssd) {
		List<IndiCrdGrdCumPd> rstList = new ArrayList<IndiCrdGrdCumPd>();
		IndiCrdGrdCumPd tempKcb;
		
		List<IndiCrdGrdPdUd> indiPdUd = IndiCrdGrdPdDao.getIndiPdUd(bssd);
		
		double prevCumPd=0.0;
		double fwdPd=0.0;
		double currCumPd=0.0;
		
		for (IndiCrdGrdPdUd aa : indiPdUd) {
			for( int k=1 ; k<= 30; k++) {
				if(k==1) {
					fwdPd = aa.getKcbPd() ;
//					평균값으로 적용
//					fwdPd = (aa.getKcbPd() + aa.getNicePd())/2 ;

					prevCumPd  = 0.0;
				}
				else {
					fwdPd = aa.getKcbPd() * (1+ aa.getFwdChangeRate(k)/100);
//					fwdPd = (aa.getKcbPd() + aa.getNicePd())/2 * (1+ aa.getFwdChangeRate(k)/100);

					prevCumPd  = currCumPd ;
				}
				currCumPd = fwdPd + (1-fwdPd) * prevCumPd;
				
				tempKcb = new IndiCrdGrdCumPd();
				
				tempKcb.setBaseYymm(bssd);
				tempKcb.setCrdEvalAgncyCd("07");
				tempKcb.setCbGrdCd(aa.getCbGrdCd());
				tempKcb.setMatCd("M"+ String.format("%04d", k*12));
				tempKcb.setCumPd(currCumPd);
				tempKcb.setFwdPd(fwdPd);
				tempKcb.setCumPdChgRate(k==1? 0 :currCumPd/prevCumPd -1);
				tempKcb.setVol(0.0);
				tempKcb.setLastModifiedBy("JOB_52");
				tempKcb.setLastUpdateDate(LocalDateTime.now());
				
//				logger.info("aaa : {},{},{}", k, aa.getCbGrdCd(), fwdPd);
				rstList.add(tempKcb);
			}
		}
		return rstList;
	}
	
	private static Map<String, Double> getVolMap(String bssd, String agency) {
		List<IndiCrdGrdPd> gradeList = IndiCrdGrdPdDao.getAgencyPd(bssd, agency);
		Map<String, List<IndiCrdGrdPd>> rstMap = gradeList.stream().collect(Collectors.groupingBy(s -> s.getCbGrdCd(), Collectors.toList()));
		
		return FinUtils.getVolMap(rstMap);
	}

	
	
	
}
