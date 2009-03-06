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
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.eecommon.api.DomainEditor;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
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
        EjbResourceConfiguration
{
    
    private Hk2Configuration config;
    private J2eeModule module;
    private Lookup lookup;

    ModuleConfigurationImpl(J2eeModule module) throws ConfigurationException {
        this.module = module;
        this.config = new Hk2Configuration(module);
        addSampleDatasource();
    }

    // ------------------------------------------------------------------------
    // J2EE Server API implementations
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Implementation of ModuleConfiguration
    // ------------------------------------------------------------------------
    public synchronized Lookup getLookup() {
        if (null == lookup) {
            lookup = Lookups.fixed(this);
        }
        return lookup;
    }

    public J2eeModule getJ2eeModule() {
        return module;
    }

    public void dispose() {
        config.dispose();
    }

    // ------------------------------------------------------------------------
    // Implementation of ContextRootConfiguration
    // ------------------------------------------------------------------------
    public String getContextRoot() throws ConfigurationException {
        return config.getContextRoot();
    }

    public void setContextRoot(String contextRoot) throws ConfigurationException {
        config.setContextRoot(contextRoot);
    }

    // ------------------------------------------------------------------------
    // Implementation of DeploymentPlanConfiguration
    // ------------------------------------------------------------------------
    public void save(OutputStream outputStream) throws ConfigurationException {
        config.saveConfiguration(outputStream);
    }

    // ------------------------------------------------------------------------
    // Implementation of DatasourceConfiguration
    // ------------------------------------------------------------------------
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return config.getDatasources();
    }

    public boolean supportsCreateDatasource() {
        return config.supportsCreateDatasource();
    }

    public Datasource createDatasource(String jndiName, String url, String username, String password, String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        return config.createDatasource(jndiName, url, username, password, driver);
    }

    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReference(referenceName, jndiName);
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiName(referenceName);
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiNameForEjb(ejbName, referenceName);
    }

    // ------------------------------------------------------------------------
    // Implementation of EjbResourceConfiguration
    // ------------------------------------------------------------------------
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return config.findJndiNameForEjb(ejbName);
    }

    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindEjbReference(referenceName, jndiName);
    }

    public void bindEjbReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        config.bindEjbReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }

    private void addSampleDatasource() {
        File f = module.getResourceDirectory();
        if(null != f && f.exists()){
            f = f.getParentFile();
        }
        if (null != f) {
            Project p = FileOwnerQuery.getOwner(f.toURI());
            if (null != p) {
                J2eeModuleProvider jmp = getProvider(p);
                if (null != jmp) {
                    DeploymentManager dm = getDeploymentManager(jmp);
                    if (dm instanceof Hk2DeploymentManager) {
                        GlassfishModule commonSupport = ((Hk2DeploymentManager) dm).getCommonServerSupport();
                        String gfdir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR); 
                        String domain = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
                        DomainEditor de = new DomainEditor(gfdir, domain, false);
                        de.createSampleDatasource();
                    }
                }
            } else {
                Logger.getLogger("glassfish-javaee").finer("Could not find project for J2eeModule");   // NOI18N
            }
        } else {
            Logger.getLogger("glassfish-javaee").finer("Could not find project root directory for J2eeModule");   // NOI18N
        }
    }

    static private J2eeModuleProvider getProvider(Project project) {
        J2eeModuleProvider provider = null;
        if (project != null) {
            org.openide.util.Lookup lookup = project.getLookup();
            provider = lookup.lookup(J2eeModuleProvider.class);
        }
        return provider;
    }

    static private DeploymentManager getDeploymentManager(J2eeModuleProvider provider) {
        DeploymentManager dm = null;
        InstanceProperties ip = provider.getInstanceProperties();
        if (ip != null) {
            dm = ip.getDeploymentManager();
        }
        return dm;
    }
}

