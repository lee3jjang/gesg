package com.gof.test;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.ejml.equation.IntegerSequence.For;
import org.hibernate.Session;
import org.renjin.repackaged.guava.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.csvmapper.IrCurveHisMapper;
import com.gof.dao.IrCurveHisDao;
import com.gof.entity.DcntSce;
import com.gof.entity.IrCurveHis;
import com.gof.entity.ParamCalcHis;
import com.gof.process.Job11_EsgParameter;
import com.gof.process.Job23_BottomUpScenario;
import com.gof.util.FileUtils;
import com.gof.util.HibernateUtil;

public class CompleteFutureFileTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			
	public static void main(String[] args) {
		zzz();
		xxx();
	}
	private static void zzz() {
		int poolSize = 5000;
//		int poolSize = 5;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		String output ="D:\\Dev\\ESG\\" ;
		Path irScePath = Paths.get(output + "IrSce_KRW_201712.csv");
		
		List<CompletableFuture<IrCurveHis>> sceJobFutures = new ArrayList<>();
		CompletableFuture<IrCurveHis> temp;
		
		logger.info("aaa");
		try (Stream<IrCurveHis> rStream = IrCurveHisMapper.readEntityNoHeader(irScePath)) {
			sceJobFutures = rStream
								.filter(s-> s.getSceNo().equals("1"))
//								.filter(s-> s.getMatCd().equals("M0001"))
//								.filter(s-> s.getMatCd().startsWith("M11"))
								.map(entry -> CompletableFuture.supplyAsync(() -> new FilePara2(entry).getIrCurve(), exe))
								.collect(Collectors.toList());
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		List<IrCurveHis> rst = sceJobFutures.stream().map(CompletableFuture::join)
//				  				.flatMap(s ->s.stream())
				  				.collect(Collectors.toList());
		
		rst.forEach(s -> logger.info("aaa : {}", s));
		
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}

//		session.getTransaction().commit();
	}
	
	
	private static void xxx() {
		int poolSize = 5000;
//		int poolSize = 5;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		
		Stream<IrCurveHis> rStream = IrCurveHisDao.getIrCurveHis("201712").stream();
		List<CompletableFuture<IrCurveHis>> sceJobFutures =	rStream
//								.filter(s-> s.getSceNo().equals("1"))
//								.filter(s-> s.getMatCd().equals("M0001"))
//								.filter(s-> s.getMatCd().startsWith("M11"))
								.map(entry -> CompletableFuture.supplyAsync(() -> new FilePara2(entry).getIrCurve(), exe))
								.collect(Collectors.toList());
		
		List<IrCurveHis> rst = sceJobFutures.stream().map(CompletableFuture::join)
//				  				.flatMap(s ->s.stream())
				  				.collect(Collectors.toList());
		
		rst.forEach(s -> logger.info("aaa : {}", s));
		
//		session.getTransaction().commit();
	}
}
