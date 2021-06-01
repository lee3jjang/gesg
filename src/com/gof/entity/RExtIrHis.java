package com.gof.entity;

import java.util.ArrayList;
import java.util.List;

public class RExtIrHis {
	
	private String baseYymm;
	private Double ktb5Y;
	private Double corp3Y;
	private Double mnsb1Y;
	private Double cd91D;
	
	public RExtIrHis() {
	
	}
	
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	
	public Double getKtb5Y() {
		return ktb5Y;
	}

	public void setKtb5Y(Double ktb5y) {
		ktb5Y = ktb5y;
	}

	public Double getCorp3Y() {
		return corp3Y;
	}

	public void setCorp3Y(Double corp3y) {
		corp3Y = corp3y;
	}

	public Double getMnsb1Y() {
		return mnsb1Y;
	}

	public void setMnsb1Y(Double mnsb1y) {
		mnsb1Y = mnsb1y;
	}

	public Double getCd91D() {
		return cd91D;
	}

	public void setCd91D(Double cd91d) {
		cd91D = cd91d;
	}

	public static List<RExtIrHis> convertFrom(List<UserDiscRateExBaseIr> extIrHis){
		List<RExtIrHis> rst = new ArrayList<RExtIrHis>();
		RExtIrHis temp;
		
		for(UserDiscRateExBaseIr  entry : extIrHis) {
			temp = new RExtIrHis();
			temp.setBaseYymm(entry.getBaseYymm());
			temp.setKtb5Y(entry.getKtbY5Ir()/100);
			temp.setCorp3Y(entry.getCorpY3Ir()/100);
			temp.setMnsb1Y(entry.getMnsbY1Ir()/100);
			temp.setCd91D(entry.getCd91Ir()/100);
			rst.add(temp);
		}
		
		return rst;
	}
}
