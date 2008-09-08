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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
import org.netbeans.modules.ruby.testrunner.ui.RspecHandlerFactory;
import org.netbeans.modules.ruby.testrunner.ui.TestRecognizer;
import org.netbeans.modules.ruby.testrunner.ui.TestSession;
import org.netbeans.modules.ruby.testrunner.ui.TestSession.SessionType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Test runner for RSpec tests.
 *
 * <i>TODO: get rid of duplication with RSpecSupport, such as finding the rspec binary</i>.
 *
 * @author Erno Mononen
 */
public class RspecRunner implements TestRunner, RakeTaskCustomizer {

    private static final String PLUGIN_SPEC_PATH = "vendor/plugins/rspec/bin/spec"; // NOI18N
    private static final String SCRIPT_SPEC_PATH = "script/spec"; // NOI18N
    private static final String SPEC_BIN = "spec"; // NOI18N
    private static final TestRunner INSTANCE = new RspecRunner();
    private static final String SPEC_OPTS = "spec/spec.opts"; // NOI18N
    private static final String NETBEANS_SPEC_OPTS = SPEC_OPTS + ".netbeans"; // NOI18N

    public static final String RSPEC_MEDIATOR_SCRIPT = "nb_rspec_mediator.rb"; //NOI18N
    public TestRunner getInstance() {
        return INSTANCE;
    }

    public boolean supports(TestType type) {
        return TestType.RSPEC == type;
    }

    public void runTest(FileObject testFile, boolean debug) {
        List<String> specFile = new ArrayList<String>();
        specFile.add(FileUtil.toFile(testFile).getAbsolutePath());
        List<String> additionalArgs = new ArrayList<String>();
        additionalArgs.add(FileUtil.toFile(testFile).getAbsolutePath());
        run(FileOwnerQuery.getOwner(testFile),
                additionalArgs,
                testFile.getName(),
                debug);
    }

    public void runSingleTest(FileObject testFile, String testMethod, boolean debug) {
        // the testMethod param here actually presents the line number
        // of the rspec specification in the test file.
        List<String> additionalArgs = new ArrayList<String>();
        additionalArgs.add("--line");
        additionalArgs.add(testMethod);
        additionalArgs.add(FileUtil.toFile(testFile).getAbsolutePath());
        run(FileOwnerQuery.getOwner(testFile), additionalArgs, testFile.getName(), debug);
    }

    public void runAllTests(Project project, boolean debug) {
        // collect specs from the test dirs (will be changed to use rake spec in the future)
        RubyBaseProject baseProject = project.getLookup().lookup(RubyBaseProject.class);
        FileObject[] testDirs = baseProject.getTestSourceRootFiles();
        List<String> specs = new ArrayList<String>();
        for (FileObject dir : testDirs) {
            Enumeration<? extends FileObject> children = dir.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject each = children.nextElement();
                if (!each.isFolder() && "rb".equals(each.getExt()) && each.getName().endsWith("spec")) { //NOI18N
                    specs.add(FileUtil.toFile(each).getAbsolutePath());
                }
            }
        }
        run(project, specs, ProjectUtils.getInformation(project).getDisplayName(), debug);
    }

    private void run(Project project, List<String> additionalArgs, String name, boolean debug) {
        FileLocator locator = project.getLookup().lookup(FileLocator.class);
        RubyPlatform platform = RubyPlatform.platformFor(project);

        if (additionalArgs.isEmpty()) {
            // just display 'no tests run' immediately if there are no files to run
            TestSession empty = new TestSession(locator, debug ? SessionType.DEBUG : SessionType.TEST);
            Manager.getInstance().emptyTestRun(empty);
            return;
        }

        List<String> arguments = new ArrayList<String>();
        arguments.add("--require"); //NOI18N
        arguments.add(getMediatorScript().getAbsolutePath());
        arguments.add("--runner"); //NOI18N
        arguments.add("NbRspecMediator"); //NOI18N

        addSpecOpts(project, additionalArgs);

        arguments.addAll(additionalArgs);

        ExecutionDescriptor desc = null;
        String charsetName = null;
        desc = new ExecutionDescriptor(platform,
                name,
                FileUtil.toFile(project.getProjectDirectory()),
                getSpec(project).getAbsolutePath());
        desc.additionalArgs(arguments.toArray(new String[arguments.size()]));
        desc.initialArgs(RubyProjectUtil.getLoadPath(project)); //NOI18N

        desc.debug(debug);
        desc.allowInput();
        desc.fileLocator(locator);
        desc.addStandardRecognizers();

        TestRecognizer recognizer = new TestRecognizer(Manager.getInstance(),
                locator,
                RspecHandlerFactory.getHandlers(),
                debug ? SessionType.DEBUG : SessionType.TEST);
        TestExecutionManager.getInstance().start(desc, recognizer);
    }

    /**
     * Gets the spec binary to use for the project. Prefers the spec rails plugin
     * if found.
     * 
     * @param project
     * @return
     */
    private File getSpec(Project project) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject specScript = projectDir.getFileObject(SCRIPT_SPEC_PATH);
        if (specScript != null) {
            return FileUtil.toFile(specScript);
        }
        if (projectDir != null) {
            FileObject pluginSpec = projectDir.getFileObject(PLUGIN_SPEC_PATH);
            if (pluginSpec != null) {
                return FileUtil.toFile(pluginSpec);
            }
        }
        RubyPlatform platform = RubyPlatform.platformFor(project);
        String spec = platform.findExecutable(SPEC_BIN); //NOI18N
        if (spec != null) {
            return new File(spec);
        }
        // this should not happen as the presence of the binary
        // should be checked before invoking this test runner
        assert false : "Could not find RSpec binary"; //NOI18N
        return null;
    }

    static File getMediatorScript() {
        File mediatorScript = InstalledFileLocator.getDefault().locate(
                RSPEC_MEDIATOR_SCRIPT, "org.netbeans.modules.ruby.testrunner", false);  // NOI18N

        if (mediatorScript == null) {
            throw new IllegalStateException("Could not locate " + RSPEC_MEDIATOR_SCRIPT); // NOI18N

        }
        return mediatorScript;
    }

    private static void addSpecOpts(Project project, List<String> additionalArgs) {
        FileObject specOpts = getSpecOpts(project);
        if (specOpts != null) {
            additionalArgs.add("--options"); // NOI18N
            additionalArgs.add(FileUtil.toFile(specOpts).getAbsolutePath());
        }
    }

    private static FileObject getSpecOpts(Project project) {
        // TODO: duplicated in RSpecSupport
        FileObject projectDir = project.getProjectDirectory();
        // First look for a NetBeans-specific options file, in case you want different
        // options when running under the IDE (for example, no --color since the
        // color escape codes don't work under our terminal)
        FileObject specOpts = projectDir.getFileObject(NETBEANS_SPEC_OPTS);

        if (specOpts == null) {
            specOpts = projectDir.getFileObject(SPEC_OPTS);
        }

        return specOpts;
    }

    private String getSpecOptsContent(FileObject specOpts) {
        StringBuilder result = new StringBuilder();
        BufferedReader from = null;
        try {
            from = new BufferedReader(new InputStreamReader(specOpts.getInputStream()));
            String line;
            while ((line = from.readLine()) != null) {
                result.append(line);
                result.append(" ");
            } 
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            try {
                from.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result.toString();
    }

    public void customize(Project project, RakeTask task, ExecutionDescriptor taskDescriptor, boolean debug) {
        boolean useRunner = TestRunnerUtilities.useTestRunner(project, SharedRubyProjectProperties.SPEC_TASKS, task, new DefaultTaskEvaluator() {

            public boolean isDefault(RakeTask task) {
                return "spec".equals(task.getTask()); //NOI18N
            }
        });
          
        if (!useRunner) {
            return;
        }

        TestExecutionManager.getInstance().reset();
        String path = getMediatorScript().getAbsolutePath();
        if (Utilities.isWindows()) {
            RubyPlatform platform = RubyPlatform.platformFor(project);
            if (platform != null && platform.isJRuby()) {
                // backslashes don't work with JRuby here
                path = path.replace('\\', '/'); //NOI18N
            }
        }
        String options = "--require '" + path + "' --runner NbRspecMediator"; //NOI18N
        FileObject specOpts = getSpecOpts(project);
        if (specOpts != null) {
            options += " " + getSpecOptsContent(specOpts);
        }
        task.addTaskParameters("SPEC_OPTS=" + options); //NOI18N

        FileLocator locator = project.getLookup().lookup(FileLocator.class);
        TestRecognizer recognizer = new TestRecognizer(Manager.getInstance(),
                locator,
                RspecHandlerFactory.getHandlers(),
                debug ? SessionType.DEBUG : SessionType.TEST);
        taskDescriptor.addOutputRecognizer(recognizer);
        // using a shorter wait time than for test/unit since the only cases
        // i've seen requiring more than 1000ms have all been test/unit executions
        taskDescriptor.setReadMaxWaitTime(1500);
        taskDescriptor.setRerun(false);
    }

}
