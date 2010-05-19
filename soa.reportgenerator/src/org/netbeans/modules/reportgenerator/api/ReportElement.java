package org.netbeans.modules.reportgenerator.api;

import java.awt.Image;
import java.util.List;
/**
 * ReportElement represents one element in a ReportSection.
 * Typically a ReportElement represents one node on a canvas.
 * It is the basic building bloc for which a report will be generated with 
 * details about it.
 * For example it can represents one IEP operator or one BPEL activity like receive/invoke
 * etc.
 * 
 * @author radval
 *
 */
public interface ReportElement extends ReportNode, ReportAttributeContainer {

	/**
	 * Get the name of this report element.
	 * The name usually is the used to start a section
	 * for this report element.
	 * @return name
	 */
	String getName();
	
	/**
	 * Set the name of this report element.
	 * The name usually is the used to start a section
	 * for this report element.
	 * @param name name
	 */
	void setName(String name);
	
	/**
	 * Get image for this element. Usually it is an
	 * image specific for this element.
	 * @return
	 */
	Image getImage();
	
	/**
	 * Set image for this element. Usually it is an
	 * image specific for this element.
	 * @param image Image
	 */
	void setImage(Image image);

	/**
	 * Get the description for this element.
	 * This is usually a documentation about what
	 * this element represents.
	 * @return description.
	 */
	String getDescription();
	
	/**
	 * Set the description for this element.
	 * This is usually a documentation about what
	 * this element represents.
	 * @param description
	 */
	void setDescription(String description);
	
}
