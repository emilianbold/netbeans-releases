/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicApplication;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * EAR application deployment configuration handles weblogic-application.xml configuration 
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration extends WLDeploymentConfiguration {
    
    private File file;
    private WeblogicApplication weblogicApplication;
        
    /**
     * Creates a new instance of EarDeploymentConfiguration 
     */
    public EarDeploymentConfiguration(DeployableObject deployableObject) {
        super(deployableObject);
    }
    
    /**
     * EarDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     * 
     * @param file weblogic-application.xml file.
     */
    public void init(File file) {
        this.file = file;
        getWeblogicApplication();
        if (dataObject == null) {
            try {
                dataObject = dataObject.find(FileUtil.toFileObject(file));
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
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
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // weblogic-application.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic-application.xml if it does not exist yet
                    weblogicApplication = genereateweblogicApplication();
                    writefile(file, weblogicApplication);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return weblogicApplication;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        WeblogicApplication weblogicApplication = getWeblogicApplication();
        if (weblogicApplication == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            weblogicApplication.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
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
