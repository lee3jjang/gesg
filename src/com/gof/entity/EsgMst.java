package com.gof.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;

import com.gof.enums.EBoolean;

@Entity
@Table(name ="EAS_ESG_MST")
public class EsgMst implements Serializable{
	
	private String irModelId;
	private String irModelNm;
	private String irModedType;
	private String paramApplCd;
	private EBoolean useYn;
	
//	private List<ParamApply> paramApply = new ArrayList<ParamApply>();
//	private List<BizEsgParam> bizApplyParam = new ArrayList<BizEsgParam>();
	
	@Id
//	@GeneratedValue(strategy =GenerationType.IDENTITY)
//	@Column(name ="IR_MODEL_ID")
	public String getIrModelId() {
		return irModelId;
	}

	public void setIrModelId(String irModelId) {
		this.irModelId = irModelId;
	}

	@Column(name ="IR_MODEL_NM")
//	@Transient
	public String getIrModelNm() {
		return irModelNm;
	}

	public void setIrModelNm(String irModelNm) {
		this.irModelNm = irModelNm;
	}

	@Column(name ="IR_MODEL_TYP")
	public String getIrModedType() {
		return irModedType;
	}

	public void setIrModedType(String irModedType) {
		this.irModedType = irModedType;
	}
	
	@Column(name ="PARAM_APPL_CD")
	public String getParamApplCd() {
		return paramApplCd;
	}
	
	public void setParamApplCd(String paramApplCd) {
		this.paramApplCd = paramApplCd;
	}
	
	@Enumerated(EnumType.STRING)
	public EBoolean getUseYn() {
		return useYn;
	}


	public void setUseYn(EBoolean useYn) {
		this.useYn = useYn;
	}

//	@OneToMany(mappedBy ="esgMst")
//	@Filter(name ="paramApplyEqBaseYymm", condition= "BASE_YYMM = :baseYymm")
//	public List<ParamApply> getParamApply() {
//		return paramApply;
//	}
//
//	public void setParamApply(List<ParamApply> paramApply) {
//		this.paramApply = paramApply;
//	}

	/*@OneToMany(mappedBy ="esgMst")
	@Filter(name ="bizApplyParamEqBaseYymm", condition= "BASE_YYMM = :baseYymm")
	public List<BizEsgParam> getBizApplyParam() {
		return bizApplyParam;
	}

	public void setBizApplyParam(List<BizEsgParam> bizApplyParam) {
		this.bizApplyParam = bizApplyParam;
	}*/
	
	
	
}
