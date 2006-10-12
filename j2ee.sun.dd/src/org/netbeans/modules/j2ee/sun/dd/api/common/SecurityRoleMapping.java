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
 * SecurityRoleMapping.java
 *
 * Created on November 17, 2004, 4:29 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface SecurityRoleMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String VERSION_SERVER_8_0 = "Server 8.0"; 
    
    public static final String ROLE_NAME = "RoleName";	// NOI18N
    public static final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
    public static final String PRINCIPALNAMECLASSNAME = "PrincipalNameClassName";	// NOI18N
    public static final String GROUP_NAME = "GroupName";	// NOI18N

    public void setRoleName(java.lang.String value);
    public java.lang.String getRoleName();

    public String[] getPrincipalName();
    public String getPrincipalName(int index);
    public void setPrincipalName(String[] value);
    public void setPrincipalName(int index, String value);
    public int addPrincipalName(String value);
    public int removePrincipalName(String value);
    public int sizePrincipalName();
    
	public void setPrincipalNameClassName(int index, String value) throws VersionNotSupportedException;
	public String getPrincipalNameClassName(int index) throws VersionNotSupportedException;
	public int sizePrincipalNameClassName() throws VersionNotSupportedException;
    
    public String[] getGroupName();
    public String getGroupName(int index);
    public void setGroupName(String[] value);
    public void setGroupName(int index, String value);
    public int addGroupName(String value);
    public int removeGroupName(String value);
    public int sizeGroupName();
    
}
