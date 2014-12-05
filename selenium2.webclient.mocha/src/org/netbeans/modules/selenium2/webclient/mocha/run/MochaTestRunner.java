/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.mocha.run;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.selenium2.webclient.api.RunInfo;
import org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback;
import org.netbeans.modules.selenium2.webclient.api.Utilities;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Theofanis Oikonomou
 */
@NbBundle.Messages("TestRunner.noName=<no name>")
public final class MochaTestRunner {

    static final Logger LOGGER = Logger.getLogger(MochaTestRunner.class.getName());

    public static final String NB_LINE = "mocha-netbeans-reporter "; // NOI18N
    
    static final Pattern OK_PATTERN = Pattern.compile("^ok (?<INDEX>[\\d]+) (?<FULLTITLE>.*), suite=(?<SUITE>.*), testcase=(?<TESTCASE>.*), duration=(?<DURATION>[\\d]+)"); // NOI18N
    static final Pattern NOT_OK_PATTERN = Pattern.compile("^not ok (?<INDEX>[\\d]+) (?<FULLTITLE>.*), suite=(?<SUITE>.*), testcase=(?<TESTCASE>.*), duration=(?<DURATION>[\\d]+)"); // NOI18N
    static final Pattern SESSION_START_PATTERN = Pattern.compile("^1\\.\\.(?<TOTAL>[\\d]+)"); // NOI18N
    static final Pattern SESSION_END_PATTERN = Pattern.compile("^tests (?<TOTAL>[\\d]+), pass (?<PASS>[\\d]+), fail (?<FAIL>[\\d]+)"); // NOI18N

    private final RunInfo runInfo;

    private TestSession testSession;
    private TestSuite testSuite;
    private long testSuiteRuntime = 0;
    private boolean hasTests = false;
    private int testIndex = 0;
    private Trouble trouble;
    private final ArrayList<String> stackTrace = new ArrayList<>();
    private String runningSuite;
    private String testcase;
    private long duration;


    public MochaTestRunner(RunInfo runInfo) {
        assert runInfo != null;
        this.runInfo = runInfo;
    }
    
    public String processLine(String logMessage) {
        String line = logMessage.replace(NB_LINE, "");
        
        Matcher matcher;
        matcher = SESSION_START_PATTERN.matcher(line);
        if (matcher.find()) {
            sessionStarted(line);
            return line;
        }

        matcher = SESSION_END_PATTERN.matcher(line);
        if (matcher.find()) {
            handleTrouble();
            sessionFinished(line);
            return "";
        }

        matcher = OK_PATTERN.matcher(line);
        if (matcher.find()) {
            String output2display = parseTestResult(matcher);
            addTestCase(testcase, Status.PASSED, duration);
            getManager().displayOutput(testSession, output2display, false);
            return output2display;
        }

        matcher = NOT_OK_PATTERN.matcher(line);
        if (matcher.find()) {
            String output2display = parseTestResult(matcher);
            getManager().displayOutput(testSession, output2display, false);
            return output2display;
        }
        if(trouble != null) {
            stackTrace.add(line);
        }
        return line;
    }
    
    private String parseTestResult(Matcher matcher) {
        handleTrouble();
        testIndex = Integer.parseInt(matcher.group("INDEX"));
        String suite = matcher.group("SUITE");
        testcase = matcher.group("TESTCASE");
        if (matcher.pattern().pattern().equals(NOT_OK_PATTERN.pattern())) {
            trouble = new Trouble(false);
        }
        duration = Long.parseLong(matcher.group("DURATION"));
        if (runningSuite == null) {
            runningSuite = suite;
            suiteStarted(runningSuite);
        } else {
            if (!runningSuite.equals(suite)) {
                suiteFinished(runningSuite);
                runningSuite = suite;
                suiteStarted(runningSuite);
            }
        }
        return (matcher.pattern().pattern().equals(OK_PATTERN.pattern()) ? "ok " : "not ok ") + testIndex + " " + runningSuite + " " + testcase;
    }
    
    private void handleTrouble() {
        if (trouble != null) {
            trouble.setStackTrace(stackTrace.toArray(new String[stackTrace.size()]));
            addTestCase(testcase, Status.FAILED, duration, trouble);
            stackTrace.clear();
            trouble = null;
        }
    }
    
    static String removeEscapeCharachters(String line) {
        return line.replaceAll("\u001B", "").replaceAll("\\[[;\\d]*m", "").trim();
    }

    private Manager getManager() {
        return Manager.getInstance();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "TestRunner.runner.title={0} (Selenium)",
    })
    private String getOutputTitle() {
        StringBuilder sb = new StringBuilder(30);
        sb.append(ProjectUtils.getInformation(runInfo.getProject()).getDisplayName());
        if (!runInfo.isTestingProject()) {
            String testFile = runInfo.getTestFile();
            if (testFile != null) {
                sb.append(":"); // NOI18N
                sb.append(new File(testFile).getName());
            }
        }
        return Bundle.TestRunner_runner_title(sb.toString());
    }

    private void sessionStarted(String line) {
        assert testSession == null;
//        getManager().setNodeFactory(new SeleniumTestRunnerNodeFactory(new CallStackCallback(runInfo.getProject())));
        getManager().setNodeFactory(Utilities.getTestRunnerNodeFactory(new CallStackCallback(runInfo.getProject())));
        testSession = new TestSession(getOutputTitle(), runInfo.getProject(), TestSession.SessionType.TEST);
        testSession.setRerunHandler(runInfo.getRerunHandler());
        getManager().testStarted(testSession);
        getManager().displayOutput(testSession, line, false);
    }

    @NbBundle.Messages({
        "TestRunner.tests.none.1=No tests executed - perhaps an error occured?",
        "TestRunner.tests.none.2=\nFull output can be verified in Output window.",
        "TestRunner.output.full=\nFull output can be found in Output window.",
    })
    private void sessionFinished(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for qunit
            suiteFinished(null);
        }
        if (!hasTests) {
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_none_1(), true);
            getManager().displayOutput(testSession, Bundle.TestRunner_tests_none_2(), true);
        } else {
            getManager().displayOutput(testSession, Bundle.TestRunner_output_full(), false);
        }
        getManager().sessionFinished(testSession);
        testSession = null;
        hasTests = false;
    }

    @NbBundle.Messages({
        "# {0} - suite name",
        "TestRunner.suite.name={0}",
    })
    private void suiteStarted(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for more browsers and last browser suite
            suiteFinished(null);
        }
        assert testSuite == null;
        assert testSuiteRuntime == 0;
        String name = Bundle.TestRunner_suite_name(line);
        if (!StringUtilities.hasText(name)) {
            name = Bundle.TestRunner_noName();
        }
        testSuite = new TestSuite(name);
        testSession.addSuite(testSuite);
        getManager().displaySuiteRunning(testSession, testSuite.getName());
    }

    private void suiteFinished(@NullAllowed String line) {
        assert testSession != null;
        if (testSuite == null) {
            // current suite already finished
            return;
        }
        getManager().displayReport(testSession, testSession.getReport(testSuiteRuntime), true);
        testSuite = null;
        testSuiteRuntime = 0;
    }

    private void addTestCase(String name, Status status) {
        addTestCase(name, status, 0L);
    }

    private void addTestCase(String name, Status status, long runtime) {
        addTestCase(name, status, runtime, null);
    }

    private void addTestCase(String name, Status status, long runtime, Trouble trouble) {
        hasTests = true;
        Testcase testCase = new Testcase(name, "Selenium", testSession); // NOI18N
        testCase.setStatus(status);
        testSuiteRuntime += runtime;
        testCase.setTimeMillis(runtime);
        if (trouble != null) {
            testCase.setTrouble(trouble);
        }
        testSession.addTestCase(testCase);
    }

    // ~ Inner classes
    
    public static final class CallStackCallback implements JumpToCallStackCallback {

        // at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29
        // at notify (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:465:12)
        static final Pattern FILE_LINE_PATTERN = Pattern.compile("at([^/]+)(?<FILE>[^:]+):(?<LINE>\\d+):(?<COLUMN>\\d+)"); // NOI18N

        final Project project;


        public CallStackCallback(Project project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public Pair<File, int[]> parseLocation(String callStack, boolean underTestRoot) {
            Matcher matcher = FILE_LINE_PATTERN.matcher(callStack.trim());
            if (matcher.find()) {
                File path = new File(matcher.group("FILE").replace('/', File.separatorChar)); // NOI18N
                File file;
                FileObject projectDir = project.getProjectDirectory();
                if (path.isAbsolute()) {
                    file = path;
                } else {
                    file = new File(FileUtil.toFile(projectDir), path.getPath());
                    if (!file.isFile()) {
                        return null;
                    }
                }
                FileObject parent = underTestRoot ? Utilities.getTestsSeleniumFolder(project, false) : projectDir;
                if(!FileUtil.isParentOf(parent, FileUtil.toFileObject(file))) {
                    return null;
                }
                return Pair.of(file, new int[] {Integer.parseInt(matcher.group("LINE")), Integer.parseInt(matcher.group("COLUMN"))}); // NOI18N
            }
            return null;
        }

    }

}
