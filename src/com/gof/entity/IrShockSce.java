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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;

@Entity
@IdClass(IrSceId.class)
@Table(name ="EAS_IR_SHOCK_SCE")
public class IrShockSce implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4458482460359847563L;

	@Id
	private String baseDate;
	
    @Id
	private String irModelId;

    @Id
	private String matCd;

    @Id
	private String sceNo;

    @Id
	private String irCurveId;	
	
	private Double rfIr;
	private Double riskAdjIr;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name ="IR_MODEL_ID", insertable=false, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private EsgMst esgMst;	

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name ="IR_CURVE_ID", insertable=false, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private IrCurve irCurve;	
	
	public IrShockSce() {}

	public String getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}

	public String getIrModelId() {
		return irModelId;
	}

	public void setIrModelId(String irModelId) {
		this.irModelId = irModelId;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getSceNo() {
		return sceNo;
	}

	public void setSceNo(String sceNo) {
		this.sceNo = sceNo;
	}

	public String getIrCurveId() {
		return irCurveId;
	}

	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}

	public Double getRfIr() {
		return rfIr;
	}

	public void setRfIr(Double rfIr) {
		this.rfIr = rfIr;
	}

	public EsgMst getEsgMst() {
		return esgMst;
	}

	public void setEsgmst(EsgMst esgMst) {
		this.esgMst = esgMst;
	}

	public IrCurve getIrCurve() {
		return irCurve;
	}

	public void setIrCurve(IrCurve irCurve) {
		this.irCurve = irCurve;
	}

	public Double getRiskAdjIr() {
		return riskAdjIr;
	}

	public void setRiskAdjIr(Double riskAdjIr) {
		this.riskAdjIr = riskAdjIr;
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

	public void setEsgMst(EsgMst esgMst) {
		this.esgMst = esgMst;
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
		
		builder.append(baseDate).append(delimeter)
			   .append(irModelId).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(sceNo).append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(rfIr).append(delimeter)
			   .append(riskAdjIr).append(delimeter)
			   .append(lastModifiedBy).append(delimeter)
			   .append(lastUpdateDate);
		
		return builder.toString();
	}
}


