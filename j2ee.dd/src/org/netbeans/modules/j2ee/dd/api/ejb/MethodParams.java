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

public interface MethodParams extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
    
        public static final String METHOD_PARAM = "MethodParam";	// NOI18N
    
        public void setMethodParam(int index, String value);

        public String getMethodParam(int index);

        public void setMethodParam(String[] value);

        public String[] getMethodParam();
    
	public int sizeMethodParam();

	public int addMethodParam(String value);

	public int removeMethodParam(String value);

}

