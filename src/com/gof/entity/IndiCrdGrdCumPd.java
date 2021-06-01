package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(IndiCrdGrdCumPdId.class)
@Table( name ="EAS_INDI_CRD_GRD_CUM_PD")
public class IndiCrdGrdCumPd implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 593109163503140012L;
	
	@Id
	private String baseYymm;

	@Id
	private String cbGrdCd;
	
	@Id
	private String matCd;	

	@Id
	private String crdEvalAgncyCd;
	
	private Double cumPd;	
	
	private Double fwdPd;
	
	private Double cumPdChgRate;
	
	private Double vol;
	
	private String lastModifiedBy;
	
	private LocalDateTime lastUpdateDate;
	
	public IndiCrdGrdCumPd() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getCbGrdCd() {
		return cbGrdCd;
	}

	public void setCbGrdCd(String cbGrdCd) {
		this.cbGrdCd = cbGrdCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getCrdEvalAgncyCd() {
		return crdEvalAgncyCd;
	}

	public void setCrdEvalAgncyCd(String crdEvalAgncyCd) {
		this.crdEvalAgncyCd = crdEvalAgncyCd;
	}

	public Double getCumPd() {
		return cumPd;
	}

	public void setCumPd(Double cumPd) {
		this.cumPd = cumPd;
	}

	public Double getFwdPd() {
		return fwdPd;
	}

	public void setFwdPd(Double fwdPd) {
		this.fwdPd = fwdPd;
	}

	public Double getCumPdChgRate() {
		return cumPdChgRate;
	}

	public void setCumPdChgRate(Double cumPdChgRate) {
		this.cumPdChgRate = cumPdChgRate;
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

//	@Override
//	public String toString() {
//		return "IndiCrdGrdCumPd [baseYymm=" + baseYymm + ", cbGrdCd=" + cbGrdCd + ", matCd=" + matCd
//				+ ", crdEvalAgncyCd=" + crdEvalAgncyCd + ", cumPd=" + cumPd + ", fwdPd=" + fwdPd + ", cumPdChgRate="
//				+ cumPdChgRate + "]";
//	}
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(cbGrdCd).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(crdEvalAgncyCd).append(delimeter)
			   .append(cumPd).append(delimeter)
			   .append(fwdPd).append(delimeter)
			   .append(cumPdChgRate).append(delimeter)
			   .append(vol)
			   ;
		
		return builder.toString();
	}
		
}


