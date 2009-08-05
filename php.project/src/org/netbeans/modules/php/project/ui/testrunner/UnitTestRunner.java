/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessors;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.OutputLineHandler;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.util.Exceptions;
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
    private static final PhpOutputLineHandler PHP_OUTPUT_LINE_HANDLER = new PhpOutputLineHandler();

    private final PhpProject project;
    private final TestSession testSession;
    private final boolean allTests;

    private volatile boolean started = false;

    public UnitTestRunner(PhpProject project, TestSession.SessionType sessionType, RerunHandler rerunHandler, boolean allTests) {
        assert project != null;
        assert sessionType != null;
        assert rerunHandler != null;

        this.project = project;
        this.allTests = allTests;

        testSession = new TestSession("PHPUnit test session", project, sessionType, new PhpTestRunnerNodeFactory()); // NOI18N
        testSession.setRerunHandler(rerunHandler);
        testSession.setOutputLineHandler(PHP_OUTPUT_LINE_HANDLER);
    }

    public void start() {
        MANAGER.testStarted(testSession);
        started = true;
    }

    public void showResults() {
        if (!started) {
            throw new IllegalStateException("Test runner must be started. Call start() method first.");
        }
        Reader reader;
        try {
            // #163633 - php unit always uses utf-8 for its xml logs
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(PhpUnit.XML_LOG), "UTF-8")); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return;
        } catch (FileNotFoundException ex) {
            processPhpUnitError();
            return;
        }
        TestSessionVO session = new TestSessionVO();
        PhpUnitLogParser.parse(reader, session);
        if (!PhpUnit.KEEP_LOGS) {
            PhpUnit.XML_LOG.delete();
        }

        if (allTests) {
            // custom suite?
            File customSuite = PhpUnit.getCustomSuite(project);
            if (customSuite != null) {
                MANAGER.displayOutput(testSession, NbBundle.getMessage(UnitTestRunner.class, "MSG_CustomSuiteUsed", customSuite.getAbsolutePath()), false);
                MANAGER.displayOutput(testSession, "", false); // NOI18N
            }
        }

        for (TestSuiteVO suite : session.getTestSuites()) {
            MANAGER.displaySuiteRunning(testSession, suite.getName());

            TestSuite testSuite = new TestSuite(suite.getName());
            testSession.addSuite(testSuite);

            for (TestCaseVO kase : suite.getTestCases()) {
                Testcase testcase = new Testcase(kase.getName(), "PHPUnit test case", testSession); // NOI18N
                testcase.setTimeMillis(kase.getTime());
                testcase.setStatus(kase.getStatus());

                String[] stacktrace = kase.getStacktrace();
                if (stacktrace.length > 0) {
                    boolean isError = kase.isError();
                    Trouble trouble = new Trouble(isError);
                    trouble.setStackTrace(stacktrace);
                    // XXX will be used with php unit 3.4+
//                    Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure("abc\na", "abcd\na");
//                    trouble.setComparisonFailure(failure);
                    testcase.setTrouble(trouble);
                    MANAGER.displayOutput(testSession, suite.getName() + "::"  + kase.getName() + "()", isError); // NOI18N
                    testSession.addOutput("<u>" + kase.getName() + ":</u>"); // NOI18N
                    for (String s : stacktrace) {
                        MANAGER.displayOutput(testSession, s, isError);
                        testSession.addOutput(s);
                    }
                    MANAGER.displayOutput(testSession, "", false); // NOI18N
                    testSession.addOutput(""); // NOI18N
                }
                testSession.addTestCase(testcase);
            }
            MANAGER.displayReport(testSession, testSession.getReport(suite.getTime()));
        }

        MANAGER.displayOutput(testSession, NbBundle.getMessage(UnitTestRunner.class, "MSG_OutputInOutput"), false);
        MANAGER.sessionFinished(testSession);
    }

    private void processPhpUnitError() {
        LOGGER.info(String.format("File %s not found. If there are no errors in PHPUnit output (verify in Output window), "
                + "please report an issue (http://www.netbeans.org/issues/).", PhpUnit.XML_LOG));
        MANAGER.displayOutput(testSession, NbBundle.getMessage(UnitTestRunner.class, "MSG_PerhapsError"), true);
        MANAGER.sessionFinished(testSession);
    }

    private static final class PhpOutputLineHandler implements OutputLineHandler {
        private static final LineConvertor CONVERTOR = LineConvertors.filePattern(null, PhpUnit.LINE_PATTERN, null, 1, 2);

        public void handleLine(OutputWriter out, String text) {
            LineProcessors.printing(out, CONVERTOR, true).processLine(text);
        }
    }
}
