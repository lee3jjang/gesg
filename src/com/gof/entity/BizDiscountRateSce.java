package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;


@Entity
@IdClass(BizDiscountRateSceId.class)
@Table(name ="EAS_BIZ_APLY_DCNT_SCE")

public class BizDiscountRateSce implements Serializable {

	private static final long serialVersionUID = -4252300668894647002L;
	
	@Id
	private String baseYymm;
	@Id
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	@Id
	private String irCurveId;
	@Id
	private String sceNo;

	@Id
	private String matCd;
	
	private Double rfRate;
	
	private Double liqPrem;
	
	private Double refYield;
	
	private Double crdSpread;
	
	private Double riskAdjRfRate;
	
	private Double riskAdjRfFwdRate;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public BizDiscountRateSce() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getApplyBizDv() {
		return applyBizDv;
	}

	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}

	public String getIrCurveId() {
		return irCurveId;
	}

	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}

	public String getSceNo() {
		return sceNo;
	}

	public void setSceNo(String sceNo) {
		this.sceNo = sceNo;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getRfRate() {
		return rfRate;
	}

	public void setRfRate(Double rfRate) {
		this.rfRate = rfRate;
	}

	public Double getLiqPrem() {
		return liqPrem;
	}

	public void setLiqPrem(Double liqPrem) {
		this.liqPrem = liqPrem;
	}

	public Double getRefYield() {
		return refYield;
	}

	public void setRefYield(Double refYield) {
		this.refYield = refYield;
	}

	public Double getCrdSpread() {
		return crdSpread;
	}

	public void setCrdSpread(Double crdSpread) {
		this.crdSpread = crdSpread;
	}

	public Double getRiskAdjRfRate() {
		return riskAdjRfRate;
	}

	public void setRiskAdjRfRate(Double riskAdjRfRate) {
		this.riskAdjRfRate = riskAdjRfRate;
	}

	public Double getRiskAdjRfFwdRate() {
		return riskAdjRfFwdRate;
	}

	public void setRiskAdjRfFwdRate(Double riskAdjRfFwdRate) {
		this.riskAdjRfFwdRate = riskAdjRfFwdRate;
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

//	@Override
//	public String toString() {
//		return "DcntSce [baseYymm=" + baseYymm + ", irCurveId=" + irCurveId + ", sceNo=" + sceNo + ", matCd=" + matCd
//				+ ", rfRate=" + rfRate + ", liqPrem=" + liqPrem + ", refYield=" + refYield + ", crdSpread=" + crdSpread
//				+ ", riskAdjRfRate=" + riskAdjRfRate + ", riskAdjRfFwdRate=" + riskAdjRfFwdRate + ", irCurve=" + irCurve
//				+ "]";
//	}	
	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append("I").append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(sceNo).append(delimeter)
			   .append(matCd).append(delimeter)
			   
			   
			   .append(rfRate).append(delimeter)
			   .append(liqPrem).append(delimeter)
			   
			   .append(refYield).append(delimeter)
			   .append(crdSpread).append(delimeter)
			   
			   .append(riskAdjRfRate).append(delimeter)
			   .append(riskAdjRfFwdRate).append(delimeter)
			   
			   .append(0.0)
//			   .append(lastModifiedBy).append(delimeter)
//			   .append(lastUpdateDate)
			   ;
		
		return builder.toString();
	}
}


