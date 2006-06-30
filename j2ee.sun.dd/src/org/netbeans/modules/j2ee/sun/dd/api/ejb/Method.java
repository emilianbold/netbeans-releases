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
