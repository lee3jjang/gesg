package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gof.interfaces.EntityIdentifier;



//@Entity
//@IdClass(IrSceId.class)
//@Table(schema="QCM", name ="EAS_IR_SCE")

public class MvSce implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = 4458482460359847563L;

//	@Id
	private String baseDate;
	
//    @Id
	private String modelId;

//    @Id
    private String sceNo;

//    @Id
	private String mvId;	
	
	private Double mvValue;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
		
	public MvSce() {}

	public String getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getSceNo() {
		return sceNo;
	}

	public void setSceNo(String sceNo) {
		this.sceNo = sceNo;
	}

	public String getMvId() {
		return mvId;
	}

	public void setMvId(String mvId) {
		this.mvId = mvId;
	}

	public Double getMvValue() {
		return mvValue;
	}

	public void setMvValue(Double mvValue) {
		this.mvValue = mvValue;
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
		return super.equals(arg0);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return toString(",");
	}
	
	public String toString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(baseDate).append(delimeter)
			   .append(modelId).append(delimeter)
			   .append(sceNo).append(delimeter)
			   .append(mvId).append(delimeter)
			   .append(mvValue).append(delimeter)
			   .append(lastModifiedBy).append(delimeter)
			   .append(lastUpdateDate);
		
		return builder.toString();
	}
}


