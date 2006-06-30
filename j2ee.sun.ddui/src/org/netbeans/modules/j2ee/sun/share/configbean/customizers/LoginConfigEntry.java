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
        // Set blank strings to null.  This object also handles message-security-binding
        // though, so we have to check it out.        
        if(value instanceof String && ((String) value).length() == 0) {
            value = null;
        }

        LoginConfig lc = (LoginConfig) parent.getValue(WebserviceEndpoint.LOGIN_CONFIG);
        if(value != null) {
            if(lc == null) {
                lc = StorageBeanFactory.getDefault().createLoginConfig();
                parent.setValue(propertyName, lc);
            }

            lc.setValue(LoginConfig.AUTH_METHOD, value);
        } else {
            if(lc != null) {
                parent.setValue(WebserviceEndpoint.LOGIN_CONFIG, null);
            }
        }
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}	
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
	
}
