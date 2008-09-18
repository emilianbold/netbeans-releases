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
package org.netbeans.modules.ruby.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FilteredOutput;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.RecognizedOutput;
import org.netbeans.modules.ruby.testrunner.RspecRunner;

/**
 * An output recognizer for parsing output of the rspec runner script, 
 * <code>nb_rspec_mediator.rb</code>. Updates the test result UI.
 *
 * @author Erno Mononen
 */
public class RspecHandlerFactory {

    public static  List<TestRecognizerHandler> getHandlers() {
        List<TestRecognizerHandler> result = new ArrayList<TestRecognizerHandler>();
        result.add(new SuiteStartingHandler());
        result.add(new SuiteStartedHandler());
        result.add(new SuiteFinishedHandler());
        result.add(new TestStartedHandler());
        result.add(new TestFailedHandler());
        result.add(new TestPendingHandler());
        result.add(new TestFinishedHandler());
        result.add(new StackTraceFilterHandler());
        return result;
    }

    static class TestFailedHandler extends TestRecognizerHandler {

        public TestFailedHandler() {
            super(".*%RSPEC_TEST_FAILED%\\s(.*)\\stime=(\\d+\\.\\d+)\\smessage=(.*)\\slocation=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(2));
            testcase.className = matcher.group(1);
            testcase.name = matcher.group(1);
            testcase.trouble = new Report.Trouble(false);
            testcase.trouble.stackTrace = filterStackTrace(matcher.group(3), matcher.group(4));
            session.addTestCase(testcase);
            for (String line : testcase.trouble.stackTrace) {
                manager.displayOutput(session, line, false);
            }
        }

        @Override
        RecognizedOutput getRecognizedOutput() {
            return new FilteredOutput(matcher.group(3), matcher.group(4));
        }
        
        private String[] filterStackTrace(String... stackTrace) {
            List<String> result = new ArrayList<String>();
            for (String location : stackTrace) {
                if (!location.contains(RspecRunner.RSPEC_MEDIATOR_SCRIPT)) {
                    result.add(location);
                }
            }
            return result.toArray(new String[result.size()]);
        }
        
    }

    static class TestStartedHandler extends TestRecognizerHandler {

        public TestStartedHandler() {
            super(".*%RSPEC_TEST_STARTED%\\s(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class TestFinishedHandler extends TestRecognizerHandler {

        public TestFinishedHandler() {
            super(".*%RSPEC_TEST_FINISHED%\\s(.*)\\stime=(.+)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(2));
            testcase.className = session.getSuiteName();
            testcase.name = matcher.group(1);
            session.addTestCase(testcase);
        }
    }

    static class TestPendingHandler extends TestRecognizerHandler {

        public TestPendingHandler() {
            super(".*%RSPEC_TEST_PENDING%\\s(.*)\\stime=(.+)\\smessage=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(2));
            testcase.className = session.getSuiteName();
            testcase.name = matcher.group(1);
            testcase.trouble = new Report.Trouble(false);
            testcase.trouble.stackTrace = new String[]{matcher.group(3)};
            testcase.setStatus(Status.PENDING);
            session.addTestCase(testcase);
        }
    }

    static class SuiteFinishedHandler extends TestRecognizerHandler {

        public SuiteFinishedHandler() {
            super(".*%RSPEC_SUITE_FINISHED%\\s(.+)\\stime=(.+)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            manager.displayReport(session, session.getReport(toMillis(matcher.group(2))));
        }
    }

    static class SuiteStartedHandler extends TestRecognizerHandler {

        public SuiteStartedHandler() {
            super(".*%RSPEC_SUITE_STARTED%\\s.*"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SuiteStartingHandler extends TestRecognizerHandler {

        private boolean firstSuite = true;

        public SuiteStartingHandler() {
            super(".*%RSPEC_SUITE_STARTING%\\s(.+)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            if (firstSuite) {
                firstSuite = false;
                manager.testStarted(session);
            }
            String suiteName = matcher.group(1);
            session.setSuiteName(suiteName);
            manager.displaySuiteRunning(session, suiteName);
        }
    }

    static class StackTraceFilterHandler extends TestRecognizerHandler {

        public StackTraceFilterHandler() {
            super(".*" + RspecRunner.RSPEC_MEDIATOR_SCRIPT + ".*"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }
}
