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

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.ExecProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import static org.netbeans.modules.apisupport.project.ui.Bundle.*;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

public final class ModuleActions implements ActionProvider, ExecProject {
    private static final String RUN_ARGS_IDE = "run.args.ide";    // NOI18N
    private static final String TEST_USERDIR_LOCK_PROP_VALUE = "--test-userdir-lock-with-invalid-arg";    // NOI18N

    static final Set<String> bkgActions = new HashSet<String>(Arrays.asList(
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE
    ));

    private static final String COMMAND_NBM = "nbm";
    private static final String MODULE_ACTIONS_TYPE = "org-netbeans-modules-apisupport-project";
    private static final String MODULE_ACTIONS_PATH = "Projects/" + MODULE_ACTIONS_TYPE + "/Actions";

    private static final RequestProcessor RP = new RequestProcessor(ModuleActions.class);

    @Override
    public Task execute(String... args) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String r : args) {
            sb.append(r).append(' ');
        }
        Properties p = new Properties();
        p.setProperty("run.args", sb.substring(0, sb.length() - 1));

        return ActionUtils.runTarget(findBuildXml(project), new String[]{"run"}, p);
    }
    
    static Action[] getProjectActions(NbModuleProject project) {
        return CommonProjectActions.forType(MODULE_ACTIONS_TYPE);
    }
    
    private final NbModuleProject project;
    private final Map<String,String[]> globalCommands = new HashMap<String,String[]>();
    private String[] supportedActions = null;
    
    public ModuleActions(NbModuleProject project) {
        this.project = project;
        refresh();
    }

    public void refresh() {
        Set<String> supportedActionsSet = new HashSet<String>();
        globalCommands.put(ActionProvider.COMMAND_BUILD, new String[] {"netbeans"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        //a gross hack to prevent 158481 and the like
        if ("mkleint".equals(System.getProperty("user.name"))) {
            globalCommands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "netbeans", "do-test-build"}); // NOI18N
        } else {
            globalCommands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "netbeans"}); // NOI18N
        }
        globalCommands.put(ActionProvider.COMMAND_RUN, new String[] {"run"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        globalCommands.put("profile", new String[] {"profile"}); // NOI18N
        globalCommands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc-nb"}); // NOI18N
        globalCommands.put(ActionProvider.COMMAND_TEST, new String[] {"test-unit"}); // NOI18N
        globalCommands.put(COMMAND_NBM, new String[] {COMMAND_NBM});
        supportedActionsSet.addAll(globalCommands.keySet());
        supportedActionsSet.add(ActionProvider.COMMAND_COMPILE_SINGLE);
        supportedActionsSet.add(JavaProjectConstants.COMMAND_DEBUG_FIX); // #47012
        if (!project.supportedTestTypes().isEmpty()) {
            supportedActionsSet.add(ActionProvider.COMMAND_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_SINGLE);
            supportedActionsSet.add(SingleMethod.COMMAND_RUN_SINGLE_METHOD);
            supportedActionsSet.add(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD);
        }
        supportedActionsSet.add(ActionProvider.COMMAND_RENAME);
        supportedActionsSet.add(ActionProvider.COMMAND_MOVE);
        supportedActionsSet.add(ActionProvider.COMMAND_COPY);
        supportedActionsSet.add(ActionProvider.COMMAND_DELETE);
        supportedActions = supportedActionsSet.toArray(new String[supportedActionsSet.size()]);
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    private static FileObject findBuildXml(NbModuleProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public boolean isActionEnabled(String command, Lookup context) {
        if (ActionProvider.COMMAND_DELETE.equals(command) ||
                ActionProvider.COMMAND_RENAME.equals(command) ||
                ActionProvider.COMMAND_MOVE.equals(command) ||
                ActionProvider.COMMAND_COPY.equals(command)) {
            return true;
        } else if (findBuildXml(project) == null) {
            // All other actions require a build script.
            return false;
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            return findSources(context) != null || findTestSources(context, true) != null;
        } else if (command.equals(COMMAND_TEST)) {
            return project.supportedTestTypes().contains("unit");
        } else if (command.equals(COMMAND_TEST_SINGLE)) {
            return findTestSourcesForSources(context) != null || findTestSources(context, true) != null;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            TestSources testSources = findTestSourcesForSources(context);
            if (testSources == null)
                    testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            return findTestSources(context, false) != null;
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            NbPlatform plaf = project.getPlatform(false);
            if (plaf == null || plaf.getHarnessVersion().compareTo(HarnessVersion.V70) < 0) {
                return false;
            }
            return findTestMethodSources(context) != null;
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findSources(context);
            if (files != null && files.length == 1) {
                return true;
            }
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.isSingle();
        } else {
            // other actions are global
            return true;
        }
    }
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    
    private FileObject[] findSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory();
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
            //System.err.println("findSources: srcDir=" + srcDir + " files=" + (files != null ? java.util.Arrays.asList(files) : null) + " context=" + context);
            return files;
        } else {
            return null;
        }
    }
    
    static class TestSources {
        private final @NonNull FileObject[] sources;
        final @NonNull String testType;
        private final @NonNull FileObject sourceDirectory;
        final @NullAllowed String method;
        TestSources(@NonNull FileObject[] sources, @NonNull String testType, @NonNull FileObject sourceDirectory, String method) {
            this.sources = sources;
            this.testType = testType;
            this.sourceDirectory = sourceDirectory;
            this.method = method;
        }
        boolean isSingle() {
            return sources.length == 1;
        }
        @NonNull String includes() {
            return ActionUtils.antIncludesList(sources, sourceDirectory);
        }
        @Override public String toString() {
            return testType + ":" + includes() + (method != null ? ("#" + method) : "");
        }
    }
    @CheckForNull TestSources findTestSources(@NonNull Lookup context, boolean allowFolders) {
        TYPE: for (String testType : project.supportedTestTypes()) {
            FileObject testSrcDir = project.getTestSourceDirectory(testType);
            if (testSrcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, null, true);
                if (files != null) {
                    for (FileObject file : files) {
                        if (!(file.hasExt("java") || allowFolders && file.isFolder())) {
                            break TYPE;
                        }
                    }
                    return new TestSources(files, testType, testSrcDir, null);
                }
            }
        }
        return null;
    }
    @CheckForNull private TestSources findTestMethodSources(@NonNull Lookup context) {
        SingleMethod meth = context.lookup(SingleMethod.class);
        if (meth != null) {
            FileObject file = meth.getFile();
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    if (FileUtil.isParentOf(testSrcDir, file)) {
                        return new TestSources(new FileObject[] {file}, testType, testSrcDir, meth.getMethodName());
                    }
                }
            }
        }
        return null;
    }

    private String getMainClass(Lookup context) {
        FileObject[] files = ActionUtils.findSelectedFiles(context, null, ".java", true); // NOI18N
        if (files.length == 1) {
            FileObject f = files[0];
            Collection<ElementHandle<TypeElement>> mcs = SourceUtils.getMainClasses(f);
            if (mcs.size() > 0) {
                ElementHandle<TypeElement> h = mcs.iterator().next();
                String qname = h.getQualifiedName();
                return qname;
            }
        }
        return null;
    }

    
    /** Find tests corresponding to selected sources.
     */
    private TestSources findTestSourcesForSources(Lookup context) {
        String testType = "unit"; // NOI18N
        FileObject[] sourceFiles = findSources(context);
        if (sourceFiles == null) {
            return null;
        }
        FileObject testSrcDir = project.getTestSourceDirectory(testType);
        if (testSrcDir == null) {
            return null;
        }
        FileObject srcDir = project.getSourceDirectory();
        FileObject[] matches = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
        if (matches != null) {
            return new TestSources(matches, testType, testSrcDir, null);
        } else {
            return null;
        }
    }
    
    @Messages("MSG_no_source=No source to operate on.")
    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        if (ActionProvider.COMMAND_DELETE.equals(command)) {
            if (ModuleOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultDeleteOperation(project);
            }
            return;
        } else if (ActionProvider.COMMAND_RENAME.equals(command)) {
            if (ModuleOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultRenameOperation(project, null);
            }
            return;
        } else if (ActionProvider.COMMAND_MOVE.equals(command)) {
            if (ModuleOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultMoveOperation(project);
            }
            return;
        } else if (ActionProvider.COMMAND_COPY.equals(command)) {
            if (ModuleOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultCopyOperation(project);
            }
            return;
        }
        if (!verifySufficientlyNewHarness(project)) {
            return;
        }

        Runnable runnable = new Runnable() {
            public void run() {
                Properties p = new Properties();
                String[] targetNames;
                if (command.equals(COMMAND_COMPILE_SINGLE)) {
                    FileObject[] files = findSources(context);
                    if (files != null) {
                        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
                        targetNames = new String[]{"compile-single"}; // NOI18N
                    } else {
                        TestSources testSources = findTestSources(context, true);
                        p.setProperty("javac.includes", testSources.includes()); // NOI18N
                        p.setProperty("test.type", testSources.testType);
                        targetNames = new String[]{"compile-test-single"}; // NOI18N
                    }
                } else if (command.equals(COMMAND_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, true);

                    }
                    targetNames = setupTestSingle(p, testSources);
                } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, false);

                    }
                    targetNames = setupDebugTestSingle(p, testSources);
                } else if (command.equals(COMMAND_RUN_SINGLE)) {
                    TestSources testSources = findTestSources(context, false);
//       TODO CoS     String enableQuickTest = project.evaluator().getProperty("quick.test.single"); // NOI18N
//            if (    Boolean.parseBoolean(enableQuickTest)
//                 && "unit".equals(testSources.testType) // NOI18N
//                 && !hasTestUnitDataDir()) { // NOI18N
//                if (bypassAntBuildScript(command, testSources.sources)) {
//                    return ;
//                }
//            }
                    String clazz = getMainClass(context);
                    if (clazz != null) {
                        targetNames = setupRunMain(p, testSources, context, clazz);
                    } else {
                        // fallback to "old" run tests behavior
                        targetNames = setupTestSingle(p, testSources);
                    }
                } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
                    TestSources testSources = findTestSources(context, false);
                    String clazz = getMainClass(context);
                    if (clazz != null) {
                        targetNames = setupDebugMain(p, testSources, context, clazz);
                    } else {
                        // fallback to "old" debug tests behavior
                        targetNames = setupDebugTestSingle(p, testSources);
                    }
                } else if (command.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD)) {
                    TestSources testSources = findTestMethodSources(context);
                    p.setProperty("test.class", testClassName(testSources)); // NOI18N
                    p.setProperty("test.type", testSources.testType); // NOI18N
                    p.setProperty("test.methods", testSources.method); // NOI18N
                    targetNames = new String[] {"test-method"}; // NOI18N
                } else if (command.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
                    TestSources testSources = findTestMethodSources(context);
                    p.setProperty("test.class", testClassName(testSources)); // NOI18N
                    p.setProperty("test.type", testSources.testType); // NOI18N
                    p.setProperty("test.methods", testSources.method); // NOI18N
                    targetNames = new String[] {"debug-test-single-nb"}; // NOI18N
                } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
                    FileObject[] files = findSources(context);
                    String path = null;
                    if (files != null) {
                        path = FileUtil.getRelativePath(project.getSourceDirectory(), files[0]);
                        assert path != null;
                        assert path.endsWith(".java");
                        targetNames = new String[]{"debug-fix-nb"}; // NOI18N
                    } else {
                        TestSources testSources = findTestSources(context, false);
                        if (testSources == null) {  // #174147
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(MSG_no_source());
                            DialogDisplayer.getDefault().notify(msg);
                            return;
                        }
                        p.setProperty("test.type", testSources.testType);
                        path = testSources.includes();
                        assert path.endsWith(".java");
                        targetNames = new String[]{"debug-fix-test-nb"}; // NOI18N
                    }
                    String clazzSlash = path.substring(0, path.length() - 5);
                    p.setProperty("fix.class", clazzSlash); // NOI18N
                } else if (command.equals(JavaProjectConstants.COMMAND_JAVADOC) && !project.supportsJavadoc()) {
                    promptForPublicPackagesToDocument();
                    return;
                } else {
                    // XXX consider passing PM.fP(FU.toFO(SuiteUtils.suiteDirectory(project))) instead for a suite component project:
                    setRunArgsIde(project, SingleModuleProperties.getInstance(project), command, p, project.getTestUserDirLockFile());
                    if (command.equals(ActionProvider.COMMAND_REBUILD)) {
                        p.setProperty("do.not.clean.module.config.xml", "true"); // #196192
                    }

                    targetNames = globalCommands.get(command);
                    if (targetNames == null) {
                        throw new IllegalArgumentException(command);
                    }
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), targetNames, p);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
        if (bkgActions.contains(command)) {
            RP.post(runnable);
        } else
            runnable.run();
    }

    static void setRunArgsIde(Project project, ModuleProperties modprops, String command, Properties p, File testUserDirLockFile) {
        StringBuilder runArgsIde = new StringBuilder();
        StartupExtender.StartMode mode;
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals("profile")) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals("profile-test-single-nb")) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            mode = null;
        }
        if (mode != null) {
            JavaPlatform plaf = modprops.getJavaPlatform();
            Lookup context = Lookups.fixed(project, plaf != null ? plaf : JavaPlatformManager.getDefault().getDefaultPlatform());
            for (StartupExtender group : StartupExtender.getExtenders(context, mode)) {
                for (String arg : group.getArguments()) {
                    runArgsIde.append("-J").append(arg).append(' ');
                }
            }
        }
        if ((command.equals(ActionProvider.COMMAND_RUN) || command.equals(ActionProvider.COMMAND_DEBUG)) // #63652
                && testUserDirLockFile.isFile()) {
            // #141069: lock file exists, run with bogus option
            runArgsIde.append(TEST_USERDIR_LOCK_PROP_VALUE);
        }
        if (runArgsIde.length() > 0) {
            p.setProperty(RUN_ARGS_IDE, runArgsIde.toString());
        }
    }

    @Messages({
        "TITLE_javadoc_disabled=No Public Packages",
        "ERR_javadoc_disabled=<html>Javadoc cannot be produced for this module.<br>It is not yet configured to export any packages to other modules.",
        "LBL_configure_pubpkg=Configure Public Packages..."
    })
    private void promptForPublicPackagesToDocument() {
        // #61372: warn the user, rather than disabling the action.
        if (ApisupportAntUIUtils.showAcceptCancelDialog(
                TITLE_javadoc_disabled(),
                ERR_javadoc_disabled(),
                LBL_configure_pubpkg(),
                null,
                NotifyDescriptor.WARNING_MESSAGE)) {
            CustomizerProviderImpl cpi = project.getLookup().lookup(CustomizerProviderImpl.class);
            cpi.showCustomizer(CustomizerProviderImpl.CATEGORY_VERSIONING, CustomizerProviderImpl.SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES);
        }
    }

    private boolean hasTestUnitDataDir() {
        String dataDir = project.evaluator().getProperty("test.unit.data.dir");
        return dataDir != null && project.getHelper().resolveFileObject(dataDir) != null;
    }
    
    private static final String SYSTEM_PROPERTY_PREFIX = "test-unit-sys-prop.";
    
    private void prepareSystemProperties(Map<String, Object> properties) {
        Map<String, String> evaluated = project.evaluator().getProperties();

        if (evaluated == null) {
            return ;
        }
        
        for (Entry<String, String> e : evaluated.entrySet()) {
            if (e.getKey().startsWith(SYSTEM_PROPERTY_PREFIX) && e.getValue() != null) {
                @SuppressWarnings("unchecked")
                Collection<String> systemProperties = (Collection<String>) properties.get(JavaRunner.PROP_RUN_JVMARGS);

                if (systemProperties == null) {
                    properties.put(JavaRunner.PROP_RUN_JVMARGS, systemProperties = new LinkedList<String>());
                }

                systemProperties.add("-D" + e.getKey().substring(SYSTEM_PROPERTY_PREFIX.length()) + "=" + e.getValue());
            }
        }
    }

    private static boolean verifySufficientlyNewHarness(NbModuleProject project) {
        NbPlatform plaf = project.getPlatform(false);
        if (plaf != null && plaf.getHarnessVersion() != HarnessVersion.UNKNOWN &&
                plaf.getHarnessVersion().compareTo(project.getMinimumHarnessVersion()) < 0) {
            promptForNewerHarness();
            return false;
        } else {
            return true;
        }
    }
    @Messages({
        "ERR_harness_too_old=You are attempting to build a module or suite project which uses a new metadata format with an old version of the module build harness which does not understand this format. You may either choose a newer NetBeans platform, or switch the harness used by the selected platform to use a newer harness (try using the harness supplied with the IDE).",
        "TITLE_harness_too_old=Harness Too Old"
    })
    static void promptForNewerHarness() {
        // #82388: warn the user that the harness version is too low.
        NotifyDescriptor d = new NotifyDescriptor.Message(ERR_harness_too_old(), NotifyDescriptor.ERROR_MESSAGE);
        d.setTitle(TITLE_harness_too_old());
        DialogDisplayer.getDefault().notify(d);
    }
    
    private String[] setupTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.includes", testSources.includes().replace("**", "**/*Test.java")); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupRunMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return  new String[] {"run-test-main"};    // NOI18N
    }

    private String[] setupDebugMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return  new String[] {"debug-test-main-nb"};    // NOI18N
    }
    
    private String testClassName(TestSources testSources) {
        String path = testSources.includes();
        assert path.endsWith(".java") && !path.contains(",") : path;
        // Convert foo/FooTest.java -> foo.FooTest
        return path.substring(0, path.length() - 5).replace('/', '.'); // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.class", testClassName(testSources)); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"debug-test-single-nb"}; // NOI18N
    }
    
    private boolean bypassAntBuildScript(String command, FileObject[] files) throws IllegalArgumentException {
        FileObject toRun = null;

        if (COMMAND_RUN_SINGLE.equals(command) || COMMAND_DEBUG_SINGLE.equals(command)) {
            toRun = files[0];
        }
        
        if (toRun != null) {
            String commandToExecute = COMMAND_RUN_SINGLE.equals(command) ? JavaRunner.QUICK_TEST : JavaRunner.QUICK_TEST_DEBUG;
            if (!JavaRunner.isSupported(commandToExecute, Collections.singletonMap(JavaRunner.PROP_EXECUTE_FILE, toRun))) {
                return false;
            }
            try {
                Map<String, Object> properties = new HashMap<String, Object>();

                prepareSystemProperties(properties);

                properties.put(JavaRunner.PROP_EXECUTE_FILE, toRun);

                JavaRunner.execute(commandToExecute, properties);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return true;
        }

        return false;
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.reload")
    @ActionRegistration(displayName="#ACTION_reload", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1400)
    @Messages("ACTION_reload=Reload in Target Platform")
    public static Action reload() {
        return reload(false);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.reloadInIde")
    @ActionRegistration(displayName="#ACTION_reload_in_ide", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1500)
    @Messages("ACTION_reload_in_ide=Install/Reload in Development IDE")
    public static Action reloadInIde() {
        return reload(true);
    }

    @Messages({
        "LBL_reload_in_ide_confirm=<html>Reloading a module in the running development IDE can be dangerous.<br>Errors in the module could corrupt your environment and force you to use a new user directory.<br>(In most cases it is wiser to use <b>Run</b> or <b>Reload in Target Platform</b>.)<br>Do you really want to reload this module in your own IDE?",
        "LBL_reload_in_ide_confirm_title=Confirm Install/Reload in Development IDE"
    })
    private static Action reload(final boolean inIDE) {
        return ProjectSensitiveActions.projectSensitiveAction(new ProjectActionPerformer() {
            @Override public boolean enable(Project _project) {
                if (!(_project instanceof NbModuleProject)) {
                    return false;
                }
                NbModuleProject project = (NbModuleProject) _project;
                if (findBuildXml(project) == null) {
                    return false;
                }
                if (!inIDE) {
                    return project.getTestUserDirLockFile().isFile();
                }
                if (Boolean.parseBoolean(project.evaluator().getProperty("is.autoload")) || Boolean.parseBoolean(project.evaluator().getProperty("is.eager"))) {
                    return false; // #86395 but #208415
                }
                NbModuleType type = project.getModuleType();
                if (type == NbModuleType.NETBEANS_ORG) {
                    return true;
                } else if (type == NbModuleType.STANDALONE) {
                    NbPlatform p = project.getPlatform(false);
                    return p != null && p.isDefault();
                } else {
                    assert type == NbModuleType.SUITE_COMPONENT : type;
                    try {
                        SuiteProject suite = SuiteUtils.findSuite(project);
                        if (suite == null) {
                            return false;
                        }
                        NbPlatform p = suite.getPlatform(false);
                        if (/* #67148 */p == null || !p.isDefault()) {
                            return false;
                        }
                        return SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.ENABLED_CLUSTERS_PROPERTY).length == 0 &&
                                SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY).length == 0 &&
                                SuiteProperties.getArrayProperty(suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY).length == 0;
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        return false;
                    }
                }
            }
            @Override public void perform(Project p) {
                NbModuleProject project = (NbModuleProject) p;
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                if (inIDE && ModuleUISettings.getDefault().getConfirmReloadInIDE()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            LBL_reload_in_ide_confirm(),
                            LBL_reload_in_ide_confirm_title(),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                    ModuleUISettings.getDefault().setConfirmReloadInIDE(false); // do not ask again
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), new String[] {inIDE ? "reload-in-ide" : "reload"}, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        }, inIDE ? ACTION_reload_in_ide() : ACTION_reload(), null);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.createNbm")
    @ActionRegistration(displayName="#ACTION_nbm", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1600)
    @Messages("ACTION_nbm=Create NBM")
    public static Action createNbm() {
        return ProjectSensitiveActions.projectCommandAction(COMMAND_NBM, ACTION_nbm(), null);
    }

    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.arch")
    @ActionRegistration(displayName="#ACTION_arch", lazy=false)
    @ActionReference(path=MODULE_ACTIONS_PATH, position=1900)
    @Messages("ACTION_arch=Generate Architecture Description")
    public static Action arch() {
        return ProjectSensitiveActions.projectSensitiveAction(new ProjectActionPerformer() {
            @Override public boolean enable(Project p) {
                if (!(p instanceof NbModuleProject)) {
                    return false;
                }
                NbModuleProject project = (NbModuleProject) p;
                return findBuildXml(project) != null;
            }
            @Override public void perform(Project p) {
                final NbModuleProject project = (NbModuleProject) p;
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                    public Void run() {
                        String prop = "javadoc.arch"; // NOI18N
                        if (project.evaluator().getProperty(prop) == null) {
                            // User has not yet configured an arch desc. Assume we should just do it for them.
                            EditableProperties props = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.setProperty(prop, "${basedir}/arch.xml"); // NOI18N
                            project.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            try {
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException e) {
                                Util.err.notify(e);
                            }
                        }
                        return null;
                    }
                });
                try {
                    ActionUtils.runTarget(findBuildXml(project), new String[] {"arch-nb"}, null); // NOI18N
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        }, ACTION_arch(), null);
    }

}
