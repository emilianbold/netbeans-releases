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

package org.netbeans.modules.j2me.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2me.project.api.PropertyEvaluatorProvider;
import org.netbeans.modules.j2me.project.api.UpdateHelperProvider;
import org.netbeans.modules.j2me.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2me.project.ui.customizer.J2MECompositeCategoryProvider;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.api.common.project.ProjectHooks;
import org.netbeans.modules.java.api.common.project.ProjectOperations;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tomas Zezula
 */
@AntBasedProjectRegistration(
    type=J2MEProject.TYPE,
    iconResource=J2MEProject.ICON,
    sharedNamespace = J2MEProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace= J2MEProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public class J2MEProject implements Project {

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
    };

    public static final String TYPE = "org.netbeans.modules.j2me.project"; //NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2me-embedded-project/1"; //NOI18N
    public static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2me-embedded-project-private/1"; //NOI18N
    static final String ICON = "org/netbeans/modules/j2me/project/ui/resources/j2meProject.gif";    //NOI18N
    private static final String EXTENSION_FOLDER = "org-netbeans-modules-j2me-project";    //NOI18N
    private static final String EXTENSION_POINT = "Projects/"+EXTENSION_FOLDER+"/Lookup";  //NOI18N

    private final AntProjectHelper helper;
    private final UpdateHelper updateHelper;
    private final AuxiliaryConfiguration auxCfg;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final ClassPathProviderImpl cpProvider;
    private final Lookup lkp;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private volatile PlatformListener platformListener;

    @SuppressWarnings("LeakingThisInConstructor")
    public J2MEProject(@NonNull final AntProjectHelper helper) {
        Parameters.notNull("helper", helper);   //NOI18N
        this.helper = helper;
        final J2MEProjectUpdates updates = new J2MEProjectUpdates(helper);
        this.updateHelper = new UpdateHelper(updates, helper);
        this.auxCfg = helper.createAuxiliaryConfiguration();
        this.eval = ProjectConfigurations.createPropertyEvaluator(this, helper);
        this.refHelper = new ReferenceHelper(helper, auxCfg, eval);
        final AntBuildExtender buildExtender = AntBuildExtenderFactory.createAntExtender(
            new J2MEExtenderImplementation(),
            refHelper);
        this.genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        this.sourceRoots = createRoots(false);
        this.testRoots = createRoots(true);
        this.cpProvider = ClassPathProviderImpl.Builder.create(
                helper,
                eval,
                sourceRoots,
                testRoots).
            setBootClasspathProperties(J2MEProjectProperties.PROP_PLATFORM_BOOTCLASSPATH).
            build();        
        this.lkp = createLookup(buildExtender, newProjectOperationsCallback(this, updates));
    }

    @Override
    @NonNull
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    @NonNull
    public Lookup getLookup() {
        return lkp;
    }

    @NonNull
    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }

    @NonNull
    public AntProjectHelper getHelper() {
        return helper;
    }

    @NonNull
    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }

    @NonNull
    public SourceRoots getSourceRoots() {
        return sourceRoots;
    }

    @NonNull
    public SourceRoots getTestRoots() {
        return testRoots;
    }

    @NonNull
    public PropertyEvaluator evaluator() {
        return eval;
    }

    @NonNull
    public ClassPathProviderImpl getClassPathProvider() {
        return cpProvider;
    }

    private void setName(@NonNull final String name) {
        Parameters.notNull("name", name);   //NOI18N
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                final Element data = updateHelper.getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(PROJECT_CONFIGURATION_NAMESPACE, "name");  //NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                updateHelper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    @NonNull
    private SourceRoots createRoots(final boolean tests) {
        return SourceRoots.create(
            updateHelper,
            eval,
            refHelper,
            PROJECT_CONFIGURATION_NAMESPACE,
            tests ? "test-roots" : "source-roots", //NOI18N
            tests,
            tests ? "test.{0}{1}.dir" : "src.{0}{1}.dir"); //NOI18N
    }

    @NonNull
    private Lookup createLookup(
            @NonNull final AntBuildExtender buildExtender,
            @NonNull final ProjectOperations.Callback opsCallback) {
        Parameters.notNull("buildExtender", buildExtender); //NOI18N
        final FileEncodingQueryImplementation encodingQuery =
                QuerySupport.createFileEncodingQuery(eval, ProjectProperties.SOURCE_ENCODING);
        final ClassPathModifier cpMod = new ClassPathModifier(
                this,
                updateHelper,
                eval,
                refHelper,
                null,
                newClassPathModifierCallback(),
                null);
        final Lookup base = Lookups.fixed(
                J2MEProject.this,                                
                auxCfg,
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                refHelper.createSubprojectProvider(),
                new J2MEActionProvider(
                    this,
                    updateHelper,
                    sourceRoots,
                    testRoots),
                QuerySupport.createProjectInformation(helper, this, ImageUtilities.loadImageIcon(ICON, false)),
                encodingQuery,
                QuerySupport.createSourceLevelQuery2(eval, J2MEProjectProperties.PLATFORM_TYPE_J2ME),
                QuerySupport.createSources(
                    this,
                    helper,
                    eval,
                    sourceRoots,
                    testRoots,
                    Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, ProjectProperties.DIST_DIR)),
                QuerySupport.createCompiledSourceForBinaryQuery(
                    helper,
                    eval,
                    sourceRoots,
                    testRoots),
                QuerySupport.createJavadocForBinaryQuery(
                    helper,
                    eval),
                QuerySupport.createUnitTestForSourceQuery(sourceRoots, testRoots),
                QuerySupport.createSharabilityQuery2(helper, eval, sourceRoots, testRoots),
                QuerySupport.createBinaryForSourceQueryImplementation(sourceRoots, testRoots, helper, eval),
                QuerySupport.createAnnotationProcessingQuery(helper, eval,
                        ProjectProperties.ANNOTATION_PROCESSING_ENABLED,
                        ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR,
                        ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS,
                        ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST,
                        ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT,
                        ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
                QuerySupport.createFileBuiltQuery(helper, eval, sourceRoots, testRoots),
                QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
                ProjectHooks.createProjectXmlSavedHookBuilder(eval, updateHelper, genFilesHelper).
                        setBuildImplTemplate(J2MEProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                        setBuildTemplate(J2MEProject.class.getResource("resources/build.xsl")). //NOI18N
                        setOverrideModifiedBuildImplPredicate(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return J2MEProjectProperties.isPropertiesSave();
                            }
                        }).
                        build(),
                UILookupMergerSupport.createProjectOpenHookMerger(
                    ProjectHooks.createProjectOpenedHookBuilder(this, eval, updateHelper, genFilesHelper, cpProvider).
                        addClassPathType(ClassPath.BOOT).
                        addClassPathType(ClassPath.COMPILE).
                        addClassPathType(ClassPath.SOURCE).
                        addOpenPostAction(newStartPlatformListenerAction()).
                        addClosePostAction(newStopPlatformListenerAction()).
                        setBuildImplTemplate(J2MEProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                        setBuildTemplate(J2MEProject.class.getResource("resources/build.xsl")). //NOI18N
                        build()),
                ProjectOperations.createBuilder(this, eval, updateHelper, refHelper, sourceRoots, testRoots).
                        addDataFiles("manifest.mf").    //NOI18N
                        addUpdatedNameProperty(J2MEProjectProperties.DIST_JAR_FILE, "{0}.jar", true).    //NOI18N
                        addUpdatedNameProperty(J2MEProjectProperties.DIST_JAD, "{0}.jad", true).    //NOI18N
                        setCallback(opsCallback).
                        build(),
                new CustomizerProviderImpl(this),
                LogicalViewProviders.createBuilder(this, eval, EXTENSION_FOLDER).
                        build(),
                cpMod,
                ProjectClassPathModifier.extenderForModifier(cpMod),
                buildExtender,
                new BuildArtifacts(helper, eval),
                new Templates(),
                ProjectConfigurations.createConfigurationProviderBuilder(this, eval, updateHelper).
                    addConfigurationsAffectActions(ActionProvider.COMMAND_RUN, ActionProvider.COMMAND_DEBUG).
                    setCustomizerAction(newConfigCustomizerAction()).
                    build(),
                ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
                ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
                LookupMergerSupport.createClassPathProviderMerger(cpProvider),
                LookupMergerSupport.createSFBLookupMerger(),
                LookupMergerSupport.createJFBLookupMerger(),
                LookupProviderSupport.createSourcesMerger(),
                LookupProviderSupport.createActionProviderMerger(),
                UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                UILookupMergerSupport.createRecommendedTemplatesMerger(),
                UILookupMergerSupport.createProjectProblemsProviderMerger(),
                new UHPImpl(),
                new PEPImpl()
        );
        return LookupProviderSupport.createCompositeLookup(base, EXTENSION_POINT);
    }
    

    private ClassPathModifier.Callback newClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            @Override
            public String getClassPathProperty(
                    @NonNull final SourceGroup sg,
                    @NonNull final String type) {
                Parameters.notNull("sg", sg);   //NOI18N
                Parameters.notNull("type", type);  //NOI18N
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

    @NonNull
    private Runnable newConfigCustomizerAction() {
        return new Runnable() {
            @Override
            public void run() {
                J2MEProject.this.getLookup().lookup(CustomizerProviderImpl.class).
                    showCustomizer(J2MECompositeCategoryProvider.RUN, null);
            }
        };
    }

    @NonNull
    private Runnable newStartPlatformListenerAction() {
        return new Runnable() {
            @Override
            public void run() {
                PlatformListener pl = platformListener;
                if (pl == null) {
                    pl = PlatformListener.create(J2MEProject.this);
                    pl.start();
                    platformListener = pl;
                }
            }
        };
    }

    @NonNull
    private Runnable newStopPlatformListenerAction() {
        return new Runnable() {
            @Override
            public void run() {
                PlatformListener pl = platformListener;
                if (pl != null) {
                    pl.stop();
                    platformListener = null;
                }
            }
        };
    }

    @NonNull
    private static ProjectOperations.Callback newProjectOperationsCallback(
        @NonNull final J2MEProject project,
        @NonNull final J2MEProjectUpdates updates) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("updates", updates); //NOI18N
        return new ProjectOperations.Callback() {
            @Override
            public void beforeOperation(@NonNull final ProjectOperations.Callback.Operation operation) {
            }

            @Override
            @SuppressWarnings("fallthrough")
            public void afterOperation(
                    @NonNull final ProjectOperations.Callback.Operation operation,
                    @NonNull final String newName,
                    @NonNull final Pair<File, Project> oldProject) {
                switch (operation) {
                    case COPY:
                        updates.setTransparentUpdate(true);
                    case MOVE:
                    case RENAME:
                        project.setName(newName);
                }
            }
        };
    }

    private class J2MEExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        @Override
        @NonNull
        public List<String> getExtensibleTargets() {
            return Arrays.asList(EXTENSIBLE_TARGETS);
        }

        @Override
        public Project getOwningProject() {
            return J2MEProject.this;
        }
    }

    private final class UHPImpl implements UpdateHelperProvider {
        @Override
        public UpdateHelper getUpdateHelper() {
            return J2MEProject.this.getUpdateHelper();
        }
    }

    private final class PEPImpl implements PropertyEvaluatorProvider {
        @Override
        public PropertyEvaluator getPropertyEvaluator() {
            return J2MEProject.this.evaluator();
        }
    }
}
