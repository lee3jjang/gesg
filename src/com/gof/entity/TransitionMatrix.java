package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.enums.ECreditGrade;

@Entity
@IdClass(TransitionMatrixId.class)
@Table(name ="EAS_USER_CORP_CRD_GRD_TM")
public class TransitionMatrix implements Serializable, Comparable<TransitionMatrix>{
	
	private String baseYyyy;
	private String fromGrade;
	private ECreditGrade fromGradeEnum;
	
	private String toGrade;
	private ECreditGrade toGradeEnum;
	private String tmType;
	private double tmRate;
	
	
	@Id
//	@Column(name = "BASE_YYYY") 
	public String getBaseYyyy() {
		return baseYyyy;
	}
	public void setBaseYyyy(String baseYyyy) {
		this.baseYyyy = baseYyyy;
	}
	
	@Id
	@Column(name = "FROM_CRD_GRD_CD") 
	public String getFromGrade() {
		return fromGrade;
	}
	
	public void setFromGrade(String fromGrade) {
		this.fromGrade = fromGrade;
	}
	
	@Transient
	public ECreditGrade getFromGradeEnum() {
		return ECreditGrade.getECreditGrade(fromGrade) ;
	}
	
	@Id
//	@Column(name = "TO_CRD_GRD_CD") 
	public String getToGrade() {
		return toGrade;
	}
	public void setToGrade(String toGrade) {
		this.toGrade = toGrade;
	}

	@Transient
	public ECreditGrade getToGradeEnum() {
		return ECreditGrade.getECreditGrade(toGrade) ;
	}

	@Id
//	@Column(name = "TM_TYPE") 
	public String getTmType() {
		return tmType;
	}
	public void setTmType(String tmType) {
		this.tmType = tmType;
	}
	
	@Column(name = "PROB_RATE")
	public double getTmRate() {
		return tmRate;
	}
	public void setTmRate(double tmRate) {
		this.tmRate = tmRate;
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
	public int compareTo(TransitionMatrix other) {
		return 10* ( this.getFromGradeEnum().getOrder() - other.getFromGradeEnum().getOrder()) 
				 + ( this.getToGradeEnum().getOrder()  - other.getToGradeEnum().getOrder())
				 ;  
		
	}

	
}
