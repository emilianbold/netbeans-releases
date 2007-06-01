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

package org.netbeans.modules.j2ee.api.ejbjar;

/**
 *
 * @author Martin Adamek
 */
public final class ResourceReference {

    private final String resRefName;
    private final String resType;
    private final String resAuth;
    private final String resSharingScope;
    private final String defaultDescription;
    
    private ResourceReference(String resRefName, String resType, String resAuth, String resSharingScope, String defaultDescription) {
        this.resRefName = resRefName;
        this.resType = resType;
        this.resAuth = resAuth;
        this.resSharingScope = resSharingScope;
        this.defaultDescription = defaultDescription;
    }
    
    public static ResourceReference create(String resRefName, String resType, String resAuth, String resSharingScope, String defaultDescription) {
        return new ResourceReference(resRefName, resType, resAuth, resSharingScope, defaultDescription);
    }
    
    public String getResRefName() {
        return resRefName;
    }

    public String getResType() {
        return resType;
    }

    public String getResAuth() {
        return resAuth;
    } 
    
    public String getResSharingScope() {
        return resSharingScope;
    }
    
    public String getDefaultDescription() {
        return defaultDescription;
    }
    
}
