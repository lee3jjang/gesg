package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;


public class BizCorpPdId implements Serializable{

	private String baseYymm;
	private String applyBizDv;
	private String crdGrdCd;
	private String matCd;

	@Column(name = "BASE_YYMM") 
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	@Column(name = "APPL_BIZ_DV")
	public String getApplyBizDv() {
		return applyBizDv;
	}
	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}
	
	@Column(name = "CRD_GRD_CD")
	public String getCrdGrdCd() {
		return crdGrdCd;
	}
	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}
	
	@Column(name = "MAT_CD")
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
