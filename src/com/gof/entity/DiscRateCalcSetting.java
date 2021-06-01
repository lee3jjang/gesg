package com.gof.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;


@Entity
//@IdClass(DiscRateCalcSettingId.class)
@Table(name ="EAS_DISC_RATE_CALC_SETTING")
public class DiscRateCalcSetting implements Serializable {

	private static final long serialVersionUID = -1727949379975877048L;

//	@Id
	private String applStYymm;
	
//	@Id
//	@Column(name ="DISC_RATE_CALC_TYP")
//	private String discRateType;
	
	@Id
	private String intRateCd;		
	
	private String applEdYymm;	
	
	private String acctDvCd;	
	
	private String applMatDv;
	
	private String unrealizedPlInclYn;
	
	private String exBaseIrCalcPeriod;	
	
	private Double exBaseIrWght;	
	
	private String remark;
	
	@Transient
//	@OneToMany(mappedBy ="discSetting")
//	@Filter(name ="discRateHisEqBaseYymm", condition= "BASE_YYMM =:baseYymm")
	private List<DiscRateHis> hisList = new ArrayList<DiscRateHis>();
	
	@Transient
//	@OneToMany(mappedBy ="discSetting")
//	@Filter(name ="discRateStatEqApplStYymm", condition= "APPL_ST_YYMM = :applStYymm")
	private List<DiscRateStats> statsList = new ArrayList<DiscRateStats>();
	
	@Transient
//	@OneToMany(mappedBy ="discSetting")
//	@Filter(name ="discRateWghtEqBaseYymm", condition= "BASE_YYMM =:baseYymm")
	private List<DiscRateWght> wghtList = new ArrayList<>();
	
	
	public DiscRateCalcSetting() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
	}

//	public String getDiscRateCalcTyp() {
//		return discRateCalcTyp;
//	}
//
//	public void setDiscRateCalcTyp(String discRateCalcTyp) {
//		this.discRateCalcTyp = discRateCalcTyp;
//	}
	public String getIntRateCd() {
		return intRateCd;
	}

//	public String getDiscRateType() {
//		return discRateType;
//	}
//
//	public void setDiscRateType(String discRateType) {
//		this.discRateType = discRateType;
//	}

	public void setIntRateCd(String intRateCd) {
		this.intRateCd = intRateCd;
	}

	public String getApplEdYymm() {
		return applEdYymm;
	}

	public void setApplEdYymm(String applEdYymm) {
		this.applEdYymm = applEdYymm;
	}

	public String getAcctDvCd() {
		return acctDvCd;
	}

	public void setAcctDvCd(String acctDvCd) {
		this.acctDvCd = acctDvCd;
	}

	public String getApplMatDv() {
		return applMatDv;
	}

	public void setApplMatDv(String applMatDv) {
		this.applMatDv = applMatDv;
	}

	public String getUnrealizedPlInclYn() {
		return unrealizedPlInclYn;
	}

	public void setUnrealizedPlInclYn(String unrealizedPlInclYn) {
		this.unrealizedPlInclYn = unrealizedPlInclYn;
	}

	public String getExBaseIrCalcPeriod() {
		return exBaseIrCalcPeriod;
	}

	public void setExBaseIrCalcPeriod(String exBaseIrCalcPeriod) {
		this.exBaseIrCalcPeriod = exBaseIrCalcPeriod;
	}

	public Double getExBaseIrWght() {
		return exBaseIrWght;
	}

	public void setExBaseIrWght(Double exBaseIrWght) {
		this.exBaseIrWght = exBaseIrWght;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	
	public List<DiscRateHis> getHisList() {
		return hisList;
	}

	public void setHisList(List<DiscRateHis> hisList) {
		this.hisList = hisList;
	}

	public List<DiscRateStats> getStatsList() {
		return statsList;
	}

	public void setStatsList(List<DiscRateStats> statsList) {
		this.statsList = statsList;
	}

	public List<DiscRateWght> getWghtList() {
		return wghtList;
	}

	public void setWghtList(List<DiscRateWght> wghtList) {
		this.wghtList = wghtList;
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
		return "DiscRateCalcSetting [applStYymm=" + applStYymm  + ", intRateCd="
				+ intRateCd + ", applEdYymm=" + applEdYymm + ", acctDvCd=" + acctDvCd + ", applMatDv=" + applMatDv
				+ ", unrealizedPlInclYn=" + unrealizedPlInclYn + ", exBaseIrCalcPeriod=" + exBaseIrCalcPeriod
				+ ", exBaseIrWght=" + exBaseIrWght + ", remark=" + remark + "]";
	}	

//	***************************Biz *************************
	
	@Transient
	public String getCalcType() {
		return remark.split("_")[0];
	}
	@Transient
	public boolean isCalculable() {
		String temp = remark.split("_")[0];
		if(temp.equals("01") || temp.equals("02")) {
			return true;
		}
		else if(temp.equals("03")) {
			return false;
		}
		else {
			return false;
		}
//		if(discRateType.equals("01") || discRateType.equals("02")) {
//			return true;
//		}
//		else if(discRateType.equals("03")) {
//			return true;
//		}
//		else {
//			return false;
//		}
	}
	
}


