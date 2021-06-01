package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class DiscRateStatsId implements Serializable {	

	private static final long serialVersionUID = 8896041712907223964L;

	@Column(name="APPL_ST_YYMM", nullable=false)
	private String applStYymm;	
	
	@Column(name="DISC_RATE_CALC_TYP", nullable=false)
	private String discRateCalcTyp;
	
	@Column(name="INT_RATE_CD", nullable=false)
	private String intRateCd;

	@Column(name="DEPN_VARIABLE", nullable=false)
	private String depnVariable;

	@Column(name="INDP_VARIABLE", nullable=false)
	private String indpVariable;
	
	public DiscRateStatsId() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
	}

	public String getDiscRateCalcTyp() {
		return discRateCalcTyp;
	}

	public void setDiscRateCalcTyp(String discRateCalcTyp) {
		this.discRateCalcTyp = discRateCalcTyp;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public String getDepnVariable() {
		return depnVariable;
	}

	public void setDepnVariable(String depnVariable) {
		this.depnVariable = depnVariable;
	}

	public String getIndpVariable() {
		return indpVariable;
	}

	public void setIndpVariable(String indpVariable) {
		this.indpVariable = indpVariable;
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
		return "DiscRateStatsId [applStYymm=" + applStYymm + ", discRateCalcTyp=" + discRateCalcTyp + ", intRateCd="
				+ intRateCd + ", depnVariable=" + depnVariable + ", indpVariable=" + indpVariable + "]";
	}
	
	

	
}
