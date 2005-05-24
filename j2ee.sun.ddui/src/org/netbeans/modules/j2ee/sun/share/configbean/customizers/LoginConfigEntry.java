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
 * LoginConfigEntry.java
 *
 * Created on December 12, 2003, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig;

import org.netbeans.modules.j2ee.sun.share.configbean.ServletRef;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;


/**
 *
 * @author  Peter Williams
 */
public class LoginConfigEntry extends GenericTableModel.TableEntry {

	private static final ResourceBundle customizerBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle");	// NOI18N

	public LoginConfigEntry() {
		super(WebserviceEndpoint.LOGIN_CONFIG, customizerBundle.getString("LBL_AuthenticationMethod"));	// NOI18N
	}

	public Object getEntry(CommonDDBean parent) {
		Object result = null;

		CommonDDBean loginConfig = (CommonDDBean) parent.getValue(propertyName);
		if(loginConfig != null) {
			result = loginConfig.getValue(LoginConfig.AUTH_METHOD);
		}

		return result;
	}

	public void setEntry(CommonDDBean parent, Object value) {
		LoginConfig lc = (LoginConfig) parent.getValue(WebserviceEndpoint.LOGIN_CONFIG);
		if(lc == null) {
			lc = StorageBeanFactory.getDefault().createLoginConfig();
			parent.setValue(propertyName, lc);
		}

		lc.setValue(LoginConfig.AUTH_METHOD, value);
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}	
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
	
}
