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
 * MdbResourceAdapter.java
 *
 * Created on November 17, 2004, 5:19 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface MdbResourceAdapter extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String RESOURCE_ADAPTER_MID = "ResourceAdapterMid";	// NOI18N
    public static final String ACTIVATION_CONFIG = "ActivationConfig";	// NOI18N
        
    /** Setter for resource-adapter-mid property
     * @param value property value
     */
    public void setResourceAdapterMid(java.lang.String value);
    /** Getter for resource-adapter-mid property.
     * @return property value
     */
    public java.lang.String getResourceAdapterMid();
    /** Setter for activation-config property
     * @param value property value
     */
    public void setActivationConfig(ActivationConfig value);
    /** Getter for activation-config property.
     * @return property value
     */
    public ActivationConfig getActivationConfig(); 
    
    public ActivationConfig newActivationConfig();
}
