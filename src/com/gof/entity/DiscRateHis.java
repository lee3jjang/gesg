package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


@Entity
@IdClass(DiscRateHisId.class)
@Table(name ="EAS_DISC_RATE_HIS")
@FilterDef(name="discRateHisEqBaseYymm", parameters= { @ParamDef(name="baseYymm",  type="string")} )

public class DiscRateHis implements Serializable {

	private static final long serialVersionUID = 5044731109927550489L;

	@Id
	private String baseYymm;	
	
	@Id
	private String intRateCd;
	
	@Id
	private String matCd;	
	
	@Id
	private String acctDvCd;

	private Double mgtAsstYield;
	
	private Double exBaseIr;
	
	private Double discRateSpread;
	
	private Double exBaseIrWght;
	
	private Double baseDiscRate;
	
	private Double discRateAdjRate;
	
	private Double applDiscRate;
	
//	@Transient
	@ManyToOne
	@JoinColumn(name ="INT_RATE_CD", insertable=false, updatable= false)
	private DiscRateCalcSetting discSetting;
	
	public DiscRateHis() {
		this.discRateAdjRate =1.0;
	}

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

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getAcctDvCd() {
		return acctDvCd;
	}

	public void setAcctDvCd(String acctDvCd) {
		this.acctDvCd = acctDvCd;
	}

	public Double getMgtAsstYield() {
		return mgtAsstYield;
	}

	public void setMgtAsstYield(Double mgtAsstYield) {
		this.mgtAsstYield = mgtAsstYield;
	}

	public Double getExBaseIr() {
		return exBaseIr;
	}

	public void setExBaseIr(Double exBaseIr) {
		this.exBaseIr = exBaseIr;
	}

	public Double getDiscRateSpread() {
		return discRateSpread;
	}

	public void setDiscRateSpread(Double discRateSpread) {
		this.discRateSpread = discRateSpread;
	}

	public Double getExBaseIrWght() {
		return exBaseIrWght;
	}

	public void setExBaseIrWght(Double exBaseIrWght) {
		this.exBaseIrWght = exBaseIrWght;
	}

	public Double getBaseDiscRate() {
		return baseDiscRate;
	}

	public void setBaseDiscRate(Double baseDiscRate) {
		this.baseDiscRate = baseDiscRate;
	}

	public Double getDiscRateAdjRate() {
		return discRateAdjRate;
	}

	public void setDiscRateAdjRate(Double discRateAdjRate) {
		this.discRateAdjRate = discRateAdjRate;
	}

	public Double getApplDiscRate() {
		return applDiscRate;
	}

	public void setApplDiscRate(Double applDiscRate) {
		this.applDiscRate = applDiscRate;
	}


	public DiscRateCalcSetting getDiscSetting() {
		return discSetting;
	}

	public void setDiscSetting(DiscRateCalcSetting discSetting) {
		this.discSetting = discSetting;
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
		return "DiscRateHis [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", matCd=" + matCd + ", acctDvCd="
				+ acctDvCd + ", mgtAsstYield=" + mgtAsstYield + ", exBaseIr=" + exBaseIr + ", discRateSpread="
				+ discRateSpread + ", exBaseIrWght=" + exBaseIrWght + ", baseDiscRate=" + baseDiscRate
				+ ", discRateAdjRate=" + discRateAdjRate + ", applDiscRate=" + applDiscRate + "]";
	}	
	
}


