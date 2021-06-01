package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class DiscRateSceId implements Serializable {
	
	private static final long serialVersionUID = 7346541519373980852L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;	
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;
	
	@Column(name="DISC_RATE_CALC_TYP", nullable=false)
	private String discRateCalcTyp;
	
	@Column(name="SCE_NO", nullable=false)
	private String sceNo;
	
	@Column(name="MAT_CD", nullable=false)
	private String matCd;	
	
	public DiscRateSceId() {}

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

	public String getDiscRateCalcTyp() {
		return discRateCalcTyp;
	}

	public void setDiscRateCalcTyp(String discRateCalcTyp) {
		this.discRateCalcTyp = discRateCalcTyp;
	}

	public String getSceNo() {
		return sceNo;
	}

	public void setSceNo(String sceNo) {
		this.sceNo = sceNo;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
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
		return "DiscRateSceId [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", discRateCalcTyp="
				+ discRateCalcTyp + ", sceNo=" + sceNo + ", matCd=" + matCd + "]";
	}
	
}
