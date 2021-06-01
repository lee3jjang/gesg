package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LiqPremiumId implements Serializable{
	
	private String baseYymm; 
	private String modelId;
	private String irCurveId;
	private String matCd;
	
	
	@Column(name ="BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	
	public String getIrCurveId() {
		return irCurveId;
	}
	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}
	@Column(name ="DCNT_APPL_MODEL_CD")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
	@Column(name ="MAT_CD")
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
