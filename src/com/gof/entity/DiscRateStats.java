package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


@Entity
@IdClass(DiscRateStatsId.class)
@Table(name ="EAS_DISC_RATE_STATS")
@FilterDef(name="discRateStatEqApplStYymm", parameters= { @ParamDef(name="applStYymm", type="string") })
public class DiscRateStats implements Serializable {

	private static final long serialVersionUID = 6207498187147536428L;

	@Id
	private String applStYymm;	
	
	@Id
	private String discRateCalcTyp;
	
	@Id
	private String intRateCd;

	@Id
	private String depnVariable;

	@Id
	private String indpVariable;
	
	private String applEdYymm;
	
	private Double regrConstant;
	
	private Double regrCoef;
	
	private String remark;	
	
	@Column(name ="VOL")			// mismatch column
	private Double avgNum;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	@ManyToOne
	@JoinColumn(name ="INT_RATE_CD", insertable=false, updatable= false)
	private DiscRateCalcSetting discSetting ;
	
	public DiscRateStats() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
	}

	public String getDiscRateCalcTyp() {
		return discRateCalcTyp;
	}

	public void setDiscRateCalcTyp(String discRateCalcTyp) {
		this.discRateCalcTyp = discRateCalcTyp;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public String getDepnVariable() {
		return depnVariable;
	}

	public void setDepnVariable(String depnVariable) {
		this.depnVariable = depnVariable;
	}

	public String getIndpVariable() {
		return indpVariable;
	}

	public void setIndpVariable(String indpVariable) {
		this.indpVariable = indpVariable;
	}

	public String getApplEdYymm() {
		return applEdYymm;
	}

	public void setApplEdYymm(String applEdYymm) {
		this.applEdYymm = applEdYymm;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public DiscRateCalcSetting getDiscSetting() {
		return discSetting;
	}

	public void setDiscSetting(DiscRateCalcSetting discSetting) {
		this.discSetting = discSetting;
	}

	public Double getAvgNum() {
		return avgNum;
	}

	public void setAvgNum(Double avgNum) {
		this.avgNum = avgNum;
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

	@Transient
	public String getIndiVariableMatCd() {
		if(indpVariable.contains("M")) {
			return "M" +String.format("%04d", Integer.parseInt(indpVariable.replace("KTB","").replace("M", "").trim()));
		}else {
			return "M" +String.format("%04d", 12 * Integer.parseInt(indpVariable.replace("KTB","").replace("Y", "").trim()));
		}
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
		return "DiscRateStats [applStYymm=" + applStYymm + ", discRateCalcTyp=" + discRateCalcTyp + ", intRateCd="
				+ intRateCd + ", depnVariable=" + depnVariable + ", indpVariable=" + indpVariable + ", avgNum=" + avgNum + ", applEdYymm="
				+ applEdYymm + ", regrConstant=" + regrConstant + ", regrCoef=" + regrCoef + ", remark=" + remark + "]";
	}	
	
}


