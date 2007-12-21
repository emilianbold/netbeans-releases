package org.netbeans.modules.reportgenerator.api;

import java.io.File;
import java.io.OutputStream;

/**
 * ReportGenerator is the base interface implemented
 * by a type of ReportGenerator.
 * 
 * For example there will be one ReportGenerator for pdf
 * reports, one for html reports etc.
 * @author radval
 *
 */
public interface ReportGenerator {

	/**
	 * Generate a specific kind of report given the Report object.
	 * @param report
	 * @throws ReportException
	 */
	void generateReport(Report report) throws ReportException;
	
}
