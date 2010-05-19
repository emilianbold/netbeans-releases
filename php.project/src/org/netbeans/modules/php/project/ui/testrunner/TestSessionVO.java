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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.openide.util.NbBundle;

/**
 * Value objects for unit test session.
 * <p>
 * All times are in milliseconds.
 * @author Tomas Mysik
 */
public final class TestSessionVO {
    private final List<TestSuiteVO> testSuites = new ArrayList<TestSuiteVO>();
    private long time = -1;
    private int tests = -1;

    public void addTestSuite(TestSuiteVO testSuite) {
        testSuites.add(testSuite);
    }

    public List<TestSuiteVO> getTestSuites() {
        return testSuites;
    }

    public int getTests() {
        return tests;
    }

    public void setTests(int tests) {
        this.tests = tests;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("TestSessionVO{time: %d, tests: %d, suites: %d}", time, tests, testSuites.size());
    }

    public static final class TestSuiteVO {
        private final List<TestCaseVO> testCases = new ArrayList<TestCaseVO>();
        private final String name;
        private final String file;
        private final long time;

        public TestSuiteVO(String name, String file, long time) {
            assert name != null;
            assert file != null;

            this.name = name;
            this.file = file;
            this.time = time;
        }

        void addTestCase(TestCaseVO testCase) {
            testCases.add(testCase);
        }

        public String getFile() {
            return file;
        }

        public String getName() {
            return name;
        }

        public List<TestCaseVO> getTestCases() {
            checkTestCases();
            return testCases;
        }

        public long getTime() {
            return time;
        }

        private void checkTestCases() {
            if (!testCases.isEmpty()) {
                return;
            }
            testCases.add(TestCaseVO.skippedTestCase());
        }

        @Override
        public String toString() {
            return String.format("TestSuiteVO{name: %s, file: %s, time: %d, cases: %d}", name, file, time, testCases.size());
        }
    }

    public static final class TestCaseVO {
        private final List<String> stacktrace = new ArrayList<String>();
        private final String name;
        private final String file;
        private final int line;
        private final long time;
        private Status status = Status.PASSED;

        public TestCaseVO(String name, String file, int line, long time) {
            assert name != null;

            this.name = name;
            this.file = file;
            this.line = line;
            this.time = time;
        }

        static TestCaseVO skippedTestCase() {
            // suite with no testcases => create a fake with error message
            TestCaseVO testCase = new TestCaseVO(NbBundle.getMessage(TestSuiteVO.class, "LBL_NoTestCasesFound"), null, -1, -1);
            testCase.status = Status.SKIPPED;
            return testCase;
        }

        public String getFile() {
            return file;
        }

        public int getLine() {
            return line;
        }

        public String getName() {
            return name;
        }

        public void addStacktrace(String line) {
            stacktrace.add(line);
        }

        public String[] getStacktrace() {
            return stacktrace.toArray(new String[stacktrace.size()]);
        }

        public long getTime() {
            return time;
        }

        public void setErrorStatus() {
            assert status == Status.PASSED;
            status = Status.ERROR;
        }

        public void setFailureStatus() {
            assert status == Status.PASSED;
            status = Status.FAILED;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isError() {
            return status.equals(Status.ERROR);
        }

        public boolean isFailure() {
            return status.equals(Status.FAILED);
        }

        @Override
        public String toString() {
            return String.format("TestCaseVO{name: %s, file: %s, line: %d, time: %d, status: %s, stacktrace: %s}",
                    name, file, line, time, status, stacktrace);
        }
    }
}
