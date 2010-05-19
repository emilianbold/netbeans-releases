package org.netbeans.modules.reportgenerator.api;

import java.util.List;
/**
 * ReportBody represents Body of a report.
 * 
 * a ReportBody contains a list ReportSection.
 * 
 * @author radval
 *
 */
public interface ReportBody {

	/**
	 * add a ReportSection
	 * @param section
	 */
	void addReportSection(ReportSection section);
	
	/**
	 * Remove a ReportSection
	 * @param section
	 */
	void removeReportSection(ReportSection section);
	
	/**
	 * Get a list of ReportSection.
	 * @return
	 */
	List<ReportSection> getReportSection();
	
}
