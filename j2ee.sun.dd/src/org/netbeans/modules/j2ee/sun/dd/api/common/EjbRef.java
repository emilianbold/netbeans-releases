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
 * EjbRef.java
 *
 * Created on November 17, 2004, 5:05 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EjbRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String EJB_REF_NAME = "EjbRefName";	// NOI18N
    public static final String JNDI_NAME = "JndiName";	// NOI18N
        
    /** Setter for ejb-ref-name property.
     * @param value property value
     */
    public void setEjbRefName(java.lang.String value);
    /** Getter for ejb-ref-name property.
     * @return property value
     */
    public java.lang.String getEjbRefName();
    /** Setter for jndi-name property.
     * @param value property value
     */
    public void setJndiName(java.lang.String value);
    /** Getter for jndi-name property.
     * @return property value
     */
    public java.lang.String getJndiName();
    
}
