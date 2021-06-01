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
 *  <p> 금리모형에 적용할 평균회귀계수, 변동성을 추정하는 작업을 관리하는 클래스
 *  <p> IFRS 17 에서는 매개변수 적용요건이 구체적으로 제시되지 않으나 KICS 에서는 매개변수 적용 요건을 명시하고 있음.         
 *  <p> 금리 데이터가 존재하는 만기 구간에 대해서는 금리 데이터 이용하여 산출한 결과를 적용하며   
 *  <p> 최장 만기에 대해서는 이전 만기의 추정 결과를 이동 평균하여 적용함. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job13_BizEsgParameter {
	
	private final static Logger logger = LoggerFactory.getLogger(Job13_BizEsgParameter.class);
	
	/**
	 *  ESG 모형의 최종적으로 매개변수를 산출함.
	 *  <p> 사용자가 입력한 매개변수를 우선적으로 적용하며, 입력한 매개변수가 존재하지 않을 경우 ESG Engine 이 KICS 의 방법론에 따라 생성한 매개변수를 적용함. 
	 *  @param bssd 기준년월
	 *  @return  적용 매개변수 
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
	 *  최장 만기에 대한 ESG 매개변수 적용 요건 반영
	 *  @param bssd 기준년월
	 *  @param monthNum 과거 이동평균 월수
	 *  @param paramType 매개변수 유형
	 *  @param matCd 만기코드 
	 *  @return  적용 매개변수 
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