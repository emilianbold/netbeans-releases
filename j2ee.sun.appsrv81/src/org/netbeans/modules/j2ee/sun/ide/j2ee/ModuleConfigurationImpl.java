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
import java.io.OutputStream;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import org.netbeans.modules.j2ee.dd.api.common.ComponentInterface;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;

import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MappingConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.ErrorManager;
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
        EjbResourceConfiguration, ContextRootConfiguration, MappingConfiguration, ModuleConfiguration {
    
    private SunONEDeploymentConfiguration config;
    private J2eeModule module;
    private Lookup lookup;

    /** Creates a new instance of ConfigurationSupport */
    ModuleConfigurationImpl(J2eeModule module) throws ConfigurationException {
        this.module = module;
        this.config = new SunONEDeploymentConfiguration(module);
        Object type = module.getModuleType();
        File dds[] = new File[0];
        if (module.EJB.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("META-INF/sun-ejb-jar.xml"),
                module.getDeploymentConfigurationFile("META-INF/sun-cmp-mappings.xml") };
        } else if (module.CLIENT.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("META-INF/sun-application-client.xml")};
        } else if (module.WAR.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("WEB-INF/sun-web.xml") };
        } else if (module.CONN.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("META-INF/sun-ra.xml") };
        } else if (module.EAR.equals(type)) {
            dds = new File[] { module.getDeploymentConfigurationFile("META-INF/sun-application.xml") };
        }
        try {            
            config.init(dds, module.getResourceDirectory(), true);
        } catch (javax.enterprise.deploy.spi.exceptions.ConfigurationException ex) {
            throw new ConfigurationException("",ex);
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
        checkConfiguration(config);
        ((SunONEDeploymentConfiguration)config).dispose();
    }

    
    /** Called through j2eeserver when a new EJB resource may need to be added to the
     *  user's project.
     */
    public void ensureResourceDefined(ComponentInterface ci, String jndiName) {
        checkConfiguration(config);
        if(ci == null) {
            throw new IllegalArgumentException("DDBean parameter cannot be null.");
        }
//        ((SunONEDeploymentConfiguration)config).ensureResourceDefinedForEjb(ci, jndiName);
    }

    
    /** Conduit to pass the cmp mapping information directly to the configuration
     *  backend.
     */
    public void setMappingInfo(OriginalCMPMapping[] mapping){
        checkConfiguration(config);
        SunONEDeploymentConfiguration s1config = (SunONEDeploymentConfiguration) config;
        EjbJarRoot ejbJarRoot = s1config.getEjbJarRoot();
        if(ejbJarRoot != null) {
            ejbJarRoot.mapCmpBeans(mapping);
        }
    }


    /** Retrieves the context root field from sun-web.xml for this module, if the module is a
     *  web application.  Otherwise, returns null.
     */
    public String getContextRoot() {
        checkConfiguration(config);
        return ((SunONEDeploymentConfiguration)config).getContextRoot();
    }

    
    /** Sets the context root field in sun-web.xml for this module, if the module is a
     *  web application.
     */
    public void setContextRoot(String contextRoot) {
        checkConfiguration(config);
        ((SunONEDeploymentConfiguration)config).setContextRoot(contextRoot);
    }    
    
    
    /** Utility method to validate the configuration object being passed to the
     *  other methods in this class.
     */
    private void checkConfiguration(DeploymentConfiguration config) {
        if(config == null) {
            throw new IllegalArgumentException("DeploymentConfiguration is null");
        }
        if(!(config instanceof SunONEDeploymentConfiguration)) {
            throw new IllegalArgumentException("Wrong DeploymentConfiguration instance " + config.getClass().getName());
        }
    }
    
    /**
     * Implementation of DS Management API in ConfigurationSupport
     * @param config deployment configuration object for this J2EE project.
     * @return Returns Set of SunDataSource's(JDBC Resources) present in this J2EE project
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     */
    public Set getDatasources() {
        checkConfiguration(config);
        SunONEDeploymentConfiguration sunConfig = ((SunONEDeploymentConfiguration)config);
        Set projectDS = sunConfig.getDatasources();
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
        checkConfiguration(config);
        SunONEDeploymentConfiguration sunConfig = ((SunONEDeploymentConfiguration)config);
        return sunConfig.createDatasource(jndiName, url, username, password, driver);
    }

    /**
     * Write the deployment plan file to the specified output stream.
     * 
     * 
     * @param outputStream the deployment paln file should be written to.
     * @throws ConfigurationException if an error
     */
    public void save(OutputStream outputStream) throws ConfigurationException {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new UnsupportedOperationException());
    }

    public void bindDatasourceReference(String jndiName, String dsJNDIName) throws ConfigurationException {
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String jndiName, String dsJNDIName) throws ConfigurationException {
    }

    public String findDatasourceJndiName(String jndiName) throws ConfigurationException {
        return null;
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String jndiName) throws ConfigurationException {
        return null;
    }

}

