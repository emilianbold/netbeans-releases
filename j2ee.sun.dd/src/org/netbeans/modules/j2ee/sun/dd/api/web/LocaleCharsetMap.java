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
