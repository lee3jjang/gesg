package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(AssetClassMapId.class)
@Table(name ="EAS_ASST_CLASS_MAP")
public class AssetClassMap implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 195231169699880868L;
	
	@Id
	private String prodTypCd;
	
	@Id
	private String asstClassTypCd;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name ="ASST_CLASS_TYP_CD", insertable=false, updatable=false)
	@NotFound(action=NotFoundAction.IGNORE)
	private TopDownAsstClass topdownAsstClass;
	
	public AssetClassMap() {}

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

	public TopDownAsstClass getTopdownAsstClass() {
		return topdownAsstClass;
	}

	public void setTopdownAsstClass(TopDownAsstClass topdownAsstClass) {
		this.topdownAsstClass = topdownAsstClass;
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
		return "AsstClassMap [prodTypCd=" + prodTypCd + ", asstClassTypCd=" + asstClassTypCd + ", topdownAsstClass="
				+ topdownAsstClass + "]";
	}

}


