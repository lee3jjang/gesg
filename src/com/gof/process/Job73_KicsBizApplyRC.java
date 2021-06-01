package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.dao.CorpCrdGrdPdDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.DiscRateDao;
import com.gof.dao.IndiCrdGrdPdDao;
import com.gof.dao.InflationDao;
import com.gof.dao.SegLgdDao;
import com.gof.dao.SegPrepayDao;
import com.gof.entity.BizCorpPd;
import com.gof.entity.BizCrdSpread;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateUd;
import com.gof.entity.BizIndiPd;
import com.gof.entity.BizInflation;
import com.gof.entity.BizSegLgd;
import com.gof.entity.BizSegPrepay;
import com.gof.entity.BizSegPrepayUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.CorpCumPd;
import com.gof.entity.CreditSpread;
import com.gof.entity.DiscRate;
import com.gof.entity.IndiCrdGrdCumPd;
import com.gof.entity.Inflation;
import com.gof.entity.InflationUd;
import com.gof.entity.SegLgd;
import com.gof.entity.SegPrepay;
import com.gof.util.ParamUtil;

/**
 *  <p> 경험 통계 정보를 KICS 용도로 적용하기 위해 업무목적별 활용 테이블로 이관함. 
 *  <p> 할인율, 공시이율, 기업부도율, 소매부도율, LGD, 조기상환율 등이 대상임.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job73_KicsBizApplyRC {
	private final static Logger logger = LoggerFactory.getLogger(Job73_KicsBizApplyRC.class);

	
	public static List<BizCorpPd> getBizCorpPdFromCumPd(String bssd) {
		List<BizCorpPd> rstList = new ArrayList<BizCorpPd>();
		BizCorpPd tempPd;
		
		List<CorpCumPd> corpPdList = CorpCrdGrdPdDao.getCorpCumPd(bssd);
		
		for (CorpCumPd aa : corpPdList) {
			tempPd = new BizCorpPd();

			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("K");
			tempPd.setCrdGrdCd(aa.getGradeCode());
			tempPd.setMatCd(aa.getMatCd());
			
			tempPd.setPd( aa.getMatCd().compareTo("M0012") < 1  ? aa.getCumPd(): 0.0);
			tempPd.setCumPd(aa.getCumPd());
			tempPd.setFwdPd(aa.getFwdPd());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempPd);
			
		}
		logger.info("Job 73 (Kics Applied Cumulative Corporate Pd ) create {} result.  They are inserted into EAS_BIZ_APPL_CORP_PD Table ", rstList.size());
		return rstList;
	}

	public static List<BizIndiPd> getBizIndiPdFromCumPd(String bssd) {
		List<BizIndiPd> rstList = new ArrayList<BizIndiPd>();
		BizIndiPd tempPd;

		String kicsIndiPdType = ParamUtil.getParamMap().getOrDefault("kicsIndiPdType", "07");
		
		List<IndiCrdGrdCumPd> indiPdList = IndiCrdGrdPdDao.getAgencyCumPd(bssd, kicsIndiPdType);   //감독원 제공 CB 불량률의 평균치 
		
		for (IndiCrdGrdCumPd aa : indiPdList) {
			tempPd = new BizIndiPd();

			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("K");
			tempPd.setCbGrdCd(aa.getCbGrdCd());
			tempPd.setMatCd(aa.getMatCd());
			tempPd.setCrdEvalAgncyCd(aa.getCrdEvalAgncyCd());
			
			tempPd.setCumPd(aa.getCumPd());
			tempPd.setFwdPd(aa.getFwdPd());
			tempPd.setCumPdChgRate(aa.getCumPdChgRate());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempPd);
		}
		logger.info("Job 73 (Kics Applied Cumulative Individual PD ) create {} result.  They are inserted into EAS_BIZ_APPL_INDI_PD Table ", rstList.size());
		return rstList;
	}

	public static List<BizSegLgd> getBizSegLgd(String bssd) {
		
		List<BizSegLgd> rstList = new ArrayList<BizSegLgd>();
		BizSegLgd tempPd;
		
		List<SegLgd> lgdList = SegLgdDao.getSegLgd(bssd);   //감독원 제공 CB 불량률
		
		for (SegLgd aa : lgdList) {
			tempPd = new BizSegLgd();

			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("K");
			tempPd.setSegId(aa.getSegId());
			tempPd.setLgdCalcTypCd(aa.getLgdCalcTypCd());
			
			tempPd.setApplyLgd(aa.getLgd());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempPd);
			
		}
		logger.info("Job 73 (Kics Applied Segment LGD ) create {} result.  They are inserted into EAS_BIZ_APPL_SEG_LGD Table ", rstList.size());
		return rstList;
	}
	
	public static List<BizCrdSpread> getBizCrdSpread(String bssd) {
		List<BizCrdSpread> rstList = new ArrayList<BizCrdSpread>();
		BizCrdSpread tempPd =new BizCrdSpread();
		Map<String, Object> param = new HashMap<>();
		param.put("baseYymm", bssd);
		
		List<CreditSpread> spreadList = DaoUtil.getEntities(CreditSpread.class, param);
		
		for (CreditSpread aa : spreadList) {
			
			tempPd = new BizCrdSpread();
			tempPd.setBaseYymm(bssd);
//			tempPd.setApplyStYymm(bssd);
//			tempPd.setApplyEndYymm(bssd);
			tempPd.setApplyBizDv("K");
			
			tempPd.setCrdGrdCd(aa.getCrdGrdCd());
			tempPd.setMatCd(aa.getMatCd());
			
			tempPd.setApplyCrdSpread(aa.getCrdSpread());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempPd);
		}
		
		logger.info("Job 73 (Kics Applied Credit Spread) create {} result.  They are inserted into EAS_BIZ_APLY_CRD_SPREAD Table ", rstList.size());
		return rstList;
	}
	
	public static List<BizSegPrepay> getBizSegPrepay(String bssd) {
		
		List<BizSegPrepay> rstList = new ArrayList<BizSegPrepay>();
		BizSegPrepay tempPd =new BizSegPrepay();
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("baseYymm", bssd);
		param.put("segTypCd", "5");
		List<SegPrepay> prepayList = DaoUtil.getEntities(SegPrepay.class, param);
		
		
		List<SegPrepay> calcPrepayList = SegPrepayDao.getSegPrepay(bssd);
		List<BizSegPrepayUd> udPrepayList = SegPrepayDao.getSegPrepayUd(bssd);
		
		if(udPrepayList.isEmpty()) {
			for (SegPrepay aa : prepayList) {
				
				tempPd = new BizSegPrepay();
				
				tempPd.setBaseYymm(bssd);
				tempPd.setApplyBizDv("K");
				tempPd.setSegId(aa.getSegId());
				tempPd.setApplyPrepRate(aa.getPrepayRateYr()==null?0.0:aa.getPrepayRateYr());
				tempPd.setVol(0.0);
				
				tempPd.setLastModifiedBy("ESG");
				tempPd.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempPd);
				
			}
			logger.info("Job 73 (Kics Applied Segment Prepay Rate) : Calucated Data is used");
		}
		else {
			for (BizSegPrepayUd aa : udPrepayList) {
				
				tempPd = new BizSegPrepay();
				
				tempPd.setBaseYymm(bssd);
				tempPd.setApplyBizDv("K");
				tempPd.setSegId(aa.getSegId());
				
				tempPd.setApplyPrepRate(aa.getApplyPrepRate());
				tempPd.setVol(0.0);
				
				tempPd.setLastModifiedBy("ESG");
				tempPd.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempPd);
			}
			logger.info("Job 73 (Kics Applied Segment Prepay Rate) : User Input Data is used");
		}
		
		logger.info("Job 73 (Kics Applied Segment Prepay Rate) create {} result.  They are inserted into EAS_BIZ_APLY_SEG_PREP_RATE Table ", rstList.size());
		return rstList;
	}
	
	
	public static List<BizInflation> getBizInflationMA(String bssd) {
		List<BizInflation> rstList = new ArrayList<BizInflation>();
		BizInflation tempPd =new BizInflation();
		
		int avgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
		
		List<InflationUd> infUdList = InflationDao.getPrecedingInflationUd(bssd, 0);
		
//		List<Inflation> infList = InflationDao.getPrecedingInflation(bssd, avgNum); 
		String maxSettingYymm = InflationDao.getMaxSettingYymm(bssd);
		List<Inflation> infList = InflationDao.getPrecedingInflation(bssd, avgNum).stream().filter(s->s.getSettingYymm().equals(maxSettingYymm)).collect(Collectors.toList()); 
		
		tempPd = new BizInflation();
		tempPd.setBaseYymm(bssd);
		tempPd.setApplyBizDv("K");

		double avgSum =0.0;
		int   avgCnt =0;
		if( infUdList.size() > 0 && infUdList.get(0).getKicsTgtIndex() != null && infUdList.get(0).getKicsTgtIndex() != 0.0  ) { 
			InflationUd infUd = infUdList.get(0);
			
			tempPd = new BizInflation();
			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("K");
			tempPd.setInflationId("TGT");
			tempPd.setInflation(infUd.getKicsTgtIndex());			
			
			tempPd.setMgmtTargetLowerVal(0.0);
			tempPd.setMgmtTargetUpperVal(0.0);
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempPd);
		}
		else if(infList.size()>0){
			for (Inflation zz : infList) {
				avgSum = avgSum + zz.getInflation();
				avgCnt  = avgCnt +1;
//				logger.info("zzz1 : {},{},{},{},{}", avgCnt, avgSum);
				
			}
			logger.info("zzz1 : {},{},{},{},{}", bssd, avgCnt, avgSum);
			
			Inflation inf = infList.get(0);
			
			tempPd = new BizInflation();
			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("K");
			tempPd.setInflationId(inf.getInflationId());
			tempPd.setInflationIndex(inf.getInflationIndex());
//			tempPd.setInflation(inf.getInflation());
			tempPd.setInflation(avgSum/avgCnt);
			
			tempPd.setMgmtTargetLowerVal(0.0);
			tempPd.setMgmtTargetUpperVal(0.0);
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());

			rstList.add(tempPd);
		}
		else {
			
		}
		logger.info("Job 73 (KICS Applied Inflation) create {} result.  They are inserted into EAS_BIZ_APPL_INFLATION Table ", rstList.size());
		return rstList;
	}
	
}
