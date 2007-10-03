/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                WebserviceEndpoint endpoint = (WebserviceEndpoint) parent;
                lc = endpoint.newLoginConfig();
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
