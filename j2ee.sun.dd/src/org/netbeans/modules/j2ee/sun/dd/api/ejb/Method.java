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
 * Method.java
 *
 * Created on November 18, 2004, 11:51 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Method extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String DESCRIPTION = "Description";	// NOI18N
    public static final String METHOD_INTF = "MethodIntf";	// NOI18N
    public static final String METHOD_NAME = "MethodName";	// NOI18N
    public static final String METHOD_PARAMS = "MethodParams";	// NOI18N
    public static final String EJB_NAME = "EjbName";	// NOI18N
    
    /** Setter for description property
     * @param value property value
     */
    public void setDescription(java.lang.String value);
    /** Getter for description property.
     * @return property value
     */
    public java.lang.String getDescription();
    /** Setter for method-intf property
     * @param value property value
     */
    public void setMethodIntf(java.lang.String value);
    /** Getter for method-intf property.
     * @return property value
     */
    public java.lang.String getMethodIntf();
    /** Setter for method-name property
     * @param value property value
     */
    public void setMethodName(java.lang.String value);
    /** Getter for method-name property.
     * @return property value
     */
    public java.lang.String getMethodName();
    /** Setter for method-params property
     * @param value property value
     */
    public void setMethodParams(MethodParams value);
    /** Getter for method-params property.
     * @return property value
     */
    public MethodParams getMethodParams();
    
    public MethodParams newMethodParams(); 
    
    /** Setter for ejb-name property
     * @param value property value
     */
    public void setEjbName(java.lang.String value);
    /** Getter for ejb-name property.
     * @return property value
     */
    public java.lang.String getEjbName();
    
}
