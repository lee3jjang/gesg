package com.gof.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.gof.enums.ETopDownMatCd;
import com.gof.interfaces.EntityIdentifier;

@Entity
@IdClass(AssetYieldId.class)
@Table(name = "EAS_ASST_YIELD")

public class AssetYield implements Serializable, EntityIdentifier {

	private static final long serialVersionUID = -8158796295253534107L;

	@Id
	private String baseYymm;
	@Id
	private String expoId;
	private String expoName;
	private String prodTypCd;
	private String assetClassTypCd;
	private String crdGrdCd;
	private String matCd;
	private String issueDate;
	private String matDate;
	private Double couponRate;
	private Double bookBal;
	private Double asstYield;
	private Double residualSpread;
	private Double creditSpread;
	private String lastModifiedBy;
	private LocalDateTime lastUpdateDate;

	public AssetYield() {
	}

	public String getBaseYymm() {
		return baseYymm;
	}

	public void setBaseYymm(String baseYymm) {
		this.baseYymm = baseYymm;
	}

	public String getExpoId() {
		return expoId;
	}

	public void setExpoId(String expoId) {
		this.expoId = expoId;
	}

	public String getExpoName() {
		return expoName;
	}

	public void setExpoName(String expoName) {
		this.expoName = expoName;
	}

	public String getProdTypCd() {
		return prodTypCd;
	}

	public void setProdTypCd(String prodTypCd) {
		this.prodTypCd = prodTypCd;
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

	public String getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}

	public String getMatDate() {
		return matDate;
	}

	public void setMatDate(String matDate) {
		this.matDate = matDate;
	}

	public Double getCouponRate() {
		return couponRate;
	}

	public void setCouponRate(Double couponRate) {
		this.couponRate = couponRate;
	}

	public Double getBookBal() {
		return bookBal;
	}

	public void setBookBal(Double bookBal) {
		this.bookBal = bookBal;
	}

	public Double getAsstYield() {
		return asstYield;
	}

	public void setAsstYield(Double asstYield) {
		this.asstYield = asstYield;
	}

	public Double getResidualSpread() {
		return residualSpread;
	}

	public void setResidualSpread(Double residualSpread) {
		this.residualSpread = residualSpread;
	}

	public Double getCreditSpread() {
		return creditSpread;
	}

	public void setCreditSpread(Double creditSpread) {
		this.creditSpread = creditSpread;
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
		return "AsstYield [baseYymm=" + baseYymm + ", expoId=" + expoId + ", prodTypCd=" + prodTypCd + ", asstYield="
				+ asstYield + "]";
	}

	public String toGroupingIdString() {
		return toGroupingIdString(",");
	}

	public String toGroupingIdString(String delimeter) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseYymm).append(delimeter)
				.append(assetClassTypCd).append(delimeter)
				// .append("ASSET" ).append(delimeter)
				// .append(crdGrdCd).append(delimeter)
				.append("ALL").append(delimeter)
				// .append(convertFormMatCd())
				.append(ETopDownMatCd.getBaseMatCd(matCd))
//				.append(matCd)
				;

		return builder.toString();

	}

	private String convertFormMatCd() {
		double matYearFrac = Double.parseDouble(this.matCd.split("M")[1]) / 12;
		String rst = "M0012";
		for (int i = 0; i < 20; i++) {

			if (i < matYearFrac) {
				continue;
			} else {
				return "M" + String.format("%04d", i * 12);
			}
		}
		return rst;
	}

}
