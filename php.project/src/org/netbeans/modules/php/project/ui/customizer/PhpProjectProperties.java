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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.ui.customizer;

import java.io.File;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik, Radek Matous
 */
public class PhpProjectProperties implements ConfigManager.ConfigProvider {
    public static final int DEFAULT_DEBUG_PROXY_PORT = 9001;

    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; // NOI18N
    public static final String SELENIUM_SRC_DIR = "selenium.src.dir"; // NOI18N
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    public static final String COPY_SRC_FILES = "copy.src.files"; // NOI18N
    public static final String COPY_SRC_TARGET = "copy.src.target"; // NOI18N
    public static final String WEB_ROOT = "web.root"; // NOI18N
    public static final String URL = "url"; // NOI18N
    public static final String INDEX_FILE = "index.file"; // NOI18N
    public static final String INCLUDE_PATH = "include.path"; // NOI18N
    public static final String GLOBAL_INCLUDE_PATH = "php.global.include.path"; // NOI18N
    public static final String ARGS = "script.arguments"; // NOI18N
    public static final String INTERPRETER = "interpreter"; // NOI18N
    public static final String RUN_AS = "run.as"; // NOI18N
    public static final String REMOTE_CONNECTION = "remote.connection"; // NOI18N
    public static final String REMOTE_DIRECTORY = "remote.directory"; // NOI18N
    public static final String REMOTE_UPLOAD = "remote.upload"; // NOI18N
    public static final String REMOTE_PERMISSIONS = "remote.permissions"; // NOI18N
    public static final String REMOTE_UPLOAD_DIRECTLY = "remote.upload.directly"; // NOI18N
    public static final String DEBUG_URL = "debug.url"; // NOI18N
    public static final String DEBUG_PATH_MAPPING_REMOTE = "debug.path.mapping.remote"; // NOI18N
    public static final String DEBUG_PATH_MAPPING_LOCAL = "debug.path.mapping.local"; // NOI18N
    public static final String DEBUG_PROXY_HOST = "debug.proxy.host"; // NOI18N
    public static final String DEBUG_PROXY_PORT = "debug.proxy.port"; // NOI18N
    public static final String SHORT_TAGS = "tags.short"; // NOI18N
    public static final String ASP_TAGS = "tags.asp"; // NOI18N
    public static final String PHP_VERSION = "php.version"; // NOI18N
    public static final String IGNORE_PATH = "ignore.path"; // NOI18N
    public static final String PHP_UNIT_BOOTSTRAP = "phpunit.bootstrap"; // NOI18N
    public static final String PHP_UNIT_CONFIGURATION = "phpunit.configuration"; // NOI18N
    public static final String PHP_UNIT_SUITE = "phpunit.suite"; // NOI18N

    public static final String DEBUG_PATH_MAPPING_SEPARATOR = "||NB||"; // NOI18N

    public static final String[] CFG_PROPS = new String[] {
        URL,
        INDEX_FILE,
        ARGS,
        INTERPRETER,
        RUN_AS,
        REMOTE_CONNECTION,
        REMOTE_DIRECTORY,
        REMOTE_UPLOAD,
        REMOTE_PERMISSIONS,
        REMOTE_UPLOAD_DIRECTLY,
        DEBUG_URL,
        DEBUG_PATH_MAPPING_REMOTE,
        DEBUG_PATH_MAPPING_LOCAL,
        DEBUG_PROXY_HOST,
        DEBUG_PROXY_PORT,
    };

    public static enum RunAsType {
        LOCAL,
        SCRIPT,
        REMOTE
    }

    public static enum UploadFiles {
        MANUALLY ("LBL_UploadFilesManually", "TXT_UploadFilesManually"), // NOI18N
        ON_RUN ("LBL_UploadFilesOnRun", "TXT_UploadFilesOnRun"), // NOI18N
        ON_SAVE ("LBL_UploadFilesOnSave", "TXT_UploadFilesOnSave"); // NOI18N

        private final String label;
        private final String description;

        UploadFiles(String labelKey, String descriptionKey) {
            label = NbBundle.getMessage(PhpProjectProperties.class, labelKey);
            description = NbBundle.getMessage(PhpProjectProperties.class, descriptionKey);
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
          return description;
        }
    }

    public static enum DebugUrl {
        DEFAULT_URL,
        ASK_FOR_URL,
        DO_NOT_OPEN_BROWSER
    }

    static final String CONFIG_PRIVATE_PROPERTIES_PATH = "nbproject/private/config.properties"; // NOI18N

    private final PhpProject project;
    private final IncludePathSupport includePathSupport;
    private final IgnorePathSupport ignorePathSupport;

    // all these fields don't have to be volatile - this ensures request processor
    // CustomizerSources
    private String srcDir;
    private String copySrcFiles;
    private String copySrcTarget;
    private String webRoot;
    private String url;
    private String indexFile;
    private String encoding;
    private String shortTags;
    private String aspTags;
    private String phpVersion;
    private String phpUnitBootstrap;
    private String phpUnitConfiguration;
    private String phpUnitSuite;

    // CustomizerRun
    Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> runConfigs;
    String activeConfig;

    // CustomizerPhpIncludePath
    private DefaultListModel includePathListModel = null;
    private ListCellRenderer includePathListRenderer = null;

    // CustomizerIgnorePath
    private DefaultListModel ignorePathListModel = null;
    private ListCellRenderer ignorePathListRenderer = null;

    public PhpProjectProperties(PhpProject project, IncludePathSupport includePathSupport, IgnorePathSupport ignorePathSupport) {
        assert project != null;
        assert includePathSupport != null;
        assert ignorePathSupport != null;

        this.project = project;
        this.includePathSupport = includePathSupport;
        this.ignorePathSupport = ignorePathSupport;

        runConfigs = readRunConfigs();
        activeConfig = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty("config"); // NOI18N
    }

    public String[] getConfigProperties() {
        return CFG_PROPS;
    }

    public Map<String, Map<String, String>> getConfigs() {
        return runConfigs;
    }

    public String getActiveConfig() {
        return activeConfig;
    }

    public void setActiveConfig(String configName) {
        activeConfig = configName;
    }

    public String getCopySrcFiles() {
        if (copySrcFiles == null) {
            copySrcFiles = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(COPY_SRC_FILES);
        }
        return copySrcFiles;
    }

    public void setCopySrcFiles(String copySrcFiles) {
        this.copySrcFiles = copySrcFiles;
    }

    public String getCopySrcTarget() {
        if (copySrcTarget == null) {
            copySrcTarget = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(COPY_SRC_TARGET);
        }
        return copySrcTarget;
    }

    public void setCopySrcTarget(String copySrcTarget) {
        this.copySrcTarget = copySrcTarget;
    }

    public void setShortTags(String shortTags) {
        this.shortTags = shortTags;
    }

    public void setAspTags(String aspTags) {
        this.aspTags = aspTags;
    }

    public void setPhpVersion(String phpVersion) {
        this.phpVersion = phpVersion;
    }

    /**
     * @return the webRoot, which is relative path to srcDir.
     */
    public String getWebRoot() {
        if (webRoot == null) {
            webRoot = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(WEB_ROOT);
        }
        return webRoot != null ? webRoot : ""; // NOI18N
    }

    /**
     * @param webRoot the webRoot to set
     */
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public String getEncoding() {
        if (encoding == null) {
            encoding = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(SOURCE_ENCODING);
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSrcDir() {
        if (srcDir == null) {
            srcDir = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(SRC_DIR);
        }
        return srcDir;
    }

    public String getUrl() {
        if (url == null) {
            url = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(URL);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIndexFile() {
        if (indexFile == null) {
            indexFile = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(INDEX_FILE);
        }
        return indexFile;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    public DefaultListModel getIncludePathListModel() {
        if (includePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            includePathListModel = PathUiSupport.createListModel(includePathSupport.itemsIterator(
                    properties.getProperty(INCLUDE_PATH)));
        }
        return includePathListModel;
    }

    public ListCellRenderer getIncludePathListRenderer() {
        if (includePathListRenderer == null) {
            includePathListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return includePathListRenderer;
    }

    public DefaultListModel getIgnorePathListModel() {
        if (ignorePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ignorePathListModel = PathUiSupport.createListModel(ignorePathSupport.itemsIterator(
                    properties.getProperty(IGNORE_PATH)));
        }
        return ignorePathListModel;
    }

    public ListCellRenderer getIgnorePathListRenderer() {
        if (ignorePathListRenderer == null) {
            ignorePathListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return ignorePathListRenderer;
    }

    public String getPhpUnitBootstrap() {
        if (phpUnitBootstrap == null) {
            File bootstrap = ProjectPropertiesSupport.getPhpUnitBootstrap(project);
            if (bootstrap != null) {
                phpUnitBootstrap = bootstrap.getAbsolutePath();
            }
        }
        return phpUnitBootstrap;
    }

    public void setPhpUnitBootstrap(String phpUnitBootstrap) {
        this.phpUnitBootstrap = phpUnitBootstrap;
    }

    public String getPhpUnitConfiguration() {
        if (phpUnitConfiguration == null) {
            File configuration = ProjectPropertiesSupport.getPhpUnitConfiguration(project);
            if (configuration != null) {
                phpUnitConfiguration = configuration.getAbsolutePath();
            }
        }
        return phpUnitConfiguration;
    }

    public void setPhpUnitConfiguration(String phpUnitConfiguration) {
        this.phpUnitConfiguration = phpUnitConfiguration;
    }

    public String getPhpUnitSuite() {
        if (phpUnitSuite == null) {
            File suite = ProjectPropertiesSupport.getPhpUnitSuite(project);
            if (suite != null) {
                phpUnitSuite = suite.getAbsolutePath();
            }
        }
        return phpUnitSuite;
    }

    public void setPhpUnitSuite(String phpUnitSuite) {
        this.phpUnitSuite = phpUnitSuite;
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    saveProperties();
                    return null;
                }
            });
            ProjectManager.getDefault().saveProject(project);
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void saveProperties() throws IOException {
        AntProjectHelper helper = project.getHelper();

        // encode include path
        String[] includePath = null;
        if (includePathListModel != null) {
            includePath = includePathSupport.encodeToStrings(PathUiSupport.getIterator(includePathListModel));
        }

        // encode ignore path
        String[] ignorePath = null;
        if (ignorePathListModel != null) {
            ignorePath = ignorePathSupport.encodeToStrings(PathUiSupport.getIterator(ignorePathListModel));
        }

        // get properties
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        // sources
        if (copySrcFiles != null) {
            privateProperties.setProperty(COPY_SRC_FILES, copySrcFiles);
        }
        if (copySrcTarget != null) {
            privateProperties.setProperty(COPY_SRC_TARGET, copySrcTarget);
        }
        if (encoding != null) {
            projectProperties.setProperty(SOURCE_ENCODING, encoding);
        }
        if (webRoot != null) {
            projectProperties.setProperty(WEB_ROOT, webRoot);
        }
        if (phpVersion != null) {
            projectProperties.setProperty(PHP_VERSION, phpVersion);
        }
        if (shortTags != null) {
            projectProperties.setProperty(SHORT_TAGS, shortTags);
        }
        if (aspTags != null) {
            projectProperties.setProperty(ASP_TAGS, aspTags);
        }

        // php include path
        if (includePath != null) {
            projectProperties.setProperty(INCLUDE_PATH, includePath);
        }

        // ignore path
        if (ignorePath != null) {
            projectProperties.setProperty(IGNORE_PATH, ignorePath);
        }

        // phpunit
        if (phpUnitBootstrap != null) {
            projectProperties.setProperty(PHP_UNIT_BOOTSTRAP, relativizeFile(phpUnitBootstrap));
        }
        if (phpUnitConfiguration != null) {
            projectProperties.setProperty(PHP_UNIT_CONFIGURATION, relativizeFile(phpUnitConfiguration));
        }
        if (phpUnitSuite != null) {
            projectProperties.setProperty(PHP_UNIT_SUITE, relativizeFile(phpUnitSuite));
        }

        // configs
        storeRunConfigs(runConfigs, projectProperties, privateProperties);
        EditableProperties ep = helper.getProperties(CONFIG_PRIVATE_PROPERTIES_PATH);
        if (activeConfig == null) {
            ep.remove("config"); // NOI18N
        } else {
            ep.setProperty("config", activeConfig); // NOI18N
        }

        // store all the properties
        helper.putProperties(CONFIG_PRIVATE_PROPERTIES_PATH, ep);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        // additional changes
        // encoding
        if (encoding != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(encoding));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }

        // reset timestamp of the last upload
        ProjectSettings.resetLastUpload(project);

        // UI log
        logUsage(helper.getProjectDirectory(), ProjectPropertiesSupport.getSourcesDirectory(project),
                getActiveRunAsType(), getNumOfRunConfigs(), Boolean.valueOf(getCopySrcFiles()));
    }

    private String relativizeFile(String filePath) {
        if (StringUtils.hasText(filePath)) {
            File file = new File(filePath);
            String path = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
            if (path == null) {
                // sorry, cannot be relativized
                path = file.getAbsolutePath();
            }
            return path;
        }
        return ""; // NOI18N
    }

    private String getActiveRunAsType() {
        if (activeConfig == null) {
            return ""; // NOI18N
        }
        Map<String, String> c = runConfigs.get(activeConfig);
        return c.get(RUN_AS);
    }

    private int getNumOfRunConfigs() {
        int n = 0;
        // removed configs may be null, do not count them
        for (Map.Entry<String, Map<String, String>> entry : runConfigs.entrySet()) {
            Map<String, String> c = entry.getValue();
            if (c == null) {
                // removed config
                continue;
            }
            n++;
        }
        return n;
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private void logUsage(FileObject projectDir, FileObject sourceDir, String activeRunAsType, int numOfConfigs, boolean copyFiles) {
        StringBuilder sb = new StringBuilder(200);
        LogRecord logRecord = new LogRecord(Level.INFO, "USG_PROJECT_CONFIG_PHP"); // NOI18N
        logRecord.setLoggerName(PhpProject.USG_LOGGER_NAME);
        logRecord.setResourceBundle(NbBundle.getBundle(PhpProjectProperties.class));
        logRecord.setResourceBundleName(PhpProjectProperties.class.getPackage().getName() + ".Bundle"); // NOI18N
        logRecord.setParameters(new Object[] {
            FileUtil.isParentOf(projectDir, sourceDir) ? "EXTRA_SRC_DIR_NO" : "EXTRA_SRC_DIR_YES", // NOI18N
            activeRunAsType,
            Integer.toString(numOfConfigs),
            copyFiles ? "COPY_FILES_YES" : "COPY_FILES_NO" // NOI18N
        });
        Logger.getLogger(PhpProject.USG_LOGGER_NAME).log(logRecord);
    }

    public PhpProject getProject() {
        return project;
    }

    /**
     * A mess.
     */
    Map<String/*|null*/, Map<String, String>> readRunConfigs() {
        Map<String, Map<String, String>> m = ConfigManager.createEmptyConfigs();
        Map<String, String> def = new TreeMap<String, String>();
        EditableProperties privateProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties projectProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (String prop : CFG_PROPS) {
            String v = privateProperties.getProperty(prop);
            if (v == null) {
                v = projectProperties.getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                m.put(kid.getName(), new TreeMap<String, String>(getProject().getHelper().getProperties(path)));
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                Map<String, String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                c.putAll(new HashMap<String, String>(getProject().getHelper().getProperties(path)));
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * A royal mess.
     */
    void storeRunConfigs(Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        //System.err.println("storeRunConfigs: " + configs);
        Map<String, String> def = configs.get(null);
        for (String prop : CFG_PROPS) {
            String v = def.get(prop);
            EditableProperties ep = isPrivateProperty(prop) ? privateProperties : projectProperties;
            if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                if (v != null && v.length() > 0) {
                    ep.setProperty(prop, v);
                } else {
                    ep.remove(prop);
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }

            String sharedPath = "nbproject/configs/" + config + ".properties"; // NOI18N
            String privatePath = "nbproject/private/configs/" + config + ".properties"; // NOI18N

            Map<String, String> c = entry.getValue();
            if (c == null) {
                getProject().getHelper().putProperties(sharedPath, null);
                getProject().getHelper().putProperties(privatePath, null);
                continue;
            }
            for (Map.Entry<String, String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                String path = isPrivateProperty(prop) ? privatePath : sharedPath;
                EditableProperties ep = getProject().getHelper().getProperties(path);
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (def.get(prop) != null && def.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    getProject().getHelper().putProperties(path, ep);
                }
            }
            // Make sure the definition file is always created, even if it is empty.
            getProject().getHelper().putProperties(sharedPath, getProject().getHelper().getProperties(sharedPath));
        }
    }

    private boolean isPrivateProperty(String property) {
        // #145477 - all the config properties are stored in private properties because we don't want them to be versioned
        return true;
    }
}
