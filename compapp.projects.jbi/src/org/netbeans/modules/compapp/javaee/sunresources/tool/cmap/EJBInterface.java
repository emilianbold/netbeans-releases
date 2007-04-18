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

package org.netbeans.modules.compapp.javaee.sunresources.tool.cmap;


/**
 * @author echou
 *
 */
public class EJBInterface {
    
    public enum EJBInterfaceType { LOCAL, REMOTE }
    
    private String intf;
    private EJBInterfaceType intfType = EJBInterfaceType.LOCAL;
    
    public EJBInterface(String intf, EJBInterfaceType intfType) {
        this.intf = intf;
        this.intfType = intfType;
    }
    
    public String getIntf() {
        return intf;
    }

    public EJBInterfaceType getIntfType() {
        return intfType;
    }

    public void setIntfType(EJBInterfaceType intfType) {
        this.intfType = intfType;
    }

    @Override
    public String toString() {
        return "intf=" + intf + ", intfType=" + intfType; // NOI18N
    }
    
}
