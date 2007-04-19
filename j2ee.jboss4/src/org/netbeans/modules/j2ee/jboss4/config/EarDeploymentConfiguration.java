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

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossApp;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EAR application deployment configuration handles jboss-app.xml configuration
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration extends JBDeploymentConfiguration 
implements ModuleConfiguration, DeploymentPlanConfiguration {
    
    private File jbossAppFile;
    private JbossApp jbossApp;
        
    /**
     * Creates a new instance of EarDeploymentConfiguration 
     */
    public EarDeploymentConfiguration(J2eeModule j2eeModule) {
        super(j2eeModule);
        jbossAppFile = j2eeModule.getDeploymentConfigurationFile("META-INF/jboss-app.xml"); // NOI18N
        getJbossApp();
        if (deploymentDescriptorDO == null) {
            try {
                deploymentDescriptorDO = deploymentDescriptorDO.find(FileUtil.toFileObject(jbossAppFile));
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
    }
    
    public void dispose() {
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
       
    /**
     * Return jbossApp graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return jbossApp graph or null if the jboss-app.xml file is not parseable.
     */
    public synchronized JbossApp getJbossApp() {
        if (jbossApp == null) {
            try {
                if (jbossAppFile.exists()) {
                    // load configuration if already exists
                    try {
                        jbossApp = jbossApp.createGraph(jbossAppFile);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss-app.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-app.xml if it does not exist yet
                    jbossApp = genereatejbossApp();
                    ResourceConfigurationHelper.writeFile(jbossAppFile, jbossApp);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jbossApp;
    }
    
    public void save(OutputStream os) throws ConfigurationException {
        JbossApp jbossApp = getJbossApp();
        if (jbossApp == null) {
            String msg = NbBundle.getMessage(EarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", jbossAppFile.getAbsolutePath());
            throw new ConfigurationException(msg);
        }
        try {
            jbossApp.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(EarDeploymentConfiguration.class, "MSG_CannotUpdateFile", jbossAppFile.getAbsolutePath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Context graph.
     */
    private JbossApp genereatejbossApp() {
        return new JbossApp();
    }

    public boolean supportsCreateDatasource() {
        return false;
    }
    
    public boolean supportsCreateMessageDestination() {
        return false;
    }
    
}
