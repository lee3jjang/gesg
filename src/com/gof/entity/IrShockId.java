package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import com.gof.interfaces.EntityIdentifier;

public class IrShockId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = 205371050298889931L;

	private String baseYymm; 
	private String irShockTyp;
	private String irCurveId;	
	private String shockTypCd;
	private String matCd;	
	
	
	public IrShockId() {}



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



	public String getShockTypCd() {
		return shockTypCd;
	}



	public void setShockTypCd(String shockTypCd) {
		this.shockTypCd = shockTypCd;
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
			
}
