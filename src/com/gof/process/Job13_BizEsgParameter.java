package com.gof.process;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.EsgMstDao;
import com.gof.dao.EsgParamDao;
import com.gof.entity.BizEsgParam;
import com.gof.entity.BizEsgParamUd;
import com.gof.entity.EsgMst;
import com.gof.entity.ParamCalcHis;
import com.gof.enums.EBoolean;
import com.gof.util.ParamUtil;

/**
 *  <p> �ݸ������� ������ ���ȸ�Ͱ��, �������� �����ϴ� �۾��� �����ϴ� Ŭ����
 *  <p> IFRS 17 ������ �Ű����� �������� ��ü������ ���õ��� ������ KICS ������ �Ű����� ���� ����� ����ϰ� ����.         
 *  <p> �ݸ� �����Ͱ� �����ϴ� ���� ������ ���ؼ��� �ݸ� ������ �̿��Ͽ� ������ ����� �����ϸ�   
 *  <p> ���� ���⿡ ���ؼ��� ���� ������ ���� ����� �̵� ����Ͽ� ������. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job13_BizEsgParameter {
	
	private final static Logger logger = LoggerFactory.getLogger(Job13_BizEsgParameter.class);
	
	/**
	 *  ESG ������ ���������� �Ű������� ������.
	 *  <p> ����ڰ� �Է��� �Ű������� �켱������ �����ϸ�, �Է��� �Ű������� �������� ���� ��� ESG Engine �� KICS �� ����п� ���� ������ �Ű������� ������. 
	 *  @param bssd ���س��
	 *  @return  ���� �Ű����� 
	*/
	
	public static List<BizEsgParam> createBizAppliedParameter(String bssd) {
		List<BizEsgParam> bizApplied = new ArrayList<BizEsgParam>();
		bizApplied.addAll(createBizAppliedParameter(bssd, "I"));
		bizApplied.addAll(createBizAppliedParameter(bssd, "K"));
		
		return bizApplied;
	}
	
	public static List<BizEsgParam> createBizAppliedParameter(String bssd, String bizDv) {
		List<BizEsgParam> bizApplied = new ArrayList<BizEsgParam>();
		List<BizEsgParamUd> userParam = EsgParamDao.getBizEsgParamUdByBiz(bssd, bizDv);
//		List<BizEsgParamUd> kicsUserParam = EsgParamDao.getBizEsgParamUd(bssd, "K");
		
		if(userParam.isEmpty()) {
			bizApplied =calculateBizAppliedParameter(bssd, bizDv);
			logger.info("Job13 (Setting Biz Applied Parameter ) : Biz Applied parameter is used with calucated parameters");
		}
		
		else {
			bizApplied = userParam.stream().map(s -> s.convertToBizEsgParam(bssd)).collect(Collectors.toList());
			logger.info("Job13 (Setting Biz Applied Parameter ) : Biz Applied parameter is used with user Defined parameters");
		}

		logger.info("Job13 (Setting Biz Applied Parameter ) creates {} resutls. They are inserted into EAS_BIZ_APLY_PARAM Table", bizApplied.size());
		
		return bizApplied;
		
	}
	public static List<BizEsgParam> createBizAppliedParameterZ(String bssd ) {
		List<BizEsgParam> bizApplied = new ArrayList<BizEsgParam>();
		List<BizEsgParamUd> userParam = EsgParamDao.getBizEsgParamUd(bssd);
		
		if(userParam.isEmpty()) {
			bizApplied =calculateBizAppliedParameter(bssd, "I");
			logger.info("Job13 (Setting Biz Applied Parameter ) : Biz Applied parameter is used with calucated parameters");
		}
		
		else {
			bizApplied = userParam.stream().map(s -> s.convertToBizEsgParam(bssd)).collect(Collectors.toList());
			logger.info("Job13 (Setting Biz Applied Parameter ) : Biz Applied parameter is used with user Defined parameters");
		}

		logger.info("Job13 (Setting Biz Applied Parameter ) creates {} resutls. They are inserted into EAS_BIZ_APLY_PARAM Table", bizApplied.size());
		
		return bizApplied;
	}
	
	private static List<BizEsgParam> calculateBizAppliedParameter(String bssd, String bizDv ) {
		List<BizEsgParam> bizApplyRst = new ArrayList<BizEsgParam>();
		BizEsgParam temp;
		
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);			
		
		Map<String, Object> param1 = new HashMap<String, Object>();
		
		for(EsgMst aa : esgMstList) {
			param1.put("baseYymm", bssd);
			param1.put("irModelTyp", aa.getIrModedType());
			param1.put("paramCalcCd", aa.getParamApplCd());
			
			List<ParamCalcHis> paramHisRst = DaoUtil.getEntities(ParamCalcHis.class, param1);
			logger.debug("applied : {}", paramHisRst.size());
			
			for(ParamCalcHis bb : paramHisRst) {
				temp = new BizEsgParam();
				temp.setBaseYymm(bssd);
				temp.setApplyBizDv(bizDv);
				temp.setIrModelId(aa.getIrModelId());
				temp.setParamTypCd(bb.getParamTypCd());
				temp.setMatCd(bb.getMatCd());
				temp.setApplParamVal(bb.getParamVal());
				temp.setVol(0.0);
				temp.setLastModifiedBy("ESG");
				temp.setLastUpdateDate(LocalDateTime.now());
				bizApplyRst.add(temp);
			}
			
			int esgParamAlphaOuterAvgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("esgParamAlphaOuterAvgNum", "-36")); 
			String esgParamAlphaOuterMatCd  = ParamUtil.getParamMap().getOrDefault("esgParamAlphaOuterMatCd", "M0240");
			
			int esgParamSigmaOuterAvgNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("esgParamSigmaOuterAvgNum", "-36"));
			String esgParamSigmaOuterMatCd  = ParamUtil.getParamMap().getOrDefault("esgParamSigmaOuterMatCd", "M0120");
					 
			if(aa.getIrModedType().equals("4")) {
				bizApplyRst.addAll(createBizAppliedParameterOuter(bssd, bizDv, esgParamAlphaOuterAvgNum, "ALPHA", esgParamAlphaOuterMatCd))	;
				bizApplyRst.addAll(createBizAppliedParameterOuter(bssd, bizDv, esgParamSigmaOuterAvgNum, "SIGMA", esgParamSigmaOuterMatCd))	;
			}
		}
		return bizApplyRst;
	}
	
	
	
	/**
	 *  ���� ���⿡ ���� ESG �Ű����� ���� ��� �ݿ�
	 *  @param bssd ���س��
	 *  @param monthNum ���� �̵���� ����
	 *  @param paramType �Ű����� ����
	 *  @param matCd �����ڵ� 
	 *  @return  ���� �Ű����� 
	*/
	
	private static List<BizEsgParam> createBizAppliedParameterOuter(String bssd , String bizDv, int monthNum, String paramType, String matCd) {
		List<BizEsgParam> bizApplyRst = new ArrayList<BizEsgParam>();
		BizEsgParam temp;
		
//		Map<String, Object> param = new HashMap<String, Object>();
//		param.put("useYn", EBoolean.Y);
//		List<EsgMst> esgMstList = DaoUtil.getEntities(EsgMst.class, param);
		
		List<EsgMst> esgMstList = EsgMstDao.getEsgMst(EBoolean.Y);		
		
		for(EsgMst aa : esgMstList) {
			List<ParamCalcHis> paramHisRst = EsgParamDao.getParamCalHis(bssd, monthNum, paramType, matCd);
//			logger.info("applied Outer: {}", paramHisRst.size());
			
			temp = new BizEsgParam();
			temp.setBaseYymm(bssd);
			temp.setApplyBizDv(bizDv);
			temp.setIrModelId(aa.getIrModelId());
			temp.setParamTypCd(paramType);
			temp.setMatCd("M1200");
			temp.setApplParamVal(paramHisRst.stream().collect(Collectors.averagingDouble(s ->s.getParamVal())));
			temp.setVol(0.0);
			temp.setLastModifiedBy("ESG");
			temp.setLastUpdateDate(LocalDateTime.now());
			bizApplyRst.add(temp);
		}
		return bizApplyRst;
	}

}