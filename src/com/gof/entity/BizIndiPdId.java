package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class BizIndiPdId implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = 3190211902934726894L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name = "APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	
	@Column(name="CB_GRD_CD", nullable=false)
	private String cbGrdCd;
	
	@Column(name="MAT_CD", nullable=false)
	private String matCd;	

	@Column(name="CRD_EVAL_AGNCY_CD", nullable=false)
	private String crdEvalAgncyCd;
	
	public BizIndiPdId() {}

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

	public String getCbGrdCd() {
		return cbGrdCd;
	}

	public void setCbGrdCd(String cbGrdCd) {
		this.cbGrdCd = cbGrdCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getCrdEvalAgncyCd() {
		return crdEvalAgncyCd;
	}

	public void setCrdEvalAgncyCd(String crdEvalAgncyCd) {
		this.crdEvalAgncyCd = crdEvalAgncyCd;
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
		return "IndiCrdGrdCumPdId [baseYymm=" + baseYymm + ", cbGrdCd=" + cbGrdCd + ", matCd=" + matCd
				+ ", crdEvalAgncyCd=" + crdEvalAgncyCd + "]";
	}
		
}
