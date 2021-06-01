package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;

import com.gof.interfaces.EntityIdentifier;


@Entity
@Table(name ="EAS_TOPDOWN_ASST_CLASS")
@FilterDef(name="ASST_FILTER", parameters= { @ParamDef(name="asstClassTypCd", type="string") })
@Filters( { @Filter(name ="ASST_FILTER", condition="ASST_CLASS_TYP_CD = :asstClassTypCd") } )
public class TopDownAsstClass implements Serializable, EntityIdentifier {	
	
	private static final long serialVersionUID = -8293371374588012080L;

	@Id
	@Column(name="ASST_CLASS_TYP_CD", nullable=false)
	private String asstClassTypCd;
	
	private String asstClassTypNm;
	
	public TopDownAsstClass() {}

	public String getAsstClassTypCd() {
		return asstClassTypCd;
	}

	public void setAsstClassTypCd(String asstClassTypCd) {
		this.asstClassTypCd = asstClassTypCd;
	}

	public String getAsstClassTypNm() {
		return asstClassTypNm;
	}

	public void setAsstClassTypNm(String asstClassTypNm) {
		this.asstClassTypNm = asstClassTypNm;
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
		return "TopdownAsstClass [asstClassTypCd=" + asstClassTypCd + ", asstClassTypNm=" + asstClassTypNm + "]";
	}	

}


