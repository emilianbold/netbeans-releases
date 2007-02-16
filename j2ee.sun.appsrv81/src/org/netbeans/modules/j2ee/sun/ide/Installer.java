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

package org.netbeans.modules.j2ee.sun.ide;

import java.io.File;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.DomainCreator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {
    
    private static DeploymentFactory facadeDF = null;
    
    private static final String PROP_FIRST_RUN = "first_run";
    
    /** Factory method to create DeploymentFactory for s1as.
     */
    public static synchronized Object create() {
        if (facadeDF == null){
            //this is our JSR88 factory lazy init, only when needed via layer.
            PluginProperties.configureDefaultServerInstance();
            facadeDF =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory();
        }
        return facadeDF;
    }
    
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new PrepareEnvironment());
    }
        
    public void close() {       
    }
    
    public void uninstalled() {
    }
    
    private static class PrepareEnvironment implements Runnable {
        public void run() {
            // if the domain hasn't been created successfully previously
            if (!NbPreferences.forModule(DomainCreator.class).getBoolean(PROP_FIRST_RUN, false)) {
                String prop = System.getProperty(ServerLocationManager.INSTALL_ROOT_PROP_NAME);
                
                if (null != prop && prop.trim().length() > 0) {
                    // There is a possible root directory for the AS
                    File propFile = new File(prop);
                    ClassLoader cl = ServerLocationManager.getNetBeansAndServerClassLoader(propFile);
                    if (null != cl && !propFile.canWrite()) {
                        createDomainAndRecord(propFile);
                    } 
                }
            }
        }
    }

    static private void createDomainAndRecord(final File propFile) {
        // The root directory is valid
        // Domain can be created
        InstanceProperties ip = DomainCreator.createPersonalDefaultDomain(propFile.getAbsolutePath());
            // Sets domain creation performed flag to true
            NbPreferences.forModule(DomainCreator.class).putBoolean(PROP_FIRST_RUN, true);
    }
}
