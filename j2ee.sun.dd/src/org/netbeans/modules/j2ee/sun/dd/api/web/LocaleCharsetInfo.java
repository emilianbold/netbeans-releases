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
 * LocaleCharsetInfo.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface LocaleCharsetInfo extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
        
        public static final String DEFAULTLOCALE = "DefaultLocale";	// NOI18N
	public static final String LOCALE_CHARSET_MAP = "LocaleCharsetMap";	// NOI18N
	public static final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
	public static final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
	public static final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N

        
        /** Setter for default-locale attribute
         * @param value attribute value
         */
	public void setDefaultLocale(java.lang.String value);
        /** Getter for default-locale attribute.
         * @return attribute value
         */
	public java.lang.String getDefaultLocale();

	public void setLocaleCharsetMap(int index, LocaleCharsetMap value);
	public LocaleCharsetMap getLocaleCharsetMap(int index);
	public int sizeLocaleCharsetMap();
	public void setLocaleCharsetMap(LocaleCharsetMap[] value);
	public LocaleCharsetMap[] getLocaleCharsetMap();
	public int addLocaleCharsetMap(LocaleCharsetMap value);
	public int removeLocaleCharsetMap(LocaleCharsetMap value);
	public LocaleCharsetMap newLocaleCharsetMap();

        /** Setter for parameter-encoding property
         * @param value property value
         */
	public void setParameterEncoding(boolean value);
        /** Check for parameter-encoding property.
         * @return property value
         */
	public boolean isParameterEncoding();
        /** Setter for form-hint-field attribute of parameter-encoding
         * @param value attribute value
         */
	public void setParameterEncodingFormHintField(java.lang.String value);
         /** Getter for form-hint-field attribute of parameter-encoding
         * @return attribute value
         */
	public java.lang.String getParameterEncodingFormHintField();
        /** Setter for default-charset attribute of parameter-encoding
         * @param value attribute value
         */
	public void setParameterEncodingDefaultCharset(java.lang.String value);
         /** Getter for default-charset attribute of parameter-encoding
         * @return attribute value
         */
	public java.lang.String getParameterEncodingDefaultCharset();

}
