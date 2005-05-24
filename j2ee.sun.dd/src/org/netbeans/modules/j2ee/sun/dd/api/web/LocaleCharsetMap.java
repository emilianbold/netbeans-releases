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
 * LocaleCharsetMap.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface LocaleCharsetMap extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
        
        public static final String LOCALE = "Locale";	// NOI18N
	public static final String AGENT = "Agent";	// NOI18N
	public static final String CHARSET = "Charset";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N

        /** Setter for locale attribute.
         * @param value attribute value
         */
	public void setLocale(java.lang.String value);
        /** Getter for locale attribute.
         * @return attribute value
         */
	public java.lang.String getLocale();
        /** Setter for agent attribute.
         * @param value attribute value
         */
	public void setAgent(java.lang.String value);
        /** Getter for agent attribute.
         * @return attribute value
         */
	public java.lang.String getAgent();
        /** Setter for charset attribute.
         * @param value attribute value
         */
	public void setCharset(java.lang.String value);
        /** Getter for charset attribute.
         * @return attribute value
         */
	public java.lang.String getCharset();
        /** Setter for description property.
         * @param value property value
         */
	public void setDescription(String value);
        /** Getter for description property.
         * @return property value
         */
	public String getDescription();

}
