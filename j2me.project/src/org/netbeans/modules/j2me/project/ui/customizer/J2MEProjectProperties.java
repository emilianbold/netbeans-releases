/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2me.keystore.KeyStoreRepository;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public final class J2MEProjectProperties {

    public static final String PLATFORM_ANT_NAME = "platform.ant.name";  //NOI18N
    public static final String PLATFORM_TYPE_J2ME = "j2me"; //NOI18N
    public static String PLATFORM_SDK = "platform.sdk"; //NOI18N
    public static final String OBFUSCATION_CUSTOM = "obfuscation.custom"; //NOI18N
    public static final String OBFUSCATION_LEVEL = "obfuscation.level"; //NOI18N
    public static final String PROP_RUN_METHOD = "run.method"; //NOI18N
    public static final String PROP_USE_SECURITY_DOMAIN = "run.use.security.domain"; //NOI18N
    public static final String PROP_SECURITY_DOMAIN = "security.domain"; //NOI18N
    public static final String PROP_DEBUGGER_TIMEOUT = "debugger.timeout"; //NOI18N
    public static final String PROP_SIGN_ENABLED = "sign.enabled"; //NOI18N
    public static final String PROP_SIGN_KEYSTORE = "sign.keystore"; //NOI18N
    public static final String PROP_SIGN_ALIAS = "sign.alias"; //NOI18N
    //J2MEAttributesPanel
    public static final String MANIFEST_IS_LIBLET = "manifest.is.liblet"; //NOI18N
    public static final String MANIFEST_MIDLETS = "manifest.midlets"; //NOI18N
    public static final String MANIFEST_OTHERS = "manifest.others"; //NOI18N
    public static final String MANIFEST_MANIFEST = "manifest.manifest"; //NOI18N
    public static final String MANIFEST_JAD = "manifest.jad"; //NOI18N
    public static final String DEPLOYMENT_JARURL = "deployment.jarurl"; //MOI18N
    public static final String DEPLOYMENT_OVERRIDE_JARURL = "deployment.override.jarurl"; //MOI18N
    public static final String PLATFORM_PROFILE = "platform.profile"; //NOI18N

    // MODELS FOR VISUAL CONTROLS
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
    ComboBoxModel JAVAC_PROFILE_MODEL;

    // CustomizerLibraries
    DefaultListModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_PROCESSORPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    DefaultListModel ENDORSED_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    ListCellRenderer JAVAC_PROFILE_RENDERER;
    Document SHARED_LIBRARIES_MODEL;

    // CustomizerCompile
    ButtonModel NO_DEPENDENCIES_MODEL;

    //J2MEObfuscatingPanel
    Document ADDITIONAL_OBFUSCATION_SETTINGS_MODEL;
    BoundedRangeModel OBFUSCATION_LEVEL_MODEL;

    //CustomizerRun
    Map<String, Map<String, String>> RUN_CONFIGS;
    String activeConfig;
    String[] SECURITY_DOMAINS;
    private static final String[] CONFIG_AWARE_PROPERTIES = {
        ProjectProperties.APPLICATION_ARGS,
        PROP_RUN_METHOD,
        PROP_USE_SECURITY_DOMAIN,
        PROP_SECURITY_DOMAIN,
        PROP_DEBUGGER_TIMEOUT
    };

    //J2ME Signing customizer
    JToggleButton.ToggleButtonModel SIGN_ENABLED_MODEL;
    ComboBoxModel SIGN_KEYSTORE_MODEL;
    ComboBoxModel SIGN_ALIAS_MODEL;

    //J2MEAttributesPanel
    ButtonModel DEPLOYMENT_OVERRIDE_JARURL_MODEL;
    Document DEPLOYMENT_JARURL_MODEL;
    J2MEAttributesPanel.StorableTableModel ATTRIBUTES_TABLE_MODEL;
    String[] ATTRIBUTES_PROPERTY_NAMES = {MANIFEST_OTHERS, MANIFEST_JAD, MANIFEST_MANIFEST};

    private final List<ActionListener> optionListeners = new CopyOnWriteArrayList<>();
    private Map<String, String> additionalProperties;
    private String includes, excludes;
    ClassPathSupport cs;

    private StoreGroup privateGroup;
    private StoreGroup projectGroup;

    private final J2MEProject project;

    public J2MEProject getProject() {
        return project;
    }

    public PropertyEvaluator getEvaluator() {
        return project.evaluator();
    }

    /**
     * Creates a new instance of J2MEProjectProperties
     */
    J2MEProjectProperties(@NonNull final J2MEProject project) {
        this.project = project;
        this.cs = new ClassPathSupport(getEvaluator(), project.getReferenceHelper(), project.getHelper(), project.getUpdateHelper(), null);

        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        additionalProperties = new HashMap<>();
        init();
    }

    private void init() {
        PropertyEvaluator evaluator = getEvaluator();
        UpdateHelper updateHelper = getProject().getUpdateHelper();

        CLASS_PATH_LIST_RENDERER = ClassPathListCellRenderer.createClassPathListRenderer(evaluator, project.getProjectDirectory());

        // CustomizerSources
        SOURCE_ROOTS_MODEL = SourceRootsUi.createModel(project.getSourceRoots());
        TEST_ROOTS_MODEL = SourceRootsUi.createModel(project.getTestRoots());
        includes = evaluator.getProperty(ProjectProperties.INCLUDES);
        if (includes == null) {
            includes = "**"; // NOI18N
        }
        excludes = evaluator.getProperty(ProjectProperties.EXCLUDES);
        if (excludes == null) {
            excludes = ""; // NOI18N
        }

        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        JAVAC_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_CLASSPATH)));
        String processorPath = projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH);
        processorPath = processorPath == null ? "${javac.classpath}" : processorPath;
        JAVAC_PROCESSORPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(processorPath));
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_TEST_CLASSPATH)));
        RUN_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.RUN_CLASSPATH)));
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.RUN_TEST_CLASSPATH)));
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH)));
        final Collection<? extends PlatformFilter> filters = project.getLookup().lookupAll(PlatformFilter.class);
        PLATFORM_MODEL = filters == null ? PlatformUiSupport.createPlatformComboBoxModel(evaluator.getProperty(ProjectProperties.PLATFORM_ACTIVE))
                : PlatformUiSupport.createPlatformComboBoxModel(evaluator.getProperty(ProjectProperties.PLATFORM_ACTIVE), filters);
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel(PLATFORM_MODEL, evaluator.getProperty(ProjectProperties.JAVAC_SOURCE), evaluator.getProperty(ProjectProperties.JAVAC_TARGET));
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer();
        JAVAC_PROFILE_MODEL = PlatformUiSupport.createProfileComboBoxModel(JAVAC_SOURCE_MODEL, evaluator.getProperty(ProjectProperties.JAVAC_PROFILE), null);
        JAVAC_PROFILE_RENDERER = PlatformUiSupport.createProfileListCellRenderer();

        SHARED_LIBRARIES_MODEL = new PlainDocument();
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel(evaluator, ProjectProperties.NO_DEPENDENCIES);

        //Obfuscation customizer
        ADDITIONAL_OBFUSCATION_SETTINGS_MODEL = projectGroup.createStringDocument(getEvaluator(), OBFUSCATION_CUSTOM);
        OBFUSCATION_LEVEL_MODEL = ModelHelper.createSliderModel(getEvaluator(), OBFUSCATION_LEVEL, 0, 0, 9);

        RUN_CONFIGS = readRunConfigs();
        activeConfig = evaluator.getProperty("config");
        SECURITY_DOMAINS = readSecurityDomains();

        // Signning customizer
        SIGN_ENABLED_MODEL = projectGroup.createToggleButtonModel(evaluator, PROP_SIGN_ENABLED);
        SIGN_KEYSTORE_MODEL = ModelHelper.createComboBoxModel(evaluator, PROP_SIGN_KEYSTORE, loadKeystores());
        SIGN_ALIAS_MODEL = ModelHelper.createComboBoxModel(evaluator, PROP_SIGN_ALIAS, loadAliases());

        //J2MEAttributesPanel
        DEPLOYMENT_OVERRIDE_JARURL_MODEL = projectGroup.createToggleButtonModel(evaluator, DEPLOYMENT_OVERRIDE_JARURL);
        if (getEvaluator().getProperty(DEPLOYMENT_OVERRIDE_JARURL) == null) {
            DEPLOYMENT_OVERRIDE_JARURL_MODEL.setSelected(false);
        }
        DEPLOYMENT_JARURL_MODEL = projectGroup.createStringDocument(getEvaluator(), DEPLOYMENT_JARURL);
        if (getEvaluator().getProperty(DEPLOYMENT_JARURL) == null) {
            try {
                DEPLOYMENT_JARURL_MODEL.insertString(0, "${dist.jar}", null); //NOI18N
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        ATTRIBUTES_TABLE_MODEL = new J2MEAttributesPanel.StorableTableModel();

    }

    void collectData() {
    }

    void storeData() {
        try {
            saveLibrariesLocation();
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    storeProperties();
                    return null;
                }
            });
            // and save the project
            ProjectManager.getDefault().saveProject(project);
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private void saveLibrariesLocation() throws IOException, IllegalArgumentException {
        try {
            String str = SHARED_LIBRARIES_MODEL.getText(0, SHARED_LIBRARIES_MODEL.getLength()).trim();
            if (str.length() == 0) {
                str = null;
            }
            String old = project.getHelper().getLibrariesLocation();
            if ((old == null && str == null) || (old != null && old.equals(str))) {
                //ignore, nothing changed..
            } else {
                project.getHelper().setLibrariesLocation(str);
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (BadLocationException x) {
            ErrorManager.getDefault().notify(x);
        }
    }

    private void storeProperties() throws IOException {
        // Store special properties

        // Modify the project dependencies properly        
        resolveProjectDependencies();

        // Encode all paths (this may change the project properties)
        String[] javac_cp = cs.encodeToStrings(ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL));
        String[] javac_pp = cs.encodeToStrings(ClassPathUiSupport.getList(JAVAC_PROCESSORPATH_MODEL));
        String[] javac_test_cp = cs.encodeToStrings(ClassPathUiSupport.getList(JAVAC_TEST_CLASSPATH_MODEL));
        String[] run_cp = cs.encodeToStrings(ClassPathUiSupport.getList(RUN_CLASSPATH_MODEL));
        String[] run_test_cp = cs.encodeToStrings(ClassPathUiSupport.getList(RUN_TEST_CLASSPATH_MODEL));
        String[] endorsed_cp = cs.encodeToStrings(ClassPathUiSupport.getList(ENDORSED_CLASSPATH_MODEL));

        // Store source roots
        storeRoots(project.getSourceRoots(), SOURCE_ROOTS_MODEL);
//        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );

        // Store standard properties
        EditableProperties projectProperties = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = project.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        // Standard store of the properties
        projectGroup.store(projectProperties);
        privateGroup.store(privateProperties);

        //store Signing properties
        if (SIGN_ENABLED_MODEL.isSelected() && SIGN_KEYSTORE_MODEL.getSelectedItem() != null) {
            projectProperties.put(PROP_SIGN_KEYSTORE, ((KeyStoreRepository.KeyStoreBean) SIGN_KEYSTORE_MODEL.getSelectedItem()).getKeyStorePath());
            if (SIGN_ALIAS_MODEL.getSelectedItem() != null) {
                projectProperties.put(PROP_SIGN_ALIAS, ((KeyStoreRepository.KeyStoreBean.KeyAliasBean) SIGN_ALIAS_MODEL.getSelectedItem()).getAlias());
            }
        }
        if (!SIGN_ENABLED_MODEL.isSelected()) {
            projectProperties.remove(PROP_SIGN_ENABLED);
            projectProperties.remove(PROP_SIGN_KEYSTORE);
            projectProperties.remove(PROP_SIGN_ALIAS);
        }

        //Store run configs
        storeRunConfigs(RUN_CONFIGS, projectProperties, privateProperties);
        EditableProperties ep = project.getUpdateHelper().getProperties("nbproject/private/config.properties");
        if (activeConfig == null) {
            ep.remove("config");
        } else {
            ep.setProperty("config", activeConfig);
        }
        project.getUpdateHelper().putProperties("nbproject/private/config.properties", ep);

        // Save all paths
        projectProperties.setProperty(ProjectProperties.JAVAC_CLASSPATH, javac_cp);
        projectProperties.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, javac_pp);
        projectProperties.setProperty(ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp);
        projectProperties.setProperty(ProjectProperties.RUN_CLASSPATH, run_cp);
        projectProperties.setProperty(ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp);
        projectProperties.setProperty(ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp);

        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform(
                projectProperties,
                project.getUpdateHelper(),
                J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,
                PLATFORM_MODEL.getSelectedItem(),
                JAVAC_SOURCE_MODEL.getSelectedItem(),
                JAVAC_PROFILE_MODEL.getSelectedItem(),
                true);

        // Handle other special cases
        if (NO_DEPENDENCIES_MODEL.isSelected()) { // NOI18N
            projectProperties.remove(ProjectProperties.NO_DEPENDENCIES); // Remove the property completely if not set
        }

        projectProperties.putAll(additionalProperties);

        projectProperties.put(ProjectProperties.INCLUDES, includes);
        projectProperties.put(ProjectProperties.EXCLUDES, excludes);

        //J2MEAttributesPanel
        projectProperties.put(DEPLOYMENT_OVERRIDE_JARURL, Boolean.toString(DEPLOYMENT_OVERRIDE_JARURL_MODEL.isSelected()));
        try {
            projectProperties.put(DEPLOYMENT_JARURL, DEPLOYMENT_JARURL_MODEL.getText(0, DEPLOYMENT_JARURL_MODEL.getLength()));
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        Object[] dataDelegates = ATTRIBUTES_TABLE_MODEL.getDataDelegates();
        for (int i = 0; i < dataDelegates.length; i++) {
            projectProperties.put(ATTRIBUTES_PROPERTY_NAMES[i], ATTRIBUTES_TABLE_MODEL.encode(dataDelegates[i]));
        }

        //Obfusation properties
        projectProperties.put(OBFUSCATION_LEVEL, String.valueOf(OBFUSCATION_LEVEL_MODEL.getValue()));
        projectGroup.store(projectProperties);

        // Store the property changes into the project
        project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        project.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        String value = additionalProperties.get(ProjectProperties.SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }

    /**
     * Finds out what are new and removed project dependencies and applyes the
     * info to the project
     */
    private void resolveProjectDependencies() {

        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<>();
        EditableProperties projectProperties = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.JAVAC_CLASSPATH)));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH)));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.JAVAC_TEST_CLASSPATH)));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.RUN_CLASSPATH)));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.RUN_TEST_CLASSPATH)));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH)));

        Set<ClassPathSupport.Item> newArtifacts = new HashSet<>();
        newArtifacts.addAll(ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList(JAVAC_PROCESSORPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList(JAVAC_TEST_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList(RUN_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList(RUN_TEST_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList(ENDORSED_CLASSPATH_MODEL));

        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<>(oldArtifacts);
        removed.removeAll(newArtifacts);
        Set<ClassPathSupport.Item> added = new HashSet<>(newArtifacts);
        added.removeAll(oldArtifacts);

        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for (ClassPathSupport.Item item : removed) {
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT
                    || item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                project.getReferenceHelper().destroyReference(item.getReference());
                if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                    item.removeSourceAndJavadoc(project.getUpdateHelper());
                }
            }
        }

        boolean changed = false;
        // 2. now read project.properties and modify rest
        EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        for (ClassPathSupport.Item item : removed) {
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = item.getReference();
                prop = CommonProjectUtils.getAntPropertyName(prop);
                ep.remove(prop);
                changed = true;
            }
        }
        if (changed) {
            project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }

    private void storeRoots(SourceRoots roots, DefaultTableModel tableModel) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String[] rootLabels = new String[data.size()];
        final LinkedList<URL> oldRootURLs = new LinkedList<>(Arrays.asList(roots.getRootURLs(false)));
        final LinkedList<String> oldRootLabels = new LinkedList<>(Arrays.asList(roots.getRootNames()));
        final LinkedList<String> oldRootProps = new LinkedList<>(Arrays.asList(roots.getRootProperties()));
        boolean rootsAreSame = true;
        for (int i = 0; i < data.size(); i++) {
            File f = (File) ((Vector) data.elementAt(i)).elementAt(0);
            rootURLs[i] = Utilities.toURI(f).toURL();
            if (!rootURLs[i].toExternalForm().endsWith("/")) {  //NOI18N
                rootURLs[i] = new URL(rootURLs[i] + "/");
            }
            validateURL(rootURLs[i], f);
            rootLabels[i] = (String) ((Vector) data.elementAt(i)).elementAt(1);
            rootsAreSame &= !oldRootURLs.isEmpty()
                    && oldRootURLs.removeFirst().equals(rootURLs[i])
                    && roots.getRootDisplayName(oldRootLabels.removeFirst(), oldRootProps.removeFirst()).equals(rootLabels[i]);
        }
        if (!rootsAreSame || !oldRootURLs.isEmpty()) {
            roots.putRoots(rootURLs, rootLabels);
        }
    }

    private void validateURL(final URL url, final File file) {
        try {
            final URI uri = url.toURI();
            if (!uri.isAbsolute()) {
                throw new IllegalArgumentException("URI is not absolute: " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
            if (uri.isOpaque()) {
                throw new IllegalArgumentException("URI is not hierarchical: " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
            if (!"file".equals(uri.getScheme())) {
                throw new IllegalArgumentException("URI scheme is not \"file\": " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    @NonNull
    Iterable<? extends ActionListener> getOptionListeners() {
        return optionListeners;
    }

    void addOptionListener(@NonNull final ActionListener al) {
        Parameters.notNull("al", al);   //NOI18N
        optionListeners.add(al);
    }

    void removeOptionListener(@NonNull final ActionListener al) {
        Parameters.notNull("al", al);   //NOI18N
        optionListeners.remove(al);
    }

    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }

    void loadIncludesExcludes(IncludeExcludeVisualizer v) {
        Set<File> roots = new HashSet<>();
        for (DefaultTableModel model : new DefaultTableModel[]{SOURCE_ROOTS_MODEL, TEST_ROOTS_MODEL}) {
            for (Object row : model.getDataVector()) {
                File d = (File) ((Vector) row).elementAt(0);
                if (/* #104996 */d.isDirectory()) {
                    roots.add(d);
                }
            }
        }
        v.setRoots(roots.toArray(new File[roots.size()]));
        v.setIncludePattern(includes);
        v.setExcludePattern(excludes);
    }

    void storeIncludesExcludes(IncludeExcludeVisualizer v) {
        includes = v.getIncludePattern();
        excludes = v.getExcludePattern();
    }

    boolean makeSharable() {
        List<String> libs = new ArrayList<String>();
        List<String> jars = new ArrayList<String>();
        collectLibs(JAVAC_CLASSPATH_MODEL, libs, jars);
        collectLibs(JAVAC_PROCESSORPATH_MODEL, libs, jars);
        collectLibs(JAVAC_TEST_CLASSPATH_MODEL, libs, jars);
        collectLibs(RUN_CLASSPATH_MODEL, libs, jars);
        collectLibs(RUN_TEST_CLASSPATH_MODEL, libs, jars);
        collectLibs(ENDORSED_CLASSPATH_MODEL, libs, jars);
        libs.add("CopyLibs"); // #132201 - copylibs is integral part of j2seproject
        String customTasksLibs = getProject().evaluator().getProperty(AntBuildExtender.ANT_CUSTOMTASKS_LIBS_PROPNAME);
        if (customTasksLibs != null) {
            String libIDs[] = customTasksLibs.split(",");
            for (String libID : libIDs) {
                libs.add(libID.trim());
            }
        }
        return SharableLibrariesUtils.showMakeSharableWizard(getProject().getHelper(),
                getProject().getReferenceHelper(), libs, jars);
    }

    private void collectLibs(DefaultListModel model, List<String> libs, List<String> jarReferences) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                if (!item.isBroken() && !libs.contains(item.getLibrary().getName())) {
                    libs.add(item.getLibrary().getName());
                }
            }
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null && item.getVariableBasedProperty() == null && !jarReferences.contains(item.getReference())) {
                    //TODO reference is null for not yet persisted items.
                    // there seems to be no way to generate a reference string without actually
                    // creating and writing the property..
                    jarReferences.add(item.getReference());
                }
            }
        }
    }

    public static boolean isTrue(final String value) {
        return value != null
                && (value.equalsIgnoreCase("true") || //NOI18N
                value.equalsIgnoreCase("yes") || //NOI18N
                value.equalsIgnoreCase("on"));     //NOI18N
    }

    public void store() throws IOException {
    }

    private J2MECompilingPanel compilingPanel = null;

    public J2MECompilingPanel getCompilingPanel() {
        if (compilingPanel == null) {
            compilingPanel = new J2MECompilingPanel(this);
        }
        return compilingPanel;
    }

    private J2MEPackagingPanel packagingPanel = null;

    public J2MEPackagingPanel getPackagingPanel() {
        if (packagingPanel == null) {
            packagingPanel = new J2MEPackagingPanel(this);
        }
        return packagingPanel;
    }

    private J2MERunPanel runPanel = null;

    public J2MERunPanel getRunPanel() {
        if (runPanel == null) {
            runPanel = new J2MERunPanel(this);
        }
        return runPanel;
    }

    private J2MEApplicationPanel applicationPanel = null;

    public J2MEApplicationPanel getApplicationPanel() {
        if (applicationPanel == null) {
            applicationPanel = new J2MEApplicationPanel(this);
        }
        return applicationPanel;
    }

    private J2MEDeploymentPanel deploymentPanel = null;

    public J2MEDeploymentPanel getDeploymentPanel() {
        if (deploymentPanel == null) {
            deploymentPanel = new J2MEDeploymentPanel(this);
        }
        return deploymentPanel;
    }

    /**
     * Reads settings for configurations.
     */
    private Map<String/*|null*/, Map<String, String>> readRunConfigs() {
        Map<String, Map<String, String>> m = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        Map<String, String> def = new TreeMap<>();
        for (String prop : CONFIG_AWARE_PROPERTIES) {
            String v = project.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(prop);
            if (v == null) {
                v = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs");
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                final String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                if (relPath != null) {
                    m.put(kid.getName(), new TreeMap<>(project.getUpdateHelper().getProperties(relPath)));
                }
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs");
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                Map<String, String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                final String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                if (relPath != null) {
                    c.putAll(new HashMap<>(project.getUpdateHelper().getProperties(relPath)));
                }
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * Stores settings to configurations.
     */
    private void storeRunConfigs(Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        //System.err.println("storeRunConfigs: " + configs);
        Map<String, String> def = configs.get(null);
        for (String prop : CONFIG_AWARE_PROPERTIES) {
            String v = def.get(prop);
            EditableProperties ep
                    = (prop.equals(ProjectProperties.APPLICATION_ARGS)
                    || prop.equals(ProjectProperties.RUN_WORK_DIR)
                    || privateProperties.containsKey(prop))
                    ? privateProperties : projectProperties;
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
                project.getUpdateHelper().putProperties(sharedPath, null);
                project.getUpdateHelper().putProperties(privatePath, null);
                continue;
            }
            final EditableProperties sharedCfgProps = project.getUpdateHelper().getProperties(sharedPath);
            final EditableProperties privateCfgProps = project.getUpdateHelper().getProperties(privatePath);
            boolean privatePropsChanged = false;
            for (Map.Entry<String, String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                EditableProperties ep
                        = (prop.equals(ProjectProperties.APPLICATION_ARGS)
                        || prop.equals(ProjectProperties.RUN_WORK_DIR)
                        || privateCfgProps.containsKey(prop))
                        ? privateCfgProps : sharedCfgProps;
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (def.get(prop) != null && def.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    privatePropsChanged |= ep == privateCfgProps;
                }
            }
            project.getUpdateHelper().putProperties(sharedPath, sharedCfgProps);    //Make sure the definition file is always created, even if it is empty.
            if (privatePropsChanged) {                              //Definition file is written, only when changed
                project.getUpdateHelper().putProperties(privatePath, privateCfgProps);
            }
        }
    }

    /**
     * Fetches available security domains for selected J2ME device.
     *
     * @return available security domains.
     */
    private String[] readSecurityDomains() {
        String[] securityDomains = new String[0];
        EditableProperties props = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String activePlatform = props.getProperty("platform.active"); //NOI18N
        String activeDevice = props.getProperty("platform.device"); //NOI18N
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().
                getPlatforms(null, new Specification(J2MEPlatform.SPECIFICATION_NAME, new SpecificationVersion("8.0"))); //NOI18N
        if (activePlatform != null && activeDevice != null && platforms != null) {
            for (int i = 0; i < platforms.length; i++) {
                if (platforms[i] instanceof J2MEPlatform) {
                    final J2MEPlatform platform = (J2MEPlatform) platforms[i];
                    if (activePlatform.equals(platform.getProperties().get("platform.ant.name"))) { //NOI18N
                        final J2MEPlatform.Device[] devices = platform.getDevices();
                        if (devices != null) {
                            for (int j = 0; j < devices.length; j++) {
                                final J2MEPlatform.Device device = devices[j];
                                if (activeDevice.equals(device.getName())) {
                                    if (device.getSecurityDomains() != null) {
                                        securityDomains = device.getSecurityDomains();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return securityDomains;
    }

    List<KeyStoreRepository.KeyStoreBean> loadKeystores() {
        return KeyStoreRepository.getDefault().getKeyStores();
    }

    Set<KeyStoreRepository.KeyStoreBean.KeyAliasBean> loadAliases() {
        return ((KeyStoreRepository.KeyStoreBean) SIGN_KEYSTORE_MODEL.getSelectedItem()).aliasses();
    }

    /**
     * Helper class for components models instantiation.
     */
    public static class ModelHelper {

        /**
         * Creates a model for JSlider.
         */
        public static BoundedRangeModel createSliderModel(PropertyEvaluator evaluator, String propertyName, int extent, int minVal, int maxVal) {
            String value = evaluator.getProperty(propertyName);
            if (value == null) {
                value = "0"; // NOI18N
            }

            DefaultBoundedRangeModel model = new DefaultBoundedRangeModel(Integer.valueOf(value), extent, minVal, maxVal);
            return model;
        }

        public static <T> ComboBoxModel<T> createComboBoxModel(PropertyEvaluator evaluator, String propertyName, Collection<T> items) {
            if (items == null || items.isEmpty()) {
                return new DefaultComboBoxModel<>();
            }
            DefaultComboBoxModel model = new DefaultComboBoxModel<>(items.toArray());
            String value = evaluator.getProperty(propertyName);
            Class<?> type = items.toArray()[0].getClass();
            if (value != null) {
                if (type.equals(KeyStoreRepository.KeyStoreBean.class)) {
                    for (Object item : items) {
                        if (value.equals(((KeyStoreRepository.KeyStoreBean) item).getKeyStorePath())) {
                            model.setSelectedItem(item);
                            break;
                        }
                    }
                } else if (type.equals(KeyStoreRepository.KeyStoreBean.KeyAliasBean.class)) {
                    for (Object item : items) {
                        if (value.equals(((KeyStoreRepository.KeyStoreBean.KeyAliasBean) item).getAlias())) {
                            model.setSelectedItem(item);
                            break;
                        }
                    }
                }
            }
            return model;
        }
    }
}
