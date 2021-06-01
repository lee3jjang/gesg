package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class CorpCrdGrdPdId implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 7758100265355791143L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;

	@Column(name="CRD_EVAL_AGNCY_CD", nullable=false)
	private String crdEvalAgncyCd;

	@Column(name="CRD_GRD_CD", nullable=false)
	private String crdGrdCd;	
	
	public CorpCrdGrdPdId() {}

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

	public String getCrdGrdCd() {
		return crdGrdCd;
	}

	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
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
		return "CorpCrdGrdPdId [baseYymm=" + baseYymm + ", crdEvalAgncyCd=" + crdEvalAgncyCd + ", crdGrdCd=" + crdGrdCd
				+ "]";
	}
		
}
