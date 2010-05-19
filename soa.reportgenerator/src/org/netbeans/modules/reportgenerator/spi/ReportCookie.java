package org.netbeans.modules.reportgenerator.spi;

import org.netbeans.modules.reportgenerator.api.*;
import org.openide.nodes.Node;

/**
 * ReportCookie is the SPI which a DataObject should
 * implement and put it in its CookieSet.
 * 
 * ReportCookie allows implementer to create and populate Report
 * objects which are used in Report generation.
 * 
 * @author radval
 *
 */
public interface ReportCookie extends Node.Cookie {

	/**
	 * Populate and return a Report
	 * @return
	 */
    Report generateReport();
}
