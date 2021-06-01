package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class BizCrdSpreadId implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -5962004839804687117L;
	

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	private String crdGrdCd;	
	private String matCd;
	
	public BizCrdSpreadId() {}
	
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

	public String getCrdGrdCd() {
		return crdGrdCd;
	}

	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
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
