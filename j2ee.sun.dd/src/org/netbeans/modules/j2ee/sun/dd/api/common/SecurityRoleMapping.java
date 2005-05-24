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
 * SecurityRoleMapping.java
 *
 * Created on November 17, 2004, 4:29 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface SecurityRoleMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String ROLE_NAME = "RoleName";	// NOI18N
    public static final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
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
    
    public String[] getGroupName();
    public String getGroupName(int index);
    public void setGroupName(String[] value);
    public void setGroupName(int index, String value);
    public int addGroupName(String value);
    public int removeGroupName(String value);
    public int sizeGroupName();
    
}
