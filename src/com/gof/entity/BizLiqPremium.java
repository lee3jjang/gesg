package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@IdClass(BizLiqPremiumId.class)
@Table(name ="EAS_BIZ_APLY_LIQ_PREM")
@FilterDef(name="eqBaseYymm", parameters= @ParamDef(name ="bssd",  type="string"))
public class BizLiqPremium implements Serializable{

	private String baseYymm;
	private String applyBizDv;
	private String irCurveId;
	private String matCd;
//	private String applyEndYymm;
	
	private Double applyLiqPrem;
	private Double liqPrem;
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	
	public BizLiqPremium() {
	
	}
	public BizLiqPremium(double liqPrem) {
		this.liqPrem = liqPrem;
	}
	
	@Id
//	@Column(name ="BASE_YYMM")
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	
	@Id
//	@Column(name ="APPL_BIZ_DV")
	public String getApplyBizDv() {
		return applyBizDv;
	}
	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}
	
	@Id
	public String getIrCurveId() {
		return irCurveId;
	}
	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}
	
	@Id
//	@Column(name ="MAT_CD")
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}
	
	@Column(name ="APPL_LIQ_PREM")
	public Double getApplyLiqPrem() {
		return applyLiqPrem;
	}
	public void setApplyLiqPrem(Double applyLiqPrem) {
		this.applyLiqPrem = applyLiqPrem;
	}
	
	@Column(name ="LIQ_PREM")
	public double getLiqPrem() {
		return liqPrem;
	}
	public void setLiqPrem(Double liqPrem) {
		this.liqPrem = liqPrem;
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
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(applyBizDv).append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(applyLiqPrem).append(delimeter)
			   .append(liqPrem).append(delimeter)
			   .append(vol)
//			   .append(lastModifiedBy).append(delimeter)
//			   .append(lastUpdateDate)
			   ;
		return builder.toString();
	}
	
	public HisLiqPremium convertToHisLiqPremium(int seq) {
		HisLiqPremium rst = new HisLiqPremium();
		
		rst.setBaseYymm(this.baseYymm);
		rst.setApplyBizDv(this.applyBizDv);
		rst.setIrCurveId(this.irCurveId);
		rst.setSeq(seq);
		rst.setMatCd(this.matCd);
		rst.setApplyLiqPrem(this.applyLiqPrem);
		rst.setLiqPrem(this.liqPrem);
		rst.setVol(this.vol);
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
				
		return rst;
	}
	
}
