package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(SegLgdUdId.class)
@Table( name ="EAS_USER_SEG_LGD")
public class SegLgdUd implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -5982886374650308033L;

	@Id
	private String applStYymm;
	
	@Id
	private String lgdCalcTypCd;
	
	@Id
	private String segId;	
	
    private String applEdYymm;
	
	private Double lgd;
	

    
	@Transient
//	@ManyToOne(fetch=FetchType.LAZY)
//	@JoinColumn(name ="SEG_ID", insertable=false, updatable=false)
//	@NotFound(action=NotFoundAction.IGNORE)
	private Seg seg;
    
	public SegLgdUd() {}

	public String getApplStYymm() {
		return applStYymm;
	}

	public void setApplStYymm(String applStYymm) {
		this.applStYymm = applStYymm;
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

	public String getApplEdYymm() {
		return applEdYymm;
	}

	public void setApplEdYymm(String applEdYymm) {
		this.applEdYymm = applEdYymm;
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
		return "SegLgdUd [applStYymm=" + applStYymm + ", lgdCalcTypCd=" + lgdCalcTypCd + ", segId=" + segId
				+ ", applEdYymm=" + applEdYymm + ", lgd=" + lgd + ", seg=" + seg + "]";
	}	

}


