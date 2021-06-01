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
 *  <p> ESG 모형의 금리/시장변수 시나리오를 생성작업 수행함.       
 *  <p> Hull White 1Factor/ 2Factor 모형, CIR, Vasicek 등 일반적인 금리 시나리오 모형을 구현하고 있음.
 *  <p> 주식 등의 시장변수에 대해서는 기본적인 Brownian 모형, Black 모형 등으로 시나리오를 생성함.
 *  <p> 주식 등의 일반시장변수는 모수 추정과 생성된 Random Number 를 ESG 모형에 입력하여 시나리오를 생성하는 2단게로 구성됨
 *  <p> 금리 시나리오는 금리 기간 구조 Fitting 단계가 추가됨.  
 *  <p>  1. 금리 모형의 모수 추정 
 *  <p>  2. 금리모형 ,  매개변수 ,  Random Number 의 조합으로 Short Rate 의 시나리오 산출
 *  <p>  3. 금리 기간 구조 Fitting 로직으로 Bucket 별 금리 산출로 전체 시나리오 생성 
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job15_EsgScenarioAsync {
	private final static Logger logger = LoggerFactory.getLogger(Job15_EsgScenarioAsync.class);
	
	
	public static List<IrSce> createEsgScenarioAsync(String bssd,  String irCurveId, List<IrCurveHis> irCurveHisList, List<BizEsgParam> esgParam, EsgMst esgMst, double ufr, double ufrt,  double shortRate, int batchNo) {
		
		logger.info("IR Scenario for {} Batch No and Thread Name: {}, {}", irCurveHisList.get(0).getIrCurveId(),batchNo, Thread.currentThread().getName());
		List<IrSce> irScenarioList = new ArrayList<IrSce>();
		String baseYymm = bssd + String.format("%02d", batchNo);   //기준일자로 시드 넘버를 고정하고 있음. batchNo 에 따라 다른 시드를 주기위해 날짜를 조정함.
		
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
		String baseYymm = bssd + String.format("%02d", batchNo);   //기준일자로 시드 넘버를 고정하고 있음. batchNo 에 따라 다른 시드를 주기위해 날짜를 조정함.
		
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
		
		List<EsgMst> esgMstList = EsgMstDao.getEsgMstWithParam(bssd, EBoolean.Y);			//TODO : irCurveId 로 filtering 처리 고려???
		
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
