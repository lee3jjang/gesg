package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.CorpCrdGrdPdDao;
import com.gof.dao.DaoUtil;
import com.gof.dao.InflationDao;
import com.gof.dao.SegLgdDao;
import com.gof.entity.BizCorpPd;
import com.gof.entity.BizCrdSpread;
import com.gof.entity.BizInflation;
import com.gof.entity.BizSegLgd;
import com.gof.entity.CorpCumPd;
import com.gof.entity.CreditSpread;
import com.gof.entity.Inflation;
import com.gof.entity.InflationUd;
import com.gof.entity.SegLgd;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;

/**
 *  <p> 경험 통계 정보를 IFRS17 용도로 적용하기 위해 업무목적별 활용 테이블로 이관함. 
 *  <p> 유동성 프리미엄, 할인율, 공시이율, 기업부도율, 손실률, 신용스프레드, 인플레이션 등이 대상임.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job63_IFRSBizApplyRC {
	private final static Logger logger = LoggerFactory.getLogger(Job63_IFRSBizApplyRC.class);

	public static List<BizCorpPd> getBizCorpPdFromCumPd(String bssd) {
		List<BizCorpPd> rstList = new ArrayList<BizCorpPd>();
		BizCorpPd tempPd;
		
		List<CorpCumPd> corpPdList = CorpCrdGrdPdDao.getCorpCumPd(bssd);
		
		for (CorpCumPd aa : corpPdList) {
			tempPd = new BizCorpPd();

			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("I");
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
		logger.info("Job 63 (IFRS17 Applied Cumulative Corporate Pd ) create {} result.  They are inserted into EAS_BIZ_APPL_CORP_PD Table ", rstList.size());
		return rstList;
	}

	public static List<BizSegLgd> getBizSegLgd(String bssd) {
		List<BizSegLgd> rstList = new ArrayList<BizSegLgd>();
		BizSegLgd tempPd;
		
		List<SegLgd> lgdList = SegLgdDao.getSegLgd(bssd);   //감독원 제공 LGD
		
		for (SegLgd aa : lgdList) {
			tempPd = new BizSegLgd();

			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("I");
			tempPd.setSegId(aa.getSegId());
			tempPd.setLgdCalcTypCd(aa.getLgdCalcTypCd());
			
			tempPd.setApplyLgd(aa.getLgd());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempPd);
			
		}
		logger.info("Job 63 (IFRS17 Applied Segment LGD ) create {} result.  They are inserted into EAS_BIZ_APPL_SEG_LGD Table ", rstList.size());
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
			tempPd.setApplyBizDv("I");
			
			tempPd.setCrdGrdCd(aa.getCrdGrdCd());
			tempPd.setMatCd(aa.getMatCd());
			
			tempPd.setApplyCrdSpread(aa.getCrdSpread());
			tempPd.setVol(0.0);
			
			tempPd.setLastModifiedBy("ESG");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			rstList.add(tempPd);
		}
		
		logger.info("Job 63 (IFRS17 Applied Credit Spread) create {} result.  They are inserted into EAS_BIZ_APLY_CRD_SPREAD Table ", rstList.size());
		return rstList;
	}

//	public static List<BizInflation> getBizInflation(String bssd) {
//		List<BizInflation> rstList = new ArrayList<BizInflation>();
//		BizInflation tempPd =new BizInflation();
//		
//		int inflationAvgMonNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
//		
//		List<Inflation> infList = InflationDao.getPrecedingInflation(bssd, inflationAvgMonNum);          //과거 3년 inflation 증가율을 평균하여 적용함.
//		
//		double avgInflation =0.0;
//		for (Inflation aa : infList) {
//			avgInflation = avgInflation + aa.getInflation();
//			tempPd = new BizInflation();
//			
//			tempPd.setBaseYymm(bssd);
//			tempPd.setApplyBizDv("I");
//			tempPd.setInflationId(aa.getInflationId());
//			tempPd.setInflationIndex(aa.getInflationIndex());
//			
//			tempPd.setInflation(avgInflation/infList.size());
//			
//			tempPd.setMgmtTargetLowerVal(aa.getMgmtTargetLowerVal());
//			tempPd.setMgmtTargetUpperVal(aa.getMgmtTargetUpperVal());
//			tempPd.setVol(0.0);
//			
//			tempPd.setLastModifiedBy("ESG");
//			tempPd.setLastUpdateDate(LocalDateTime.now());
//		}
//		
//		rstList.add(tempPd);
//		logger.info("Job 63 (IFRS17 Applied Inflation) create {} result.  They are inserted into EAS_BIZ_APPL_INFLATION Table ", rstList.size());
//		return rstList;
//	}
	
	
	public static List<BizInflation> getBizInflationMA(String bssd) {
		List<BizInflation> rstList = new ArrayList<BizInflation>();
		BizInflation tempPd =new BizInflation();
		
		int avgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("inflationAvgMonNum", "-36"));
		
		List<InflationUd> infUdList = InflationDao.getPrecedingInflationUd(bssd, 0);
		
		String maxSettingYymm = InflationDao.getMaxSettingYymm(bssd);
		List<Inflation> infList = InflationDao.getPrecedingInflation(bssd, avgNum).stream().filter(s->s.getSettingYymm().equals(maxSettingYymm)).collect(Collectors.toList()); 
		
		tempPd = new BizInflation();
		tempPd.setBaseYymm(bssd);
		tempPd.setApplyBizDv("I");

		double avgSum =0.0;
		int   avgCnt =0;
		if( infUdList.size() > 0 && infUdList.get(0).getIfrsTgtIndex() != null && infUdList.get(0).getIfrsTgtIndex() != 0.0  ) { 
			InflationUd infUd = infUdList.get(0);
			
			tempPd = new BizInflation();
			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("I");
			tempPd.setInflationId("TGT");
//			tempPd.setInflationIndex(infUd.getIfrsTgtIndex());   
			tempPd.setInflation(infUd.getIfrsTgtIndex());			//TODO : 테이블 칼럼 추가
			
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
				
			}
//			logger.info("zzz1 : {},{},{},{},{}", bssd, avgCnt, avgSum);
			
			Inflation inf = infList.get(0);
			
			tempPd = new BizInflation();
			tempPd.setBaseYymm(bssd);
			tempPd.setApplyBizDv("I");
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
		rstList.forEach(s-> logger.info("zzzz : {},{},{}", s.toString()) );
		logger.info("Job 63 (IFRS17 Applied Inflation) create {} result.  They are inserted into EAS_BIZ_APPL_INFLATION Table ", rstList.size());
		return rstList;
	}
}
