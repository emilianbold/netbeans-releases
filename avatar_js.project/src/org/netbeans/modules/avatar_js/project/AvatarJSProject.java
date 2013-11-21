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

package org.netbeans.modules.avatar_js.project;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.avatar_js.project.ui.nodes.AvatarJSLogicalViewProvider;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.api.common.project.ProjectHooks;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Represents an Avatar.js project.
 * 
 * @author Martin
 */
@AntBasedProjectRegistration(
    type=AvatarJSProject.TYPE,
    iconResource=AvatarJSProject.AVATAR_JS_PROJECT_ICON_PATH,
    sharedName=AvatarJSProject.PROJECT_CONFIGURATION_NAME,
    sharedNamespace= AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateName=AvatarJSProject.PRIVATE_CONFIGURATION_NAME,
    privateNamespace= AvatarJSProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public class AvatarJSProject implements Project {
    
    public static final String TYPE = "org.netbeans.modules.avatar_js.project"; // NOI18N
    public static final String ID = "org-netbeans-modules-avatar_js-project"; // NOI18N
    public static final int VERSION = 1;
    static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/avatar-js-project/"+VERSION; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/avatar-js-project-private/1"; // NOI18N
    
    public static final String CONFIG_JS_SOURCE_PATH = "js";            // NOI18N
    public static final String JS_FILE_EXT = ".js";                     // NOI18N
    public static final String AVATAR_JS_JAR_NAME = "avatar-js.jar";    // NOI18N
    public static final String CONFIG_JAVA_SOURCE_PATH = "java/src";    // NOI18N
    
    @org.netbeans.api.annotations.common.StaticResource
    static final String AVATAR_JS_PROJECT_ICON_PATH = "org/netbeans/modules/avatar_js/project/ui/resources/avatarJSProject.png"; // NOI18N
    private static final Icon AVATAR_JS_PROJECT_ICON = ImageUtilities.loadImageIcon(AVATAR_JS_PROJECT_ICON_PATH, false);

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
    
    private static final PropertyProvider UPDATE_PROPERTIES;
    static {
        Map<String, String> defs = new HashMap<>();

        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
        defs.put(ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
        defs.put("javac.test.processorpath", "${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); // NOI18N

        UPDATE_PROPERTIES = PropertyUtils.fixedPropertyProvider(defs);
    }

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final AntBuildExtender buildExtender;
    private final GeneratedFilesHelper genFilesHelper;
    private final AuxiliaryConfiguration aux;
    private Lookup lookup;
    private final UpdateHelper updateHelper;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final ClassPathProviderImpl cpProvider;
    private final ClassPathModifier cpMod;
    
    @NbBundle.Messages({"# {0} - project directory",
                        "AvatarJSProject.too_new=This version of the IDE is too old to read metadata in {0}."})
    public AvatarJSProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        aux = helper.createAuxiliaryConfiguration();
        UpdateProjectImpl updateProject = new UpdateProjectImpl(this, helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        eval = ProjectConfigurations.createPropertyEvaluator(this, helper, UPDATE_PROPERTIES);
        for (int v = VERSION + 1; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/avatar-js-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("Too new"), // NOI18N
                        Bundle.AvatarJSProject_too_new(FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        refHelper = new ReferenceHelper(helper, aux, evaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new AvatarJSExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        
        cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(), getTestSourceRoots()); //Does not use APH to get/put properties/cfgdata
        cpMod = new ClassPathModifier(this, this.updateHelper, evaluator(), refHelper, null, createClassPathModifierCallback(), null);
        lookup = createLookup(aux);
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        assert eval != null;
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }
    
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
       }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }
    
    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }

    public ClassPathModifier getProjectClassPathModifier () {
        return this.cpMod;
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
    
    @Override
    public String toString() {
        return "AvatarJSProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
    }
    
    private Lookup createLookup(final AuxiliaryConfiguration aux) {
        final Lookup base = Lookups.fixed(
            AvatarJSProject.this,
            QuerySupport.createProjectInformation(updateHelper, this, AVATAR_JS_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            /*LogicalViewProviders.createBuilder(
                this,
                eval,
                ID).
                setHelpCtx(new HelpCtx(TYPE+".ui.AvatarJSLogicalViewProvider.AvatarJSLogicalViewRootNode")).    //NOI18N
                //setCompileOnSaveBadge(newCoSBadge()).
                build(),*/
            new AvatarJSLogicalViewProvider(this),
            ProjectHooks.createProjectXmlSavedHookBuilder(eval, updateHelper, genFilesHelper).
                    setBuildImplTemplate(AvatarJSProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                    setBuildTemplate(AvatarJSProject.class.getResource("resources/build.xsl")).             //NOI18N
                    /*setOverrideModifiedBuildImplPredicate(new Callable<Boolean>(){
                        @Override
                        public Boolean call() throws Exception {
                            return projectPropertiesSave.get();
                        }
                    }).*/
                    build(),
            UILookupMergerSupport.createProjectOpenHookMerger(
                ProjectHooks.createProjectOpenedHookBuilder(this, eval, updateHelper, genFilesHelper, cpProvider).
                        addClassPathType(ClassPath.BOOT).
                        addClassPathType(ClassPath.COMPILE).
                        addClassPathType(ClassPath.SOURCE).
                        setBuildImplTemplate(AvatarJSProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                        setBuildTemplate(AvatarJSProject.class.getResource("resources/build.xsl")).             //NOI18N
                        /*addOpenPostAction(newStartMainUpdaterAction()).
                        addOpenPostAction(newWebServicesAction()).
                        addOpenPostAction(newMissingPropertiesAction()).
                        addOpenPostAction(newUpdateCopyLibsAction()).
                        addClosePostAction(newStopMainUpdaterAction()).*/
                        build()),
            LookupProviderSupport.createActionProviderMerger()
        );
        this.lookup = base;
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-avatar_js-project/Lookup"); //NOI18N
    }
    
    private class AvatarJSExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        @Override
        public List<String> getExtensibleTargets() {
            return Arrays.asList(EXTENSIBLE_TARGETS);
        }

        @Override
        public Project getOwningProject() {
            return AvatarJSProject.this;
        }

    }
}
