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
 * ResourceRef.java
 *
 * Created on November 17, 2004, 5:06 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ResourceRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String RES_REF_NAME = "ResRefName";	// NOI18N
    public static final String JNDI_NAME = "JndiName";	// NOI18N
    public static final String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal";	// NOI18N
        
    /** Setter for res-ref-name property
     * @param value property value
     */
    public void setResRefName(java.lang.String value);
    /** Getter for res-ref-name property.
     * @return property value
     */
    public java.lang.String getResRefName();
    /** Setter for jndi-name property
     * @param value property value
     */
    public void setJndiName(java.lang.String value);
    /** Getter for jndi-name property.
     * @return property value
     */
    public java.lang.String getJndiName();
    /** Setter for default-resource-principal property
     * @param value property value 
     */
    public void setDefaultResourcePrincipal(DefaultResourcePrincipal value);
    /** Getter for default-resource-principal property.
     * @return property value
     */
    public DefaultResourcePrincipal getDefaultResourcePrincipal();
    
    /**
     * Create a new default-resource-principal
     * This does not add it to any bean graph.
     */
    public DefaultResourcePrincipal newDefaultResourcePrincipal();
    
}
