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
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.ParamDef;

import com.gof.enums.EBaseMatCd;
import com.gof.interfaces.IIntRate;
import com.gof.util.FinUtils;


@Entity
@IdClass(BottomupDcntId.class)
@Table(name ="EAS_BOTTOMUP_DCNT")
@FilterDef(name="IR_FILTER", parameters= { @ParamDef(name="baseYymm", type="string"), @ParamDef(name="irCurveId", type="string") })
@Filters( { @Filter(name ="IR_FILTER", condition="BASE_YYMM = :baseYymm"),  @Filter(name ="IR_FILTER", condition="IR_CURVE_ID like :irCurveId") } )
public class BottomupDcnt implements Serializable , IIntRate{

	private static final long serialVersionUID = -8105176349509184506L;

	@Id
	private String baseYymm;
	
	@Id
	private String irCurveId;
	
	@Id
	private String matCd;	
	
	private Double rfRate;
	
	private Double liqPrem;
	
	private Double riskAdjRfRate;
	
	private Double riskAdjRfFwdRate;
	 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name ="IR_CURVE_ID", insertable=false, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private IrCurve irCurve;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public BottomupDcnt() {}

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
		return toString(",");
	}

	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseYymm).append(delimeter)
				.append(irCurveId).append(delimeter)
				.append(matCd).append(delimeter)
				.append(rfRate).append(delimeter)
				.append(liqPrem).append(delimeter)
				.append(riskAdjRfRate).append(delimeter)
				.append(riskAdjRfFwdRate).append(delimeter)
				.append(vol)
				;

		return builder.toString();

	}
	@Transient
	@Override
	public Double getIntRate() {
		return getRiskAdjRfRate();
	}

	@Transient
	@Override
	public Double getSpread() {
		return getLiqPrem();
	}

	
	public IrCurveHis convert() {
		IrCurveHis irHis  = new IrCurveHis(this.baseYymm, this.matCd, this.riskAdjRfRate);
		irHis.setIrCurveId(this.irCurveId);
		return irHis;		
	}

	
}


