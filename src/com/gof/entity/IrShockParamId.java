package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import com.gof.interfaces.EntityIdentifier;

public class IrShockParamId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 205371050298889931L;

	private String baseYymm; 
	private String irShockTyp;
	private String irCurveId;	
	private String paramTypCd;
	
	public IrShockParamId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getIrShockTyp() {
		return irShockTyp;
	}
	public void setIrShockTyp(String irShockTyp) {
		this.irShockTyp = irShockTyp;
	}
	public String getIrCurveId() {
		return irCurveId;
	}
	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}
	public String getParamTypCd() {
		return paramTypCd;
	}
	public void setParamTypCd(String paramTypCd) {
		this.paramTypCd = paramTypCd;
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
