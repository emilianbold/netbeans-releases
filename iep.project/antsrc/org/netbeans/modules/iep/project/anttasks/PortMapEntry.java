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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.project.anttasks;

/**
 *
 * @author Bing Lu
 */
public class PortMapEntry {
    public final static String MY_ROLE="myRole";
    public final static String PARTNER_ROLE="partnerRole";
    
    private String mPartnerLink;
    private String mPortType;
    private String mRole;
    private String mRoleName;
    
    /** Creates a new instance of PortMapEntry */
    public PortMapEntry(String partnerLink, 
                        String portType,
                        String role,
                        String roleName) {
        mPartnerLink = partnerLink;
        mPortType = portType;
        mRole = role;
        mRoleName = roleName;
    }
    
    public String getPartnerLink() {
        return mPartnerLink;
    }
    
    public String getPortType() {
        return mPortType;
    }
    
    public String getRole() {
        return mRole;
    }
    
    public String getRoleName() {
        return mRoleName;
    }
    
}
