package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class IndiCrdGrdPdId implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -4339748831995686539L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;

	@Column(name="CRD_EVAL_AGNCY_CD", nullable=false)
	private String crdEvalAgncyCd;

	@Column(name="CB_GRD_CD", nullable=false)
	private String cbGrdCd;	
	
	public IndiCrdGrdPdId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getCrdEvalAgncyCd() {
		return crdEvalAgncyCd;
	}

	public void setCrdEvalAgncyCd(String crdEvalAgncyCd) {
		this.crdEvalAgncyCd = crdEvalAgncyCd;
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
		return "IndiCrdGrdPdId [baseYymm=" + baseYymm + ", crdEvalAgncyCd=" + crdEvalAgncyCd + ", cbGrdCd=" + cbGrdCd
				+ "]";
	}
	
}
