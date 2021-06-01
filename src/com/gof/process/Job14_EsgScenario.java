package com.gof.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Job14_EsgScenario {
	private final static Logger logger = LoggerFactory.getLogger(Job14_EsgScenario.class);
	
	/*public static List<IrSce> createEsgScenario(String bssd,  List<IrCurveHis> irCurveHisList, EsgMst esgMst, double ufr, double ufrt,  int batchNo) {
		logger.info("IR Scenario Thread and Batch No : {}, {}", batchNo, Thread.currentThread().getName());
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		String irCurveId ="";
		double shortRate =0.01;
		String baseYymm = bssd + String.format("%02d", batchNo);
		
			switch (EIrModelType.getEIrModelType(esgMst.getIrModedType())) {
				case MERTON :
						break;
					
				case VASICEK:	
						Vasicek vasicek = new Vasicek(baseYymm, shortRate, esgMst.getBizApplyParam());
						irScenarioList.addAll(vasicek.getVasicekScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HOLEE:		
						
						break;
					
				case HULLWHITE:	
						HullWhite hullWhite = new HullWhite(baseYymm, irCurveHisList, esgMst.getBizApplyParam(), ufr, ufrt);
//						irScenarioList.addAll(hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						return  hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 1000, batchNo);
//						break;
						
				case CIR:		
						CIR cir = new CIR(baseYymm, shortRate, esgMst.getBizApplyParam());
						irScenarioList.addAll(cir.getCirScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HW2:		 
//						HullWhite2Factor hw2 = new HullWhite2Factor(bssd + "_" + batchNo, irCurveHisList, esgMst.getParamApply(), ufr, ufrt);
						HullWhite2Factor hw2 = new HullWhite2Factor(baseYymm, irCurveHisList, esgMst.getBizApplyParam(), ufr, ufrt);
						irScenarioList.addAll(hw2.getHW2Scenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				default:
						
						break;
			}
//		}
			
		return irScenarioList;
	}
	
	public static List<IrSce> createEsgScenarioAsync(String bssd,  String irCurveId, List<IrCurveHis> irCurveHisList, EsgMst esgMst, double ufr, double ufrt,  double shortRate, int batchNo) {
		
		logger.info("IR Scenario for {} Batch No and Thread Name: {}, {}", irCurveHisList.get(0).getIrCurveId(),batchNo, Thread.currentThread().getName());
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		String baseYymm = bssd + String.format("%02d", batchNo);   //�������ڷ� �õ� �ѹ��� �����ϰ� ����. batchNo �� ���� �ٸ� �õ带 �ֱ����� ��¥�� ������.
		
			switch (EIrModelType.getEIrModelType(esgMst.getIrModedType())) {
				case MERTON :
						break;
					
				case VASICEK:	
						Vasicek vasicek = new Vasicek(baseYymm, shortRate, esgMst.getBizApplyParam());
						irScenarioList.addAll(vasicek.getVasicekScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HOLEE:		
						
						break;
					
				case HULLWHITE:	
						
						HullWhite hullWhite = new HullWhite(baseYymm, irCurveHisList, esgMst.getBizApplyParam(), ufr, ufrt);
//						irScenarioList.addAll(hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						return  hullWhite.getHullWhiteScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo);
//						break;
						
				case CIR:		
						CIR cir = new CIR(baseYymm, shortRate, esgMst.getBizApplyParam());
						irScenarioList.addAll(cir.getCirScenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				case HW2:		 
						HullWhite2Factor hw2 = new HullWhite2Factor(baseYymm, irCurveHisList, esgMst.getBizApplyParam(), ufr, ufrt);
						irScenarioList.addAll(hw2.getHW2Scenario(bssd, irCurveId, esgMst.getIrModelId(), 100, batchNo));
						break;
						
				default:
						
						break;
			}
			
		return irScenarioList;
	}
	*/
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
