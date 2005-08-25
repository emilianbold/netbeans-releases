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

package org.netbeans.modules.j2ee.jboss4.config;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossApp;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * EAR application deployment configuration handles jboss-app.xml configuration 
 * file creation.
 *
 * @author sherold
 */
public class EarDeploymentConfiguration extends JBDeploymentConfiguration {
    
    private File file;
    private JbossApp jbossApp;
        
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
     * @param file jboss-app.xml file.
     */
    public void init(File file) {
        this.file = file;
        getJbossApp();
        if (dataObject == null) {
            try {
                dataObject = dataObject.find(FileUtil.toFileObject(file));
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
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
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        jbossApp = jbossApp.createGraph(file);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss-app.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss-app.xml if it does not exist yet
                    jbossApp = genereatejbossApp();
                    writefile(file, jbossApp);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jbossApp;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        JbossApp jbossApp = getJbossApp();
        if (jbossApp == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            jbossApp.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Context graph.
     */
    private JbossApp genereatejbossApp() {
        return new JbossApp();
    }
}
