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
import com.gof.dao.SegLgdDao;
import com.gof.dao.SegPrepayDao;
import com.gof.entity.BizCorpPd;
import com.gof.entity.BizCrdSpread;
import com.gof.entity.BizDiscRate;
import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateUd;
import com.gof.entity.BizIndiPd;
import com.gof.entity.BizSegLgd;
import com.gof.entity.BizSegPrepay;
import com.gof.entity.BizSegPrepayUd;
import com.gof.entity.BottomupDcnt;
import com.gof.entity.CorpCumPd;
import com.gof.entity.CreditSpread;
import com.gof.entity.DiscRate;
import com.gof.entity.IndiCrdGrdCumPd;
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
public class Job71_KicsBizApplyDcnt {
	private final static Logger logger = LoggerFactory.getLogger(Job71_KicsBizApplyDcnt.class);

	
	/*public static List<BizDiscountRate> getBizDcntRateKics(String bssd) {
		List<BizDiscountRate> rstList = new ArrayList<BizDiscountRate>();
		BizDiscountRate tempIr;
		
		List<BizDiscountRateUd> kicsDcntUd = BottomupDcntDao.getBizDiscountRateUd(bssd, "K");
		List<BottomupDcnt> bottomUpList = BottomupDcntDao.getTermStructure(bssd, "RF_KRW_KICS");
		
		if(!kicsDcntUd.isEmpty()) {
			rstList.addAll(kicsDcntUd.stream().map(s -> s.convertToBizDiscountRate()).collect(Collectors.toList()));
			logger.info("Job 71 (KICS Applied Discount Rate) is used with UserInput {}", rstList.size());
		}
		else {
			for (BottomupDcnt aa : bottomUpList) {
				tempIr = new BizDiscountRate();
				
				tempIr.setBaseYymm(aa.getBaseYymm());
				tempIr.setApplyBizDv("K");
				tempIr.setIrCurveId(aa.getIrCurveId());
				tempIr.setMatCd(aa.getMatCd());
				tempIr.setRfRate(aa.getRfRate());
				tempIr.setLiqPrem(aa.getLiqPrem());
				
				tempIr.setRefYield(0.0);
				tempIr.setCrdSpread(0.0);
				
				tempIr.setRiskAdjRfRate(aa.getRiskAdjRfRate());
				tempIr.setRiskAdjRfFwdRate(aa.getRiskAdjRfFwdRate());
				
				tempIr.setVol(0.0);
				
				tempIr.setLastModifiedBy("ESG");
				tempIr.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempIr);
			}
		}
		
		logger.info("Job 71 (KICS Applied Discount Rate) create {} result.  They are inserted into EAS_BIZ_APLY_APPL_DCNT Table", rstList.size());
		return rstList;
	}*/
	public static List<BizDiscountRate> getBizDcntRateKics(String bssd) {
//		return getBizDcntRateKics(bssd, "KRW", "K");
		
		List<BizDiscountRate> rst = new ArrayList<BizDiscountRate>();
		
		rst.addAll(	getBizDcntRateKics(bssd, "KRW", "K"))	;
		rst.addAll(	getBizDcntRateKics(bssd, "USD", "K"))	;
		
		return rst;
	}
	
	public static List<BizDiscountRate> getBizDcntRateKics(String bssd, String curCd, String bizDv) {
		List<BizDiscountRate> rstList = new ArrayList<BizDiscountRate>();
		BizDiscountRate tempIr;
		
		List<BizDiscountRateUd> kicsDcntUd = BottomupDcntDao.getBizDiscountRateUd(bssd, curCd, bizDv);
		List<BottomupDcnt> bottomUpList = BottomupDcntDao.getTermStructure(bssd, "RF_"+curCd+"_KICS");
		
		if(!kicsDcntUd.isEmpty()) {
			rstList.addAll(kicsDcntUd.stream().map(s -> s.convertToBizDiscountRate()).collect(Collectors.toList()));
			logger.info("Job 71 (KICS Applied Discount Rate) is used with UserInput {}", rstList.size());
		}
		else {
			for (BottomupDcnt aa : bottomUpList) {
				tempIr = new BizDiscountRate();
				
				tempIr.setBaseYymm(aa.getBaseYymm());
				tempIr.setApplyBizDv("K");
				tempIr.setIrCurveId(aa.getIrCurveId());
				tempIr.setMatCd(aa.getMatCd());
				tempIr.setRfRate(aa.getRfRate());
				tempIr.setLiqPrem(aa.getLiqPrem());
				
				tempIr.setRefYield(0.0);
				tempIr.setCrdSpread(0.0);
				
				tempIr.setRiskAdjRfRate(aa.getRiskAdjRfRate());
				tempIr.setRiskAdjRfFwdRate(aa.getRiskAdjRfFwdRate());
				
				tempIr.setVol(0.0);
				
				tempIr.setLastModifiedBy("ESG");
				tempIr.setLastUpdateDate(LocalDateTime.now());
				
				rstList.add(tempIr);
			}
		}
		
		logger.info("Job 71 (KICS Applied Discount Rate for  {}) create {} result.  They are inserted into EAS_BIZ_APLY_APPL_DCNT Table", curCd, rstList.size());
		return rstList;
	}
	
}
