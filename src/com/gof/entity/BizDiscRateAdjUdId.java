package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class BizDiscRateAdjUdId implements Serializable {	

	private static final long serialVersionUID = 8896041712907223964L;

	@Column(name="APPL_ST_YYMM", nullable=false)
	private String applStYymm;	
	
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applBizDv;
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;
	

	
	public BizDiscRateAdjUdId() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
	}

	public String getApplBizDv() {
		return applBizDv;
	}

	public void setApplBizDv(String applBizDv) {
		this.applBizDv = applBizDv;
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
}
