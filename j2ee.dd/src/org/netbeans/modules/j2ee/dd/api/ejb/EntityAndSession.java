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

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EntityAndSession extends Ejb {
    //Entity & Session Only
    public static final String HOME = "Home";	// NOI18N
    public static final String REMOTE = "Remote";	// NOI18N
    public static final String LOCAL_HOME = "LocalHome";	// NOI18N
    public static final String LOCAL = "Local";	// NOI18N
    public static final String SECURITY_ROLE_REF = "SecurityRoleRef";	// NOI18N
    
    // entity & session only
    public String getHome();
    
    public void setHome(String value);
    
    public String getRemote();
    
    public void setRemote(String value);
    
    public String getLocal();
    
    public void setLocal(String value);
    
    public String getLocalHome();
    
    public void setLocalHome(String value);
    
    public void setSecurityRoleRef(int index,SecurityRoleRef value);
    
    public SecurityRoleRef getSecurityRoleRef(int index);
    
    public void setSecurityRoleRef(SecurityRoleRef[] value);
    
    public SecurityRoleRef[] getSecurityRoleRef();
    
    public int sizeSecurityRoleRef();
    
    public int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.ejb.SecurityRoleRef value);
    
    public int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.ejb.SecurityRoleRef value);
    
    public SecurityRoleRef newSecurityRoleRef();
        
}
