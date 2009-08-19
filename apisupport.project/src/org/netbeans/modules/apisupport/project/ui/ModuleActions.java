/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

public final class ModuleActions implements ActionProvider {
    static final String TEST_USERDIR_LOCK_PROP_NAME = "run.args.ide";    // NOI18N
    static final String TEST_USERDIR_LOCK_PROP_VALUE = "--test-userdir-lock-with-invalid-arg";    // NOI18N

    static final Set<String> bkgActions = new HashSet<String>(Arrays.asList(
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE
    ));

    static Action[] getProjectActions(NbModuleProject project) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(ModuleActions.class, "ACTION_build"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(ModuleActions.class, "ACTION_rebuild"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(ModuleActions.class, "ACTION_clean"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, NbBundle.getMessage(ModuleActions.class, "ACTION_run"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(ModuleActions.class, "ACTION_debug"), null));
        actions.addAll(Utilities.actionsForPath("Projects/Profiler_Actions_temporary")); //NOI18N
        if (project.supportedTestTypes().contains("unit")) { // NOI18N
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, NbBundle.getMessage(ModuleActions.class, "ACTION_test"), null));
        }
        actions.add(null);
        boolean isNetBeansOrg = Util.getModuleType(project) == NbModuleProvider.NETBEANS_ORG;
        if (isNetBeansOrg) {
            actions.add(createCheckBundleAction(project, NbBundle.getMessage(ModuleActions.class, "ACTION_unused_bundle_keys")));
            actions.add(null);
        }
        actions.add(createReloadAction(project, new String[] {"reload"}, NbBundle.getMessage(ModuleActions.class, "ACTION_reload"), false));
        actions.add(createReloadAction(project, new String[] {"reload-in-ide"}, NbBundle.getMessage(ModuleActions.class, "ACTION_reload_in_ide"), true));
        actions.add(createSimpleAction(project, new String[] {"nbm"}, NbBundle.getMessage(ModuleActions.class, "ACTION_nbm")));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(JavaProjectConstants.COMMAND_JAVADOC, NbBundle.getMessage(ModuleActions.class, "ACTION_javadoc"), null));
        actions.add(createArchAction(project));
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(CommonProjectActions.renameProjectAction());
        actions.add(CommonProjectActions.moveProjectAction());
        actions.add(CommonProjectActions.copyProjectAction());
        actions.add(CommonProjectActions.deleteProjectAction());
        
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        // Honor #57874 contract:
        actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return actions.toArray(new Action[actions.size()]);
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
        if (project.supportedTestTypes().contains("unit")) { // NOI18N
            globalCommands.put(ActionProvider.COMMAND_TEST, new String[] {"test-unit"}); // NOI18N
        }
        supportedActionsSet.addAll(globalCommands.keySet());
        supportedActionsSet.add(ActionProvider.COMMAND_COMPILE_SINGLE);
        supportedActionsSet.add(JavaProjectConstants.COMMAND_DEBUG_FIX); // #47012
        if (!project.supportedTestTypes().isEmpty()) {
            supportedActionsSet.add(ActionProvider.COMMAND_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_TEST_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_RUN_SINGLE);
            supportedActionsSet.add(ActionProvider.COMMAND_DEBUG_SINGLE);
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
            return findSources(context) != null || findTestSources(context, false) != null;
        } else if (command.equals(COMMAND_TEST_SINGLE)) {
            return findTestSourcesForSources(context) != null || findTestSources(context, false) != null;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            TestSources testSources = findTestSourcesForSources(context);
            if (testSources == null)
                    testSources = findTestSources(context, false);
            return testSources != null && testSources.sources.length == 1;
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            return findTestSources(context, false) != null;
        } else if (command.equals(COMMAND_DEBUG_SINGLE)) {
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.sources.length == 1;
        } else if (command.equals(JavaProjectConstants.COMMAND_DEBUG_FIX)) {
            FileObject[] files = findSources(context);
            if (files != null && files.length == 1) {
                return true;
            }
            TestSources testSources = findTestSources(context, false);
            return testSources != null && testSources.sources.length == 1;
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
        final FileObject[] sources;
        final String testType;
        final FileObject sourceDirectory;
        public TestSources(FileObject[] sources, String testType, FileObject sourceDirectory) {
            assert sources != null;
            assert sourceDirectory != null;
            this.sources = sources;
            this.testType = testType;
            this.sourceDirectory = sourceDirectory;
        }
    }
    private TestSources findTestSources(Lookup context, boolean checkInSrcDir) {
        for (String testType : project.supportedTestTypes()) {
            FileObject testSrcDir = project.getTestSourceDirectory(testType);
            if (testSrcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcDir, ".java", true); // NOI18N
                if (files != null) {
                    return new TestSources(files, testType, testSrcDir);
                }
            }
        }
        if (checkInSrcDir) {
            FileObject srcDir = project.getSourceDirectory();
            FileObject testSrcDir = project.getTestSourceDirectory("unit"); // NOI18N
            //System.err.println("  srcDir=" + srcDir);
            if (srcDir != null && testSrcDir != null) {
                FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true); // NOI18N
                //System.err.println("  files=" + files);
                if (files != null) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files, srcDir, SRCDIRJAVA, testSrcDir, SUBST, true);
                    //System.err.println("  files2=" + files2);
                    if (files2 != null) {
                        return new TestSources(files2, "unit", testSrcDir); // NOI18N
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
            return new TestSources(matches, testType,testSrcDir);
        } else {
            return null;
        }
    }
    
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
                        TestSources testSources = findTestSources(context, false);
                        p.setProperty("javac.includes", ActionUtils.antIncludesList(testSources.sources, testSources.sourceDirectory)); // NOI18N
                        p.setProperty("test.type", testSources.testType);
                        targetNames = new String[]{"compile-test-single"}; // NOI18N
                    }
                } else if (command.equals(COMMAND_TEST_SINGLE)) {
                    TestSources testSources = findTestSourcesForSources(context);
                    if (testSources == null) {
                        testSources = findTestSources(context, false);

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
                        path = FileUtil.getRelativePath(testSources.sourceDirectory, testSources.sources[0]);
                        p.setProperty("test.type", testSources.testType);
                        assert path != null;
                        assert path.endsWith(".java");
                        targetNames = new String[]{"debug-fix-test-nb"}; // NOI18N
                    }
                    String clazzSlash = path.substring(0, path.length() - 5);
                    p.setProperty("fix.class", clazzSlash); // NOI18N
                } else if (command.equals(JavaProjectConstants.COMMAND_JAVADOC) && !project.supportsJavadoc()) {
                    promptForPublicPackagesToDocument();
                    return;
                } else {
                    if ((command.equals(ActionProvider.COMMAND_RUN) || command.equals(ActionProvider.COMMAND_DEBUG)) // #63652
                            && project.getTestUserDirLockFile().isFile()) {
                        // #141069: lock file exists, run with bogus option
                        p.setProperty(TEST_USERDIR_LOCK_PROP_NAME, TEST_USERDIR_LOCK_PROP_VALUE);
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
            RequestProcessor.getDefault().post(runnable);
        } else
            runnable.run();
    }

    private void promptForPublicPackagesToDocument() {
        // #61372: warn the user, rather than disabling the action.
        if (UIUtil.showAcceptCancelDialog(
                NbBundle.getMessage(ModuleActions.class, "TITLE_javadoc_disabled"),
                NbBundle.getMessage(ModuleActions.class, "ERR_javadoc_disabled"),
                NbBundle.getMessage(ModuleActions.class, "LBL_configure_pubpkg"),
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
        if (plaf != null && plaf.getHarnessVersion() != NbPlatform.HARNESS_VERSION_UNKNOWN && plaf.getHarnessVersion() < project.getMinimumHarnessVersion()) {
            promptForNewerHarness();
            return false;
        } else {
            return true;
        }
    }
    static void promptForNewerHarness() {
        // #82388: warn the user that the harness version is too low.
        NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(ModuleActions.class, "ERR_harness_too_old"), NotifyDescriptor.ERROR_MESSAGE);
        d.setTitle(NbBundle.getMessage(ModuleActions.class, "TITLE_harness_too_old"));
        DialogDisplayer.getDefault().notify(d);
    }
    
    private String[] setupTestSingle(Properties p, TestSources testSources) {
        p.setProperty("test.includes", ActionUtils.antIncludesList(testSources.sources, testSources.sourceDirectory)); // NOI18N
        p.setProperty("test.type", testSources.testType); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupRunMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        return  new String[] {"run-test-main"};    // NOI18N
    }

    private String[] setupDebugMain(Properties p, TestSources testSources, Lookup context, String mainClass) {
        p.setProperty("main.class", mainClass);    // NOI18N
        return  new String[] {"debug-test-main-nb"};    // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, TestSources testSources) {
        String path = FileUtil.getRelativePath(testSources.sourceDirectory, testSources.sources[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
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
    
    private static Action createSimpleAction(final NbModuleProject project, final String[] targetNames, String displayName) {
        return new AbstractAction(displayName) {
            public @Override boolean isEnabled() {
                return findBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), targetNames, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createCheckBundleAction(final NbModuleProject project, String displayName) {
        return new AbstractAction(displayName) {
            public @Override boolean isEnabled() {
                return findMonitorXml() != null && project.getPathWithinNetBeansOrg() != null;
            }
            public void actionPerformed(ActionEvent ignore) {
                Properties props = new Properties();
                props.put("modules", project.getPathWithinNetBeansOrg()); // NOI18N
                props.put("fixedmodules", ""); // NOI18N
                try {
                    ActionUtils.runTarget(findMonitorXml(), new String[] {"check-bundle-usage"}, props); // NOI18N
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
            private FileObject findMonitorXml() {
                return project.getNbrootFileObject("nbbuild/monitor.xml"); // NOI18N
            }
        };
    }
    
    private static Action createReloadAction(final NbModuleProject project, final String[] targetNames, String displayName, final boolean inIDE) {
        return new AbstractAction(displayName) {
            public @Override boolean isEnabled() {
                if (findBuildXml(project) == null) {
                    return false;
                }
                if (Boolean.parseBoolean(project.evaluator().getProperty("is.autoload")) || Boolean.parseBoolean(project.evaluator().getProperty("is.eager"))) { // NOI18N
                    return false; // #86395
                }
                if (!inIDE) {
                    return project.getTestUserDirLockFile().isFile();
                }
                NbModuleProvider.NbModuleType type = Util.getModuleType(project);
                if (type == NbModuleProvider.NETBEANS_ORG) {
                    return true;
                } else if (type == NbModuleProvider.STANDALONE) {
                    NbPlatform p = project.getPlatform(false);
                    return p != null && p.isDefault();
                } else {
                    assert type == NbModuleProvider.SUITE_COMPONENT : type;
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
            public void actionPerformed(ActionEvent ignore) {
                if (!verifySufficientlyNewHarness(project)) {
                    return;
                }
                if (inIDE && ModuleUISettings.getDefault().getConfirmReloadInIDE()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(ModuleActions.class, "LBL_reload_in_ide_confirm"),
                            NbBundle.getMessage(ModuleActions.class, "LBL_reload_in_ide_confirm_title"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                    ModuleUISettings.getDefault().setConfirmReloadInIDE(false); // do not ask again
                }
                try {
                    ActionUtils.runTarget(findBuildXml(project), targetNames, null);
                } catch (IOException e) {
                    Util.err.notify(e);
                }
            }
        };
    }
    
    private static Action createArchAction(final NbModuleProject project) {
        return new AbstractAction(NbBundle.getMessage(ModuleActions.class, "ACTION_arch")) {
            public @Override boolean isEnabled() {
                return findBuildXml(project) != null;
            }
            public void actionPerformed(ActionEvent ignore) {
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
        };
    }
    
}
