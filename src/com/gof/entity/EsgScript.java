package com.gof.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.gof.enums.EBoolean;


@Entity
@Table( name ="EAS_ESG_SCRIPT")
public class EsgScript implements Serializable{

	private static final long serialVersionUID = 5370330968175334208L;

	@Id	
	@Column(name ="SCRIPT_ID")	
	private String scriptId;
	
	@Column(name ="SCRIPT_NM")
	private String scriptNm;	
	
	@Column(name ="SCRIPT_TYP")
	private String scriptType;
	
	@Enumerated(EnumType.STRING)
	private EBoolean useYn;

	@Column(name="SCRIPT_CONTENT")
	private String scriptContent;	
	
	public EsgScript() {}

	public String getScriptId() {
		return scriptId;
	}
	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}
	public String getScriptNm() {
		return scriptNm;
	}
	public void setScriptNm(String scriptNm) {
		this.scriptNm = scriptNm;
	}
	public String getScriptType() {
		return scriptType;
	}
	public void setScriptType(String scriptType) {
		this.scriptType = scriptType;
	}
	public EBoolean getUseYn() {
		return useYn;
	}
	public void setUseYn(EBoolean useYn) {
		this.useYn = useYn;
	}
	public String getScriptContent() {
		return scriptContent;
	}
	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
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
		return "EsgMstTmp [scriptId=" + scriptId + ", scriptNm=" + scriptNm + ", scriptContent=" + scriptContent + "]";
	}	

}
