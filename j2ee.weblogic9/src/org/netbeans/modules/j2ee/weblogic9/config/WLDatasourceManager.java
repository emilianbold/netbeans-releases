/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLCommandDeployer;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceManager implements DatasourceManager {

    private static final Logger LOGGER = Logger.getLogger(WLDatasourceManager.class.getName());

    private final WLDeploymentManager manager;

    public WLDatasourceManager(WLDeploymentManager manager) {
        this.manager = manager;
    }

    // TODO we should start required JDBC datasources on server just for case
    // it previously failed (ie. db was not accessible at that time)
    @Override
    public void deployDatasources(Set<Datasource> datasources) throws ConfigurationException, DatasourceAlreadyExistsException {
        Set<Datasource> deployedDatasources = getDatasources();
        // for faster searching
        Map<String, Datasource> deployed = createMap(deployedDatasources);

        // will contain all ds which do not conflict with existing ones
        Map<String, WLDatasource> toDeploy = new HashMap<String, WLDatasource>();
        
        // resolve all conflicts
        LinkedList<Datasource> conflictDS = new LinkedList<Datasource>();
        for (Datasource datasource : datasources) {
            if (!(datasource instanceof WLDatasource)) {
                continue;
            }

            String jndiName = datasource.getJndiName();
            if (deployed.keySet().contains(jndiName)) { // conflicting ds found
                if (!deployed.get(jndiName).equals(datasource)) { // found ds is not equal
                    conflictDS.add(deployed.get(jndiName)); // NOI18N
                }
            } else if (jndiName != null) {
                if (datasource instanceof WLDatasource) {
                    toDeploy.put(jndiName, (WLDatasource) datasource);
                } else {
                    LOGGER.log(Level.INFO, "Unable to deploy {0}", datasource);
                }
            }
        }
        
        if (!conflictDS.isEmpty()) {
            throw new DatasourceAlreadyExistsException(conflictDS);
        }

        WLCommandDeployer deployer = new WLCommandDeployer(WLDeploymentFactory.getInstance(),
                manager.getInstanceProperties());
        waitFor(deployer.deployDatasource(toDeploy.values()));
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        File domainPath = FileUtil.normalizeFile(new File(domainDir));
        FileObject domain = FileUtil.toFileObject(domainPath);
        FileObject domainConfig = null;
        if (domain != null) {
            domainConfig = domain.getFileObject("config/config.xml"); // NOI18N
        }

        return new HashSet<Datasource>(WLDatasourceSupport.getDatasources(domainPath, domainConfig));
    }

    private Map<String, Datasource> createMap(Set<Datasource> datasources) {
        if (datasources.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Datasource> map = new HashMap<String, Datasource>();
        for (Datasource datasource : datasources) {
            map.put(datasource.getJndiName(), datasource);
        }
        return map;
    }

    private void waitFor(ProgressObject obj) {
        final CountDownLatch latch = new CountDownLatch(1);
        obj.addProgressListener(new ProgressListener() {

            @Override
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted() || pe.getDeploymentStatus().isFailed()) {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(WLDeploymentManager.MANAGER_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
