package com.gof.comparator;

import java.util.Comparator;

import com.gof.entity.TransitionMatrix;

public class TmComparator  implements Comparator<TransitionMatrix>{

	@Override
	public int compare(TransitionMatrix base, TransitionMatrix other) {
		return 100* ( base.getFromGradeEnum().getOrder() - other.getFromGradeEnum().getOrder()) 
				 + ( base.getToGradeEnum().getOrder()  - other.getToGradeEnum().getOrder())
				 ;  
	}
}
