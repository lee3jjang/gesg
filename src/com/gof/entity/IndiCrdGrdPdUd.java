package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(IndiCrdGrdPdUdId.class)
@Table( name ="EAS_USER_INDI_CRD_GRD_PD")
public class IndiCrdGrdPdUd implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -4498270211514363191L;

	@Id
	private String baseYymm;

	@Id
	private String cbGrdCd;
	
	private Double kcbPd;	
	
	private Double nicePd;
	
	private Double y1;
	
	private Double y2;
	
	private Double y3;
	
	private Double y4;
	
	private Double y5;
	
	private Double y6;
	
	private Double y7;
	
	private Double y8;
	
	private Double y9;
	
	private Double y10;
	
	private Double y11;
	
	private Double y12;
	
	private Double y13;
	
	private Double y14;
	
	private Double y15;
	
	private Double y16;
	
	private Double y17;
	
	private Double y18;
	
	private Double y19;
	
	private Double y20;
	
	private Double y21;
	
	private Double y22;
	
	private Double y23;
	
	private Double y24;
	
	private Double y25;
	
	private Double y26;
	
	private Double y27;
	
	private Double y28;
	
	private Double y29;
	
	private Double y30;	
	
	public IndiCrdGrdPdUd() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getCbGrdCd() {
		return cbGrdCd;
	}

	public void setCbGrdCd(String cbGrdCd) {
		this.cbGrdCd = cbGrdCd;
	}

	public Double getKcbPd() {
		return kcbPd;
	}

	public void setKcbPd(Double kcbPd) {
		this.kcbPd = kcbPd;
	}

	public Double getNicePd() {
		return nicePd;
	}

	public void setNicePd(Double nicePd) {
		this.nicePd = nicePd;
	}

	public Double getY1() {
		return y1;
	}

	public void setY1(Double y1) {
		this.y1 = y1;
	}

	public Double getY2() {
		return y2;
	}

	public void setY2(Double y2) {
		this.y2 = y2;
	}

	public Double getY3() {
		return y3;
	}

	public void setY3(Double y3) {
		this.y3 = y3;
	}

	public Double getY4() {
		return y4;
	}

	public void setY4(Double y4) {
		this.y4 = y4;
	}

	public Double getY5() {
		return y5;
	}

	public void setY5(Double y5) {
		this.y5 = y5;
	}

	public Double getY6() {
		return y6;
	}

	public void setY6(Double y6) {
		this.y6 = y6;
	}

	public Double getY7() {
		return y7;
	}

	public void setY7(Double y7) {
		this.y7 = y7;
	}

	public Double getY8() {
		return y8;
	}

	public void setY8(Double y8) {
		this.y8 = y8;
	}

	public Double getY9() {
		return y9;
	}

	public void setY9(Double y9) {
		this.y9 = y9;
	}

	public Double getY10() {
		return y10;
	}

	public void setY10(Double y10) {
		this.y10 = y10;
	}

	public Double getY11() {
		return y11;
	}

	public void setY11(Double y11) {
		this.y11 = y11;
	}

	public Double getY12() {
		return y12;
	}

	public void setY12(Double y12) {
		this.y12 = y12;
	}

	public Double getY13() {
		return y13;
	}

	public void setY13(Double y13) {
		this.y13 = y13;
	}

	public Double getY14() {
		return y14;
	}

	public void setY14(Double y14) {
		this.y14 = y14;
	}

	public Double getY15() {
		return y15;
	}

	public void setY15(Double y15) {
		this.y15 = y15;
	}

	public Double getY16() {
		return y16;
	}

	public void setY16(Double y16) {
		this.y16 = y16;
	}

	public Double getY17() {
		return y17;
	}

	public void setY17(Double y17) {
		this.y17 = y17;
	}

	public Double getY18() {
		return y18;
	}

	public void setY18(Double y18) {
		this.y18 = y18;
	}

	public Double getY19() {
		return y19;
	}

	public void setY19(Double y19) {
		this.y19 = y19;
	}

	public Double getY20() {
		return y20;
	}

	public void setY20(Double y20) {
		this.y20 = y20;
	}

	public Double getY21() {
		return y21;
	}

	public void setY21(Double y21) {
		this.y21 = y21;
	}

	public Double getY22() {
		return y22;
	}

	public void setY22(Double y22) {
		this.y22 = y22;
	}

	public Double getY23() {
		return y23;
	}

	public void setY23(Double y23) {
		this.y23 = y23;
	}

	public Double getY24() {
		return y24;
	}

	public void setY24(Double y24) {
		this.y24 = y24;
	}

	public Double getY25() {
		return y25;
	}

	public void setY25(Double y25) {
		this.y25 = y25;
	}

	public Double getY26() {
		return y26;
	}

	public void setY26(Double y26) {
		this.y26 = y26;
	}

	public Double getY27() {
		return y27;
	}

	public void setY27(Double y27) {
		this.y27 = y27;
	}

	public Double getY28() {
		return y28;
	}

	public void setY28(Double y28) {
		this.y28 = y28;
	}

	public Double getY29() {
		return y29;
	}

	public void setY29(Double y29) {
		this.y29 = y29;
	}

	public Double getY30() {
		return y30;
	}

	public void setY30(Double y30) {
		this.y30 = y30;
	}

	
	public double getPd(String agencyCode) {
		if("03".equals(agencyCode)) {
			return nicePd;
		}
		else if("08".equals(agencyCode)) {
			return kcbPd;
		}
		else {
			return (kcbPd + nicePd) /2.0;
		}
	}
	
	public Double  getFwdChangeRate(int yearNum){
		     if(yearNum ==1) {return y1 ;} 
		else if(yearNum ==2) {return y2 ;}
		else if(yearNum ==3) {return y3 ;}
		else if(yearNum ==4) {return y4 ;}
		else if(yearNum ==5) {return y5 ;}
		else if(yearNum ==6) {return y6 ;}
		else if(yearNum ==7) {return y7 ;}
		else if(yearNum ==8) {return y8 ;}
		else if(yearNum ==9) {return y9 ;}
		else if(yearNum ==10) {return y10 ;}
		else if(yearNum ==11) {return y11 ;}
		else if(yearNum ==12) {return y12 ;}
		else if(yearNum ==13) {return y13 ;}
		else if(yearNum ==14) {return y14 ;}
		else if(yearNum ==15) {return y15 ;}
		else if(yearNum ==16) {return y16 ;}
		else if(yearNum ==17) {return y17 ;}
		else if(yearNum ==18) {return y18 ;}
		else if(yearNum ==19) {return y19 ;}
		else if(yearNum ==20) {return y20 ;}
		else if(yearNum ==21) {return y21 ;}
		else if(yearNum ==22) {return y22 ;}
		else if(yearNum ==23) {return y23 ;}
		else if(yearNum ==24) {return y24 ;}
		else if(yearNum ==25) {return y25 ;}
		else if(yearNum ==26) {return y26 ;}
		else if(yearNum ==27) {return y27 ;}
		else if(yearNum ==28) {return y28 ;}
		else if(yearNum ==29) {return y29 ;}
		else if(yearNum ==30) {return y30 ;}
		else 				  {return 1.0;}
	}

	
	
	public List<IndiCrdGrdCumPd> getCumPd(String bssd, String agencyCode, String methodType){
		List<IndiCrdGrdCumPd> rstList = new ArrayList<IndiCrdGrdCumPd>();
		
		IndiCrdGrdCumPd tempPd;
		
		double prevCumPd=0.0;
		double fwdPd=0.0;
		double currCumPd=0.0;
		
		
		for( int k=1 ; k<= 30; k++) {			//감독원 데이터가 30Y 증감율임.
			if("01".equals(methodType)) {
				currCumPd = Math.min(this.getPd(agencyCode)* this.getFwdChangeRate(k), 1.0);
				fwdPd= prevCumPd==1? 0.0: (currCumPd - prevCumPd) /(1-prevCumPd);
			}
			else if("02".equals(methodType)) {
				currCumPd = Math.min(this.getPd(agencyCode) *(1+ this.getFwdChangeRate(k)/100), 1.0);
				fwdPd= prevCumPd==1? 0.0: (currCumPd - prevCumPd) /(1-prevCumPd);
			}
			else if("03".equals(methodType)) {
				fwdPd=  getPd(agencyCode) * getFwdChangeRate(k) ;
				currCumPd = Math.min( fwdPd + (1-fwdPd) * prevCumPd, 1.0);
			}
			else if("04".equals(methodType)) {
				fwdPd=  getPd(agencyCode) * (1 + getFwdChangeRate(k) / 100.0);
				currCumPd = Math.min( fwdPd + (1-fwdPd) * prevCumPd, 1.0);
			}
			
			tempPd = new IndiCrdGrdCumPd();
			
			tempPd.setBaseYymm(bssd);
			tempPd.setCrdEvalAgncyCd(agencyCode);
			tempPd.setCbGrdCd(this.getCbGrdCd());
			tempPd.setMatCd("M"+ String.format("%04d", k*12));
			tempPd.setCumPd(currCumPd);
			tempPd.setFwdPd(fwdPd);
			tempPd.setCumPdChgRate(prevCumPd == 0? 0 :currCumPd/prevCumPd -1);
			tempPd.setVol(0.0);
			tempPd.setLastModifiedBy("ESG_52");
			tempPd.setLastUpdateDate(LocalDateTime.now());
			
			rstList.add(tempPd);
			prevCumPd = currCumPd;
		}
		return rstList;
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
		return "IndiCrdGrdPdUd [baseYymm=" + baseYymm + ", cbGrdCd=" + cbGrdCd + ", kcbPd=" + kcbPd + ", nicePd="
				+ nicePd + ", y1=" + y1 + ", y2=" + y2 + ", y3=" + y3 + ", y4=" + y4 + ", y5=" + y5 + ", y6=" + y6
				+ ", y7=" + y7 + ", y8=" + y8 + ", y9=" + y9 + ", y10=" + y10 + ", y11=" + y11 + ", y12=" + y12
				+ ", y13=" + y13 + ", y14=" + y14 + ", y15=" + y15 + ", y16=" + y16 + ", y17=" + y17 + ", y18=" + y18
				+ ", y19=" + y19 + ", y20=" + y20 + ", y21=" + y21 + ", y22=" + y22 + ", y23=" + y23 + ", y24=" + y24
				+ ", y25=" + y25 + ", y26=" + y26 + ", y27=" + y27 + ", y28=" + y28 + ", y29=" + y29 + ", y30=" + y30
				+ "]";
	}
		
}


