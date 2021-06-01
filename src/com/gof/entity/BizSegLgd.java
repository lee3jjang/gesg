package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.interfaces.BaseValue;
import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(BizSegLgdId.class)
@Table(name ="EAS_BIZ_APLY_SEG_LGD")
public class BizSegLgd implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 2360652480675748510L;

	@Id
	private String baseYymm;
	@Id
	@Column(name="APPL_BIZ_DV")
	private String applyBizDv;
	
	@Id
	private String lgdCalcTypCd;
	
	@Id
	private String segId;	
	
	@Column(name="APPL_LGD")	
    private Double applyLgd;
    
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
    
	public BizSegLgd() {}

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

	public String getLgdCalcTypCd() {
		return lgdCalcTypCd;
	}

	public void setLgdCalcTypCd(String lgdCalcTypCd) {
		this.lgdCalcTypCd = lgdCalcTypCd;
	}

	public String getSegId() {
		return segId;
	}

	public void setSegId(String segId) {
		this.segId = segId;
	}
	
	public Double getApplyLgd() {
		return applyLgd;
	}

	public void setApplyLgd(Double applyLgd) {
		this.applyLgd = applyLgd;
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
	public Double getBasicValue() {
		return applyLgd;
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
		return "SegLgd [baseYymm=" + baseYymm + ", lgdCalcTypCd=" + lgdCalcTypCd + ", segId=" + segId + ", lgd=" + applyLgd +  "]";
	}

}


