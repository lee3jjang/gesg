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

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(BizEsgParamUdId.class)
@Table(name ="EAS_USER_ESG_PARAM")
@FilterDef(name="paramApplyEqBaseYymm", parameters= { @ParamDef(name="baseYymm", type="string") })

public class BizEsgParamUd implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 1524655691890282755L;

	@Id
	@Column(name="APPL_ST_YYMM", nullable=false)		
	private String applyStartYymm;
	
	
	@Id
	@Column(name="APPL_BIZ_DV", nullable=false)		
	private String applyBizDv;
	
	@Id
	@Column(name="IR_MODEL_ID", nullable=false)
	private String irModelId;

	@Id
	@Column(name="PARAM_TYP_CD", nullable=false)
	private String paramTypCd;
	
	@Id
	@Column(name="MAT_CD", nullable=false)
	private String matCd;
	
	@Column(name="APPL_ED_YYMM", nullable=false)		
	private String applyEndYymm;
	
	
	@Column(name="APPL_PARAM_VAL")
	private Double applParamVal;

	@Column(name="VOL")
	private Double vol;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	@ManyToOne()
	@JoinColumn(name ="IR_MODEL_ID", insertable=false, updatable=false)
	private EsgMst esgMst ;
	
	public BizEsgParamUd() {}
	
	
	public String getApplyStartYymm() {
		return applyStartYymm;
	}


	public void setApplyStartYymm(String applyStartYymm) {
		this.applyStartYymm = applyStartYymm;
	}


	public String getApplyBizDv() {
		return applyBizDv;
	}


	public void setApplyBizDv(String applyBizDv) {
		this.applyBizDv = applyBizDv;
	}


	public String getIrModelId() {
		return irModelId;
	}

	public void setIrModelId(String irModelId) {
		this.irModelId = irModelId;
	}

	public String getParamTypCd() {
		return paramTypCd;
	}
	public void setParamTypCd(String paramTypCd) {
		this.paramTypCd = paramTypCd;
	}
	
	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public String getApplyEndYymm() {
		return applyEndYymm;
	}

	public void setApplyEndYymm(String applyEndYymm) {
		this.applyEndYymm = applyEndYymm;
	}

	public Double getApplParamVal() {
		return applParamVal;
	}

	public void setApplParamVal(Double applParamVal) {
		this.applParamVal = applParamVal;
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

	public EsgMst getEsgMst() {
		return esgMst;
	}

	public void setEsgMst(EsgMst esgMst) {
		this.esgMst = esgMst;
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

	public BizEsgParam convertToBizEsgParam(String bssd) {
		BizEsgParam rst = new BizEsgParam();
		rst.setBaseYymm(bssd);
		rst.setApplyBizDv(this.applyBizDv);
		rst.setIrModelId(this.irModelId);
		rst.setParamTypCd(this.paramTypCd);
		rst.setMatCd(this.matCd);
		rst.setApplParamVal(this.applParamVal);
		rst.setVol(0.0);
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;
	}
}


