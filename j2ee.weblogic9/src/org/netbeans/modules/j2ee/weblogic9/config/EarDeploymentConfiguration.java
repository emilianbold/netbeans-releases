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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicApplication;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * EAR application deployment configuration handles weblogic-application.xml configuration 
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration implements ModuleConfiguration, DeploymentPlanConfiguration {
    
    private final File file;
    private final J2eeModule j2eeModule;
    private final DataObject dataObject;
    
    private WeblogicApplication weblogicApplication;
        
    /**
     * Creates a new instance of EarDeploymentConfiguration 
     */
    public EarDeploymentConfiguration(J2eeModule j2eeModule) {
        this.j2eeModule = j2eeModule;
        file = j2eeModule.getDeploymentConfigurationFile("META-INF/weblogic-application.xml"); // NOI18N
        getWeblogicApplication();
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(FileUtil.toFileObject(file));
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        }
        this.dataObject = dataObject;
    }
       
    /**
     * Return weblogicApplication graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return weblogicApplication graph or null if the weblogic-application.xml file is not parseable.
     */
    public synchronized WeblogicApplication getWeblogicApplication() {
        if (weblogicApplication == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        weblogicApplication = weblogicApplication.createGraph(file);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // weblogic-application.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic-application.xml if it does not exist yet
                    weblogicApplication = genereateweblogicApplication();
                    ConfigUtil.writefile(file, weblogicApplication);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return weblogicApplication;
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }
    

    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public void dispose() {
    }
    
    public void save(OutputStream os) throws ConfigurationException {
        WeblogicApplication weblogicApplication = getWeblogicApplication();
        if (weblogicApplication == null) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_cannotSaveNotParseableConfFile", file.getPath());
            throw new ConfigurationException(msg);
        }
        try {
            weblogicApplication.write(os);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WarDeploymentConfiguration.class, "MSG_CannotUpdateFile", file.getPath());
            throw new ConfigurationException(msg, ioe);
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Context graph.
     */
    private WeblogicApplication genereateweblogicApplication() {
        return new WeblogicApplication();
    }
}
