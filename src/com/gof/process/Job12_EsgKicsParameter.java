package com.gof.process;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.dao.SwaptionVolDao;
import com.gof.entity.IrCurveHis;
import com.gof.entity.ParamCalcHis;
import com.gof.entity.SmithWilsonParam;
import com.gof.entity.SwaptionVol;
import com.gof.model.CIRParameter;
import com.gof.model.HullWhite2FactorParameter;
import com.gof.model.HullWhiteParameter;
import com.gof.model.VasicekParameter;
import com.gof.util.FinUtils;

/**
 *  <p> 금리모형에 적용할 평균회귀계수, 변동성을 추정하는 작업을 관리하는 클래스
 *  <p> IFRS 17 에서는 매개변수 적용요건이 구체적으로 제시되지 않으나 KICS 에서는 매개변수 적용 요건을 명시하고 있음.         
 *  <p> 금리 데이터가 존재하는 만기 구간에 대해서는 금리 데이터 이용하여 산출한 결과를 적용하며   
 *  <p> 최장 만기에 대해서는 이전 만기의 추정 결과를 이동 평균하여 적용함. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job12_EsgKicsParameter {
	
	private final static Logger logger = LoggerFactory.getLogger(Job12_EsgKicsParameter.class);
	
	/**
	 *  기준년월의 금리 데이터를 반영하여 산출한 매개변수
	 *  @param bssd 기준년월
	 *  @return  적용 매개변수 
	*/
	public static List<ParamCalcHis> createHwParamCalcHis(String bssd, double errorTolerance) {
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst = IrCurveHisDao.getKTBIrCurveHis(bssd);
		List<SwaptionVol> volRst = SwaptionVolDao.getSwaptionVol(bssd);
		
		logger.info("Swpation vol Size : {},{}", volRst.size());
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		List<SmithWilsonParam> swParamList = swParam.stream().filter(s->s.getCurCd().equals("KRW")).collect(Collectors.toList());
		double ufr = swParamList.isEmpty()? 0.045: swParamList.get(0).getUfr();
		double ufrt = swParamList.isEmpty()? 60: swParamList.get(0).getUfrT();
		
		HullWhiteParameter hullWhiteParameter = new HullWhiteParameter(curveRst, volRst, ufr, ufrt);
		rst = hullWhiteParameter.getKicsParamCalcHis(bssd, "4", errorTolerance);
		
		logger.info("Job11 (Historical Hull White Parameter for Kics) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	public static List<ParamCalcHis> createHwParamCalcHisAsync(String bssd, List<IrCurveHis> curveRst, List<SwaptionVol> volRst, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for Hw Kics :  Thread Name: {}", Thread.currentThread().getName());
		List<ParamCalcHis> rst ;
				
		HullWhiteParameter hullWhiteParameter = new HullWhiteParameter(curveRst, volRst, ufr, ufrt);
		rst = hullWhiteParameter.getKicsParamCalcHis(bssd, "4", errorTolerance);
		
		logger.info("Job11 (Historical Hull White Parameter for Kics) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
}