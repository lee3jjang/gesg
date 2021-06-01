package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(IrShockId.class)
@Table(name ="EAS_IR_SHOCK")
@Access(AccessType.FIELD)
public class IrShock implements Serializable {
	
	private static final long serialVersionUID = -7783664746646277314L;

	@Id
	private String baseYymm; 
	
	@Id
	private String irShockTyp;
	
	@Id
	private String irCurveId;	
	
	@Id
	private String shockTypCd;
	
	@Id
	private String matCd;	
	
	private Double shockVal;
	
	private String lastModifiedBy;
	
	private LocalDateTime lastUpdateDate;
	
	public IrShock() {}

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

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getIrShockTyp() {
		return irShockTyp;
	}

	public void setIrShockTyp(String irShockTyp) {
		this.irShockTyp = irShockTyp;
	}

	public String getIrCurveId() {
		return irCurveId;
	}

	public void setIrCurveId(String irCurveId) {
		this.irCurveId = irCurveId;
	}

	public String getShockTypCd() {
		return shockTypCd;
	}

	public void setShockTypCd(String shockTypCd) {
		this.shockTypCd = shockTypCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getShockVal() {
		return shockVal;
	}

	public void setShockVal(Double shockVal) {
		this.shockVal = shockVal;
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

	
}
