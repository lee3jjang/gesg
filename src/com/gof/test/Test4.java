package com.gof.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test4 {
	private final static Logger logger = LoggerFactory.getLogger("DAO");
	
	public static void main(String[] args) {
		TestConverter converter = new TestConverter();
		TestAdder adder = new TestAdder();
		TestAdderUni adderUni = new TestAdderUni();
		
		double add = 2.0;
		double ratio = 1.5;
		double value = 3.0;
		
//		f(x,y) = x* (y-1)
		logger.info("aa :  {}" , converter.apply(ratio, value) );
		
		logger.info("aa :  {}" , converter.apply(1.5, 3.0) );
		
//		g(y) = f(ratio,y) = ratio * (y-1)		
		logger.info("aa :  {}" , converter.fixedX(ratio).apply( 3.0) );
		
		
//		h(x) = f(x, value) = x * (value-1)
		logger.info("curry2 :  {}" , converter.fixedY(value).apply(2.0) );
		
//		 f(x, y) + 1
		logger.info("andTen :  {}" , converter.andThen(n -> n+1).apply(1.5, 3.0));
		
//		(g o h)(y) = f(ratio,h(y)) = ratio * (h(y)-1)   h(z) = z+1 
		logger.info("compose :  {}" , converter.fixedX(ratio).compose((Double n) -> n+1).apply(2.0));

		
//		(g o h)(y) = f(ratio,h(y)) = ratio * (h(y)-1)   h(z) = z+1 
		logger.info("compose :  {}" , converter.composeY((Double n) -> n+1).fixedX(ratio).apply(2.0));
		
		logger.info("compose :  {}" , converter.composeY(adder.fixedY(2.0)).apply(1.5, 2.0));
		
//		logger.info("compose :  {}" , converter.composeY(adder.fixedX(add)).apply(ratio, value));
		
		logger.info("compose3 :  {}" , converter.apply(ratio, adder.apply(add,value)));
		
		
		logger.info("compose4:  {}" , converter.composeY(adderUni).apply(1.5, 2.0));
		
		logger.info("compose5 :  {}" , converter.composeY(new TestAdderUni()).apply(1.5, 2.0));
		
		

	}

}
