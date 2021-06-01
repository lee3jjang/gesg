package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
public class LogTableId implements Serializable{
	
	private String prjtNm;
	private String prjtPtlNo;
	private LocalDateTime prjtPtlStrDttm;
	
	@Column(name ="PRJT_NM")
	public String getPrjtNm() {
		return prjtNm;
	}


	public void setPrjtNm(String prjtNm) {
		this.prjtNm = prjtNm;
	}

	@Column(name ="PRJT_PTL_NO")
	public String getPrjtPtlNo() {
		return prjtPtlNo;
	}


	public void setPrjtPtlNo(String prjtPtlNo) {
		this.prjtPtlNo = prjtPtlNo;
	}

	@Column(name ="PRJT_PTL_STR_DTTM")
	public LocalDateTime getPrjtPtlStrDttm() {
		return prjtPtlStrDttm;
	}


	public void setPrjtPtlStrDttm(LocalDateTime prjtPtlStrDttm) {
		this.prjtPtlStrDttm = prjtPtlStrDttm;
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
