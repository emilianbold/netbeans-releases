/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 

public interface QueryMethod extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
    
    public static final String METHOD_NAME = "MethodName";	// NOI18N
    public static final String METHOD_PARAMS = "MethodParams";	// NOI18N
        
    public void setMethodName(String value);
    
    public String getMethodName();
    
    public void setMethodParams(MethodParams value);
    
    public MethodParams getMethodParams();
 
    public MethodParams newMethodParams();
    
}

