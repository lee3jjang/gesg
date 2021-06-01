package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


@Entity
@IdClass(BizDiscRateAdjUdId.class)
@Table( name ="EAS_USER_DISC_RATE_ADJ")
public class BizDiscRateAdjUd implements Serializable {

	private static final long serialVersionUID = 6207498187147536428L;

	@Id
	private String applStYymm;	
	
	@Id
	private String applBizDv;

	
	@Id
	private String intRateCd;
	
	
	private String applEdYymm;
	
	private Double applAdjRate;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public BizDiscRateAdjUd() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
	}


	public String getApplBizDv() {
		return applBizDv;
	}

	public void setApplBizDv(String applBizDv) {
		this.applBizDv = applBizDv;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public Double getApplAdjRate() {
		return applAdjRate;
	}

	public void setApplAdjRate(Double applAdjRate) {
		this.applAdjRate = applAdjRate;
	}

	public String getApplEdYymm() {
		return applEdYymm;
	}

	public void setApplEdYymm(String applEdYymm) {
		this.applEdYymm = applEdYymm;
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


