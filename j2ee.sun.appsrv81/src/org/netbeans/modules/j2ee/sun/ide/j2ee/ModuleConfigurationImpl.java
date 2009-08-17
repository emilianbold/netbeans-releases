/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MappingConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/** Implementation of ModuleConfiguration.
 *
 *  Primarily serves to delegate directly to the specified DeploymentConfiguration
 *  instance, as that is in shared code and has appropriate access and this instance
 *  is not.
 *
 */
public class ModuleConfigurationImpl implements 
        ModuleConfiguration,
        ContextRootConfiguration,
        DeploymentPlanConfiguration,
        DatasourceConfiguration,
        EjbResourceConfiguration,
        MessageDestinationConfiguration,
        MappingConfiguration
{
    
    private SunONEDeploymentConfiguration config;
    private J2eeModule module;
    private Lookup lookup;
    
    ModuleConfigurationImpl(J2eeModule module) throws ConfigurationException {
        this.module = module;
        this.config = new SunONEDeploymentConfiguration(module);
        
        // Support build extension for new resource persistence strategy
        File f = module.getResourceDirectory();
        if(null != f && f.exists()){
             ResourceUtils.migrateResources(f);
        }
        while (null != f && !f.exists()) {
            f = f.getParentFile();
        }
        if (null != f) {
            Project p = FileOwnerQuery.getOwner(f.toURI());
            if (null != p) {
                J2eeModuleProvider jmp = getProvider(p);
                if (null != jmp) {
                    ResourceUtils.createSampleDataSource(jmp);
                    InstanceListener il = new StaticBuildExtensionListener(f);
                    
                    // TODO : reenable when GF 3317 is resolved
                    //jmp.addInstanceListener(il);
                    
                    // migrate existing projects to currently supported resource
                    // registration strategy
                    il.instanceAdded("ignored");    // NOI18N
                }
            } else {
                Logger.getLogger(ModuleConfigurationImpl.class.getName()).finer("Could not find project for J2eeModule");   // NOI18N
            }
        } else {
            Logger.getLogger(ModuleConfigurationImpl.class.getName()).finer("Could not find project root directory for J2eeModule");   // NOI18N
        }
    }
    
    
    public J2eeModule getJ2eeModule() {
        return module;
    }
    
    
    public synchronized Lookup getLookup() {
        if (null == lookup) {
            lookup = Lookups.fixed(this);
        }
        return lookup;
    }
    
    
    /** Called by j2eeserver to allow us to cleanup the deployment configuration object
     *  for this J2EE project.
     */
    public void dispose() {
        config.dispose();
    }
    
    
    /** Called through j2eeserver when a new EJB resource may need to be added to the
     *  user's project.
     */
    public void setCMPResource(String ejbName, String jndiName) throws ConfigurationException {
        config.setCMPResource(ejbName, jndiName);
    }

    
    /** Conduit to pass the cmp mapping information directly to the configuration
     *  backend.
     */
    public void setMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException {
        config.setMappingInfo(mappings);
    }
    
    
    /** Retrieves the context root field from sun-web.xml for this module, if the module is a
     *  web application.  Otherwise, returns null.
     */
    public String getContextRoot() throws ConfigurationException {
        return config.getContextRoot();
    }


    /** Sets the context root field in sun-web.xml for this module, if the module is a
     *  web application.
     */
    public void setContextRoot(String contextRoot) throws ConfigurationException {
        config.setContextRoot(contextRoot);
    }
    
    
    /**
     * Implementation of DS Management API in ConfigurationSupport
     * @param config deployment configuration object for this J2EE project.
     * @return Returns Set of SunDataSource's(JDBC Resources) present in this J2EE project
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     */
    public Set<Datasource> getDatasources() {
        Set<Datasource> projectDS = config.getDatasources();
        return projectDS;
    }
    
    /**
     * Implementation of DS Management API in DatasourceConfiguration
     *
     * @return Returns true of plugin implements DS Management API's
     */
    public boolean supportsCreateDatasource() {
        return true;
    }
    
    /**
     * Implementation of DS Management API in ConfigurationSupport
     * Creates DataSource objects for this J2EE Project
     * @param config deployment configuration object for this J2EE project.
     * @param jndiName JNDI Name of JDBC Resource
     * @param url Url for database referred to by this JDBC Resource's Connection Pool
     * @param username UserName for database referred to by this JDBC Resource's Connection Pool
     * @param password Password for database referred to by this JDBC Resource's Connection Pool
     * @param driver Driver ClassName for database referred to by this JDBC Resource's Connection Pool
     * @return Set containing SunDataSource
     */
    public Datasource createDatasource(String jndiName, String  url, String username,
            String password, String driver)
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException    {
        return config.createDatasource(jndiName, url, username, password, driver);
    }
    
    /**
     * Write the deployment plan file to the specified output stream.
     *
     *
     * @param outputStream the deployment paln file should be written to.
     * @throws ConfigurationException if an error
     */
    public void save(OutputStream outputStream) throws ConfigurationException {
        config.saveConfiguration(outputStream);
    }
    
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReference(referenceName, jndiName);
    }
    
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }
    
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiName(referenceName);
    }
    
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiNameForEjb(ejbName, referenceName);
    }
    
    /****************************  EjbResourceConfiguration ************************************/
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        // TODO
        return config.findJndiNameForEjb(ejbName);
    }
    
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindEjbReference(referenceName, jndiName);
    }
    
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName,
            String jndiName) throws ConfigurationException {
        config.bindEjbReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }
    
    /****************************  MessageDestinationConfiguration ************************************/
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return config.getMessageDestinations();
    }
    
    public boolean supportsCreateMessageDestination(){
        return true;
    }
    
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, ConfigurationException {
        return config.createMessageDestination(name, type);
    }
    
    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException {
        config.bindMdbToMessageDestination(mdbName, name, type);
    }
    
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return config.findMessageDestinationName(mdbName);
    }
    
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
        config.bindMessageDestinationReference(referenceName, connectionFactoryName,
                destName, type);
    }
    
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
        config.bindMessageDestinationReferenceForEjb(ejbName, ejbType, referenceName,
                connectionFactoryName, destName, type);
    }
    
    static private J2eeModuleProvider getProvider(Project project) {
        J2eeModuleProvider provider = null;
        if (project != null) {
            org.openide.util.Lookup lookup = project.getLookup();
            provider = lookup.lookup(J2eeModuleProvider.class);
        }
        return provider;
    }

    static class StaticBuildExtensionListener  implements InstanceListener {
        File projectDirectory;
        StaticBuildExtensionListener(File pd) {
            projectDirectory = pd;
        }
                
        private void rewriteBuildImpl(FileObject tmp) {
            File f = FileUtil.toFile(tmp);
            final Project p;// = null;
            if (null != f) {
                p = FileOwnerQuery.getOwner(f.toURI());
            } else {
                p = null;
            }
            if (null != p) {
                final boolean addExtension;// = false;
                DeploymentManager dm = getDeploymentManager(p);
                if (null == dm) {
                    addExtension = false;
                } else if (dm instanceof SunDeploymentManagerInterface) {
                    SunDeploymentManagerInterface sdmi =
                            (SunDeploymentManagerInterface) dm;
                    if (ServerLocationManager.getAppServerPlatformVersion(sdmi.getPlatformRoot()) < ServerLocationManager.GF_V2) {
                        addExtension = false;
                    } else {
                        // TODO : change addExtension default value to true after GF 3317 is resolved
                        //addExtension = true;
                        addExtension = false;
                    }
                } else { // null != dm && ! (dm instanceof SunDeploymentManagerInterface) 
                    // remove the extension  -- the project isn't targeted
                    // for us anymore
                    addExtension = false;
                }
                J2eeModuleProvider jmp = getProvider(p);
                if (null == jmp) {
                    return;
                }
                final String target = J2eeModule.Type.EAR.equals(jmp.getJ2eeModule().getType()) ? "pre-dist" : "-pre-dist"; // NOI18N
                ProjectManager.mutex().postWriteRequest(new Runnable() {

                    public void run() {
                        try {
                            if (addExtension) {
                                BuildExtension.copyTemplate(p);
                                BuildExtension.extendBuildXml(p, target);
                            } else {
                                BuildExtension.abbreviateBuildXml(p, target);
                                BuildExtension.removeTemplate(p);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            }
        }

        private DeploymentManager getDeploymentManager(Project p) {
            DeploymentManager dm = null;
            J2eeModuleProvider provider = getProvider(p);
            if(provider != null) {
                InstanceProperties ip = provider.getInstanceProperties();
                if(ip != null) {
                    dm = ip.getDeploymentManager();
                } else {
                    Logger.getLogger(ModuleConfigurationImpl.class.getName()).finer("Null Server InstanceProperties");
                }
            } else {
                Logger.getLogger(ModuleConfigurationImpl.class.getName()).finer("Null J2eeModuleProvider");
            }
            return dm;
        }
        
        public void changeDefaultInstance(String oldServerInstanceID,
                                          String newServerInstanceID) {
            // Ignored
        }

        public void instanceAdded(String serverInstanceID) {
            //Thread.dumpStack();
            reactToInstanceChange(this);
        }

        public void instanceRemoved(String serverInstanceID) {
            // Ignored
        }
        
        private void reactToInstanceChange(final InstanceListener il) {
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    Project p = FileOwnerQuery.getOwner(projectDirectory.toURI());
                    J2eeModuleProvider jmp = null;
                    if (null != p) {
                        jmp = getProvider(p);
                    }
                    if (null != jmp) {
                        jmp.removeInstanceListener(il);
                        FileObject tmp[] = jmp.getSourceRoots();
                        if (null != tmp && tmp.length > 0) {
                            if (null == tmp[0]) {
                                Logger.getLogger(ModuleConfigurationImpl.class.getName()).log(
                                    Level.FINER,"ignorable", new IllegalArgumentException("FileObject tmp[0] is null"));
                                return;
                            }
                            rewriteBuildImpl(tmp[0]);
                        }
                        // TODO : reenable this when GF 3317 is resolved
                        //jmp.addInstanceListener(il);
                    }
                }
            });
        }
    }

}

