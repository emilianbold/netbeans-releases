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

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for LoginConfig element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface LoginConfig extends CommonDDBean, CreateCapability {
        /** Setter for auth-method property.
         * @param value property value
         */
	public void setAuthMethod(java.lang.String value);
        /** Getter for auth-method property.
         * @return property value 
         */
	public java.lang.String getAuthMethod();
        /** Setter for realm-name property.
         * @param value property value
         */
	public void setRealmName(java.lang.String value);
        /** Getter for realm-name property.
         * @return property value 
         */
	public java.lang.String getRealmName();
        /** Setter for form-login-config element.
         * @param valueInterface form-login-config element (FormLoginConfig object)
         */
	public void setFormLoginConfig(org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig valueInterface);
        /** Getter for form-login-config element.
         * @return form-login-config element (FormLoginConfig object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig getFormLoginConfig();

}
