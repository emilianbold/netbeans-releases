package org.netbeans.modules.reportgenerator.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.api.ReportSection;

public class ReportBodyImpl implements ReportBody {
	
	private List<ReportSection> mSections = new ArrayList<ReportSection>();
	
	
	public void addReportSection(ReportSection section) {
		this.mSections.add(section);
	}

	public List<ReportSection> getReportSection() {
		return this.mSections;
	}

	public void removeReportSection(ReportSection section) {
		this.mSections.remove(section);
	}

}
