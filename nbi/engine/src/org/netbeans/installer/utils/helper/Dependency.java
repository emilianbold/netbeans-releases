/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.product.components.Product;

/**
 *
 * @author Kirill Sorokin
 */

public abstract class Dependency {    
    private String uid;
    private Version versionLower;
    private Version versionUpper;
    private Version versionResolved;
    
    protected Dependency(
            final String uid,
            final Version versionLower,
            final Version versionUpper,
            final Version versionResolved) {
        this.uid              = uid;
        this.versionLower     = versionLower;
        this.versionUpper     = versionUpper;
        this.versionResolved = versionResolved;
    }
   
    public String getUid() {
        return uid;
    }
    
    public Version getVersionLower() {
        return versionLower;
    }
    
    public Version getVersionUpper() {
        return versionUpper;
    }
    
    public Version getVersionResolved() {
        return versionResolved;
    }
    
    public void setVersionResolved(final Version version) {
        this.versionResolved = version;
    }
    
    public abstract String getName();
    
    public abstract boolean satisfies(Product product);    
    
}
