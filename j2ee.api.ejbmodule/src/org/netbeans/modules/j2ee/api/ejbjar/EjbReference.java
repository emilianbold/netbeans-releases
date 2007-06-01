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
public final class EjbReference {

    private final String ejbClass;
    private final String ejbRefType;
    private final String local;
    private final String localHome;
    private final String remote;
    private final String remoteHome;
    private final EjbJar ejbModule;
    
    private EjbReference(String ejbClass, String ejbRefType, String local, String localHome, String remote, String remoteHome, EjbJar ejbModule) {
        this.ejbClass = ejbClass;
        this.ejbRefType = ejbRefType;
        this.local = local;
        this.localHome = localHome;
        this.remote = remote;
        this.remoteHome = remoteHome;
        this.ejbModule = ejbModule;
    }

    public static EjbReference create(String ejbClass, String ejbRefType, String local, String localHome, String remote, String remoteHome, EjbJar ejbModule) {
        return new EjbReference(ejbClass, ejbRefType, local, localHome, remote, remoteHome, ejbModule);
    }
    
    public String getEjbClass() {
        return ejbClass;
    }
    
    public String getEjbRefType() {
        return ejbRefType;
    }
    
    public String getLocal() {
        return local;
    }

    public String getLocalHome() {
        return localHome;
    }

    public String getRemote() {
        return remote;
    }

    public String getRemoteHome() {
        return remoteHome;
    }

    public EjbJar getEjbModule() {
        return ejbModule;
    }
    
}
