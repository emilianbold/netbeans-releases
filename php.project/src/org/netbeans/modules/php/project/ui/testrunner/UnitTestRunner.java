/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.testrunner;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.OutputLineHandler;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.coverage.Coverage;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase.Diff;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.OutputWriter;

/**
 * Test runner UI for PHP unit tests. One must call {@link #start()} first
 * and after the test results are available, {@link #showResults()} will show them.
 * <p>
 * Currently, only PHPUnit is supported.
 * All the times are in milliseconds.
 * @author Tomas Mysik
 */
public final class UnitTestRunner {
    private static final Logger LOGGER = Logger.getLogger(UnitTestRunner.class.getName());
    private static final Manager MANAGER = Manager.getInstance();

    private final PhpProject project;
    private final TestSession testSession;
    private final TestRunInfo info;
    private final ControllableRerunHandler rerunHandler;
    private final PhpCoverageProvider coverageProvider;
    private final PhpTestingProvider testingProvider;


    public UnitTestRunner(PhpProject project, TestRunInfo info, ControllableRerunHandler rerunHandler) {
        assert project != null;
        assert rerunHandler != null;
        assert info != null;

        this.project = project;
        this.info = info;
        this.rerunHandler = rerunHandler;
        coverageProvider = project.getLookup().lookup(PhpCoverageProvider.class);
        assert coverageProvider != null;
        // XXX use all test providers
        testingProvider = project.getFirstTestingProvider();
        assert testingProvider != null;

        testSession = new TestSession(getOutputTitle(project, info), project, map(info.getSessionType()), new PhpTestRunnerNodeFactory(new CallStackCallback(project)));
        testSession.setRerunHandler(rerunHandler);
    }

    public void run() {
        if (!checkTestingProviders()) {
            return;
        }
        try {
            rerunHandler.disable();
            MANAGER.testStarted(testSession);
            org.netbeans.modules.php.spi.testing.run.TestSession session = runInternal();
            if (session != null) {
                handleCodeCoverage(session.getCoverage());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            MANAGER.sessionFinished(testSession);
            rerunHandler.enable();
        }
    }

    private org.netbeans.modules.php.spi.testing.run.TestSession runInternal() {
        org.netbeans.modules.php.spi.testing.run.TestSession session;
        try {
            session = testingProvider.runTests(project.getPhpModule(), info);
        } catch (TestRunException exc) {
            LOGGER.log(Level.INFO, null, exc);
            MANAGER.displayOutput(testSession, NbBundle.getMessage(UnitTestRunner.class, "MSG_PerhapsError"), true);
            return null;
        }
        if (session == null) {
            // some error occured
            return null;
        }

        org.netbeans.modules.php.spi.testing.run.OutputLineHandler outputLineHandler = session.getOutputLineHandler();
        if (outputLineHandler != null) {
            testSession.setOutputLineHandler(map(outputLineHandler));
        }

        String initMessage = session.getInitMessage();
        if (initMessage != null) {
            MANAGER.displayOutput(testSession, initMessage, false);
            MANAGER.displayOutput(testSession, "", false); // NOI18N
        }

        for (org.netbeans.modules.php.spi.testing.run.TestSuite suite : session.getTestSuites()) {
            MANAGER.displaySuiteRunning(testSession, suite.getName());

            TestSuite testSuite = new TestSuite(suite.getName());
            testSession.addSuite(testSuite);

            for (TestCase kase : suite.getTestCases()) {
                Testcase testCase = new Testcase(kase.getName(), kase.getType(), testSession);
                testCase.setClassName(getClassName(kase, suite));
                testCase.setLocation(getLocation(kase, suite));
                testCase.setTimeMillis(kase.getTime());
                testCase.setStatus(map(kase.getStatus()));

                String[] stacktrace = kase.getStacktrace();
                if (stacktrace.length > 0) {
                    boolean isError = kase.isError();
                    Trouble trouble = new Trouble(isError);
                    trouble.setStackTrace(stacktrace);

                    Diff diff = kase.getDiff();
                    if (diff.isValid()) {
                        Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure(diff.getExpected(), diff.getActual());
                        trouble.setComparisonFailure(failure);
                    }
                    testCase.setTrouble(trouble);
                    MANAGER.displayOutput(testSession, getClassName(kase, suite) + "::"  + kase.getName() + "()", isError); // NOI18N
                    testSession.addOutput("<u>" + kase.getName() + ":</u>"); // NOI18N
                    for (String s : stacktrace) {
                        MANAGER.displayOutput(testSession, s, isError);
                        testSession.addOutput(s.replace("<", "&lt;")); // NOI18N
                    }
                    MANAGER.displayOutput(testSession, "", false); // NOI18N
                    testSession.addOutput(""); // NOI18N
                }
                testSession.addTestCase(testCase);
            }
            MANAGER.displayReport(testSession, testSession.getReport(suite.getTime()));
        }

        String finishMessage = session.getFinishMessage();
        if (finishMessage != null) {
            MANAGER.displayOutput(testSession, finishMessage, false);
        }
        return session;
    }

    @NbBundle.Messages("UnitTestRunner.error.noProviders=No PHP testing provider found, install one via Plugins (e.g. PHPUnit).")
    private boolean checkTestingProviders() {
        if (!project.getTestingProviders().isEmpty()) {
            return true;
        }
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.UnitTestRunner_error_noProviders(), NotifyDescriptor.INFORMATION_MESSAGE));
        return false;
    }

    private void handleCodeCoverage(Coverage coverage) {
        if (!coverageProvider.isEnabled()) {
            return;
        }
        if (!testingProvider.isCoverageSupported(project.getPhpModule())) {
            return;
        }
        if (coverage == null) {
            // some error
            return;
        }
        if (info.allTests()) {
            coverageProvider.setCoverage(coverage);
        } else {
            coverageProvider.updateCoverage(coverage);
        }
    }

    private String getOutputTitle(PhpProject project, TestRunInfo info) {
        StringBuilder sb = new StringBuilder(30);
        sb.append(project.getName());
        String testName = info.getTestName();
        if (testName != null) {
            sb.append(":"); // NOI18N
            sb.append(testName);
        }
        return sb.toString();
    }

    private String getClassName(TestCase testCase, org.netbeans.modules.php.spi.testing.run.TestSuite testSuite) {
        String className = testCase.getClassName();
        if (className != null) {
            return className;
        }
        className = testSuite.getName();
        assert className != null;
        return className;
    }

    private String getLocation(TestCase testCase, org.netbeans.modules.php.spi.testing.run.TestSuite testSuite) {
        Locations.Line locationWithLine = testCase.getLocation();
        if (locationWithLine != null) {
            return FileUtil.toFile(locationWithLine.getFile()).getAbsolutePath();
        }
        FileObject location = testSuite.getLocation();
        assert location != null;
        return FileUtil.toFile(location).getAbsolutePath();
    }

    //~ Mappers

    private OutputLineHandler map(final org.netbeans.modules.php.spi.testing.run.OutputLineHandler outputLineHandler) {
        return new OutputLineHandler() {
            @Override
            public void handleLine(OutputWriter out, String text) {
                outputLineHandler.handleLine(out, text);
            }
        };
    }

    private Status map(TestCase.Status status) {
        return Status.valueOf(status.name());
    }

    private TestSession.SessionType map(TestRunInfo.SessionType type) {
        return TestSession.SessionType.valueOf(type.name());
    }

    //~ Inner classes

    private static final class CallStackCallback implements JumpToCallStackAction.Callback {

        private final PhpProject project;

        public CallStackCallback(PhpProject project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public Locations.Line parseLocation(String callStack) {
            for (PhpTestingProvider testingProvider : project.getTestingProviders()) {
                Locations.Line location = testingProvider.parseFileFromOutput(callStack);
                if (location != null) {
                    return location;
                }
            }
            return null;
        }

    }

}
