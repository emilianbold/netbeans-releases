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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender.Item;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ui.StoreGroup;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public abstract class SharedRubyProjectProperties {

    private static final Logger LOGGER = Logger.getLogger(SharedRubyProjectProperties.class.getName());
    
    public static final String MAIN_CLASS = "main.file"; // NOI18N
    public static final String RUBY_OPTIONS = "ruby.options"; // NOI18N
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    public static final String RAKE_ARGS = "rake.args"; // NOI18N
    // if you change the value, update also o.n.m.ruby.RubyParser
    public static final String JVM_ARGS = "jvm.args"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    /**
     * The project property for the RAILS_ENV env variable; defined here instead
     * of RailsProjectProperties as this is needed in RakeRunner.
     */
    public static final String RAILS_ENV = "rails.env"; // NOI18N

    /**
     * Support for odd property name ('run.jvmargs'). Will be dropped in the
     * future
     */
    public static final String RUBY_OPTIONS_DEPRECATED = "run.jvmargs"; // NOI18N

    /**
     * The key for the names of the rake tasks that invoke RSpec tests and should be
     * run using the UI test runner.
     */
    public static final String SPEC_TASKS = "spec.tasks"; //NOI18N

    /**
     * The key for the names of the rake tasks that invoke Test/Unit tests and should be 
     * run using the UI test runner.
     */
    public static final String TEST_TASKS = "test.tasks"; //NOI18N

    // External Java integration
    public DefaultListModel JAVAC_CLASSPATH_MODEL;
    //public ButtonModel INCLUDE_JAVA_MODEL;
    public ListCellRenderer CLASS_PATH_LIST_RENDERER;
    
    private final RubyBaseProject project;
    private RubyPlatform platform;
    protected final PropertyEvaluator evaluator;
    private final UpdateHelper updateHelper;
    private final GeneratedFilesHelper genFileHelper;
    private final ProjectPropertyExtender cs;

    private final StoreGroup privateGroup;
    private final StoreGroup projectGroup;

    private final Map<String, String> additionalProperties;

    private String activeConfig;
    private Map<String, Map<String, String>> runConfigs;

    private List<GemRequirement> gemRequirements;
    private List<GemRequirement> gemRequirementsTests;
    
    public static final String[] WELL_KNOWN_PATHS = new String[]{
        "${" + JAVAC_CLASSPATH + "}", // NOI18N
    };
    public static final String LIBRARY_PREFIX = "${libs."; // NOI18N
    public static final String LIBRARY_SUFFIX = ".classpath}"; // NOI18N
    // XXX looks like there is some kind of API missing in ReferenceHelper?
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    //public abstract DefaultListModel getListModel(String propertyName);
    //public abstract ListCellRenderer getListRenderer(String propertyName);
    
    public SharedRubyProjectProperties(
            final RubyBaseProject project,
            final PropertyEvaluator evaluator,
            final UpdateHelper updateHelper,
            final GeneratedFilesHelper genFileHelper,
            final ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.genFileHelper = genFileHelper;
        this.evaluator = evaluator;
        this.cs = new ProjectPropertyExtender(evaluator, refHelper, updateHelper.getRakeProjectHelper(),
                WELL_KNOWN_PATHS, LIBRARY_PREFIX, LIBRARY_SUFFIX, ANT_ARTIFACT_PREFIX);

        additionalProperties = new HashMap<String, String>();

        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();

        init();
    }

    protected abstract String[] getConfigProperties();
    
    protected abstract String[] getConfigPrivateProperties();

    protected abstract void prePropertiesStore() throws IOException;
    
    protected abstract void storeProperties(
            final EditableProperties projectProperties,
            final EditableProperties privateProperties) throws IOException;

    /** Initializes the visual models/ */
    private void init() {
        CLASS_PATH_LIST_RENDERER = new JavaClassPathUi.ClassPathListCellRenderer(evaluator);

        EditableProperties projectProperties = getUpdateHelper().getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        String cp = projectProperties.get(JAVAC_CLASSPATH);
        JAVAC_CLASSPATH_MODEL = /*ClassPathUiSupport.*/ createListModel(cs.itemsIterator(cp));

        // CustomizerRun
        runConfigs = readRunConfigs();
        activeConfig = evaluator.getProperty("config"); // NOI18N
    }

    protected UpdateHelper getUpdateHelper() {
        return updateHelper;
    }

    protected RubyBaseProject getProject() {
        return project;
    }

    public RubyPlatform getPlatform() {
        return getProject().getPlatform();
    }

    public static String getRubyOptions(final RubyBaseProject project) {
        String options = project.evaluator().getProperty(SharedRubyProjectProperties.RUBY_OPTIONS);

        if (isEmpty(options)) {
            options = null;
        }

        if (options == null) { // support for depreacted 'run.jvmargs' key
            options = project.evaluator().getProperty(SharedRubyProjectProperties.RUBY_OPTIONS_DEPRECATED);
            if (isEmpty(options)) {
                options = null;
            }
        }
        return options;
    }

    public void setPlatform(final RubyPlatform platform) {
        this.platform = platform;
    }

    public static void storePlatform(final EditableProperties ep, final RubyPlatform platform) {
        if (platform == null) {
            LOGGER.fine("Project has invalid platform (null).");
            return;
        }
        ep.setProperty(PLATFORM_ACTIVE, platform.getID());
    }

    public String getActiveConfig() {
        return activeConfig;
    }

    public void setActiveConfig(String activeConfig) {
        this.activeConfig = activeConfig;
    }

    public List<GemRequirement> getGemRequirements() {
        return gemRequirements;
    }

    public void setGemRequirements(List<GemRequirement> gemRequirements) {
        this.gemRequirements = gemRequirements;
    }

    public List<GemRequirement> getGemRequirementsForTests() {
        return gemRequirementsTests;
    }

    public void setGemRequirementsForTests(List<GemRequirement> gemRequirements) {
        this.gemRequirementsTests = gemRequirements;
    }

    public Map<String, Map<String, String>> getRunConfigs() {
        return runConfigs;
    }

    private boolean isPrivateConfigProperty(final String prop) {
        return Arrays.asList(getConfigPrivateProperties()).contains(prop);
    }
    
    protected Map<String, Map<String, String>> readRunConfigs() {
        Map<String, Map<String, String>> m = new TreeMap<String, Map<String, String>>(new Comparator<String>() {

            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        Map<String, String> def = new TreeMap<String, String>();
        for (String prop : getConfigProperties()) {
            String v = getUpdateHelper().getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(prop);
            if (v == null) {
                v = getUpdateHelper().getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(prop);
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
                m.put(kid.getName(), new TreeMap<String, String>(getUpdateHelper().getProperties(FileUtil.getRelativePath(project.getProjectDirectory(), kid))));
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
                c.putAll(new HashMap<String, String>(getUpdateHelper().getProperties(FileUtil.getRelativePath(project.getProjectDirectory(), kid))));
            }
        }
        return m;
    }

    protected void storeRunConfigs(Map<String, Map<String, String>> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        Map<String, String> defaultConf = configs.get(null);
        for (String confProp : getConfigProperties()) {
            String defConfValue = defaultConf.get(confProp);
            EditableProperties ep = isPrivateConfigProperty(confProp) ? privateProperties : projectProperties;
            if (!Utilities.compareObjects(defConfValue, ep.getProperty(confProp))) {
                if (defConfValue != null && defConfValue.length() > 0) {
                    ep.setProperty(confProp, defConfValue);
                } else {
                    ep.remove(confProp);
                }
            }
        }
        for (Map.Entry<String, Map<String, String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config == null) { // default one
                continue;
            }
            String sharedPath = "nbproject/configs/" + config + ".properties"; // NOI18N
            String privatePath = "nbproject/private/configs/" + config + ".properties"; // NOI18N
            Map<String, String> c = entry.getValue();
            if (c == null) {
                updateHelper.putProperties(sharedPath, null);
                updateHelper.putProperties(privatePath, null);
                continue;
            }
            for (Map.Entry<String, String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                String path = isPrivateConfigProperty(prop) ? privatePath : sharedPath;
                EditableProperties ep = updateHelper.getProperties(path);
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (defaultConf.get(prop) != null && defaultConf.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    updateHelper.putProperties(path, ep);
                }
            }
            // Make sure the definition file is always created, even if it is empty.
            updateHelper.putProperties(sharedPath, updateHelper.getProperties(sharedPath));
        }
    }

    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }

    protected static boolean showModifiedMessage(String title) {
        String message = NbBundle.getMessage(SharedRubyProjectProperties.class, "TXT_Regenerate");
        JButton regenerateButton = new JButton(NbBundle.getMessage(SharedRubyProjectProperties.class, "CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(SharedRubyProjectProperties.class, "AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[]{regenerateButton, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }

    private void storeCommonProperties() throws IOException {
        // Encode all paths (this may change the project properties)
        String[] javac_cp = cs.encodeToStrings(/*ClassPathUiSupport.*/getIterator(JAVAC_CLASSPATH_MODEL));

        prePropertiesStore();

        // Store standard properties
        EditableProperties projectProperties = getUpdateHelper().getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = getUpdateHelper().getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);

        storeProperties(projectProperties, privateProperties);
        
        SharedRubyProjectProperties.storePlatform(privateProperties, getPlatform());

        // Standard store of the properties
        projectGroup.store(projectProperties);
        privateGroup.store(privateProperties);

        storeRunConfigs(runConfigs, projectProperties, privateProperties);
        EditableProperties configProperties = getUpdateHelper().getProperties("nbproject/private/config.properties"); // NOI18N
        if (activeConfig == null) {
            configProperties.remove("config"); // NOI18N
        } else {
            configProperties.setProperty("config", activeConfig); // NOI18N
        }
        getUpdateHelper().putProperties("nbproject/private/config.properties", configProperties); // NOI18N

        putGemRequirements(projectProperties);
        // Save all paths
        projectProperties.setProperty(JAVAC_CLASSPATH, javac_cp);

        projectProperties.putAll(additionalProperties);

        // Store the property changes into the project
        getUpdateHelper().putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        getUpdateHelper().putProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        // Ugh - this looks like global clobbering!
        String encoding = additionalProperties.get(SOURCE_ENCODING);
        if (encoding != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(encoding));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }

    private void putGemRequirements(EditableProperties projectProperties) {
        if (gemRequirements == null) {
            projectProperties.remove(RequiredGems.REQUIRED_GEMS_PROPERTY);
        } else {
            projectProperties.put(RequiredGems.REQUIRED_GEMS_PROPERTY, RequiredGems.asString(gemRequirements));
        }
        if (gemRequirementsTests == null) {
            projectProperties.remove(RequiredGems.REQUIRED_GEMS_TESTS_PROPERTY);
        } else {
            projectProperties.put(RequiredGems.REQUIRED_GEMS_TESTS_PROPERTY, RequiredGems.asString(gemRequirementsTests));
        }
    }

    public void save() {
        try {
            // Store properties 
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {

                final FileObject projectDir = getUpdateHelper().getRakeProjectHelper().getProjectDirectory();

                public Boolean run() throws IOException {
                    if ((genFileHelper.getBuildScriptState(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            project.getClass().getResource("resources/build-impl.xsl")) //NOI18N
                            & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) {  //NOI18N
                        if (showModifiedMessage(NbBundle.getMessage(SharedRubyProjectProperties.class, "TXT_ModifiedTitle"))) {
                            //Delete user modified build-impl.xml
                            FileObject fo = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                            if (fo != null) {
                                fo.delete();
                            }
                        } else {
                            return false;
                        }
                    }
                    storeCommonProperties();
                    return true;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(getProject());
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    // From ClassPathUiSupport:
    private static DefaultListModel createListModel(final Iterator it) {
        DefaultListModel model = new DefaultListModel();
        while (it.hasNext()) {
            model.addElement(it.next());
        }
        return model;
    }

    // From ClassPathUiSupport:
    private static Iterator<Item> getIterator(final DefaultListModel model) {
        // XXX Better performing impl. would be nice
        return getList(model).iterator();
    }

    // From ClassPathUiSupport:
    private static List<Item> getList(final DefaultListModel model) {
        @SuppressWarnings("unchecked")
        List<Item> items = (List<Item>) Collections.list(model.elements());
        return items;
    }

    /** Tests whether the given string is non-null and empty. */
    private static boolean isEmpty(final String options) {
        return options != null && options.trim().length() == 0;
    }
}
