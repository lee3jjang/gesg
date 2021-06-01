package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;


@Entity
@IdClass(DiscRateSpreadId.class)
@Table(name ="EAS_DISC_RATE_SPREAD")
@FilterDef(name="FILTER", parameters= { @ParamDef(name="baseYymm", type="string") })
@Filters( { @Filter(name ="FILTER", condition="BASE_YYMM = :baseYymm") } )
public class DiscRateSpread implements Serializable {

	private static final long serialVersionUID = -348383584338312083L;
	
	@Id
	private String baseYymm;	
	
	@Id
	private String intRateCd;		

	private Double discRateSpread;	
	
	public DiscRateSpread() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public Double getDiscRateSpread() {
		return discRateSpread;
	}

	public void setDiscRateSpread(Double discRateSpread) {
		this.discRateSpread = discRateSpread;
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
		return "DiscRateSpread [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", discRateSpread="
				+ discRateSpread + "]";
	}
	
}


