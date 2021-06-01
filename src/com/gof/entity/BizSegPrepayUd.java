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
@IdClass(BizSegPrepayUdId.class)
@Table(name ="EAS_USER_SEG_PREP_RATE")
public class BizSegPrepayUd implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 2360652480675748510L;
	
	
	@Id
	@Column(name="APPL_ST_YYMM", nullable=false)
	private String applyStartYymm;

	@Id
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	@Id
	@Column(name="SEG_ID", nullable=false)
	private String segId;	
	
	@Column(name ="APPL_ED_YYMM")
	private String applyEndYymm;
	
	@Column(name="APPL_PREP_RATE")	
    private Double applyPrepRate;
	
//	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
    
	public BizSegPrepayUd() {}

	public String getApplyStartYymm() {
		return applyStartYymm;
	}
	public void setApplyStartYymm(String applyStartYymm) {
		this.applyStartYymm = applyStartYymm;
	}

	public String getApplyBizDv() {
		return applyBizDv;
	}
	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}
	public String getSegId() {
		return segId;
	}
	public void setSegId(String segId) {
		this.segId = segId;
	}
	public String getApplyEndYymm() {
		return applyEndYymm;
	}
	public void setApplyEndYymm(String applyEndYymm) {
		this.applyEndYymm = applyEndYymm;
	}
	
	public Double getApplyPrepRate() {
		return applyPrepRate;
	}
	public void setApplyPrepRate(Double applyPrepRate) {
		this.applyPrepRate = applyPrepRate;
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
		return applyPrepRate;
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
