package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@IdClass(SwaptionVolId.class)
@Table(name ="EAS_SWAPTION_VOL")
@FilterDef(name="eqBaseDate", parameters= @ParamDef(name ="bssd",  type="string"))
public class SwaptionVol implements Serializable{
	
	private String baseYymm; 
	private Double swaptionMaturity;
	private Double swapTenor;
	
	private Double vol;
	
	@Id
	@Column(name ="BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	
	@Id
	@Column(name ="SWAPTION_MATURITY")
	public Double getSwaptionMaturity() {
		return swaptionMaturity;
	}
	public void setSwaptionMaturity(Double swaptionMaturity) {
		this.swaptionMaturity = swaptionMaturity;
	}
	@Id
	@Column(name ="SWAP_TENOR")
	public Double getSwapTenor() {
		return swapTenor;
	}
	public void setSwapTenor(Double swapTenor) {
		this.swapTenor = swapTenor;
	}
	@Column(name ="VOL")
	public Double getVol() {
		return vol;
	}
	public void setVol(Double vol) {
		this.vol = vol;
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
			   .append(swaptionMaturity).append(delimeter)
			   .append(swapTenor).append(delimeter)
			   .append(vol).append(delimeter)
			   ;
		return builder.toString();
	}
	
}
