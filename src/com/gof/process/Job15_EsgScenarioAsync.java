package com.gof.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizEsgParam;
import com.gof.entity.DcntSce;
import com.gof.entity.EsgMst;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.enums.EIrModelType;
import com.gof.model.CIR;
import com.gof.model.HullWhite;
import com.gof.model.HullWhite2Factor;
import com.gof.model.HullWhiteNew;
import com.gof.model.Vasicek;

/**
 *  <p> ESG ������ �ݸ�/���庯�� �ó������� �����۾� ������.       
 *  <p> Hull White 1Factor/ 2Factor ����, CIR, Vasicek �� �Ϲ����� �ݸ� �ó����� ������ �����ϰ� ����.
 *  <p> �ֽ� ���� ���庯���� ���ؼ��� �⺻���� Brownian ����, Black ���� ������ �ó������� ������.
 *  <p> �ֽ� ���� �Ϲݽ��庯���� ��� ������ ������ Random Number �� ESG ������ �Է��Ͽ� �ó������� �����ϴ� 2�ܰԷ� ������
 *  <p> �ݸ� �ó������� �ݸ� �Ⱓ ���� Fitting �ܰ谡 �߰���.  
 *  <p>  1. �ݸ� ������ ��� ���� 
 *  <p>  2. �ݸ����� ,  �Ű����� ,  Random Number �� �������� Short Rate �� �ó����� ����
 *  <p>  3. �ݸ� �Ⱓ ���� Fitting �������� Bucket �� �ݸ� ����� ��ü �ó����� ���� 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job15_EsgScenarioAsync {
	private final static Logger logger = LoggerFactory.getLogger(Job15_EsgScenarioAsync.class);
	
	
	public static List<IrSce> createEsgScenarioAsync(String bssd,  String irCurveId, List<IrCurveHis> irCurveHisList, List<BizEsgParam> esgParam, EsgMst esgMst, double ufr, double ufrt,  double shortRate, int batchNo) {
		
		logger.info("IR Scenario for {} Batch No and Thread Name: {}, {}", irCurveHisList.get(0).getIrCurveId(),batchNo, Thread.currentThread().getName());
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		String baseYymm = bssd + String.format("%02d", batchNo);   //�������ڷ� �õ� �ѹ��� �����ϰ� ����. batchNo �� ���� �ٸ� �õ带 �ֱ����� ��¥�� ������.
		
			switch (EIrModelType.getEIrModelType(esgMst.getIrModedType())) {
				case MERTON :
						break;
					
				case VASICEK:	
						Vasicek vasicek = new Vasicek(baseYymm, shortRate, esgParam);
						irScenarioList.addAll(vasicek.getVasicekScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HOLEE:		
						
						break;
					
				case HULLWHITE:	
						
						HullWhite hullWhite = new HullWhite(baseYymm, irCurveHisList, esgParam, ufr, ufrt);
//						irScenarioList.addAll(hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						return  hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo);
//						break;
						
				case CIR:		
						CIR cir = new CIR(baseYymm, shortRate, esgParam);
						irScenarioList.addAll(cir.getCirScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HW2:		 
						HullWhite2Factor hw2 = new HullWhite2Factor(baseYymm, irCurveHisList, esgParam, ufr, ufrt);
						irScenarioList.addAll(hw2.getHW2Scenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				default:
						
						break;
			}
//		}
			
		return irScenarioList;
	}
	
	
	public static List<DcntSce> createDcntScenarioAsync(String bssd,  String irCurveId, List<IrCurveHis> irCurveHisList, List<BizEsgParam> esgParam, EsgMst esgMst, double ufr, double ufrt,  double shortRate, int batchNo) {
		
		logger.info("IR Scenario for {} Batch No and Thread Name: {}, {}", irCurveHisList.get(0).getIrCurveId(),batchNo, Thread.currentThread().getName());
		List<DcntSce> irScenarioList = new ArrayList<DcntSce>();
		String baseYymm = bssd + String.format("%02d", batchNo);   //�������ڷ� �õ� �ѹ��� �����ϰ� ����. batchNo �� ���� �ٸ� �õ带 �ֱ����� ��¥�� ������.
		
			switch (EIrModelType.getEIrModelType(esgMst.getIrModedType())) {
				case MERTON :
						break;
					
				case VASICEK:	
						break;
						
				case HOLEE:		
						
						break;
					
				case HULLWHITE:	
						
					HullWhiteNew hullWhite = new HullWhiteNew(baseYymm, irCurveHisList, esgParam, ufr, ufrt);
//						irScenarioList.addAll(hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						return  hullWhite.getBottomUpScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo);
//						break;
						
				case CIR:		
						break;
						
				case HW2:		 
						break;
						
				default:
						
						break;
			}
//		}
			
		return irScenarioList;
	}

	/*public static List<IrSce> createHwScenario(String bssd,  IrCurve irCurve , int batchNo) {
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		
		List<IrCurveHis> irCurveHisList = IrCurveHisDao.getIrCurveHis(bssd, irCurve.getIrCurveId());
		
		if(irCurveHisList.size() == 0) {
			logger.warn("IR Curve History of {} Data is not found at {}", irCurve.getIrCurveId(), bssd);
			return irScenarioList;
		}
		String irCurveId = irCurve.getIrCurveId();
		String curCd     = irCurve.getCurCd();
		
		List<EsgMst> esgMstList = EsgMstDao.getEsgMstWithParam(bssd, EBoolean.Y);			//TODO : irCurveId �� filtering ó�� ���???
		
		Stream<SmithWilsonParam> swStream        = DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
		Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
		
		double ufr  =  swParamMap.get(curCd).getUfr();
		double ufrt =  swParamMap.get(curCd).getUfrT();
		
//		logger.info("Setting for IR Scenario {} : size of Mat Date  : {} , siz of Param : {}", irCurveId, irCurveHisList.size(), tempEsgMst.getParamApply().size());
		EsgMst tempEsgMst = esgMstList.get(0);
//		for(EsgMst tempEsgMst : EsgMstList) {
			switch (EIrModelType.getEIrModelType(tempEsgMst.getIrModedType())) {
				case MERTON :
		
						break;
					
				case VASICEK:	
						Vasicek vasicek = new Vasicek(bssd + "_" + batchNo, irCurveHisList, tempEsgMst.getParamApply(), ufr, ufrt);
						irScenarioList.addAll(vasicek.getVasicekScenario(bssd, irCurveId, tempEsgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HOLEE:		
						
						break;
					
				case HULLWHITE:	
						HullWhite hullWhite = new HullWhite(bssd + "_" + batchNo, irCurveHisList, tempEsgMst.getParamApply(), ufr, ufrt);
						irScenarioList.addAll(hullWhite.getHullWhiteScenario(bssd, irCurveId, tempEsgMst.getIrModelId(), 100, batchNo));			
						break;
						
				case CIR:		
						CIR cir = new CIR(bssd + "_" + batchNo, irCurveHisList, tempEsgMst.getParamApply(), ufr, ufrt);
						irScenarioList.addAll(cir.getCirScenario(bssd, irCurveId, tempEsgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HW2:		 
						HullWhite2Factor hw2 = new HullWhite2Factor(bssd + "_" + batchNo, irCurveHisList, tempEsgMst.getParamApply(), ufr, ufrt);
						irScenarioList.addAll(hw2.getHW2Scenario(bssd, irCurveId, tempEsgMst.getIrModelId(), 100, batchNo));
						break;
						
				default:
						
						break;
			}
//		}
			
		return irScenarioList;
	}*/
	
	
}
