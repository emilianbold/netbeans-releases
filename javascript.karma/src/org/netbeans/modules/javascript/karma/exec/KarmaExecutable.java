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

package org.netbeans.modules.javascript.karma.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.karma.browsers.Browser;
import org.netbeans.modules.javascript.karma.browsers.Browsers;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferencesValidator;
import org.netbeans.modules.javascript.karma.run.RunInfo;
import org.netbeans.modules.javascript.karma.run.TestRunner;
import org.netbeans.modules.javascript.karma.ui.customizer.KarmaCustomizer;
import org.netbeans.modules.javascript.karma.util.ExternalExecutable;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.netbeans.modules.javascript.karma.util.StringUtils;
import org.netbeans.modules.javascript.karma.util.ValidationResult;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

public final class KarmaExecutable {

    private static final Logger LOGGER = Logger.getLogger(KarmaExecutable.class.getName());

    public static final String KARMA_NAME = "karma"; // NOI18N
    public static final String KARMA_LONG_NAME = KARMA_NAME + FileUtils.getScriptExtension(true, true);
    private static final String PROJECT_KARMA_BASE_PATH = "node_modules/karma/bin/"; // NOI18N
    public static final String PROJECT_KARMA_PATH = PROJECT_KARMA_BASE_PATH + KARMA_NAME;
    public static final String PROJECT_KARMA_LONG_PATH = PROJECT_KARMA_BASE_PATH + KARMA_LONG_NAME;

    private static final String START_COMMAND = "start";
    private static final String RUN_COMMAND = "run";
    private static final String PORT_PARAMETER = "--port";

    private final Project project;
    private final String karmaPath;


    private KarmaExecutable(Project project) {
        assert project != null;
        this.project = project;
        karmaPath = KarmaPreferences.getKarma(project);
        assert karmaPath != null;
    }

    @CheckForNull
    public static KarmaExecutable forProject(Project project, boolean showCustomizer) {
        ValidationResult result = new KarmaPreferencesValidator()
                .validate(project)
                .getResult();
        if (validateResult(result) != null) {
            if (showCustomizer) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(KarmaCustomizer.IDENTIFIER, null);
            }
            return null;
        }
        return new KarmaExecutable(project);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "KarmaExecutable.start=Karma ({0})",
    })
    @CheckForNull
    public Future<Integer> start(int port, RunInfo runInfo) {
        List<String> params = new ArrayList<>(4);
        params.add(START_COMMAND);
        params.add(runInfo.getNbConfigFile());
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Runnable countDownTask = new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        };
        Future<Integer> task = getExecutable(Bundle.KarmaExecutable_start(ProjectUtils.getInformation(project).getDisplayName()), getProjectDir())
                .additionalParameters(params)
                .environmentVariables(runInfo.getEnvVars())
                .run(getStartDescriptor(runInfo, countDownTask));
        assert task != null : karmaPath;
        try {
            countDownLatch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (task.isDone()) {
            // some error, task is not running
            return null;
        }
        return task;
    }

    public void runTests(int port) {
        List<String> params = new ArrayList<>(3);
        params.add(RUN_COMMAND);
        params.add(PORT_PARAMETER);
        params.add(Integer.toString(port));
        getExecutable("karma run...", getProjectDir()) // NOI18N
                .additionalParameters(params)
                // XXX wait?
                .run(getRunDescriptor());
    }

    private File getProjectDir() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    private ExternalExecutable getExecutable(String title, File workDir) {
        return new ExternalExecutable(karmaPath)
                .workDir(workDir)
                .displayName(title)
                .noOutput(false);
    }

    private ExecutionDescriptor getStartDescriptor(RunInfo runInfo, Runnable serverStartTask) {
        return new ExecutionDescriptor()
                .frontWindow(false)
                .frontWindowOnError(false)
                .outLineBased(true)
                .errLineBased(true)
                .outConvertorFactory(new ServerLineConvertorFactory(runInfo, serverStartTask))
                .postExecution(serverStartTask);
    }

    private ExecutionDescriptor getRunDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getErrors().get(0).getMessage();
        }
        return result.getWarnings().get(0).getMessage();
    }

    //~ Inner classes

    private static final class ServerLineConvertorFactory implements ExecutionDescriptor.LineConvertorFactory {

        private final LineConvertor serverLineConvertor;


        public ServerLineConvertorFactory(RunInfo runInfo, Runnable startFinishedTask) {
            assert runInfo != null;
            assert startFinishedTask != null;
            serverLineConvertor = new ServerLineConvertor(runInfo, startFinishedTask);
        }

        @Override
        public LineConvertor newLineConvertor() {
            return serverLineConvertor;
        }

    }

    private static final class ServerLineConvertor implements LineConvertor {

        private static final String NB_BROWSERS = "$NB$netbeans browsers "; // NOI18N

        private final RunInfo runInfo;
        private final Runnable startFinishedTask;
        private final TestRunner testRunner;

        private boolean firstLine = true;
        private boolean startFinishedTaskRun = false;
        private Collection<Browser> browsers = null;
        private int browserCount = -1;
        private int connectedBrowsers = 0;


        public ServerLineConvertor(RunInfo runInfo, Runnable startFinishedTask) {
            assert runInfo != null;
            assert startFinishedTask != null;
            this.runInfo = runInfo;
            this.startFinishedTask = startFinishedTask;
            testRunner = new TestRunner(runInfo);
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            // info
            if (firstLine
                    && line.contains(runInfo.getNbConfigFile())) {
                firstLine = false;
                return Collections.singletonList(ConvertedLine.forText(
                        line.replace(runInfo.getNbConfigFile(), runInfo.getProjectConfigFile()), null));
            }
            // server start
            if (browsers == null
                    && line.startsWith(NB_BROWSERS)) {
                List<String> allBrowsers = StringUtils.explode(line.substring(NB_BROWSERS.length()), ","); // NOI18N
                browserCount = allBrowsers.size();
                browsers = Browsers.getBrowsers(allBrowsers);
                return Collections.emptyList();
            }
            if (startFinishedTask != null
                    && !startFinishedTaskRun
                    && line.contains("Connected on socket")) { // NOI18N
                assert browsers != null;
                connectedBrowsers++;
                if (connectedBrowsers == browserCount) {
                    startFinishedTask.run();
                    startFinishedTaskRun = true;
                }
            } else if (line.startsWith(TestRunner.NB_LINE)) {
                // test result
                testRunner.process(line);
                return Collections.emptyList();
            }
            OutputListener outputListener = null;
            if (browsers == null) {
                // some error?
                Pair<String, Integer> fileLine = FileLineParser.getOutputFileLine(line);
                if (fileLine != null) {
                    outputListener = new FileOutputListener(fileLine.first(), fileLine.second());
                }
                return Collections.singletonList(ConvertedLine.forText(line, outputListener));
            } else {
                // karma log
                for (Browser browser : browsers) {
                    Pair<String, Integer> fileLine = browser.getOutputFileLine(line);
                    if (fileLine != null) {
                        outputListener = new FileOutputListener(fileLine.first(), fileLine.second());
                        break;
                    }
                }
            }
            return Collections.singletonList(ConvertedLine.forText(line, outputListener));
        }

    }

    private static final class FileOutputListener implements OutputListener {

        final String file;
        final int line;


        public FileOutputListener(String file, int line) {
            assert file != null;
            this.file = file;
            this.line = line;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            // noop
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    FileUtils.openFile(new File(file), line);
                }
            });
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
            // noop
        }

    }

    static final class FileLineParser {

        // (/usr/lib/node_modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js:211:36)
        // ^/home/gapon/NetBeansProjects/Calculator-PHPUnit5/README.md:1$
        static final Pattern OUTPUT_FILE_LINE_PATTERN = Pattern.compile("(?:^|\\()(?<FILE>[^(]+?):(?<LINE>\\d+)(?::\\d+)?(?:$|\\))"); // NOI18N


        static Pair<String, Integer> getOutputFileLine(String line) {
            Matcher matcher = OUTPUT_FILE_LINE_PATTERN.matcher(line);
            if (!matcher.find()) {
                return null;
            }
            String file = matcher.group("FILE"); // NOI18N
            if (!new File(file).isFile()) {
                // incomplete path
                return null;
            }
            return Pair.of(file, Integer.valueOf(matcher.group("LINE"))); // NOI18N
        }

    }

}
