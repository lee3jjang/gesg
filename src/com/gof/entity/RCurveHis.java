package com.gof.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gof.comparator.RCurveHisComparator;

public class RCurveHis {
	
	private String baseYymm;
	private Double ktb1M;
	private Double ktb3Y;
	private Double ktb5Y;
	
	public RCurveHis() {
	
	}
	
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}
	public Double getKtb3Y() {
		return ktb3Y;
	}
	public void setKtb3Y(Double ktb3y) {
		ktb3Y = ktb3y;
	}
	public Double getKtb5Y() {
		return ktb5Y;
	}
	public void setKtb5Y(Double ktb5y) {
		ktb5Y = ktb5y;
	}
	
	public Double getKtb1M() {
		return ktb1M;
	}

	public void setKtb1M(Double ktb1m) {
		ktb1M = ktb1m;
	}

	public static List<RCurveHis> convertFrom(List<IrCurveHis> curveHis){
		List<RCurveHis> rst = new ArrayList<RCurveHis>();
		RCurveHis temp;
		
		Map<String, List<IrCurveHis>> rstMap = curveHis.stream().collect(Collectors.groupingBy(s->s.getBaseDate(), Collectors.toList()));
		for(Map.Entry<String, List<IrCurveHis>> entry :rstMap.entrySet()) {
			temp = new RCurveHis();
			temp.setBaseYymm(entry.getKey());
			temp.setKtb3Y(0.0);
			temp.setKtb5Y(0.0);
			for(IrCurveHis aa : entry.getValue()) {
				if(aa.getMatCd().equals("M0036")) {
					temp.setKtb3Y(aa.getIntRate());
				}else if( aa.getMatCd().equals("M0060")) {
					temp.setKtb5Y(aa.getIntRate());
				}else {
					temp.setKtb1M(aa.getIntRate());
				}
			}
			rst.add(temp);
		}
		rst.sort(new RCurveHisComparator());
		
		return rst;
	}
}
