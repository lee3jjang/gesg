package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class DiscRateHisId implements Serializable {	

	private static final long serialVersionUID = 8465878386389748580L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;	
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;
	
	@Column(name="MAT_CD", nullable=false)
	private String matCd;	
	
	@Column(name="ACCT_DV_CD", nullable=false)
	private String acctDvCd;
	
	public DiscRateHisId() {}

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

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
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
		return "DiscRateHisId [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", matCd=" + matCd + ", acctDvCd="
				+ acctDvCd + "]";
	}
	
}
