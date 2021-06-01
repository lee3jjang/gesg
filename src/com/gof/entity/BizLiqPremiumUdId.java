package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
public class BizLiqPremiumUdId implements Serializable{
	
	private String applyStartYymm;
	private String applyBizDv;
	private String irCurveId;
	private String matCd;
	
	@Column(name ="APPL_ST_YYMM")
	public String getApplyStartYymm() {
		return applyStartYymm;
	}
	public void setApplyStartYymm(String applyStartYymm) {
		this.applyStartYymm = applyStartYymm;
	}
	@Column(name ="APPL_BIZ_DV")
	public String getApplyBizDv() {
		return applyBizDv;
	}
	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}

	@Column(name ="IR_CURVE_ID")
	public String getIrCurveId() {
		return irCurveId;
	}
	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
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
