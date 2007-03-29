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


package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.openide.WizardDescriptor;

/**
 * Factory for optional deployment functionality that a plugin can provide.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @author  Pavel Buzek
 */
public abstract class OptionalDeploymentManagerFactory {

    /**
     * Create StartServer for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */ 
    public abstract StartServer getStartServer (DeploymentManager dm);
    
    /** 
     * Create IncrementalDeployment for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract IncrementalDeployment getIncrementalDeployment (DeploymentManager dm);
    
    /** 
     * Create FindJSPServlet for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract FindJSPServlet getFindJSPServlet (DeploymentManager dm);
    
    /** 
     * Create TargetModuleIDResolver for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    /** 
     * Create the wizard iterator to be used in the Add Server Instance wizard
     */
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return null;
    }
    
    /**
     * Creates an Ant deployment provider for the specified deployment manager.
     *
     * @param dm deployment manager.
     * @return an instance of the AntDeploymentProvider if Ant deployment
     *         is supported for the specified deployment manager, null otherwise.
     * @since 1.18
     */
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return null;
    }
    
    /**
     * Creates a <code>DatasourceManager</code> for the given deployment manager
     * or <code>null</code> if data source management is not supported
     *
     * @param dm the deployment manager
     *
     * @return a data source manager or <code>null</code> if data source management
     *         is not supported
     *
     * @since 1.15
     */
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return null;
    }
}
