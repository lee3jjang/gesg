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
@IdClass(BizEsgParamId.class)
@Table(name ="EAS_BIZ_APLY_PARAM")
//@FilterDef(name="bizApplyParamEqBaseYymm", parameters= { @ParamDef(name="baseYymm", type="string") })
public class BizEsgParam implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 1524655691890282755L;

	@Id
	@Column(name="BASE_YYMM", nullable=false)		
	private String baseYymm;
	
	
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
	
	@Column(name="APPL_PARAM_VAL")
	private Double applParamVal;

	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	@ManyToOne()
	@JoinColumn(name ="IR_MODEL_ID", insertable=false, updatable=false)
	private EsgMst esgMst ;
	
	public BizEsgParam() {}
	
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
	
	public HisEsgParam convertToHisEsgParam(int seq) {
		HisEsgParam rst = new HisEsgParam();

		rst.setBaseYymm(this.baseYymm);
		rst.setApplyBizDv(this.applyBizDv);
		rst.setSeq(seq);
		rst.setIrModelId(this.irModelId);
		rst.setParamTypCd(this.paramTypCd);
		rst.setMatCd(this.matCd);
		rst.setApplParamVal(this.applParamVal);
		rst.setVol(this.vol);
		rst.setLastModifiedBy("ESG");
		rst.setLastUpdateDate(LocalDateTime.now());
		
		return rst;
	}

}


