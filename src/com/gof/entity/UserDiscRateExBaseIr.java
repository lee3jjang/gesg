package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@Table(name ="EAS_USER_DISC_RATE_EX_BASE_IR")
public class UserDiscRateExBaseIr implements Serializable, EntityIdentifier {
	
	private static final long serialVersionUID = -295414795997288198L;

	@Id
	@Column(name="BASE_YYMM", nullable=false)
	private String baseYymm;	
	
	@Column(name="KTB_Y5_IR")
	private Double ktbY5Ir;
	
	@Column(name="CORP_Y3_IR")
	private Double corpY3Ir;
	
	@Column(name="MNSB_Y1_IR")
	private Double mnsbY1Ir;
	
	@Column(name="CD_91_IR")
	private Double cd91Ir;

	public UserDiscRateExBaseIr() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public Double getKtbY5Ir() {
		return ktbY5Ir;
	}

	public void setKtbY5Ir(Double ktbY5Ir) {
		this.ktbY5Ir = ktbY5Ir;
	}

	public Double getCorpY3Ir() {
		return corpY3Ir;
	}

	public void setCorpY3Ir(Double corpY3Ir) {
		this.corpY3Ir = corpY3Ir;
	}

	public Double getMnsbY1Ir() {
		return mnsbY1Ir;
	}

	public void setMnsbY1Ir(Double mnsbY1Ir) {
		this.mnsbY1Ir = mnsbY1Ir;
	}

	public Double getCd91Ir() {
		return cd91Ir;
	}

	public void setCd91Ir(Double cd91Ir) {
		this.cd91Ir = cd91Ir;
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
		return "UserDiscRateExBaseIr [baseYymm=" + baseYymm + ", ktbY5Ir=" + ktbY5Ir + ", corpY3Ir=" + corpY3Ir
				+ ", mnsbY1Ir=" + mnsbY1Ir + ", cd91Ir=" + cd91Ir + "]";
	}

}


