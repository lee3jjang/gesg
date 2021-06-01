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
 *  <p> KICS �� SCR ����� ����Ǵ� �ݸ� ��� �ó����� ���� ����         
 *  <p> ���� ǥ�ظ��������� �ݸ� ��� �ó������� ������������ �����ϰ� ������ ���θ��� ����� ��ü���� ��� �ó������� �����ؾ���.
 *  <p> ��� �ó������� ������ �ݸ� ���θ��� ä�ý� ��ݽó����� ������ ���� ������. 
 *  <p>    1. �ʱ� �Ű����� ����
 *  <p>    2. Calman Filter �� ������ DNS ������ Level, Trend, Curvature ����� ������.  
 *  <p>    3. ������ ����� �̿��Ͽ� �ݸ� �ó������� ������. 
 *  <p>    4. PCA ������� �����Ͽ� Flatten, Steepen, ShiftUp, ShiftDown �ó����� ���� ������.   
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job18_DnsScenario {
	private final static Logger logger = LoggerFactory.getLogger("EsgScenario");
	
	public static List<IrShockSce> createDnaShockScenario(String bssd, String curCd, String irCurveId, int batchNo, double errorTolerance, double volAdjust) {
		List<IrShockSce> irScenarioList = new ArrayList<IrShockSce>();
		IrShockSce tempSce;

//		���� 1�� �ݸ� ������ ����
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
