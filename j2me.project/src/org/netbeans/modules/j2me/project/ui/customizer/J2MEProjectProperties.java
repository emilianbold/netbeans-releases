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

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2me.keystore.KeyStoreRepository;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.j2me.project.J2MEProjectUtils;
import org.netbeans.modules.j2me.project.ui.SourceLevelComboBoxModel;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import static org.netbeans.modules.java.api.common.project.ProjectProperties.JAVAC_DEBUG;
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
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
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
    //J2MEObfuscatingPanel
    public static final String OBFUSCATION_CUSTOM = "obfuscation.custom"; //NOI18N
    public static final String OBFUSCATION_LEVEL = "obfuscation.level"; //NOI18N
    //J2MERunPanel
    public static final String PROP_RUN_METHOD = "run.method"; //NOI18N
    public static final String PROP_DEBUGGER_TIMEOUT = "debugger.timeout"; //NOI18N
    //J2MESigningPanel
    public static final String PROP_SIGN_ENABLED = "sign.enabled"; //NOI18N
    public static final String PROP_SIGN_KEYSTORE = "sign.keystore"; //NOI18N
    public static final String PROP_SIGN_ALIAS = "sign.alias"; //NOI18N
    //J2MEAttributesPanel
    public static final String MANIFEST_IS_LIBLET = "manifest.is.liblet"; //NOI18N
    public static final String MANIFEST_OTHERS = "manifest.others"; //NOI18N
    public static final String MANIFEST_MANIFEST = "manifest.manifest"; //NOI18N
    public static final String MANIFEST_JAD = "manifest.jad"; //NOI18N
    public static final String DEPLOYMENT_JARURL = "deployment.jarurl"; //MOI18N
    public static final String DEPLOYMENT_OVERRIDE_JARURL = "deployment.override.jarurl"; //MOI18N
    public static final String PLATFORM_PROFILE = "platform.profile"; //NOI18N
    //J2MEMIDletsPanel
    public static final String MANIFEST_MIDLETS = "manifest.midlets"; //NOI18N
    public static final String DIST_JAD = "dist.jad";   //NOI18N
    public static final String DIST_JAR_FILE = "dist.jar.file"; //NOI18N
    //J2MEAPIPermissionsPanel
    public static final String MANIFEST_APIPERMISSIONS = "manifest.apipermissions"; //NOI18N
    public static final String MANIFEST_APIPERMISSIONS_CLASSES = "manifest.apipermissions.classes"; //NOI18N
    //J2MEPushRegistryPanel
    public static final String MANIFEST_PUSHREGISTRY = "manifest.pushregistry"; //NOI18N
    //PlatformDevicesPanel
    public static final String PROP_PLATFORM_TYPE = "platform.type"; //NOI18N
    public static final String PROP_PLATFORM_DEVICE = "platform.device"; //NOI18N
    public static final String PROP_PLATFORM_CONFIGURATION = "platform.configuration"; //NOI18N
    public static final String PROP_PLATFORM_PROFILE = "platform.profile"; //NOI18N
    public static final String PROP_PLATFORM_APIS = "platform.apis"; //NOI18N
    public static final String PROP_PLATFORM_BOOTCLASSPATH = "platform.bootcp"; //NOI18N
    public static final String PLATFORM_HOME = "platform.home"; //NOI18N
    //Liblets in J2MECompilingPanel
    public static final String PROP_LIBLET_PREFIX = "liblets."; //NOI18N
    public static final String PROP_LIBLET_DEPENDENCY = ".dependency"; //NOI18N
    public static final String PROP_LIBLET_URL = ".url"; //NOI18N
    public static final String PROP_LIBLET_EXTRACT = ".extract"; //NOI18N

    private static final Integer BOOLEAN_KIND_TF = new Integer(0);
    private static final Integer BOOLEAN_KIND_YN = new Integer(1);
    private static final Integer BOOLEAN_KIND_ED = new Integer(2);

    private static final ThreadLocal<Boolean> propertiesSave = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    private static final ThreadLocal<List<Runnable>> postSaveAction = new ThreadLocal<List<Runnable>>() {
        @Override
        protected List<Runnable> initialValue() {
            return new ArrayList<>();
        }
    };
    // MODELS FOR VISUAL CONTROLS
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    ComboBoxModel SOURCE_LEVEL_MODEL;

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
    Document SHARED_LIBRARIES_MODEL;
    DefaultComboBoxModel LIBLETS_MODEL;

    //J2MEObfuscatingPanel
    Document ADDITIONAL_OBFUSCATION_SETTINGS_MODEL;
    BoundedRangeModel OBFUSCATION_LEVEL_MODEL;

    //Configurations-related fields
    public Map<String, Map<String, String>> RUN_CONFIGS;
    public String activeConfig;
    public DefaultComboBoxModel CONFIGS_MODEL = new DefaultComboBoxModel(new String[]{"<default>"});
    private static final String[] CONFIG_AWARE_PROPERTIES = {
        ProjectProperties.APPLICATION_ARGS,
        PROP_RUN_METHOD,
        PROP_DEBUGGER_TIMEOUT,
        PROP_PLATFORM_DEVICE,
        PROP_PLATFORM_CONFIGURATION,
        PROP_PLATFORM_PROFILE,
        PROP_PLATFORM_APIS,
        PROP_PLATFORM_BOOTCLASSPATH,
        MANIFEST_OTHERS,
        MANIFEST_JAD,
        MANIFEST_MANIFEST
    };

    //PlatformDevicesPanel
    public ComboBoxModel J2ME_PLATFORM_MODEL;
    public ComboBoxModel JDK_PLATOFRM_MODEL;
    private HashMap<String, J2MEPlatform.J2MEProfile> name2ProfileMap;

    //J2ME Signing customizer
    JToggleButton.ToggleButtonModel SIGN_ENABLED_MODEL;
    ComboBoxModel SIGN_KEYSTORE_MODEL;
    ComboBoxModel SIGN_ALIAS_MODEL;

    //J2MEAttributesPanel
    ButtonModel DEPLOYMENT_OVERRIDE_JARURL_MODEL;
    Document DEPLOYMENT_JARURL_MODEL;
    J2MEAttributesPanel.StorableTableModel ATTRIBUTES_TABLE_MODEL;
    String[] ATTRIBUTES_PROPERTY_NAMES = {MANIFEST_OTHERS, MANIFEST_JAD, MANIFEST_MANIFEST};

    //J2MEMIDletsPanel
    public J2MEMIDletsPanel.MIDletsTableModel MIDLETS_TABLE_MODEL;
    public String[] MIDLETS_PROPERTY_NAMES = {MANIFEST_MIDLETS};

    //J2MEAPIPermissionsPanel
    boolean LIBLET_PACKAGING;
    J2MEAPIPermissionsPanel.StorableTableModel API_PERMISSIONS_TABLE_MODEL;
    String[] API_PERMISSIONS_PROPERTY_NAMES = {MANIFEST_APIPERMISSIONS, MANIFEST_APIPERMISSIONS_CLASSES};

    //J2MEPushRegistryPanel
    J2MEPushRegistryPanel.StorableTableModel PUSH_REGISTRY_TABLE_MODEL;
    String[] PUSH_REGISTRY_PROPERTY_NAMES = {MANIFEST_PUSHREGISTRY};

    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL;
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel DO_DEPEND_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL;
    DefaultListModel ANNOTATION_PROCESSORS_MODEL;
    DefaultTableModel PROCESSOR_OPTIONS_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    private Integer javacDebugBooleanKind;

    //J2MEJavadocPanel
    ButtonModel JAVADOC_PRIVATE_MODEL;
    ButtonModel JAVADOC_NO_TREE_MODEL;
    ButtonModel JAVADOC_USE_MODEL;
    ButtonModel JAVADOC_NO_NAVBAR_MODEL;
    ButtonModel JAVADOC_NO_INDEX_MODEL;
    ButtonModel JAVADOC_SPLIT_INDEX_MODEL;
    ButtonModel JAVADOC_AUTHOR_MODEL;
    ButtonModel JAVADOC_VERSION_MODEL;
    Document JAVADOC_WINDOW_TITLE_MODEL;
    ButtonModel JAVADOC_PREVIEW_MODEL;
    Document JAVADOC_ADDITIONALPARAM_MODEL;
    private Integer javadocPreviewBooleanKind;

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
    public J2MEProjectProperties(@NonNull final J2MEProject project) {
        this.project = project;
        this.cs = new ClassPathSupport(getEvaluator(), project.getReferenceHelper(), project.getHelper(), project.getUpdateHelper(), null);

        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        additionalProperties = new HashMap<>();
        init();
    }

    public static boolean isPropertiesSave() {
        return propertiesSave.get();
    }

    public static void postSave(@NonNull final Runnable action) {
        Parameters.notNull("action", action);   //NOI18N
        if (!isPropertiesSave()) {
            throw new IllegalStateException("Not in properties save");  //NOI18N
        }
        List<Runnable> l = postSaveAction.get();
        l.add(action);
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
        LIBLETS_MODEL = createLibletModelFromProps(evaluator);

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

        //J2MERunPanel
        RUN_CONFIGS = readRunConfigs();
        activeConfig = evaluator.getProperty("config");

        //PlatformDevicesPanel
        J2ME_PLATFORM_MODEL = ModelHelper.createComboBoxModel(evaluator, ProjectProperties.PLATFORM_ACTIVE, Arrays.asList(J2MEProjectUtils.readPlatforms()));
        JDK_PLATOFRM_MODEL = loadJdkPlatforms();
        name2ProfileMap = J2MEProjectUtils.getNameToProfileMap();

        // Signning customizer
        SIGN_ENABLED_MODEL = projectGroup.createToggleButtonModel(evaluator, PROP_SIGN_ENABLED);
        SIGN_KEYSTORE_MODEL = ModelHelper.createComboBoxModel(evaluator, PROP_SIGN_KEYSTORE, loadKeystores(), Utilities.toFile(project.getProjectDirectory().toURI()));
        SIGN_ALIAS_MODEL = ModelHelper.createComboBoxModel(evaluator, PROP_SIGN_ALIAS, loadAliases());

        //J2MEAttributesPanel
        DEPLOYMENT_OVERRIDE_JARURL_MODEL = projectGroup.createToggleButtonModel(evaluator, DEPLOYMENT_OVERRIDE_JARURL);
        if (getEvaluator().getProperty(DEPLOYMENT_OVERRIDE_JARURL) == null) {
            DEPLOYMENT_OVERRIDE_JARURL_MODEL.setSelected(false);
        }
        DEPLOYMENT_JARURL_MODEL = projectGroup.createStringDocument(getEvaluator(), DEPLOYMENT_JARURL);
        if (getEvaluator().getProperty(DEPLOYMENT_JARURL) == null) {
            try {
                DEPLOYMENT_JARURL_MODEL.insertString(0, "${dist.jar.file}", null); //NOI18N
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        ATTRIBUTES_TABLE_MODEL = new J2MEAttributesPanel.StorableTableModel(this);
        String libletPropValue = evaluator.getProperty(MANIFEST_IS_LIBLET);
        LIBLET_PACKAGING = libletPropValue != null ? Boolean.valueOf(libletPropValue) : false;

        //J2MEMIDletsPanel
        MIDLETS_TABLE_MODEL = new J2MEMIDletsPanel.MIDletsTableModel(this);

        //J2MEAPIPermissionsPanel
        API_PERMISSIONS_TABLE_MODEL = new J2MEAPIPermissionsPanel.StorableTableModel(this);

        //J2MEPushRegistryPanel
        PUSH_REGISTRY_TABLE_MODEL = new J2MEPushRegistryPanel.StorableTableModel(this);

        // J2MECompilingPanel
        Integer[] kind = new Integer[1];
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVAC_DEPRECATION);
        JAVAC_DEBUG_MODEL = ModelHelper.createToggleButtonModel(evaluator, ProjectProperties.JAVAC_DEBUG, kind);
        javacDebugBooleanKind = kind[0];
        DO_DEPEND_MODEL = privateGroup.createToggleButtonModel(evaluator, ProjectProperties.DO_DEPEND);
        ENABLE_ANNOTATION_PROCESSING_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED);
        ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR);
        String annotationProcessors = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST);
        if (annotationProcessors == null) {
            annotationProcessors = ""; //NOI18N
        }
        ANNOTATION_PROCESSORS_MODEL = ClassPathUiSupport.createListModel(
                (annotationProcessors.length() > 0 ? Arrays.asList(annotationProcessors.split(",")) : Collections.emptyList()).iterator()); //NOI18N
        String processorOptions = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
        if (processorOptions == null) {
            processorOptions = ""; //NOI18N
        }
        PROCESSOR_OPTIONS_MODEL = new DefaultTableModel(new String[][]{}, new String[]{
            NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Processor_Options_Key"), //NOI18N
            NbBundle.getMessage(J2MECompilingPanel.class, "LBL_CustomizeCompile_Processor_Options_Value") //NOI18N
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String option : processorOptions.split("\\s")) { //NOI18N
            if (option.startsWith("-A") && option.length() > 2) { //NOI18N
                int sepIndex = option.indexOf('='); //NOI18N
                String key = null;
                String value = null;
                if (sepIndex == -1) {
                    key = option.substring(2);
                } else if (sepIndex >= 3) {
                    key = option.substring(2, sepIndex);
                    value = (sepIndex < option.length() - 1) ? option.substring(sepIndex + 1) : null;
                }
                PROCESSOR_OPTIONS_MODEL.addRow(new String[]{key, value});
            }
        }
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument(evaluator, ProjectProperties.JAVAC_COMPILERARGS);

        // J2MEJavadocPanel
        JAVADOC_PRIVATE_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_PRIVATE);
        JAVADOC_NO_TREE_MODEL = projectGroup.createInverseToggleButtonModel(evaluator, ProjectProperties.JAVADOC_NO_TREE);
        JAVADOC_USE_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_USE);
        JAVADOC_NO_NAVBAR_MODEL = projectGroup.createInverseToggleButtonModel(evaluator, ProjectProperties.JAVADOC_NO_NAVBAR);
        JAVADOC_NO_INDEX_MODEL = projectGroup.createInverseToggleButtonModel(evaluator, ProjectProperties.JAVADOC_NO_INDEX);
        JAVADOC_SPLIT_INDEX_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_SPLIT_INDEX);
        JAVADOC_AUTHOR_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_AUTHOR);
        JAVADOC_VERSION_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_VERSION);
        JAVADOC_WINDOW_TITLE_MODEL = projectGroup.createStringDocument(evaluator, ProjectProperties.JAVADOC_WINDOW_TITLE);
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        JAVADOC_PREVIEW_MODEL = ModelHelper.createToggleButtonModel(evaluator, ProjectProperties.JAVADOC_PREVIEW, kind);
        javadocPreviewBooleanKind = kind[0];
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument(evaluator, ProjectProperties.JAVADOC_ADDITIONALPARAM);

        //Source Level model (Sources panel)
        SOURCE_LEVEL_MODEL = new SourceLevelComboBoxModel(JDK_PLATOFRM_MODEL, J2ME_PLATFORM_MODEL, evaluator.getProperty(ProjectProperties.JAVAC_SOURCE));
    }

    void collectData() {
    }

    void storeData() {
        propertiesSave.set(Boolean.TRUE);
        try {
            saveLibrariesLocation();
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    storeProperties();
                    try {
                        for (Runnable action : postSaveAction.get()) {
                            try {
                                action.run();
                            } catch (Throwable t) {
                                if (t instanceof ThreadDeath) {
                                    throw (ThreadDeath) t;
                                } else {
                                    Exceptions.printStackTrace(t);
                                }
                            }
                        }
                    } finally {
                        postSaveAction.remove();
                    }
                    return null;
                }
            });
            // and save the project
            ProjectManager.getDefault().saveProject(project);
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            propertiesSave.remove();
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

        // Strore liblet dependencies
        int libletIndex = 0;
        for (libletIndex = 0; libletIndex < LIBLETS_MODEL.getSize(); libletIndex++) {
            LibletInfo li = (LibletInfo) LIBLETS_MODEL.getElementAt(libletIndex);
            String libletDep = li.getType().name().toLowerCase() + ";" + li.getRequirement().name().toLowerCase() + ";" + li.getName() + ";" + li.getVendor() + ";" + li.getVersion(); //NOI18N
            projectProperties.put(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_DEPENDENCY, libletDep);
            if (li.getUrl() != null) {
                projectProperties.put(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_URL, li.getUrl());
            }
            projectProperties.put(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_EXTRACT, String.valueOf(li.isExtractClasses()));
        }
        String nextLiblet = null;
        while ((nextLiblet = project.evaluator().getProperty(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_DEPENDENCY)) != null) {
            projectProperties.remove(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_DEPENDENCY);
            projectProperties.remove(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_URL);
            projectProperties.remove(PROP_LIBLET_PREFIX + libletIndex + PROP_LIBLET_EXTRACT);
            libletIndex++;
        }

        //store Signing properties
        if (SIGN_ENABLED_MODEL.isSelected() && SIGN_KEYSTORE_MODEL.getSelectedItem() != null) {
            final File ksFile = ((KeyStoreRepository.KeyStoreBean) SIGN_KEYSTORE_MODEL.getSelectedItem()).getKeyStoreFile();
            String relativeKsPath = null;
            if (SIGN_KEYSTORE_MODEL.getSelectedItem().equals(SIGN_KEYSTORE_MODEL.getElementAt(0))) {
                final String ksPath = ksFile.getAbsolutePath();
                if (ksPath.startsWith(Places.getUserDirectory().getAbsolutePath())) {
                    relativeKsPath = ksPath.replace(Places.getUserDirectory().getAbsolutePath(), "${netbeans.user}").replace("\\", "/"); //NOI18N
                }
            } else {
                final File prjDir = Utilities.toFile(project.getProjectDirectory().toURI());
                relativeKsPath = PropertyUtils.relativizeFile(prjDir, ksFile);
            }
            projectProperties.put(PROP_SIGN_KEYSTORE, relativeKsPath != null ? relativeKsPath : ksFile.getAbsolutePath());
            if (SIGN_ALIAS_MODEL.getSelectedItem() != null) {
                projectProperties.put(PROP_SIGN_ALIAS, ((KeyStoreRepository.KeyStoreBean.KeyAliasBean) SIGN_ALIAS_MODEL.getSelectedItem()).getAlias());
            }
        }
        if (!SIGN_ENABLED_MODEL.isSelected()) {
            projectProperties.remove(PROP_SIGN_ENABLED);
            projectProperties.remove(PROP_SIGN_KEYSTORE);
            projectProperties.remove(PROP_SIGN_ALIAS);
        }

        //Compute Bootclasspath
        for (Map.Entry<String, Map<String, String>> configEntry : RUN_CONFIGS.entrySet()) {
            Map<String, String> config = configEntry.getValue();
            if (config == null) {
                continue;
            }
            String platformConfiguration = config.get(PROP_PLATFORM_CONFIGURATION) != null ? config.get(PROP_PLATFORM_CONFIGURATION) : RUN_CONFIGS.get(null).get(PROP_PLATFORM_CONFIGURATION);
            String platformProfile = config.get(PROP_PLATFORM_PROFILE) != null ? config.get(PROP_PLATFORM_PROFILE) : RUN_CONFIGS.get(null).get(PROP_PLATFORM_PROFILE);
            String platformApis = config.get(PROP_PLATFORM_APIS) != null ? config.get(PROP_PLATFORM_APIS) : RUN_CONFIGS.get(null).get(PROP_PLATFORM_APIS);
            StringBuilder sbBootCP = new StringBuilder();
            if (name2ProfileMap.get(platformConfiguration) == null || name2ProfileMap.get(platformProfile) == null) {
                //If config or profile is missing, probably platform is broken. Then don't overwrite bootcp.
                continue;
            }
            sbBootCP.append(name2ProfileMap.get(platformConfiguration).getClassPath());
            sbBootCP.append(":").append(name2ProfileMap.get(platformProfile).getClassPath()); //NOI18N
            if (platformApis != null) {
                String[] optionalPackages = platformApis.split(","); //NOI18N
                for (String pkg : optionalPackages) {
                    J2MEPlatform.J2MEProfile profile = name2ProfileMap.get(pkg);
                    if (profile != null) {
                        sbBootCP.append(":").append(profile.getClassPath()); //NOI18N
                    }
                }
            }
            if (configEntry.getKey() == null || !sbBootCP.toString().equals(RUN_CONFIGS.get(null).get(PROP_PLATFORM_BOOTCLASSPATH))) {
                config.put(PROP_PLATFORM_BOOTCLASSPATH, sbBootCP.toString());
            } else {
                config.put(PROP_PLATFORM_BOOTCLASSPATH, null);
            }
        }

        storeAttributesToRunConfigs();

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

        if (SOURCE_LEVEL_MODEL.getSelectedItem() != null) {
            String sourceLevel = ((SourceLevelComboBoxModel.SourceLevel) SOURCE_LEVEL_MODEL.getSelectedItem()).getSourceLevel();
            projectProperties.put(ProjectProperties.JAVAC_SOURCE, sourceLevel);
            projectProperties.put(ProjectProperties.JAVAC_TARGET, sourceLevel);
        }

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

        //J2MEMIDletsPanel
        Object[] dataDelegates = MIDLETS_TABLE_MODEL.getDataDelegates();
        for (int i = 0; i < dataDelegates.length; i++) {
            projectProperties.put(MIDLETS_PROPERTY_NAMES[i], encode(dataDelegates[i]));
        }

        //J2MEAPIPermissionsPanel
        dataDelegates = API_PERMISSIONS_TABLE_MODEL.getDataDelegates();
        for (int i = 0; i < dataDelegates.length; i++) {
            projectProperties.put(API_PERMISSIONS_PROPERTY_NAMES[i], encode(dataDelegates[i]));
        }

        //J2MEPushRegistryPanel
        dataDelegates = PUSH_REGISTRY_TABLE_MODEL.getDataDelegates();
        for (int i = 0; i < dataDelegates.length; i++) {
            projectProperties.put(PUSH_REGISTRY_PROPERTY_NAMES[i], encode(dataDelegates[i]));
        }

        //PlatformAndDevicesPanel
        JavaPlatform selectedPlatform = (JavaPlatform) J2ME_PLATFORM_MODEL.getSelectedItem();
        if (selectedPlatform != null) {
            projectProperties.put(ProjectProperties.PLATFORM_ACTIVE, selectedPlatform.getProperties().get(PLATFORM_ANT_NAME));
            projectProperties.put(PROP_PLATFORM_TYPE, ((J2MEPlatform) selectedPlatform).getType());
        }
        JavaPlatform selectedJdkPlatform = PlatformUiSupport.getPlatform(JDK_PLATOFRM_MODEL.getSelectedItem());
        if (selectedJdkPlatform != null) {
            projectProperties.put(PLATFORM_SDK, selectedJdkPlatform.getProperties().get(PLATFORM_ANT_NAME));
        }

        //Obfusation properties
        projectProperties.put(OBFUSCATION_LEVEL, String.valueOf(OBFUSCATION_LEVEL_MODEL.getValue()));
        projectGroup.store(projectProperties);

        //Save javac.debug
        privateProperties.setProperty(JAVAC_DEBUG, encodeBoolean(JAVAC_DEBUG_MODEL.isSelected(), javacDebugBooleanKind));

        //Save javadoc.preview
        privateProperties.setProperty(ProjectProperties.JAVADOC_PREVIEW, encodeBoolean(JAVADOC_PREVIEW_MODEL.isSelected(), javadocPreviewBooleanKind));

        //Save annotation processors
        StringBuilder sb = new StringBuilder();
        for (Enumeration elements = ANNOTATION_PROCESSORS_MODEL.elements(); elements.hasMoreElements();) {
            sb.append(elements.nextElement());
            if (elements.hasMoreElements()) {
                sb.append(',');
            }
        }
        if (sb.length() > 0) {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, encodeBoolean(false, BOOLEAN_KIND_TF));
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, sb.toString());
        } else {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, encodeBoolean(true, BOOLEAN_KIND_TF));
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        }

        sb = new StringBuilder();
        for (int i = 0; i < PROCESSOR_OPTIONS_MODEL.getRowCount(); i++) {
            String key = (String) PROCESSOR_OPTIONS_MODEL.getValueAt(i, 0);
            String value = (String) PROCESSOR_OPTIONS_MODEL.getValueAt(i, 1);
            sb.append("-A").append(key); //NOI18N
            if (value != null && value.length() > 0) {
                sb.append('=').append(value); //NOI18N
            }
            if (i < PROCESSOR_OPTIONS_MODEL.getRowCount() - 1) {
                sb.append(' '); //NOI18N
            }
        }
        if (sb.length() > 0) {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, sb.toString());
        } else {
            projectProperties.remove(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
        }

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

    Object decode(String raw) {
        try {
            if (raw == null) {
                return null;
            }
            BufferedReader br = new BufferedReader(new StringReader(raw));
            HashMap<String, String> map = new HashMap<>();
            for (;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                int i = line.indexOf(':');
                if (i < 0) {
                    continue;
                }
                map.put(line.substring(0, i), line.substring(i + 1).trim());
            }
            return map;
        } catch (IOException ioe) {
            assert false : ioe;
            return null;
        }
    }

    public String encode(Object val) {
        HashMap<String, String> map = (HashMap<String, String>) val;
        if (map == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (String key : map.keySet()) {
            if (key == null) {
                continue;
            }
            String value = map.get(key);
            if (value == null) {
                continue;
            }
            buffer.append(key).append(": ").append(value).append('\n'); //NOI18N
        }
        return buffer.toString();
    }

    private static String encodeBoolean(boolean value, Integer kind) {
        if (kind == BOOLEAN_KIND_ED) {
            return value ? "on" : "off"; // NOI18N
        } else if (kind == BOOLEAN_KIND_YN) { // NOI18N
            return value ? "yes" : "no";
        } else {
            return value ? "true" : "false"; // NOI18N
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

    private J2MERunPanel runPanel = null;

    public J2MERunPanel getRunPanel() {
        if (runPanel == null) {
            runPanel = new J2MERunPanel(this);
        }
        return runPanel;
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

    public void storeAttributesToRunConfigs() {
        HashMap[] dataDelegatesAttributes = ATTRIBUTES_TABLE_MODEL.getDataDelegates();
        for (int i = 0; i < dataDelegatesAttributes.length; i++) {
            for (Map.Entry<String, Map<String, String>> entry : RUN_CONFIGS.entrySet()) {
                String value = encode(dataDelegatesAttributes[i].get(entry.getKey()));
                if (entry.getKey() != null
                        && RUN_CONFIGS.get(null).get(ATTRIBUTES_PROPERTY_NAMES[i]).equals(value)) {
                    value = null;
                }
                if (entry.getValue() != null) {
                    entry.getValue().put(ATTRIBUTES_PROPERTY_NAMES[i], value);
                }
            }
        }
    }

    List<KeyStoreRepository.KeyStoreBean> loadKeystores() {
        return KeyStoreRepository.getDefault().getKeyStores();
    }

    Set<KeyStoreRepository.KeyStoreBean.KeyAliasBean> loadAliases() {
        return ((KeyStoreRepository.KeyStoreBean) SIGN_KEYSTORE_MODEL.getSelectedItem()).aliasses();
    }

    public void reloadJ2MEPlatforms() {
        J2ME_PLATFORM_MODEL = ModelHelper.createComboBoxModel(project.evaluator(), ProjectProperties.PLATFORM_ACTIVE, Arrays.asList(J2MEProjectUtils.readPlatforms()));
    }

    public ComboBoxModel loadJdkPlatforms() {
        ComboBoxModel model = J2MEProjectUtils.createJDKPlatformComboBoxModel();
        String platformActive = project.evaluator().getProperty(PLATFORM_SDK);
        if (platformActive != null) {
            for (int i = 0; i < model.getSize(); i++) {
                JavaPlatform jp = PlatformUiSupport.getPlatform(model.getElementAt(i));
                if (platformActive.equals(jp.getProperties().get(PLATFORM_ANT_NAME))) {
                    model.setSelectedItem(model.getElementAt(i));
                    break;
                }
            }
        }
        return model;
    }
    
    private static DefaultComboBoxModel<LibletInfo> createLibletModelFromProps(PropertyEvaluator eval) {
        DefaultComboBoxModel<LibletInfo> model = new DefaultComboBoxModel<>();
        int i = 0;
        String dep = null;
        while ((dep = eval.getProperty(PROP_LIBLET_PREFIX + i + PROP_LIBLET_DEPENDENCY)) != null) {
            String url = eval.getProperty(PROP_LIBLET_PREFIX + i + PROP_LIBLET_URL);
            boolean extractClasses = Boolean.parseBoolean(eval.getProperty(PROP_LIBLET_PREFIX + i + PROP_LIBLET_EXTRACT));
            String[] splittedDep = dep.split(";"); //NOI18N
            LibletInfo li = new LibletInfo(
                    LibletInfo.LibletType.valueOf(splittedDep[0].trim().toUpperCase()),
                    splittedDep[2].trim(),
                    splittedDep.length > 3 ? splittedDep[3].trim() : "",
                    splittedDep.length > 4 ? splittedDep[4].trim() : "",
                    LibletInfo.Requirement.valueOf(splittedDep[1].trim().toUpperCase()),
                    url,
                    extractClasses);
            model.addElement(li);

            i++;
        }
        return model;
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
            return createComboBoxModel(evaluator, propertyName, items, null);
        }

        public static <T> ComboBoxModel<T> createComboBoxModel(PropertyEvaluator evaluator, String propertyName, Collection<T> items, @NullAllowed File projectDir) {
            if (items == null || items.isEmpty()) {
                return new DefaultComboBoxModel<>();
            }
            DefaultComboBoxModel model = new DefaultComboBoxModel<>(items.toArray());
            String value = evaluator.getProperty(propertyName);
            Class<?> type = items.toArray()[0].getClass();
            if (value != null) {
                if (type.equals(KeyStoreRepository.KeyStoreBean.class) && projectDir != null) {
                    String absolutePath = PropertyUtils.resolveFile(projectDir, value).getAbsolutePath();
                    for (Object item : items) {
                        if (absolutePath.equals(((KeyStoreRepository.KeyStoreBean) item).getKeyStorePath())) {
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
                } else if (type.equals(J2MEPlatform.class)) {
                    for (Object item : items) {
                        if (value.equals((((JavaPlatform) item).getProperties().get(PLATFORM_ANT_NAME)))) {
                            model.setSelectedItem(item);
                            break;
                        }
                    }
                }
            }
            return model;
        }

        public static JToggleButton.ToggleButtonModel createToggleButtonModel(final PropertyEvaluator evaluator, final String propName, Integer[] kind) {
            assert evaluator != null && propName != null && kind != null && kind.length == 1;
            String value = evaluator.getProperty(propName);
            boolean isSelected = false;
            if (value == null) {
                isSelected = true;
            } else {
                String lowercaseValue = value.toLowerCase();
                switch (lowercaseValue) {
                    case "yes":
                    case "no":// NOI18N
                        kind[0] = BOOLEAN_KIND_YN;
                        break;
                    case "on":
                    case "off":// NOI18N
                        kind[0] = BOOLEAN_KIND_ED;
                        break;
                    default:
                        kind[0] = BOOLEAN_KIND_TF;
                        break;
                }

                if (lowercaseValue.equals("true") || // NOI18N
                        lowercaseValue.equals("yes") || // NOI18N
                        lowercaseValue.equals("on")) {  // NOI18N
                    isSelected = true;
                }
            }
            JToggleButton.ToggleButtonModel bm = new JToggleButton.ToggleButtonModel();
            bm.setSelected(isSelected);
            return bm;
        }
    }

    public abstract static class DataSource {

        private final String propName;
        private final JComboBox<?> configCombo;
        private final Map<String, Map<String, String>> configs;
        private final JComponent label;
        private Font basefont = null;
        private Font boldfont = null;

        public DataSource(
                @NonNull final String propName,
                @NullAllowed final JComponent label,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            Parameters.notNull("propName", propName); //NOI18N
            Parameters.notNull("configCombo", configCombo); //NOI18N
            Parameters.notNull("configs", configs); //NOI18N
            this.propName = propName;
            this.configCombo = configCombo;
            this.configs = configs;
            this.label = label;
            if (label != null) {
                basefont = label.getFont();
                boldfont = basefont.deriveFont(Font.BOLD);
            }
        }

        public final String getPropertyName() {
            return propName;
        }

        public final JComponent getLabel() {
            return label;
        }

        public final void changed(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            if (value != null && config != null && value.equals(configs.get(null).get(propName))) {
                // default value, do not store as such
                value = null;
            }
            configs.get(config).put(propName, value);
            //updateFont(value);
        }

        public final void updateFont(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            String def = configs.get(null).get(propName);
            if (label != null) {
                label.setFont(config != null && !Utilities.compareObjects(
                        value != null ? value : "", def != null ? def : "") ? boldfont : basefont);
            }
        }

        @CheckForNull
        public final String getPropertyValue(
                @NullAllowed String config,
                @NonNull String key) {
            final Map<String, String> m = configs.get(config);
            String v = m.get(key);
            if (v == null) {
                // display default value
                final Map<String, String> def = configs.get(null);
                v = def.get(getPropertyName());
            }
            return v;
        }

        public abstract String getPropertyValue();

        public abstract void update(@NullAllowed String activeConfig);
    }

    public static class ButtonGroupDataSource extends DataSource {

        private final List<AbstractButton> options;

        public ButtonGroupDataSource(
                @NonNull final String propName,
                @NonNull final ButtonGroup group,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            super(propName, null, configCombo, configs);
            Parameters.notNull("group", group); //NOI18N
            options = Collections.list(group.getElements());
            for (final AbstractButton button : options) {
                button.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (button.isSelected()) {
                            changed(getPropertyValue());
                        }
                    }
                });
            }
        }

        @Override
        public final String getPropertyValue() {
            for (AbstractButton button : options) {
                if (button.isSelected()) {
                    return button.getActionCommand();
                }
            }
            return null;
        }

        @Override
        public void update(String activeConfig) {
            String selectedOption = getPropertyValue(activeConfig, getPropertyName());
            if (selectedOption != null) {
                for (AbstractButton button : options) {
                    if (selectedOption.equals(button.getActionCommand())) {
                        button.setSelected(true);
                    }
                }
            }
        }
    }

    public static class ComboDataSource extends DataSource {

        private final JComboBox<?> combo;

        public ComboDataSource(
                @NonNull final String propName,
                @NonNull final JComboBox<J2MEPlatform.Device> combo,
                @NonNull final JComboBox<?> configCombo,
                @NonNull final Map<String, Map<String, String>> configs) {
            super(propName, combo, configCombo, configs);
            Parameters.notNull("combo", combo); //NOI18N
            this.combo = combo;
            this.combo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    changed(getPropertyValue());
                }
            });
        }

        @Override
        public final String getPropertyValue() {
            return combo.getSelectedItem() != null ? combo.getSelectedItem().toString() : null;
        }

        @Override
        public void update(String activeConfig) {
            String currentValue = getPropertyValue(activeConfig, getPropertyName());
            if (currentValue == null) {
                currentValue = "";   //NOI18N
            }
            final ComboBoxModel<?> model = combo.getModel();

            for (int i = 0; i < model.getSize(); i++) {
                final Object itemAt = model.getElementAt(i);
                if (currentValue.equals(itemAt.toString())) {
                    combo.setSelectedItem(itemAt);
                    return;
                }
            }
            if (combo.getModel().getSize() != 0) {
                combo.setSelectedIndex(0);
                changed(getPropertyValue());
            }
        }
    }
}
