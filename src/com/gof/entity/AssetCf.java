package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.enums.EBaseMatCd;
import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(AssetCfId.class)
@Table(name ="EAS_ASST_CF")
public class AssetCf implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4104586581897784001L;
	
	
	@Id	
	private String baseYymm;
	
	@Id
	private String matCd;
	
	@Id
	private String cfMatCd;
	
	private Double cfAmt;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	

	public AssetCf() {}
	
	public AssetCf(String baseYymm, String matCd, String cfMatCd, Double cfAmt, Double vol, String lastModifiedBy, LocalDateTime lastUpdateDate) {
		this.baseYymm = baseYymm;
		this.matCd = matCd;
		this.cfMatCd = cfMatCd;
		this.cfAmt = cfAmt;
		this.vol = vol;
		this.lastModifiedBy = lastModifiedBy;
		this.lastUpdateDate = lastUpdateDate;
	}


	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getCfMatCd() {
		return cfMatCd;
	}

	public void setCfMatCd(String cfMatCd) {
		this.cfMatCd = cfMatCd;
	}

	public Double getCfAmt() {
		return cfAmt;
	}

	public void setCfAmt(Double cfAmt) {
		this.cfAmt = cfAmt;
	}

	public Double getVol() {
		return vol;
	}

	public void setVol(Double vol) {
		this.vol = vol;
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
		return "AsstCf [baseYymm=" + baseYymm + ", matCd=" + matCd + ", cfAmt=" + cfAmt + "]";
	}	
	
	
	public String toGroupingIdString() {
		return toGroupingIdString(",");
	}
	
	public String toGroupingIdString( String delimeter) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseYymm).append(delimeter)
				.append(EBaseMatCd.getBaseMatCd(matCd)).append(delimeter)
				.append(EBaseMatCd.getBaseMatCd(cfMatCd))
				;
		
		return builder.toString();
		
	}	
}


