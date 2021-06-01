package com.gof.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.DaoUtil;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrShockSce;
import com.gof.entity.SmithWilsonParam;
import com.gof.model.DynamicNelsonSiegel;
import com.gof.util.FinUtils;

/**
 *  <p> KICS 의 SCR 산출시 적용되는 금리 충격 시나리오 생성 모형         
 *  <p> 현재 표준모형에서는 금리 충격 시나리오를 금융감독원이 제시하고 있으나 내부모형 적용시 자체적인 충격 시나리오를 산출해야함.
 *  <p> 충격 시나리오법 적용한 금리 내부모형 채택시 충격시나리오 생성을 위한 모형임. 
 *  <p>    1. 초기 매개변수 설정
 *  <p>    2. Calman Filter 를 적용한 DNS 모형의 Level, Trend, Curvature 모수를 추정함.  
 *  <p>    3. 추정된 모수를 이용하여 금리 시나리오를 생성함. 
 *  <p>    4. PCA 방법론을 적용하여 Flatten, Steepen, ShiftUp, ShiftDown 시나리오 등을 생성함.   
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job18_DnsScenario {
	private final static Logger logger = LoggerFactory.getLogger("EsgScenario");
	
	public static List<IrShockSce> createDnaShockScenario(String bssd, String curCd, String irCurveId, int batchNo, double errorTolerance, double volAdjust) {
		List<IrShockSce> irScenarioList = new ArrayList<IrShockSce>();
		IrShockSce tempSce;

//		과거 1년 금리 데이터 추출
		Map<String, List<IrCurveHis>> curveMap = IrCurveHisDao.getIrCurveListTermStructure(bssd, FinUtils.addMonth(bssd, -12), irCurveId);
		
		if(curveMap.size()==0) {
			logger.warn("IR Curve History of {} Data is not found at {}", irCurveId, bssd);
			return irScenarioList;
		}
		
		Stream<SmithWilsonParam> swStream =DaoUtil.getEntityStream(SmithWilsonParam.class, new HashMap<>());
		Map<String, SmithWilsonParam> swParamMap = swStream.collect(Collectors.toMap(s->s.getCurCd(), Function.identity()));
		
		double ufr =  swParamMap.get(curCd).getUfr();
		double ufrt =  swParamMap.get(curCd).getUfrT();
		
		logger.debug("Curve His Data :  {},{},{}", irCurveId, curveMap.get("M0003").size(), ufr, ufrt);
		
		
		DynamicNelsonSiegel dns = new DynamicNelsonSiegel(bssd, curveMap, ufr, ufrt);
		
		irScenarioList.addAll(dns.getDnsScenario(bssd, irCurveId, errorTolerance, volAdjust));
		
		logger.info("Job18( Dynamic Nelson Siegle Scenario Calculation) creates  {} results.  They are inserted into EAS_IR_SHOCK_SCE Table", irScenarioList.size());
		irScenarioList.stream().forEach(s->logger.debug("Dynamic Nelson Siegle Scenario Result : {}", s.toString()));
		
		return irScenarioList;
	}
}
