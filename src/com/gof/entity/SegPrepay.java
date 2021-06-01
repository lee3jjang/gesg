package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.interfaces.BaseValue;
import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(SegPrepayId.class)
@Table(name ="EAS_SEG_PREP_RATE")

public class SegPrepay implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 2360652480675748510L;

	@Id
	private String baseYymm;
	@Id
	private String segTypCd;
	@Id
	private String segId;
	
    private Double loanBal;
    private Double prepayAmt;
    private Double prepayRate;
    private Double prepayRateYr;
    
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
    
	public SegPrepay() {}

	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getSegTypCd() {
		return segTypCd;
	}
	public void setSegTypCd(String segTypCd) {
		this.segTypCd = segTypCd;
	}
	public String getSegId() {
		return segId;
	}
	public void setSegId(String segId) {
		this.segId = segId;
	}
	public Double getLoanBal() {
		return loanBal;
	}
	public void setLoanBal(Double loanBal) {
		this.loanBal = loanBal;
	}
	public Double getPrepayAmt() {
		return prepayAmt;
	}
	public void setPrepayAmt(Double prepayAmt) {
		this.prepayAmt = prepayAmt;
	}
	public Double getPrepayRate() {
		return prepayRate;
	}
	public void setPrepayRate(Double prepayRate) {
		this.prepayRate = prepayRate;
	}
	public Double getPrepayRateYr() {
		return prepayRateYr;
	}
	public void setPrepayRateYr(Double prepayRateYr) {
		this.prepayRateYr = prepayRateYr;
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
	public Double getBasicValue() {
		return prepayRateYr;
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


