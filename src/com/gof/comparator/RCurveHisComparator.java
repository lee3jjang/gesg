package com.gof.comparator;

import java.util.Comparator;

import com.gof.entity.RCurveHis;

public class RCurveHisComparator  implements Comparator<RCurveHis>{

	@Override
	public int compare(RCurveHis base, RCurveHis other) {
		return base.getBaseYymm().compareTo(other.getBaseYymm());
	}
}
