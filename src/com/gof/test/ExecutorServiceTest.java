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
import java.util.concurrent.Future;
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

public class ExecutorServiceTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
//	private static Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//	private static Session session = HibernateUtil.getSessionFactory().openSession();
			
	public static void main(String[] args) {
//		logger.info("session open 1 : {},{}", session.isOpen());
//		session.beginTransaction();
//		logger.info("session open 2 : {},{}", session.isOpen());
		
		int core = Runtime.getRuntime().availableProcessors();
		IntStream.range(0,5).forEach(s -> logger.info("ss :{}", s));
		logger.info("core : {},{}", core, Thread.currentThread().getName());
		double rst = 0.0;
		
		Double[] input = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,10.0,11.0, 12.0, 13.0, 14.0, 15.0};
		Stream<Double> in =Stream.of(input);

//		List<IrCurveHis> irCuve = getIrCurveHis("20171229", "A100");
//		logger.info("Size  : {},{}", irCuve.size());
		
		List<Reporter> rstMap = in.map(s-> new Reporter(s)).collect(Collectors.toList());
		
		int poolSize = 5;
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
			
		});
		
		/*for(Double zz : input) {
			exe.submit(new ReporterRunnable(zz));
//			exe.execute(new ReporterRunnable(zz));
		}*/

		List<Future<Double>> fuList = new ArrayList<Future<Double>>();
		
		for(final Reporter aa : rstMap) {
//			logger.info("put : {}", aa.getNumber());
			fuList.add(exe.submit(aa));
		}
		exe.shutdown();
		
		try {
			for(Future<Double> aa : fuList) {
				logger.info("rst : {}", aa.get());
			}
			
		} catch (Exception e) {
			logger.info("error : {}", e);
		}
//		session.getTransaction().commit();
	}
}
