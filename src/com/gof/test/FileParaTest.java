package com.gof.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.csvmapper.IrCurveHisMapper;
import com.gof.entity.IrCurveHis;

public class FileParaTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private static String output;
	private static Path dcntScePath = Paths.get("D:\\Dev\\FileAsync.csv");
	
	public static void main(String[] args) {
//		bbb();
//		ccc();
		ddd();
	}
	
	private static void aaa() {
		
		int poolSize = 5;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		List<Future<Double>> fuList = new ArrayList<Future<Double>>();
		
		
		output ="D:\\Dev\\ESG\\" ;
		Path irScePath = Paths.get(output + "IrSce_KRW_201712.csv");
		
		
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
//			rStream.forEach(s -> logger.info("aaa : {}", s));
			rStream.filter(s-> s.getSceNo().equals("1"))
//					.filter(s-> s.getMatCd().equals("M0001"))
					.filter(s-> s.getMatCd().startsWith("M01"))
					.forEach(s -> fuList.add(exe.submit(new FilePara(s.getBaseYymm()))));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			for(Future<Double> aa : fuList) {
				logger.info("rst : {}", aa.get());
			}
			
		} catch (Exception e) {
			logger.info("error : {}", e);
		}
	}
	private static void bbb() {
		
		int poolSize = 50;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		List<Future<Double>> fuList = new ArrayList<Future<Double>>();
		
		
		output ="D:\\Dev\\ESG\\" ;
		Path irScePath = Paths.get(output + "IrSce_KRW_201712.csv");
		
		
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
//			rStream.forEach(s -> logger.info("aaa : {}", s));
			rStream.filter(s-> s.getSceNo().equals("1"))
//					.filter(s-> s.getMatCd().equals("M0001"))
					.filter(s-> s.getMatCd().startsWith("M11"))
					.forEach(s -> fuList.add(exe.submit(new FilePara3(s))));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		try (Writer writer = new BufferedWriter(new FileWriter(dcntScePath.toFile(), true)))
		{
			for(Future<Double> aa : fuList) {
				logger.info("rst : {}", aa.get());
				writer.write(aa.get().toString());
				writer.write("\n");
			}
		} catch (Exception e) {
			logger.info("error : {}", e);
		}
	}
	private static void ddd() {
		
		int poolSize = 5;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		List<Future<IrCurveHis>> fuList = new ArrayList<Future<IrCurveHis>>();
		
		
		output ="D:\\Dev\\ESG\\" ;
		Path irScePath = Paths.get(output + "IrSce_KRW_201712.csv");
		
		
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
//			rStream.forEach(s -> logger.info("aaa : {}", s));
			rStream.filter(s-> s.getSceNo().equals("1"))
//					.filter(s-> s.getMatCd().equals("M0001"))
					.filter(s-> s.getMatCd().startsWith("M11"))
					.forEach(s -> fuList.add(exe.submit(new FilePara2(s))));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		try (Writer writer = new BufferedWriter(new FileWriter(dcntScePath.toFile(), true)))
		{
			for(Future<IrCurveHis> aa : fuList) {
				logger.info("rst : {}", aa.get());
				writer.write(aa.get().toString());
				writer.write("\n");
			}
		} catch (Exception e) {
			logger.info("error : {}", e);
		}
	}

	private static void ccc() {
		output ="D:\\Dev\\ESG\\" ;
		Path irScePath = Paths.get(output + "IrSce_KRW_201712.csv");
		
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
			rStream.filter(s-> s.getSceNo().equals("1"))
//					.filter(s-> s.getMatCd().equals("M0001"))
				    .filter(s-> s.getMatCd().startsWith("M01"))
				    .map(s -> new FilePara2(s))
				    .forEach(s -> logger.info("aaa : {}", s.getIrCurve()))
				    ;
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}


}
