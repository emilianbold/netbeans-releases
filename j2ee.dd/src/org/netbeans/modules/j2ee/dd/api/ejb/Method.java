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
import org.netbeans.api.web.dd.common.CommonDDBean;
import org.netbeans.api.web.dd.common.DescriptionInterface;

public interface Method extends CommonDDBean, DescriptionInterface {
    
    public static final String EJB_NAME = "EjbName";	// NOI18N
    public static final String METHOD_INTF = "MethodIntf";	// NOI18N
    public static final String METHOD_NAME = "MethodName";	// NOI18N
    public static final String METHOD_PARAMS = "MethodParams";	// NOI18N
        
    public void setEjbName(String value);
    
    public String getEjbName();
    
    public void setMethodIntf(String value);
    
    public String getMethodIntf();
    
    public void setMethodName(String value);
    
    public String getMethodName();
    
    public void setMethodParams(MethodParams value);
    
    public MethodParams getMethodParams();
    
    public MethodParams newMethodParams();
        
}

