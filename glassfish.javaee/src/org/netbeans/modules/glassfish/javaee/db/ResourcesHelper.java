/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.glassfish.eecommon.api.DomainEditor;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourcesHelper {
    public static void addSampleDatasource(J2eeModule module) {
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
                        if (commonSupport.getServerState() != ServerState.RUNNING) {
                            DomainEditor de = new DomainEditor(gfdir, domain, false);
                            de.createSampleDatasource();
                        } else {
                            registerSampleResource(commonSupport);
                        }
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

    static private void registerSampleResource(GlassfishModule commonSupport) {
        String sample_poolname = "SamplePool"; //NOI18N
        String sample_jdbc = "jdbc/sample"; //NOI18N
        String sample_classname = "org.apache.derby.jdbc.ClientDataSource"; //NOI18N
        String sample_restype = "javax.sql.DataSource"; //NOI18N
        String sample_props = "DatabaseName=sample:User=app:Password=app:PortNumber=1527:serverName=localhost:URL=jdbc:derby://localhost:1527/sample"; //NOI18N
        Map<String, ResourceDesc> jdbcsMap = commonSupport.getResourcesMap(GlassfishModule.JDBC_RESOURCE);
        if (!jdbcsMap.containsKey(sample_jdbc)) {
            CreateJDBCConnectionPoolCommand poolCmd = new CreateJDBCConnectionPoolCommand(sample_poolname, sample_classname, sample_restype, sample_props);
            Future<OperationState> poolResult = commonSupport.execute(poolCmd);
            try {
                if (poolResult.get(60, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                    CreateJDBCResourceCommand jdbcCmd = new CreateJDBCResourceCommand(sample_jdbc, sample_poolname);
                    commonSupport.execute(jdbcCmd);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            } catch (ExecutionException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            } catch (TimeoutException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    public static final class CreateJDBCConnectionPoolCommand extends ServerCommand {

       public CreateJDBCConnectionPoolCommand(final String name, final String classname, final String restype, final String properties) {
            super("create-jdbc-connection-pool"); // NOI18N
            StringBuilder cmd = new StringBuilder(128); // NOI18N
            if ((classname != null && classname.length() > 0) &&
                    (restype != null && restype.length() > 0) &&
                    (name != null && name.length() > 0)) {
                cmd.append("datasourceclassname=" + classname);
                cmd.append(PARAM_SEPARATOR + "restype=" + restype); // NOI18N
                cmd.append(PARAM_SEPARATOR + "properties=" + properties); // NOI18N
                cmd.append(PARAM_SEPARATOR + "jdbc_connection_pool_id" + "=");
                cmd.append(name);
                query = cmd.toString();
            }
        }
    }

    public static final class CreateJDBCResourceCommand extends ServerCommand {

        public CreateJDBCResourceCommand(final String name, final String poolname) {
            super("create-jdbc-resource"); // NOI18N
            StringBuilder cmd = new StringBuilder(128);
            cmd.append("connectionpoolid=" + poolname); // NOI18N
            cmd.append(PARAM_SEPARATOR + "jndi_name" + "="); // NOI18N
            cmd.append(name);
            query = cmd.toString();
        }
    }
}
