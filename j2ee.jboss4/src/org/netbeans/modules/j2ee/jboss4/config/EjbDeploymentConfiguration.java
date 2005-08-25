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
import org.netbeans.modules.j2ee.jboss4.config.gen.Jboss;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * EJB module deployment configuration handles jboss.xml configuration file creation.
 *
 * @author sherold
 */
public class EjbDeploymentConfiguration extends JBDeploymentConfiguration {
    
    private File file;
    private Jboss jboss;
        
    /**
     * Creates a new instance of EjbDeploymentConfiguration 
     */
    public EjbDeploymentConfiguration(DeployableObject deployableObject) {
        super(deployableObject);
    }
    
    /**
     * EjbDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     * 
     * @param file jboss.xml file.
     */
    public void init(File file) {
        this.file = file;
        getJboss();
        if (dataObject == null) {
            try {
                dataObject = dataObject.find(FileUtil.toFileObject(file));
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
    }
       
    /**
     * Return jboss graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return jboss graph or null if the jboss.xml file is not parseable.
     */
    public synchronized Jboss getJboss() {
        if (jboss == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        jboss = jboss.createGraph(file);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // jboss.xml is not parseable, do nothing
                    }
                } else {
                    // create jboss.xml if it does not exist yet
                    jboss = genereatejboss();
                    writefile(file, jboss);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return jboss;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        Jboss jboss = getJboss();
        if (jboss == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            jboss.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate Jboss graph.
     */
    private Jboss genereatejboss() {
        return new Jboss();
    }
}
