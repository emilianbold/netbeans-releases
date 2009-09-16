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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.javaee.db.ResourcesHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2Configuration extends GlassfishConfiguration implements DeploymentConfiguration {

    public Hk2Configuration(J2eeModule module) throws ConfigurationException {
        super(module);
    }

    @Deprecated
    public Hk2Configuration(DeployableObject dObj) {
    }

    // ------------------------------------------------------------------------
    // DatasourceConfiguration support
    // ------------------------------------------------------------------------
    @Override
    public Set<Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        return Hk2DatasourceManager.getDatasources(module.getResourceDirectory());
    }

    @Override
    public boolean supportsCreateDatasource() {
        return true;
    }

    @Override
    public Datasource createDatasource(String jndiName, String url, String username, String password, String driver) throws UnsupportedOperationException, org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException, DatasourceAlreadyExistsException {
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            // Unable to create JDBC data source for resource ref.
//            postResourceError(NbBundle.getMessage(ModuleConfigurationImpl.class,
//                    "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
            Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                    "Resource Folder " + resourceDir + " does not exist.");
            throw new ConfigurationException(NbBundle.getMessage(
                    ModuleConfigurationImpl.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
        }

        return Hk2DatasourceManager.createDataSource(jndiName, url, username, password, driver, resourceDir);
    }

    // ------------------------------------------------------------------------
    // MessageDestinationConfiguration support
    // ------------------------------------------------------------------------
    @Override
    public Set<MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        return Hk2MessageDestinationManager.getMessageDestinations(module.getResourceDirectory());
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return ResourcesHelper.isEE6(module);
    }

    @Override
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING,
                    "Resource Folder " + resourceDir + " does not exist.");
            throw new ConfigurationException(NbBundle.getMessage(
                    ModuleConfigurationImpl.class, "ERR_NoJMSResource", name, type)); // NOI18N
    }

        return Hk2MessageDestinationManager.createMessageDestination(name, type, resourceDir);
    }

    // ------------------------------------------------------------------------
    // Implementation (or lack thereof) of JSR-88 DeploymentConfiguration interface
    // Here to make the deployment manager class happy.
    // ------------------------------------------------------------------------
    public DeployableObject getDeployableObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot ddbeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDConfigBean(DConfigBeanRoot dconfigBeanRoot) throws BeanNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DConfigBeanRoot restoreDConfigBean(InputStream is, DDBeanRoot ddbeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveDConfigBean(OutputStream os, DConfigBeanRoot dconfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void restore(InputStream is) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void save(OutputStream os) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
