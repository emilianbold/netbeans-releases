package org.netbeans.modules.reportgenerator.api.impl;

import org.netbeans.modules.reportgenerator.api.ReportHeader;

public class ReportHeaderImpl implements ReportHeader {

	private String mDescription;
	
	public String getDescription() {
		return this.mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

}
