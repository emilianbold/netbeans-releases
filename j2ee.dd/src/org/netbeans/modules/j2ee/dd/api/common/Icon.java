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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Generated interface for Icon element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface Icon extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        public static final String XMLLANG = "XmlLang";	// NOI18N
	public static final String SMALL_ICON = "SmallIcon";	// NOI18N
	public static final String LARGE_ICON = "LargeIcon";	// NOI18N
        
        /** Setter for xml:lang attribute.
         * @param value attribute value
         */
	public void setXmlLang(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for xml:lang attribute.
         * @return attribute value
         */
	public java.lang.String getXmlLang() throws VersionNotSupportedException;
        /** Setter for small-icon property.
         * @param value property value
         */
	public void setSmallIcon(java.lang.String value);
        /** Getter for small-icon  property.
         * @return property value 
         */
	public java.lang.String getSmallIcon();
        /** Setter for large-icon property.
         * @param value property value
         */
	public void setLargeIcon(java.lang.String value);
        /** Getter for large-icon  property.
         * @return property value 
         */
	public java.lang.String getLargeIcon();

}
