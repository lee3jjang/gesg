package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name ="EAS_USER_INV_MGT_COST")
public class InvestManageCostUd implements Serializable {

	private static final long serialVersionUID = 3991701278375058071L;

	@Id
	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
//	private Double longMgtAsstAmt;
//	private Double longInvCostAmt;
//	private Double longInvCostRate;
//	private Double pensMgtAsstAmt;
//	private Double pensInvCostAmt;
//	private Double pensInvCostRate;
	private Double longMgtAsstYield;
	private Double pensMgtAsstYield;
	private Double longInvCostRate;
	private Double pensInvCostRate;
	
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
		
	public InvestManageCostUd() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public Double getLongMgtAsstYield() {
		return longMgtAsstYield;
	}

	public void setLongMgtAsstYield(Double longMgtAsstYield) {
		this.longMgtAsstYield = longMgtAsstYield;
	}

	public Double getPensMgtAsstYield() {
		return pensMgtAsstYield;
	}

	public void setPensMgtAsstYield(Double pensMgtAsstYield) {
		this.pensMgtAsstYield = pensMgtAsstYield;
	}

	public Double getLongInvCostRate() {
		return longInvCostRate;
	}

	public void setLongInvCostRate(Double longInvCostRate) {
		this.longInvCostRate = longInvCostRate;
	}

	public Double getPensInvCostRate() {
		return pensInvCostRate;
	}

	public void setPensInvCostRate(Double pensInvCostRate) {
		this.pensInvCostRate = pensInvCostRate;
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

	@Transient
	public Map<String, Double>  getInvCostRateByAccount(){
		Map<String, Double> rst = new HashMap<>();
		rst.put("8300_장기무배당" , getLongInvCostRate()==null? 0.0:getLongInvCostRate());
		rst.put("8100_개인연금" , getPensInvCostRate() ==null? 0.0: getPensInvCostRate() );
		return rst;		
	}
	
}


