package com.gof.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.renjin.sexp.SEXP;
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
import com.gof.model.SmithWilsonModel;
import com.gof.model.VasicekParameter;

/**
 *  <p> �ݸ������� ������ ���ȸ�Ͱ��, �������� �����ϴ� �۾��� �����ϴ� Ŭ����
 *  <p> IFRS 17 ������ �Ű����� �������� ��ü������ ���õ��� ������ KICS ������ �Ű����� ���� ����� ����ϰ� ����.         
 *  <p> �ݸ� �����Ͱ� �����ϴ� ���� ������ ���ؼ��� �ݸ� ������ �̿��Ͽ� ������ ����� �����ϸ�   
 *  <p> ���� ���⿡ ���ؼ��� ���� ������ ���� ����� �̵� ����Ͽ� ������. 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job11_EsgParameter {
	
	private final static Logger logger = LoggerFactory.getLogger(Job11_EsgParameter.class);
	
	
	
	/**
	 *  ���س���� �ݸ� �����͸� �ݿ��Ͽ� ������ �Ű�����
	 *  @param bssd ���س��
	 *  @return  ���� �Ű����� 
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
		rst = hullWhiteParameter.getParamCalcHis(bssd, "4", errorTolerance);
		
		logger.info("Job11 (Historical Hull White Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	/**
	 *  ���� �ݸ� �����͸� �̿��� CIR ������ �Ű����� ����
	 *  @param bssd ���س��
	 *  @return  ���� �Ű����� 
	*/
	public static List<ParamCalcHis> createCirParamCalcHis(String bssd, double errorTolerance) {
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst= new ArrayList<IrCurveHis>();
//		List<IrCurveHis> curveRst = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, -36), "A100")
//												 .stream()
//												 .filter(s -> s.getMatCd().equals("M0003")).collect(Collectors.toList());;
//		
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		List<SmithWilsonParam> swParamList = swParam.stream().filter(s->s.getCurCd().equals("KRW")).collect(Collectors.toList());
		double ufr = swParamList.isEmpty()? 0.045: swParamList.get(0).getUfr();
		double ufrt = swParamList.isEmpty()? 60: swParamList.get(0).getUfrT();
		
		List<IrCurveHis> currentTermStructure = IrCurveHisDao.getKTBIrCurveHis(bssd);
		
		SmithWilsonModel rf = new SmithWilsonModel(currentTermStructure, ufr, ufrt);
		SEXP rfRst         = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix:
		IrCurveHis temp;
		
		for(int i =0; i< 1200; i++) {
			temp = new IrCurveHis();
			temp.setBaseDate(bssd);
			temp.setIrCurveId("A100");
			temp.setMatCd("M" + String.format("%04d", i+1));
			temp.setIntRate(rfRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			curveRst.add(temp);
		}
		
		CIRParameter cirParameter = new CIRParameter(curveRst);
		rst = cirParameter.getParamCalcHis(bssd, "5", errorTolerance);
		
		logger.info("Job11 (Historical CIR Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		return rst;
	}
	
	/**
	 *  ���� �ݸ� �����͸� �̿��� Vasicek ������ �Ű����� ����
	 *  @param bssd ���س��
	 *  @return  ���� �Ű����� 
	*/
	
	public static List<ParamCalcHis> createVasicekParamCalcHis(String bssd, double errorTolerance) {
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst= new ArrayList<IrCurveHis>();
//		List<IrCurveHis> curveRst = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, -12), "A100")
//												 .stream()
//												 .filter(s -> s.getMatCd().equals("M0003")).collect(Collectors.toList());;
		
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		List<SmithWilsonParam> swParamList = swParam.stream().filter(s->s.getCurCd().equals("KRW")).collect(Collectors.toList());
		double ufr = swParamList.isEmpty()? 0.045: swParamList.get(0).getUfr();
		double ufrt = swParamList.isEmpty()? 60: swParamList.get(0).getUfrT();
		
		List<IrCurveHis> currentTermStructure = IrCurveHisDao.getKTBIrCurveHis(bssd);
		
		SmithWilsonModel rf = new SmithWilsonModel(currentTermStructure, ufr, ufrt);
		SEXP rfRst         = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix:
		IrCurveHis temp;
		
		for(int i =0; i< 1200; i++) {
			temp = new IrCurveHis();
			temp.setBaseDate(bssd);
			temp.setIrCurveId("A100");
			temp.setMatCd("M" + String.format("%04d", i+1));
			temp.setIntRate(rfRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			curveRst.add(temp);
			
		}
		
		VasicekParameter vasicekParameter = new VasicekParameter(curveRst);
		rst = vasicekParameter.getParamCalcHis(bssd, "2", errorTolerance);
		
		logger.info("Job11 (Historical Vasicek Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}

	/**
	 *  ���س���� �ݸ� �����͸� �ݿ��Ͽ� ������ �Ű�����
	 *  @param bssd ���س��
	 *  @param errorTolerance ��������
	 *  @return  ���� �Ű����� 
	*/
	public static List<ParamCalcHis> createHw2FactorParamCalcHis(String bssd, double errorTolerance) {
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst = IrCurveHisDao.getKTBIrCurveHis(bssd);
//		List<SwaptionVol> volRst = SwaptionVolDao.getPrecedingSwaptionVol(bssd, -36);
		List<SwaptionVol> volRst = SwaptionVolDao.getSwaptionVol(bssd);
		
		List<SmithWilsonParam> swParam = DaoUtil.getEntities(SmithWilsonParam.class, new HashMap<String, Object>());
		List<SmithWilsonParam> swParamList = swParam.stream().filter(s->s.getCurCd().equals("KRW")).collect(Collectors.toList());
		double ufr = swParamList.isEmpty()? 0.045: swParamList.get(0).getUfr();
		double ufrt = swParamList.isEmpty()? 60: swParamList.get(0).getUfrT();
		
		HullWhite2FactorParameter hw2FactorParameter = new HullWhite2FactorParameter(curveRst, volRst, ufr, ufrt);
		rst = hw2FactorParameter.getParamCalcHis(bssd, "6", errorTolerance); 			// 6 : HW2 Model
		
		logger.info("Job11 (Historical HW 2 Factor Parameter) creates {} resutls. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	public static List<ParamCalcHis> createCirParamCalcHisAsync(String bssd, List<IrCurveHis> currentTermStructure, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for CIR :  Thread Name: {}", Thread.currentThread().getName());
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst= new ArrayList<IrCurveHis>();
		
		SmithWilsonModel rf = new SmithWilsonModel(currentTermStructure, ufr, ufrt);
		SEXP rfRst         = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix:
		IrCurveHis temp;
		
		for(int i =0; i< 1200; i++) {
			temp = new IrCurveHis();
			temp.setBaseDate(bssd);
			temp.setIrCurveId("A100");
			temp.setMatCd("M" + String.format("%04d", i+1));
			temp.setIntRate(rfRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			curveRst.add(temp);
		}
		
		CIRParameter cirParameter = new CIRParameter(curveRst);
		rst = cirParameter.getParamCalcHis(bssd, "5", errorTolerance);
		
		logger.info("Job11 (Historical CIR Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		return rst;
	}
	
	
	public static List<ParamCalcHis> createVasicekParamCalcHisAsync(String bssd, List<IrCurveHis> currentTermStructure, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for Vasicek :  Thread Name: {}", Thread.currentThread().getName());
		
		List<ParamCalcHis> rst ;
		List<IrCurveHis> curveRst= new ArrayList<IrCurveHis>();
		
		SmithWilsonModel rf = new SmithWilsonModel(currentTermStructure, ufr, ufrt);
		SEXP rfRst         = rf.getSmithWilsonSEXP(false).getElementAsSEXP(0);			// Spot:  [Time , Month_Seq, spot, spot_annu, df, fwd, fwd_annu] , Forward Matrix:
		IrCurveHis temp;
		
		for(int i =0; i< 1200; i++) {
			temp = new IrCurveHis();
			temp.setBaseDate(bssd);
			temp.setIrCurveId("A100");
			temp.setMatCd("M" + String.format("%04d", i+1));
			temp.setIntRate(rfRst.getElementAsSEXP(6).getElementAsSEXP(i).asReal());
			curveRst.add(temp);
			
		}
		
		VasicekParameter vasicekParameter = new VasicekParameter(curveRst);
		rst = vasicekParameter.getParamCalcHis(bssd, "2", errorTolerance);
		
		logger.info("Job11 (Historical Vasicek Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	public static List<ParamCalcHis> createVasicekParamCalcHisAsync1(String bssd, List<IrCurveHis> shortRateTimeSeries, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for Vasicek :  Thread Name: {}", Thread.currentThread().getName());
		
		List<ParamCalcHis> rst ;

		VasicekParameter vasicekParameter = new VasicekParameter(shortRateTimeSeries);
		rst = vasicekParameter.getParamCalcHis(bssd, "2", errorTolerance);
		
		logger.info("Job11 (Historical Vasicek Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	public static List<ParamCalcHis> createHwParamCalcHisAsync(String bssd, List<IrCurveHis> curveRst , List<SwaptionVol> volRst, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for HW :  Thread Name: {}", Thread.currentThread().getName());
		List<ParamCalcHis> rst ;
		HullWhiteParameter hullWhiteParameter = new HullWhiteParameter(curveRst, volRst, ufr, ufrt);
		rst = hullWhiteParameter.getParamCalcHis(bssd, "4", errorTolerance);
		
		logger.info("Job11 (Historical Hull White Parameter) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	public static List<ParamCalcHis> createHw2FactorParamCalcHisAsync(String bssd, List<IrCurveHis> curveRst, List<SwaptionVol> volRst, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for HW2 :  Thread Name: {}", Thread.currentThread().getName());
		List<ParamCalcHis> rst ;
		
		HullWhite2FactorParameter hw2FactorParameter = new HullWhite2FactorParameter(curveRst, volRst, ufr, ufrt);
		rst = hw2FactorParameter.getParamCalcHis(bssd, "6", errorTolerance); 			// 6 : HW2 Model
		
		logger.info("Job11 (Historical HW 2 Factor Parameter) creates {} resutls. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}
	
	public static List<ParamCalcHis> createHwKicsParamCalcHis(String bssd, double errorTolerance) {
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
	
	public static List<ParamCalcHis> createHwKicsParamCalcHisAsync(String bssd, List<IrCurveHis> curveRst, List<SwaptionVol> volRst, double ufr, double ufrt, double errorTolerance) {
		logger.info("ESG Parameter for Hw Kics :  Thread Name: {}", Thread.currentThread().getName());
		List<ParamCalcHis> rst ;
				
		HullWhiteParameter hullWhiteParameter = new HullWhiteParameter(curveRst, volRst, ufr, ufrt);
		rst = hullWhiteParameter.getKicsParamCalcHis(bssd, "4", errorTolerance);
		
		logger.info("Job11 (Historical Hull White Parameter for Kics) creates {} results. They are inserted into EAS_PARAM_CALC_HIS Table", rst.size());
		
		return rst;
	}

}