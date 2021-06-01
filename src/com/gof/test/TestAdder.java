package com.gof.test;

public class TestAdder implements ExtendedBiFun<Double, Double, Double>{

	@Override
	public Double apply(Double a, Double b) {
		return a+b;
		
	}
	

}
