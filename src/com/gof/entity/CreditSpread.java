package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(CreditSpreadId.class)
@Table(name ="EAS_CRD_SPREAD")

public class CreditSpread implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -8151467682976876533L;
	
	@Id
	private String baseYymm;

	@Id
	private String crdGrdCd;

	@Id
	private String matCd;
	
	@Column(name ="CRD_GRD_NM")
	private String crdGrdName;
	
	
	private Double crdSpread;	
	
	public CreditSpread() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getCrdGrdCd() {
		return crdGrdCd;
	}

	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}

	public String getCrdGrdName() {
		return crdGrdName;
	}

	public void setCrdGrdName(String crdGrdName) {
		this.crdGrdName = crdGrdName;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getCrdSpread() {
		return crdSpread;
	}

	public void setCrdSpread(Double crdSpread) {
		this.crdSpread = crdSpread;
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


