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

package org.netbeans.modules.ruby.rubyproject.rake;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.RecognizedOutput;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.rubyproject.RubyFileLocator;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.TestNotifier;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner.TestType;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Provides Rake running infrastructure.
 */
public final class RakeRunner {
    
    private final Project project;
    private boolean showWarnings;
    private boolean debug;
    private FileObject rakeFile;
    private RubyFileLocator fileLocator;
    private File pwd;
    private boolean test;
    private String displayName;
    private String[] parameters;

    public RakeRunner(final Project project) {
        this.project = project;
    }

    public static void runTask(final Project project, final RakeTask task, final boolean debug) {
        RakeRunner runner = new RakeRunner(project);
        runner.showWarnings(true);
        runner.setDebug(debug);
        runner.run(task);
    }

    public void setRakeFile(final FileObject rakeFile) {
        this.rakeFile = rakeFile;
    }

    /**
     * @param warn if true, produce popups if {@link RubyPlatform} is not ready
     *   for running Rake.
     */
    public void showWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @param fileLocator the file locator to be used to resolve output
     *   hyperlinks
     */
    public void setFileLocator(final RubyFileLocator fileLocator) {
        this.fileLocator = fileLocator;
    }

    /**
     * @param displayName the displayname to be shown in the output window
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * @param pwd If you specify the rake file, you can pass null as the
     *   directory, and the directory containing the rakeFile will be used,
     *   otherwise it specifies the dir to run rake in
     */
    public void setPWD(File pwd) {
        this.pwd = pwd;
    }

    private void setTest(final boolean test) {
        this.test = test;
    }
    
    public void setParameters(final String... parameters) {
        this.parameters = parameters;
    }

    public void run(final String... tasksNames) {
        if (!RubyPlatform.hasValidRake(project, showWarnings)) {
            return;
        }
        
        RakeTask[] rakeTasks = new RakeTask[tasksNames.length];
        for (int i = 0; i < tasksNames.length; i++) {
            RakeTask rakeTask = RakeSupport.getRakeTask(project, tasksNames[i]);
            if (rakeTask == null) {
                if (showWarnings) {
                    Util.notifyLocalized(RakeRunner.class, "RakeRunner.task.does.not.exist", tasksNames[i]);
                }
                return; // run only when all tasks are available
            }
            rakeTasks[i] = rakeTask;
        }
        run(rakeTasks);
    }
    
    /**
     * Hooks in the UI test runner for running <code>test</code> and
     * <code>spec</code> tasks.
     * 
     * @param taskName
     * @return true if the given task was handled.
     */
    private boolean handleTestingTasks(String taskName) {
        // TODO: handle also test:integration, test:functional etc.
        if ("test".equals(taskName) || "spec".equals(taskName)) { //NOI18N
            TestRunner runner = getTestRunner("test".equals(taskName) ? TestType.TEST_UNIT : TestType.RSPEC);//NOI18N
            if (runner != null) {
                runner.runAllTests(project, debug);
                return true;
            }
        }
        
        return false;
    }

    private void run(final RakeTask... tasks) {
        assert tasks.length > 0 : "must pass at least on task";
        
        if (!RubyPlatform.hasValidRake(project, showWarnings)) {
            return;
        }

        // Save all files first
        LifecycleManager.getDefault().saveAll();

        List<RakeTask> tasksToRun = new ArrayList<RakeTask>(Arrays.asList(tasks));
        for (Iterator<RakeTask> it = tasksToRun.iterator(); it.hasNext();) {
            if (handleTestingTasks(it.next().getTask())) {
                it.remove();
            }
        }
        
        // check whether there was only one task that got already
        // handled by the test handler hook
        if (tasksToRun.isEmpty()) {
            return;
        }

        // EMPTY CONTEXT??
        if (fileLocator == null) {
            fileLocator = new RubyFileLocator(Lookup.EMPTY, project);
        }

        if (rakeFile == null) {
            rakeFile = RakeSupport.findRakeFile(project);
        }
        if (rakeFile == null) {
            pwd = FileUtil.toFile(project.getProjectDirectory());
        }
        
        if (pwd == null) {
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        if (tasksToRun.size() == 1) { // test?
            String taskName = tasksToRun.get(0).getTask();
            setTest(taskName != null && (taskName.equals("test") || taskName.startsWith("test:"))); // NOI18N
        }
        
        computeAndSetDisplayName(tasksToRun);

        List<String> additionalArgs = new ArrayList<String>();

        if (rakeFile != null) {
            additionalArgs.add("-f"); // NOI18N
            additionalArgs.add(FileUtil.toFile(rakeFile).getAbsolutePath());
        }
        
        for (RakeTask task : tasksToRun) {
            additionalArgs.add(task.getTask());
        }

        if ((parameters != null) && (parameters.length > 0)) {
            for (String parameter : parameters) {
                additionalArgs.add(parameter);
            }
        }
        
        String charsetName = null;
        String classPath = null;
        String extraArgs = null;
        String jrubyProps = null;
        
        if (project != null) {
            PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
            if (evaluator != null) {
                charsetName = evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING);
                classPath = evaluator.getProperty(SharedRubyProjectProperties.JAVAC_CLASSPATH);
                extraArgs = evaluator.getProperty(SharedRubyProjectProperties.RAKE_ARGS);
                jrubyProps = evaluator.getProperty(RubyProjectProperties.JRUBY_PROPS);
            }
        }
        
        if (extraArgs != null) {
            String[] args = Utilities.parseParameters(extraArgs);
            if (args != null) {
                for (String arg : args) {
                    additionalArgs.add(arg);
                }
            }
        }

        // validity was checked before
        RubyPlatform platform = RubyPlatform.platformFor(project);
        GemManager gemManager = platform.getGemManager();
        String rake = gemManager.getRake();
        
        ExecutionDescriptor desc = new ExecutionDescriptor(platform, displayName, pwd, rake);
        if (!additionalArgs.isEmpty()) {
            desc.additionalArgs(additionalArgs.toArray(new String[additionalArgs.size()]));
        }
        desc.allowInput();
        desc.classPath(classPath); // Applies only to JRuby
        desc.jrubyProperties(jrubyProps);
        desc.fileLocator(fileLocator);
        desc.addStandardRecognizers();

        if (platform.isJRuby()) {
            desc.appendJdkToPath(true);
        }

        if (test) {
            desc.addOutputRecognizer(new TestNotifier(true, true));
        }
        
        desc.addOutputRecognizer(new RakeErrorRecognizer(desc, charsetName)).debug(debug);

        new RubyExecution(desc, charsetName).run();
    }

    private void computeAndSetDisplayName(final List<RakeTask> tasks) {
        ProjectInformation info = ProjectUtils.getInformation(project);
        String baseDisplayName = info == null ? NbBundle.getMessage(RakeRunnerAction.class, "RakeRunnerAction.Rake") : info.getDisplayName();
        StringBuilder displayNameSB = new StringBuilder(baseDisplayName).append(" ("); // NOI18N
        for (int i = 0; i < tasks.size(); i++) {
            displayNameSB.append(tasks.get(i).getTask());
            if (i != tasks.size() - 1) {
                displayNameSB.append(", "); // NOI18N
            }
        }
        displayNameSB.append(')');
        setDisplayName(displayNameSB.toString()); // NOI18N
    }
    
    private static TestRunner getTestRunner(TestRunner.TestType testType) {
        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
        for (TestRunner each : testRunners) {
            if (each.supports(testType)) {
                return each;
            }
        }
        return null;
    }


    private class RakeErrorRecognizer extends OutputRecognizer implements Runnable {

        private final ExecutionDescriptor desc;
        private final String charsetName;

        RakeErrorRecognizer(ExecutionDescriptor desc, String charsetName) {
            this.desc = desc;
            this.charsetName = charsetName;
        }

        @Override
        public RecognizedOutput processLine(String line) {
            if (line.indexOf("(See full trace by running task with --trace)") != -1) {
                return new OutputRecognizer.ActionText(new String[]{line},
                        new String[]{NbBundle.getMessage(RakeSupport.class, "RakeSupport.RerunRakeWithTrace")},
                        new Runnable[]{RakeErrorRecognizer.this}, null);
            }

            return null;
        }

        public void run() {
            String[] additionalArgs = desc.getAdditionalArgs();
            if (additionalArgs != null) {
                List<String> args = new ArrayList<String>();
                boolean found = false;
                for (String s : additionalArgs) {
                    args.add(s);
                    if (s.equals("--trace")) {
                        found = true;
                    }
                }
                if (!found) {
                    args.add(0, "--trace");
                }
                desc.additionalArgs(args.toArray(new String[args.size()]));
            } else {
                desc.additionalArgs("--trace");
            }
            new RubyExecution(desc, charsetName).run();
        }
    }
}
