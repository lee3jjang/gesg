package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
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
@IdClass(RefPortYieldId.class)
@Table(name ="EAS_REF_PORT_YIELD")
public class RefPortYield implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 8064247948865500380L;

	@Id
	private String baseYymm;	
	@Id
	private String asstClassTypCd;
	@Id
	private String matCd;

	@Column(name ="ASST_YIELD")
	private Double assetYield;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public RefPortYield() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getAsstClassTypCd() {
		return asstClassTypCd;
	}

	public void setAsstClassTypCd(String asstClassTypCd) {
		this.asstClassTypCd = asstClassTypCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getAssetYield() {
		return assetYield;
	}

	public void setAssetYield(Double assetYield) {
		this.assetYield = assetYield;
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
		return "RefPortYield [baseYymm=" + baseYymm + ", asstClassTypCd=" + asstClassTypCd + ", matCd=" + matCd
				+ ", assetYield=" + assetYield +"]";
	}

}


