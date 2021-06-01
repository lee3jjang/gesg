package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;


public class TransitionMatrixId implements Serializable{
	
	private String baseYyyy;
	private String fromGrade;
	private String toGrade;
	private String tmType;

	
	
	@Column(name = "BASE_YYYY") 
	public String getBaseYyyy() {
		return baseYyyy;
	}
	public void setBaseYyyy(String baseYyyy) {
		this.baseYyyy = baseYyyy;
	}
	
	
	@Column(name = "FROM_CRD_GRD_CD") 
	public String getFromGrade() {
		return fromGrade;
	}
	public void setFromGrade(String fromGrade) {
		this.fromGrade = fromGrade;
	}
	
	@Column(name = "TO_CRD_GRD_CD") 
	public String getToGrade() {
		return toGrade;
	}
	
	public void setToGrade(String toGrade) {
		this.toGrade = toGrade;
	}
	@Column(name = "TM_TYPE") 
	public String getTmType() {
		return tmType;
	}
	public void setTmType(String tmType) {
		this.tmType = tmType;
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
