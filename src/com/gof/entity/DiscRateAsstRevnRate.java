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
@IdClass(DiscRateAsstRevnRateId.class)
@Table(name ="EAS_DISC_RATE_ASST_REVN_RATE")
@FilterDef(name="FILTER", parameters= { @ParamDef(name="baseYymm", type="string") })
@Filters( { @Filter(name ="FILTER", condition="BASE_YYMM = :baseYymm") } )
public class DiscRateAsstRevnRate implements Serializable {

	private static final long serialVersionUID = 1134677128445417422L;
	
	@Id
	private String baseYymm;	
	
	@Id
	private String acctDvCd;
	
	private Double mgtAsstAmt;
	
	private Double invRevnAmt;
	
	private Double ociAssetEvalPl;
	
	private Double ociRelCorpStkEvalPl;
	
	private Double ociFxRevalPl;
	
	private Double ociRevalRevnAmt;
	
	private Double ociOtherAmt;
	
	private Double unrealizedPlSumAmt;
	
	private Double mgtAsstRevnRate;
	
	private Double invCostAmt;
	
	private Double invCostRate;
	
	private Double mgtAsstYield;
	
	public DiscRateAsstRevnRate() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getAcctDvCd() {
		return acctDvCd;
	}

	public void setAcctDvCd(String acctDvCd) {
		this.acctDvCd = acctDvCd;
	}

	public Double getMgtAsstAmt() {
		return mgtAsstAmt;
	}

	public void setMgtAsstAmt(Double mgtAsstAmt) {
		this.mgtAsstAmt = mgtAsstAmt;
	}

	public Double getInvRevnAmt() {
		return invRevnAmt;
	}

	public void setInvRevnAmt(Double invRevnAmt) {
		this.invRevnAmt = invRevnAmt;
	}

	public Double getOciAssetEvalPl() {
		return ociAssetEvalPl;
	}

	public void setOciAssetEvalPl(Double ociAssetEvalPl) {
		this.ociAssetEvalPl = ociAssetEvalPl;
	}

	public Double getOciRelCorpStkEvalPl() {
		return ociRelCorpStkEvalPl;
	}

	public void setOciRelCorpStkEvalPl(Double ociRelCorpStkEvalPl) {
		this.ociRelCorpStkEvalPl = ociRelCorpStkEvalPl;
	}

	public Double getOciFxRevalPl() {
		return ociFxRevalPl;
	}

	public void setOciFxRevalPl(Double ociFxRevalPl) {
		this.ociFxRevalPl = ociFxRevalPl;
	}

	public Double getOciRevalRevnAmt() {
		return ociRevalRevnAmt;
	}

	public void setOciRevalRevnAmt(Double ociRevalRevnAmt) {
		this.ociRevalRevnAmt = ociRevalRevnAmt;
	}

	public Double getOciOtherAmt() {
		return ociOtherAmt;
	}

	public void setOciOtherAmt(Double ociOtherAmt) {
		this.ociOtherAmt = ociOtherAmt;
	}

	public Double getUnrealizedPlSumAmt() {
		return unrealizedPlSumAmt;
	}

	public void setUnrealizedPlSumAmt(Double unrealizedPlSumAmt) {
		this.unrealizedPlSumAmt = unrealizedPlSumAmt;
	}

	public Double getMgtAsstRevnRate() {
		return mgtAsstRevnRate;
	}

	public void setMgtAsstRevnRate(Double mgtAsstRevnRate) {
		this.mgtAsstRevnRate = mgtAsstRevnRate;
	}

	public Double getInvCostAmt() {
		return invCostAmt;
	}

	public void setInvCostAmt(Double invCostAmt) {
		this.invCostAmt = invCostAmt;
	}

	public Double getInvCostRate() {
		return invCostRate;
	}

	public void setInvCostRate(Double invCostRate) {
		this.invCostRate = invCostRate;
	}

	public Double getMgtAsstYield() {
		return mgtAsstYield;
	}

	public void setMgtAsstYield(Double mgtAsstYield) {
		this.mgtAsstYield = mgtAsstYield;
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
		return "DiscRateAsstRevnRate [baseYymm=" + baseYymm + ", acctDvCd=" + acctDvCd + ", mgtAsstAmt=" + mgtAsstAmt
				+ ", invRevnAmt=" + invRevnAmt + ", ociAssetEvalPl=" + ociAssetEvalPl + ", ociRelCorpStkEvalPl="
				+ ociRelCorpStkEvalPl + ", ociFxRevalPl=" + ociFxRevalPl + ", ociRevalRevnAmt=" + ociRevalRevnAmt
				+ ", ociOtherAmt=" + ociOtherAmt + ", unrealizedPlSumAmt=" + unrealizedPlSumAmt + ", mgtAsstRevnRate="
				+ mgtAsstRevnRate + ", invCostAmt=" + invCostAmt + ", invCostRate=" + invCostRate + ", mgtAsstYield="
				+ mgtAsstYield + "]";
	}
	
}


