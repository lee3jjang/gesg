package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.interfaces.BaseValue;
import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(SegLgdId.class)
@Table( name ="EAS_SEG_LGD")

public class SegLgd implements Serializable, EntityIdentifier, BaseValue {

	private static final long serialVersionUID = 2360652480675748510L;

	@Id
	private String baseYymm;
	
	@Id
	private String lgdCalcTypCd;
	
	@Id
	private String segId;	
	
    private Double lgd;
    
	private Double vol;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
    @Transient
//	@ManyToOne(fetch=FetchType.LAZY)
//	@JoinColumn(name ="SEG_ID", insertable=false, updatable=false)
//	@NotFound(action=NotFoundAction.IGNORE)
	private Seg seg;
    
	public SegLgd() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getLgdCalcTypCd() {
		return lgdCalcTypCd;
	}

	public void setLgdCalcTypCd(String lgdCalcTypCd) {
		this.lgdCalcTypCd = lgdCalcTypCd;
	}

	public String getSegId() {
		return segId;
	}

	public void setSegId(String segId) {
		this.segId = segId;
	}

	public Double getLgd() {
		return lgd;
	}

	public void setLgd(Double lgd) {
		this.lgd = lgd;
	}

	public Seg getSeg() {
		return seg;
	}

	public void setSeg(Seg seg) {
		this.seg = seg;
	}

	public Double getVol() {
		return vol;
	}

	public void setVol(Double vol) {
		this.vol = vol;
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
	public Double getBasicValue() {
		return lgd;
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

//	@Override
//	public String toString() {
//		return "SegLgd [baseYymm=" + baseYymm + ", lgdCalcTypCd=" + lgdCalcTypCd + ", segId=" + segId + ", lgd=" + lgd
//				+ ", seg=" + seg + "]";
//	}
	
	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseYymm).append(delimeter)
			   .append(lgdCalcTypCd).append(delimeter)
			   .append(segId).append(delimeter)
			   .append(lgd).append(delimeter)
			   .append(vol)
			   ;
		
		return builder.toString();
	}
		
}


