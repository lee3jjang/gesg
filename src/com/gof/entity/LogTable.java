package com.gof.entity;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(LogTableId.class)
@Table(name ="FFMTFC02003", schema="IMSFM")
public class LogTable implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4104586581897784001L;
	
	@Id	
	private String prjtNm;
	
	@Id
	private String prjtPtlNo;
	
	@Id
	private LocalDateTime prjtPtlStrDttm;
	
	private String batTypNm;
	
	private LocalDateTime prjtPtlFinDttm;
	private String prjtPtlExctTime;
	private double tgExctCnum;
	private String bzDvnNm;
	private String prjtExctStatNm;
	private String prjtPtlOprRslNm;
	private String prjtRutNm;
	private String etlOutpFinm;
	private String etlPrjtId;
	
	private double etlPrjtExctSno;
	private String bsePotmDvcdVal;
	private String bsePotmVal;
	private String errLogCn1;
	private String errLogCn2;
	private String frstIpmnEmpno;
	private String frstInptDttm;
	private String fnalAmdrEmpno;
	private String fnalUpdtDttm;
	
	
	
	public LogTable() {}



	public String getPrjtNm() {
		return prjtNm;
	}



	public void setPrjtNm(String prjtNm) {
		this.prjtNm = prjtNm;
	}



	public String getPrjtPtlNo() {
		return prjtPtlNo;
	}



	public void setPrjtPtlNo(String prjtPtlNo) {
		this.prjtPtlNo = prjtPtlNo;
	}



	public LocalDateTime getPrjtPtlStrDttm() {
		return prjtPtlStrDttm;
	}



	public void setPrjtPtlStrDttm(LocalDateTime prjtPtlStrDttm) {
		this.prjtPtlStrDttm = prjtPtlStrDttm;
	}



	public String getBatTypNm() {
		return batTypNm;
	}



	public void setBatTypNm(String batTypNm) {
		this.batTypNm = batTypNm;
	}



	public LocalDateTime getPrjtPtlFinDttm() {
		return prjtPtlFinDttm;
	}



	public void setPrjtPtlFinDttm(LocalDateTime prjtPtlFinDttm) {
		this.prjtPtlFinDttm = prjtPtlFinDttm;
	}



	public String getPrjtPtlExctTime() {
		return prjtPtlExctTime;
	}



	public void setPrjtPtlExctTime(String prjtPtlExctTime) {
		this.prjtPtlExctTime = prjtPtlExctTime;
	}



	public double getTgExctCnum() {
		return tgExctCnum;
	}



	public void setTgExctCnum(double tgExctCnum) {
		this.tgExctCnum = tgExctCnum;
	}



	public String getBzDvnNm() {
		return bzDvnNm;
	}



	public void setBzDvnNm(String bzDvnNm) {
		this.bzDvnNm = bzDvnNm;
	}



	public String getPrjtExctStatNm() {
		return prjtExctStatNm;
	}



	public void setPrjtExctStatNm(String prjtExctStatNm) {
		this.prjtExctStatNm = prjtExctStatNm;
	}



	public String getPrjtPtlOprRslNm() {
		return prjtPtlOprRslNm;
	}



	public void setPrjtPtlOprRslNm(String prjtPtlOprRslNm) {
		this.prjtPtlOprRslNm = prjtPtlOprRslNm;
	}



	public String getPrjtRutNm() {
		return prjtRutNm;
	}



	public void setPrjtRutNm(String prjtRutNm) {
		this.prjtRutNm = prjtRutNm;
	}



	public String getEtlOutpFinm() {
		return etlOutpFinm;
	}



	public void setEtlOutpFinm(String etlOutpFinm) {
		this.etlOutpFinm = etlOutpFinm;
	}



	public String getEtlPrjtId() {
		return etlPrjtId;
	}



	public void setEtlPrjtId(String etlPrjtId) {
		this.etlPrjtId = etlPrjtId;
	}



	public double getEtlPrjtExctSno() {
		return etlPrjtExctSno;
	}



	public void setEtlPrjtExctSno(double etlPrjtExctSno) {
		this.etlPrjtExctSno = etlPrjtExctSno;
	}



	public String getBsePotmDvcdVal() {
		return bsePotmDvcdVal;
	}



	public void setBsePotmDvcdVal(String bsePotmDvcdVal) {
		this.bsePotmDvcdVal = bsePotmDvcdVal;
	}



	public String getBsePotmVal() {
		return bsePotmVal;
	}



	public void setBsePotmVal(String bsePotmVal) {
		this.bsePotmVal = bsePotmVal;
	}



	public String getErrLogCn1() {
		return errLogCn1;
	}



	public void setErrLogCn1(String errLogCn1) {
		this.errLogCn1 = errLogCn1;
	}



	public String getErrLogCn2() {
		return errLogCn2;
	}



	public void setErrLogCn2(String errLogCn2) {
		this.errLogCn2 = errLogCn2;
	}



	public String getFrstIpmnEmpno() {
		return frstIpmnEmpno;
	}



	public void setFrstIpmnEmpno(String frstIpmnEmpno) {
		this.frstIpmnEmpno = frstIpmnEmpno;
	}



	public String getFrstInptDttm() {
		return frstInptDttm;
	}



	public void setFrstInptDttm(String frstInptDttm) {
		this.frstInptDttm = frstInptDttm;
	}



	public String getFnalAmdrEmpno() {
		return fnalAmdrEmpno;
	}



	public void setFnalAmdrEmpno(String fnalAmdrEmpno) {
		this.fnalAmdrEmpno = fnalAmdrEmpno;
	}



	public String getFnalUpdtDttm() {
		return fnalUpdtDttm;
	}



	public void setFnalUpdtDttm(String fnalUpdtDttm) {
		this.fnalUpdtDttm = fnalUpdtDttm;
	}



	public LogTable updateExecTime(LocalDateTime now) {
		long execTime = Duration.between(this.getPrjtPtlStrDttm(), now).getSeconds();
		String execString = String.format("%02d:%02d:%02d", execTime/3600, (execTime % 3600)/60, execTime % 60);
				
		this.setPrjtPtlFinDttm(now);
		this.setPrjtPtlExctTime(execString);
		this.setFnalUpdtDttm(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		return this;
	}
	
	
	public LogTable updateFail(Exception e) {
		this.setPrjtExctStatNm("FAILED");
		this.setPrjtPtlOprRslNm("FAILED");
		this.setErrLogCn1(e.getMessage());
		return this;
	}
	

	
}


