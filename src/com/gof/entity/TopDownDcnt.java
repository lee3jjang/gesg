package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(TopDownDcntId.class)
@Table( name ="EAS_TOPDOWN_DCNT")

public class TopDownDcnt implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -903417344477099537L;

	@Id
	private String baseYymm;
	
	@Id
	private String irCurveId;
	
	@Id
	private String matCd;	
	
	private Double refYield;
	
	private Double crdSpread;
	
	private Double riskAdjRfRate;
	
	private Double riskAdjRfFwdRate;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name ="IR_CURVE_ID", insertable=false, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private IrCurve irCurve;
	
	public TopDownDcnt() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIrCurveId() {
		return irCurveId;
	}

	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
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

	public IrCurve getIrCurve() {
		return irCurve;
	}

	public void setIrCurve(IrCurve irCurve) {
		this.irCurve = irCurve;
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

	@Override
	public String toString() {
		return "TopdownDcnt [baseYymm=" + baseYymm + ", irCurveId=" + irCurveId + ", matCd=" + matCd + ", refYield="
				+ refYield + ", crdSpread=" + crdSpread + ", riskAdjRfRate=" + riskAdjRfRate + ", riskAdjRfFwdRate="
				+ riskAdjRfFwdRate + ", irCurve=" + irCurve + "]";
	}	

}


