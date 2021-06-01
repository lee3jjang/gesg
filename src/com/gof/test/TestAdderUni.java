package com.gof.test;

import java.util.function.Function;

public class TestAdderUni implements Function<Double, Double>{

	@Override
	public Double apply(Double b) {
		return b + 2 ;
		
	}
	

}
