package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class CashFlowMatchAdjId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -7880088681747598811L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;

	@Column(name="MAT_CD", nullable=false)
	private String matCd;
	
	public CashFlowMatchAdjId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
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
		return "AsstYieldId [baseYymm=" + baseYymm  ; 
	}
		
}
