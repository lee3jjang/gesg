package com.gof.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.gof.model.CIR;
import com.gof.model.HullWhite;
import com.gof.model.Vasicek;

public enum EIrModelType {
	   MERTON    ("1")
	 , VASICEK 	 ("2")
	 , HOLEE 	 ("3")
	 , HULLWHITE ("4")
	 , CIR	     ("5")
	 , HW2       ("6")
;
	
	private String legacyCode;

	private EIrModelType(String legacy) {
		this.legacyCode = legacy;
	}

	public String getLegacyCode() {
		return legacyCode;
	}

	public static EIrModelType getEIrModelType(String legacyCode) {
		for(EIrModelType aa : EIrModelType.values()) {
			if(aa.getLegacyCode().equals(legacyCode)) {
				return aa;
			}
		}
		return HULLWHITE;
	}

}
