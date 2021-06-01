package com.gof.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.entity.BizDiscountRate;
import com.gof.entity.IrCurveHis;
import com.gof.entity.IrSce;
import com.gof.util.FileUtils;
import com.gof.util.HibernateUtil;

public class FssCurveTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static Session session = HibernateUtil.getSessionFactory().openSession();
	public static void main(String[] args) {
		String baseDate ="20171231";
		Path base1 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base1.txt");
		Path outbase1 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201712_Z.csv");
//		
//		toIrCurveSce(baseDate, "BASE1", base1, outbase1);
		toDiscountSce("201712", "Z", base1, outbase1);
		
		Path base8 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base8.txt");
		Path outbase8 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201712_X.csv");
		toDiscountSce("201712", "X", base8, outbase8);
//		toIrCurveSce(baseDate, "BASE8", base8, outbase8);
//		
		Path base9 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base9.txt");
		Path outbase9 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201712_Y.csv");
		toDiscountSce("201712", "Y", base9, outbase9);
//		toIrCurveSce(baseDate, "BASE9", base9, outbase9);
		
		Path base2 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base2.txt");
		Path outbase2 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201612_Z.csv");
		toDiscountSce("201612", "Z", base2, outbase2);
		
		Path base3 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base3.txt");
		Path outbase3 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201512_Z.csv");
		toDiscountSce("201512", "Z", base3, outbase3);
		
		Path base4 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base4.txt");
		Path outbase4 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201412_Z.csv");
		toDiscountSce("201412", "Z", base4, outbase4);
		
		
		Path base5 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base5.txt");
		Path outbase5 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201312_Z.csv");
		toDiscountSce("201312", "Z", base5, outbase5);
		
		
		Path base6 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base6.txt");
		Path outbase6 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201212_Z.csv");
		toDiscountSce("201212", "Z", base6, outbase6);
		
		Path base7 = Paths.get("D:\\Dev\\IR_CURVE\\fssSce_base7.txt");
		Path outbase7 = Paths.get("D:\\Dev\\ESG\\DcntSce_KRW_201112_Z.csv");
		toDiscountSce("201112", "Z", base7, outbase7);
		
//		Path spot = Paths.get("D:\\Dev\\IR_CURVE\\fssCurve_Spot.txt");
//		Path outspot = Paths.get("D:\\Dev\\ESG\\fssCurve_Spot_base1.csv");
//		toIrCurveSpot(baseDate, 2, spot, outspot);		
		
		
//		Path fwd = Paths.get("D:\\Dev\\IR_CURVE\\fssCurve_Forward.txt");
//		Path outfwd = Paths.get("D:\\Dev\\ESG\\fssCurve_Forward_base1.csv");
//		toIrCurveSpot(baseDate, 2, fwd, outfwd);
		
		
		
	}
	
	
	private static List<String> realFile(Path inPath) {
		List<String> rst = new ArrayList<String>();
		String bssd ="20171231";
		
		int cnt =0;
		
		try (Stream<String> rStream = Files.lines(inPath)) {
			
//			rStream.skip(1).limit(2).flatMap(s -> Arrays.stream(s.split("\t"))).forEach(s-> logger.info("aa : {}", Double.parseDouble(s.replace("%", "").trim())/100.0));
//			rStream.skip(1).limit(2).flatmap(s-> Arrays.stream(s.splitforEach(s-> logger.info("aa : {}", s));
//			rst =rStream.skip(1).limit(2).flatMap(s -> Arrays.stream(s.split("\t"))).collect(Collectors.toList()); 
			
			rst =rStream
					.skip(1)
//					.limit(2)
					.collect(Collectors.toList());
			
		}
		catch (Exception e) {
			logger.info("aa : {}", e);
		}
//		rst.forEach(s -> logger.info("aaa : {}", s));
		return rst;
	}
	
	private static List<String> realSpotFile(Path inPath) {
		List<String> rst = new ArrayList<String>();
		try (Stream<String> rStream = Files.lines(inPath)) {
			rst =rStream
//					.skip(1)
//					.limit(2)
					.collect(Collectors.toList());
		}
		catch (Exception e) {
			logger.info("aa : {}", e);
		}
		return rst;
	}
	
	private static List<IrSce> toIrCurveSce(String bssd, String sceSet, Path inPath, Path outPath) {
		List<IrSce> rst = new ArrayList<IrSce>();
		int cnt =0;
		for(String aa : realFile(inPath)) {
			String[] zz = aa.split("\t");
			logger.info("aaa : {}", zz[0]);
			cnt =0;
			for(String bb : zz) {
				if(cnt >0) {
					
					IrSce rstTemp = new  IrSce();
				rstTemp.setBaseDate(bssd);
				rstTemp.setIrCurveId("A100");
				rstTemp.setIrModelId(sceSet);
				rstTemp.setSceNo(zz[0]);
				rstTemp.setMatCd("M" + String.format("%04d", cnt));
				rstTemp.setRfIr(Double.parseDouble(zz[cnt].replace("%", "").trim())/100.0);
				
				rst.add(rstTemp);
				}
				cnt =cnt+1;
			}
			
		}
//		rst.forEach(s -> logger.info("aaa : {}", s.toString()));
		
		FileUtils.writeHeader(outPath,"#BASE_DATE, IR_MODEL_ID, MAT_CD, SCE_NO, IR_CURVE_ID, INT_RATE, VOL, LAST_MODIFIEID_BY, LAST_UPDATE");
		try {
			logger.info("Current Thread 2:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
			Files.write(outPath, (Iterable<String>) rst.stream().map(s -> s.toString())::iterator,StandardOpenOption.APPEND);
		} catch (Exception e) {
			logger.error("Error in HW Scenario result writing : {}", e);
		}
		return rst;
		
	}

	
	
	public static List<IrCurveHis> toIrCurveSpot(String bssd, int index,  Path inPath, Path outPath) {
		List<IrCurveHis> rst = new ArrayList<IrCurveHis>();
		int cnt =0;
//		만기, 17년 Spot, 17년 할인, 16년 Spot, 16년 할인,15년 Spot, 15년 할인,14년 Spot, 14년 할인,13년 Spot, 13년 할인,12년 Spot, 12년 할인,11년 Spot, 11년 할인,17년 Spot, 17년 할인+80%,17년 Spot, 17년 할인+80%+자기신용,
		for(String aa : realSpotFile(inPath)) {
			String[] zz = aa.split("\t");
			int matNum = Integer.parseInt(zz[0]);
//			logger.info("aaa : {},{}", zz[0], matNum);
			cnt =0;
				IrCurveHis rstTemp = new  IrCurveHis();
				rstTemp.setBaseDate(bssd);
				rstTemp.setIrCurveId("A100");
//				rstTemp.setIrModelId(sceSet);
				rstTemp.setSceNo("BASE");
				rstTemp.setMatCd("M" + String.format("%04d", matNum));
				rstTemp.setIntRate( Double.parseDouble(zz[index].replace("%", "").trim())/100.0);
				rstTemp.setForwardNum(0);

				
				rst.add(rstTemp);
			
		}
//		rst.forEach(s -> logger.info("aaa : {}", s.toString()));
		
		FileUtils.writeHeader(outPath,"#BASE_DATE, SCE_NO,   IR_CURVE_ID, MAT_CD, INT_RATE, forwardNum LAST_MODIFIEID_BY, LAST_UPDATE");
		try {
//			logger.info("Current Thread 2:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
			Files.write(outPath, (Iterable<String>) rst.stream().map(s -> s.toString())::iterator,StandardOpenOption.APPEND);
		} catch (Exception e) {
			logger.error("Error in HW Scenario result writing : {}", e);
		}
		return rst;
		
	}
	
	public static List<IrCurveHis> toIrCurveForward(String bssd, int index,  Path inPath, Path outPath) {
		List<IrCurveHis> rst = new ArrayList<IrCurveHis>();
		int cnt =0;
//		만기, 17년 Spot, 17년 할인, 16년 Spot, 16년 할인,15년 Spot, 15년 할인,14년 Spot, 14년 할인,13년 Spot, 13년 할인,12년 Spot, 12년 할인,11년 Spot, 11년 할인,17년 Spot, 17년 할인+80%,17년 Spot, 17년 할인+80%+자기신용,
		for(String aa : realSpotFile(inPath)) {
			String[] zz = aa.split("\t");
			int matNum = Integer.parseInt(zz[0]);
//			logger.info("aaa : {},{}", zz[0], matNum);
			cnt =cnt+1;
				IrCurveHis rstTemp = new  IrCurveHis();
				rstTemp.setBaseDate(bssd);
				rstTemp.setIrCurveId("A100");
//				rstTemp.setIrModelId(sceSet);
				rstTemp.setSceNo("BASE");
				rstTemp.setMatCd("M0001");
				rstTemp.setIntRate( Double.parseDouble(zz[index].replace("%", "").trim())/100.0);
				rstTemp.setForwardNum(cnt);

				
				rst.add(rstTemp);
			
		}
//		rst.forEach(s -> logger.info("aaa : {}", s.toString()));
		
		FileUtils.writeHeader(outPath,"#BASE_DATE, SCE_NO,   IR_CURVE_ID, MAT_CD, INT_RATE, forwardNum LAST_MODIFIEID_BY, LAST_UPDATE");
		try {
//			logger.info("Current Thread 2:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
			Files.write(outPath, (Iterable<String>) rst.stream().map(s -> s.toString())::iterator,StandardOpenOption.APPEND);
		} catch (Exception e) {
			logger.error("Error in HW Scenario result writing : {}", e);
		}
		return rst;
		
	}
	
	public static List<BizDiscountRate> toIrCurveNew(String bssd, int index, String applyBizDv, Path inPath) {
		List<BizDiscountRate> rst = new ArrayList<BizDiscountRate>();
		int cnt =0;
		
		
		for(String aa : realSpotFile(inPath)) {
			String[] zz = aa.split("\t");
			int matNum    = Integer.parseInt(zz[0]);
//			double rfRate = Double.parseDouble(zz[index].replace("%", "").trim())/100.0;
//			double riskRate = Double.parseDouble(zz[index +1].replace("%", "").trim())/100.0;
//			double liq = riskRate -rfRate;
//			
//			double fwdRate = Double.parseDouble(zz[index+19].replace("%", "").trim())/100.0;

			double rfRate = Double.parseDouble(zz[1].replace("%", "").trim());
			double riskRate = Double.parseDouble(zz[2].replace("%", "").trim());
			double liq = riskRate -rfRate;
			double fwdRate = Double.parseDouble(zz[3].replace("%", "").trim());
			
			BizDiscountRate rstTemp = new  BizDiscountRate();
			
			rstTemp.setBaseYymm(bssd);
			rstTemp.setApplyBizDv(applyBizDv);
			rstTemp.setIrCurveId("RF_KRW_BU");
			rstTemp.setMatCd("M" + String.format("%04d", matNum));
			rstTemp.setRfRate(rfRate);
			rstTemp.setLiqPrem(liq);
			
			rstTemp.setRefYield(0.0);
			rstTemp.setCrdSpread(0.0);

			rstTemp.setRiskAdjRfRate(riskRate);
			rstTemp.setRiskAdjRfFwdRate(fwdRate);
			
			rstTemp.setVol(0.0);
			rstTemp.setLastModifiedBy("ESG");
			rstTemp.setLastUpdateDate(LocalDateTime.now());
			rst.add(rstTemp);
		}
		
		return rst;
	}
	
	public static List<BizDiscountRate> toIrCurve(String bssd, int index, String applyBizDv, Path inPath) {
		List<BizDiscountRate> rst = new ArrayList<BizDiscountRate>();
		int cnt =0;
//		만기, 17년 Spot, 17년 할인, 16년 Spot, 16년 할인,15년 Spot, 15년 할인,14년 Spot, 14년 할인,13년 Spot, 13년 할인,12년 Spot, 12년 할인,11년 Spot, 11년 할인,17년 Spot, 17년 할인+80%,17년 Spot, 17년 할인+80%+자기신용,
//		BizDiscountRate rstTemp17 = new  BizDiscountRate();
//		BizDiscountRate rstTemp16 = new  BizDiscountRate();
//		BizDiscountRate rstTemp15 = new  BizDiscountRate();
//		BizDiscountRate rstTemp14 = new  BizDiscountRate();
//		BizDiscountRate rstTemp13 = new  BizDiscountRate();
//		BizDiscountRate rstTemp12 = new  BizDiscountRate();
//		BizDiscountRate rstTemp11 = new  BizDiscountRate();
//		BizDiscountRate rstTemp1780 = new  BizDiscountRate();
//		BizDiscountRate rstTemp1780Spread = new  BizDiscountRate();
		
		
		for(String aa : realSpotFile(inPath)) {
			String[] zz = aa.split("\t");
			int matNum    = Integer.parseInt(zz[0]);
			double rfRate = Double.parseDouble(zz[index].replace("%", "").trim())/100.0;
			double riskRate = Double.parseDouble(zz[index +1].replace("%", "").trim())/100.0;
			double liq = riskRate -rfRate;
			double fwdRate = Double.parseDouble(zz[index+19].replace("%", "").trim())/100.0;

			
			BizDiscountRate rstTemp = new  BizDiscountRate();
			
			rstTemp.setBaseYymm(bssd);
			rstTemp.setApplyBizDv(applyBizDv);
			rstTemp.setIrCurveId("RF_KRW_BU");
			rstTemp.setMatCd("M" + String.format("%04d", matNum));
			rstTemp.setRfRate(rfRate);
			rstTemp.setLiqPrem(liq);
			
			rstTemp.setRefYield(0.0);
			rstTemp.setCrdSpread(0.0);

			rstTemp.setRiskAdjRfRate(riskRate);
			rstTemp.setRiskAdjRfFwdRate(fwdRate);
			
			rstTemp.setVol(0.0);
			rstTemp.setLastModifiedBy("ESG");
			rstTemp.setLastUpdateDate(LocalDateTime.now());
			rst.add(rstTemp);
			
		}
		
		return rst;
		
	}
	
	private static void toDiscountSce(String bssd, String applyBizDv, Path inPath, Path outPath) {
		int cnt =0;
		List<String> rst = new ArrayList<String>();
		for(String aa : realFile(inPath)) {
			String[] zz = aa.split("\t");
//			logger.info("aaa : {}", zz[0]);
			cnt =0;
			for(String bb : zz) {
				if(cnt >0) {
					
					double fwdRate = Double.parseDouble(bb.replace("%", "").trim())/100.0;
					StringBuilder builder= new StringBuilder();
					builder.append(bssd).append(",")
						.append(applyBizDv).append(",")
						.append("RF_KRW_BU").append(",")
						.append(zz[0]).append(",")			//시나리오번호
						.append("M" + String.format("%04d", cnt)).append(",")
						.append(0.0).append(",")
						.append(0.0).append(",")
						.append(0.0).append(",")
						.append(0.0).append(",")
						.append(0.0).append(",")
						.append(fwdRate).append(",")
						.append(0.0).append(",")
						.append("ESG").append(",")
						.append(LocalDateTime.now());
					
					
					rst.add(builder.toString());
				}
				cnt =cnt+1;
			}
			
		}
				
		FileUtils.writeHeader(outPath,"#BASE_DATE, BIZ_APPLY_DV,IR_CURVE_ID, SCE_NO, MAT_CD, RF_RATE, LIQ_PREM, REF_YIELD, CRD_SPREAD, RISK_ADJ_RF_RATE, RISK_ADJ_RF_FORWARD_RATE, VOL,LAST_MODIFIEID_BY, LAST_UPDATE");
		try {
			logger.info("Current Thread 2:  {},{}", Thread.currentThread().getId(),Thread.currentThread().getName());
			Files.write(outPath, (Iterable<String>) rst.stream().map(s -> s.toString())::iterator,StandardOpenOption.APPEND);
		} catch (Exception e) {
			logger.error("Error in HW Scenario result writing : {}", e);
		}
	}
}
