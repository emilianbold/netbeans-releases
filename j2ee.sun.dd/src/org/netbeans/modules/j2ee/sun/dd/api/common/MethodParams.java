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
 * MethodParams.java
 *
 * Created on November 18, 2004, 11:54 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface MethodParams extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String METHOD_PARAM = "MethodParam";	// NOI18N
    
    public String[] getMethodParam();
    public String getMethodParam(int index);
    public void setMethodParam(String[] value);
    public void setMethodParam(int index, String value);
    public int addMethodParam(String value);
    public int removeMethodParam(String value);
    public int sizeMethodParam();
    
}
