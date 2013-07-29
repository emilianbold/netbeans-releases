/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.java.j2seproject.ui.J2SELogicalViewProvider;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.project.ui.spi.TemplateCategorySorter;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MODIFIED;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_PROJECT_XML;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_STYLESHEET;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_UNKNOWN;
import org.netbeans.spi.whitelist.support.WhiteListQueryMergerSupport;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
@AntBasedProjectRegistration(
    type=J2SEProject.TYPE,
    iconResource="org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", // NOI18N
    sharedName=J2SEProject.PROJECT_CONFIGURATION_NAME,
    sharedNamespace= J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateName=J2SEProject.PRIVATE_CONFIGURATION_NAME,
    privateNamespace= J2SEProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class J2SEProject implements Project {

    public static final String TYPE = "org.netbeans.modules.java.j2seproject"; // NOI18N
    static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project/3"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project-private/1"; // NOI18N

    private static final String[] EXTENSIBLE_TARGETS = new String[] {
        "-do-init",             //NOI18N
        "-init-check",          //NOI18N
        "-post-clean",          //NOI18N
        "-pre-pre-compile",     //NOI18N
        "-do-compile",          //NOI18N
        "-do-compile-single",   //NOI18N
        "jar",                  //NOI18N
        "-post-jar",            //NOI18N
        "run",                  //NOI18N
        "debug",                //NOI18N
        "profile",              //NOI18N
    };
    private static final Icon J2SE_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", false); // NOI18N
    private static final Logger LOG = Logger.getLogger(J2SEProject.class.getName());
    private static final RequestProcessor PROJECT_OPENED_RP = new RequestProcessor(J2SEProject.class);

    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    private final UpdateHelper updateHelper;
    private MainClassUpdater mainClassUpdater;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final ClassPathProviderImpl cpProvider;
    private final ClassPathModifier cpMod;
    private final GeneratedFilesInterceptorSupport gfis;

    private AntBuildExtender buildExtender;

    /**
     * @see J2SEProject.ProjectXmlSavedHookImpl#projectXmlSaved()
     */
    private final ThreadLocal<Boolean> projectPropertiesSave;

    public J2SEProject(AntProjectHelper helper) throws IOException {
        this.projectPropertiesSave = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
        this.helper = helper;
        aux = helper.createAuxiliaryConfiguration();
        UpdateProjectImpl updateProject = new UpdateProjectImpl(this, helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        eval = createEvaluator();
        for (int v = 4; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/j2se-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(J2SEProject.class, "J2SEProject.too_new", FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        refHelper = new ReferenceHelper(helper, aux, evaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new J2SEExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);

        this.cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots()); //Does not use APH to get/put properties/cfgdata
        this.cpMod = new ClassPathModifier(this, this.updateHelper, evaluator(), refHelper, null, createClassPathModifierCallback(), null);
        lookup = createLookup(aux, new J2SEProjectOperations(this, updateProject));
        this.gfis = new GeneratedFilesInterceptorSupport(this, genFilesHelper);
    }

    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            @Override
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String[] classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                return classPathProperty[0];
            }

            @Override
            public String getElementName(String classpathProperty) {
                return null;
            }
        };
    }

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public String toString() {
        return "J2SEProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(J2SEConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(J2SEConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH),
                UPDATE_PROPERTIES);
    }
    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;

        @SuppressWarnings("LeakingThisInConstructor")
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (J2SEConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(J2SEConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String,String>emptyMap());
            }
        }
    }

    private static final PropertyProvider UPDATE_PROPERTIES;

    static {
        Map<String, String> defs = new HashMap<String, String>();

        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
        defs.put(ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
        defs.put("javac.test.processorpath", "${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); // NOI18N

        UPDATE_PROPERTIES = PropertyUtils.fixedPropertyProvider(defs);
    }

    public PropertyEvaluator evaluator() {
        assert eval != null;
        return eval;
    }

    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(final AuxiliaryConfiguration aux, final J2SEProjectOperations ops) {
        final FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), J2SEProjectProperties.SOURCE_ENCODING);
        final J2SELogicalViewProvider lvp = new J2SELogicalViewProvider(this, this.updateHelper, evaluator(), refHelper);
        final Lookup base = Lookups.fixed(
            J2SEProject.this,
            QuerySupport.createProjectInformation(updateHelper, this, J2SE_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            refHelper.createSubprojectProvider(),
            lvp,
            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),        
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator()),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery2(evaluator()),
            QuerySupport.createSources(this, helper, evaluator(), getSourceRoots(), getTestSourceRoots(), Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, J2SEProjectProperties.DIST_DIR)),
            QuerySupport.createSharabilityQuery2(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new CoSAwareFileBuiltQueryImpl(QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()), this),
            new RecommendedTemplatesImpl (this.updateHelper),
            ProjectClassPathModifier.extenderForModifier(cpMod),
            buildExtender,
            cpMod,
            ops,
            new J2SEConfigurationProvider(this),
            new J2SEPersistenceProvider(this, cpProvider),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            encodingQuery,
            new J2SEPropertyEvaluatorImpl(evaluator()),
            QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createBinaryForSourceQueryImplementation(this.sourceRoots, this.testRoots, this.helper, this.evaluator()), //Does not use APH to get/put properties/cfgdata
            QuerySupport.createAnnotationProcessingQuery(this.helper, this.evaluator(), ProjectProperties.ANNOTATION_PROCESSING_ENABLED, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
            LookupProviderSupport.createActionProviderMerger(),
            WhiteListQueryMergerSupport.createWhiteListQueryMerger(),
            BrokenReferencesSupport.createReferenceProblemsProvider(helper, refHelper, eval, lvp.getBreakableProperties(), lvp.getPlatformProperties()),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(helper, eval, new PlatformChangedHook(), JavaPlatform.getDefault().getSpecification().getName(), J2SEProjectProperties.JAVA_PLATFORM, J2SEProjectProperties.JAVAC_SOURCE, J2SEProjectProperties.JAVAC_TARGET),
            BrokenReferencesSupport.createProfileProblemProvider(helper, refHelper, eval, J2SEProjectProperties.JAVAC_PROFILE, ProjectProperties.RUN_CLASSPATH, ProjectProperties.ENDORSED_CLASSPATH),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
            new J2SEProjectPlatformImpl(this)
        );
        lookup = base; // in case LookupProvider's call Project.getLookup
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-java-j2seproject/Lookup"); //NOI18N
    }

    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }

    public ClassPathModifier getProjectClassPathModifier () {
        return this.cpMod;
    }

    // Package private methods -------------------------------------------------

    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
       }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }

    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                updateHelper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }


    /**
     * J2SEProjectProperties helper method to notify ProjectXmlSavedHookImpl about customizer save
     * @see J2SEProject.ProjectXmlSavedHookImpl#projectXmlSaved()
     * @param value true = active
     */
    public void setProjectPropertiesSave(boolean value) {
        this.projectPropertiesSave.set(value);
    }

    // Private innerclasses ----------------------------------------------------
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        ProjectXmlSavedHookImpl() {}

        protected void projectXmlSaved() throws IOException {
            //May be called by {@link AuxiliaryConfiguration#putConfigurationFragment}
            //which didn't affect the j2seproject
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        if (updateHelper.isCurrent()) {
                            //Refresh build-impl.xml only for j2seproject/2
                            final int state = genFilesHelper.getBuildScriptState(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,J2SEProject.class.getResource("resources/build-impl.xsl"));   //NOI18N
                            final Boolean projectPropertiesSave = J2SEProject.this.projectPropertiesSave.get();
                            boolean forceRewriteBuildImpl = false;
                            if ((projectPropertiesSave.booleanValue() && (state & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) ||
                                state == (FLAG_UNKNOWN | FLAG_MODIFIED | FLAG_OLD_PROJECT_XML | FLAG_OLD_STYLESHEET)) {  //missing genfiles.properties
                                //When the project.xml was changed from the customizer and the build-impl.xml was modified
                                //move build-impl.xml into the build-impl.xml~ to force regeneration of new build-impl.xml.
                                //Never do this if it's not a customizer otherwise user modification of build-impl.xml will be deleted
                                //when the project is opened.
                                final FileObject projectDir = updateHelper.getAntProjectHelper().getProjectDirectory();
                                final FileObject buildImpl = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                                if (buildImpl  != null) {
                                    final String name = buildImpl.getName();
                                    final String backupext = String.format("%s~",buildImpl.getExt());   //NOI18N
                                    final FileObject oldBackup = buildImpl.getParent().getFileObject(name, backupext);
                                    if (oldBackup != null) {
                                        oldBackup.delete();
                                    }
                                    FileUtil.copyFile(buildImpl, buildImpl.getParent(), name, backupext);
                                    forceRewriteBuildImpl = true;
                                }
                            }
                            if (forceRewriteBuildImpl) {
                                gfis.generateBuildScriptFromStylesheet(
                                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                    J2SEProject.class.getResource("resources/build-impl.xsl"));
                            } else {
                                gfis.refreshBuildScript(
                                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                    J2SEProject.class.getResource("resources/build-impl.xsl"),
                                    false);
                            }
                            gfis.refreshBuildScript(
                                J2SEProjectUtil.getBuildXmlName(J2SEProject.this),
                                J2SEProject.class.getResource("resources/build.xsl"),
                                false);
                        }
                        return null;
                    }});
            } catch (MutexException e) {
                final Exception inner = e.getException();
                throw inner instanceof IOException ? (IOException) inner : new IOException(inner);
            }
        }
    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {}
        private static final String JAX_RPC_NAMESPACE="http://www.netbeans.org/ns/j2se-project/jax-rpc"; //NOI18N
        private static final String JAX_RPC_CLIENTS="web-service-clients"; //NOI18N
        private static final String JAX_RPC_CLIENT="web-service-client"; //NOI18N

        @Override
        protected void projectOpened() {
            // Check up on build scripts.
            try {
                if (updateHelper.isCurrent()) {
                    //Refresh build-impl.xml only for j2seproject/2
                    gfis.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        J2SEProject.class.getResource("resources/build-impl.xsl"),
                        true);
                    gfis.refreshBuildScript(
                        J2SEProjectUtil.getBuildXmlName(J2SEProject.this),
                        J2SEProject.class.getResource("resources/build.xsl"),
                        true);
                }
            } catch (IOException e) {
                LOG.log(
                   Level.INFO,
                   NbBundle.getMessage(J2SEProject.class, "ERR_RegenerateProjectFiles"),
                   e);
            }

            // register project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));

            //register updater of main.class
            //the updater is active only on the opened projects
	    mainClassUpdater = new MainClassUpdater (
                    J2SEProject.this,
                    evaluator(),
                    updateHelper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0],
                    ProjectProperties.MAIN_CLASS);
            mainClassUpdater.start();

            // Make it easier to run headless builds on the same machine at least.
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run () throws IOException {
                        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                            @Override
                            public Void run() {
                                EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                                ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

                                //remove jaxws.endorsed.dir property
                                ep.remove("jaxws.endorsed.dir");

                                // move web-service-clients one level up from in project.xml
                                // WS should be part of auxiliary configuration
                                Element data = helper.getPrimaryConfigurationData(true);
                                NodeList nodes = data.getElementsByTagName(JAX_RPC_CLIENTS);
                                if(nodes.getLength() > 0) {
                                    Element oldJaxRpcClients = (Element) nodes.item(0);
                                    Document doc = createNewDocument();
                                    Element newJaxRpcClients = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENTS);
                                    NodeList childNodes = oldJaxRpcClients.getElementsByTagName(JAX_RPC_CLIENT);
                                    for (int i=0;i<childNodes.getLength();i++) {
                                        Element oldJaxRpcClient = (Element) childNodes.item(i);
                                        Element newJaxRpcClient = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENT);
                                        NodeList nodeProps = oldJaxRpcClient.getChildNodes();
                                        for (int j=0;j<nodeProps.getLength();j++) {
                                            Node n = nodeProps.item(j);
                                            if (n instanceof Element) {
                                                Element oldProp = (Element) n;
                                                Element newProp = doc.createElementNS(JAX_RPC_NAMESPACE, oldProp.getLocalName());
                                                String text = oldProp.getTextContent();
                                                newProp.setTextContent(text);
                                                newJaxRpcClient.appendChild(newProp);
                                            }
                                        }
                                        newJaxRpcClients.appendChild(newJaxRpcClient);
                                    }
                                    aux.putConfigurationFragment(newJaxRpcClients, true);
                                    data.removeChild(oldJaxRpcClients);
                                    helper.putPrimaryConfigurationData(data, true);
                                }

                                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                                ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                if (!ep.containsKey(ProjectProperties.INCLUDES)) {
                                    ep.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
                                }
                                if (!ep.containsKey(ProjectProperties.EXCLUDES)) {
                                    ep.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
                                }
                                if (!ep.containsKey("build.generated.sources.dir")) { // NOI18N
                                    ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
                                }
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                try {
                                    ProjectManager.getDefault().saveProject(J2SEProject.this);
                                } catch (IOException e) {
                                    //#91398 provide a better error message in case of read-only location of project.
                                    if (!J2SEProject.this.getProjectDirectory().canWrite()) {
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(J2SEProject.class, "ERR_ProjectReadOnly",
                                                J2SEProject.this.getProjectDirectory().getName()));
                                        DialogDisplayer.getDefault().notify(nd);
                                    } else {
                                        Exceptions.printStackTrace(e);
                                    }
                                }
                                return null;
                            }
                        });
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            String prop = evaluator().getProperty(J2SEProjectProperties.SOURCE_ENCODING);
            if (prop != null) {
                try {
                    Charset.forName(prop);
                } catch (IllegalCharsetNameException e) {
                    //Broken property, log & ignore
                    LOG.log(Level.WARNING, "Illegal charset: {0} in project: {1}", new Object[]{prop, FileUtil.getFileDisplayName(getProjectDirectory())}); //NOI18N
                }
                catch (UnsupportedCharsetException e) {
                    //todo: Needs UI notification like broken references.
                    LOG.log(Level.WARNING, "Unsupported charset: {0} in project: {1}", new Object[]{prop, FileUtil.getFileDisplayName(getProjectDirectory())}); //NOI18N
                }
            }
            //Update per project CopyLibs if needed
            new UpdateCopyLibs(J2SEProject.this).run();
            
        }

        @Override
        protected void projectClosed() {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // just do if the whole project was not deleted...
                    if (getProjectDirectory().isValid()) {
                        // Probably unnecessary, but just in case:
                        try {
                            ProjectManager.getDefault().saveProject(J2SEProject.this);
                        } catch (IOException e) {
                            if (!J2SEProject.this.getProjectDirectory().canWrite()) {
                                // #91398 - ignore, we already reported on project open.
                                // not counting with someone setting the ro flag while the project is opened.
                            } else {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                    // unregister project's classpaths to GlobalPathRegistry
                    GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
                    GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
                    GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
                    if (mainClassUpdater != null) {
                        mainClassUpdater.stop();
                        mainClassUpdater = null;
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                PROJECT_OPENED_RP.execute(r);
            } else {
                r.run();
            }
        }

    }

    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        @Override
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", evaluator(), "jar", "clean", J2SEProjectProperties.BUILD_SCRIPT), // NOI18N
            };
        }

    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates, TemplateCategorySorter {
        RecommendedTemplatesImpl (UpdateHelper helper) {
            this.helper = helper;
        }

        private UpdateHelper helper;

        // List of primarily supported templates

        private static final String[] APPLICATION_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "web-service-clients",  // NOI18N
            "REST-clients",         // NOI18N
            "wsdl",                 // NOI18N
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] LIBRARY_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            //"gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            "servlet-types-j2se-only",// NOI18N
            "web-service-clients",  // NOI18N
            "REST-clients",         // NOI18N
            "wsdl",                 // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"         // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/Persistence/Entity.java", // NOI18N
            "Templates/Persistence/RelatedCMP", // NOI18N
            "Templates/WebServices/WebServiceClient"   // NOI18N
        };

        private static final Map<String,Integer>  CAT_MAP;
        static {
            final Map<String,Integer> m = new HashMap<>();
            m.put("Classes",0);     //NOI18N
            m.put("GUIForms",1);    //NOI18N
            m.put("Beans",2);       //NOI18N
            m.put("AWTForms",3);    //NOI18N
            m.put("UnitTests",4);   //NOI18N
            CAT_MAP = Collections.unmodifiableMap(m);
        };

        @Override
        public String[] getRecommendedTypes() {            
            return isLibrary() ? LIBRARY_TYPES : APPLICATION_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        @Override
        @NonNull
        public List<DataObject> sort(@NonNull final List<DataObject> original) {
            if (!isLibrary()) {
                return original;
            }
            final List<DataObject> result = new ArrayList<>(Collections.<DataObject>nCopies(CAT_MAP.size(), null));
            for (DataObject dobj : original) {
                final String name = dobj.getName();
                final Integer index = CAT_MAP.get(name);
                if (index == null) {
                    result.add(dobj);
                } else {
                    result.set (index, dobj);
                }
            }
            return filterNulls(result);
        }

        @NonNull
        private List<DataObject> filterNulls(@NonNull final List<DataObject> list) {
            boolean hasNull = false;
            for (int i=0; i<CAT_MAP.size(); i++) {
                if (list.get(i) == null) {
                    hasNull = true;
                    break;
                }
            }
            if (!hasNull) {
                //No copy needed
                return list;
            }
            final List<DataObject> result = new ArrayList<>(list.size());
            for (DataObject dobj : list) {
                if (dobj != null) {
                    result.add(dobj);
                }
            }
            return result;
        }

        private boolean isLibrary() {
            final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            // if the project has no main class, it's not really an application
            return ep.getProperty (ProjectProperties.MAIN_CLASS) == null || "".equals (ep.getProperty (ProjectProperties.MAIN_CLASS)); // NOI18N
        }

    }

    private static final class J2SEPropertyEvaluatorImpl implements J2SEPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public J2SEPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        @Override
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }

    private class J2SEExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        @Override
        public List<String> getExtensibleTargets() {
            return Arrays.asList(EXTENSIBLE_TARGETS);
        }

        @Override
        public Project getOwningProject() {
            return J2SEProject.this;
        }

    }

    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }

    private static final class CoSAwareFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {

        private final FileBuiltQueryImplementation delegate;
        private final J2SEProject project;
        private final AtomicBoolean cosEnabled = new AtomicBoolean();
        private final Map<FileObject, Reference<StatusImpl>> file2Status = new WeakHashMap<FileObject, Reference<StatusImpl>>();

        @SuppressWarnings("LeakingThisInConstructor")
        public CoSAwareFileBuiltQueryImpl(FileBuiltQueryImplementation delegate, J2SEProject project) {
            this.delegate = delegate;
            this.project = project;

            project.evaluator().addPropertyChangeListener(this);

            setCoSEnabledAndXor();
        }

        private synchronized StatusImpl readFromCache(FileObject file) {
            Reference<StatusImpl> r = file2Status.get(file);

            return r != null ? r.get() : null;
        }

        @Override
        public Status getStatus(FileObject file) {
            StatusImpl result = readFromCache(file);

            if (result != null) {
                return result;
            }

            Status status = delegate.getStatus(file);

            if (status == null) {
                return null;
            }

            synchronized (this) {
                StatusImpl foisted = readFromCache(file);

                if (foisted != null) {
                    return foisted;
                }

                file2Status.put(file, new WeakReference<StatusImpl>(result = new StatusImpl(cosEnabled, status)));
            }

            return result;
        }

        boolean setCoSEnabledAndXor() {
            boolean nue = J2SEProjectUtil.isCompileOnSaveEnabled(project);
            boolean old = cosEnabled.getAndSet(nue);

            return old != nue;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!setCoSEnabledAndXor()) {
                return ;
            }

            Collection<Reference<StatusImpl>> toRefresh;

            synchronized (this) {
                toRefresh = new LinkedList<Reference<StatusImpl>>(file2Status.values());
            }

            for (Reference<StatusImpl> r : toRefresh) {
                StatusImpl s = r.get();

                if (s != null) {
                    s.stateChanged(null);
                }
            }
        }

        private static final class StatusImpl implements Status, ChangeListener {

            private final ChangeSupport cs = new ChangeSupport(this);
            private final AtomicBoolean cosEnabled;
            private final Status delegate;

            @SuppressWarnings("LeakingThisInConstructor")
            public StatusImpl(AtomicBoolean cosEnabled, Status delegate) {
                this.cosEnabled = cosEnabled;
                this.delegate = delegate;
                this.delegate.addChangeListener(this);
            }

            @Override
            public boolean isBuilt() {
                return cosEnabled.get() || delegate.isBuilt();
            }

            @Override
            public void addChangeListener(ChangeListener l) {
                cs.addChangeListener(l);
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
                cs.removeChangeListener(l);
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                cs.fireChange();
            }

        }
    }

    private static final class UpdateCopyLibs implements Runnable {

        private static final String LIB_COPY_LIBS = "CopyLibs"; //NOI18N
        private static final String PROP_VERSION = "version";   //NOI18N
        private static final String VOL_CP = "classpath";       //NOI18N

        private final ReferenceHelper refHelper;

        private UpdateCopyLibs(@NonNull final J2SEProject project) {
            this.refHelper = project.refHelper;
        }

        @Override
        public void run() {
            final LibraryManager projLibManager = refHelper.getProjectLibraryManager();
            if (projLibManager == null) {
                return;
            }
            final Library globalCopyLibs = LibraryManager.getDefault().getLibrary(LIB_COPY_LIBS);
            final Library projectCopyLibs = projLibManager.getLibrary(LIB_COPY_LIBS);
            if (globalCopyLibs == null || projectCopyLibs == null) {
                return;
            }
            final String globalStr = globalCopyLibs.getProperties().get(PROP_VERSION);
            if (globalStr == null) {
                return;
            }
            try {
                final SpecificationVersion globalVersion = new SpecificationVersion(globalStr);
                final String projectStr = projectCopyLibs.getProperties().get(PROP_VERSION);
                if (projectStr != null && globalVersion.compareTo(new SpecificationVersion(projectStr)) <= 0) {
                    return;
                }

                final List<URL> content = projectCopyLibs.getContent(VOL_CP);
                projLibManager.removeLibrary(projectCopyLibs);
                final FileObject projLibLoc = URLMapper.findFileObject(projLibManager.getLocation());
                if (projLibLoc != null) {
                    final FileObject libFolder = projLibLoc.getParent();
                    boolean canDelete = libFolder.canWrite();
                    FileObject container = null;
                    for (URL u : content) {
                        FileObject fo = toFile(u);
                        if (fo != null) {
                            canDelete &= fo.canWrite();
                            if (container == null) {
                                container = fo.getParent();
                                canDelete &= container.canWrite();
                                canDelete &= LIB_COPY_LIBS.equals(container.getName());
                                canDelete &= libFolder.equals(container.getParent());
                            } else {
                                canDelete &= container.equals(fo.getParent());
                            }
                        }
                    }
                    if (canDelete && container != null) {
                        container.delete();
                    }
                }
                refHelper.copyLibrary(globalCopyLibs);
                
            } catch (IllegalArgumentException iae) {
                LOG.log(
                    Level.WARNING,
                    "Cannot update {0} due to invalid version.",    //NOI18N
                    projectCopyLibs.getDisplayName());
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @CheckForNull
        private static FileObject toFile(@NonNull final URL url) {
            final URL file = FileUtil.getArchiveFile(url);
            return URLMapper.findFileObject(file != null ? file : url);
        }

    }

    private final class PlatformChangedHook implements BrokenReferencesSupport.PlatformUpdatedCallBack {
        @Override
        public void platformPropertyUpdated(@NonNull final JavaPlatform platform) {
            J2SEProjectPlatformImpl.updateProjectXml(platform, updateHelper);
        }
    }

}
