package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class BizInflationId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -9072807206431485429L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	
	@Column(name="INFLATION_ID", nullable=false)
	private String inflationId;	
	
	public BizInflationId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getApplyBizDv() {
		return applyBizDv;
	}

	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}

	public String getInflationId() {
		return inflationId;
	}

	public void setInflationId(String inflationId) {
		this.inflationId = inflationId;
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
		return "InflationId [baseYymm=" + baseYymm + ", inflationId=" + inflationId + "]";
	}
		
}
