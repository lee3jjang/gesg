package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(InflationUdId.class)
@Table(name ="EAS_USER_INFLATION")

public class InflationUd implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 7932117728201576987L;

	@Id
	private String settingYymm;
	
	@Id
	private String baseYymm;
	
	private Double inflationIndex;
	private Double ifrsTgtIndex;
	private Double kicsTgtIndex;
	
	public InflationUd() {}

	
	public String getSettingYymm() {
		return settingYymm;
	}


	public void setSettingYymm(String settingYymm) {
		this.settingYymm = settingYymm;
	}


	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public Double getInflationIndex() {
		return inflationIndex;
	}

	public void setInflationIndex(Double inflationIndex) {
		this.inflationIndex = inflationIndex;
	}

	public Double getIfrsTgtIndex() {
		return ifrsTgtIndex;
	}

	public void setIfrsTgtIndex(Double ifrsTgtIndex) {
		this.ifrsTgtIndex = ifrsTgtIndex;
	}

	public Double getKicsTgtIndex() {
		return kicsTgtIndex;
	}

	public void setKicsTgtIndex(Double kicsTgtIndex) {
		this.kicsTgtIndex = kicsTgtIndex;
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
		return "InflationUd [baseYymm=" + baseYymm + ", inflationIndex=" + inflationIndex + "]";
	}
	
}


