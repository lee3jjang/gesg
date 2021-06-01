package com.gof.model;

import java.io.Serializable;

public class IrmodelResult implements Serializable {
	
	private static final long serialVersionUID = 3248965187822308189L;

	//@Id
	private String baseDate;
	
	private String resultType;
	
	private String scenType;
	
	private String matCd;
	//@Id
//	private Double matTerm;
//	
//	private LocalDate matDate;
	
	private Double spotCont;
	
	private Double spotDisc;
	
	private Double fwdCont;
	
	private Double fwdDisc;
	
	public IrmodelResult() {}

	
	public String getBaseDate() {
		return baseDate;
	}


	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}


	public String getResultType() {
		return resultType;
	}


	public void setResultType(String resultType) {
		this.resultType = resultType;
	}


	public String getScenType() {
		return scenType;
	}


	public void setScenType(String scenType) {
		this.scenType = scenType;
	}


	public String getMatCd() {
		return matCd;
	}


	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}


	public Double getSpotCont() {
		return spotCont;
	}


	public void setSpotCont(Double spotCont) {
		this.spotCont = spotCont;
	}


	public Double getSpotDisc() {
		return spotDisc;
	}


	public void setSpotDisc(Double spotDisc) {
		this.spotDisc = spotDisc;
	}


	public Double getFwdCont() {
		return fwdCont;
	}


	public void setFwdCont(Double fwdCont) {
		this.fwdCont = fwdCont;
	}


	public Double getFwdDisc() {
		return fwdDisc;
	}


	public void setFwdDisc(Double fwdDisc) {
		this.fwdDisc = fwdDisc;
	}


	@Override
	public String toString() {
		return "IrmodelResult [baseDate=" + baseDate + ", resultType=" + resultType
				+ ", spotCont=" + spotCont + ", spotDisc=" + spotDisc + ", fwdCont=" + fwdCont
				+ ", fwdDisc=" + fwdDisc + "]";
	}
	
	
}
