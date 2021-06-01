package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class UserDiscRateAsstRevnCumRateId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 2616154384849208138L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;	
	
	@Column(name="ACCT_DV_CD", nullable=false)
	private String acctDvCd;	
	
	public UserDiscRateAsstRevnCumRateId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getAcctDvCd() {
		return acctDvCd;
	}

	public void setAcctDvCd(String acctDvCd) {
		this.acctDvCd = acctDvCd;
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
		return "UserDiscRateAsstRevnCumRateId [baseYymm=" + baseYymm + ", acctDvCd=" + acctDvCd + "]";
	}	
	
}
