package org.netbeans.modules.reportgenerator.api;

import java.util.List;

/**
 * ReportSection represents a section in the report.
 * A ReportSection consists of ReportElements.
 * @author radval
 *
 */
public interface ReportSection extends ReportElement {
	
	/**
	 * Add a ReportElement
	 * @param element
	 */
	void addReportElement(ReportElement element);
	
	/**
	 * remove a ReportElement
	 * @param element
	 */
	void removeReportElement(ReportElement element);
	
	/**
	 * Get a list of ReportElements.
	 * @return
	 */
	List<ReportElement> getReportElements();
	
}
