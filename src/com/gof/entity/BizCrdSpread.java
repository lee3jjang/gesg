package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(BizCrdSpreadId.class)
@Table(name ="EAS_BIZ_APLY_CRD_SPREAD")

public class BizCrdSpread implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -8151467682976876533L;
	
	@Id
	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Id
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	@Id
	private String crdGrdCd;	
	@Id
	private String matCd;
	
	@Column(name="APPL_CRD_SPREAD")
	private Double applyCrdSpread;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public BizCrdSpread() {}
	
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
	public String getCrdGrdCd() {
		return crdGrdCd;
	}
	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}
	public Double getApplyCrdSpread() {
		return applyCrdSpread;
	}
	public void setApplyCrdSpread(Double applyCrdSpread) {
		this.applyCrdSpread = applyCrdSpread;
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
//			   .append(applyEndYymm).append(delimeter)
			   .append(applyBizDv).append(delimeter)
			   .append(crdGrdCd).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(applyCrdSpread).append(delimeter)
			   .append(vol)
			   ;
		return builder.toString();
	}
}


