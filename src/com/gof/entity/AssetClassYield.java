package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.interfaces.EntityIdentifier;


@Entity
@IdClass(AssetClassYieldId.class)
@Table(name ="EAS_ASST_CLASS_YIELD")

public class AssetClassYield implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -8158796295253534107L;
	
	@Id
	private String baseYymm;	

	@Id
	private String assetClassTypCd;
	
	@Id
	private String crdGrdCd;
	
	@Id
	private String matCd;
	
	@Column(name="ASST_YIELD")
	private Double assetYield;	
	
	@Column(name="BOOK_BAL")
	private Double bookAmt;
	
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;
	
	public AssetClassYield() {}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getAssetClassTypCd() {
		return assetClassTypCd;
	}

	public void setAssetClassTypCd(String assetClassTypCd) {
		this.assetClassTypCd = assetClassTypCd;
	}

	public String getCrdGrdCd() {
		return crdGrdCd;
	}

	public void setCrdGrdCd(String crdGrdCd) {
		this.crdGrdCd = crdGrdCd;
	}

	public String getMatCd() {
		return matCd;
	}

	public void setMatCd(String matCd) {
		this.matCd = matCd;
	}

	public Double getAssetYield() {
		return assetYield;
	}

	public void setAssetYield(Double assetYield) {
		this.assetYield = assetYield;
	}

	public Double getBookAmt() {
		return bookAmt;
	}

	public void setBookAmt(Double bookAmt) {
		this.bookAmt = bookAmt;
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
}


