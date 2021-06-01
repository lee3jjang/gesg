package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class SegPrepayId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -574128152521871580L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="SEG_TYP_CD", nullable=false)
	private String segTypCd;
	
	@Column(name="SEG_ID", nullable=false)
	private String segId;	
	
	public SegPrepayId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getSegTypCd() {
		return segTypCd;
	}

	public void setSegTypCd(String segTypCd) {
		this.segTypCd = segTypCd;
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
}
