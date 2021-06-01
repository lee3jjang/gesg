package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@Table(name ="EAS_SEG")
public class Seg implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4501378275214851933L;

	@Id	
	@Column(name ="SEG_ID")	
	private String segId;
	
	private String segNm;
	
	private String rcTypCd;
	
	public Seg() {}

	public String getSegId() {
		return segId;
	}

	public void setSegId(String segId) {
		this.segId = segId;
	}

	public String getSegNm() {
		return segNm;
	}

	public void setSegNm(String segNm) {
		this.segNm = segNm;
	}

	public String getRcTypCd() {
		return rcTypCd;
	}

	public void setRcTypCd(String rcTypCd) {
		this.rcTypCd = rcTypCd;
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
		return "Seg [segId=" + segId + ", segNm=" + segNm + ", rcTypCd=" + rcTypCd + "]";
	}	

}
