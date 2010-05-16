package org.netbeans.modules.reportgenerator.api;

/**
 * ReportAttribute represents key value pair.
 * 
 * These are used at various Report elements
 * for storing key value pair which typically are
 * generated in a tabular form.
 * @author radval
 *
 */
public interface ReportAttribute {

	/**
	 * Get attribute name
	 * @return
	 */
	String getName();

	/**
	 * set attribute name
	 * @param name
	 */
	void setName(String name);
	
	/**
	 * get attribute value
	 * @return
	 */
	Object getValue();
	
	/**
	 * set attribute value
	 * @param value
	 */
	void setValue(Object value);
	
	
}
