package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(ParamCalcHisId.class)
@Table( name ="EAS_PARAM_CALC_HIS")
@FilterDef(name="FILTER", parameters= { @ParamDef(name="baseYymm", type="string") })
@Filters( { @Filter(name ="FILTER", condition="BASE_YYMM = :baseYymm") } )
public class ParamCalcHis implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -3199922647182076353L;

	@Id
	private String baseYymm;
	
	@Id	
	private String irModelTyp;
	
	@Id	
	private String paramCalcCd; 

	@Id
	private String paramTypCd;

	@Id
	private String matCd;	
	
	private Double paramVal;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public ParamCalcHis() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getIrModelTyp() {
		return irModelTyp;
	}

	public void setIrModelTyp(String irModelTyp) {
		this.irModelTyp = irModelTyp;
	}

	public String getParamCalcCd() {
		return paramCalcCd;
	}

	public void setParamCalcCd(String paramCalcCd) {
		this.paramCalcCd = paramCalcCd;
	}

	public String getParamTypCd() {
		return paramTypCd;
	}

	public void setParamTypCd(String paramTypCd) {
		this.paramTypCd = paramTypCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getParamVal() {
		return paramVal;
	}

	public void setParamVal(Double paramVal) {
		this.paramVal = paramVal;
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


