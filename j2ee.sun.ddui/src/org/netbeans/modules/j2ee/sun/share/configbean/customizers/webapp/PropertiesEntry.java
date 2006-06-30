/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PropertiesEntry.java
 *
 * Created on December 12, 2003, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;


/**
 *
 * @author  Peter Williams
 */
public class PropertiesEntry extends GenericTableModel.TableEntry {
	
	static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	public PropertiesEntry() {
		this(SunWebApp.PROPERTY, bundle.getString("LBL_Properties"));	// NOI18N
	}
	
	public PropertiesEntry(String propertyName, String captionName) {
		super(propertyName, captionName);
	}

	public Object getEntry(CommonDDBean parent) {
		// FIXME this can be made more concise - spread out for debugging.
		ListMapping listMap = null;
		Object obj = parent.getValues(propertyName);
		if(obj != null) {
			WebProperty [] webProps = (WebProperty []) obj;
			List properties = Utils.arrayToList(webProps);
			listMap = new ListMapping(properties);
		}
		
		return listMap;
	}

	public void setEntry(CommonDDBean parent, Object value) {
		List list = ((ListMapping) value).getList();
		WebProperty [] webProps = (WebProperty []) 
			Utils.listToArray(list, WebProperty.class);
		parent.setValue(propertyName, webProps);
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}	
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
}
