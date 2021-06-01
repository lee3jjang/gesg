package com.gof.test;

public class TestConverter implements ExtendedBiFun<Double, Double, Double>{

	@Override
	public Double apply(Double ratio, Double value) {
		return ratio *  ( value - 1.0) ;
		
	}
	

}
