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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.project.ui.actions.tests.PhpUnitConstants;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;

/**
 * Test runner UI for PHP unit tests.
 * <p>
 * Currently, only PHPUnit is supported.
 * All the times are in milliseconds.
 * @author Tomas Mysik
 */
public final class UnitTestRunner {
    private static final Logger LOGGER = Logger.getLogger(UnitTestRunner.class.getName());

    private UnitTestRunner() {
    }

    public static void start(TestSession testSession) {
        Manager manager = Manager.getInstance();
        manager.testStarted(testSession);
    }

    public static void display(TestSession testSession) {
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader(PhpUnitConstants.XML_LOG));
        } catch (FileNotFoundException ex) {
            LOGGER.warning(String.format("In order to show test results UI, file %s must exist."
                    + "Report this issue please in http://www.netbeans.org/issues/.", PhpUnitConstants.XML_LOG));
            return;
        }
        TestSessionVO session = new TestSessionVO();
        PhpUnitLogParser.parse(reader, session);

        Manager manager = Manager.getInstance();
        for (TestSuiteVO suite : session.getTestSuites()) {
            manager.displaySuiteRunning(testSession, suite.getName());

            TestSuite testSuite = new TestSuite(suite.getName());
            testSession.addSuite(testSuite);

            for (TestCaseVO kase : suite.getTestCases()) {
                Testcase testcase = new Testcase("PHPUnit test case", testSession); // NOI18N
                testcase.setName(kase.getName());
                testcase.setTimeMillis(kase.getTime());
                testcase.setStatus(kase.getStatus());

                String[] stacktrace = kase.getStacktrace();
                if (stacktrace.length > 0) {
                    boolean isError = kase.isError();
                    Trouble trouble = new Trouble(isError);
                    trouble.setStackTrace(kase.getStacktrace());
                    // XXX will be used with php unit 3.4+
//                    Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure("abc\na", "abcd\na");
//                    trouble.setComparisonFailure(failure);
                    testcase.setTrouble(trouble);
                    manager.displayOutput(testSession, kase.getName() + ":", isError); // NOI18N
                    testSession.addOutput("<u>" + kase.getName() + ":</u>"); // NOI18N
                    for (String s : kase.getStacktrace()) {
                        manager.displayOutput(testSession, s, isError);
                        testSession.addOutput(s);
                    }
                    manager.displayOutput(testSession, "", false); // NOI18N
                    testSession.addOutput(""); // NOI18N
                }
                testSession.addTestCase(testcase);
            }
            manager.displayReport(testSession, testSession.getReport(suite.getTime()));
        }

        manager.sessionFinished(testSession);
    }
}
