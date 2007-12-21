package org.netbeans.modules.reportgenerator.api;

import java.util.List;
/**
 * ReportAttributeContainer represents a container
 * for ReportAttribute.
 * 
 * Typically a class which has ReportAttribute
 * implements this interface.
 * 
 * @author radval
 *
 */
public interface ReportAttributeContainer {

	List<ReportAttribute> getAttributes();
	
	void addAttribute(ReportAttribute attr);
	
	void removeAttribute(ReportAttribute attr);
	
}
