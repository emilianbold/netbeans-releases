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
 * Generated interface for Taglib element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface Taglib extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for taglib-uri property.
         * @param value property value
         */
	public void setTaglibUri(java.lang.String value);
        /** Getter for taglib-uri property.
         * @return property value 
         */
	public java.lang.String getTaglibUri();
        /** Setter for talib-location property.
         * @param value property value
         */
	public void setTaglibLocation(java.lang.String value);
        /** Getter for talib-location property.
         * @return property value 
         */
	public java.lang.String getTaglibLocation();

}
