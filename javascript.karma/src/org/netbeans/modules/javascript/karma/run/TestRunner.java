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

package org.netbeans.modules.javascript.karma.run;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class TestRunner {

    static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());

    public static final String NB_LINE = "$NB$netbeans "; // NOI18N

    private static final String BROWSER_START = "$NB$netbeans browserStart"; // NOI18N
    private static final String BROWSER_END = "$NB$netbeans browserEnd"; // NOI18N
    private static final String SUITE_START = "$NB$netbeans suiteStart"; // NOI18N
    private static final String SUITE_END = "$NB$netbeans suiteEnd"; // NOI18N
    private static final String TEST = "$NB$netbeans test"; // NOI18N
    private static final String TEST_PASS = "$NB$netbeans testPass"; // NOI18N
    private static final String TEST_IGNORE = "$NB$netbeans testIgnore"; // NOI18N
    private static final String TEST_FAILURE = "$NB$netbeans testFailure"; // NOI18N
    private static final String NB_VALUE_REGEX = "\\$NB\\$(.+)\\$NB\\$"; // NOI18N
    private static final String NAME_REGEX = "name=" + NB_VALUE_REGEX; // NOI18N
    private static final String BROWSER_REGEX = "browser=" + NB_VALUE_REGEX; // NOI18N
    private static final String DURATION_REGEX = "duration=" + NB_VALUE_REGEX; // NOI18N
    private static final String DETAILS_REGEX = "details=" + NB_VALUE_REGEX; // NOI18N
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    private static final Pattern BROWSER_NAME_PATTERN = Pattern.compile(BROWSER_REGEX + " " + NAME_REGEX); // NOI18N
    private static final Pattern NAME_DURATION_PATTERN = Pattern.compile(NAME_REGEX + " " + DURATION_REGEX); // NOI18N
    private static final Pattern NAME_DETAILS_DURATION_PATTERN = Pattern.compile(NAME_REGEX + " " + DETAILS_REGEX + " " + DURATION_REGEX); // NOI18N

    private final RunInfo runInfo;
    private final AtomicLong browserCount = new AtomicLong();

    private TestSession testSession;
    private TestSuite testSuite;
    private long testSuiteRuntime = 0;


    public TestRunner(RunInfo runInfo) {
        assert runInfo != null;
        this.runInfo = runInfo;
    }

    public void process(String line) {
        LOGGER.finest(line);
        if (line.startsWith(BROWSER_START)) {
            if (browserCount.incrementAndGet() == 1) {
                sessionStarted(line);
            }
        } else if (line.startsWith(BROWSER_END)) {
            if (browserCount.decrementAndGet() == 0) {
                sessionFinished(line);
            }
        } else if (line.startsWith(SUITE_START)) {
            suiteStarted(line);
        } else if (line.startsWith(SUITE_END)) {
            suiteFinished(line);
        } else if (line.startsWith(TEST)) {
            testFinished(line);
        } else {
            LOGGER.log(Level.FINE, "Unexpected line: {0}", line);
            assert false : line;
        }
    }

    private Manager getManager() {
        return Manager.getInstance();
    }

    private String getOutputTitle() {
        StringBuilder sb = new StringBuilder(30);
        sb.append(ProjectUtils.getInformation(runInfo.getProject()).getDisplayName());
        String testFile = runInfo.getTestFile();
        if (testFile != null) {
            sb.append(":"); // NOI18N
            sb.append(new File(testFile).getName());
        }
        return sb.toString();
    }

    private void sessionStarted(String line) {
        assert testSession == null;
        testSession = new TestSession(getOutputTitle(), runInfo.getProject(), TestSession.SessionType.TEST,
                new KarmaTestRunnerNodeFactory(new CallStackCallback(runInfo.getProject())));
        testSession.setRerunHandler(runInfo.getRerunHandler());
        getManager().testStarted(testSession);
    }

    private void sessionFinished(String line) {
        assert testSession != null;
        getManager().sessionFinished(testSession);
        testSession = null;
    }

    @NbBundle.Messages({
        "# {0} - browser name",
        "# {1} - suite name",
        "TestRunner.suite.name=[{0}] {1}",
    })
    private void suiteStarted(String line) {
        assert testSession != null;
        if (testSuite != null) {
            // can happen for more browsers and last browser suite
            suiteFinished(null);
        }
        assert testSuite == null;
        assert testSuiteRuntime == 0;
        String name = line;
        Matcher matcher = BROWSER_NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            name = Bundle.TestRunner_suite_name(matcher.group(1), matcher.group(2));
        } else {
            LOGGER.log(Level.FINE, "Unexpected suite line: {0}", line);
            assert false : line;
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

    private void testFinished(String line) {
        assert testSession != null;
        if (line.startsWith(TEST_PASS)) {
            testPass(line);
        } else if (line.startsWith(TEST_FAILURE)) {
            testFailure(line);
        } else if (line.startsWith(TEST_IGNORE)) {
            testIgnore(line);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test line: {0}", line);
            assert false : line;
        }
    }

    private void testPass(String line) {
        Matcher matcher = NAME_DURATION_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            long runtime = Long.parseLong(matcher.group(2));
            addTestCase(name, Status.PASSED, runtime);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test PASS line: {0}", line);
            assert false : line;
        }
    }

    private void testFailure(String line) {
        Matcher matcher = NAME_DETAILS_DURATION_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            Trouble trouble = new Trouble(false);
            trouble.setStackTrace(processDetails(matcher.group(2)));
            long runtime = Long.parseLong(matcher.group(3));
            addTestCase(name, Status.FAILED, runtime, trouble);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test FAILURE line: {0}", line);
            assert false : line;
        }
    }

    private String[] processDetails(String details) {
        if (details.startsWith("[\"")) { // NOI18N
            details = details.substring(2);
        }
        if (details.endsWith("\"]")) { // NOI18N
            details = details.substring(0, details.length() - 2);
        }
        String[] lines = details.split("\\s*\\\\n\\s*"); // NOI18N
        return lines;
    }

    private void testIgnore(String line) {
        Matcher matcher = NAME_PATTERN.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            addTestCase(name, Status.IGNORED);
        } else {
            LOGGER.log(Level.FINE, "Unexpected test IGNORE line: {0}", line);
            assert false : line;
        }
    }

    private void addTestCase(String name, Status status) {
        addTestCase(name, status, 0L);
    }

    private void addTestCase(String name, Status status, long runtime) {
        addTestCase(name, status, runtime, null);
    }

    private void addTestCase(String name, Status status, long runtime, Trouble trouble) {
        Testcase testCase = new Testcase(name, "Karma", testSession); // NOI18N
        testCase.setStatus(status);
        testSuiteRuntime += runtime;
        testCase.setTimeMillis(runtime);
        if (trouble != null) {
            testCase.setTrouble(trouble);
        }
        testSession.addTestCase(testCase);
    }

    //~ Inner classes

    private static final class CallStackCallback implements JumpToCallStackAction.Callback {

        private static final String LOCALHOST = "http://localhost:"; // NOI18N
        private static final Pattern FILE_LINE_PATTERN = Pattern.compile(LOCALHOST + "\\d+/base/(.+)\\?\\d+:(\\d+):\\d+"); // NOI18N

        private final File projectDir;


        public CallStackCallback(Project project) {
            assert project != null;
            projectDir = FileUtil.toFile(project.getProjectDirectory());
        }

        @Override
        public Pair<File, Integer> parseLocation(String callStack) {
            if (!callStack.contains(LOCALHOST)) {
                return null;
            }
            Matcher matcher = FILE_LINE_PATTERN.matcher(callStack);
            if (matcher.find()) {
                String path = matcher.group(1).replace('/', File.separatorChar); // NOI18N
                return Pair.of(new File(projectDir, path), Integer.parseInt(matcher.group(2)));
            }
            assert false : callStack;
            LOGGER.log(Level.FINE, "Unexpected callstack line: {0}", callStack);
            return null;
        }

    }

}
