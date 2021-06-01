package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.gof.interfaces.EntityIdentifier;


@Embeddable
public class BizSegLgdId implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -574128152521871580L;

	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;
	
	@Column(name="SEG_ID", nullable=false)
	private String segId;	

	@Column(name="APPL_BIZ_DV", nullable=false)
	private String applyBizDv;
	
	@Column(name="LGD_CALC_TYP_CD", nullable=false)
	private String lgdCalcTypCd;

	public BizSegLgdId() {}

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

	public String getLgdCalcTypCd() {
		return lgdCalcTypCd;
	}

	public void setLgdCalcTypCd(String lgdCalcTypCd) {
		this.lgdCalcTypCd = lgdCalcTypCd;
	}

	public String getSegId() {
		return segId;
	}

	public void setSegId(String segId) {
		this.segId = segId;
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
		return "SegLgdId [baseYymm=" + baseYymm + ", lgdCalcTypCd=" + lgdCalcTypCd + ", segId=" + segId + "]";
	}
	
}
