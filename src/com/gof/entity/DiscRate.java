package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


@Entity
@IdClass(DiscRateId.class)
@Table( name ="EAS_DISC_RATE")
public class DiscRate implements Serializable {

	private static final long serialVersionUID = -8062063262761022307L;

	@Id
	private String baseYymm;
	
	@Id
	private String intRateCd;

	@Id
	private String discRateCalcTyp;

	@Id
	private String matCd;
	
	private Double mgtYield;
	
	private Double exBaseIr;
	
	private Double baseDiscRate;
	
	private Double exBaseIrWght;
	
	private Double adjRate;
	
	private Double discRate;
	
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public DiscRate() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public String getDiscRateCalcTyp() {
		return discRateCalcTyp;
	}

	public void setDiscRateCalcTyp(String discRateCalcTyp) {
		this.discRateCalcTyp = discRateCalcTyp;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getMgtYield() {
		return mgtYield;
	}

	public void setMgtYield(Double mgtYield) {
		this.mgtYield = mgtYield;
	}

	public Double getExBaseIr() {
		return exBaseIr;
	}

	public void setExBaseIr(Double exBaseIr) {
		this.exBaseIr = exBaseIr;
	}

	public Double getBaseDiscRate() {
		return baseDiscRate;
	}

	public void setBaseDiscRate(Double baseDiscRate) {
		this.baseDiscRate = baseDiscRate;
	}

	public Double getExBaseIrWght() {
		return exBaseIrWght;
	}

	public void setExBaseIrWght(Double exBaseIrWght) {
		this.exBaseIrWght = exBaseIrWght;
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
			   .append(intRateCd).append(delimeter)
			   .append(discRateCalcTyp).append(delimeter)
			   .append(matCd).append(delimeter)
			   
			   .append(mgtYield).append(delimeter)
			   .append(exBaseIr).append(delimeter)
			   
			   .append(baseDiscRate).append(delimeter)
			   .append(exBaseIrWght).append(delimeter)
			   .append(adjRate).append(delimeter)
			   .append(discRate).append(delimeter)
			   
			   .append(vol).append(delimeter)
			   .append(lastModifiedBy).append(delimeter)
			   .append(lastUpdateDate);
		
		return builder.toString();
	}
	
	public BizDiscRate convertTo() {
		
		BizDiscRate temp = new BizDiscRate();

		temp.setBaseYymm(this.baseYymm);
		temp.setApplyBizDv(this.discRateCalcTyp);
		temp.setIntRateCd(this.intRateCd);
		temp.setMatCd(this.matCd);
		
//		temp.setBaseDiscRate(aa.getBaseDiscRate());
		temp.setBaseDiscRate(this.baseDiscRate);
		
		temp.setAdjRate(this.adjRate);
		temp.setDiscRate(this.discRate);
		temp.setVol(this.vol);
		
		temp.setLastModifiedBy("ESG");
		temp.setLastUpdateDate(LocalDateTime.now());
		
		return temp;
	}
	
	public HisDiscRate convertToHisDiscRate(int seq) {
		
		HisDiscRate temp = new HisDiscRate();

		temp.setBaseYymm(this.baseYymm);
		temp.setApplyBizDv(this.discRateCalcTyp);
		temp.setSeq(seq);
		temp.setIntRateCd(this.intRateCd);
		temp.setMatCd(this.matCd);
		
//		temp.setBaseDiscRate(aa.getBaseDiscRate());
		temp.setBaseDiscRate(this.baseDiscRate);
		
		temp.setAdjRate(this.adjRate);
		temp.setDiscRate(this.discRate);
		temp.setVol(this.vol);
		
		temp.setLastModifiedBy("ESG");
		temp.setLastUpdateDate(LocalDateTime.now());
		
		return temp;
	}
}