package com.gof.process;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.csvmapper.IrCurveHisMapper;
import com.gof.dao.DiscRateSettingDao;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.DiscRateCalcSetting;
import com.gof.entity.DiscRateSce;
import com.gof.entity.IrCurveHis;
import com.gof.enums.EBaseMatCd;
import com.gof.model.DiscRateModel;
import com.gof.model.DiscRateModelNew;
import com.gof.util.FileUtils;
import com.gof.util.FinUtils;
import com.gof.util.ParamUtil;

/**
 *  <p> ������ �ݸ��� �ó�������  ���α����� �������� ���� ������ �����Ͽ� �������� �ó������� ������.
 *  <p> �������� �ó������� KICS �Ǵ� QIS �������� ������ �� ������ �������� �ó������� ������ ���� ����Ͽ� 1���� ������θ� �������� �ó������� ������.
 *  <p> ������ �ݸ� �ó������� �ݸ������� ���� ������ �۾��� �ݺ��Ͽ� �ó������� ������. ( ������ �ݸ� �ó����� ���� ��ŭ �����ϸ�, ���� 1,000 ���� ������) 
 *  <p>    1. ���� Driver �� ����ä ����  
 *  <p>    2. ����ä�� �������� ������ �ֿ� Factor �� �ڻ��� ���ͷ�, �ܺ���ǥ�ݸ��� �ΰ����踦 ��������� �м���. 
 *  <p>	     2.1 �ڻ��� ���ͷ��� ����ä�� �ð迭 �����͸� ���� ������ ���� 
 *  <p>	     2.2 �ܺ� ��ǥ�ݸ���  ����ä�� �ð迭 �����͸� ���� ������ ����
 *  <p>    3. ���� Driver �� ����ä�� �ó����� ({@link Job14_EsgScenario} �� �������� �����Ͽ� �̷� ������ �ڻ�����ͷ� , �ܺ���ǥ�ݸ� ����
 *  <p>    4. ������ �ڻ�����ͷ�, �ܺ���ǥ�ݸ��� �̿��Ͽ� ���ñ��������� ���������� ������. ( �̷��� ������ �������� ���� �������� ���� ���ٰ� ������.)
 *  
 *  
 * @author takion77@gofconsulting.co.kr 
 * @version 1.0
 */

public class Job38_DiscRateScenario {
	private final static Logger logger = LoggerFactory.getLogger(Job38_DiscRateScenario.class);
	
	public static void writeDiscRateScenario (String bssd, int batchNum, Path irScePath, String output) {	
		Map<String, List<IrCurveHis>> eomRstByMatCdMap = getEomCurveMap(bssd, -36);
		int sceCnt =0;
		
		Path discRateScePath = Paths.get(output + "DiscRateSce_" + bssd + ".csv");
		Path bizDiscRateScePath = Paths.get(output + "BizDiscRateSce_" + bssd + ".csv");
		
		FileUtils.reset(discRateScePath);
		FileUtils.reset(bizDiscRateScePath);
		
//		�ó����� ���� Reading
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
//			�ó������� �ݸ� �Ⱓ����
			Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
			
			for(Map.Entry<String, List<IrCurveHis>> entry : sceMap.entrySet()) {
				Map<String, List<IrCurveHis>> fwdMap = getFwdHisMap(bssd, eomRstByMatCdMap, entry.getValue());
//				if(!entry.getKey().equals("1")) {
//					continue;
//				}
				List<DiscRateSce> discRatesceRst = merge(bssd, entry.getKey(), fwdMap);
				try (Writer writer = new BufferedWriter(new FileWriter(discRateScePath.toFile(), true));
						Writer bizWriter = new BufferedWriter(new FileWriter(bizDiscRateScePath.toFile(), true));
					) 
				{
					for(DiscRateSce zz : discRatesceRst) {
						writer.write(zz.toString());						//�������� �ó����� ���� ����
						bizWriter.write(zz.toBizString(","));				//������ �������� �ó����� ���� ����
					}
				} catch (Exception e) {
//					logger.info("Current Thread 13:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
					logger.error("Error in DcntSce result writing : {}", e);
				}
				
				sceCnt =sceCnt +1;
				if (sceCnt % 100 == 0) {
					logger.info("DiscRateSce result Writing...  {}/{}", sceCnt, batchNum * 100);
				}
				
				}
		} catch (Exception e) {
			logger.info("IR Scenario File Error : {}", e);
		}
	}
	
	public static List<DiscRateSce> getDiscRateScenario (String bssd, int batchNum, Path irScePath) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		
		Map<String, List<IrCurveHis>> eomRstByMatCdMap = getEomCurveMap(bssd, -36);
//		eomRstByMatCdMap.entrySet().forEach(s -> logger.info("eomRstByMatCdMap : {},{}", s.getKey(), s.getValue()));
		
		int sceCnt =0;
		
//		�ó����� ���� Reading
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntity(irScePath)) {
//			�ó������� �ݸ� �Ⱓ����
			Map<String, List<IrCurveHis>> sceMap = rStream.collect(Collectors.groupingBy(s -> s.getSceNo(), Collectors.toList()));
			
			for(Map.Entry<String, List<IrCurveHis>> entry : sceMap.entrySet()) {
				if(sceCnt < 200 ) {
						
				Map<String, List<IrCurveHis>> fwdMap = getFwdHisMap(bssd, eomRstByMatCdMap, entry.getValue());
				
//				fwdMap.entrySet().forEach(s -> logger.info("fwdMap : {},{}", s.getKey(), s.getValue().size()));
//				fwdMap.entrySet().stream().filter(s -> s.getKey().equals("M0060")).forEach(s -> logger.info("aaa : {}, {}", s.getKey(), s.getValue()));
				
//				rstList.addAll(getDiscRateScenarioCalc(bssd, entry.getKey(), fwdMap));
//				rstList.addAll(getDiscRateScenarioGiven(bssd, entry.getKey(), "I"));
				rstList = merge(bssd, entry.getKey(), fwdMap);
				
				sceCnt =sceCnt +1;
				logger.info("DiscRateSce  is done {}/{}", sceCnt, batchNum * 100);
				}
			}
	
		} catch (Exception e) {
			logger.info("IR Scenario File read Error : {}", e);
		}
		return rstList;
	}
	
	private static List<DiscRateSce>  merge(String bssd, String sceNo, Map<String, List<IrCurveHis>> fwdMap){
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		
		rstList.addAll(getDiscRateScenarioCalc(bssd, sceNo, fwdMap));
		rstList.addAll(getDiscRateScenarioGiven(bssd, sceNo, "I"));
		
//		rstList.addAll(getKicsDiscRateScenarioCalc(bssd, sceNo, fwdMap));
//		rstList.addAll(getDiscRateScenarioGiven(bssd, sceNo, "K"));
		
		
		return rstList;
	}

	private static List<DiscRateSce> getDiscRateScenarioCalc(String bssd, String sceNo, Map<String, List<IrCurveHis>> fwdMap) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		
		List<DiscRateCalcSetting> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
																  .filter(s -> s.isCalculable())
//																  .filter(s -> s.getCalcType().equals("03"))
//																  .filter(s -> s.getIntRateCd().equals("2305"))
//																  .filter(s -> s.getIntRateCd().equals("3101"))
																  .collect(Collectors.toList());
		for (DiscRateCalcSetting setting : discSetting) {
			String discRateSceBizDv = ParamUtil.getParamMap().getOrDefault("discRateSceBizDv", "I");
			if(discRateSceBizDv.equals("I")) {
				DiscRateModelNew  discModel = new DiscRateModelNew(bssd, "I", setting, sceNo, fwdMap);
				rstList.addAll(discModel.getInternalDiscRateScenario());
			}else if(discRateSceBizDv.equals("K")) {
				DiscRateModelNew  discModel = new DiscRateModelNew(bssd, "K", setting, sceNo, fwdMap);
				rstList.addAll(discModel.getKicsDiscRateScenario());
			}
//			logger.info("bbbb");
		}	
		return rstList;		
	}
	
	private static List<DiscRateSce> getKicsDiscRateScenarioCalc(String bssd, String sceNo, Map<String, List<IrCurveHis>> fwdMap) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		
		List<DiscRateCalcSetting> discSetting = DiscRateSettingDao.getDiscRateSettings().stream()
																  .filter(s -> s.isCalculable())
//																  .filter(s -> s.getCalcType().equals("03"))
//																  .filter(s -> s.getIntRateCd().equals("2305"))
//																  .filter(s -> s.getIntRateCd().equals("3101"))
																  .collect(Collectors.toList());
		for (DiscRateCalcSetting setting : discSetting) {
			DiscRateModel  discModel = new DiscRateModel(bssd, "K", setting, sceNo, fwdMap);
			
			rstList.addAll(discModel.getKicsDiscRateScenario());
//			logger.info("bbbb");
		}	
		return rstList;		
	}
	
	
	private static List<DiscRateSce> getDiscRateScenarioGiven(String bssd, String sceNo, String calcType) {
		List<DiscRateSce> rstList = new ArrayList<DiscRateSce>();
		List<DiscRateCalcSetting> discSetting = DiscRateSettingDao.getDiscRateSettings().stream().filter(s -> !s.isCalculable()).collect(Collectors.toList());
		
		for (DiscRateCalcSetting setting : discSetting) {
			DiscRateModel  discModel = new DiscRateModel(bssd, calcType, setting, sceNo);
//			DiscRateModel  discModel = new DiscRateModel(bssd, "I", setting, sceNo);
			rstList.addAll(discModel.getDiscRateScenarioUserGiven(calcType));
		}
		
		return rstList;		
	}
	
	private static Map<String, List<IrCurveHis>> getEomCurveMap(String bssd, int monthNum){
		List<IrCurveHis> curveList = IrCurveHisDao.getCurveHisBetween(bssd, FinUtils.addMonth(bssd, monthNum), "A100");		
		Map<String, String> eomDate = curveList.stream().collect(Collectors.toMap(s ->s.getBaseYymm(), s-> s.getBaseDate(), (s,u)-> u));
		
		return  curveList.stream()
						 .filter(s -> eomDate.containsValue(s.getBaseDate()))
						 .map(s -> s.addForwardTerm(bssd))
						 .collect(Collectors.groupingBy(s->s.getMatCd(), Collectors.toList()))
						;
	}

	private static Map<String, List<IrCurveHis>>  getFwdHisMap(String bssd, Map<String, List<IrCurveHis>> eomRstByMatCdMap, List<IrCurveHis> curveList) {
			Map<String, List<IrCurveHis>> curveByMaturityMap = new HashMap<String, List<IrCurveHis>>();
			
	//		���� ���� ������ 
	//		Map<String, List<IrCurveHis>> eomRstByMatCdMap = getEomCurveMap(bssd, -36);
			
			//		�ݸ��Ⱓ�������� Bucket �� forward ���� & ���� ���� Add
			for(EBaseMatCd aa : EBaseMatCd.values()) {
				List<IrCurveHis> tempList = new ArrayList<IrCurveHis>();
//				if(aa.equals(EBaseMatCd.M0060)) {
					
	//			���� ������ Bucket �� �ݸ�
//				eomRstByMatCdMap.getOrDefault(aa.name(), new ArrayList<IrCurveHis>()).forEach(s -> logger.info("zzzzzzz : {},{}", s.toString()));
				tempList.addAll(eomRstByMatCdMap.getOrDefault(aa.name(), new ArrayList<IrCurveHis>()));
				
	//			�ó������� Bucket �� Forward �ݸ�
//				FinUtils.getForwardRateByMaturity(bssd, curveList, aa.name()).forEach(s -> logger.info("KKKKKKKKKKKkk : {},{}",s.toString()));
				tempList.addAll(FinUtils.getForwardRateByMaturity(bssd, curveList, aa.name()));
				
				curveByMaturityMap.put(aa.name(), tempList);
//				}
			}
			return curveByMaturityMap;
		}
}
