package com.gof.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.dao.IrCurveHisDao;
import com.gof.entity.IrCurveHis;
import com.gof.entity.LiqPremium;
import com.gof.model.LiquidPremiumModel;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;


/**
 *  <p> ������ �����̾� ���� ����          
 *  <p> BottomUp ������ ������ ����� �����ä�� �������� Ư���� �ݿ��Ͽ� ������ �ݸ��� ������ ������ �������带 ������.
 *  <p>    1. �����ä�� �������� Ư���� �ݿ��� Proxy ��� ���� (���ä) 
 *  <p>    2. Proxy ��ǰ(���ä)�� ������ �ݸ��� ���� �������� ���� 
 *  <p>	     2.1  ���忡�� ������ ��������� ������ �����̾� �Ӹ� �ƴ϶� ä���� ��������ũ  �� ������ Noise �� ���ԵǾ� ����.  
 *  <p>	     2.2  ������������ ���� ��������� ��������� 0 ���� �����ϹǷ� ���� ������ ���������� ��� ����� ������.
 *  <p>    3. Proxy ��ǰ�� ���������� 36���� �̵� ����� �̿��Ͽ� ���⺰ ������ �����̾� �̷��� ������. 
 *  <p>    4. ������ �����̾��� �⺻Ư�� ( �̰����� �Ⱓ ��, LLP ���� �Ⱓ�� ������ �����̾��� 0�̾�� �ϰ�, �ִ� ���⿡�� ( �̷����� �ʴܱ���)�� ������ �����̾��� 0 ��) �� �̿��Ͽ� 
 *  <p>    4.1  ������ ���� ������ �����̾��� Curve Fitting ��.
 *  <p>    5. ������ ������ �����̾��� Curve Fitting ���� ������ �������� ������ �����̾� ����
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */
public class Job20_LiquidPremium {
	
	private final static Logger logger = LoggerFactory.getLogger("LiquidPremium");
	
	public static List<LiqPremium> createLiquidPremium(String bssd, String lqModel) {
		List<IrCurveHis> spreadList = new ArrayList<IrCurveHis>();
		IrCurveHis spreadTemp ;
		int avgMonNum = Integer.parseInt(ParamUtil.getParamMap().getOrDefault("lqAvgNum", "-36"));
		String lqKtbIrCruveId = ParamUtil.getParamMap().getOrDefault("lqKtbIrCruveId", "A100");
		String lqKdbIrCurveId = ParamUtil.getParamMap().getOrDefault("lqKdbIrCruveId", "E110");
		
		String stBssd = FinUtils.addMonth(bssd, avgMonNum);
		List<IrCurveHis> ktbList = IrCurveHisDao.getCurveHisBetween(bssd, stBssd, lqKtbIrCruveId);
		List<IrCurveHis> kdbList = IrCurveHisDao.getCurveHisBetween(bssd, stBssd, lqKdbIrCurveId);
		
		if(ktbList.size()==0 || kdbList.size()==0) {
			return new ArrayList<LiqPremium>();
		}
		
		Map<String, Double> ktbMap = ktbList.stream().collect(Collectors.toMap(s -> s.getBaseDate() + "#" +s.getMatCd() , s ->s.getIntRate()));
		Map<String, Double> kdbMap = kdbList.stream().collect(Collectors.toMap(s -> s.getBaseDate() + "#" +s.getMatCd() , s ->s.getIntRate()));

		double ktbRate =1.0;
		double kdbRate =1.0;
		
		for(Map.Entry<String, Double> entry: ktbMap.entrySet()) {
			if(kdbMap.containsKey(entry.getKey())) {
				ktbRate = entry.getValue();
				kdbRate = kdbMap.get(entry.getKey());
				spreadTemp = new IrCurveHis(entry.getKey().split("#")[0], entry.getKey().split("#")[1], ktbRate==0? 1: kdbRate/ktbRate );
				spreadList.add(spreadTemp);
			}
		}

		Map<String, List<IrCurveHis>> spreadMap  = spreadList.stream().collect(Collectors.groupingBy(s ->s.getMatCd(), Collectors.toList()));
		
//		spreadMap.get("M0036").stream().forEach(s -> logger.info("Spread : {},{}", s.getBaseDate(), s.getIntRate()));
		
		List<IrCurveHis> liqCurveList = new ArrayList<IrCurveHis>();
		int cnt =0;
		double sumRate =0.0;
		double curRate =0.0;
		String maxBssd = "";
		
		for(Map.Entry<String, List<IrCurveHis>> entry : spreadMap.entrySet()) {
			sumRate=0.0;
			cnt = 0 ;
			
			for(IrCurveHis aa : entry.getValue()) {
				cnt = cnt+1;
//				���⺰�� ���������� �������� ������ ����
				sumRate= sumRate + aa.getIntRate();		
				if(aa.getBaseDate().compareTo(maxBssd) > 0) {
					maxBssd = aa.getBaseDate();
				}
			}	
			curRate = ktbMap.getOrDefault( maxBssd + "#" +entry.getKey() , 1.0);
			
//			���⺰�� ���������� �������� ������ ����� �����ϰ� ���� �������忡 �ݿ���.
//			logger.info("Sum : {},{},{},{},{}", entry.getKey(), sumRate, cnt, curRate, curRate * (sumRate/cnt -1));
			liqCurveList.add(new IrCurveHis(bssd, entry.getKey(), curRate * (sumRate/cnt -1) ));
		}
		
		LiquidPremiumModel lpModel = new LiquidPremiumModel(lqModel, liqCurveList, 0, 20);
//		logger.info("liq : {}", liqCurveList.size());
//		liqCurveList.stream().sorted(new IrCurveHisComparator()).forEachOrdered(s -> logger.info("Average Liqidity Premium of {} during past {} month  : {}" , s.getMatCd(), -1.0* avgMonNum, s.getIntRate()));
		
		List<LiqPremium> rst = lpModel.getLiqPremium(bssd);
		
		logger.info("Job20( Liquid Premium Calculation) creates  {} results.  They are inserted into EAS_LIQ_PREM Table", rst.size());
		rst.stream().forEach(s->logger.debug("Liquidity Premium Result : {}", s.toString()));
		return rst;
		
		
	}
}