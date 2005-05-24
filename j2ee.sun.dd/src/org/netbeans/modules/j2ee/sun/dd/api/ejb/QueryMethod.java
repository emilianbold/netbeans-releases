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
 * QueryMethod.java
 *
 * Created on November 18, 2004, 3:54 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;

/**
 *
 * @author Nitya Doraisamy
 */
public interface QueryMethod extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String METHOD_NAME = "MethodName";	// NOI18N
    public static final String METHOD_PARAMS = "MethodParams";	// NOI18N

            
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
}
