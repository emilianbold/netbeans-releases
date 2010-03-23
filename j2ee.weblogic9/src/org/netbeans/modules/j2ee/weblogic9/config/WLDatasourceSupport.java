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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSource;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSourceParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDriverParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertiesType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertyType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceSupport {

    private static final String JDBCdotXML = "-jdbc.xml"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(WLDatasourceSupport.class.getName());

    private File resourceDir;

    public WLDatasourceSupport(File resourceDir) {
        assert resourceDir != null : "Resource directory can't be null"; // NOI18N
        this.resourceDir = FileUtil.normalizeFile(resourceDir);
    }

    public static Set<Datasource> getDatasources(FileObject dir) throws ConfigurationException {
        if (dir == null || !dir.isValid() || !dir.isFolder() || !dir.canRead()) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(WLDatasourceManager.class, "ERR_WRONG_CONFIG_DIR"));
            return Collections.emptySet();
        }

        Enumeration files = dir.getChildren(true);
        List<FileObject> confs = new LinkedList<FileObject>();
        while (files.hasMoreElements()) { // searching for config files with DS
            FileObject file = (FileObject) files.nextElement();
            if (!file.isFolder() && file.getNameExt().endsWith(JDBCdotXML) && file.canRead()) {
                confs.add(file);
            }
        }

        if (confs.isEmpty()) { // nowhere to search
            return Collections.emptySet();
        }

        Set<Datasource> datasources = new HashSet<Datasource>();

        for (Iterator it = confs.iterator(); it.hasNext();) {
            FileObject dsFO = (FileObject) it.next();
            File dsFile = FileUtil.toFile(dsFO);
            try {
                JdbcDataSource ds = null;
                try {
                    ds = JdbcDataSource.createGraph(dsFile);
                } catch (RuntimeException re) {
                    String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                    LOGGER.log(Level.INFO, msg);
                    continue;
                }

                // FIXME multi datasources
                // FIXME password
                String[] names = getJndiNames(ds);
                if (names != null) {
                    String name = getName(ds);
                    String connectionURl = getConnectionUrl(ds);
                    String userName = getUserName(ds);
                    String driverClass = getDriverClass(ds);
                    for (String jndiName : names) {
                        datasources.add(new WLDatasource(name, connectionURl,
                                jndiName, userName, "", driverClass, dsFO));
                    }
                }
            } catch (IOException ioe) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_CannotReadDatasources", dsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, ioe);
                throw new ConfigurationException(msg, ioe);
            } catch (RuntimeException re) {
                String msg = NbBundle.getMessage(WLDatasourceManager.class, "MSG_NotParseableDatasources", dsFile.getAbsolutePath());
                LOGGER.log(Level.FINE, null, re);
                throw new ConfigurationException(msg, re);
            }
        }

        return datasources;

    }

    public Set<Datasource> getDatasources() throws ConfigurationException {
        FileObject resource = FileUtil.toFileObject(resourceDir);

        return getDatasources(resource);
    }

    public Datasource createDatasource(String jndiName, String  url, String username,
            String password, String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {

        return null;
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
