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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

public interface MethodPermission extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {

        public static final String ROLE_NAME = "RoleName";	// NOI18N
	public static final String UNCHECKED = "Unchecked";	// NOI18N
	public static final String UNCHECKEDID = "UncheckedId";	// NOI18N
	public static final String METHOD = "Method";	// NOI18N
        
        public void setRoleName(int index, String value);

        public String getRoleName(int index);

        public void setRoleName(String[] value);

        public String[] getRoleName();
        
        public int sizeRoleName();
        
        public int removeRoleName(String value);

	public int addRoleName(String value);
        
        public void setUnchecked(boolean value) throws VersionNotSupportedException;

        public boolean isUnchecked() throws VersionNotSupportedException;
        
        public void setMethod(int index, Method value);

        public Method getMethod(int index);

        public void setMethod(Method[] value);

        public Method[] getMethod();
        
	public int addMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method value);

	public int sizeMethod();

	public int removeMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method value);

        public Method newMethod();
        
}

