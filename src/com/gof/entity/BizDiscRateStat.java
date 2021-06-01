package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@IdClass(BizDiscRateStatId.class)
@Table(name ="EAS_BIZ_APLY_DISC_RATE_STAT")
public class BizDiscRateStat implements Serializable {

	private static final long serialVersionUID = -8062063262761022307L;

	@Id
	private String baseYymm;
	@Id
	@Column(name = "APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	
	@Id
	private String intRateCd;

	@Id
	private String indpVariable;
	
//	@Id
	@Transient
	private String depnVariable;
	
	private Double avgMonNum;
	private Double regrConstant;
	private Double regrCoef;
	private Double adjRate;
	
	private Double vol;
	private String remark;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public BizDiscRateStat() {}

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
	public String getIntRateCd() {
		return intRateCd;
	}
	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}
	
	public String getIndpVariable() {
		return indpVariable;
	}

	public void setIndpVariable(String indpVariable) {
		this.indpVariable = indpVariable;
	}

	public String getDepnVariable() {
		return depnVariable;
	}

	public void setDepnVariable(String depnVariable) {
		this.depnVariable = depnVariable;
	}

	public Double getAvgMonNum() {
		return avgMonNum;
	}

	public void setAvgMonNum(Double avgMonNum) {
		this.avgMonNum = avgMonNum;
	}

	public Double getRegrConstant() {
		return regrConstant;
	}

	public void setRegrConstant(Double regrConstant) {
		this.regrConstant = regrConstant;
	}

	public Double getRegrCoef() {
		return regrCoef;
	}

	public void setRegrCoef(Double regrCoef) {
		this.regrCoef = regrCoef;
	}

	public Double getAdjRate() {
		return adjRate;
	}

	public void setAdjRate(Double adjRate) {
		this.adjRate = adjRate;
	}

	public Double getVol() {
		return vol;
	}

	public void setVol(Double vol) {
		this.vol = vol;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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
	public String toString() {
		return toString(",");
	}
	
	
	public String getIndiVariableMatCd() {
		if(indpVariable.contains("M")) {
			return "M" +String.format("%04d", Integer.parseInt(indpVariable.replace("KTB","").replace("M", "").trim()));
		}else {
			return "M" +String.format("%04d", 12 * Integer.parseInt(indpVariable.replace("KTB","").replace("Y", "").trim()));
		}
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(applyBizDv).append(delimeter)
			   .append(intRateCd).append(delimeter)
			   .append(indpVariable).append(delimeter)
			   .append(regrConstant).append(delimeter)
			   .append(regrCoef).append(delimeter)
			   .append(adjRate).append(delimeter)
			   .append(vol).append(delimeter)
			   .append(remark).append(delimeter)
			   .append(lastModifiedBy).append(delimeter)
			   .append(lastUpdateDate);
		
		return builder.toString();
	}
	public DiscRateStats convertToDiscRateStst() {
		DiscRateStats rst = new DiscRateStats();
		
		rst.setApplStYymm(this.baseYymm);
		rst.setDiscRateCalcTyp(this.applyBizDv);
		rst.setIntRateCd(this.intRateCd);
		rst.setApplEdYymm(this.baseYymm);
		rst.setAvgNum(this.avgMonNum);
		rst.setDepnVariable("BASE_DISC");
		rst.setIndpVariable(this.indpVariable);
		rst.setRegrCoef(this.regrCoef);
		rst.setRegrConstant(this.regrConstant);
		rst.setRemark("");
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;
	}
	
	
	public HisDiscRateStat convertToHisDiscRateStst(int seq) {
		HisDiscRateStat rst = new HisDiscRateStat();

		rst.setBaseYymm(this.baseYymm);
		rst.setApplyBizDv(this.applyBizDv);
		rst.setSeq(seq);
		rst.setIntRateCd(this.intRateCd);
		rst.setIndpVariable(this.indpVariable);
		rst.setDepnVariable("BASE_DISC");
		rst.setAvgMonNum(this.avgMonNum);
		rst.setRegrCoef(this.regrCoef);
		rst.setRegrConstant(this.regrConstant);
		rst.setAdjRate(this.adjRate);
		rst.setRemark("");
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;
	}
}