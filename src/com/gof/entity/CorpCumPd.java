package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(CorpCumPdId.class)
@Table(name ="EAS_CORP_CRD_GRD_CUM_PD")
public class CorpCumPd implements Serializable{
	
	private String baseYymm;
	private String agencyCode;
	private String gradeCode;
	private String matCd;

	private Double cumPd;
	private Double fwdPd;
	private Double vol;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdated;
	
	@Id
	@Column(name = "BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	@Id
	@Column(name = "CRD_EVAL_AGNCY_CD")
	public String getAgencyCode() {
		return agencyCode;
	}
	public void setAgencyCode(String agencyCode) {
		this.agencyCode = agencyCode;
	}
	@Id
	@Column(name = "CRD_GRD_CD")
	public String getGradeCode() {
		return gradeCode;
	}
	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
	}
	@Id
	@Column(name = "MAT_CD")
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}
	
	public Double getCumPd() {
		return cumPd;
	}
	public void setCumPd(Double cumPd) {
		this.cumPd = cumPd;
	}
	public Double getFwdPd() {
		return fwdPd;
	}
	public void setFwdPd(Double fwdPd) {
		this.fwdPd = fwdPd;
	}

	public Double getVol() {
		return vol;
	}
	public void setVol(Double vol) {
		this.vol = vol;
	}
	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	

	@Column(name ="LAST_UPDATE_DATE")
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
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
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(agencyCode).append(delimeter)
			   .append(gradeCode).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(cumPd).append(delimeter)
			   .append(fwdPd).append(delimeter)
			   .append(vol)
			   ;
		
		return builder.toString();
	}
		
}
