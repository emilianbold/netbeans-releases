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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
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
    
    private Hk2Configuration config;
    private J2eeModule module;
    private Lookup lookup;

    /** Creates a new instance of ConfigurationSupport */
    ModuleConfigurationImpl(J2eeModule module) throws ConfigurationException {
        this.module = module;
        this.config = new Hk2Configuration(module);
        
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
        
        config.init(dds);
    }

    public void setCMPResource(String ejbName, String jndiName){

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
//        ((Hk2Configuration)config).dispose();
    }

    
    /** Called through j2eeserver when a new EJB resource may need to be added to the
     *  user's project.
     */
    public void ensureResourceDefined(ComponentInterface ci, String jndiName) {
        checkConfiguration(config);
        if(ci == null) {
            throw new IllegalArgumentException("DDBean parameter cannot be null.");
        }
//        ((Hk2Configuration)config).ensureResourceDefinedForEjb(ci, jndiName);
    }

    public void bindEjbReference(String referenceName, String referencedEjbName) throws ConfigurationException {}
    
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String referencedEjbName) throws ConfigurationException {}
    
    /** Conduit to pass the cmp mapping information directly to the configuration
     *  backend.
     */
    public void setMappingInfo(OriginalCMPMapping[] mapping){
        checkConfiguration(config);
//        Hk2Configuration s1config = (Hk2Configuration) config;
//        EjbJarRoot ejbJarRoot = s1config.getEjbJarRoot();
//        if(ejbJarRoot != null) {
//            ejbJarRoot.mapCmpBeans(mapping);
//        }
    }


    /** Retrieves the context root field from sun-web.xml for this module, if the module is a
     *  web application.  Otherwise, returns null.
     */
    public String getContextRoot() {
        checkConfiguration(config);
        return ((Hk2Configuration)config).getContextPath();
    }

    
    /** Sets the context root field in sun-web.xml for this module, if the module is a
     *  web application.
     */
    public void setContextRoot(String contextRoot) {
        checkConfiguration(config);
        ((Hk2Configuration)config).setContextPath(contextRoot);
    }    
    
    
    /** Utility method to validate the configuration object being passed to the
     *  other methods in this class.
     */
    private void checkConfiguration(Hk2Configuration config) {
        if(config == null) {
            throw new IllegalArgumentException("DeploymentConfiguration is null");
        }
        if(!(config instanceof Hk2Configuration)) {
            throw new IllegalArgumentException("Wrong DeploymentConfiguration instance " + config.getClass().getName());
        }
    }
    
    /**
     * Implementation of DS Management API in ConfigurationSupport
     * @param config deployment configuration object for this J2EE project.
     * @return Returns Set of SunDataSource's(JDBC Resources) present in this J2EE project
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     */
    public Set<Datasource> getDatasources() {
        checkConfiguration(config);
        Hk2Configuration sunConfig = ((Hk2Configuration)config);
//        Set projectDS = sunConfig.getDatasources();
        Set<Datasource> projectDS = new HashSet<Datasource>();
        return projectDS;
    }    
    
    /**
     * Implementation of DS Management API in DatasourceConfiguration
     * 
     * @return Returns true of plugin implements DS Management API's
     */
    public boolean supportsCreateDatasource() {
//        return true;
        return false;
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
//        Hk2Configuration sunConfig = ((Hk2Configuration)config);
        throw new UnsupportedOperationException("createDatasource not supported.");
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
        throw new UnsupportedOperationException("bindDatasourceReference not supported.");
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String jndiName, String dsJNDIName) throws ConfigurationException {
        throw new UnsupportedOperationException("bindDatasourceReferenceForEjb not supported.");
    }

    public String findDatasourceJndiName(String jndiName) throws ConfigurationException {
        return null;
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String jndiName) throws ConfigurationException {
        return null;
    }

    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        throw new UnsupportedOperationException("findJndiNameForEjb not supported.");
    }

}

