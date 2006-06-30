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

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;

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
    
    public int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef value);
    
    public int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef value);
    
    public SecurityRoleRef newSecurityRoleRef();
        
}
