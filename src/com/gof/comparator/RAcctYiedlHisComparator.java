package com.gof.comparator;

import java.util.Comparator;

import com.gof.entity.RAcctYieldHis;

public class RAcctYiedlHisComparator  implements Comparator<RAcctYieldHis>{

	@Override
	public int compare(RAcctYieldHis base, RAcctYieldHis other) {
		return base.getBaseYymm().compareTo(other.getBaseYymm());
	}
}
