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
 * Generated interface for ResourceEnvRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface ResourceEnvRef extends CommonDDBean, DescriptionInterface {
    
    
    public static final String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"; // NOI18N
    public static final String RESOURCE_ENV_REF_TYPE = "ResourceEnvRefType"; // NOI18N
    
    /** Setter for resource-env-ref-name property 
     * @param value property value
     */
    public void setResourceEnvRefName(java.lang.String value);
    /** Getter for resource-env-ref-name property.
     * @return property value 
     */
    public java.lang.String getResourceEnvRefName();
    /** Setter for resource-env-ref-type property.
     * @param value property value
     */
    public void setResourceEnvRefType(java.lang.String value);
    /** Getter for resource-env-ref-type property.
     * @return property value 
     */
    public java.lang.String getResourceEnvRefType();

}
