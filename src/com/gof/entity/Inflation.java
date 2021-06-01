package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.BaseValue;
import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(InflationId.class)
@Table( name ="EAS_INFLATION")

public class Inflation implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 2721034625992403875L;

	@Id
	private String settingYymm;
	
	@Id
	private String baseYymm;
	
	@Id
	private String inflationId;	
	
	private Double inflationIndex;
	
	private Double inflation;
	
	private Double mgmtTargetLowerVal;
	
	private Double mgmtTargetUpperVal;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public Inflation() {}

	
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

	public String getInflationId() {
		return inflationId;
	}

	public void setInflationId(String inflationId) {
		this.inflationId = inflationId;
	}

	public Double getInflationIndex() {
		return inflationIndex;
	}

	public void setInflationIndex(Double inflationIndex) {
		this.inflationIndex = inflationIndex;
	}

	public Double getInflation() {
		return inflation;
	}

	public void setInflation(Double inflation) {
		this.inflation = inflation;
	}

	public Double getMgmtTargetLowerVal() {
		return mgmtTargetLowerVal;
	}

	public void setMgmtTargetLowerVal(Double mgmtTargetLowerVal) {
		this.mgmtTargetLowerVal = mgmtTargetLowerVal;
	}

	public Double getMgmtTargetUpperVal() {
		return mgmtTargetUpperVal;
	}

	public void setMgmtTargetUpperVal(Double mgmtTargetUpperVal) {
		this.mgmtTargetUpperVal = mgmtTargetUpperVal;
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
	public Double getBasicValue() {
		return inflationIndex;
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

//	@Override
//	public String toString() {
//		return "Inflation [baseYymm=" + baseYymm + ", inflationId=" + inflationId + ", inflationIndex=" + inflationIndex
//				+ ", inflation=" + inflation + ", mgmtTargetLowerVal=" + mgmtTargetLowerVal + ", mgmtTargetUpperVal="
//				+ mgmtTargetUpperVal + "]";
//	}

	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(settingYymm).append(delimeter)
				.append(baseYymm).append(delimeter)
			   .append(inflationId).append(delimeter)
			   .append(inflationIndex).append(delimeter)
			   .append(inflation).append(delimeter)
			   .append(mgmtTargetLowerVal).append(delimeter)
			   .append(mgmtTargetUpperVal).append(delimeter)
			   .append(vol).append(delimeter)
//			   .append(lastModifiedBy).append(delimeter)
//			   .append(lastUpdateDate)
			   ;
		
		return builder.toString();
	}
	
	
}


