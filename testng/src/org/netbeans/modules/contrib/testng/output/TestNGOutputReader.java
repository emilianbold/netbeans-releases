/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.contrib.testng.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.gsf.testrunner.api.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 * Obtains events from a single session of an Ant
 * <code>junit</code> task and builds a {@link Report}. The events are delivered
 * by the {@link TestNGAntLogger}.
 *
 * @see TestNGAntLogger
 * @see Report
 * @author Marian Petras
 * @author Lukas Jungmann
 */
final class TestNGOutputReader {

    private static final Logger LOG = Logger.getLogger(TestNGOutputReader.class.getName());
    private static final Logger progressLogger = Logger.getLogger(
            "org.netbeans.modules.contrib.testng.outputreader.progress");
    /**
     *
     */
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    /**
     *
     */
    private final SessionType sessionType;
    /**
     * whether XML report is expected
     */
    private boolean offline;
    private boolean noresults = true;
    /**
     *
     */
    private final File antScript;
    /**
     *
     */
    private final long timeOfSessionStart;
    private long lastSuiteTime = 0;
    /**
     *
     */
    private final Manager manager = Manager.getInstance();
    /**
     *
     */
    private ClassPath platformSources;
    private TestNGTestSession testSession;
    private Project project;
    private File resultsDir;
    private Map<String, Report> reports;

    /**
     * Creates a new instance of TestNGOutputReader
     */
    TestNGOutputReader(final AntSession session,
            final AntSessionInfo sessionInfo,
            final Project project,
            final Properties props) {
        this.project = project;
        this.sessionType = sessionInfo.getSessionType();
        this.antScript = FileUtil.normalizeFile(session.getOriginatingScript());
        this.timeOfSessionStart = sessionInfo.getTimeOfTestTaskStart();
        if (project == null) {
            FileObject fileObj = FileUtil.toFileObject(antScript);
            this.project = FileOwnerQuery.getOwner(fileObj);
        }
        this.testSession = new TestNGTestSession(
                sessionInfo.getSessionName(), this.project, sessionType, new TestNGTestNodeFactory());
        testSession.setRerunHandler(new TestNGExecutionManager(session, testSession, props));
        reports = new HashMap<String, Report>();
    }

    /**
     * for tests
     */
    TestNGOutputReader(TestNGTestSession session) {
        testSession = session;
        sessionType = session.getSessionType();
        antScript = null;
        timeOfSessionStart = System.currentTimeMillis();
        project = session.getProject();
        reports = new HashMap<String, Report>();
    }

    Project getProject() {
        return project;
    }

    TestSession getTestSession() {
        return testSession;
    }

    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        if (!msg.startsWith(RegexpUtils.TEST_LISTENER_PREFIX) || offline) {
            //this message is not for us...
            return;
        }
        if (noresults) noresults = false;
        verboseMessageLogged(msg);
//        displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
    }
    private boolean suiteSummary = false;
    private long elapsedTime = 0;

    private class SuiteStats {

        private String name = null;
        private int testRun = -1;
        private int testFail = -1;
        private int testSkip = -1;
        private int confFail = 0;
        private int confSkip = 0;
    }
    private SuiteStats suiteStat;
    private List<String> txt = new ArrayList<String>();

    /**
     */
    synchronized void verboseMessageLogged(String msg) {
        String in = getMessage(msg);
        //suite starting
        if (in.startsWith("RUNNING: ")) {
            Matcher m = Pattern.compile(RegexpUtils.RUNNING_SUITE_REGEX).matcher(in);
            if (m.matches()) {
                suiteStarted(m.group(1), Integer.valueOf(m.group(2)), m.group(3));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }
        //suite finishing
        if (in.equals("===============================================")) {
            suiteSummary = !suiteSummary;
            if (suiteSummary) {
                suiteStat = new SuiteStats();
            } else {
                suiteFinished(suiteStat);
                suiteStat = null;
            }
            return;
        } else if (suiteSummary) {
            if (suiteStat.name != null) {
                Matcher m = Pattern.compile(RegexpUtils.STATS_REGEX).matcher(in);
                if (suiteStat.testRun < 0) {
                    //Tests run/fail/skip
                    if (m.matches()) {
                        suiteStat.testRun = Integer.valueOf(m.group(1));
                        suiteStat.testFail = Integer.valueOf(m.group(2));
                        suiteStat.testSkip = Integer.valueOf(m.group(4));
                    } else {
                        assert false : "Cannot match: '" + in + "'.";
                    }
                } else {
                    //Configuration fail/skip
                    if (m.matches()) {
                        suiteStat.confFail = Integer.valueOf(m.group(1));
                        suiteStat.confSkip = Integer.valueOf(m.group(2));
                    } else {
                        assert false : "Cannot match: '" + in + "'.";
                    }
                }
            } else {
                suiteStat.name = in.trim();
            }
            return;
        }
        //test
        if (in.startsWith("INVOKING: ")) {
            if (txt.size() > 0) {
                addStackTrace(txt);
                txt.clear();
            }
            Matcher m = Pattern.compile(RegexpUtils.TEST_REGEX).matcher(in);
            if (m.matches()) {
                testStarted(m.group(1), m.group(2), m.group(4), m.group(6));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }

        Matcher m = Pattern.compile(RegexpUtils.TEST_REGEX).matcher(in);
        if (in.startsWith("PASSED: ")) {
            if (m.matches()) {
                testFinished("PASSED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }

        if (in.startsWith("PASSED with failures: ")) {
        }

        if (in.startsWith("SKIPPED: ")) {
            if (m.matches()) {
                testFinished("SKIPPED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }

        if (in.startsWith("FAILED: ")) {
            if (m.matches()) {
                testFinished("FAILED", m.group(1), m.group(2), m.group(4), m.group(6), m.group(8));
            } else {
                assert false : "Cannot match: '" + in + "'.";
            }
            return;
        }

        //configuration methods
        if (in.contains(" CONFIGURATION: ")) {
//            if (txt.size() > 0) {
//                addStackTrace(txt);
//                txt.clear();
//            }
            return;
        }

        Matcher m1 = Pattern.compile(RegexpUtils.RUNNING_SUITE_REGEX).matcher(in);
        if (!(m.matches() || m1.matches())) {
            if (txt.isEmpty() && in.startsWith("       ")) {
                //we received test description
                addDescription(in.trim());
            } else if (in.trim().length() > 0) {
                //we have a stacktrace
                txt.add(in);
            }
        }
    }

    synchronized void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        Testcase tc = testSession.getCurrentTestCase();
        if (tc != null) {
            tc.getOutput().add(new OutputLine(msg, false));
        }
        if (!offline) {
            //log/verbose level = 0 so don't show output
            displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
            verboseMessageLogged(event);
        }
    }

    /**
     */
    private int parseTime(String timeString) {
        int timeMillis;
        try {
            double seconds = numberFormat.parse(timeString).doubleValue();
            timeMillis = Math.round((float) (seconds * 1000.0));
        } catch (ParseException ex) {
            timeMillis = -1;
        }
        return timeMillis;
    }

    /**
     * Tries to determine test results directory.
     *
     * @param event Ant event serving as a source of information
     * @return
     * <code>File<code> object representing the results directory,
     *          or
     * <code>null</code> if the results directory could not be determined
     */
    private static File determineResultsDir(final AntEvent event) {
        File resultsDir = null;

        final String taskName = event.getTaskName();
        if (taskName != null) {
            if (taskName.equals("testng")) {                             //NOI18N
                resultsDir = determineTestNGTaskResultsDir(event);
            } else if (taskName.equals("java")) {                       //NOI18N
                resultsDir = determineJavaTaskResultsDir(event);
            } else {
                assert false : "Unexpected task: " + taskName;
            }
        }

        if ((resultsDir != null) && resultsDir.exists() && resultsDir.isDirectory()) {
            return resultsDir;
        } else {
            return null;
        }
    }

    /**
     */
    private static File determineTestNGTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        String todirAttr = (taskStruct.getAttribute("outputdir") != null) //NOI18N
                ? taskStruct.getAttribute("outputdir") //NOI18N
                : (taskStruct.getAttribute("workingDir") != null) //NOI18N
                ? taskStruct.getAttribute("workingDir") + "test-output" //NOI18N
                : "test-output"; //NOI18N
        File resultsDir = new File(event.evaluate(todirAttr));
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    /**
     */
    private static File determineJavaTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }

        String todirPath = null;

        for (TaskStructure taskChild : taskStruct.getChildren()) {
            String taskChildName = taskChild.getName();
            if (taskChildName.equals("arg")) {                          //NOI18N
                String valueAttr = taskChild.getAttribute("value");     //NOI18N
                if (valueAttr == null) {
                    valueAttr = taskChild.getAttribute("line");         //NOI18N
                }
                if (valueAttr != null) {
                    valueAttr = event.evaluate(valueAttr);
                    int index = valueAttr.indexOf("-d "); //NOI18N
                    if (-1 < index) {
                        todirPath = valueAttr.substring(index + 3);
                        if (todirPath.contains(" ")) {
                            index = todirPath.startsWith("\"") //NOI18N
                                    ? todirPath.indexOf("\"", 1) + 1 //NOI18N
                                    : todirPath.indexOf(" "); //NOI18N
                            todirPath = todirPath.substring(0, index);
                            //found, let's finish
                            break;
                        }
                    }
                }
            }
        }

        if (todirPath == null) {
            //-d not set, what about parent java/exec's 'dir'?
            String dir = taskStruct.getAttribute("dir");
            if (dir != null) {
                todirPath = event.evaluate(dir) + "/test-output";
            } else {
                todirPath = "test-output";
            }
        }
        File resultsDir = new File(event.evaluate(todirPath));
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    private static File findAbsolutePath(File path, TaskStructure taskStruct, AntEvent event) {
        if (isAbsolute(path)) {
            return path;
        }
        return combine(getBaseDir(event), path);
    }

    private static File combine(File parentPath, File path) {
        return (path != null) ? new File(parentPath, path.getPath())
                : parentPath;
    }

    private static boolean isAbsolute(File path) {
        return (path != null) && path.isAbsolute();
    }

    private static File getFile(String attrValue, AntEvent event) {
        return new File(event.evaluate(attrValue));
    }

    private static File getBaseDir(AntEvent event) {
        return new File(event.getProperty("basedir"));                  //NOI18N
    }

    /**
     */
    private ClassPath findPlatformSources(final String javaExecutable) {

        /*
         * Copied from JavaAntLogger
         */

        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java");              //NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                //XXX - look for a "subpath" in case of forked JRE; is there a better way?
                String path = f.getAbsolutePath();
                if (path.startsWith(javaExecutable)
                        || javaExecutable.startsWith(path.substring(0, path.length() - 8))) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }

    /**
     * Notifies that a test (Ant) task was just started.
     */
    void testTaskStarted(boolean expectXmlOutput, AntEvent event) {
        this.offline = expectXmlOutput;
        if (!offline) {
            manager.testStarted(testSession);
        }
        resultsDir = determineResultsDir(event);
    }

    /**
     */
    void testTaskFinished() {
        if (offline) {
            manager.testStarted(testSession);
        }
        if (offline || noresults) {
            //get results from report xml file
            if (resultsDir != null) {
                File reportFile = findReportFile();
                if ((reportFile != null) && isValidReportFile(reportFile)) {
                    XmlResult reportSuite = parseReportFile(reportFile, testSession);
                    for (TestNGTestSuite ts : reportSuite.getTestSuites()) {
                        manager.displaySuiteRunning(testSession, ts);
                        testSession.setCurrentSuite(ts.getName());
                        testSession.addSuite(ts);
                        Report report = testSession.getReport(ts.getElapsedTime());
                        manager.displayReport(testSession, report, true);
                    }
                }
            }
        }
    }

    /**
     */
    void buildFinished(final AntEvent event) {
        manager.sessionFinished(testSession);
    }

    //------------------ UPDATE OF DISPLAY -------------------
    /**
     */
    private void displayOutput(final String text, final boolean error) {
        manager.displayOutput(testSession, text, error);
//        if (state == State.TESTCASE_STARTED) {
//            List<String> addedLines = new ArrayList<String>();
//            addedLines.add(text);
//            Testcase tc = testSession.getCurrentTestCase();
//            if (tc != null) {
//                tc.addOutputLines(addedLines);
//            }
//        }
    }

    //--------------------------------------------------------
    private File findReportFile() {
        File file = new File(resultsDir, "testng-results.xml"); //NOI18N
        return (file.isFile() ? file : null);
    }

    /**
     */
    private boolean isValidReportFile(File reportFile) {
        if (!reportFile.canRead()) {
            return false;
        }

        if (reportFile.canRead()) {
            return true;
        }

        long lastModified = reportFile.lastModified();
        long timeDelta = lastModified - timeOfSessionStart;

        final Logger logger = Logger.getLogger("org.netbeans.modules.contrib.testng.outputreader.timestamps");//NOI18N
        final Level logLevel = Level.FINER;
        if (logger.isLoggable(logLevel)) {
            logger.log(logLevel, "Report file: " + reportFile.getPath());//NOI18N

            final GregorianCalendar timeStamp = new GregorianCalendar();

            timeStamp.setTimeInMillis(timeOfSessionStart);
            logger.log(logLevel, "Session start:    " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(Calendar.MILLISECOND)));//NOI18N

            timeStamp.setTimeInMillis(lastModified);
            logger.log(logLevel, "Report timestamp: " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(Calendar.MILLISECOND)));//NOI18N
        }

        if (timeDelta >= 0) {
            return true;
        }

        /*
         * Normally we would return 'false' here, but:
         *
         * We must take into account that modification timestamps of files
         * usually do not hold milliseconds, just seconds. The worst case we
         * must accept is that the session started on YYYY.MM.DD hh:mm:ss.999
         * and the file was saved exactly in the same millisecond but its time
         * stamp is just YYYY.MM.DD hh:mm:ss, i.e 999 milliseconds earlier.
         */
        return -timeDelta <= timeOfSessionStart % 1000;

//        if (timeDelta < -999) {
//            return false;
//        }
//
//        final GregorianCalendar sessStartCal = new GregorianCalendar();
//        sessStartCal.setTimeInMillis(timeOfSessionStart);
//        int sessStartMillis = sessStartCal.get(Calendar.MILLISECOND);
//        if (timeDelta < -sessStartMillis) {
//            return false;
//        }
//
//        final GregorianCalendar fileModCal = new GregorianCalendar();
//        fileModCal.setTimeInMillis(lastModified);
//        if (fileModCal.get(Calendar.MILLISECOND) != 0) {
//            /* So the file's timestamp does hold milliseconds! */
//            return false;
//        }
//
//        /*
//         * Now we know that milliseconds are not part of file's timestamp.
//         * Let's substract the milliseconds part and check whether the delta is
//         * non-negative, now that we only check seconds:
//         */
//        return lastModified >= (timeOfSessionStart - sessStartMillis);
    }

    private static XmlResult parseReportFile(File reportFile, TestSession session) {
        XmlResult reports = null;
        try {
            reports = XmlOutputParser.parseXmlOutput(
                    new InputStreamReader(
                    new FileInputStream(reportFile),
                    "UTF-8"), session);                                  //NOI18N
        } catch (UnsupportedCharsetException ex) {
            assert false;
        } catch (SAXException ex) {
            /*
             * This exception has already been handled.
             */
        } catch (IOException ex) {
            /*
             * Failed to read the report file - but we still have the report
             * built from the Ant output.
             */
            Logger.getLogger(TestNGOutputReader.class.getName()).log(Level.INFO, "I/O exception while reading TestNG XML report file from TestNG: ", ex);//NOI18N
        }
        return reports;
    }

    private void suiteStarted(String name, int expectedTCases, String config) {
        TestSuite suite = new TestNGTestSuite(name, testSession, expectedTCases, config);
        testSession.addSuite(suite);
        testSession.setCurrentSuite(name);
        manager.displaySuiteRunning(testSession, suite);
        platformSources = null;
        reports.put(name, new Report(name, project));
    }

    private void suiteFinished(SuiteStats stats) {
        testSession.setCurrentSuite(stats.name);
        TestNGTestSuite s = (TestNGTestSuite) testSession.getCurrentSuite();
        s.setElapsedTime(elapsedTime);
        s.finish(stats.testRun, stats.testFail, stats.testSkip, stats.confFail, stats.confSkip);
        Report r = reports.get(stats.name);
        r.setElapsedTimeMillis(elapsedTime);
        manager.displayReport(testSession, r, true);
        elapsedTime = 0;
    }

    private void testStarted(String suiteName, String testCase, String parameters, String values) {
        testSession.setCurrentSuite(suiteName);
        TestNGTestcase tc = ((TestNGTestSuite) ((TestNGTestSession) testSession).getCurrentSuite()).getTestCase(testCase, values);
        if (tc == null) {
            tc = new TestNGTestcase(testCase, parameters, values, testSession);
            testSession.addTestCase(tc);
            manager.testStarted(testSession);
            Report r = reports.get(suiteName);
            r.update(testSession.getReport(0));
            manager.displayReport(testSession, r, false);
        } else {
            tc.addValues(values);
            //TODO: increment test case time
        }
    }

    private void testFinished(String st, String suiteName, String testCase, String parameters, String values, String duration) {
        testSession.setCurrentSuite(suiteName);
        TestNGTestcase tc = ((TestNGTestSuite) ((TestNGTestSession) testSession).getCurrentSuite()).getTestCase(testCase, values);
        if (tc == null) {
            //TestNG does not log invoke message for junit tests...
            tc = new TestNGTestcase(testCase, parameters, values, testSession);
            testSession.addTestCase(tc);
            manager.testStarted(testSession);
        }
        assert tc != null;
        if ("PASSED".equals(st)) {
            tc.setStatus(Status.PASSED);
        } else if ("FAILED".equals(st)) {
            tc.setStatus(Status.FAILED);
        } else if ("SKIPPED".equals(st)) {
            tc.setStatus(Status.SKIPPED);
        }
        long dur = 0;
        if (duration != null) {
            dur = Long.valueOf(duration);
        }
        tc.setTimeMillis(dur);
        elapsedTime += dur;
        Report r = reports.get(suiteName);
        r.update(testSession.getReport(dur));
        manager.displayReport(testSession, r, false);
    }

    private String getMessage(String msg) {
        int prefixLength = RegexpUtils.TEST_LISTENER_PREFIX.length();
        return msg.substring(prefixLength);
    }

    private void addDescription(String in) {
        Testcase tc = testSession.getCurrentTestCase();
        //FIXME!!! tc should never be null
        //looks like some bug :-(
        if (tc != null) {
            ((TestNGTestcase) tc).setDescription(in);
        }
    }

    private void addStackTrace(List<String> txt) {
        Trouble t = new Trouble(false);
        Matcher matcher = RegexpUtils.getInstance().getComparisonPattern().matcher(txt.get(0));
        if (matcher.matches()) {
            t.setComparisonFailure(
                    new Trouble.ComparisonFailure(
                    matcher.group(1) + matcher.group(2) + matcher.group(3),
                    matcher.group(4) + matcher.group(5) + matcher.group(6)));
        } else {
            matcher = RegexpUtils.getInstance().getComparisonHiddenPattern().matcher(txt.get(0));
            if (matcher.matches()) {
                t.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                        matcher.group(1),
                        matcher.group(2)));
            }
        }
        t.setStackTrace(txt.toArray(new String[txt.size()]));
        testSession.getCurrentTestCase().setTrouble(t);
    }
}
