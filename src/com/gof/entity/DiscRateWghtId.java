package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class DiscRateWghtId implements Serializable {

	private static final long serialVersionUID = 8207684050620145464L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;
	
	public DiscRateWghtId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "DiscRateWghtUdId [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + "]";
	}	
	
}
