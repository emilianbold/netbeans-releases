package org.netbeans.modules.reportgenerator.api;

/**
 * Represents types of Reports Generator available.
 * @author radval
 *
 */
public enum ReportType {

	REPORT_HTML("html"), REPORT_PDF("pdf");

	private String mReportType;
	
	ReportType(String type) {
		this.mReportType = type;
	}
	
	public String toString() {
		return this.mReportType;
	}
}
