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

package org.netbeans.modules.j2ee.dd.api.common;

/**
 * Ability to find an instance of CommonDDBean class nested inside this bean.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface FindCapability {
    
    /**
     * Method is looking for the nested DD element according to the specified property and value 
     *
     * @param beanName e.g. "Servlet" or "ResourceRef"
     * @param propertyName e.g. "ServletName" or ResourceRefName"
     * @param value specific propertyName value e.g. "ControllerServlet" or "jdbc/EmployeeAppDb"
     * @return Bean satisfying the parameter values or null if not found
     */    
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value);
}
