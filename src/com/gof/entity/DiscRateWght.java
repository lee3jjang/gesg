package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


@Entity
@IdClass(DiscRateWghtId.class)
@Table(name ="EAS_DISC_RATE_WGHT")
@FilterDef(name="discRateWghtEqBaseYymm", parameters= { @ParamDef(name="baseYymm", type="string") })

public class DiscRateWght implements Serializable {

	private static final long serialVersionUID = -1745251535302343975L;
	
	@Id
	private String baseYymm;
	
	@Id
	private String intRateCd;
	
	@Column(name="KTB_Y5_WGHT")
	private Double ktbY5Wght;
	
	@Column(name="CORP_Y3_WGHT")
	private Double corpY3Wght;
	
	@Column(name="MNSB_Y1_WGHT")
	private Double mnsbY1Wght;
	
	@Column(name="CD_91_WGHT")
	private Double cd91Wght;
	
	private Double discRateSpread;
	
	@Column(name="EXTR_IR_WGHT")
	private Double extrIrWght;
	
	@ManyToOne
	@JoinColumn(name ="INT_RATE_CD", insertable=false, updatable= false)
	private DiscRateCalcSetting discSetting;

	public DiscRateWght() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIntRateCd() {
		return intRateCd;
	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public Double getKtbY5Wght() {
		return ktbY5Wght;
	}

	public void setKtbY5Wght(Double ktbY5Wght) {
		this.ktbY5Wght = ktbY5Wght;
	}

	public Double getCorpY3Wght() {
		return corpY3Wght;
	}

	public void setCorpY3Wght(Double corpY3Wght) {
		this.corpY3Wght = corpY3Wght;
	}

	public Double getMnsbY1Wght() {
		return mnsbY1Wght;
	}

	public void setMnsbY1Wght(Double mnsbY1Wght) {
		this.mnsbY1Wght = mnsbY1Wght;
	}

	public Double getCd91Wght() {
		return cd91Wght;
	}

	public void setCd91Wght(Double cd91Wght) {
		this.cd91Wght = cd91Wght;
	}

	public Double getDiscRateSpread() {
		return discRateSpread;
	}

	public void setDiscRateSpread(Double discRateSpread) {
		this.discRateSpread = discRateSpread;
	}
	public DiscRateCalcSetting getDiscSetting() {
		return discSetting;
	}

	public void setDiscSetting(DiscRateCalcSetting discSetting) {
		this.discSetting = discSetting;
	}
	public Double getExtrIrWght() {
		return extrIrWght;
	}

	public void setExtrIrWght(Double extrIrWght) {
		this.extrIrWght = extrIrWght;
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
		return "DiscRateWghtUd [baseYymm=" + baseYymm + ", intRateCd=" + intRateCd + ", ktbY5Wght=" + ktbY5Wght
				+ ", corpY3Wght=" + corpY3Wght + ", mnsbY1Wght=" + mnsbY1Wght + ", cd91Wght=" + cd91Wght
				+ ", discRateSpread=" + discRateSpread + "]";
	}
	
}


