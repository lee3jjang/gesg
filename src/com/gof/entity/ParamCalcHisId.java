package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class ParamCalcHisId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -5314286751496179811L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="IR_MODEL_TYP", nullable=false)
	private String irModelTyp;
	
	@Column(name="PARAM_CALC_CD", nullable=false)		
	private String paramCalcCd; 

	
	@Column(name="PARAM_TYP_CD", nullable=false)
	private String paramTypCd;

	@Column(name="MAT_CD", nullable=false)
	private String matCd;	
	
	public ParamCalcHisId() {}

	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getIrModelTyp() {
		return irModelTyp;
	}
	public void setIrModelTyp(String irModelTyp) {
		this.irModelTyp = irModelTyp;
	}
	public String getParamCalcCd() {
		return paramCalcCd;
	}
	public void setParamCalcCd(String paramCalcCd) {
		this.paramCalcCd = paramCalcCd;
	}
	public String getParamTypCd() {
		return paramTypCd;
	}
	public void setParamTypCd(String paramTypCd) {
		this.paramTypCd = paramTypCd;
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
