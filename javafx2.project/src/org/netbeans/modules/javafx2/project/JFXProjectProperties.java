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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class JFXProjectProperties {

    private static final Logger LOG = Logger.getLogger(JFXProjectProperties.class.getName());
    
    public static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N
    public static final String JAVAFX_PRELOADER = "javafx.preloader"; // NOI18N
    public static final String JAVAFX_SWING = "javafx.swing"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE = "javafx.disable.autoupdate"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION = "javafx.disable.autoupdate.notification"; // NOI18N
    public static final String JAVAFX_DISABLE_CONCURRENT_RUNS = "javafx.disable.concurrent.runs"; // NOI18N
    public static final String JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS = "javafx.enable.concurrent.external.runs"; // NOI18N
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
    public static final String APPLICATION_ARGS = JFXProjectConfigurations.APPLICATION_ARGS;
    public static final String APP_PARAM_PREFIX = JFXProjectConfigurations.APP_PARAM_PREFIX;
    public static final String APP_PARAM_SUFFIXES[] = JFXProjectConfigurations.APP_PARAM_SUFFIXES;
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
    
    // Deployment - native packaging
    public static final String JAVAFX_NATIVE_BUNDLING_ENABLED = "javafx.native.bundling.enabled"; //NOI18N
    public static final String JAVAFX_NATIVE_BUNDLING_TYPE = "javafx.native.bundling.type"; //NOI18N

    //
    public static final String RUN_CP = "run.classpath";    //NOI18N
    public static final String BUILD_CLASSES = "build.classes.dir"; //NOI18N
    
    // Deployment - libraries download mode
    public static final String DOWNLOAD_MODE_LAZY_JARS = "download.mode.lazy.jars";   //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_JAR = "download.mode.lazy.jar."; //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_FORMAT = DOWNLOAD_MODE_LAZY_JAR +"%s"; //NOI18N
    
    // Deployment - callbacks
    public static final String JAVASCRIPT_CALLBACK_PREFIX = "javafx.jscallback."; // NOI18N

    // folders and files
    public static final String PROJECT_CONFIGS_DIR = JFXProjectConfigurations.PROJECT_CONFIGS_DIR;
    public static final String PROJECT_PRIVATE_CONFIGS_DIR = JFXProjectConfigurations.PROJECT_PRIVATE_CONFIGS_DIR;
    public static final String PROPERTIES_FILE_EXT = JFXProjectConfigurations.PROPERTIES_FILE_EXT;
    public static final String CONFIG_PROPERTIES_FILE = JFXProjectConfigurations.CONFIG_PROPERTIES_FILE;
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
    
    // Deployment - Native Packaging (JDK 7u6+)
    public enum BundlingType {
        NONE("None"), // NOI18N
        ALL("All"), // NOI18N
        IMAGE("Image"), // NOI18N
        INSTALLER("Installer"); // NOI18N
        private final String propertyValue;
        BundlingType(String propertyValue) {
            this.propertyValue = propertyValue;
        }
        public String getString() {
            return propertyValue;
        }
    }
    boolean nativeBundlingEnabled;
    BundlingType nativeBundlingType;
    public boolean getNativeBundlingEnabled() {
        return nativeBundlingEnabled;
    }
    public void setNativeBundlingEnabled(boolean enabled) {
        this.nativeBundlingEnabled = enabled;
    }
    public BundlingType getNativeBundlingType() {
        return nativeBundlingType;
    }
    public void setNativeBundlingType(BundlingType type) {
        this.nativeBundlingType = type;
    }
    public boolean setNativeBundlingType(String type) {
        for (BundlingType bundleType : BundlingType.values()) {
            if(bundleType.getString().equalsIgnoreCase(type)) {
                this.nativeBundlingType = bundleType;
                return true;
            }
        }
        return false;
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
     * 
     * @deprecated handle cleanup using ProjectCustomizer.Category.setCloseListener instead
     */
    @Deprecated
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
            initNativeBundling(evaluator);
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
    
    public boolean hasPreloaderInAnyConfig() {
        return hasPreloaderInAnyConfig(CONFIGS);
    }
    
    private boolean hasPreloaderInAnyConfig(@NonNull JFXConfigs configs) {
        if(configs != null) {
            for(String config : configs.getConfigNames()) {
                if(isTrue( configs.getProperty(config, PRELOADER_ENABLED))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private PreloaderArtifact getPreloaderArtifactFromConfig(@NonNull JFXConfigs configs, @NonNull String config, boolean transparent) throws IOException {       
        // check records on any type of preloader from config
        if(configs.hasConfig(config)) {
            
            PreloaderArtifact preloader = null;
            if(!isTrue( transparent ? configs.getPropertyTransparent(config, PRELOADER_ENABLED) : configs.getProperty(config, PRELOADER_ENABLED))) {
                return null;
            }
            String prelTypeString = transparent ? configs.getPropertyTransparent(config, PRELOADER_TYPE) : configs.getProperty(config, PRELOADER_TYPE);
            
            String prelProjDir = transparent ? configs.getPropertyTransparent(config, PRELOADER_PROJECT) : configs.getProperty(config, PRELOADER_PROJECT);
            if (prelProjDir != null && isEqualIgnoreCase(prelTypeString, PreloaderSourceType.PROJECT.getString())) {
                FileObject thisProjDir = project.getProjectDirectory();
                FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, prelProjDir);
                File prelProjDirF = (fo == null) ? null : FileUtil.toFile(fo);                
                if( isTrue(transparent ? configs.getPropertyTransparent(config, PRELOADER_ENABLED) : configs.getProperty(config, PRELOADER_ENABLED)) && prelProjDirF != null && prelProjDirF.exists() ) {
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
                String prelJar = transparent ? configs.getPropertyTransparent(config, PRELOADER_JAR_PATH) : configs.getProperty(config, PRELOADER_JAR_PATH);
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
            return preloader;
        }
        return null;
    }
    
    private Set<PreloaderArtifact> getPreloaderArtifactsFromConfigs(@NonNull JFXConfigs configs) throws IOException {       
        Set<PreloaderArtifact> preloaderArtifacts = new HashSet<PreloaderArtifact>();
        // check records on all preloaders from all configurations
        for(String config : configs.getConfigNames()) {
            PreloaderArtifact preloader = getPreloaderArtifactFromConfig(configs, config, false);
            if(preloader != null) {
                preloaderArtifacts.add(preloader);
            }
        }
        return preloaderArtifacts;
    }

    public void updatePreloaderDependencies() {
        try {
            updatePreloaderDependencies(CONFIGS);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void updatePreloaderDependencies(@NonNull final JFXConfigs configs) throws IOException {
        // depeding on the currently (de)selected preloaders update project dependencies,
        // i.e., remove disabled/deleted preloader project dependencies and add enabled/added preloader project dependencies
        Set<PreloaderArtifact> preloaderArtifacts = getPreloaderArtifacts(getProject());
        for(PreloaderArtifact artifact : preloaderArtifacts) {
            artifact.setValid(false);
        }
        final PreloaderArtifact preloaderActive = getPreloaderArtifactFromConfig(configs, configs.getActive(), true);
        // collect all dependencies from any configuration
        for(final String config : configs.getConfigNames()) {
            final PreloaderArtifact preloader = getPreloaderArtifactFromConfig(configs, config, false);
            if(preloader != null) {
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
        // remove all dependencies not-specified in active configuration (and add the active one)
        Set<PreloaderArtifact> toRemove = new HashSet<PreloaderArtifact>();
        for(final PreloaderArtifact artifact : preloaderArtifacts) {
            if(preloaderActive == null || !preloaderActive.equals(artifact)) {
                toRemove.add(artifact);
            }
        }
        if(preloaderActive != null || !toRemove.isEmpty()) {
            final Set<PreloaderArtifact> toRemoveFinal = Collections.unmodifiableSet(toRemove);
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    if(preloaderActive != null) {
                        try {
                            preloaderActive.addDependency();
                        } catch(IOException e) {
                            LOG.log(Level.SEVERE, "Preloader dependency addition failed."); // NOI18N
                        }
                    }
                    try {
                        for(final PreloaderArtifact artifact : toRemoveFinal) {
                            artifact.removeDependency();
                        }
                    } catch(IOException e) {
                        LOG.log(Level.SEVERE, "Preloader dependency removal failed."); // NOI18N
                    }
                }
            });
        }
        // remove from preloaderArtifacts those not more present in any config
        toRemove.clear();
        for(final PreloaderArtifact artifact : preloaderArtifacts) {
            if(!artifact.isValid()) {
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
        // store native bundling info
        editableProps.setProperty(JAVAFX_NATIVE_BUNDLING_ENABLED, nativeBundlingEnabled ? "true" : "false"); //NOI18N
        editableProps.setProperty(JAVAFX_NATIVE_BUNDLING_TYPE, nativeBundlingType.getString().toLowerCase());
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
        
    public void store() throws IOException {
        updatePreloaderDependencies(CONFIGS);
        CONFIGS.storeActive();
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties pep = new EditableProperties(true);
        final FileObject privPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);        
        try {
            final InputStream is = projPropsFO.getInputStream();
            final InputStream pis = privPropsFO.getInputStream();
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
                    try {
                        pep.load(pis);
                    } finally {
                        if (pis != null) {
                            pis.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
        fxPropGroup.store(ep);
        storeRest(ep, pep);
        CONFIGS.store(ep, pep);
        updatePreloaderComment(ep);
        logProps(ep);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
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
    
    private void initNativeBundling(PropertyEvaluator eval) {
        String enabled = eval.getProperty(JAVAFX_NATIVE_BUNDLING_ENABLED);
        String bundleProp = eval.getProperty(JAVAFX_NATIVE_BUNDLING_TYPE);
        nativeBundlingEnabled = isTrue(enabled);
        if(bundleProp == null) {
            nativeBundlingType = BundlingType.NONE;
        } else {
            if(bundleProp.equalsIgnoreCase(BundlingType.ALL.getString())) {
                nativeBundlingType = BundlingType.ALL;
            } else {
                if(bundleProp.equalsIgnoreCase(BundlingType.IMAGE.getString())) {
                    nativeBundlingType = BundlingType.IMAGE;
                } else {
                    if(bundleProp.equalsIgnoreCase(BundlingType.INSTALLER.getString())) {
                        nativeBundlingType = BundlingType.INSTALLER;
                    } else {
                        nativeBundlingType = BundlingType.NONE;
                    }
                }
            }
        }
    }
    
    private boolean isParentOf(File parent, File child) {
        if(parent == null || child == null) {
            return false;
        }
        if(!parent.exists() || !child.exists()) {
            return false;
        }
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject childFO = FileUtil.toFileObject(child);
        return FileUtil.isParentOf(parentFO, childFO);
    }

    private void initResources (final PropertyEvaluator eval, final Project prj, final JFXConfigs configs) {
        final String lz = eval.getProperty(DOWNLOAD_MODE_LAZY_JARS); //old way, when changed rewritten to new
        final String rcp = eval.getProperty(RUN_CP);        
        final String bc = eval.getProperty(BUILD_CLASSES);
        final String runtimePath = eval.getProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_RUNTIME);
        final String sdkPath = eval.getProperty(JavaFXPlatformUtils.PROPERTY_JAVAFX_SDK);
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

        File runtimeF = runtimePath != null ? new File(runtimePath) : null;
        File sdkF = sdkPath != null ? new File(sdkPath) : null;
        final List<File> resFileList = new ArrayList<File>(paths.length);
        for (String p : paths) {
            if (p.startsWith("${") && p.endsWith("}")) {    //NOI18N
                continue;
            }
            final File f = PropertyUtils.resolveFile(prjDir, p);
            if (!f.exists()) {
                continue;
            }
            if (f.equals(mainFile)) {
                continue;
            }
            if (isParentOf(runtimeF, f) || isParentOf(sdkF, f)) {
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
    public class JFXConfigs extends JFXProjectConfigurations {

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
            super(project.getProjectDirectory());
            registerProjectProperties(new String[] {
                ProjectProperties.MAIN_CLASS, MAIN_CLASS, /*APPLICATION_ARGS,*/ RUN_JVM_ARGS, 
                PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
                RUN_WORK_DIR, RUN_APP_WIDTH, RUN_APP_HEIGHT, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_AS});
            registerPrivateProperties(new String[] {
                RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_AS});
            registerStaticProperties(new String[] {
                RUN_AS});
            
            Map<String, String> substituteMissing = new HashMap<String, String>();
            substituteMissing.put(RUN_APP_WIDTH, DEFAULT_APP_WIDTH);
            substituteMissing.put(RUN_APP_HEIGHT, DEFAULT_APP_HEIGHT);
            registerDefaultsIfMissing(substituteMissing);
            
            registerCleanEmptyProjectProperties(new String[] {
                MAIN_CLASS, RUN_JVM_ARGS, 
                PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
                RUN_APP_WIDTH, RUN_APP_HEIGHT});
            registerCleanEmptyPrivateProperties(new String[] {
                RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH});
            
            defineGroup(PRELOADER_GROUP_NAME, getPreloaderProperties());
            defineGroup(BROWSER_GROUP_NAME, getBrowserProperties());
        }
        
    }
    
    static void logProps(EditableProperties ep) {
        LOG.log(Level.INFO, PRELOADER_ENABLED + " = " + (ep.get(PRELOADER_ENABLED)==null ? "null" : ep.get(PRELOADER_ENABLED)));
        LOG.log(Level.INFO, PRELOADER_TYPE + " = " + (ep.get(PRELOADER_TYPE)==null ? "null" : ep.get(PRELOADER_TYPE)));
        LOG.log(Level.INFO, PRELOADER_PROJECT + " = " + (ep.get(PRELOADER_PROJECT)==null ? "null" : ep.get(PRELOADER_PROJECT)));
        LOG.log(Level.INFO, PRELOADER_CLASS + " = " + (ep.get(PRELOADER_CLASS)==null ? "null" : ep.get(PRELOADER_CLASS)));
        LOG.log(Level.INFO, PRELOADER_JAR_FILENAME + " = " + (ep.get(PRELOADER_JAR_FILENAME)==null ? "null" : ep.get(PRELOADER_JAR_FILENAME)));
        LOG.log(Level.INFO, PRELOADER_JAR_PATH + " = " + (ep.get(PRELOADER_JAR_PATH)==null ? "null" : ep.get(PRELOADER_JAR_PATH)));
    }
}
