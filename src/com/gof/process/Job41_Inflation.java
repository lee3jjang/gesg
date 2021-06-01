package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sound.midi.SysexMessage;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.InflationDao;
import com.gof.entity.Inflation;
import com.gof.entity.InflationUd;
import com.gof.util.FinUtils;
import com.gof.util.HibernateUtil;
import com.gof.util.ParamUtil;
/**
 *  <p> 인플레이션 지표 산출모형 
 *  <p> 정부기관에서 고시하는 소비자 물가지수 (CPI)의 전년동월 대비 상승률의 평균치로 인플레이션 지표를 생성함.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job41_Inflation {
	private final static Logger logger = LoggerFactory.getLogger("Inflation");

//	public static void writeSegLgd(String bssd, Properties prop) {
//		
//		Session session = HibernateUtil.getSessionFactory(prop).openSession();
//		session.beginTransaction();
//
//		for (Inflation aa : getInflation(bssd)) {
////			logger.info("IndiCrdGrdPd is inserted : {},{}", aa.toString(), session.contains(aa));
//			session.saveOrUpdate(aa);
//		}
//
//		session.getTransaction().commit();
//		logger.info(" {} Individual Pd is inserted" );
//	}

	/*public static List<Inflation> getInflation(String bssd) {
		List<Inflation> rstList = new ArrayList<Inflation>();
		Inflation tempInflation;
		int slideWindowNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationSlidingWindowNum", "-12"));
		int avgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
		
		List<InflationUd> infList = InflationDao.getPrecedingInflationUd(bssd, slideWindowNum);
		
		List<Inflation> infHis = InflationDao.getPrecedingInflation(bssd, avgNum);

		Map<String, Double> volMap =  getVolMap(bssd);
//		volMap.entrySet().stream().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()));
		
		double curInfIndex = 0.0;
		double prevInfIndex =0.0;
		double vol = volMap.getOrDefault("CPI", 0.001);
		
		for (InflationUd aa : infList) {
			if(aa.getBaseYymm().equals(bssd)) {
				curInfIndex = aa.getInflationIndex();
			}
			else if(aa.getBaseYymm().equals(FinUtils.addMonth(bssd, slideWindowNum))) {
				prevInfIndex = aa.getInflationIndex();
//				logger.info("Prev: {},{}", prevInfIndex, FinUtils.addMonth(bssd, slideWindowNum));
			}
		}
		double avgSum =0.0;
		int   avgCnt =0;
		for(Inflation bb : infHis) {
			avgSum = avgSum + bb.getInflation();
			avgCnt  = avgCnt +1;
		}
		
		double tempInf = prevInfIndex==0? 0.0 : 100* (curInfIndex/prevInfIndex - 1);
		
		infHis.stream()
//		.filter(s->s.getBaseYymm().equals(FinUtils.addMonth(bssd, slideWindowNum)))
		.forEach(s-> logger.info("zzz : {},{},{}", s.getBaseYymm(), s.getInflationIndex(), s.getInflation()));
		
		logger.info("Prev: {},{},{},{},{},{}", prevInfIndex, curInfIndex, tempInf, avgCnt, avgSum, ( avgSum + tempInf )/ (avgCnt +1));
		
		if(curInfIndex > 0.0) {
			
			tempInflation = new Inflation();
			
			tempInflation.setBaseYymm(bssd);
			tempInflation.setInflationId("CPI");
//			tempInflation.setInflationIndex(curInfIndex);
//			tempInflation.setInflation( prevInfIndex==0? 0.0 : 100* (curInfIndex/prevInfIndex - 1) );
			
			tempInflation.setInflationIndex(tempInf);								//전년동월 대비 수익률 계산값을 저장하는 것으로 수정
			tempInflation.setInflation( ( avgSum + tempInf )/ (avgCnt +1) );        // 전년동월 대비 수익률의 5년 평균 
			
			tempInflation.setMgmtTargetLowerVal(prevInfIndex - vol * 1.96 * Math.sqrt(12));
			tempInflation.setMgmtTargetUpperVal(prevInfIndex + vol * 1.96 * Math.sqrt(12));
			tempInflation.setVol(vol * Math.sqrt(12));									//년변동성으로 변환
			
			tempInflation.setLastModifiedBy("ESG");
			tempInflation.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempInflation);
		}
		
		logger.info("Job41( Inflation Calculation) creates  {} results.  They are inserted into EAS_INFLATION Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Inflation Result : {}", s.toString()));
				
		return rstList;
	}*/
	
	
	public static List<Inflation> getInflationMA(String bssd) {
		List<Inflation> rstList = new ArrayList<Inflation>();
		Inflation tempInflation;
		int slideWindowNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationSlidingWindowNum", "-12"));
		int avgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
		
		List<InflationUd> infList = InflationDao.getPrecedingInflationUd(bssd, avgNum);
		

//		Map<String, Double> volMap =  getVolMap(bssd);
//		volMap.entrySet().stream().forEach(s -> logger.info("aaa : {},{}", s.getKey(), s.getValue()));
		
		double curInfIndex = 0.0;
		double prevInfIndex =0.0;
		double avgSum =0.0;
		int   avgCnt =0;
//		double vol = volMap.getOrDefault("CPI", 0.001);
		
		for (InflationUd curr : infList) {
			curInfIndex = curr.getInflationIndex();
			
			for(InflationUd prev : infList) {
				if(prev.getBaseYymm().equals(FinUtils.addMonth(curr.getBaseYymm(), slideWindowNum)) && prev.getInflationIndex()!= 0.0) {
					prevInfIndex = prev.getInflationIndex();
					
					avgSum = avgSum + curInfIndex/ prevInfIndex -1 ;
					avgCnt  = avgCnt +1;
					logger.info("zzz1 : {},{},{},{},{}", curr.getBaseYymm(), curr.getInflationIndex(), 100* (curInfIndex/prevInfIndex - 1) , prev.getInflationIndex(), avgSum);
				}
			}
		}
		
		double tempInf = prevInfIndex==0? 0.0 : 100* (curInfIndex/prevInfIndex - 1);
		
//		infList.stream()
//		.filter(s->s.getBaseYymm().equals(FinUtils.addMonth(bssd, slideWindowNum)))
//		.forEach(s-> logger.info("zzz : {},{},{}", s.getBaseYymm(), s.getInflationIndex()));
		
		logger.info("Prev: {},{},{},{},{},{},{},{}", prevInfIndex, curInfIndex, tempInf, avgCnt, avgSum, 100*( avgSum )/ (avgCnt ),  Math.round( 10000.0* avgSum / avgCnt) /100.0);
		
		if(curInfIndex > 0.0) {
			
			tempInflation = new Inflation();
			
			tempInflation.setBaseYymm(bssd);
			tempInflation.setInflationId("CPI");
//			tempInflation.setInflationIndex(curInfIndex);
//			tempInflation.setInflation( prevInfIndex==0? 0.0 : 100* (curInfIndex/prevInfIndex - 1) );
			
			tempInflation.setInflationIndex(tempInf);														//전년동월 대비 수익률 계산값을 저장하는 것으로 수정
			tempInflation.setInflation(100.0 * avgSum / avgCnt);       										// 전년동월 대비 수익률의 이동 평균 
//			tempInflation.setInflation( Math.round( 10000.0* avgSum / avgCnt) /100.0);       				// 전년동월 대비 수익률의 이동 평균 
			
//			tempInflation.setMgmtTargetLowerVal(prevInfIndex - vol * 1.96 * Math.sqrt(12));
//			tempInflation.setMgmtTargetUpperVal(prevInfIndex + vol * 1.96 * Math.sqrt(12));
//			tempInflation.setVol(vol * Math.sqrt(12));									//년변동성으로 변환
			
			tempInflation.setLastModifiedBy("ESG");
			tempInflation.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempInflation);
		}
		
		logger.info("Job41( Inflation Calculation) creates  {} results.  They are inserted into EAS_INFLATION Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Inflation Result : {}", s.toString()));
				
		return rstList;
	}
	
	public static List<Inflation> getInflationRate(String bssd) {
		List<Inflation> rstList = new ArrayList<Inflation>();
		Inflation tempInflation;
		int slideWindowNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationSlidingWindowNum", "-12"));
		int avgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
		
		List<InflationUd> infList = InflationDao.getPrecedingInflationUd(bssd, avgNum+slideWindowNum);

		String maxSettingYymm = InflationDao.getMaxSettingYymm(bssd);
		
		logger.info("aaaa : {},{}", infList.size(), maxSettingYymm);
		
		double curInfIndex 	= 	0.0;
		double prevInfIndex =	0.0;
		double tempInf 		=	0.0;
		for(int i= 0; i> avgNum;  i--) {
			String tempBssd = FinUtils.addMonth(bssd, i);
			
			for (InflationUd zz : infList) {
				if(zz.getBaseYymm().equals(tempBssd) && zz.getInflationIndex()!= null) {
					curInfIndex = zz.getInflationIndex();
				}
				if(zz.getBaseYymm().equals(FinUtils.addMonth(tempBssd, slideWindowNum)) && zz.getInflationIndex()!= null) {
					prevInfIndex = zz.getInflationIndex();
				}
			}
			if(curInfIndex ==0.0 || prevInfIndex==0.0) {
				logger.error("USER input CPI index error") ;
				System.exit(0);
			}
			
			tempInf = prevInfIndex==0 ? 0.0 : 100* (curInfIndex/prevInfIndex - 1);
			logger.info("zzz1 : {},{},{},{},{}", tempBssd, curInfIndex, prevInfIndex, tempInf);
			
			if(curInfIndex > 0.0) {
				tempInflation = new Inflation();
				
				tempInflation.setSettingYymm(maxSettingYymm);
				tempInflation.setBaseYymm(tempBssd);
				tempInflation.setInflationId("CPI");
				tempInflation.setInflationIndex(curInfIndex);
				tempInflation.setInflation(tempInf);		       										// 전년동월 대비 수익률
				
				tempInflation.setLastModifiedBy("ESG");
				tempInflation.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempInflation);
			}
		}
		
		logger.info("Job41( Inflation Calculation) creates  {} results.  They are inserted into EAS_INFLATION Table", rstList.size());
		rstList.stream().forEach(s->logger.debug("Inflation Result : {}", s.toString()));
				
		return rstList;
	}
	
//	private static Map<String, Double> getVolMap(String bssd) {
//		List<Inflation> infList = InflationDao.getPrecedingInflation(bssd, -36);
//		Map<String, List<Inflation>> rstMap = infList.stream().collect(Collectors.groupingBy(s -> s.getInflationId(), Collectors.toList()));
//		
//		return FinUtils.getVolMap(rstMap);
//	}

}
