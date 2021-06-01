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

import com.gof.entity.IrCurveHis;
import com.gof.entity.ParamCalcHis;
import com.gof.process.Job11_EsgParameter;
import com.gof.util.FileUtils;
import com.gof.util.HibernateUtil;

public class CompleteFutureTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
			
	public static void main(String[] args) {
		int core = Runtime.getRuntime().availableProcessors();
		logger.info("core : {},{}", core, Thread.currentThread().getName());
		zzz();
	}
	private static void zzz() {
//		session.beginTransaction();
		
		int core = Runtime.getRuntime().availableProcessors();
		
		IntStream.range(0,5).forEach(s -> logger.info("ss :{}", s));

		logger.info("core : {},{}", core, Thread.currentThread().getName());
		double rst = 0.0;
		
		Double[] input = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,10.0,11.0, 12.0, 13.0, 14.0, 15.0};
		
		Stream<Double> in =Stream.of(input);

//		List<IrCurveHis> irCuve = getIrCurveHis("20171229", "A100");
//		logger.info("Size  : {},{}", irCuve.size());
		
//		List<ReporterNormal> rstMap = in.map(s-> new ReporterNormal(s)).collect(Collectors.toList());
		
//		int poolSize = Math.max(core, input.length);
		int poolSize = 5;
		
//		ExecutorService exe = Executors.newFixedThreadPool(poolSize);
		
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				
//				Thread t = new Thread(r,"Me_%d");
				Thread t = new Thread(r);
//				System.out.println("1.thread Name : "+ t.getName());
				t.setDaemon(true);
				return t;
			}
		});
		
		/*CompletableFuture<Double> aaa = CompletableFuture.supplyAsync(()-> new Reporter(1).getNumber(), exe);
		CompletableFuture<Double> bbb = CompletableFuture.supplyAsync(()-> new Reporter(2).getNumber(), exe);
		
		List<CompletableFuture<Double>> futureList = new ArrayList<CompletableFuture<Double>>();
		futureList.add(aaa);
		futureList.add(bbb);
		
		futureList.stream().map(CompletableFuture::join).forEach(s -> logger.info("aaa : {}", s ));*/
		
		CompletableFuture<Double> aaa = CompletableFuture.supplyAsync(()-> new ReporterNormal(1).getNumber(), exe);
		
		
		try {
			CompletableFuture<Double> bbb = CompletableFuture.supplyAsync(()-> new ReporterNormal(2).getNumber(), exe);
			logger.info("zzz : {}", aaa.thenCombine(bbb, (s,t)-> s+t).get());
			
			CompletableFuture<Double> ccc = aaa.thenComposeAsync(s -> CompletableFuture.supplyAsync(()-> new ReporterNormal(s+3).getNumber()));
			
			/*try {
					Thread.sleep(1000);
			} catch (Exception e) {
				// TODO: handle exception
			}*/
			logger.info("ccc : {}");
			
//			logger.info("bbb : {}", aaa.thenComposeAsync(s -> CompletableFuture.supplyAsync(()-> new Reporter(s+3).getNumber())).get(), exe); 
			logger.info("bbb : {}", ccc.get());
		} catch (Exception e) {
			// TODO: handle exception
		}

//		session.getTransaction().commit();
	}
	
}
