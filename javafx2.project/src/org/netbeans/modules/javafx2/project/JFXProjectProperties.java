/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.ui.CustomizerJarComponent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public final class JFXProjectProperties {

    private static final Logger LOG = Logger.getLogger(JFXProjectProperties.class.getName());
    
    public static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N
    public static final String JAVAFX_PRELOADER = "javafx.preloader"; // NOI18N
    public static final String JAVAFX_SWING = "javafx.swing"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE = "javafx.disable.autoupdate"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION = "javafx.disable.autoupdate.notification"; // NOI18N
    public static final String JAVAFX_ENDORSED_ANT_CLASSPATH = "endorsed.javafx.ant.classpath"; // NOI18N
    
    /** The standard extension for FXML source files. */
    public static final String FXML_EXTENSION = "fxml"; // NOI18N    
    
    // copies of private J2SE properties
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    public static final String JAVADOC_PRIVATE = "javadoc.private"; // NOI18N
    public static final String JAVADOC_NO_TREE = "javadoc.notree"; // NOI18N
    public static final String JAVADOC_USE = "javadoc.use"; // NOI18N
    public static final String JAVADOC_NO_NAVBAR = "javadoc.nonavbar"; // NOI18N
    public static final String JAVADOC_NO_INDEX = "javadoc.noindex"; // NOI18N
    public static final String JAVADOC_SPLIT_INDEX = "javadoc.splitindex"; // NOI18N
    public static final String JAVADOC_AUTHOR = "javadoc.author"; // NOI18N
    public static final String JAVADOC_VERSION = "javadoc.version"; // NOI18N
    public static final String JAVADOC_WINDOW_TITLE = "javadoc.windowtitle"; // NOI18N
    public static final String JAVADOC_ENCODING = "javadoc.encoding"; // NOI18N
    public static final String JAVADOC_ADDITIONALPARAM = "javadoc.additionalparam"; // NOI18N
    public static final String BUILD_SCRIPT = "buildfile"; //NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N

    // Packaging properties
    public static final String JAVAFX_BINARY_ENCODE_CSS = "javafx.binarycss"; // NOI18N
    public static final String JAVAFX_DEPLOY_INCLUDEDT = "javafx.deploy.includeDT"; // NOI18N
    public static final String JAVAFX_DEPLOY_EMBEDJNLP = "javafx.deploy.embedJNLP"; // NOI18N
    public static final String JAVAFX_REBASE_LIBS = "javafx.rebase.libs"; // NOI18N
    
    // FX config properties (Run panel), replicated from ProjectProperties
    public static final String MAIN_CLASS = "javafx.main.class"; // NOI18N
    public static final String APPLICATION_ARGS = ProjectProperties.APPLICATION_ARGS;
    public static final String APP_PARAM_PREFIX = "javafx.param."; // NOI18N
    public static final String APP_PARAM_SUFFIXES[] = new String[] { "name", "value" }; // NOI18N
    public static final String RUN_JVM_ARGS = ProjectProperties.RUN_JVM_ARGS;
    public static final String FALLBACK_CLASS = "javafx.fallback.class"; // NOI18N
    public static final String SIGNED_JAR = "dist.signed.jar"; // NOI18N
    
    public static final String PRELOADER_ENABLED = "javafx.preloader.enabled"; // NOI18N
    public static final String PRELOADER_TYPE = "javafx.preloader.type"; // NOI18N
    public static final String PRELOADER_PROJECT = "javafx.preloader.project.path"; // NOI18N
    public static final String PRELOADER_CLASS = "javafx.preloader.class"; // NOI18N
    public static final String PRELOADER_JAR_FILENAME = "javafx.preloader.jar.filename"; // NOI18N
    public static final String PRELOADER_JAR_PATH = "javafx.preloader.jar.path"; // NOI18N
    
    public static final String RUN_WORK_DIR = ProjectProperties.RUN_WORK_DIR; // NOI18N
    public static final String RUN_APP_WIDTH = "javafx.run.width"; // NOI18N
    public static final String RUN_APP_HEIGHT = "javafx.run.height"; // NOI18N
    public static final String RUN_IN_HTMLTEMPLATE = "javafx.run.htmltemplate"; // NOI18N
    public static final String RUN_IN_HTMLTEMPLATE_PROCESSED = "javafx.run.htmltemplate.processed"; // NOI18N
    public static final String RUN_IN_BROWSER = "javafx.run.inbrowser"; // NOI18N
    public static final String RUN_IN_BROWSER_PATH = "javafx.run.inbrowser.path"; // NOI18N
    public static final String RUN_IN_BROWSER_ARGUMENTS = "javafx.run.inbrowser.arguments"; // NOI18N
    public static final String RUN_IN_BROWSER_UNDEFINED = "undefined"; // NOI18N
    public static final String RUN_AS = "javafx.run.as"; // NOI18N

    public static final String DEFAULT_APP_WIDTH = "800"; // NOI18N
    public static final String DEFAULT_APP_HEIGHT = "600"; // NOI18N

    // Deployment properties
    public static final String UPDATE_MODE_BACKGROUND = "javafx.deploy.backgroundupdate"; // NOI18N
    public static final String ALLOW_OFFLINE = "javafx.deploy.allowoffline"; // NOI18N
    public static final String INSTALL_PERMANENTLY = "javafx.deploy.installpermanently"; // NOI18N
    public static final String ADD_DESKTOP_SHORTCUT = "javafx.deploy.adddesktopshortcut"; // NOI18N
    public static final String ADD_STARTMENU_SHORTCUT = "javafx.deploy.addstartmenushortcut"; // NOI18N
    public static final String ICON_FILE = "javafx.deploy.icon"; // NOI18N
    public static final String PERMISSIONS_ELEVATED = "javafx.deploy.permissionselevated"; // NOI18N

    // Deployment - signing
    public static final String JAVAFX_SIGNING_ENABLED = "javafx.signing.enabled"; //NOI18N
    public static final String JAVAFX_SIGNING_TYPE = "javafx.signing.type"; //NOI18N
    public static final String JAVAFX_SIGNING_KEYSTORE = "javafx.signing.keystore"; //NOI18N
    public static final String JAVAFX_SIGNING_KEYSTORE_PASSWORD = "javafx.signing.keystore.password"; //NOI18N
    public static final String JAVAFX_SIGNING_KEY = "javafx.signing.keyalias"; //NOI18N
    public static final String JAVAFX_SIGNING_KEY_PASSWORD = "javafx.signing.keyalias.password"; //NOI18N
    
    public static final String RUN_CP = "run.classpath";    //NOI18N
    public static final String BUILD_CLASSES = "build.classes.dir"; //NOI18N
    
    public static final String DOWNLOAD_MODE_LAZY_JARS = "download.mode.lazy.jars";   //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_JAR = "download.mode.lazy.jar."; //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_FORMAT = DOWNLOAD_MODE_LAZY_JAR +"%s"; //NOI18N
    
    // Deployment - callbacks
    public static final String JAVASCRIPT_CALLBACK_PREFIX = "javafx.jscallback."; // NOI18N

    // folders and files
    public static final String PROJECT_CONFIGS_DIR = "nbproject/configs"; // NOI18N
    public static final String PROJECT_PRIVATE_CONFIGS_DIR = "nbproject/private/configs"; // NOI18N
    public static final String PROPERTIES_FILE_EXT = "properties"; // NOI18N
    // the following should be J2SEConfigurationProvider.CONFIG_PROPS_PATH which is now inaccessible from here
    public static final String CONFIG_PROPERTIES_FILE = "nbproject/private/config.properties"; // NOI18N    
    public static final String DEFAULT_CONFIG = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.default.label"); // NOI18N
    public static final String DEFAULT_CONFIG_STANDALONE = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.standalone.label"); // NOI18N
    public static final String DEFAULT_CONFIG_WEBSTART = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.webstart.label"); // NOI18N
    public static final String DEFAULT_CONFIG_BROWSER = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.browser.label"); // NOI18N

    private StoreGroup fxPropGroup = new StoreGroup();
    
    // Packaging
    JToggleButton.ToggleButtonModel binaryEncodeCSS;
    public JToggleButton.ToggleButtonModel getBinaryEncodeCSSModel() {
        return binaryEncodeCSS;
    }

    private CustomizerJarComponent jarComponent = null;
    public CustomizerJarComponent getCustomizerJarComponent() {
        if(jarComponent == null) {
            jarComponent = new CustomizerJarComponent(this);
        }
        return jarComponent;
    }

    // CustomizerRun
    private JFXConfigs CONFIGS = null;
    public JFXConfigs getConfigs() {
        return CONFIGS;
    }

    private Map<String,String> browserPaths = null;

    public Map<String, String> getBrowserPaths() {
        return browserPaths;
    }
    public void resetBrowserPaths() {
        this.browserPaths = new HashMap<String, String>();
    }
    public void setBrowserPaths(Map<String, String> browserPaths) {
        this.browserPaths = browserPaths;
    }

    // CustomizerRun - Preloader source type
    public enum PreloaderSourceType {
        NONE("none"), // NOI18N
        PROJECT("project"), // NOI18N
        JAR("jar"); // NOI18N
        private final String propertyValue;
        PreloaderSourceType(String propertyValue) {
            this.propertyValue = propertyValue;
        }
        public String getString() {
            return propertyValue;
        }
    }
    
    PreloaderClassComboBoxModel preloaderClassModel;
    public PreloaderClassComboBoxModel getPreloaderClassModel() {
        return preloaderClassModel;
    }

    // CustomizerRun - Run type
    public enum RunAsType {
        STANDALONE("standalone", DEFAULT_CONFIG_STANDALONE), // NOI18N
        ASWEBSTART("webstart", DEFAULT_CONFIG_WEBSTART), // NOI18N
        INBROWSER("embedded", DEFAULT_CONFIG_BROWSER); // NOI18N
        private final String propertyValue;
        private final String defaultConfig;
        RunAsType(String propertyValue, String defaultConfig) {
            this.propertyValue = propertyValue;
            this.defaultConfig = defaultConfig;
        }
        public String getString() {
            return propertyValue;
        }
        public String getDefaultConfig() {
            return defaultConfig;
        }
    }
    JToggleButton.ToggleButtonModel runStandalone;
    JToggleButton.ToggleButtonModel runAsWebStart;
    JToggleButton.ToggleButtonModel runInBrowser;
    
    // Deployment
    JToggleButton.ToggleButtonModel allowOfflineModel;
    public JToggleButton.ToggleButtonModel getAllowOfflineModel() {
        return allowOfflineModel;
    }
    JToggleButton.ToggleButtonModel backgroundUpdateCheck;
    public JToggleButton.ToggleButtonModel getBackgroundUpdateCheckModel() {
        return backgroundUpdateCheck;
    }
    JToggleButton.ToggleButtonModel installPermanently;
    public JToggleButton.ToggleButtonModel getInstallPermanentlyModel() {
        return installPermanently;
    }
    JToggleButton.ToggleButtonModel addDesktopShortcut;
    public JToggleButton.ToggleButtonModel getAddDesktopShortcutModel() {
        return addDesktopShortcut;
    }
    JToggleButton.ToggleButtonModel addStartMenuShortcut;
    public JToggleButton.ToggleButtonModel getAddStartMenuShortcutModel() {
        return addStartMenuShortcut;
    }
    Document iconDocument;
    public Document getIconDocumentModel() {
        return iconDocument;
    }

    // Deployment - Signing
    public enum SigningType {
        NOSIGN("notsigned"), // NOI18N
        SELF("self"), // NOI18N
        KEY("key"); // NOI18N
        private final String propertyValue;
        SigningType(String propertyValue) {
            this.propertyValue = propertyValue;
        }
        public String getString() {
            return propertyValue;
        }
    }

    boolean signingEnabled;
    SigningType signingType;
    String signingKeyStore;
    String signingKeyAlias;
    boolean permissionsElevated;
    char [] signingKeyStorePassword;
    char [] signingKeyPassword;
    public boolean getSigningEnabled() {
        return signingEnabled;
    }
    public void setSigningEnabled(boolean enabled) {
        this.signingEnabled = enabled;
    }
    public boolean getPermissionsElevated() {
        return permissionsElevated;
    }
    public void setPermissionsElevated(boolean enabled) {
        this.permissionsElevated = enabled;
    }
    public SigningType getSigningType() {
        return signingType;
    }
    public void setSigningType(SigningType type) {
        this.signingType = type;
    }
    public String getSigningKeyStore() {
        return signingKeyStore;
    }
    public String getSigningKeyAlias() {
        return signingKeyAlias;
    }
    public char[] getSigningKeyStorePassword() {
        return signingKeyStorePassword;
    }
    public char[] getSigningKeyPassword() {
        return signingKeyPassword;
    }
    public void setSigningKeyAlias(String signingKeyAlias) {
        this.signingKeyAlias = signingKeyAlias;
    }
    public void setSigningKeyPassword(char[] signingKeyPassword) {
        this.signingKeyPassword = signingKeyPassword;
    }
    public void setSigningKeyStore(String signingKeyStore) {
        this.signingKeyStore = signingKeyStore;
    }
    public void setSigningKeyStorePassword(char[] signingKeyStorePassword) {
        this.signingKeyStorePassword = signingKeyStorePassword;
    }
    
    // Deployment - Libraries Download Mode
    List<? extends File> runtimeCP;
    List<? extends File> lazyJars;
    boolean lazyJarsChanged;
    public List<? extends File> getRuntimeCP() {
        return runtimeCP;
    }
    public List<? extends File> getLazyJars() {
        return lazyJars;
    }
    public void setLazyJars(List<? extends File> newLazyJars) {
        this.lazyJars = newLazyJars;
    }
    public boolean getLazyJarsChanged() {
        return lazyJarsChanged;
    }
    public void setLazyJarsChanged(boolean changed) {
        this.lazyJarsChanged = changed;
    }
    
    // Deployment - JavaScript Callbacks
    Map<String,String> jsCallbacks;
    boolean jsCallbacksChanged;
    public Map<String,String> getJSCallbacks() {
        return jsCallbacks;
    }
    public void setJSCallbacks(Map<String,String> newCallbacks) {
        jsCallbacks = newCallbacks;
    }
    public boolean getJSCallbacksChanged() {
        return jsCallbacksChanged;
    }
    public void setJSCallbacksChanged(boolean changed) {
        jsCallbacksChanged = changed;
    }
        
    // Project related references
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;

    public Project getProject() {
        return project;
    }
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    /** Keeps singleton instance of JFXProjectProperties for any fx project for which property customizer is opened at once */
    private static Map<String, JFXProjectProperties> propInstance = new HashMap<String, JFXProjectProperties>();

    /** Keeps set of category markers used to identify validity of JFXProjectProperties instance */
    private Set<String> instanceMarkers = new TreeSet<String>();
    
    public void markInstance(@NonNull String marker) {
        instanceMarkers.add(marker);
    }
    
    public boolean isInstanceMarked(@NonNull String marker) {
        return instanceMarkers.contains(marker);
    }
    
    /** Factory method */
    public static JFXProjectProperties getInstance(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop == null) {
            prop = new JFXProjectProperties(context);
            propInstance.put(projDir, prop);
        }
        return prop;
    }

    /** Factory method 
     * This is to prevent reuse of the same instance after the properties dialog
     * has been cancelled. Called by each FX category provider at the time
     * when properties dialog is opened, it checks/stores category-specific marker strings. 
     * Previous existence of marker string indicates that properties dialog had been opened
     * before and ended by Cancel, otherwise this instance would not exist (OK would
     * cause properties to be saved and the instance deleted by a call to JFXProjectProperties.cleanup()).
     * (Note that this is a workaround to avoid adding listener to properties dialog close event.)
     * 
     * @param category marker string to indicate which category provider is calling this
     * @return instance of JFXProjectProperties shared among category panels in the current Project Properties dialog only
     */
    public static JFXProjectProperties getInstancePerSession(Lookup context, String category) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            if(prop.isInstanceMarked(category)) {
                // category marked before - create new instance to avoid reuse after Cancel
                prop = null;
            } else {
                prop.markInstance(category);
            }
        }
        if(prop == null) {
            prop = new JFXProjectProperties(context);
            propInstance.put(projDir, prop);
            prop.markInstance(category);
        }
        return prop;
    }
    
    /** Getter method */
    public static JFXProjectProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static JFXProjectProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }

    /** Keeps singleton instance of a set of preloader artifact dependencies for any fx project */
    private static Map<String, Set<PreloaderArtifact>> prelArtifacts = new HashMap<String, Set<PreloaderArtifact>>();
    
    /** Factory method */
    private static Set<PreloaderArtifact> getPreloaderArtifacts(@NonNull Project proj) {
        String projDir = proj.getProjectDirectory().getPath();
        Set<PreloaderArtifact> prels = prelArtifacts.get(projDir);
        if(prels == null) {
            prels = new HashSet<PreloaderArtifact>();
            prelArtifacts.put(projDir, prels);
        }
        return prels;
    }
    
    /** Creates a new instance of JFXProjectProperties */
    private JFXProjectProperties(Lookup context) {
        
        //defaultInstance = provider.getJFXProjectProperties();
        project = context.lookup(Project.class);
        
        if (project != null) {
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            evaluator = j2sePropEval.evaluator();
            
            // Packaging
            binaryEncodeCSS = fxPropGroup.createToggleButtonModel(evaluator, JAVAFX_BINARY_ENCODE_CSS); // set true by default in JFXProjectGenerator

            // Deployment
            allowOfflineModel = fxPropGroup.createToggleButtonModel(evaluator, ALLOW_OFFLINE); // set true by default in JFXProjectGenerator            
            backgroundUpdateCheck = fxPropGroup.createToggleButtonModel(evaluator, UPDATE_MODE_BACKGROUND); // set true by default in JFXProjectGenerator
            installPermanently = fxPropGroup.createToggleButtonModel(evaluator, INSTALL_PERMANENTLY);
            addDesktopShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_DESKTOP_SHORTCUT);
            addStartMenuShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_STARTMENU_SHORTCUT);
            iconDocument = fxPropGroup.createStringDocument(evaluator, ICON_FILE);

            // CustomizerRun
            CONFIGS = new JFXConfigs();
            CONFIGS.read();
            initPreloaderArtifacts(project, CONFIGS);
            CONFIGS.setActive(evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
            preloaderClassModel = new PreloaderClassComboBoxModel();
            
            initSigning(evaluator);
            initResources(evaluator, project, CONFIGS);
            initJSCallbacks(evaluator);
        }
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }
            
    public static boolean isEqual(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

    public static boolean isEqualIgnoreCase(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
    }                                   

    public static boolean isEqualText(final String s1, final String s2) {
        return ((s1 == null || s1.isEmpty()) && (s2 == null || s2.isEmpty())) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   
    
    public static String getSharedConfigFilePath(final @NonNull String config)
    {
        return PROJECT_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    public static String getPrivateConfigFilePath(final @NonNull String config)
    {
        return PROJECT_PRIVATE_CONFIGS_DIR + "/" + config + "." + PROPERTIES_FILE_EXT; // NOI18N
    }

    public static class PropertiesTableModel extends AbstractTableModel {
        
        private List<Map<String,String>> properties;
        private String propSuffixes[];
        private String columnNames[];
        
        public PropertiesTableModel(List<Map<String,String>> props, String sfxs[], String clmns[]) {
            if (sfxs.length != clmns.length) {
                throw new IllegalArgumentException();
            }
            properties = props;
            propSuffixes = sfxs;
            columnNames = clmns;
        }
        
        @Override
        public int getRowCount() {
            return properties.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            properties.get(rowIndex).put(propSuffixes[columnIndex], (String) aValue);
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return properties.get(rowIndex).get(propSuffixes[columnIndex]);
        }
        
        public void addRow() {
            Map<String,String> emptyMap = new HashMap<String,String>();
            for (String  suffix : propSuffixes) {
                emptyMap.put(suffix, "");
            }
            properties.add(emptyMap);
        }
        
        public void removeRow(int index) {
            properties.remove(index);
        }

    }
    
    private FileObject getSrcRoot(@NonNull Project project)
    {
        FileObject srcRoot = null;
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (!isTest(sg.getRootFolder(),project)) {
                srcRoot = sg.getRootFolder();
                break;
            }
        }
        return srcRoot;
    }

    private void initPreloaderArtifacts(@NonNull Project project, @NonNull JFXConfigs configs) {
        Set<PreloaderArtifact> prels = getPreloaderArtifacts(project);
        prels.clear();
        try {
            prels.addAll(getPreloaderArtifactsFromConfigs(configs));
        } catch (IOException ex) {
            // can be ignored
        }
    }
    
    private Set<PreloaderArtifact> getPreloaderArtifactsFromConfigs(@NonNull JFXConfigs configs) throws IOException {       
        Set<PreloaderArtifact> preloaderArtifacts = new HashSet<PreloaderArtifact>();
        // check records on all preloaders from all configurations
        for(String config : configs.getConfigNames()) {
            
            PreloaderArtifact preloader = null;
            if(!isTrue( configs.getProperty(config, PRELOADER_ENABLED))) {
                continue;
            }
            String prelTypeString = configs.getProperty(config, PRELOADER_TYPE);
            
            String prelProjDir = configs.getProperty(config, PRELOADER_PROJECT);
            if (prelProjDir != null && isEqualIgnoreCase(prelTypeString, PreloaderSourceType.PROJECT.getString())) {
                FileObject thisProjDir = project.getProjectDirectory();
                FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, prelProjDir);
                File prelProjDirF = (fo == null) ? null : FileUtil.toFile(fo);                
                if( isTrue(configs.getProperty(config, PRELOADER_ENABLED)) && prelProjDirF != null && prelProjDirF.exists() ) {
                    FileObject srcRoot = getSrcRoot(getProject());
                    if(srcRoot != null) {
                        prelProjDirF = FileUtil.normalizeFile(prelProjDirF);
                        FileObject prelProjFO = FileUtil.toFileObject(prelProjDirF);
                        final Project proj = ProjectManager.getDefault().findProject(prelProjFO);

                        AntArtifact[] artifacts = AntArtifactQuery.findArtifactsByType(proj, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                        List<URI> allURI = new ArrayList<URI>();
                        for(AntArtifact artifact : artifacts) {
                            allURI.addAll(Arrays.asList(artifact.getArtifactLocations()));
                        }
                        if(!allURI.isEmpty()) {
                            URI[] arrayURI = allURI.toArray(new URI[0]);
                            preloader = new PreloaderProjectArtifact(artifacts, arrayURI, srcRoot, ClassPath.COMPILE, prelProjDirF.getAbsolutePath());
                        }
                    }
                }
            }
            if(preloader == null) {
                String prelJar = configs.getProperty(config, PRELOADER_JAR_PATH);
                if(prelJar != null && isEqualIgnoreCase(prelTypeString, PreloaderSourceType.JAR.getString())) {
                    FileObject thisProjDir = project.getProjectDirectory();
                    FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, prelJar);
                    File prelJarF = (fo == null) ? null : FileUtil.toFile(fo);                
                    if( prelJarF != null && prelJarF.exists() ) {
                        FileObject srcRoot = getSrcRoot(getProject());
                        if(srcRoot != null) {
                            URL[] urls = new URL[1];
                            urls[0] = FileUtil.urlForArchiveOrDir(prelJarF);
                            FileObject[] fos = new FileObject[1];
                            fos[0] = FileUtil.toFileObject(prelJarF);
                            preloader = new PreloaderJarArtifact(urls, fos, srcRoot, ClassPath.COMPILE, urls[0].toString());
                        }
                    }
                }
            }
            if(preloader != null) {
                preloaderArtifacts.add(preloader);
            }
        }
        return preloaderArtifacts;
    }

    @Deprecated
    private void updatePreloaderDependencies(@NonNull JFXConfigs configs) throws IOException {
        // depeding on the currently (de)selected preloaders update project dependencies,
        // i.e., remove disabled/deleted preloader project dependencies and add enabled/added preloader project dependencies
        Set<PreloaderArtifact> preloaderArtifacts = getPreloaderArtifacts(getProject());
        for(PreloaderArtifact artifact : preloaderArtifacts) {
            artifact.setValid(false);
        }
        Set<PreloaderArtifact> currentArtifacts = getPreloaderArtifactsFromConfigs(configs);
        for(PreloaderArtifact preloader : currentArtifacts) {
            if(preloader != null) {
                preloader.addDependency();
                boolean updated = false;
                for(PreloaderArtifact a : preloaderArtifacts) {
                    if(a.equals(preloader)) {
                        a.setValid(true);
                        updated = true;
                    }
                }
                if(!updated) {
                    preloader.setValid(true);
                    preloaderArtifacts.add(preloader);
                }
            }
        }
        // remove all previous dependencies that are no more specified in any configuration
        Set<PreloaderArtifact> toRemove = new HashSet<PreloaderArtifact>();
        for(PreloaderArtifact artifact : preloaderArtifacts) {
            if(!artifact.isValid()) {
                artifact.removeDependency();
                toRemove.add(artifact);
            }
        }
        for(PreloaderArtifact artifact : toRemove) {
            preloaderArtifacts.remove(artifact);
        }
    }
    
    private static boolean isTest(final @NonNull FileObject root, final @NonNull Project project) {
        assert root != null;
        assert project != null;
        final ClassPath cp = ClassPath.getClassPath(root, ClassPath.COMPILE);
        for (ClassPath.Entry entry : cp.entries()) {
            final FileObject[] srcRoots = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
            for (FileObject srcRoot : srcRoots) {
                if (project.equals(FileOwnerQuery.getOwner(srcRoot))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void storeRest(@NonNull EditableProperties editableProps, @NonNull EditableProperties privProps) {
        // store signing info
        editableProps.setProperty(JAVAFX_SIGNING_ENABLED, signingEnabled ? "true" : "false"); //NOI18N
        editableProps.setProperty(JAVAFX_SIGNING_TYPE, signingType.getString());
        setOrRemove(editableProps, JAVAFX_SIGNING_KEY, signingKeyAlias);
        setOrRemove(editableProps, JAVAFX_SIGNING_KEYSTORE, signingKeyStore);
        editableProps.setProperty(PERMISSIONS_ELEVATED, permissionsElevated ? "true" : "false"); //NOI18N
        setOrRemove(privProps, JAVAFX_SIGNING_KEYSTORE_PASSWORD, signingKeyStorePassword);
        setOrRemove(privProps, JAVAFX_SIGNING_KEY_PASSWORD, signingKeyPassword);        
        // store resources
        storeResources(editableProps);
        // store JavaScript callbacks
        storeJSCallbacks(editableProps);
        // store JFX SDK & RT path
        storePlatform(editableProps);
    }

    private void setOrRemove(EditableProperties props, String name, char [] value) {
        setOrRemove(props, name, value != null ? new String(value) : null);
    }

    private void setOrRemove(@NonNull EditableProperties props, @NonNull String name, String value) {
        if (value != null) {
            props.setProperty(name, value);
        } else {
            props.remove(name);
        }
    }
        
    public static EditableProperties readFromFile(final @NonNull Project project, final @NonNull String relativePath) throws IOException {
        final FileObject dirFO = project.getProjectDirectory();
        return readFromFile(dirFO, relativePath);
    }

    public static EditableProperties readFromFile(final @NonNull FileObject dirFO, final @NonNull String relativePath) throws IOException {
        assert dirFO.isFolder();
        final FileObject propsFO = dirFO.getFileObject(relativePath);
        return readFromFile(propsFO);
    }

    public static EditableProperties readFromFile(final @NonNull FileObject propsFO) throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        if(propsFO != null) {
            assert propsFO.isData();
            try {
                final InputStream is = propsFO.getInputStream();
                ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        try {
                            ep.load(is);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }
        }
        return ep;
    }

    public static void deleteFile(final @NonNull Project project, final @NonNull String relativePath) throws IOException {
        final FileObject propsFO = project.getProjectDirectory().getFileObject(relativePath);
        deleteFile(propsFO);
    }
    
    public static void deleteFile(final @NonNull FileObject propsFO) throws IOException {
        if(propsFO != null) {
            try {
                final Mutex.ExceptionAction<Void> action = new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        FileLock lock = null;
                        try {
                            lock = propsFO.lock();
                            propsFO.delete(lock);
                        } finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                        return null;
                    }
                };
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        ProjectManager.mutex().readAccess(action);
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }       
        }
    }

    public static void saveToFile(final @NonNull Project project, final @NonNull String relativePath, final @NonNull EditableProperties ep) throws IOException {
        FileObject dirFO = project.getProjectDirectory();
        saveToFile(dirFO, relativePath, ep);
    }
    
    public static void saveToFile(final @NonNull FileObject dirFO, final @NonNull String relativePath, final @NonNull EditableProperties ep) throws IOException {
        assert dirFO.isFolder();
        FileObject f = dirFO.getFileObject(relativePath);
        final FileObject propsFO;
        if(f == null) {
            propsFO = FileUtil.createData(dirFO, relativePath);
            assert propsFO != null : "FU.cD must not return null; called on " + dirFO + " + " + relativePath; // #50802  // NOI18N
        } else {
            propsFO = f;
        }
        saveToFile(propsFO, ep);
    }
    
    public static void saveToFile(final @NonNull FileObject propsFO, final @NonNull EditableProperties ep) throws IOException {
        if(propsFO != null) {
            assert propsFO.isData();
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        OutputStream os = null;
                        FileLock lock = null;
                        try {
                            lock = propsFO.lock();
                            os = propsFO.getOutputStream(lock);
                            ep.store(os);
                        } finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                            if (os != null) {
                                os.close();
                            }
                        }
                        return null;
                    }
                });
            } catch (MutexException mux) {
                throw (IOException) mux.getException();
            }
        }
    }

    public void store() throws IOException {
        
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties pep = new EditableProperties(true);
        final FileObject privPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        try {
            final InputStream is = projPropsFO.getInputStream();
            final InputStream pis = privPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    try {
                        pep.load(pis);
                    } finally {
                        if (pis != null) {
                            pis.close();
                        }
                    }
                    fxPropGroup.store(ep);
                    storeRest(ep, pep);
                    CONFIGS.store(ep, pep);
                    updatePreloaderComment(ep);
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    try {
                        lock = privPropsFO.lock();
                        os = privPropsFO.getOutputStream(lock);
                        pep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
            updatePreloaderDependencies(CONFIGS);
            CONFIGS.storeActive();

        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    private void updatePreloaderComment(EditableProperties ep) {
        if(isTrue(ep.get(JFXProjectProperties.PRELOADER_ENABLED))) {
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectProperties.class, "COMMENT_use_preloader")}, false); // NOI18N    
        } else {
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectProperties.class, "COMMENT_dontuse_preloader")}, false); // NOI18N    
        }
    }

    private void initSigning(PropertyEvaluator eval) {
        String enabled = eval.getProperty(JAVAFX_SIGNING_ENABLED);
        String signedProp = eval.getProperty(JAVAFX_SIGNING_TYPE);
        signingEnabled = isTrue(enabled);
        if(signedProp == null) {
            signingType = SigningType.NOSIGN;
        } else {
            if(signedProp.equalsIgnoreCase(SigningType.SELF.getString())) {
                signingType = SigningType.SELF;
            } else {
                if(signedProp.equalsIgnoreCase(SigningType.KEY.getString())) {
                    signingType = SigningType.KEY;
                } else {
                    signingType = SigningType.NOSIGN;
                }
            }
        }
        signingKeyStore = eval.getProperty(JAVAFX_SIGNING_KEYSTORE);
        //if (signingKeyStore == null) signingKeyStore = "";
        signingKeyAlias = eval.getProperty(JAVAFX_SIGNING_KEY);
        //if (signingKeyAlias == null) signingKeyAlias = "";
        if (eval.getProperty(JAVAFX_SIGNING_KEYSTORE_PASSWORD) != null) {
            signingKeyStorePassword = eval.getProperty(JAVAFX_SIGNING_KEYSTORE_PASSWORD).toCharArray();
        }
        if (eval.getProperty(JAVAFX_SIGNING_KEY_PASSWORD) != null) {
            signingKeyPassword = eval.getProperty(JAVAFX_SIGNING_KEY_PASSWORD).toCharArray();
        }
        permissionsElevated = isTrue(eval.getProperty(PERMISSIONS_ELEVATED));
    }
    
    private void initResources (final PropertyEvaluator eval, final Project prj, final JFXConfigs configs) {
        final String lz = eval.getProperty(DOWNLOAD_MODE_LAZY_JARS); //old way, when changed rewritten to new
        final String rcp = eval.getProperty(RUN_CP);        
        final String bc = eval.getProperty(BUILD_CLASSES);        
        final File prjDir = FileUtil.toFile(prj.getProjectDirectory());
        final File bcDir = bc == null ? null : PropertyUtils.resolveFile(prjDir, bc);
        final List<File> lazyFileList = new ArrayList<File>();
        String[] paths;
        if (lz != null) {
            paths = PropertyUtils.tokenizePath(lz);            
            for (String p : paths) {
                lazyFileList.add(PropertyUtils.resolveFile(prjDir, p));
            }
        }
        paths = PropertyUtils.tokenizePath(rcp);
        String mainJar = eval.getProperty(DIST_JAR);
        final File mainFile = PropertyUtils.resolveFile(prjDir, mainJar);
        List<FileObject> preloaders = new ArrayList<FileObject>();
        try {
            for(PreloaderArtifact pa : getPreloaderArtifactsFromConfigs(configs)) {
                preloaders.addAll(Arrays.asList(pa.getFileObjects()));
            }
        } catch (IOException ex) {
            // no need to react
        }

        final List<File> resFileList = new ArrayList<File>(paths.length);
        for (String p : paths) {
            if (p.startsWith("${") && p.endsWith("}")) {    //NOI18N
                continue;
            }
            final File f = PropertyUtils.resolveFile(prjDir, p);
            if (f.equals(mainFile)) {
                continue;
            }
            boolean isPrel = false;
            for(FileObject prelfo : preloaders) {
                File prelf = FileUtil.toFile(prelfo);
                if(prelf != null && prelf.equals(f)) {
                    isPrel = true;
                    continue;
                }
            }
            if (!isPrel && (bc == null || !bcDir.equals(f)) ) {
                resFileList.add(f);
                if (isTrue(eval.getProperty(String.format(DOWNLOAD_MODE_LAZY_FORMAT, f.getName())))) {
                    lazyFileList.add(f);
                }
            }
        }
        lazyJars = lazyFileList;
        runtimeCP = resFileList;
        lazyJarsChanged = false;
    }
    
    private void storeResources(final EditableProperties props) {
        if (lazyJarsChanged) {
            //Remove old way if exists
            props.remove(DOWNLOAD_MODE_LAZY_JARS);
            final Iterator<Map.Entry<String,String>> it = props.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey().startsWith(DOWNLOAD_MODE_LAZY_JAR)) {
                    it.remove();
                }
            }
            for (File lazyJar : lazyJars) {
                props.setProperty(String.format(DOWNLOAD_MODE_LAZY_FORMAT, lazyJar.getName()), "true");  //NOI18N
            }
        }
    }

    private void initJSCallbacks (final PropertyEvaluator eval) {
        String platformName = eval.getProperty("platform.active");
        Map<String,List<String>/*|null*/> callbacks = JFXProjectUtils.getJSCallbacks(platformName);
        Map<String,String/*|null*/> result = new LinkedHashMap<String,String/*|null*/>();
        for(Map.Entry<String,List<String>/*|null*/> entry : callbacks.entrySet()) {
            String v = eval.getProperty(JFXProjectProperties.JAVASCRIPT_CALLBACK_PREFIX + entry.getKey());
            if(v != null && !v.isEmpty()) {
                result.put(entry.getKey(), v);
            }
        }
        jsCallbacks = result;
        jsCallbacksChanged = false;
    }
    
    private void storeJSCallbacks(final EditableProperties props) {
        if (jsCallbacksChanged && jsCallbacks != null) {
            for (Map.Entry<String,String> entry : jsCallbacks.entrySet()) {
                if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                    props.setProperty(JAVASCRIPT_CALLBACK_PREFIX + entry.getKey(), entry.getValue());  //NOI18N
                } else {
                    props.remove(JAVASCRIPT_CALLBACK_PREFIX + entry.getKey());
                }
            }
        }
    }

    private void storePlatform(EditableProperties editableProps) {
        String activePlatform = editableProps.getProperty("platform.active"); // NOI18N
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : installedPlatforms) {
            String platformName = javaPlatform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
            if (isEqual(platformName, activePlatform) && JavaFXPlatformUtils.isJavaFXEnabled(javaPlatform)) {
                editableProps.setProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_SDK, JavaFXPlatformUtils.getJavaFXSDKPathReference(activePlatform));
                editableProps.setProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_RUNTIME, JavaFXPlatformUtils.getJavaFXRuntimePathReference(activePlatform));
            }
        }
    }

    public class PreloaderClassComboBoxModel extends DefaultComboBoxModel {
        
        private boolean filling = false;
        private ChangeListener changeListener = null;
              
        public PreloaderClassComboBoxModel() {
            fillNoPreloaderAvailable();
        }
        
        public void addChangeListener (ChangeListener l) {
            changeListener = l;
        }

        public void removeChangeListener (ChangeListener l) {
            changeListener = null;
        }

        public final void fillNoPreloaderAvailable() {
            removeAllElements();
            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
        }
        
        public void fillFromProject(final Project project, final String select, final JFXConfigs configs, final String activeConfig) {
            final Map<FileObject,List<ClassPath>> classpathMap = JFXProjectUtils.getClassPathMap(project);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    if(!filling) {
                        filling = true;
                        removeAllElements();
                        if(project == null) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                            return;
                        }
                        final Set<String> appClassNames = JFXProjectUtils.getAppClassNames(classpathMap, "javafx.application.Preloader"); //NOI18N
                        if(appClassNames.isEmpty()) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                        } else {
                            addElements(appClassNames);
                            if(select != null) {
                                setSelectedItem(select);
                            }
                            //if(activeConfig != null) {
                                String verify = (String)getSelectedItem();
                                if(!isEqual(configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS), verify)) {
                                    configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS, verify);
                                    //configs.solidifyBoundedGroups(activeConfig, verify);
//                                    configs.setProperty(activeConfig, JFXProjectProperties.PRELOADER_ENABLED, 
//                                            configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED));
                                }
                            //}
                        }
                        if (changeListener != null) {
                            changeListener.stateChanged (appClassNames.isEmpty() ? null : new ChangeEvent (this));
                        }
                        filling = false;
                    }
                }
            });            
        }

        public void fillFromJAR(final FileObject jarFile, final String select, final JFXConfigs configs, final String activeConfig) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    if(!filling) {
                        filling = true;
                        removeAllElements();
                        if(jarFile == null) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                            return;
                        }
                        final Set<String> appClassNames = JFXProjectUtils.getAppClassNamesInJar(jarFile, "javafx.application.Preloader"); //NOI18N    
                        appClassNames.remove("com.javafx.main.Main"); // NOI18N
                        appClassNames.remove("com.javafx.main.NoJavaFXFallback"); // NOI18N
                        if(appClassNames.isEmpty()) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                        } else {
                            addElements(appClassNames);
                            if(select != null) {
                                setSelectedItem(select);
                            }
                            //if(activeConfig != null) {
                                String verify = (String)getSelectedItem();
                                if(!isEqual(configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS), verify)) {
                                    configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS, verify);
//                                    configs.setProperty(activeConfig, JFXProjectProperties.PRELOADER_ENABLED, 
//                                            configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_ENABLED));
                                }
                            //}
                        }
                        if (changeListener != null) {
                            changeListener.stateChanged (appClassNames.isEmpty() ? null : new ChangeEvent (this));
                        }
                        filling = false;
                    }
                }
            });            
        }

        private void addElements(Set<String> elems) {
            for (String elem : elems) {
                addElement(elem);
            }
        }
        
    }
    
    /**
     * Each preloader specified in project configurations needs
     * to be added/removed to/from project dependencies whenever
     * configurations change (see Run category in Project Properties
     * dialog). 
     * List of preoader artifacts is thus needed to keep track which
     * project dependencies are preloader related.
     */
    abstract class PreloaderArtifact {
        
        /**
         * Dependency validity tag
         */
        private boolean valid;
        
        /**
         * Add {@code this} to dependencies of project if it is not there yet
         * @return true if preloader artifact has been added, false if it was already there
         */
        abstract boolean addDependency() throws IOException, UnsupportedOperationException;
        
        /**
         * Remove {@code this} from dependencies of project if it is there
         * @return true if preloader artifact has been removed, false if it was not among project dependencies
         */
        abstract boolean removeDependency() throws IOException, UnsupportedOperationException;
        
        /**
         * Returns array of files represented by this PreloaderArtifact
         * @return array of FileObjects of files represented by this object
         */
        abstract FileObject[] getFileObjects();
        
        /**
         * Set the validity tag for {@code this} artifact
         * @param valid true for dependencies to be kept, false for dependencies to be removed
         */
        void setValid(boolean valid) {
            this.valid = valid;
        }
        
        /**
         * Get the validity tag for {@code this} artifact
         * @return valid true for dependencies to be kept, false for dependencies to be removed
         */
        boolean isValid() {
            return valid;
        }
    }
    
    class PreloaderProjectArtifact extends PreloaderArtifact {

        private final String ID;
        private final AntArtifact[] artifacts;
        private final URI[] artifactElements;
        private final FileObject projectArtifact;
        private final String classPathType;
                
        PreloaderProjectArtifact(final @NonNull AntArtifact[] artifacts, final @NonNull URI[] artifactElements,
            final @NonNull FileObject projectArtifact, final @NonNull String classPathType, final @NonNull String ID) {
            this.artifacts = artifacts;
            this.artifactElements = artifactElements;
            this.projectArtifact = projectArtifact;
            this.classPathType = classPathType;
            this.ID = ID;
        }
        
        @Override
        public boolean addDependency() throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.addAntArtifacts(artifacts, artifactElements, projectArtifact, classPathType);
        }

        @Override
        public boolean removeDependency()  throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.removeAntArtifacts(artifacts, artifactElements, projectArtifact, classPathType);
        }

        @Override
        public boolean equals(Object that){
            if ( this == that ) return true;
            if ( !(that instanceof PreloaderProjectArtifact) ) return false;
            PreloaderProjectArtifact concrete = (PreloaderProjectArtifact)that;
            return ID.equals(concrete.ID);
        }

        @Override
        final FileObject[] getFileObjects() {
            List<FileObject> l = new ArrayList<FileObject>();
            for(AntArtifact a : artifacts) {
                l.addAll(Arrays.asList(a.getArtifactFiles()));
            }
            return l.toArray(new FileObject[l.size()]);
        }
    }

    class PreloaderJarArtifact extends PreloaderArtifact {

        private final String ID;
        private final URL[] classPathRoots;
        private final FileObject[] fileObjects;
        private final FileObject projectArtifact;
        private final String classPathType;
                
        PreloaderJarArtifact(final @NonNull URL[] classPathRoots, final @NonNull FileObject[] fileObjects, final @NonNull FileObject projectArtifact, 
                final @NonNull String classPathType, final @NonNull String ID) {
            this.classPathRoots = classPathRoots;
            this.fileObjects = fileObjects;
            this.projectArtifact = projectArtifact;
            this.classPathType = classPathType;
            this.ID = ID;
        }
        
        @Override
        public boolean addDependency() throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.addRoots(classPathRoots, projectArtifact, classPathType);
        }

        @Override
        public boolean removeDependency()  throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.removeRoots(classPathRoots, projectArtifact, classPathType);
        }
        
        @Override
        public boolean equals(Object that){
            if ( this == that ) return true;
            if ( !(that instanceof PreloaderJarArtifact) ) return false;
            PreloaderJarArtifact concrete = (PreloaderJarArtifact)that;
            return ID.equals(concrete.ID);
        }

        @Override
        final FileObject[] getFileObjects() {
            return fileObjects;
        }
    }

    /**
     * Project configurations maintenance class
     * 
     * Getter/Setter naming conventions:
     * "Property" in method name -> method deals with single properties in configuration given by parameter config
     * "Default" in method name -> method deals with properties in default configuration
     * "Active" in method name -> method deals with properties in currently chosen configuration
     * "Transparent" in method name -> method deals with property in configuration fiven by parameter config if
     *     exists, or with property in default configuration otherwise. This is to provide simple access to
     *     union of default and non-default properties that are to be presented to users in non-default configurations
     * "Param" in method name -> metod deals with properties representing sets of application parameters
     */
    public class JFXConfigs {
        
        private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> RUN_CONFIGS;
        private Set<String> ERASED_CONFIGS;
        private Map<String/*|null*/,List<Map<String,String/*|null*/>>/*|null*/> APP_PARAMS;
        private BoundedPropertyGroups groups = new BoundedPropertyGroups();
        private String active;
        
        private Comparator<String> getComparator() {
            return new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
                }
            };
        }

        // list of all properties related to project configurations (excluding application parameter properties that are handled separately)
        private List<String> PROJECT_PROPERTIES = Arrays.asList(new String[] {
            ProjectProperties.MAIN_CLASS, MAIN_CLASS, /*APPLICATION_ARGS,*/ RUN_JVM_ARGS, 
            PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
            RUN_WORK_DIR, RUN_APP_WIDTH, RUN_APP_HEIGHT, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_AS});
        // list of those properties that should be stored in private.properties instead of project.properties
        private List<String> PRIVATE_PROPERTIES = Arrays.asList(new String[] {
            RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_AS});
        // list of properties that, if set, should later not be overriden by changes in default configuration
        // (useful for keeping pre-defined configurations that do not change unexpectedly after changes in default config)
        // Note that the standard behavior is: when setting a default property, the property is checked in all configs
        // and reset if its value in any non-def config is equal to that in default config
        private List<String> STATIC_PROPERTIES = Arrays.asList(new String[] {
            RUN_AS});

        // property groups
        private String PRELOADER_GROUP_NAME = "preloader"; // NOI18N
        private List<String> PRELOADER_PROPERTIES = Arrays.asList(new String[] {
            JFXProjectProperties.PRELOADER_ENABLED, JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PRELOADER_PROJECT, 
            JFXProjectProperties.PRELOADER_JAR_PATH, JFXProjectProperties.PRELOADER_JAR_FILENAME, JFXProjectProperties.PRELOADER_CLASS});
        private String BROWSER_GROUP_NAME = "browser"; // NOI18N
        private List<String> BROWSER_PROPERTIES = Arrays.asList(new String[] {
            JFXProjectProperties.RUN_IN_BROWSER, JFXProjectProperties.RUN_IN_BROWSER_PATH});
        
        public final List<String> getPreloaderProperties() {
            return Collections.unmodifiableList(PRELOADER_PROPERTIES);
        }
        
        public final List<String> getBrowserProperties() {
            return Collections.unmodifiableList(BROWSER_PROPERTIES);
        }

        JFXConfigs() {
            reset();
            defineGroup(PRELOADER_GROUP_NAME, getPreloaderProperties());
            defineGroup(BROWSER_GROUP_NAME, getBrowserProperties());
        }
        
        private void reset() {
            RUN_CONFIGS = new TreeMap<String,Map<String,String>>(getComparator());
            ERASED_CONFIGS = null;
            APP_PARAMS = new TreeMap<String,List<Map<String,String>>>(getComparator());
        }
        
        private boolean configNameWrong(String config) {
            return config !=null && config.contains("default");
        }

        public final void defineGroup(String groupName, Collection<String> props) {
            groups.defineGroup(groupName, props);
        }
        
        public final void clearGroup(String groupName) {
            groups.clearGroup(groupName);
        }

        public final void clearAllGroups() {
            groups.clearAllGroups();
        }

        public boolean isBound(String prop) {
            return groups.isBound(prop);
        }

        public Collection<String> getBoundedProperties(String prop) {
            return groups.getBoundedProperties(prop);
        }

        //==========================================================

        public String getActive() {
            return active;
        }
        public void setActive(String config) {
            assert !configNameWrong(config);
            active = config;
        }
        
        //==========================================================

        public boolean hasConfig(String config) {
            assert !configNameWrong(config);
            return RUN_CONFIGS.containsKey(config);
        }
        
        public boolean isConfigEmpty(String config) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap != null) {
                return configMap.isEmpty();
            }
            return true;
        }
        
        public boolean isDefaultConfigEmpty() {
            return isConfigEmpty(null);
        }
        
        public boolean isActiveConfigEmpty() {
            return isConfigEmpty(getActive());
        }

        //----------------------------------------------------------

        public Set<String> getConfigNames() {
            return Collections.unmodifiableSet(RUN_CONFIGS.keySet());
        }
        
        private Map<String,String/*|null*/> getConfigUnmodifyable(String config) {
            assert !configNameWrong(config);
            return Collections.unmodifiableMap(RUN_CONFIGS.get(config));
        }
        
        private Map<String,String/*|null*/> getDefaultConfigUnmodifyable() {
            return getConfigUnmodifyable(null);
        }

        private Map<String,String/*|null*/> getActiveConfigUnmodifyable() {
            return getConfigUnmodifyable(getActive());
        }

        private Map<String,String/*|null*/> getConfig(String config) {
            assert !configNameWrong(config);
            return RUN_CONFIGS.get(config);
        }
        
        private Map<String,String/*|null*/> getDefaultConfig() {
            return getConfig(null);
        }
        
        private Map<String,String/*|null*/> getActiveConfig() {
            return getConfig(getActive());
        }

        private Map<String,String/*|null*/> getConfigNonNull(String config) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap == null) {
                configMap = new TreeMap<String,String>(getComparator());
                RUN_CONFIGS.put(config, configMap);
            }
            return configMap;
        }
        
        private Map<String,String/*|null*/> getDefaultConfigNonNull() {
            return getConfigNonNull(null);
        }
        
        private Map<String,String/*|null*/> getActiveConfigNonNull() {
            return getConfigNonNull(getActive());
        }
        
        //----------------------------------------------------------

        /**
         * Adds new and replaces existing properties
         * @param config
         * @param props 
         */
        public void addToConfig(String config, Map<String,String/*|null*/> props) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap == null) {
                configMap = new TreeMap<String,String>(getComparator());
                RUN_CONFIGS.put(config, configMap);
            }
            configMap.putAll(props);
        }
        
        public void addToDefaultConfig(Map<String,String/*|null*/> props) {
            addToConfig(null, props);
        }

        public void addToActiveConfig(Map<String,String/*|null*/> props) {
            addToConfig(getActive(), props);
        }

        public void addToConfig(String config, EditableProperties props) {
            assert !configNameWrong(config);
            addToConfig(config, new HashMap<String,String>(props));
        }

        public void addToDefaultConfig(EditableProperties props) {
            addToConfig(null, props);
        }
        
        public void addToActiveConfig(EditableProperties props) {
            addToConfig(getActive(), props);
        }

        //----------------------------------------------------------

        public void eraseConfig(String config) {
            assert !configNameWrong(config);
            assert config != null; // erasing default config not allowed
            RUN_CONFIGS.remove(config);
            if(ERASED_CONFIGS == null) {
                ERASED_CONFIGS = new HashSet<String>();
            }
            ERASED_CONFIGS.add(config);
        }

        //==========================================================

        /**
         * Returns true if property name is defined in configuration config, false otherwise
         * @param config
         * @param name
         * @return 
         */
        public boolean isPropertySet(String config, @NonNull String prop) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap != null) {
                return configMap.containsKey(prop);
            }
            return false;
        }
        
        public boolean isDefaultPropertySet(@NonNull String prop) {
            return isPropertySet(null, prop);
        }
        
        public boolean isActivePropertySet(@NonNull String prop) {
            return isPropertySet(getActive(), prop);
        }

        /**
         * Returns true if bounded properties exist for prop and at least
         * one of them is set. This is to be used in updateProperty() to
         * indicate that an empty property needs to be stored to editable properties
         * 
         * @param config
         * @param prop
         * @return 
         */
        private boolean isBoundedToNonemptyProperty(String config, String prop) {
            assert !configNameWrong(config);
            for(String name : groups.getBoundedProperties(prop)) {
                if(isPropertySet(config, name)) {
                    return true;
                }
            }
            return false;
        }

        //----------------------------------------------------------

        /**
         * Returns property value from configuration config if defined, null otherwise
         * @param config
         * @param name
         * @return 
         */
        public String getProperty(String config, @NonNull String prop) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap != null) {
                return configMap.get(prop);
            }
            return null;
        }
        
        public String getDefaultProperty(@NonNull String prop) {
            return getProperty(null, prop);
        }
        
        public String getActiveProperty(@NonNull String prop) {
            return getProperty(getActive(), prop);
        }

        /**
         * Returns property value from configuration config (if exists), or
         * value from default config (if exists) otherwise
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getPropertyTransparent(String config, @NonNull String prop) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            String value = null;
            if(configMap != null) {
                value = configMap.get(prop);
                if(value == null && config != null) {
                    return getDefaultProperty(prop);
                }
            }
            return value;
        }
        
        public String getActivePropertyTransparent(@NonNull String prop) {
            return getPropertyTransparent(getActive(), prop);
        }
        
        //----------------------------------------------------------

        public void setProperty(String config, @NonNull String prop, String value) {
            setPropertyImpl(config, prop, value);
            solidifyBoundedGroups(config, prop);
            if(config == null) {
                for(String c: getConfigNames()) {
                    if(c != null && isEqual(getProperty(c, prop), value) && !STATIC_PROPERTIES.contains(prop) && isBoundedPropertiesEraseable(c, prop)) {
                        eraseProperty(c, prop);
                    }
                }
            }
        }

        private void setPropertyImpl(String config, @NonNull String prop, String value) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfigNonNull(config);
            configMap.put(prop, value);            
        }
        
        public void setDefaultProperty(@NonNull String prop, String value) {
            setProperty(null, prop, value);
        }
        
        public void setActiveProperty(@NonNull String prop, String value) {
            setProperty(getActive(), prop, value);
        }

        public void setPropertyTransparent(String config, @NonNull String prop, String value) {
            assert !configNameWrong(config);
            if(config != null && isEqual(getDefaultProperty(prop), value) && (!STATIC_PROPERTIES.contains(prop) || !isPropertySet(config, prop)) && isBoundedPropertiesEraseable(config, prop)) {
                eraseProperty(config, prop);
            } else {
                setProperty(config, prop, value);
            }
        }
        
        public void setActivePropertyTransparent(@NonNull String prop, String value) {
            setPropertyTransparent(getActive(), prop, value);
        }

        //----------------------------------------------------------
        
        /**
         * In non-default configurations if prop is not set, then
         * this method sets it to a value taken from default config.
         * The result is transparent to getPropertyTransparent(), which
         * returns the same value before and after solidifyProperty() call.
         * 
         * @param config
         * @param prop
         * @return false if property had existed in config, true if it had been set by this method
         */
        public boolean solidifyProperty(String config, @NonNull String prop) {
            if(!isPropertySet(config, prop)) {
                if(config != null) {
                    setPropertyImpl(config, prop, getDefaultProperty(prop));
                } else {
                    setPropertyImpl(null, prop, ""); // NOI18N
                }
                return true;
            }
            return false;
        }
        
        /**
         * Solidifies all properties that are in any bounded group with the 
         * property prop
         * 
         * @param config
         * @param prop
         * @return false if nothing was solidified, true otherwise
         */
        private boolean solidifyBoundedGroups(String config, @NonNull String prop) {
            boolean solidified = false;
            for(String name : groups.getBoundedProperties(prop)) {
                solidified |= solidifyProperty(config, name);
            }
            return solidified;
        }
        
        //----------------------------------------------------------

        public void eraseProperty(String config, @NonNull String prop) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> configMap = getConfig(config);
            if(configMap != null) {
                configMap.remove(prop);
                for(String name : groups.getBoundedProperties(prop)) {
                    configMap.remove(name);
                }
            }
        }
        
        public void eraseDefaultProperty(@NonNull String prop) {
            eraseProperty(null, prop);
        }

        public void eraseActiveProperty(@NonNull String prop) {
            eraseProperty(getActive(), prop);
        }

        /**
         * Returns true if property prop and all properties bounded to it
         * can be erased harmlessly, i.e., to ensure that getPropertyTransparent()
         * returns for each of them the same value before and after erasing
         * 
         * @param prop
         * @return 
         */
        private boolean isBoundedPropertiesEraseable(String config, String prop) {
            assert !configNameWrong(config);
            if(config == null) {
                return false;
            }
            boolean canErase = true;
            for(String name : groups.getBoundedProperties(prop)) {
                if((isPropertySet(config, name) && !isEqual(getDefaultProperty(name), getProperty(config, name))) || STATIC_PROPERTIES.contains(name)) {
                    canErase = false;
                    break;
                }
            }
            return canErase;
        }
        
        //==========================================================
        
        /**
         * Returns true if param named name is present in configuration
         * config in any form - with value or without value
         * @param config
         * @param name
         * @return 
         */
        public boolean hasParam(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getParam(config, name) != null;
        }

        public boolean hasDefaultParam(@NonNull String name) {
            return hasParam(null, name);
        }
        
        public boolean hasActiveParam(@NonNull String name) {
            return hasParam(getActive(), name);
        }

        public boolean hasParamTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            //return hasParam(config, name) || hasDefaultParam(name);
            return getParamTransparent(config, name) != null;
        }
        
        public boolean hasActiveParamTransparent(@NonNull String name) {
            return hasParamTransparent(getActive(), name);
        }
        
        //----------------------------------------------------------

        /**
         * Returns true if exactly the parameter with name name and value value
         * is present in configuration config
         * 
         * @param config
         * @param name
         * @param value
         * @return 
         */
        public boolean hasParam(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getParamValue(config, name);
            return isEqual(v, value);
        }
        
        public boolean hasDefaultParam(@NonNull String name, @NonNull String value) {
            return hasParam(null, name, value);
        }
        
        public boolean hasActiveParam(@NonNull String name, @NonNull String value) {
            return hasParam(getActive(), name, value);
        }

        public boolean hasParamTransparent(String config, @NonNull String name, @NonNull String value) {
            assert !configNameWrong(config);
            String v = getParamValueTransparent(config, name);
            return isEqual(v, value);
        }

        //----------------------------------------------------------

        public boolean hasParamValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getParam(config, name);
            if(param != null) {
                if(param.containsKey(APP_PARAM_SUFFIXES[1])) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean hasDefaultParamValue(@NonNull String name) {
            return hasParamValue(null, name);
        }

        public boolean hasActiveParamValue(@NonNull String name) {
            return hasParamValue(getActive(), name);
        }

        public boolean hasParamValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return (config != null && hasParamValue(config, name)) || hasDefaultParamValue(name);
        }

        public boolean hasActiveParamValueTransparent(@NonNull String name) {
            return hasParamValueTransparent(getActive(), name);
        }
        
        //----------------------------------------------------------

        /**
         * Returns param as map if exists in configuration config, null otherwise
         * 
         * @param config
         * @param name
         * @return 
         */
        public Map<String, String> getParam(String config, @NonNull String name) {
            assert !configNameWrong(config);
            return getParam(getParams(config), name);
        }
        
        public Map<String, String> getDefaultParam(@NonNull String name) {
            return getParam((String)null, name);
        }
        
        public Map<String, String> getActiveParam(@NonNull String name) {
            return getParam(getActive(), name);
        }

        public Map<String, String> getParamTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getParam(config, name);
            if(param == null) {
                param = getDefaultParam(name);
            }
            return param;
        }
        
        public Map<String, String> getActiveParamTransparent(@NonNull String name) {
            return getParamTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*ParamValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getParamValue(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String,String/*|null*/> param = getParam(config, name);
            if(param != null) {
                return param.get(APP_PARAM_SUFFIXES[1]);
            }
            return null;
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*ParamValue*()
         * 
         * @param name
         * @return 
         */
        public String getDefaultParamValue(@NonNull String name) {
            return getParamValue(null, name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*ParamValue*()
         * 
         * @param name
         * @return 
         */
        public String getActiveParamValue(@NonNull String name) {
            return getParamValue(getActive(), name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*ParamValue*()
         * 
         * @param config
         * @param name
         * @return 
         */
        public String getParamValueTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            Map<String, String> param = getParam(config, name);
            if(param != null) {
                return param.get(APP_PARAM_SUFFIXES[1]);
            }
            return getDefaultParamValue(name);
        }

        /**
         * Note that returned null is ambiguous - may mean that there
         * was no value defined or that it was defined and its value was null.
         * To check this ask has*ParamValue*()
         * 
         * @param name
         * @return 
         */
        public String getActiveParamValueTransparent(@NonNull String name) {
            return getParamValueTransparent(getActive(), name);
        }
        
        //----------------------------------------------------------

        private List<Map<String,String/*|null*/>> getParams(String config) {
            assert !configNameWrong(config);
            return APP_PARAMS.get(config);
        }

        private List<Map<String,String/*|null*/>> getDefaultParams() {
            return APP_PARAMS.get(null);
        }
                
        private List<Map<String,String/*|null*/>> getActiveParams() {
            return APP_PARAMS.get(getActive());
        }

        /**
        * Returns (copy of) list of default parameters if config==default or
        * union of default config and current config parameters otherwise
        * 
        * @param config current config
        * @return union of default and current parameters
        */
        private List<Map<String,String/*|null*/>> getParamsTransparent(String config) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> union = JFXProjectUtils.copyList(getDefaultParams());
            if(config != null && getParams(config) != null) {
                for(Map<String,String> map : getParams(config)) {
                    String name = map.get(APP_PARAM_SUFFIXES[0]);
                    String value = map.get(APP_PARAM_SUFFIXES[1]);
                    if(name != null && !name.isEmpty()) {
                        Map<String, String> old = getParam(union, name);
                        if(old != null) {
                            old.put(APP_PARAM_SUFFIXES[0], name);
                            old.put(APP_PARAM_SUFFIXES[1], value);
                        } else {
                            union.add(map);
                        }
                    }
                }
            }
            return union;
        }   
        
        public List<Map<String,String/*|null*/>> getActiveParamsTransparent() {
            return getParamsTransparent(getActive());
        }
        
        //----------------------------------------------------------

        /**
        * Gathers all parameters applicable to config configuration to one String
        * 
        * @param commandLine if true, formats output as if to be passed on command line, otherwise prouces comma separated list
        * @return a String containing all parameters as if passed as command line parameters
        */
        public String getParamsTransparentAsString(String config, boolean commandLine) {
            assert !configNameWrong(config);
            return getParamsAsString(getParamsTransparent(config), commandLine);
        }

        public String getActiveParamsTransparentAsString(boolean commandLine) {
            return getParamsAsString(getActiveParamsTransparent(), commandLine);
        }

        public String getParamsAsString(String config, boolean commandLine) {
            return getParamsAsString(getParams(config), commandLine);
        }

        public String getActiveParamsAsString(boolean commandLine) {
            return getParamsAsString(getActiveParams(), commandLine);
        }
        
        public String getDefaultParamsAsString(boolean commandLine) {
            return getParamsAsString(getDefaultParams(), commandLine);
        }

        private String getParamsAsString(List<Map<String,String/*|null*/>> params, boolean commandLine)
        {
            StringBuilder sb = new StringBuilder();
            if(params != null) {
                int index = 0;
                for(Map<String,String> m : params) {
                    String name = m.get(APP_PARAM_SUFFIXES[0]);
                    String value = m.get(APP_PARAM_SUFFIXES[1]);
                    if(name != null && name.length() > 0) {
                        if(sb.length() > 0) {
                            if(!commandLine) {
                                sb.append(","); // NOI18N
                            }
                            sb.append(" "); // NOI18N
                        }
                        if(value != null && value.length() > 0) {
                            if(commandLine) {
                                sb.append("--"); // NOI18N
                            }
                            sb.append(name);
                            sb.append("="); // NOI18N
                            sb.append(value);
                        } else {
                            sb.append(name);                        
                        }
                        index++;
                    }
                }
            }
            return sb.toString();
        }

        //----------------------------------------------------------

        private Map<String, String> createParam(@NonNull String name) {
            Map<String, String> param = new TreeMap<String,String>(getComparator());
            param.put(APP_PARAM_SUFFIXES[0], name);
            return param;
        }
        
        private Map<String, String> createParam(@NonNull String name, String value) {
            Map<String, String> param = new TreeMap<String,String>(getComparator());
            param.put(APP_PARAM_SUFFIXES[0], name);
            param.put(APP_PARAM_SUFFIXES[1], value);
            return param;
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) valueless param (i.e., argument)
         * to configuration config
         */
        public void addParam(String config, @NonNull String name) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> params = getParams(config);
            if(params == null) {
                params = new ArrayList<Map<String,String/*|null*/>>();
                APP_PARAMS.put(config, params);
            } else {
                eraseParam(params, name);
            }
            params.add(createParam(name));
        }
        
        public void addDefaultParam(@NonNull String name) {
            addParam(null, name);
        }

        public void addActiveParam(@NonNull String name) {
            addParam(getActive(), name);
        }

        public void addParamTransparent(String config, @NonNull String name) {
            assert !configNameWrong(config);
            if(config == null) {
                addDefaultParam(name);
            } else {
                if(hasDefaultParam(name) && !hasDefaultParamValue(name)) {
                    eraseParam(config, name);
                } else {
                    addParam(config, name);
                }
            }
        }

        public void addActiveParamTransparent(@NonNull String name) {
            addParamTransparent(getActive(), name);
        }

        //----------------------------------------------------------

        /**
         * Add (or replace if present) named param (i.e., having a value)
         * to configuration config
         */
        public void addParam(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            List<Map<String,String/*|null*/>> params = getParams(config);
            if(params == null) {
                params = new ArrayList<Map<String,String/*|null*/>>();
                APP_PARAMS.put(config, params);
            } else {
                eraseParam(params, name);
            }
            params.add(createParam(name, value));
        }

        public void addDefaultParam(@NonNull String name, String value) {
            addParam(null, name, value);
        }
        
        public void addActiveParam(@NonNull String name, String value) {
            addParam(getActive(), name, value);
        }

        public void addParamTransparent(String config, @NonNull String name, String value) {
            assert !configNameWrong(config);
            if(config == null) {
                addDefaultParam(name, value);
            } else {
                if(hasDefaultParam(name, value)) {
                    eraseParam(config, name);
                } else {
                    addParam(config, name, value);
                }
            }
        }
        
        public void addActiveParamTransparent(@NonNull String name, String value) {
            addParamTransparent(getActive(), name, value);
        }

        //----------------------------------------------------------

        /**
        * Updates parameters; if config==default, then simply updates default parameters,
        * otherwise updates parameters in current config so that only those different
        * from those in default config are stored.
        * 
        * @param config
        * @param params 
        */
        public void setParamsTransparent(String config, List<Map<String,String/*|null*/>>/*|null*/ params) {
            assert !configNameWrong(config);
            if(config == null) {
                APP_PARAMS.put(null, params);
            } else {
                List<Map<String,String/*|null*/>> reduct = new ArrayList<Map<String,String/*|null*/>>();
                List<Map<String,String/*|null*/>> def = JFXProjectUtils.copyList(getDefaultParams());
                if(params != null) {
                    for(Map<String,String> map : params) {
                        String name = map.get(APP_PARAM_SUFFIXES[0]);
                        String value = map.get(APP_PARAM_SUFFIXES[1]);
                        Map<String, String> old = getDefaultParam(name);
                        if(old != null) {
                            String oldValue = old.get(APP_PARAM_SUFFIXES[1]);
                            if( !isEqual(value, oldValue) ) {
                                reduct.add(JFXProjectUtils.copyMap(old));
                            }
                            def.remove(old);
                        } else {
                            reduct.add(JFXProjectUtils.copyMap(map));
                        }
                    }
                    for(Map<String,String> map : def) {
                        map.put(APP_PARAM_SUFFIXES[1], ""); // NOI18N
                        reduct.add(JFXProjectUtils.copyMap(map));
                    }
                }
                APP_PARAMS.put(config, reduct);
            }
        }
        
        public void setActiveParamsTransparent(List<Map<String,String/*|null*/>>/*|null*/ params) {
            setParamsTransparent(getActive(), params);
        }

        //----------------------------------------------------------

        public void eraseParam(String config, @NonNull String name) {
            assert !configNameWrong(config);
            eraseParam(getParams(config), name);
        }

        public void eraseDefaultParam(@NonNull String name) {
            eraseParam((String)null, name);
        }
        
        public void eraseActiveParam(@NonNull String name) {
            eraseParam(getActive(), name);
        }

        public void eraseParams(String config) {
            assert !configNameWrong(config);
            APP_PARAMS.remove(config);
        }

        public void eraseDefaultParams() {
            eraseParams(null);
        }

        public void eraseActiveParams() {
            eraseParams(getActive());
        }

        //==========================================================

        /**
        * If paramName exists in params, returns the map representing it
        * Returns null if param does not exist.
        * 
        * @param params list of application parameters (each stored in a map in keys 'name' and 'value'
        * @param paramName parameter to be searched for
        * @return parameter if found, null otherwise
        */
        private Map<String, String> getParam(List<Map<String, String>> params, String paramName) {
            if(params != null) {
                for(Map<String, String> map : params) {
                    String name = map.get(APP_PARAM_SUFFIXES[0]);
                    if(name != null && name.equals(paramName)) {
                        return map;
                    }
                }
            }
            return null;
        }
        
        private void eraseParam(List<Map<String, String>> params, String paramName) {
            if(params != null) {
                Map<String, String> toErase = null;
                for(Map<String, String> map : params) {
                    String name = map.get(APP_PARAM_SUFFIXES[0]);
                    if(name != null && name.equals(paramName)) {
                        toErase = map;
                        break;
                    }
                }
                if(toErase != null) {
                    params.remove(toErase);
                }
            }
        }

        //==========================================================
        
        /**
         * Reads configuration properties from project properties files
         * (modified from "A mess." from J2SEProjectProperties)"
         */
        public void read() {
        //Map<String/*|null*/,Map<String,String>> readRunConfigs() {
            reset();
            // read project properties
            readDefaultConfig(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            // overwrite by project private properties
            readDefaultConfig(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            // set properties that were not set but should have a value
            addDefaultsIfMissing();
            // add project properties read from config files
            readNonDefaultConfigs(PROJECT_CONFIGS_DIR, true);
            // add/overwrite project properties read from private config files
            readNonDefaultConfigs(PROJECT_PRIVATE_CONFIGS_DIR, false);
        }
        
        private void readDefaultConfig(String propsFile) {
            EditableProperties ep = null;
            try {
                ep = readFromFile(project, propsFile);
            } catch (IOException ex) {
                // can be ignored
            }
            if(ep != null) {
                for (String prop : PROJECT_PROPERTIES) {
                    String v = ep.getProperty(prop);
                    if (v != null) {
                        setDefaultProperty(prop, v);
                    }
                }
            }
            extractDefaultParams(ep);
        }
        
        private void addDefaultsIfMissing() {
            if(!isDefaultPropertySet(RUN_APP_WIDTH)) {
                setDefaultProperty(RUN_APP_WIDTH, DEFAULT_APP_WIDTH);
            }
            if(!isDefaultPropertySet(RUN_APP_HEIGHT)) {
                setDefaultProperty(RUN_APP_HEIGHT, DEFAULT_APP_HEIGHT);
            }
        }
        
        private void readNonDefaultConfigs(String subDir, boolean createIfNotExists) {
            FileObject configsFO = project.getProjectDirectory().getFileObject(subDir); // NOI18N
            if (configsFO != null) {
                for (FileObject kid : configsFO.getChildren()) {
                    if (!kid.hasExt(PROPERTIES_FILE_EXT)) { // NOI18N
                        continue;
                    }
                    Map<String,String> c = getConfig(kid.getName());
                    if (c == null && !createIfNotExists) {
                        continue;
                    }
                    EditableProperties cep = null;
                    try {
                        cep = readFromFile( project, FileUtil.getRelativePath(project.getProjectDirectory(), kid) );
                    } catch (IOException ex) {
                        // can be ignored
                    }
                    addToConfig(kid.getName(), cep);
                    extractParams(cep, kid.getName());
                }
            }
        }
    
        /**
        * Extract from editable properties all properties depicting application parameters
        * and store them as such in params. If such exist in params, then override their values.
        * 
        * @param ep editable properties to extract from
        * @param params application parameters to add to / update in
        */
        private void extractParams(@NonNull EditableProperties ep, String config) {
            if(ep != null) {
                for(String prop : ep.keySet()) {
                    if(prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[0])) {
                        String name = ep.getProperty(prop);
                        if(name != null) {
                            String propV = prop.replace(APP_PARAM_SUFFIXES[0], APP_PARAM_SUFFIXES[1]);
                            String value = ep.getProperty(propV);
                            if(value != null) {
                                addParam(config, name, value);
                            } else {
                                addParam(config, name);
                            }
                        }
                    }
                }
            }
        }
        
        private void extractDefaultParams(@NonNull EditableProperties ep) {
            extractParams(ep, null);
        }

        private void extractActiveParams(@NonNull EditableProperties ep) {
            extractParams(ep, getActive());
        }

        //----------------------------------------------------------

        public void storeActive() throws IOException {
            String configPath = CONFIG_PROPERTIES_FILE;
            if (active == null) {
                try {
                    deleteFile(project, configPath);
                } catch (IOException ex) {
                }
            } else {
                final EditableProperties configProps = readFromFile(project, configPath);
                configProps.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, active);
                saveToFile(project, configPath, configProps);
            }
        }

        //----------------------------------------------------------
        
        /**
         * Stores/updates configuration properties and parameters to EditableProperties in case of default
         * config, or directly to project properties files in case of non-default configs.
         * (modified from "A royal mess." from J2SEProjectProperties)"
         */
        public void store(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {

            for (String name : PROJECT_PROPERTIES) {
                String value = getDefaultProperty(name);
                updateProperty(name, value, projectProperties, privateProperties, isBoundedToNonemptyProperty(null, name));
            }
            List<String> paramNamesUsed = new ArrayList<String>();
            updateDefaultParamProperties(projectProperties, privateProperties, paramNamesUsed);
            storeDefaultParamsAsCommandLine(privateProperties);

            for (Map.Entry<String,Map<String,String>> entry : RUN_CONFIGS.entrySet()) {
                String config = entry.getKey();
                if (config == null) {
                    continue;
                }
                String sharedPath = getSharedConfigFilePath(config);
                String privatePath = getPrivateConfigFilePath(config);
                Map<String,String> configProps = entry.getValue();
                if (configProps == null) {
                    try {
                        deleteFile(project, sharedPath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                    }
                    try {
                        deleteFile(project, privatePath);
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                    }
                    continue;
                }
                final EditableProperties sharedCfgProps = readFromFile(project, sharedPath);
                final EditableProperties privateCfgProps = readFromFile(project, privatePath);
                boolean privatePropsChanged = false;
                
                for (Map.Entry<String,String> prop : configProps.entrySet()) {
                    String name = prop.getKey();
                    String value = prop.getValue();
                    String defaultValue = getDefaultProperty(name);
                    boolean storeIfEmpty = (defaultValue != null && defaultValue.length() > 0) || isBoundedToNonemptyProperty(config, name);
                    privatePropsChanged |= updateProperty(name, value, sharedCfgProps, privateCfgProps, storeIfEmpty);
                }

                cleanPropertiesIfEmpty(
                        new String[] {MAIN_CLASS, RUN_JVM_ARGS, 
                        PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
                        RUN_APP_WIDTH, RUN_APP_HEIGHT}, config, sharedCfgProps);
                privatePropsChanged |= cleanPropertiesIfEmpty(
                        new String[] {RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH}, config, privateCfgProps);
                privatePropsChanged |= updateParamProperties(config, sharedCfgProps, privateCfgProps, paramNamesUsed);  
                privatePropsChanged |= storeParamsAsCommandLine(config, privateCfgProps);

                saveToFile(project, sharedPath, sharedCfgProps);    //Make sure the definition file is always created, even if it is empty.
                if (privatePropsChanged) {                              //Definition file is written, only when changed
                    saveToFile(project, privatePath, privateCfgProps);
                }
            }
            if(ERASED_CONFIGS != null) {
                for (String entry : ERASED_CONFIGS) {
                    if(!RUN_CONFIGS.containsKey(entry)) {
                        // config has been erased, and has not been recreated
                        String sharedPath = getSharedConfigFilePath(entry);
                        String privatePath = getPrivateConfigFilePath(entry);
                        try {
                            deleteFile(project, sharedPath);
                        } catch (IOException ex) {
                            LOG.log(Level.WARNING, "Failed to delete file: {0}", sharedPath); // NOI18N
                        }
                        try {
                            deleteFile(project, privatePath);
                        } catch (IOException ex) {
                            LOG.log(Level.WARNING, "Failed to delete file: {0}", privatePath); // NOI18N
                        }
                    }
                }
            }
        }

        //----------------------------------------------------------

        /**
        * Updates the value of existing property in editable properties if value differs.
        * If value is not set or is set empty, removes property from editable properties
        * unless storeEmpty==true, in which case the property is preserved and set to empty
        * in editable properties.
        * 
        * @param name property to be updated
        * @param value new property value
        * @param projectProperties project editable properties
        * @param privateProperties private project editable properties
        * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
        * @return true if private properties have been edited
        */
        private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, boolean storeEmpty) {
            boolean changePrivate = PRIVATE_PROPERTIES.contains(name) || privateProperties.containsKey(name);
            EditableProperties ep = changePrivate ? privateProperties : projectProperties;
            if(changePrivate) {
                projectProperties.remove(name);
            }
            if (!Utilities.compareObjects(value, ep.getProperty(name))) {
                if (value != null && (value.length() > 0 || storeEmpty)) {
                    ep.setProperty(name, value);
                } else {
                    ep.remove(name);
                }
                return changePrivate;
            }
            return false;
        }

        /**
        * Updates the value of existing property in editable properties if value differs.
        * If value is not set or is set empty, removes property from editable properties.
        *
        * @param name property to be updated
        * @param value new property value
        * @param projectProperties project editable properties
        * @param privateProperties private project editable properties
        */
        private boolean updateProperty(@NonNull String name, String value, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties) {
            return updateProperty(name, value, projectProperties, privateProperties, false);
        }

        /**
         * If property not present in config configuration, remove it from editable properties.
         * This is to propagate property deletions in config to property files
         * @param name
         * @param config
         * @param ep
         * @return true if properties have been edited
         */
        private boolean cleanPropertyIfEmpty(@NonNull String name, String config, @NonNull EditableProperties ep) {
            if(!isPropertySet(config, name)) {
                ep.remove(name);
                return true;
            }
            return false;
        }

        private boolean cleanPropertiesIfEmpty(@NonNull String[] names, String config, @NonNull EditableProperties ep) {
            boolean updated = false;
            for(String name : names) {
                updated |= cleanPropertyIfEmpty(name, config, ep);
            }
            return updated;
        }
        
        //----------------------------------------------------------

        private boolean isParamNameProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[0]);
        }

        private boolean isParamValueProperty(@NonNull String prop) {
            return prop != null && prop.startsWith(APP_PARAM_PREFIX) && prop.endsWith(APP_PARAM_SUFFIXES[1]);
        }
        
        private String getParamValueProperty(String paramNameProperty) {
            if(paramNameProperty != null && isParamNameProperty(paramNameProperty)) {
                return paramNameProperty.replace(APP_PARAM_SUFFIXES[0], APP_PARAM_SUFFIXES[1]);
            }
            return null;
        }

        private String getParamNameProperty(int index) {
            return APP_PARAM_PREFIX + index + "." + APP_PARAM_SUFFIXES[0]; // NOI18N
        }
        
        private String getParamValueProperty(int index) {
            return APP_PARAM_PREFIX + index + "." + APP_PARAM_SUFFIXES[1]; // NOI18N
        }
        
        private boolean isFreeParamPropertyIndex(int index, @NonNull EditableProperties ep) {
            return !ep.containsKey(getParamNameProperty(index));
        }
        
        private int getFreeParamPropertyIndex(int start, @NonNull EditableProperties ep, @NonNull EditableProperties pep, List<String> paramNamesUsed) {
            int index = (start >= 0) ? start : 0;
            while(index >= 0) {
                if(isFreeParamPropertyIndex(index, ep) && isFreeParamPropertyIndex(index, pep) && (paramNamesUsed == null || !paramNamesUsed.contains(getParamNameProperty(index)))) {
                    break;
                }
                index++;
            }
            return (index >= 0) ? index : 0;
        }

        /**
         * Adds/updates properties representing parameters in editable properties
         * 
         * @param config
         * @param projectProperties
         * @param privateProperties
         * @return 
         */
        private boolean updateParamProperties(String config, @NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, @NonNull List<String> paramNamesUsed) {
            assert !configNameWrong(config);
            boolean privateUpdated = false;
            List<Map<String, String>> reduce = JFXProjectUtils.copyList(getParams(config));
            // remove properties with indexes used before (to be replaced later by new unique property names)
            for(String prop : paramNamesUsed) {
                if(prop != null && prop.length() > 0) {
                    projectProperties.remove(prop);
                    projectProperties.remove(getParamValueProperty(prop));
                    privateProperties.remove(prop);
                    privateProperties.remove(getParamValueProperty(prop));
                }
            }
            // delete those private param properties not present in config and log usage of the remaining
            cleanParamPropertiesIfEmpty(config, privateProperties);
            for(String prop : privateProperties.keySet()) {
                if(isParamNameProperty(prop)) {
                    paramNamesUsed.add(prop);
                }
            }
            // update private properties
            List<Map<String, String>> toEraseList = new LinkedList<Map<String, String>>();
            for(Map<String, String> map : reduce) {
                String name = map.get(APP_PARAM_SUFFIXES[0]);
                String value = map.get(APP_PARAM_SUFFIXES[1]);
                if(updateParamPropertyIfExists(name, value, privateProperties, true)) {
                    toEraseList.add(map);
                    privateUpdated = true;
                }
            }
            for(Map<String, String> toErase : toEraseList) {
                reduce.remove(toErase);
            }
            // delete those nonprivate param properties not present in reduce and log usage of the remaining
            cleanParamPropertiesNotListed(reduce, projectProperties);
            for(String prop : projectProperties.keySet()) {
                if(isParamNameProperty(prop)) {
                    paramNamesUsed.add(prop);
                }
            }
            // now create new nonprivate param properties
            int index = 0;
            for(Map<String, String> map : reduce) {
                String name = map.get(APP_PARAM_SUFFIXES[0]);
                String value = map.get(APP_PARAM_SUFFIXES[1]);
                if(name != null && name.length() > 0 && !updateParamPropertyIfExists(name, value, projectProperties, false)) {
                    index = getFreeParamPropertyIndex(index, projectProperties, privateProperties, paramNamesUsed);
                    exportParamProperty(map, getParamNameProperty(index), getParamValueProperty(index), projectProperties);
                    paramNamesUsed.add(getParamNameProperty(index));
                }
            }
            return privateUpdated;
        }
        
        private boolean updateDefaultParamProperties(@NonNull EditableProperties projectProperties, @NonNull EditableProperties privateProperties, List<String> paramNamesUsed) {
            return updateParamProperties(null, projectProperties, privateProperties, paramNamesUsed);
        }
        
        /**
        * Searches in properties for parameter named 'name'. If found, updates
        * both existing param properties (for 'name' and 'value') and returns
        * true, otherwise returns false.
        * 
        * @param name parameter name
        * @param value parameter value
        * @param properties editable properties in which to search for updateable properties
        * @param storeEmpty true==keep empty properties in editable properties, false==remove empty properties
        * @return true if updated existing property, false otherwise
        */
        private boolean updateParamPropertyIfExists(@NonNull String name, String value, EditableProperties ep, boolean storeEmpty) {
            if(name != null && !name.isEmpty()) {
                for(String prop : ep.keySet()) {
                    if(isParamNameProperty(prop)) {
                        if(isEqual(name, ep.get(prop))) {
                            String propVal = getParamValueProperty(prop);
                            if (value != null && (value.length() > 0 || storeEmpty)) {
                                ep.setProperty(propVal, value);
                            } else {
                                ep.remove(propVal);
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
        * Remove from ep all parameter related properties that represent
        * params not present in config
        * 
        * @param ep editable properties
        */
        private void cleanParamPropertiesIfEmpty(String config, EditableProperties ep) {
            assert !configNameWrong(config);
            List<String> toRemove = new LinkedList<String>();
            for(String prop : ep.keySet()) {
                if(isParamNameProperty(prop)) {
                    String name = ep.get(prop);
                    if(!hasParam(config, name)) {
                        toRemove.add(prop);
                    }
                }
            }
            for(String prop : toRemove) {
                ep.remove(prop);
                ep.remove(getParamValueProperty(prop));
            }
        }
        
        /**
        * Remove from ep all parameter related properties that represent
        * params not present in props
        * 
        * @param ep editable properties
        */
        private void cleanParamPropertiesNotListed(List<Map<String, String>> props, EditableProperties ep) {
            List<String> toRemove = new LinkedList<String>();
            for(String name : ep.keySet()) {
                if(isParamNameProperty(name)) {
                    boolean inProps = false;
                    for(Map<String,String> map : props) {
                        String prop = map.get(APP_PARAM_SUFFIXES[0]);
                        if(isEqual(name, prop)) {
                            inProps = true;
                            break;
                        }
                    }
                    if(!inProps) {
                        toRemove.add(name);
                    }
                }
            }
            for(String prop : toRemove) {
                ep.remove(prop);
                ep.remove(getParamValueProperty(prop));
            }
        }

        /**
        * Store one parameter to editable properties (effectively as two properties,
        * one for name, second for value), index is used to distinguish among
        * parameter-property instances
        * 
        * @param param parameter to be stored in editable properties
        * @param newPropName name of property to store parameter name
        * @param newPropValue name of property to store parameter value
        * @param ep editable properties to which param is to be stored
        */
        private void exportParamProperty(@NonNull Map<String, String> param, String newPropName, String newPropValue, @NonNull EditableProperties ep) {
            String name = param.get(APP_PARAM_SUFFIXES[0]);
            String value = param.get(APP_PARAM_SUFFIXES[1]);
            if(name != null) {
                ep.put(newPropName, name);
                if(value != null && value.length() > 0) {
                    ep.put(newPropValue, value);
                }
            }
        }

        /**
        * Gathers application parameters to one property APPLICATION_ARGS
        * to be passed to run/debug target in build-impl.xml when Run as Standalone
        * 
        * @param config
        * @param ep editable properties to which to store the generated property
        * @return true if properties have been edited
        */
        private boolean storeParamsAsCommandLine(String config, EditableProperties projectProperties) {
            assert !configNameWrong(config);
            String params = getParamsTransparentAsString(config, true);
            if(config != null) {
                if(isEqual(params, getDefaultParamsAsString(true))) {
                    params = null;
                }
            }
            if (!Utilities.compareObjects(params, projectProperties.getProperty(APPLICATION_ARGS))) {
                if (params != null && params.length() > 0) {
                    projectProperties.setProperty(APPLICATION_ARGS, params);
                    projectProperties.setComment(APPLICATION_ARGS, new String[]{"# " + NbBundle.getMessage(JFXProjectProperties.class, "COMMENT_app_args")}, false); // NOI18N
                } else {
                    projectProperties.remove(APPLICATION_ARGS);
                }
                return true;
            }
            return false;
        }

        private boolean storeDefaultParamsAsCommandLine(EditableProperties projectProperties) {
            return storeParamsAsCommandLine(null, projectProperties);
        }

        //----------------------------------------------------------
        
        /**
         * For properties registered in bounded groups special
         * handling is to be followed. Either all bounded properties
         * must exist or none of bounded properties must exist
         * in project configuration. The motivation is to enable
         * treating all Preloader related properties is one pseudo-property
         */
        private class BoundedPropertyGroups {
            
            Map<String, Set<String>> groups = new HashMap<String, Set<String>>();
                        
            public void defineGroup(String groupName, Collection<String> props) {
                Set<String> group = new HashSet<String>();
                group.addAll(props);
                groups.put(groupName, group);
            }
            
            public void clearGroup(String groupName) {
                groups.remove(groupName);
            }
            
            public void clearAllGroups() {
                groups.clear();
            }
            
            /**
             * Returns true if property prop is bound with any other properties
             * @return 
             */
            public boolean isBound(String prop) {
                for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                    Set<String> group = entry.getValue();
                    if(group != null && group.contains(prop) && group.size() > 1) {
                        return true;
                    }
                }
                return false;
            }
            
            /**
             * Returns collection of all properties from any group of which
             * property prop is member. prop is not included in result.
             * @param prop
             * @return 
             */
            public Collection<String> getBoundedProperties(String prop) {
                Set<String> bounded = new HashSet<String>();
                for(Map.Entry<String, Set<String>> entry : groups.entrySet()) {
                    Set<String> group = entry.getValue();
                    if(group != null && group.contains(prop)) {
                        bounded.addAll(group);
                    }
                }
                bounded.remove(prop);
                return bounded;
            }
            
        }

    }
    
}
