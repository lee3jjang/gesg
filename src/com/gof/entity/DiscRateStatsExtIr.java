package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


@Entity
@IdClass(DiscRateStatsExtIrId.class)
@Table( name ="EAS_DISC_RATE_STATS_EXT_IR")

public class DiscRateStatsExtIr implements Serializable {

	private static final long serialVersionUID = 6207498187147536428L;

	@Id
	private String baseYymm;	
	
	@Id
	private String discRateCalcTyp;
	
	@Id
	private String extIrCd;

	@Id
	private String indpVariable;
	
	private Double avgMonNum;
	
	private Double regrConstant;
	
	private Double regrCoef;
	
	private String remark;	
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	
	public DiscRateStatsExtIr() {}

	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getDiscRateCalcTyp() {
		return discRateCalcTyp;
	}

	public void setDiscRateCalcTyp(String discRateCalcTyp) {
		this.discRateCalcTyp = discRateCalcTyp;
	}

	public String getExtIrCd() {
		return extIrCd;
	}

	public void setExtIrCd(String extIrCd) {
		this.extIrCd = extIrCd;
	}

	public String getIndpVariable() {
		return indpVariable;
	}

	public void setIndpVariable(String indpVariable) {
		this.indpVariable = indpVariable;
	}

	public Double getAvgMonNum() {
		return avgMonNum;
	}

	public void setAvgMonNum(Double avgMonNum) {
		this.avgMonNum = avgMonNum;
	}

	public Double getRegrConstant() {
		return regrConstant;
	}

	public void setRegrConstant(Double regrConstant) {
		this.regrConstant = regrConstant;
	}

	public Double getRegrCoef() {
		return regrCoef;
	}

	public void setRegrCoef(Double regrCoef) {
		this.regrCoef = regrCoef;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public LocalDateTime getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
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


