package com.gof.test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.util.ScriptUtil;

public class RenjinTest {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	private final static ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<ScriptEngine>();
	
	public static void main(String[] args) {
		int core = Runtime.getRuntime().availableProcessors();
		Double[] input = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,10.0,11.0, 12.0, 13.0, 14.0, 15.0};
		Stream<Double> in =Stream.of(input);
//		int poolSize = Math.max(core, input.length);
		int poolSize = 5;
		
		ExecutorService exe = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				
				Thread t = new Thread(r);
//				System.out.println("thread Name : "+ t.getName());
				t.setDaemon(true);
				return t;
			}
		});
		
		bbb(in, exe).stream().forEachOrdered(s -> logger.info("kkk : {}", s));
		
//		for(Double zz: input) {
//			doAsync(zz);
//		}
	}
	

	private static List<Double> aaa(Stream<Double> in, ExecutorService exe){
		 List<CompletableFuture<Double>> rstFuture =
					in.map(aa -> CompletableFuture.supplyAsync(() -> doAsync(aa), exe))
//					in.map(aa -> CompletableFuture.supplyAsync(Test2::doAsync, exe))
					.collect(Collectors.toList());


		 return rstFuture.stream().map(CompletableFuture::join).collect(Collectors.toList());
	}
	
	private static List<String> bbb(Stream<Double> in, ExecutorService exe){
		 List<CompletableFuture<String>> rstFuture =
					in.map(aa -> CompletableFuture.supplyAsync(() -> doAsync1(aa), exe))
//					in.map(aa -> CompletableFuture.supplyAsync(Test2::doAsync, exe))
					.collect(Collectors.toList());


		 return rstFuture.stream().map(CompletableFuture::join).collect(Collectors.toList());
	}
	
	private static Double doAsync(Double in) {
		ScriptEngine engine = ENGINE.get();
		if(engine==null) {
			logger.info("factroy time : {}", System.nanoTime()/1000000000);
			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
			engine = factory.getScriptEngine();
//			logger.info("factroy time : {}", System.nanoTime()/1000000000);
			ENGINE.set(engine);
		}
		
//		logger.info("factroy time : {}", System.nanoTime()/1000000000);
//		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
//		ScriptEngine engine = factory.getScriptEngine();
//		logger.info("factroy time : {}", System.nanoTime()/1000000000);
		
		List<String> scriptString = ScriptUtil.getScriptContents();
		logger.info("engine start: {},{}", engine.get("maturity"), in);
		
		try {
			for(String aa : scriptString) {
				engine.eval( aa);
			}
			
			engine.put("maturity", "M00"+ in);
//			engine.put("maturity", 3);
			
			
			String script = "Mat.code.to.mat(maturity)";
//			String script = "Mat.to.mat.code(maturity)";
//			String script = "e.date(base_dt, length.month)";

			logger.info("engine : {},{}", engine.get("maturity"), Thread.currentThread().getName());
//			logger.info("engine : {},{}", engine.getContext().getAttribute("maturity"), Thread.currentThread().getName());
			logger.info("factroy time1 : {}", System.nanoTime()/1000000000);
			try {
				Thread.sleep(20000);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			logger.info("factroy time1 : {}", System.nanoTime()/1000000000);
			SEXP rst = (SEXP)engine.eval(script);
//			
			logger.error("Renjin aaa : {},{}", rst, rst.getElementAsSEXP(0).asReal());
			
//			return rst.getElementAsSEXP(0).asString();
			return rst.getElementAsSEXP(0).asReal();
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return 1.0;
		
	}
	
	
	private static String doAsync1(Double in) {
		ScriptEngine engine = ENGINE.get();
		if(engine==null) {
			logger.info("factroy time : {}", System.nanoTime()/1000000000);
			RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
			engine = factory.getScriptEngine();
//			logger.info("factroy time : {}", System.nanoTime()/1000000000);
			ENGINE.set(engine);
		}
		try {
			Thread.sleep(2000);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
//		logger.info("factroy time : {}", System.nanoTime()/1000000000);
//		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();		
//		ScriptEngine engine = factory.getScriptEngine();
//		logger.info("factroy time : {}", System.nanoTime()/1000000000);
		
		List<String> scriptString = ScriptUtil.getScriptContents();
		logger.info("engine start: {},{}", engine.get("maturity"), in);
		
		try {
			for(String aa : scriptString) {
				engine.eval( aa);
			}
			
			engine.put("maturity", in);
			
			
			String script = "Mat.to.mat.code(maturity)";

			logger.info("engine : {},{}", engine.get("maturity"), Thread.currentThread().getName());
//			logger.info("engine : {},{}", engine.getContext().getAttribute("maturity"), Thread.currentThread().getName());
			logger.info("factroy time1 : {}", System.nanoTime()/1000000000);
			try {
				Thread.sleep(2000);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			logger.info("factroy time1 : {}", System.nanoTime()/1000000000);
			SEXP rst = (SEXP)engine.eval(script);
//			
			logger.error("Renjin aaa : {},{}", rst, rst.getElementAsSEXP(0).asString());
			
//			return rst.getElementAsSEXP(0).asString();
			return rst.getElementAsSEXP(0).asString();
		} catch (Exception e) {
			logger.error("Renjin Error : {}", e);
		}
		return "";
		
	}
}
