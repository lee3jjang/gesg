package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class SegLgdUdId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -4017379086329605634L;

	@Column(name="APPL_ST_YYMM", nullable=false)
	private String applStYymm;
	
	@Column(name="LGD_CALC_TYP_CD", nullable=false)
	private String lgdCalcTypCd;

	@Column(name="SEG_ID", nullable=false)
	private String segId;	
	
	public SegLgdUdId() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
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
		return "SegLgdUdId [applStYymm=" + applStYymm + ", lgdCalcTypCd=" + lgdCalcTypCd + ", segId=" + segId + "]";
	}
	
}
