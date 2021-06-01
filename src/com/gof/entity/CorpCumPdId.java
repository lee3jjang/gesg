package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;


public class CorpCumPdId implements Serializable{

	private String baseYymm;
	private String agencyCode;
	private String gradeCode;
	private String matCd;

	@Column(name = "BASE_YYMM") 
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	@Column(name = "CRD_EVAL_AGNCY_CD")
	public String getAgencyCode() {
		return agencyCode;
	}
	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	@Column(name = "CRD_GRD_CD")
	public String getGradeCode() {
		return gradeCode;
	}
	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
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
