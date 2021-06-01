package com.gof.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ERunArgument {
	  time ( "TIME")
//	  , t( "TIME")
//	, job ( "JOB")
	, properties ( "PROPERTIES")
//	, p ("PROPERTIES")
	;
	
	
	private String alias;

	private ERunArgument(String alias) {
		this.alias = alias;
	}
	
	


	
}
