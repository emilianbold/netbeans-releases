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

import java.io.OutputStream;
import java.util.Set;
import org.netbeans.modules.glassfish.javaee.db.ResourcesHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
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
        MessageDestinationConfiguration,
        EjbResourceConfiguration
{
    
    private Hk2Configuration config;
    private J2eeModule module;
    private Lookup lookup;

    ModuleConfigurationImpl(J2eeModule module) throws ConfigurationException {
        this.module = module;
        this.config = new Hk2Configuration(module);
        ResourcesHelper.addSampleDatasource(module);
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

    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return config.getMessageDestinations();
 }

    public boolean supportsCreateMessageDestination() {
        return config.supportsCreateMessageDestination();
    }

    public MessageDestination createMessageDestination(String name, Type type) throws UnsupportedOperationException, ConfigurationException {
        return config.createMessageDestination(name, type);
    }

    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        config.bindMdbToMessageDestination(mdbName, name, type);
    }

    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return config.findMessageDestinationName(mdbName);
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        config.bindMessageDestinationReference(referenceName, connectionFactoryName, destName, type);
    }

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        config.bindMessageDestinationReferenceForEjb(ejbName, ejbType, referenceName, connectionFactoryName, destName, type);
    }

 }

