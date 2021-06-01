package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


@Entity
@IdClass(HisDiscRateId.class)
@Table(name ="EAS_HIS_DISC_RATE")
public class HisDiscRate implements Serializable {

	private static final long serialVersionUID = -8062063262761022307L;

	@Id
	private String baseYymm;
	@Id
	@Column(name = "APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	@Id
	private int seq;	
	
	@Id
	private String intRateCd;

	@Id
	private String matCd;
	
	private Double baseDiscRate;
	
	private Double adjRate;
	
	private Double discRate;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public HisDiscRate() {}

	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getApplyBizDv() {
		return applyBizDv;
	}
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}
	public String getIntRateCd() {
		return intRateCd;
	}
	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}
	public Double getBaseDiscRate() {
		return baseDiscRate;
	}
	public void setBaseDiscRate(Double baseDiscRate) {
		this.baseDiscRate = baseDiscRate;
	}
	public Double getAdjRate() {
		return adjRate;
	}

	public void setAdjRate(Double adjRate) {
		this.adjRate = adjRate;
	}

	public Double getDiscRate() {
		return discRate;
	}

	public void setDiscRate(Double discRate) {
		this.discRate = discRate;
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
//		return "DiscRate [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", discRateCalcTyp=" + discRateCalcTyp
//				+ ", matCd=" + matCd + ", mgtYield=" + mgtYield + ", exBaseIr=" + exBaseIr + ", baseDiscRate="
//				+ baseDiscRate + ", exBaseIrWght=" + exBaseIrWght + ", adjRate=" + adjRate + ", discRate=" + discRate
//				+ "]";
//	}	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(applyBizDv).append(delimeter)
			   .append(seq).append(delimeter)
			   .append(intRateCd).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(baseDiscRate).append(delimeter)
			   .append(adjRate).append(delimeter)
			   .append(discRate).append(delimeter)
			   
			   .append(vol).append(delimeter)
			   .append(lastModifiedBy).append(delimeter)
			   .append(lastUpdateDate);
		
		return builder.toString();
	}
}