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
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicEjbJar;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * EJB module deployment configuration handles weblogic-ejb-jar.xml configuration file creation.
 *
 * @author sherold
 */
public class EjbDeploymentConfiguration extends WLDeploymentConfiguration {
    
    private File file;
    private WeblogicEjbJar weblogicEjbJar;
        
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
     * @param file weblogic-ejb-jar.xml file.
     */
    public void init(File file) {
        this.file = file;
        getWeblogicEjbJar();
        if (dataObject == null) {
            try {
                dataObject = dataObject.find(FileUtil.toFileObject(file));
            } catch(DataObjectNotFoundException donfe) {
                ErrorManager.getDefault().notify(donfe);
            }
        }
    }
       
    /**
     * Return WeblogicEjbJar graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return WeblogicEjbJar graph or null if the weblogic-ejb-jar.xml file is not parseable.
     */
    public synchronized WeblogicEjbJar getWeblogicEjbJar() {
        if (weblogicEjbJar == null) {
            try {
                if (file.exists()) {
                    // load configuration if already exists
                    try {
                        weblogicEjbJar = weblogicEjbJar.createGraph(file);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    } catch (RuntimeException re) {
                        // weblogic-ejb-jar.xml is not parseable, do nothing
                    }
                } else {
                    // create weblogic-ejb-jar.xml if it does not exist yet
                    weblogicEjbJar = genereateWeblogicEjbJar();
                    writefile(file, weblogicEjbJar);
                }
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
            }
        }
        return weblogicEjbJar;
    }
    
    // JSR-88 methods ---------------------------------------------------------
    
    public void save(OutputStream os) throws ConfigurationException {
        WeblogicEjbJar weblogicEjbJar = getWeblogicEjbJar();
        if (weblogicEjbJar == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            weblogicEjbJar.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }
    
    // private helper methods -------------------------------------------------
    
    /**
     * Genereate WeblogicEjbJar graph.
     */
    private WeblogicEjbJar genereateWeblogicEjbJar() {
        return new WeblogicEjbJar();
    }
}
