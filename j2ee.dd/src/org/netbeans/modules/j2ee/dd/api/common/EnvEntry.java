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
