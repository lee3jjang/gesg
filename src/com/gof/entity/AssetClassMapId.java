package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class AssetClassMapId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -2793349946630671081L;

	@Column(name="PROD_TYP_CD", nullable=false)
	private String prodTypCd;
	
	@Column(name="ASST_CLASS_TYP_CD", nullable=false)
	private String asstClassTypCd;
	
	public AssetClassMapId() {}

	public String getProdTypCd() {
		return prodTypCd;
	}

	public void setProdTypCd(String prodTypCd) {
		this.prodTypCd = prodTypCd;
	}

	public String getAsstClassTypCd() {
		return asstClassTypCd;
	}

	public void setAsstClassTypCd(String asstClassTypCd) {
		this.asstClassTypCd = asstClassTypCd;
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
		return "AsstClassMapId [prodTypCd=" + prodTypCd + ", asstClassTypCd=" + asstClassTypCd + "]";
	}
	
}
