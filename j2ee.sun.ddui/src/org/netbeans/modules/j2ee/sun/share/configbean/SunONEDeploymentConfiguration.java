/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.sun.api.CmpMappingProvider;
import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/** Manages the deployment plan I/O and access for initializing DConfigBeans
 *
 * @author Vince Kraemer
 * @author Peter Williams
 */ 
public class SunONEDeploymentConfiguration extends GlassfishConfiguration implements DeploymentConfiguration { //implements Constants, SunDeploymentConfigurationInterface {

    /**
     * inject cmp bean & field update support into descriptor listener factories
     */
    static {
        CmpListenerSupport.enableCmpListenerSupport();
    }

    /** Value to hold the module name used by the IDE to define the deployable object
     *  this is a jsr88 extension for directory deployment: we need to find a good
     *  dir name to put the bits that will be deployed.
     */
    private String deploymentModuleName = "_default_"; // NOI18N

    private static final RequestProcessor resourceProcessor = new RequestProcessor("sun-resource-ref"); // NOI18N
    

    /** Create an instance of SunONEDeploymentConfiguration for GF V2
     *  and earlier servers.
     *
     * @param module J2eeModule instance for the project represented by this config.
     *
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public SunONEDeploymentConfiguration(J2eeModule module) throws ConfigurationException {
        super(module);
    }

    /** Create an instance of SunONEDeploymentConfiguration for Webserver.
     *
     * @param module J2eeModule instance for the project represented by this config.
     * @param webServerDDName short name for web server sun dd
     *
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public SunONEDeploymentConfiguration(J2eeModule module, String webServerDDName) throws ConfigurationException {
        super(module, J2eeModuleHelper.getWsModuleHelper(webServerDDName));
    }

    /** Deprecated form used for JSR-88.  Only exists to keep legacy parts of
     *  j2eeserver module happy.
     *
     * @param dObj JSR-88 deployable object for this JavaEE module.
     * @deprecated
     */
    @Deprecated
    public SunONEDeploymentConfiguration(javax.enterprise.deploy.model.DeployableObject dObj) {
    }


    private void postResourceError(String resourceMsg) {
        // Unable to create JDBC data source for CMP.
        // JNDI name of CMP resource field not set.
        String folderMsg;
        String projectName = getProjectName(primarySunDD);
        if (projectName != null) {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderForProject", projectName); // NOI18N
        } else {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderUnknown"); // NOI18N
        }

        final String text = folderMsg + " " + resourceMsg;
        resourceProcessor.post(new Runnable() {

            public void run() {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });
    }

    private ResourceConfiguratorInterface getResourceConfigurator() {
        ResourceConfiguratorInterface rci = null;
        DeploymentManager dm = getDeploymentManager();
        if (dm instanceof SunDeploymentManagerInterface) {
            SunDeploymentManagerInterface sdmi = (SunDeploymentManagerInterface) dm;
            rci = sdmi.getResourceConfigurator();
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Invalid DeploymentManager: " + dm));
        }
        return rci;
    }

    private DeploymentManager getDeploymentManager() {
        DeploymentManager dm = null;
        J2eeModuleProvider provider = getProvider(primarySunDD);
        if (provider != null) {
            InstanceProperties ip = provider.getInstanceProperties();
            if (ip != null) {
                dm = ip.getDeploymentManager();
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null Server InstanceProperties"));
            }
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null J2eeModuleProvider"));
        }
        return dm;
    }

    private String getProjectName(File file) {
        String result = null;
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                ProjectInformation info = ProjectUtils.getInformation(project);
                if (info != null) {
                    result = info.getName();
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // CMP related automatic descriptor updating support.
    // ------------------------------------------------------------------------
    void removeMappingForCmp(String beanName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.removeMappingForCmp(sunCmpMappings, beanName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void removeMappingForCmpField(String beanName, String fieldName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.removeMappingForCmpField(sunCmpMappings, beanName, fieldName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void renameMappingForCmp(String oldBeanName, String newBeanName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.renameMappingForCmp(sunCmpMappings, oldBeanName, newBeanName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void renameMappingForCmpField(String beanName, String oldFieldName, String newFieldName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.renameMappingForCmpField(sunCmpMappings, beanName, oldFieldName, newFieldName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    /* Get the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public String getDeploymentModuleName() {
        return deploymentModuleName;
    }

    /* Set the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public void setDeploymentModuleName(String s) {
        deploymentModuleName = s;
    }

    // ------------------------------------------------------------------------
    // Implementation of abstract portion of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    public boolean supportsCreateDatasource() {
        return true;
    }

    public Set<Datasource> getDatasources() {
        Set<Datasource> datasources = null;
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        File resourceDir = module.getResourceDirectory();
        if (rci != null && resourceDir != null && resourceDir.exists()) {
            datasources = rci.getResources(resourceDir);
        }
        if(datasources == null) {
            datasources = new HashSet<Datasource>();
        }
        return datasources;
    }

    public Datasource createDatasource(final String jndiName, final String url, final String username, final String password, final String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        Datasource ds = null;
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            // Unable to create JDBC data source for resource ref.
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
            throw new ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
        }

        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            ds = rci.createDataSource(jndiName, url, username, password, driver, resourceDir);
        }
        return ds;
    }

    // ------------------------------------------------------------------------
    // Implementation of abstract portion of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        Set<MessageDestination> destinations = null;
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        File resourceDir = module.getResourceDirectory();
        if (rci != null && resourceDir != null && resourceDir.exists()) {
            destinations = rci.getMessageDestinations(resourceDir);
        }
        if(destinations == null) {
            destinations = new HashSet<MessageDestination>();
        }
        return destinations;
    }

    public boolean supportsCreateMessageDestination() {
        return true;
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, ConfigurationException {
        MessageDestination jmsResource = null;
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            // Unable to create reqested JMS Resource
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
            throw new ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
        }
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            if (!rci.isJMSResourceDefined(name, resourceDir)) {
                jmsResource = rci.createJMSResource(name, type, name, resourceDir);
            }
        }
        return jmsResource;
    }

    // ------------------------------------------------------------------------
    // Implementation of MappingConfiguration
    // ------------------------------------------------------------------------
    public void setCMPResource(String ejbName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    CmpResource cmpResource = eb.getCmpResource();
                    if(cmpResource == null) {
                        cmpResource = eb.newCmpResource();
                        eb.setCmpResource(cmpResource);
                    }

                    String oldJndiName = cmpResource.getJndiName();
                    if(!Utils.strEquivalent(oldJndiName, jndiName)) {
                        if(Utils.notEmpty(oldJndiName)) {
                            // !PW FIXME changing existing jndi name, should we notify user?
                        }

                        cmpResource.setJndiName(jndiName);

                        // if changes, save file.
                        sunEjbJar.write(primarySunDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public void setMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException {
        if(!J2eeModule.Type.EJB.equals(module.getType())) {
            return; // wrong module type.
        }
        
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, true);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;

                    try {
                       CmpMappingProvider mapper = getSunCmpMapper();
                       mapper.mapCmpBeans(sunCmpDDFO, mappings, sunCmpMappings);
                   } catch(Exception ex) {
                       ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                   }                
                   
                    // if changes, save file.
                    sunCmpMappings.write(sunCmpDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    private CmpMappingProvider getSunCmpMapper() {
       CmpMappingProvider mapper = null;
       DeploymentManager dm = getDeploymentManager();
       if(dm instanceof SunDeploymentManagerInterface) {
           SunDeploymentManagerInterface sdmi = (SunDeploymentManagerInterface) dm;
           mapper = sdmi.getSunCmpMapper();
       } else {
           throw new IllegalStateException("Invalid DeploymentManager: " + dm);
       }
       return mapper;
    }

    // ------------------------------------------------------------------------
    // Implementation (or lack thereof) of JSR-88 DeploymentConfiguration
    // ------------------------------------------------------------------------

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot dDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public javax.enterprise.deploy.model.DeployableObject getDeployableObject() {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public void removeDConfigBean(DConfigBeanRoot dConfigBeanRoot) throws BeanNotFoundException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public void restore(InputStream inputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public DConfigBeanRoot restoreDConfigBean(InputStream inputStream, DDBeanRoot dDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public void save(OutputStream outputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    public void saveDConfigBean(OutputStream outputStream, DConfigBeanRoot rootBean) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

}
