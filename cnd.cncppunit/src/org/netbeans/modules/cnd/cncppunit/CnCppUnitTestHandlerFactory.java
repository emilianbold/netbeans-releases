/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.cncppunit;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;

/**
 * Sample factory.
 *
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 */
public class CnCppUnitTestHandlerFactory implements TestHandlerFactory {

    private static String C_UNIT = "C Unit Test"; // NOI18N
    private static String CPP_UNIT = "Cpp Unit Test"; // NOI18N

    public List<TestRecognizerHandler> createHandlers() {
        List<TestRecognizerHandler> result = new ArrayList<TestRecognizerHandler>();

        // CppUnit
        result.add(new CppUnitHandler());

        // CUnit
        result.add(new CUnitSuiteStartingHandler());
        result.add(new CUnitSuiteFinishedHandler());
        result.add(new CUnitTestFinishedHandler());
        result.add(new CUnitTestFailedHandler());

        return result;
    }

    public boolean printSummary() {
        return true;
    }

    //
    // CUnit tests output support
    //

    static class CUnitSuiteStartingHandler extends TestRecognizerHandler {

        private boolean firstSuite = true;

        public CUnitSuiteStartingHandler() {
            super("Suite: (.+)"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            if (firstSuite) {
                firstSuite = false;
                manager.testStarted(session);
            }
            String suiteName = matcher.group(1);
            session.addSuite(new TestSuite(suiteName));
            manager.displaySuiteRunning(session, suiteName);
        }
    }

    static class CUnitTestFinishedHandler extends TestRecognizerHandler {

        public CUnitTestFinishedHandler() {
            super("Test: (.*) \\.\\.\\. passed"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            Testcase testcase = new Testcase(matcher.group(1), C_UNIT, session);
            testcase.setTimeMillis(0);
            testcase.setClassName(session.getCurrentSuite().getName());
            session.addTestCase(testcase);
        }
    }

    static class CUnitTestFailedHandler extends TestRecognizerHandler {

        public CUnitTestFailedHandler() {
            super("Test: (.*) \\.\\.\\. FAILED"); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            Testcase testcase = new Testcase(matcher.group(1), C_UNIT, session);
            testcase.setTimeMillis(0);
            testcase.setClassName(session.getCurrentSuite().getName());

            testcase.setTrouble(new Trouble(true));

            session.addTestCase(testcase);
        }
    }

    static class CUnitSuiteFinishedHandler extends TestRecognizerHandler {

        public CUnitSuiteFinishedHandler() {
            super("--Run Summary: "); //NOI18N
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            manager.displayReport(session, session.getReport(0));
            manager.sessionFinished(session);
        }
    }

    //
    // CppUnit tests output support
    //

    static class CppUnitHandler extends TestRecognizerHandler {

        private final List<TestRecognizerHandler> handlers;

        private String currentSuiteName;
        private boolean currentSuiteFinished = false;

        public CppUnitHandler() {
            super("(((.*)::(.+) : .*)|(Run: )|(OK \\())"); //NOI18N

            handlers = new ArrayList<TestRecognizerHandler>();
            handlers.add(new CppUnitTestFinishedHandler());
            handlers.add(new CppUnitTestFailedHandler());
            handlers.add(new CppUnitSuiteFinishedHandler());
        }

        @Override
        public void updateUI(Manager manager, TestSession session) {
            String line = matcher.group(0);
            for (TestRecognizerHandler handler : handlers) {
                if (handler.matches(line)) {
                    handler.updateUI(manager, session);
                    break;
                }
            }
        }

        class CppUnitTestFinishedHandler extends TestRecognizerHandler {

            public CppUnitTestFinishedHandler() {
                super("(.*)::(.+) : OK"); //NOI18N
            }

            @Override
            public void updateUI( Manager manager, TestSession session) {

                String suiteName = matcher.group(1);

                final TestSuite currentSuite = session.getCurrentSuite();
                if (currentSuite == null) {
                    manager.testStarted(session);
                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                    currentSuiteFinished = false;
                } else if(!currentSuite.getName().equals(suiteName)) {
                    if(currentSuite.getName().equals(currentSuiteName) &&
                            !currentSuiteFinished) {
                        manager.displayReport(session, session.getReport(0));
                    }

                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                    currentSuiteFinished = false;
                }
                currentSuiteName = suiteName;

                Testcase testcase = new Testcase(matcher.group(2), CPP_UNIT, session);
                testcase.setTimeMillis(0);
                testcase.setClassName(suiteName);

                session.addTestCase(testcase);
            }
        }

        class CppUnitTestFailedHandler extends TestRecognizerHandler {

            public CppUnitTestFailedHandler() {
                super("(.*)::(.+) : (.*)"); //NOI18N
            }

            @Override
            public void updateUI( Manager manager, TestSession session) {

                String suiteName = matcher.group(1);

                final TestSuite currentSuite = session.getCurrentSuite();
                if (currentSuite == null) {
                    manager.testStarted(session);
                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                } else if(!currentSuite.getName().equals(suiteName)) {
                    if(currentSuite.getName().equals(currentSuiteName) &&
                            !currentSuiteFinished) {
                        manager.displayReport(session, session.getReport(0));
                        currentSuiteFinished = true;
                    }

                    session.addSuite(new TestSuite(suiteName));
                    manager.displaySuiteRunning(session, suiteName);
                }
                currentSuiteName = suiteName;

                Testcase testcase = new Testcase(matcher.group(2), CPP_UNIT, session);
                testcase.setTimeMillis(0);
                testcase.setClassName(suiteName);

                testcase.setTrouble(new Trouble(true));
                String message = matcher.group(3); // NOI18N
                testcase.getTrouble().setStackTrace(getStackTrace(message ,"")); // NOI18N

                session.addTestCase(testcase);
            }
        }

        class CppUnitSuiteFinishedHandler extends TestRecognizerHandler {

            public CppUnitSuiteFinishedHandler() {
                super("((Run: )|(OK \\())"); //NOI18N
            }

            @Override
            public void updateUI( Manager manager, TestSession session) {
                manager.displayReport(session, session.getReport(0));
                manager.sessionFinished(session);
                currentSuiteFinished = true;
            }
        }
    }

    static String[] getStackTrace(String message, String stackTrace) {
        List<String> stackTraceList = new ArrayList<String>();
        stackTraceList.add(message);
        return stackTraceList.toArray(new String[stackTraceList.size()]);
    }

}
