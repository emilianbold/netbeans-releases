/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
