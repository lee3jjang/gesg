package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class IndiCrdGrdPdUdId implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 5828848745084355867L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;

	@Column(name="CB_GRD_CD", nullable=false)
	private String cbGrdCd;	
	
	public IndiCrdGrdPdUdId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getCbGrdCd() {
		return cbGrdCd;
	}

	public void setCbGrdCd(String cbGrdCd) {
		this.cbGrdCd = cbGrdCd;
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
		return "IndiCrdGrdPdUdId [baseYymm=" + baseYymm + ", cbGrdCd=" + cbGrdCd + "]";
	}
	
}
