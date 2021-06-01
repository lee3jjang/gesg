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
@IdClass(IndiCrdGrdPdId.class)
@Table( name ="EAS_INDI_CRD_GRD_PD")
public class IndiCrdGrdPd implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 1739001196612838477L;

	@Id
	private String baseYymm;

	@Id
	private String crdEvalAgncyCd;

	@Id
	private String cbGrdCd;
	
	private Double pd;	
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public IndiCrdGrdPd() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getCrdEvalAgncyCd() {
		return crdEvalAgncyCd;
	}

	public void setCrdEvalAgncyCd(String crdEvalAgncyCd) {
		this.crdEvalAgncyCd = crdEvalAgncyCd;
	}

	public String getCbGrdCd() {
		return cbGrdCd;
	}

	public void setCbGrdCd(String cbGrdCd) {
		this.cbGrdCd = cbGrdCd;
	}

	public Double getPd() {
		return pd;
	}

	public void setPd(Double pd) {
		this.pd = pd;
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
		return pd;
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
//		return "IndiCrdGrdPd [baseYymm=" + baseYymm + ", crdEvalAgncyCd=" + crdEvalAgncyCd + ", cbGrdCd=" + cbGrdCd
//				+ ", pd=" + pd + "]";
//	}
	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(cbGrdCd).append(delimeter)
			   .append(crdEvalAgncyCd).append(delimeter)
			   .append(pd).append(delimeter)
			   .append(vol)
			   ;
		
		return builder.toString();
	}
		
}


