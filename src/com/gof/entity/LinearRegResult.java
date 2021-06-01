package com.gof.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gof.interfaces.Rrunnable;

public class LinearRegResult implements Rrunnable {
	private final static Logger logger = LoggerFactory.getLogger("HullWhite");

	private String baseYymm;
	private String depVariable;
	private String indepVariable;
	private Double avgMonNum;
	private Double regConstant;
	private Double regCoef;
	private Double regRsqr;
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public String getDepVariable() {
		return depVariable;
	}
	public void setDepVariable(String depVariable) {
		this.depVariable = depVariable;
	}
	public String getIndepVariable() {
		return indepVariable;
	}
	public void setIndepVariable(String indepVariable) {
		this.indepVariable = indepVariable;
	}
	public Double getAvgMonNum() {
		return avgMonNum;
	}
	public void setAvgMonNum(Double avgMonNum) {
		this.avgMonNum = avgMonNum;
	}
	public Double getRegConstant() {
		return regConstant;
	}
	public void setRegConstant(Double regConstant) {
		this.regConstant = regConstant;
	}
	public Double getRegCoef() {
		return regCoef;
	}
	public void setRegCoef(Double regCoef) {
		this.regCoef = regCoef;
	}
	public Double getRegRsqr() {
		return regRsqr;
	}
	public void setRegRsqr(Double regRsqr) {
		this.regRsqr = regRsqr;
	}
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(depVariable).append(delimeter)
			   .append(indepVariable).append(delimeter)
			   .append(avgMonNum).append(delimeter)
			   .append(regConstant).append(delimeter)
			   
			   
			   .append(regCoef).append(delimeter)
			   .append(regRsqr).append(delimeter)
//			   .append(lastModifiedBy).append(delimeter)
//			   .append(lastUpdateDate)
			   ;
		
		return builder.append("\n").toString();
	}
}
