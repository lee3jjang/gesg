package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class BizEsgParamUdId implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -1540161178787348772L;
	  
	@Column(name="APPL_ST_YYMM", nullable=false)			//TODO column name change !!!
	private String applyStartYymm;
	
	@Column(name="APPL_BIZ_DV", nullable=false)		
	private String applyBizDv;
	
	@Column(name="IR_MODEL_ID", nullable=false)
	private String irModelId;
	
	@Column(name="PARAM_TYP_CD", nullable=false)
	private String paramTypCd;
	
	@Column(name="MAT_CD", nullable=false)
	private String matCd;
	
	public BizEsgParamUdId() {}
	
	
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
}
