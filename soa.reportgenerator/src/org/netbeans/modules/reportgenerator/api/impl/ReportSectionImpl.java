package org.netbeans.modules.reportgenerator.api.impl;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.reportgenerator.api.ReportElement;
import org.netbeans.modules.reportgenerator.api.ReportNode;
import org.netbeans.modules.reportgenerator.api.ReportSection;

public class ReportSectionImpl extends ReportElementImpl implements ReportSection {

	private List<ReportElement> mElements = new ArrayList<ReportElement>();
	
	public void addReportElement(ReportElement element) {
		this.mElements.add(element);
	}

	public void removeReportElement(ReportElement element) {
		this.mElements.remove(element);
	}

	public List<ReportElement> getReportElements() {
		return this.mElements;
	}

}
