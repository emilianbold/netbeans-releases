/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ListMapping.java
 *
 * Created on January 9, 2004, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/** Class that associates a list of properties owned by a CommonDDBean with a calculated
 *  string for display in a combobox or table.
 *  
 *  It is expected that the underlying bean and list referenced may be changed
 *  during this object's lifetime, and between calls to toString().
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class BeanListMapping {
	
	// Standard resource bundle to use for non-property list fields
	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	private static final String formatPattern = bundle.getString("LBL_SizeOfListText");	// NOI18N
	
	private CommonDDBean theBean;
	private String listPropertyName;
	private String displayText;
	private int listSize;

	public BeanListMapping(CommonDDBean bean, String propertyName) {
		theBean = bean;
		listPropertyName = propertyName;
		displayText = null;
		listSize = 0;
	}

	public String toString() {
		if(textOutOfDate()) {
			buildDisplayText();
		}
		
		return displayText;
	}
	
	private void buildDisplayText() {
		listSize = (theBean != null) ? theBean.size(listPropertyName) : 0;
		Object [] args = { new Integer(listSize) };
		displayText = MessageFormat.format(formatPattern, args);
	}
	
	private boolean textOutOfDate() {
		// Rebuild display Text if text is null or if size of list has changed.
		if(displayText == null) {
			return true;
		}
		
		int newListSize = 0;
		if(theBean != null) {
			newListSize = theBean.size(listPropertyName);
		}
		
		if(listSize != newListSize) {
			return true;
		}
		
		return false;
	}
	
	public CommonDDBean getBean() {
		return theBean;
	}
	
	public String getListPropertyName() {
		return listPropertyName;
	}
}
