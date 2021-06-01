package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="EAS_IR_SHOCK_PARAM")
@Access(AccessType.FIELD)
public class IrShockParam implements Serializable {
	
	private static final long serialVersionUID = -6565657307170343742L;
	
	@Id
	private String baseYymm; 
	
	@Id
	private String irShockTyp;
	
	@Id
	private String irCurveId;
	
	@Id
	private String paramTypCd;
	
	private Double paramVal;
	
	private String lastModifiedBy;

	private LocalDateTime lastUpdateDate;	
	
	public IrShockParam() {}

	
	public String getBaseYymm() {
		return baseYymm;
	}


	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}


	public String getIrShockTyp() {
		return irShockTyp;
	}


	public void setIrShockTyp(String irShockTyp) {
		this.irShockTyp = irShockTyp;
	}


	public String getIrCurveId() {
		return irCurveId;
	}


	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}


	public String getParamTypCd() {
		return paramTypCd;
	}


	public void setParamTypCd(String paramTypCd) {
		this.paramTypCd = paramTypCd;
	}


	public Double getParamVal() {
		return paramVal;
	}


	public void setParamVal(Double paramVal) {
		this.paramVal = paramVal;
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
