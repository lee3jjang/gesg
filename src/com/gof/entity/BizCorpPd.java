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
@IdClass(BizCorpPdId.class)
@Table(name ="EAS_BIZ_APLY_CORP_PD")
public class BizCorpPd implements Serializable, EntityIdentifier{
	
	private String baseYymm;
	private String applyBizDv;
	private String crdGrdCd;
	private String matCd;

	private Double pd;
	private Double cumPd;
	private Double fwdPd;
	private Double vol;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	@Id
	@Column(name = "BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	@Id
	@Column(name = "APPL_BIZ_DV")
	public String getApplyBizDv() {
		return applyBizDv;
	}
	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}
	
	@Id
	@Column(name = "CRD_GRD_CD")
	public String getCrdGrdCd() {
		return crdGrdCd;
	}
	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}
	
	@Id
	@Column(name = "MAT_CD")
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getPd() {
		return pd;
	}
	public void setPd(Double pd) {
		this.pd = pd;
	}
	
	public Double getCumPd() {
		return cumPd;
	}
	public void setCumPd(Double cumPd) {
		this.cumPd = cumPd;
	}
	public Double getFwdPd() {
		return fwdPd;
	}
	public void setFwdPd(Double fwdPd) {
		this.fwdPd = fwdPd;
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
	
}
