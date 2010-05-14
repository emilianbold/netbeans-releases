package org.netbeans.modules.reportgenerator.api;

/**
 * ReportException is the base exception thrown
 * by Report Framework.
 * @author radval
 *
 */
public class ReportException extends Exception {

	public ReportException(String message) {
		super(message);
	}
	
	public ReportException(String message, Exception ex) {
		super(message, ex);
	}
}
