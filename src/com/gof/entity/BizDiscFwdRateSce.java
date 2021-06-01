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
@IdClass(BizDiscFwdRateSceId.class)
@Table(name ="EAS_BIZ_APLY_FWD_RATE_SCE")

public class BizDiscFwdRateSce implements Serializable {

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
	
	@Id
	private String fwdNo;
	
	
	private Double fwdRate;
	
	private Double avgFwdRate;
	
	private Double riskAdjFwdRate;
	
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public BizDiscFwdRateSce() {}

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

	

	public String getFwdNo() {
		return fwdNo;
	}

	public void setFwdNo(String fwdNo) {
		this.fwdNo = fwdNo;
	}

	public Double getFwdRate() {
		return fwdRate;
	}

	public void setFwdRate(Double fwdRate) {
		this.fwdRate = fwdRate;
	}

	public Double getAvgFwdRate() {
		return avgFwdRate;
	}

	public void setAvgFwdRate(Double avgFwdRate) {
		this.avgFwdRate = avgFwdRate;
	}

	public Double getRiskAdjFwdRate() {
		return riskAdjFwdRate;
	}

	public void setRiskAdjFwdRate(Double riskAdjFwdRate) {
		this.riskAdjFwdRate = riskAdjFwdRate;
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
			   .append(applyBizDv).append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(sceNo).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(fwdNo).append(delimeter)
			   
			   .append(fwdRate).append(delimeter)
			   .append(avgFwdRate).append(delimeter)
			   
			   .append(riskAdjFwdRate)
//			   .append(lastModifiedBy).append(delimeter)
//			   .append(lastUpdateDate)
			   ;
		
		return builder.toString();
	}
}


