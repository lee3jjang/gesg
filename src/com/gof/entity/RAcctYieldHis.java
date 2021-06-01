package com.gof.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gof.comparator.RAcctYiedlHisComparator;

public class RAcctYieldHis  {
	
	private String baseYymm;
	private Double longIns;
	private Double indiPenson;
	
	public RAcctYieldHis() {
	
	}
	
	public String getBaseYymm() {
		return baseYymm;
	}
	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public Double getLongIns() {
		return longIns;
	}

	public void setLongIns(Double longIns) {
		this.longIns = longIns;
	}

	public Double getIndiPenson() {
		return indiPenson;
	}

	public void setIndiPenson(Double indiPenson) {
		this.indiPenson = indiPenson;
	}

	public static List<RAcctYieldHis> convertFrom(List<UserDiscRateAsstRevnCumRate> assetYield, boolean isRevenue){
		List<RAcctYieldHis> rst = new ArrayList<RAcctYieldHis>();
		RAcctYieldHis temp;
		
		
		Map<String, List<UserDiscRateAsstRevnCumRate>> rstMap = assetYield.stream().collect(Collectors.groupingBy(s->s.getBaseYymm(), Collectors.toList()));
		
		for(Map.Entry<String, List<UserDiscRateAsstRevnCumRate>> entry :rstMap.entrySet()) {
			temp = new RAcctYieldHis();
			temp.setBaseYymm(entry.getKey());
			
			for(UserDiscRateAsstRevnCumRate aa : entry.getValue()) {
				if(aa.getAcctDvCd().contains("8300")) {
					if(isRevenue) {
						temp.setLongIns(aa.getMgtAsstRevnCumRate());
					}
					else {
						temp.setLongIns(aa.getMgtAsstCumYield());
					}
				}else if( aa.getAcctDvCd().contains("8100")) {
					if(isRevenue) {
						
						temp.setIndiPenson(aa.getMgtAsstRevnCumRate());
					}
					else {
						temp.setIndiPenson(aa.getMgtAsstCumYield());
					}
					
					
				}
			}
			rst.add(temp);
		}
		rst.sort(new RAcctYiedlHisComparator());
		
		return rst;
	}
}
