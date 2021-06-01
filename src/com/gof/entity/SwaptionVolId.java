package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SwaptionVolId implements Serializable{
	
	
	private String baseYymm; 
	private Double swaptionMaturity;
	private Double swapTenor;
	
	@Column(name ="BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	

	@Column(name ="SWAPTION_MATURITY")
	public Double getSwaptionMaturity() {
		return swaptionMaturity;
	}
	public void setSwaptionMaturity(Double swaptionMaturity) {
		this.swaptionMaturity = swaptionMaturity;
	}

	@Column(name ="SWAP_TENOR")
	public Double getSwapTenor() {
		return swapTenor;
	}
	public void setSwapTenor(Double swapTenor) {
		this.swapTenor = swapTenor;
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
