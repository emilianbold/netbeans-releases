package org.netbeans.modules.reportgenerator.api;

import java.awt.Image;

/**
 * Report represents a logical report.
 * It consist of high level report description, an overview
 * image and report level attributes which are key value pair.
 * 
 * A report consists of a header, body and footer.
 * @author radval
 *
 */
public interface Report extends ReportAttributeContainer {
	
	/**
	 * Get the name of the report.
	 * @return
	 */
	String getName();
	
	/**
	 * set the name of the report. This is typically
	 * the heading at the start of the generated report.
	 * 
	 * @param name
	 */
	void setName(String name);
	
	/**
	 * Get the description for this report.
	 * Usually it is high level description of report
	 * describing what this report is all about.
	 * @return report description.
	 */
	String getDescription();
	
	/**
	 * set the description for this report.
	 * Usually it is high level description of report
	 * describing what this report is all about.
	 * @param description report description
	 */
	void setDescription(String description);
	
	/**
	 * set the image for whole report. Usually
	 * this image is an overview image for whole report.
	 * @param image Image
	 */
	void setOverViewImage(Image image);
	
	/**
	 * Get the image for whole report. Usually
	 * this image is an overview image for whole report.
	 * @return Image
	 */
	Image getOverviewImage();
	
	/**
	 * Get the header section for this report.
	 * @return ReportHeader.
	 */
	ReportHeader getHeader();
	
	/**
	 * 
	 * @param header
	 */
	void setHeader(ReportHeader header);
	
	/**
	 *  Get report body.
	 * @return ReportBody
	 */
	ReportBody getBody();
	
	/**
	 * Set report body.
	 * @param body ReportBody
	 */
	void setBody(ReportBody body);
	
	/**
	 * Get report footer.
	 * @return ReportFooter.
	 */
	ReportFooter getFooter();
	
	/**
	 * Set report footer.
	 * @param footer ReportFooter
	 */
	void setFooter(ReportFooter footer);
}
