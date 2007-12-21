package org.netbeans.modules.reportgenerator.api.impl;

import org.netbeans.modules.reportgenerator.api.ReportFooter;

public class ReportFooterImpl implements ReportFooter {

	private String mDescription;
	
	public String getDescription() {
		return this.mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

}
