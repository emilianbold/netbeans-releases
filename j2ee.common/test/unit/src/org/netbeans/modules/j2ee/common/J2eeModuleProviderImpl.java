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

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Libor Kotouc
 */
public class J2eeModuleProviderImpl extends J2eeModuleProvider {
    
    private Set<Datasource> moduleDatasources;
    private Set<Datasource> serverDatasources;
    private boolean creationAllowed;
    
    public J2eeModuleProviderImpl(Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources) {
        this(moduleDatasources, serverDatasources, true);
    }

    public J2eeModuleProviderImpl(Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources, boolean creationAllowed) {
        this.moduleDatasources = moduleDatasources;
        this.serverDatasources = serverDatasources;
        this.creationAllowed = creationAllowed;
    }

    // J2eeModuleProvider abstract methods implementation
    
    public void setServerInstanceID(String severInstanceID) {
    }

    public File getDeploymentConfigurationFile(String name) {
        return null;
    }

    public FileObject findDeploymentConfigurationFile(String name) {
        return null;
    }

    public ModuleChangeReporter getModuleChangeReporter() {
        return null;
    }

    public J2eeModule getJ2eeModule() {
        return null;
    }

    // J2eeModuleProvider DS API methods
    
    public Set<Datasource> getModuleDatasources() {
        return moduleDatasources;
    }

    public Set<Datasource> getServerDatasources() {
        return serverDatasources;
    }
    
    public boolean isDatasourceCreationSupported() {
        return creationAllowed;
    }    
    
}
