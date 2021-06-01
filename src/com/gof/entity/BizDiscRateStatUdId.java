package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;


@Embeddable
public class BizDiscRateStatUdId implements Serializable {

	private static final long serialVersionUID = -8075451385628396380L;

	@Column(name="APPL_ST_YYMM", nullable=false)
	private String applyStartYymm;
	
	@Column(name = "APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;

	@Column(name="INDP_VARIABLE", nullable=false)
	private String indpVariable;
	
	@Column(name="DEPN_VARIABLE", nullable=false)
	private String depnVariable;
	
	public BizDiscRateStatUdId() {}	

	
	public String getApplyStartYymm() {
		return applyStartYymm;
	}

	public void setApplyStartYymm(String applyStartYymm) {
		this.applyStartYymm = applyStartYymm;
	}

	public String getApplyBizDv() {
		return applyBizDv;
	}

	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public String getIndpVariable() {
		return indpVariable;
	}

	public void setIndpVariable(String indpVariable) {
		this.indpVariable = indpVariable;
	}
	

	public String getDepnVariable() {
		return depnVariable;
	}

	public void setDepnVariable(String depnVariable) {
		this.depnVariable = depnVariable;
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

//	@Override
//	public String toString() {
//		return "DiscRateId [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", discRateCalcTyp=" + discRateCalcTyp
//				+ ", matCd=" + matCd + "]";
//	}	
	
}
