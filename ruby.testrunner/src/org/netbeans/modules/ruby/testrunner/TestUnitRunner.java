/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.netbeans.modules.ruby.rubyproject.spi.RakeTaskCustomizer;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.testrunner.TestRunnerUtilities.DefaultTaskEvaluator;
import org.netbeans.modules.ruby.testrunner.ui.Manager;
import org.netbeans.modules.ruby.testrunner.ui.TestRecognizer;
import org.netbeans.modules.ruby.testrunner.ui.TestSession.SessionType;
import org.netbeans.modules.ruby.testrunner.ui.TestUnitHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Test runner implmentation for running test/unit tests.
 *
 * @author Erno Mononen
 */
public final class TestUnitRunner implements TestRunner, RakeTaskCustomizer {

    private static final Logger LOGGER = Logger.getLogger(TestUnitRunner.class.getName());
    private static final String NB_TEST_RUNNER = "NB_TEST_RUNNER"; //NOI18N
    public static final String MEDIATOR_SCRIPT_NAME = "nb_test_mediator.rb";  //NOI18N
    public static final String RUNNER_SCRIPT_NAME = "nb_test_runner.rb";  //NOI18N
    private static final TestRunner INSTANCE = new TestUnitRunner();
    /**
     * Sometimes, or rather for some projects (such as RubyGems), when running the test task the underlying
     * stream stalls for some time in the middle of the test run. 5000ms seems to enough
     * for most cases.
     */
    static final int DEFAULT_WAIT_TIME = 5000;

    static {
        // this env variable is referenced from nb_test_runner.rb, where it
        // gets appended to the rake require path
        System.setProperty(NB_TEST_RUNNER, getScript(RUNNER_SCRIPT_NAME).getAbsolutePath());
    }

    public TestRunner getInstance() {
        return INSTANCE;
    }

    public void runSingleTest(FileObject testFile, String testMethod, boolean debug) {
        List<String> additionalArgs = getTestFileArgs(testFile);
        additionalArgs.add("-m");
        additionalArgs.add(testMethod);
        run(FileOwnerQuery.getOwner(testFile), additionalArgs, testMethod, debug);
    }

    public void runTest(FileObject testFile, boolean debug) {
        Project project = FileOwnerQuery.getOwner(testFile);
        if (!testFile.isFolder()) {
            run(project, getTestFileArgs(testFile), testFile.getName(), debug);
        } else {
            List<String> additionalArgs = new ArrayList<String>();
            additionalArgs.add("-d"); //NOI18N
            additionalArgs.add(FileUtil.toFile(testFile).getAbsolutePath());
            String name = ProjectUtils.getInformation(project).getDisplayName();
            run(project, additionalArgs, name, debug);
        }
    }

    private List<String> getTestFileArgs(FileObject testFile) {
        String testFilePath = FileUtil.toFile(testFile).getAbsolutePath();
        List<String> additionalArgs = new ArrayList<String>();
        additionalArgs.add("-f"); //NOI18N
        additionalArgs.add(testFilePath);
        return additionalArgs;
    }

    private static File getScript(String name) {
        File script = InstalledFileLocator.getDefault().locate(
                name, "org.netbeans.modules.ruby.testrunner", false);  // NOI18N

        if (script == null) {
            throw new IllegalStateException("Could not locate " + name); // NOI18N

        }
        return script;

    }

    static void addTestUnitRunnerToEnv(Map<String, String> env) {
        env.put(NB_TEST_RUNNER, getScript(RUNNER_SCRIPT_NAME).getAbsolutePath());
    }
    
    public void runAllTests(Project project, boolean debug) {
        List<String> additionalArgs = new ArrayList<String>();
        RubyBaseProject baseProject = project.getLookup().lookup(RubyBaseProject.class);
        for (FileObject testDir : baseProject.getTestSourceRootFiles()) {
            additionalArgs.add("-d"); //NOI18N
            additionalArgs.add(FileUtil.toFile(testDir).getAbsolutePath());
        }

        String name = ProjectUtils.getInformation(project).getDisplayName();

        run(project, additionalArgs, name, debug);
    }

    private void run(Project project, List<String> additionalArgs, String name, boolean debug) {
        FileLocator locator = project.getLookup().lookup(FileLocator.class);
        RubyPlatform platform = RubyPlatform.platformFor(project);

        String targetPath = getScript(MEDIATOR_SCRIPT_NAME).getAbsolutePath();
        ExecutionDescriptor desc = null;
        String charsetName = null;
        desc = new ExecutionDescriptor(platform, name, FileUtil.toFile(project.getProjectDirectory()), targetPath);
        desc.additionalArgs(additionalArgs.toArray(new String[additionalArgs.size()]));
        desc.initialArgs(RubyProjectUtil.getLoadPath(project)); //NOI18N

        desc.debug(debug);
        desc.allowInput();
        desc.fileLocator(locator);
        desc.addStandardRecognizers();
        TestRecognizer recognizer = new TestRecognizer(Manager.getInstance(),
                locator,
                TestUnitHandlerFactory.getHandlers(),
                debug ? SessionType.DEBUG : SessionType.TEST);
        TestExecutionManager.getInstance().start(desc, recognizer);
    }

    public boolean supports(TestType type) {
        return type == TestType.TEST_UNIT;
    }

    public void customize(Project project, RakeTask task, ExecutionDescriptor taskDescriptor, boolean debug) {
        boolean useRunner = TestRunnerUtilities.useTestRunner(project, SharedRubyProjectProperties.TEST_TASKS, task, new DefaultTaskEvaluator() {

            public boolean isDefault(RakeTask task) {
                return "test".equals(task.getTask()) || task.getTask().startsWith("test:"); //NOI18N
            }
        });

        if (!useRunner) {
            return;
        }
        
        TestExecutionManager.getInstance().reset();
        // this takes care of loading our custom TestTask, which in turn passes
        // the custom test runner as an option for the task. This is needed since
        // the test run is forked to a different process (by Rake::TestTask) than rake itself
        task.addRakeParameters("-r \"" + getScript(RUNNER_SCRIPT_NAME).getAbsolutePath() + "\""); //NOI18N
        FileLocator locator = project.getLookup().lookup(FileLocator.class);
        TestRecognizer recognizer = new TestRecognizer(Manager.getInstance(),
                locator,
                TestUnitHandlerFactory.getHandlers(),
                //XXX
                debug ? SessionType.DEBUG : SessionType.TEST);

        Map<String, String> env = new HashMap<String, String>(1);
        addTestUnitRunnerToEnv(env);
        taskDescriptor.addAdditionalEnv(env);
        taskDescriptor.addOutputRecognizer(recognizer);
        taskDescriptor.setReadMaxWaitTime(DEFAULT_WAIT_TIME);
        taskDescriptor.setRerun(false);
    }

}
