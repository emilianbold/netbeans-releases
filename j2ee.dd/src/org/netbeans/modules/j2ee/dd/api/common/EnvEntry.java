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
 * Generated interface for EnvEntry element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface EnvEntry extends CommonDDBean, DescriptionInterface {
    
    public static final String ENV_ENTRY_NAME = "EnvEntryName";	// NOI18N
    public static final String ENV_ENTRY_TYPE = "EnvEntryType";	// NOI18N
    public static final String ENV_ENTRY_VALUE = "EnvEntryValue";	// NOI18N

        /** Setter for env-entry-name property.
         * @param value property value
         */
	public void setEnvEntryName(java.lang.String value);
        /** Getter for env-entry-name property.
         * @return property value 
         */
	public java.lang.String getEnvEntryName();
        /** Setter for env-entry-type property.
         * @param value property value
         */
	public void setEnvEntryType(java.lang.String value);
        /** Getter for env-entry-type property.
         * @return property value 
         */
	public java.lang.String getEnvEntryType();
        /** Setter for env-entry-value property.
         * @param value property value
         */
	public void setEnvEntryValue(java.lang.String value);
        /** Getter for env-entry-value property.
         * @return property value 
         */
	public java.lang.String getEnvEntryValue();
}
