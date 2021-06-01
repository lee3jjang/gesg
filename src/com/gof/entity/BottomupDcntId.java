package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class BottomupDcntId implements Serializable {

	private static final long serialVersionUID = 4763422431886069365L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="IR_CURVE_ID", nullable=false)
	private String irCurveId;
	
	@Column(name="MAT_CD", nullable=false)
	private String matCd;	
	
	public BottomupDcntId() {}

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
		return "BottomupDcntId [baseYymm=" + baseYymm + ", irCurveId=" + irCurveId + ", matCd=" + matCd + "]";
	}
		
}
