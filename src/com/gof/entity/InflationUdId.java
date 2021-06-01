package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class InflationUdId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -9072807206431485429L;

	@Column(name="SETTING_YYMM", nullable=false)
	private String settingYymm;	

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	
	public InflationUdId() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getSettingYymm() {
		return settingYymm;
	}

	public void setSettingYymm(String settingYymm) {
		this.settingYymm = settingYymm;
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
		return "InflationId [baseYymm=" + baseYymm + ", settingYymm=" + settingYymm + "]";
	}
		
}
