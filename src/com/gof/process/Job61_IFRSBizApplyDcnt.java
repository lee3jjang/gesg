package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.BottomupDcntDao;
import com.gof.entity.BizDiscountRate;
import com.gof.entity.BizDiscountRateUd;
import com.gof.entity.BottomupDcnt;

/**
 *  <p> ���� ��� ������ IFRS17 �뵵�� �����ϱ� ���� ���������� Ȱ�� ���̺�� �̰���. 
 *  <p> ������ �����̾�, ������, ��������, ����ε���, �սǷ�, �ſ뽺������, ���÷��̼� ���� �����.
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job61_IFRSBizApplyDcnt {
	private final static Logger logger = LoggerFactory.getLogger(Job61_IFRSBizApplyDcnt.class);

	/*public static List<BizDiscountRate> getBizDcntRate(String bssd) {
		List<BizDiscountRate> rstList = new ArrayList<BizDiscountRate>();
		BizDiscountRate tempIr;
		
		List<BizDiscountRateUd> dcntRateUd = BottomupDcntDao.getBizDiscountRateUd(bssd, "I");
		List<BottomupDcnt> bottomUpList = BottomupDcntDao.getTermStructure(bssd, "RF_KRW_BU");
		
		if(!dcntRateUd.isEmpty()) {
			rstList.addAll(dcntRateUd.stream().map(s -> s.convertToBizDiscountRate()).collect(Collectors.toList()));
		}
		else {
			for (BottomupDcnt aa : bottomUpList) {
				tempIr = new BizDiscountRate();
	
				tempIr.setBaseYymm(aa.getBaseYymm());
				tempIr.setApplyBizDv("I");
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
		logger.info("Job 61 (IFRS17 Applied Discount Rate) create {} result.  They are inserted into EAS_BIZ_APLY_APPL_DCNT Table", rstList.size());
		return rstList;
	}*/
	
	public static List<BizDiscountRate> getBizDcntRate(String bssd) {
		List<BizDiscountRate> rst = new ArrayList<BizDiscountRate>();
		
		rst.addAll(	getBizDcntRate(bssd, "KRW", "I"))	;
		rst.addAll(	getBizDcntRate(bssd, "USD", "I"))	;
		
		return rst;
	}
	
	public static List<BizDiscountRate> getBizDcntRate(String bssd, String curCd, String bizDv) {
		List<BizDiscountRate> rstList = new ArrayList<BizDiscountRate>();
		BizDiscountRate tempIr;
		
		List<BizDiscountRateUd> dcntRateUd = BottomupDcntDao.getBizDiscountRateUd(bssd, curCd, bizDv);
		List<BottomupDcnt> bottomUpList = BottomupDcntDao.getTermStructure(bssd, "RF_"+curCd+"_BU");
		
		if(!dcntRateUd.isEmpty()) {
			rstList.addAll(dcntRateUd.stream().map(s -> s.convertToBizDiscountRate()).collect(Collectors.toList()));
		}
		else {
			for (BottomupDcnt aa : bottomUpList) {
				tempIr = new BizDiscountRate();
	
				tempIr.setBaseYymm(aa.getBaseYymm());
				tempIr.setApplyBizDv("I");
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
		logger.info("Job 61 (IFRS17 Applied Discount Rate for {}) create {} result.  They are inserted into EAS_BIZ_APLY_APPL_DCNT Table", curCd, rstList.size());
		return rstList;
	}
}
