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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
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
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.ErrorManager;
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
public class ModuleConfigurationImpl implements DatasourceConfiguration, DeploymentPlanConfiguration,
        EjbResourceConfiguration, ContextRootConfiguration, MappingConfiguration, ModuleConfiguration, MessageDestinationConfiguration {
    
    private SunONEDeploymentConfiguration config;
    private J2eeModule module;
    private Lookup lookup;
    
    /** Creates a new instance of ConfigurationSupport */
    ModuleConfigurationImpl(J2eeModule mod) throws ConfigurationException {
        this.module = mod;
        this.config = new SunONEDeploymentConfiguration(mod);
        Object type = mod.getModuleType();
        File dds[] = new File[0];
        
        if (J2eeModule.WAR.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("sun-web.xml") };
        } else if (J2eeModule.EJB.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("sun-ejb-jar.xml"),
            module.getDeploymentConfigurationFile("sun-cmp-mappings.xml") };
        } else if (J2eeModule.CLIENT.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("sun-application-client.xml")};
        } else if (J2eeModule.EAR.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("sun-application.xml") };
        } else if (J2eeModule.CONN.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("sun-ra.xml") };
        }
        
        try {
            config.init(dds, module.getResourceDirectory(), true);
        } catch (javax.enterprise.deploy.spi.exceptions.ConfigurationException ex) {
            throw new ConfigurationException("",ex);
        }
        
        // Support build extension for new resource persistence strategy
        File f = module.getResourceDirectory();
        while (null != f && !f.exists()) {
            f = f.getParentFile();
        }
        if (null != f) {
            Project p = FileOwnerQuery.getOwner(f.toURI());
            if (null != p) {
                J2eeModuleProvider jmp = getProvider(p);
                if (null != jmp) {
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
    public void setMappingInfo(OriginalCMPMapping[] mapping) throws ConfigurationException {
        config.mapCmpBeans(mapping);
    }
    
    
    /** Retrieves the context root field from sun-web.xml for this module, if the module is a
     *  web application.  Otherwise, returns null.
     */
    public String getContextRoot() {
        return config.getContextRoot();
    }
    
    
    /** Sets the context root field in sun-web.xml for this module, if the module is a
     *  web application.
     */
    public void setContextRoot(String contextRoot) {
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
    public void bindEjbReference(String referenceName, String referencedEjbName) throws ConfigurationException {
        config.bindEjbReference(referenceName, referencedEjbName);
    }
    
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName,
            String referencedEjbName) throws ConfigurationException {
        config.bindEjbReferenceForEjb(ejbName, ejbType, referenceName, referencedEjbName);
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
            Project p = null;
            if (null != f) {
                p = FileOwnerQuery.getOwner(f.toURI());
            }
            if (null != p) {
                // TODO : change addExtension default value to true after GF 3317 is resolved
                //boolean addExtension = true;
                boolean addExtension = false;
                DeploymentManager dm = getDeploymentManager(p);
                if (null == dm) {
                    addExtension = false;
                }
                if (dm instanceof SunDeploymentManagerInterface) {
                    SunDeploymentManagerInterface sdmi =
                            (SunDeploymentManagerInterface) dm;
                    if (ServerLocationManager.getAppServerPlatformVersion(sdmi.getPlatformRoot()) < ServerLocationManager.GF_V2) {
                        addExtension = false;
                    }
                } else {
                    // remove the extension  -- the project isn't targeted
                    // for us anymore
                    addExtension = false;
                }
                J2eeModuleProvider jmp = getProvider(p);
                if (null == jmp) {
                    return;
                }
                String target = ModuleType.EAR.equals(jmp.getJ2eeModule().getModuleType()) ? "pre-dist" : "-pre-dist"; // NOI18N
                try {
                    if (addExtension) {
                        BuildExtension.copyTemplate(p);
                        BuildExtension.extendBuildXml(p,target);
                    } else {
                        BuildExtension.abbreviateBuildXml(p,target);
                        BuildExtension.removeTemplate(p);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
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
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null Server InstanceProperties"));
                }
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null J2eeModuleProvider"));
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
            ProjectManager.mutex().writeAccess(new Runnable() {
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

