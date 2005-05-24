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
 * ActivationConfigProperty.java
 *
 * Created on November 18, 2004, 11:43 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ActivationConfigProperty extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String ACTIVATION_CONFIG_PROPERTY_NAME = "ActivationConfigPropertyName";	// NOI18N
    public static final String ACTIVATION_CONFIG_PROPERTY_VALUE = "ActivationConfigPropertyValue";	// NOI18N
        
    /** Setter for activation-config-property-name property
     * @param value property value
     */
    public void setActivationConfigPropertyName(java.lang.String value);
    /** Getter for activation-config-property-name property.
     * @return property value
     */
    public java.lang.String getActivationConfigPropertyName();
    /** Setter for activation-config-property-value property
     * @param value property value
     */
    public void setActivationConfigPropertyValue(java.lang.String value);
    /** Getter for activation-config-property-value property.
     * @return property value
     */
    public java.lang.String getActivationConfigPropertyValue();
}
