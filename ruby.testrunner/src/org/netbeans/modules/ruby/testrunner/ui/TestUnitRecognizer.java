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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;

/**
 * An output recognizer for parsing output of the test/unit runner script, 
 * <code>nb_test_mediator.rb</code>. Updates the test result UI.
 *
 * @author Erno Mononen
 */
public class TestUnitRecognizer extends OutputRecognizer {

    private final Manager manager;
    private final TestSession session;
    private final List<TestHandler> handlers;

    public TestUnitRecognizer(Manager manager, TestSession session) {
        this.manager = manager;
        this.session = session;
        this.handlers = initHandlers();
    }

    private List<TestHandler> initHandlers() {
        List<TestHandler> result = new ArrayList<TestHandler>();
        result.add(new SuiteStartingHandler());
        result.add(new SuiteStartedHandler());
        result.add(new SuiteFinishedHandler());
        result.add(new TestStartedHandler());
        result.add(new TestFailedHandler());
        result.add(new TestErrorHandler());
        result.add(new TestFinishedHandler());
        return result;
    }

    @Override
    public void start() {
        manager.testStarted(session);
    }

    @Override
    public RecognizedOutput processLine(String line) {

        for (TestHandler handler : handlers) {
            if (handler.match(line).matches()) {
                handler.updateUI(manager, session);
                return null;
            }
        }

        return null;
    }

    @Override
    public void finish() {
        manager.sessionFinished(session);
    }

    private static int toMillis(String timeInSeconds) {
        Double elapsedTimeMillis = Double.parseDouble(timeInSeconds) * 1000;
        return elapsedTimeMillis.intValue();
    }

    static abstract class TestHandler {

        protected final Pattern pattern;
        protected Matcher matcher;

        public TestHandler(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        final Matcher match(String line) {
            this.matcher = pattern.matcher(line);
            return matcher;
        }

        abstract void updateUI(Manager manager, TestSession session);
    }

    static class TestFailedHandler extends TestHandler {

        public TestFailedHandler() {
            super("%TEST_FAILED%\\stime=(\\d+\\.\\d+)\\sFailure:[.[^\\w]]*(\\w+)\\((\\w+)\\)(.*)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.className = matcher.group(3);
            testcase.name = matcher.group(2);
            testcase.trouble = new Report.Trouble(false);
            testcase.trouble.stackTrace = new String[]{matcher.group(4)};
            session.addTestCase(testcase);
            manager.displayOutput(session, matcher.group(4), false);
        }
    }

    static class TestErrorHandler extends TestHandler {

        public TestErrorHandler() {
            super("%TEST_ERROR%\\stime=(\\d+\\.\\d+)\\sError:[.[^\\w]]*(\\w+)\\((\\w+)\\)(.*)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.className = matcher.group(3);
            testcase.name = matcher.group(2);
            testcase.trouble = new Report.Trouble(true);
            testcase.trouble.stackTrace = new String[]{matcher.group(4)};
            session.addTestCase(testcase);
            manager.displayOutput(session, matcher.group(4), true);

        }
    }

    static class TestStartedHandler extends TestHandler {

        public TestStartedHandler() {
            super("%TEST_STARTED%\\s([\\w]+)\\(([\\w]+)\\)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class TestFinishedHandler extends TestHandler {

        public TestFinishedHandler() {
            super("%TEST_FINISHED%\\stime=(\\d+\\.\\d+)\\s([\\w]+)\\(([\\w]+)\\)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.className = matcher.group(3);
            testcase.name = matcher.group(2);
            session.addTestCase(testcase);
        }
    }

    static class SuiteFinishedHandler extends TestHandler {

        public SuiteFinishedHandler() {
            super("%SUITE_FINISHED%\\s(\\d+\\.\\d+)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            String timeInSeconds = matcher.group(1);
            Double elapsedTimeMillis = Double.parseDouble(timeInSeconds) * 1000;
            Report result = session.getReport();
            result.elapsedTimeMillis = elapsedTimeMillis.intValue();
            manager.displayReport(session, result);
        }
    }

    static class SuiteStartedHandler extends TestHandler {

        public SuiteStartedHandler() {
            super("%SUITE_STARTED%\\s.*");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SuiteStartingHandler extends TestHandler {

        public SuiteStartingHandler() {
            super("%SUITE_STARTING%\\s(\\w+)");
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            String suiteName = matcher.group(1);
            session.setSuiteName(suiteName);
            manager.displaySuiteRunning(session, suiteName);
        }
    }
}
