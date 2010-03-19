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
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSource;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSourceParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDriverParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertiesType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertyType;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceManager implements DatasourceManager {

    private static final Logger LOGGER = Logger.getLogger(WLDatasourceManager.class.getName());

    private static final String JDBCdotXML = "-jdbc.xml"; // NOI18N

    private final WLDeploymentManager manager;

    public WLDatasourceManager(WLDeploymentManager manager) {
        this.manager = manager;
    }

    @Override
    public void deployDatasources(Set<Datasource> datasources) throws ConfigurationException, DatasourceAlreadyExistsException {
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        Set<Datasource> datasources = new HashSet<Datasource>();

        String domainDir = manager.getInstanceProperties().getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR);
        FileObject domain = FileUtil.toFileObject(FileUtil.normalizeFile(new File(domainDir)));
        FileObject jdbcConfig = null;
        if (domain != null) {
            jdbcConfig = domain.getFileObject("config/jdbc"); // NOI18N
        }

        if (jdbcConfig == null || !jdbcConfig.isValid() || !jdbcConfig.isFolder() || !jdbcConfig.canRead()) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(WLDatasourceManager.class, "ERR_WRONG_CONFIG_DIR"));
            return datasources;
        }

        Enumeration files = jdbcConfig.getChildren(true);
        List<FileObject> confs = new LinkedList<FileObject>();
        while (files.hasMoreElements()) { // searching for config files with DS
            FileObject file = (FileObject) files.nextElement();
            if (!file.isFolder() && file.getNameExt().endsWith(JDBCdotXML) && file.canRead()) {
                confs.add(file);
            }
        }

        if (confs.size() == 0) { // nowhere to search
            return datasources;
        }

        for (Iterator it = confs.iterator(); it.hasNext();) {
            FileObject dsFO = (FileObject) it.next();
            File dsFile = FileUtil.toFile(dsFO);
            try {
                JdbcDataSource ds = null;
                try {
                    ds = JdbcDataSource.createGraph(dsFile);
                } catch (RuntimeException re) {
                    String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                    Logger.getLogger("global").log(Level.INFO, msg);
                    continue;
                }

                // FIXME multi datasources
                // FIXME password
                // FIXME multiple jndis
                if (getJndiNames(ds) != null && getJndiNames(ds).length > 0) {
                    datasources.add(new WLDatasource(getName(ds), getConnectionUrl(ds),
                            getJndiNames(ds)[0], getUserName(ds), getUserName(ds),
                            getDriverClass(ds)));
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_CannotReadDatasources", dsFile.getAbsolutePath());
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                re.printStackTrace();
                throw new ConfigurationException(msg, re);
            }
        }

        return datasources;
    }

    private static String getName(JdbcDataSource ds) {
        return ds.getName();
    }

    private static String[] getJndiNames(JdbcDataSource ds) {
        JdbcDataSourceParamsType params = ds.getJdbcDataSourceParams();
        if (params != null) {
            return params.getJndiName();
        }
        return null;
    }

    private static String getConnectionUrl(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getUrl();
        }
        return null;
    }

    private static String getDriverClass(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getDriverName();
        }
        return null;
    }

    private static String getUserName(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            JdbcPropertiesType props = params.getProperties();
            if (props != null) {
                for (JdbcPropertyType item : props.getProperty2()) {
                    if ("user".equals(item.getName())) { // NOI18N
                        return item.getValue();
                    }
                }
            }
        }
        return null;
    }

}
