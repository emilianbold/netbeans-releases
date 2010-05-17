/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.websphere6.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;

import org.netbeans.modules.j2ee.websphere6.util.WSUtil;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppExt;

/**
 * EAR application deployment configuration handles websphere-application.xml configuration
 * file creation.
 *
 * @author Arathi
 */

public class WSEarModuleConfiguration extends WSModuleConfiguration 
        implements DeploymentPlanConfiguration, PropertyChangeListener {
    
    private File  webspheredpfFile;
    private File  wsAppBndFile;
    private File  wsAppExtFile;
    
    private WSAppBnd websphereApplicationBnd;
    private WSAppExt websphereApplicationExt;
    
    DataObject [] dataObjects;
    
    public WSEarModuleConfiguration(J2eeModule j2eeModule) {
        
        super(j2eeModule);
   
        webspheredpfFile = j2eeModule.getDeploymentConfigurationFile("WebSphere6.dpf");
        wsAppBndFile = j2eeModule.getDeploymentConfigurationFile("ibm-application-bnd.xmi");
        wsAppExtFile = j2eeModule.getDeploymentConfigurationFile("ibm-application-ext.xmi");
        
        getWebSphereApplicationExt();
        getWebSphereApplicationBnd();
        
        try {
            DataObject webBndDO = DataObject.find(FileUtil.toFileObject(wsAppBndFile));
            webBndDO.addPropertyChangeListener(this);

            DataObject webExtDO = DataObject.find(FileUtil.toFileObject(wsAppExtFile));
            webExtDO.addPropertyChangeListener(this);

            dataObjects = new DataObject[] {webBndDO, webExtDO};
        } catch(DataObjectNotFoundException donfe) {
            dataObjects = new DataObject[] {};
            Exceptions.printStackTrace(donfe);
        }
        correctDeploymentDescriptors();
        attachFOListener();
        
    }
    
    
    
    /**
     *
     */
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (evt.getPropertyName() == DataObject.PROP_MODIFIED &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, WSWeb{Ext,Bnd} graph is out of sync
            synchronized (this) {
                websphereApplicationExt = null;
                websphereApplicationBnd = null;
            }
        }
    }
    
    /**
     * Return websphereApplication graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     * @return websphereApplication graph or null if the websphere-application.xml file is not parseable.
     */
    public synchronized WSAppBnd getWebSphereApplicationBnd() {
        if (websphereApplicationBnd == null) {
            try {

                if (!webspheredpfFile.exists()) {
                    writefile(webspheredpfFile, null);
                }

                if (wsAppBndFile.exists()) {
                    // load configuration if already exists
                    try {                        
                        websphereApplicationBnd = new WSAppBnd(wsAppBndFile, false);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                    // websphere-application.xml is not parseable, do nothing
                    }
                } else {
                    // create ibm-application-bnd.xmi if it does not exist yet
                    websphereApplicationBnd = new WSAppBnd();
                    websphereApplicationBnd.setDefaults();
                    writefile(wsAppBndFile, websphereApplicationBnd);
                }

            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }

        return websphereApplicationBnd;
    }

    public synchronized WSAppExt getWebSphereApplicationExt() {
        if (websphereApplicationExt == null) {
            try {
                if (!webspheredpfFile.exists()) {
                    writefile(webspheredpfFile, null);
                }

                if (wsAppExtFile.exists()) {
                    // load configuration if already exists
                    try {
                        websphereApplicationExt = new WSAppExt(wsAppExtFile, false);
                    //websphereApplicationExt = new WSAppExt(file[i]);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                    // ibm-application-ext.xmi is not parseable, do nothing
                    }
                } else {
                    // create websphere-application.xml if it does not exist yet
                    websphereApplicationExt = new WSAppExt();
                    websphereApplicationExt.setDefaults();
                    writefile(wsAppExtFile, websphereApplicationExt);
                }

            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return websphereApplicationExt;
    }
    
    private void attachFOListener() {
        
       File appInfFile = getJ2eeModule().getDeploymentConfigurationFile("META_INF/application.xml");
        
        int appStart = 0;        
        if(appInfFile.exists()) {
            FileObject fo = FileUtil.toFileObject(appInfFile);
            
            fo.addFileChangeListener(new FileChangeAdapter() {
                public void fileChanged(FileEvent fe) {
                    correctDeploymentDescriptors();
                }
            });
        }
    }
    
    public void correctDeploymentDescriptors() {
       // if (WSDebug.isEnabled())
         //   WSDebug.notify(getClass(), "correcting application.xml");
               
        File appInfFile = getJ2eeModule().getDeploymentConfigurationFile("META_INF/application.xml");
        
        int appStart = 0;        
        if(appInfFile.exists()) {
            String contents = WSUtil.readFile(appInfFile);
            if(contents != null) {
                if((appStart=contents.indexOf("<application")) >= 0) {
                    String appAttr = contents.substring(appStart, contents.indexOf(">", appStart));
                    if(!appAttr.contains(" id=")) {
                        WSUtil.writeFile(appInfFile , contents.replaceFirst("<application", "<application id=\"Application_ID\"\n"));
                    }
                }
            }
        }
    }

    public void save(OutputStream os) throws ConfigurationException {
        WSAppExt websphereApplication = getWebSphereApplicationExt();
        if (websphereApplication == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            websphereApplication.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
     
     
     
        WSAppBnd websphereApplication2 = getWebSphereApplicationBnd();
        if (websphereApplication == null) {
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            websphereApplication2.write(os);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
     
     
    }
    
    
}
