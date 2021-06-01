package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
public class BizSegPrepayUdId implements Serializable{
	
	@Column(name ="APPL_ST_YYMM")
	private String applyStartYymm;
	@Column(name ="APPL_BIZ_DV")
	private String applyBizDv;
	@Column(name ="SEG_ID")
	private String segId;
	
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
	
	@Override
	public boolean equals(Object arg0) {
		return super.equals(arg0);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
