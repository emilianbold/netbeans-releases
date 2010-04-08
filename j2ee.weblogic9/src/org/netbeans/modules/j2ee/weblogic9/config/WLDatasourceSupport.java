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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSource;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDataSourceParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcDriverParamsType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertiesType;
import org.netbeans.modules.j2ee.weblogic9.config.gen.JdbcPropertyType;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Hejl
 */
public class WLDatasourceSupport {

    private static final String JDBC_FILE = "-jdbc.xml"; // NOI18N

    private static final String NAME_PATTERN = "datasource-"; // NOI18N

    private static final FileFilter JDBC_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return !pathname.isDirectory() && pathname.getName().endsWith(JDBC_FILE);
        }
    };

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
            List<File> confs = new ArrayList<File>(Arrays.asList(file.listFiles(JDBC_FILE_FILTER)));

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

    public Datasource createDatasource(final String jndiName, final String  url, final String username,
            final String password, final String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {

        WLDatasource ds = modifyDatasource(new DatasourceModifier() {

            @Override
            public JdbcDataSource modify(Set<JdbcDataSource> datasources) throws DatasourceAlreadyExistsException {
                for (JdbcDataSource ds : datasources) {
                    String[] names = getJndiNames(ds);
                    if (names != null) {
                        for (String name : names) {
                            if (name.equals(jndiName)) {
                                WLDatasource existing = new WLDatasource(
                                        getName(ds), getConnectionUrl(ds), name,
                                        getUserName(ds), "", getDriverClass(ds), null);
                                throw new DatasourceAlreadyExistsException(existing);
                            }
                        }
                    }
                }

                // create the datasource
                ensureResourceDirExists();

                File candidate;
                int counter = 1;
                do {
                    candidate = new File(resourceDir, NAME_PATTERN
                            + counter + JDBC_FILE);
                    counter++;
                } while (candidate.exists());

                JdbcDataSource ds = new JdbcDataSource();
                setName(ds, jndiName);
                setConnectionUrl(ds, url);
                addJndiName(ds, jndiName);
                setUserName(ds, username);
                // FIXME password
                setDriverClass(ds, driver);

                try {
                    writeFile(candidate, ds);
                } catch (ConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return ds;
            }
        });

        return ds;
    }

    private WLDatasource modifyDatasource(DatasourceModifier modifier)
            throws ConfigurationException, DatasourceAlreadyExistsException {

        try {
            ensureResourceDirExists();

            FileObject resourceDirObject = FileUtil.toFileObject(resourceDir);
            assert resourceDirObject != null;

            Map<JdbcDataSource, DataObject> datasources = new LinkedHashMap<JdbcDataSource, DataObject>();
            for (FileObject dsFileObject : resourceDirObject.getChildren()) {
                if (dsFileObject.isData() && dsFileObject.getNameExt().endsWith(JDBC_FILE)) {

                    DataObject datasourceDO = DataObject.find(dsFileObject);

                    EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                    StyledDocument doc = editor.getDocument();
                    if (doc == null) {
                        doc = editor.openDocument();
                    }

                    JdbcDataSource source = null;
                    try {  // get the up-to-date model
                        // try to create a graph from the editor content
                        byte[] docString = doc.getText(0, doc.getLength()).getBytes();
                        source = JdbcDataSource.createGraph(new ByteArrayInputStream(docString));
                    } catch (RuntimeException e) {
                        InputStream is = new BufferedInputStream(dsFileObject.getInputStream());
                        try {
                            source = JdbcDataSource.createGraph(is);
                        } finally {
                            is.close();
                        }
                        if (source == null) {
                            // neither the old graph is parseable, there is not much we can do here
                            // we could skip it but we can't be sure whether there are duplicate
                            // entries
                            // TODO: should we notify the user?
                            throw new ConfigurationException(
                                    NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlCannotParse", dsFileObject.getNameExt()));
                        }
                        // current editor content is not parseable, ask whether to override or not
                        NotifyDescriptor notDesc = new NotifyDescriptor.Confirmation(
                                NbBundle.getMessage(WLDatasourceSupport.class, "MSG_datasourcesXmlNotValid", dsFileObject.getNameExt()),
                                NotifyDescriptor.YES_NO_OPTION);
                        Object result = DialogDisplayer.getDefault().notify(notDesc);
                        if (result == NotifyDescriptor.NO_OPTION) {
                            // keep the old content
                            return null;
                        }
                        datasources.put(source, datasourceDO);
                    }
                }
            }

            JdbcDataSource modifiedSource = modifier.modify(datasources.keySet());

            // TODO for now this code won't be called probably as there is no
            // real modify in our code just create
            DataObject datasourceDO = datasources.get(modifiedSource);
            if (datasourceDO != null) {
                boolean modified = datasourceDO.isModified();
                EditorCookie editor = (EditorCookie) datasourceDO.getCookie(EditorCookie.class);
                StyledDocument doc = editor.getDocument();
                if (doc == null) {
                    doc = editor.openDocument();
                }
                replaceDocument(doc, modifiedSource);

                if (!modified) {
                    SaveCookie cookie = (SaveCookie) datasourceDO.getCookie(SaveCookie.class);
                    cookie.save();
                }
            }

            // FIXME multi datasources
            // FIXME password
            // FIXME null file
            String[] names = getJndiNames(modifiedSource);
            if (names != null && names.length > 0) {
                String name = getName(modifiedSource);
                String connectionURl = getConnectionUrl(modifiedSource);
                String userName = getUserName(modifiedSource);
                String driverClass = getDriverClass(modifiedSource);
                return new WLDatasource(name, connectionURl,
                            names[0], userName, "", driverClass, null);
            }
        } catch(DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (BadLocationException ble) {
            // this should not occur, just log it if it happens
            Exceptions.printStackTrace(ble);
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WLDatasourceSupport.class, "MSG_CannotUpdateFile");
            throw new ConfigurationException(msg, ioe);
        }

        return null;
    }

    private void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        assert file != null : "File to write can't be null"; // NOI18N
        assert file.getParentFile() != null : "File parent folder can't be null"; // NOI18N

        try {
            FileObject cfolder = FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile()));
            if (cfolder == null) {
                try {
                    cfolder = FileUtil.createFolder(FileUtil.normalizeFile(file.getParentFile()));
                } catch (IOException ex) {
                    throw new ConfigurationException(NbBundle.getMessage(WLDatasourceSupport.class,
                            "MSG_FailedToCreateConfigFolder", file.getParentFile().getAbsolutePath()));
                }
            }

            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch(IOException ioe) {
                                LOGGER.log(Level.FINE, null, ioe);
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
            });
            
            FileUtil.refreshFor(file);
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }

    private void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    private void ensureResourceDirExists() {
        if (!resourceDir.exists()) {
            resourceDir.mkdir();
            FileUtil.refreshFor(resourceDir);
        }
    }

    private static String getName(JdbcDataSource ds) {
        return ds.getName();
    }

    private static void setName(JdbcDataSource ds, String name) {
        ds.setName(name);
    }

    private static String[] getJndiNames(JdbcDataSource ds) {
        JdbcDataSourceParamsType params = ds.getJdbcDataSourceParams();
        if (params != null) {
            return params.getJndiName();
        }
        return null;
    }

    private static void addJndiName(JdbcDataSource ds, String name) {
        JdbcDataSourceParamsType params = ds.getJdbcDataSourceParams();
        if (params == null) {
            params = new JdbcDataSourceParamsType();
            ds.setJdbcDataSourceParams(params);
        }
        
        String[] oldNames = params.getJndiName();
        if (oldNames != null) {
            String[] newNames = new String[oldNames.length + 1];
            System.arraycopy(oldNames, 0, newNames, 0, oldNames.length);
            newNames[newNames.length - 1] = name;
            params.setJndiName(newNames);
        } else {
            params.setJndiName(new String[] {name});
        }
    }

    private static String getConnectionUrl(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getUrl();
        }
        return null;
    }

    private static void setConnectionUrl(JdbcDataSource ds, String url) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }
        params.setUrl(url);
    }

    private static String getDriverClass(JdbcDataSource ds) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params != null) {
            return params.getDriverName();
        }
        return null;
    }

    private static void setDriverClass(JdbcDataSource ds, String driver) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }
        params.setDriverName(driver);
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

    private static void setUserName(JdbcDataSource ds, String username) {
        JdbcDriverParamsType params = ds.getJdbcDriverParams();
        if (params == null) {
            params = new JdbcDriverParamsType();
            ds.setJdbcDriverParams(params);
        }

        JdbcPropertiesType props = params.getProperties();
        if (props == null) {
            props = new JdbcPropertiesType();
            params.setProperties(props);
        }

        for (JdbcPropertyType item : props.getProperty2()) {
            if ("user".equals(item.getName())) { // NOI18N
                item.setValue(username);
                return;
            }
        }

        JdbcPropertyType item = new JdbcPropertyType();
        item.setName("user"); // NOI18N
        item.setValue(username);
        props.addProperty2(item);
    }

    private interface DatasourceModifier {

        @NonNull
        JdbcDataSource modify(Set<JdbcDataSource> datasources) throws DatasourceAlreadyExistsException;

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
