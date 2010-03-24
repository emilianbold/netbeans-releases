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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

    public static Set<WLDatasource> getDatasources(FileObject inputFile) throws ConfigurationException {
        if (inputFile == null || !inputFile.isValid() || !inputFile.canRead()) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(WLDatasourceManager.class, "ERR_WRONG_CONFIG_DIR"));
            return Collections.emptySet();
        }
        if (inputFile.isData() && inputFile.hasExt("xml")) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                JdbcSystemResourceHandler handler = new JdbcSystemResourceHandler();
                parser.parse(new BufferedInputStream(inputFile.getInputStream()), handler);

                File folder = FileUtil.toFile(inputFile.getParent());
                List<File> confs = new ArrayList<File>();
                Set<String> nameOnly = new HashSet<String>();

                // load by path in config.xml
                for (JdbcSystemResource resource : handler.getResources()) {
                    // FIXME check target
                    if (resource.getFile() != null) {
                        File config = new File(resource.getFile());
                        if (!config.isAbsolute()) {
                            config = new File(folder, resource.getFile());
                        }
                        if (config.exists() && config.isFile() && config.canRead()) {
                            confs.add(config);
                        }
                    } else if (resource.getName() != null) {
                        nameOnly.add(resource.getName());
                    }
                }

                Set<WLDatasource> result = new HashSet<WLDatasource>();
                result.addAll(getDatasources(confs));

                // load those in config/jdbc by name
                if (!nameOnly.isEmpty()) {
                    Set<WLDatasource> configDatasources = getDatasources(inputFile.getParent().getFileObject("jdbc")); // NOI18N
                    for (WLDatasource ds : configDatasources) {
                        if (nameOnly.contains(ds.getName())) {
                            result.add(ds);
                        }
                    }
                }

                return result;
            } catch (IOException ex) {
                return Collections.emptySet();
            } catch (ParserConfigurationException ex) {
                return Collections.emptySet();
            } catch (SAXException ex) {
                return Collections.emptySet();
            }
        } else if (inputFile.isFolder()) {
            File file = FileUtil.toFile(inputFile);
            List<File> confs = new ArrayList<File>();
            for (File child : file.listFiles()) {
                if (!file.isDirectory() && file.canRead() && file.getName().endsWith(JDBCdotXML)) {
                    confs.add(child);
                }
            }

            if (confs.isEmpty()) { // nowhere to search
                return Collections.emptySet();
            }

            return getDatasources(confs);
        }
        return Collections.emptySet();
    }

    public static Set<WLDatasource> getDatasources(Collection<File> confs) throws ConfigurationException {
        Set<WLDatasource> datasources = new HashSet<WLDatasource>();

        for (Iterator it = confs.iterator(); it.hasNext();) {
            File dsFile = (File) it.next();
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
                                jndiName, userName, "", driverClass, dsFile));
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

    public Set<WLDatasource> getDatasources() throws ConfigurationException {
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

    private static class JdbcSystemResourceHandler extends DefaultHandler {

        private final List<JdbcSystemResource> resources = new ArrayList<JdbcSystemResource>();

        private JdbcSystemResource resource;

        private String value;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            value = null;
            if ("jdbc-system-resource".equals(qName)) { // NOI18N
                resource = new JdbcSystemResource();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (resource == null) {
                return;
            }

            if ("jdbc-system-resource".equals(qName)) { // NOI18N
                resources.add(resource);
                resource = null;
            } else if("name".equals(qName)) { // NOI18N
                resource.setName(value);
            } else if ("taget".equals(qName)) { // NOI18N
                resource.setTarget(value);
            } else if ("descriptor-file-name".equals(qName)) { // NOI18N
                resource.setFile(value);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            value = new String(ch, start, length);
        }

        public List<JdbcSystemResource> getResources() {
            return resources;
        }
        
    }

    private static class JdbcSystemResource {

        private String name;

        private String target;

        private String file;

        public JdbcSystemResource() {
            super();
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
        
    }
}
