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
/**
 * Generated interface for ErrorPage element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ErrorPage extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for error-code property.
         * @param value property value
         */
	public void setErrorCode(java.lang.Integer value);
        /** Getter for error-code property.
         * @return property value 
         */
	public java.lang.Integer getErrorCode();
        /** Setter for exception-type property.
         * @param value property value
         */
	public void setExceptionType(java.lang.String value);
        /** Getter for exception-type property.
         * @return property value 
         */
	public java.lang.String getExceptionType();
        /** Setter for location property.
         * @param value property value
         */
	public void setLocation(java.lang.String value);
        /** Getter for location property.
         * @return property value 
         */
	public java.lang.String getLocation();

}
