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
import java.util.Iterator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.xml.namespace.NamespaceContext;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;


import org.netbeans.modules.j2ee.websphere6.util.WSUtil;
import org.netbeans.modules.j2ee.websphere6.config.sync.EjbSynchronizer;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbExt;

/**
 * EJB module deployment configuration handles ws-ejb-jar-bnd.xmi and
 * ws-ebj-jar-ext.xmi configuration files creation.
 *
 *
 * @author Arathi
 */
public class WSEjbModuleConfiguration extends WSModuleConfiguration
        implements  DeploymentPlanConfiguration, PropertyChangeListener {
        
    private final File wsEjbJarExtFile;
            
    private final File wsEjbJarBndFile; 
    private WSEjbBnd WSEjbJarBnd;
    private WSEjbExt WSEjbJarExt;
    private DataObject [] dataObjects;
    
    public WSEjbModuleConfiguration(J2eeModule j2eeModule) {
        super(j2eeModule);
         
         wsEjbJarBndFile = j2eeModule.getDeploymentConfigurationFile("ibm-ejb-jar-bnd.xmi");
         wsEjbJarExtFile  = j2eeModule.getDeploymentConfigurationFile("ibm-ejb-jar-ext.xmi");         
        
        getWSEjbJarBnd();                       
        getWSEjbJarExt();
        
        try {
            DataObject webBndDO = DataObject.find(FileUtil.toFileObject(wsEjbJarBndFile));
            webBndDO.addPropertyChangeListener(this);

            DataObject webExtDO = DataObject.find(FileUtil.toFileObject(wsEjbJarExtFile));
            webExtDO.addPropertyChangeListener(this);

            dataObjects = new DataObject[] {webBndDO, webExtDO};
        } catch(DataObjectNotFoundException donfe) {
            dataObjects = new DataObject[] {};
            Exceptions.printStackTrace(donfe);
        }
               
        File ejbJar = getJ2eeModule().getDeploymentConfigurationFile("ejb-jar.xml");
      
        if (ejbJar.exists()) {
            String contents = WSUtil.readFile(ejbJar);
            
            String ID = "id=\"ID_ejb_jar\"";
            if(contents!=null && contents.indexOf(ID)==-1) {
                String EjbJarTagOpen = "<ejb-jar";
                String EjbJarTagClose = ">";
                int startIndex = contents.indexOf(EjbJarTagOpen);
                if(startIndex!=-1) {
                    String afterTageOpen = contents.substring(startIndex + EjbJarTagOpen.length());
                    String tag = afterTageOpen.substring(0,afterTageOpen.indexOf(EjbJarTagClose));
                    if(tag.indexOf(ID)==-1) {
                        WSUtil.writeFile(ejbJar , contents.replaceFirst(EjbJarTagOpen + " ",
                                EjbJarTagOpen + " " + ID +" \n    "));
                    }
                }
            }
            
            final EjbSynchronizer ejbSync = new EjbSynchronizer(ejbJar,wsEjbJarBndFile );
            ejbSync.addSyncFile(ejbJar);            
        }
        
    }
    
    /**
     * Return WSEjbJarExt graph. If it was not created yet, load it from the
     * files and cache it. If the files does not exist, generate it.
     *
     * @return WSEjbJarExt graph or null if the ws-ejb-jar-ext.xml files is not
     * parseable.
     */
    public synchronized WSEjbExt getWSEjbJarExt() {
        if (WSEjbJarExt == null) {
            try {
              
                    if (wsEjbJarExtFile.exists()) {
                        try {
                            WSEjbJarExt = new WSEjbExt(wsEjbJarExtFile, false);
                        }
                        catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                        catch (RuntimeException re) {
                        }
                    } else {
                        // create WS-ejb-jar.xml if it does not exist yet
                        WSEjbJarExt = new WSEjbExt();
                        WSEjbJarExt.setDefaults();
                        writefile(wsEjbJarExtFile, WSEjbJarExt);
                    }
                
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        
        return WSEjbJarExt;
    }
    
    /**
     * Return WSEjbJarBnd graph. If it was not created yet, load it from the
     * files and cache it. If the files does not exist, generate it.
     *
     * @return WSEjbJarBnd graph or null if the ws-ejb-jar-bnd.xml files is not
     * parseable.
     */
    public synchronized WSEjbBnd getWSEjbJarBnd() {
        if (WSEjbJarBnd == null) {
            try {
              
                    if (wsEjbJarBndFile.exists()) {
                        try {
                            WSEjbJarBnd = new WSEjbBnd(wsEjbJarBndFile, false);
                        }
                        catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                        catch (RuntimeException re) {
                        }
                    } else {
                        WSEjbJarBnd = new WSEjbBnd();
                        WSEjbJarBnd.setDefaults();
                        writefile(wsEjbJarBndFile, WSEjbJarBnd);
                    }
                
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        
        return WSEjbJarBnd;
    }
    
    public DataObject [] getDataObject() {
        return dataObjects.clone();
    }
    
    public void save(OutputStream outputStream) throws ConfigurationException {
        WSEjbBnd  wsEjbBnd = getWSEjbJarBnd();
        WSEjbExt wsEjbExt = getWSEjbJarExt();
        
        if ( (wsEjbBnd == null) || (wsEjbExt == null)){
            throw new ConfigurationException("Cannot read configuration, it is probably in an inconsistent state."); // NOI18N
        }
        try {
            wsEjbBnd.write(outputStream);
            wsEjbExt.write(outputStream);
        } catch (IOException ioe) {
            throw new ConfigurationException(ioe.getLocalizedMessage());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED) &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, ibmWebApp graph is out of sync
            synchronized (this) {
                WSEjbJarBnd = null;
                WSEjbJarExt = null;
            }
        }
    }

    
}
