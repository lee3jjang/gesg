package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.IIntRate;
import com.gof.util.FinUtils;

@Entity
@IdClass(IrCurveHisId.class)
@Table(name ="EAS_IR_CURVE_WEEK")
@FilterDef(name="irCurveEqBaseDate", parameters= @ParamDef(name ="baseDate",  type="string"))
public class IrCurveWeek {
	
	private String baseDate; 
	private String irCurveId;
	private String matCd;
	private Double intRate;
	
	private String dayOfWeek;
	private IrCurve irCurve;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public IrCurveWeek() {
	
	}
	public IrCurveWeek(String baseDate, String matCd, Double intRate) {
		this.baseDate = baseDate;
		this.matCd = matCd;
		this.intRate = intRate;
	}
	
	public IrCurveWeek(String bssd, IrCurveWeek curveHis) {
		this.baseDate = curveHis.getBaseDate();
		this.irCurveId = curveHis.getIrCurveId();
		this.matCd = curveHis.getMatCd();
		this.intRate = curveHis.getIntRate();
				
	}
	@Id
	@Column(name ="BASE_DATE")
	public String getBaseDate() {
		return baseDate;
	}
	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}
	@Id
	@Column(name ="IR_CURVE_ID")
	public String getIrCurveId() {
		return irCurveId;
	}
	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}
	
	@Id
	@Column(name ="MAT_CD")
	public String getMatCd() {
		return matCd;
	}
	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}
	@Column(name ="INT_RATE")
	public Double getIntRate() {
		return intRate;
	}
	public void setIntRate(Double intRate) {
		this.intRate = intRate;
	}
	
	public String getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	@ManyToOne
	@JoinColumn(name ="IR_CURVE_ID", insertable=false, updatable= false)
	public IrCurve getIrCurve() {
		return irCurve;
	}
	public void setIrCurve(IrCurve irCurve) {
		this.irCurve = irCurve;
	}

	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public LocalDateTime getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
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
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseDate).append(delimeter)
			   .append(irCurveId).append(delimeter)
			   .append(matCd).append(delimeter)
			   .append(intRate).append(delimeter)
			   ;

		return builder.toString();
	}
//******************************************************Biz Method**************************************
	
	public IrCurveHis convertToHis() {
		IrCurveHis rst = new IrCurveHis();
		
		rst.setBaseDate(this.baseDate);
		rst.setIrCurveId(this.irCurveId);
		rst.setMatCd(this.matCd);
		rst.setIntRate(this.intRate);
		rst.setIrCurve(this.irCurve);
		
		return rst;
	}
}
