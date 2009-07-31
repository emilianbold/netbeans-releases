/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.junit.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
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
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.api.OutputLine;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.junit.output.antutils.AntProject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import static java.util.logging.Level.FINER;
import static org.netbeans.modules.junit.output.RegexpUtils.ADD_ERROR_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.ADD_FAILURE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.END_OF_TEST_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.START_OF_TEST_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTCASE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TEST_LISTENER_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTS_COUNT_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTSUITE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTSUITE_STATS_PREFIX;

/**
 * Obtains events from a single session of an Ant <code>junit</code> task
 * and builds a {@link Report}.
 * The events are delivered by the {@link JUnitAntLogger}.
 *
 * @see  JUnitAntLogger
 * @see  Report
 * @author  Marian Petras
 */
final class JUnitOutputReader {

    private static final int MAX_REPORT_FILE_SIZE = 1 << 22;    //2 MiB
    /** */
    private static final String XML_FORMATTER_CLASS_NAME
            = "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N

    /** */
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    
    /** */
    private final SessionType sessionType;

    /** whether XML report is expected */
    private boolean expectXmlReport;
    /** */
    private final File antScript;
    /** */
    private final long timeOfSessionStart;
    
    /** */
    private RegexpUtils regexp = RegexpUtils.getInstance();
    
    /** */
    private boolean lastHeaderBrief;
    /** */
    private final Manager manager = Manager.getInstance();
    /** */
    private ClassPath platformSources;
    
    private TestSession testSession;

    private Project project;

    private File resultsDir;

    private JUnitTestcase testcase;

    private Report report;

    enum State {DEFAULT, SUITE_STARTED, TESTCASE_STARTED, SUITE_FINISHED, TESTCASE_ISSUE};

    private State state = State.DEFAULT;

    /** Creates a new instance of JUnitOutputReader */
    JUnitOutputReader(final AntSession session,
                      final AntSessionInfo sessionInfo,
                      final Project project,
                      final Properties props) {
        this.project = project;
        this.sessionType = sessionInfo.getSessionType();
        this.antScript = FileUtil.normalizeFile(session.getOriginatingScript());
        this.timeOfSessionStart = sessionInfo.getTimeOfTestTaskStart();
        if (project == null){
            FileObject fileObj = FileUtil.toFileObject(antScript);
            this.project = FileOwnerQuery.getOwner(fileObj);
        }
        this.testSession = new JUnitTestSession("", this.project, sessionType, new JUnitTestRunnerNodeFactory()); //NOI18N
        testSession.setRerunHandler(new JUnitExecutionManager(session, testSession, props));
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
        verboseMessageLogged(msg);
    }

    synchronized void verboseMessageLogged(String msg) {
        switch(state){
            case SUITE_STARTED: {
                if (msg.startsWith(TEST_LISTENER_PREFIX)) {
                    String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
                    if (testListenerMsg.startsWith(TESTS_COUNT_PREFIX)) {
//                        String countStr = testListenerMsg.substring(TESTS_COUNT_PREFIX.length());
                        return;
                    }

                    int leftBracketIndex = testListenerMsg.indexOf('(');
                    if (leftBracketIndex == -1) {
                        return;
                    }

                    final String shortMsg = testListenerMsg.substring(0, leftBracketIndex);
                    if (shortMsg.equals(START_OF_TEST_PREFIX)) {
                        String restOfMsg = testListenerMsg.substring(START_OF_TEST_PREFIX.length());
                        if (restOfMsg.length() != 0) {
                            char firstChar = restOfMsg.charAt(0);
                            char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                            if ((firstChar == '(') && (lastChar == ')')) {
                                testCaseStarted(restOfMsg.substring(1, restOfMsg.length() - 1));
                            }
                        }
                        return;
                    }
                }
                break;
            }
            case TESTCASE_STARTED: {
                if (msg.startsWith(TEST_LISTENER_PREFIX)) {
                    String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
                    int leftBracketIndex = testListenerMsg.indexOf('(');
                    if (leftBracketIndex == -1) {
                        return;
                    }
                    final String shortMsg = testListenerMsg.substring(0, leftBracketIndex);

                    if (shortMsg.equals(END_OF_TEST_PREFIX)) {
                        String restOfMsg = testListenerMsg.substring(END_OF_TEST_PREFIX.length());
                        if (restOfMsg.length() != 0) {
                            char firstChar = restOfMsg.charAt(0);
                            char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                            if ((firstChar == '(') && (lastChar == ')')) {
                                String name = restOfMsg.substring(1, restOfMsg.length() - 1);
                                if (name.equals(testSession.getCurrentTestCase().getName())) {
                                    testCaseFinished();
                                }
                            }
                        }
                        return;
                    } else if (shortMsg.equals(ADD_FAILURE_PREFIX)
                            || shortMsg.equals(ADD_ERROR_PREFIX)) {
                        int lastCharIndex = testListenerMsg.length() - 1;

                        String insideBrackets = testListenerMsg.substring(
                                                                shortMsg.length() + 1,
                                                                lastCharIndex);
                        int commaIndex = insideBrackets.indexOf(',');
                        String testName = (commaIndex == -1)
                                          ? insideBrackets
                                          : insideBrackets.substring(0, commaIndex);
                        if (!testName.equals(testSession.getCurrentTestCase().getName())) {
                            return;
                        }
                        testSession.getCurrentTestCase().setTrouble(new Trouble(shortMsg.equals(ADD_ERROR_PREFIX)));
                        if (commaIndex != -1) {
                            int errMsgStart;
                            if (Character.isSpaceChar(insideBrackets.charAt(commaIndex + 1))) {
                                errMsgStart = commaIndex + 2;
                            } else {
                                errMsgStart = commaIndex + 1;
                            }
                            String troubleMsg = insideBrackets.substring(errMsgStart);
                            if (!troubleMsg.equals("null")) {                   //NOI18N
                                addStackTraceLine(testSession.getCurrentTestCase(), troubleMsg, false);
                            }
                        }
                        return;
                    }
                }
                break;
            }
            case DEFAULT:
            case SUITE_FINISHED:
            case TESTCASE_ISSUE:
            {
                Matcher matcher = RegexpUtils.JAVA_EXECUTABLE.matcher(msg);
                if (matcher.find()) {
                    String executable = matcher.group(1);
                    ClassPath platformSrcs = findPlatformSources(executable);
                    if (platformSrcs != null) {
                        this.platformSources = platformSrcs;
                    }
                }
                break;
            }
        }
    }

    synchronized void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }

        switch (state){
            case TESTCASE_ISSUE:
            case SUITE_FINISHED:{
                if (msg.startsWith(TESTCASE_PREFIX)) {
                    String header = msg.substring(TESTCASE_PREFIX.length());
                    boolean success =
                        lastHeaderBrief
                        ? tryParseBriefHeader(header)
                            || !(lastHeaderBrief = !tryParsePlainHeader(header))
                        : tryParsePlainHeader(header)
                            || (lastHeaderBrief = tryParseBriefHeader(header));
                    if (success) {
                        state = State.TESTCASE_ISSUE;
                    }
                    break;
                }
            }
            case DEFAULT: {
                if (msg.startsWith(TESTSUITE_PREFIX)) {
                    String suiteName = msg.substring(TESTSUITE_PREFIX.length());
                    if (regexp.getFullJavaIdPattern().matcher(suiteName).matches()){
                        suiteStarted(suiteName);
                        resultsDir = determineResultsDir(event);
                    }
                }

                if (state.equals(State.TESTCASE_ISSUE) && !msg.equals("")){
                    addStackTraceLine(testcase, msg, true);
                }
                break;
            }
            case SUITE_STARTED: {
                if (msg.startsWith(TESTSUITE_STATS_PREFIX)) {
                    Matcher matcher = regexp.getSuiteStatsPattern().matcher(msg);
                    if (matcher.matches()) {
                        try {
                            suiteFinished(Integer.parseInt(matcher.group(1)),
                                          Integer.parseInt(matcher.group(2)),
                                          Integer.parseInt(matcher.group(3)),
                                          parseTime(matcher.group(4)));
                        } catch (NumberFormatException ex) {
                            assert false;
                        }
                    } else {
                        assert false;
                    }
                    break;
                }
            }
            case TESTCASE_STARTED: {
                int posTestListener = msg.indexOf(TEST_LISTENER_PREFIX);
                if (posTestListener != -1) {
                    displayOutput(msg.substring(0, posTestListener), event.getLogLevel() == AntEvent.LOG_WARN);
                    verboseMessageLogged(msg.substring(posTestListener));
                } else {
                    displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
                }
                break;
            }
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
     * @param  event  Ant event serving as a source of information
     * @return  <code>File<code> object representing the results directory,
     *          or <code>null</code> if the results directory could not be
     *          determined
     */
    private static File determineResultsDir(final AntEvent event) {
        File resultsDir = null;
        
        final String taskName = event.getTaskName();
        if (taskName != null) {
            if (taskName.equals("junit")) {                             //NOI18N
                resultsDir = determineJunitTaskResultsDir(event);
            } else if (taskName.equals("java")) {                       //NOI18N
                resultsDir = determineJavaTaskResultsDir(event);
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
    private static File determineJunitTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        
        String todirAttr = null;
        boolean hasXmlFileOutput = false;
        
        for (TaskStructure taskChild : taskStruct.getChildren()) {
            String taskChildName = taskChild.getName();
            if (taskChildName.equals("batchtest")                       //NOI18N
                    || taskChildName.equals("test")) {                  //NOI18N
                todirAttr = taskChild.getAttribute("todir");            //NOI18N
                
            } else if (taskChildName.equals("formatter")) {             //NOI18N
                if (hasXmlFileOutput) {
                    continue;
                }
                String typeAttr = taskChild.getAttribute("type");       //NOI18N
                if ((typeAttr != null)
                        && "xml".equals(event.evaluate(typeAttr))) {    //NOI18N
                    String useFileAttr
                            = taskChild.getAttribute("usefile");        //NOI18N
                    if ((useFileAttr == null)
                        || AntProject.toBoolean(event.evaluate(useFileAttr))) {
                        hasXmlFileOutput = true;
                    }
                }
            }
        }

        if (!hasXmlFileOutput) {
            return null;
        }

        File resultsDir = (todirAttr != null) ? getFile(todirAttr, event)
                                              : getBaseDir(event);
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
                    if (valueAttr.startsWith("formatter=")) {           //NOI18N
                        String formatter = valueAttr.substring("formatter=".length());//NOI18N
                        int commaIndex = formatter.indexOf(',');
                        if ((commaIndex != -1)
                                && formatter.substring(0, commaIndex).equals(XML_FORMATTER_CLASS_NAME)) {
                            String fullReportFileName = formatter.substring(commaIndex + 1);
                            todirPath = new File(fullReportFileName).getParent();
                            if (todirPath == null) {
                                todirPath = ".";                        //NOI18N
                            }
                        }
                    }
                }
            }
        }
            
        if (todirPath == null) {
            return null;
        }

        File resultsDir = (!todirPath.equals(".")) ? new File(todirPath)      //NOI18N
                                             : null;
        return findAbsolutePath(resultsDir, taskStruct, event);
    }

    private static File findAbsolutePath(File path, TaskStructure taskStruct, AntEvent event) {
        if (isAbsolute(path)) {
            return path;
        }

        String forkAttr = taskStruct.getAttribute("fork");              //NOI18N
        if ((forkAttr != null) && AntProject.toBoolean(event.evaluate(forkAttr))) {
            String dirAttr = taskStruct.getAttribute("dir");            //NOI18N
            if (dirAttr != null) {
                path = combine(getFile(dirAttr, event), path);
                if (isAbsolute(path)) {
                    return path;
                }
            }
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
        
        /* Copied from JavaAntLogger */
        
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault()
                                         .getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java");              //NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f.getAbsolutePath().startsWith(javaExecutable)) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }
    
    /**
     * Notifies that a test (Ant) task was just started.
     *
     * @param  expectedSuitesCount  expected number of test suites going to be
     *                              executed by this task
     */
    void testTaskStarted(int expectedSuitesCount, boolean expectXmlOutput) {
        this.expectXmlReport = expectXmlOutput;
        manager.testStarted(testSession);
    }
    
    /**
     */
    void testTaskFinished() {
    }

    private void closePereviousReport(){
        TestSuite currentSuite = testSession.getCurrentSuite();
        if (currentSuite != null){
            //try to get results from report xml file
            if (resultsDir != null) {
                File reportFile = findReportFile();
                if ((reportFile != null) && isValidReportFile(reportFile)) {
                    JUnitTestSuite reportSuite = parseReportFile(reportFile);
                    if ((reportSuite != null) && (reportSuite.getName().equals(currentSuite.getName()))) {
                        lastSuiteTime = reportSuite.getElapsedTime();
                        for(Testcase tc: currentSuite.getTestcases()){
                            if (!tc.getOutput().isEmpty()){
                                List<String> output = new ArrayList();
                                for(OutputLine l: tc.getOutput()){
                                    output.add(l.getLine());
                                }
                                Testcase rtc = findTest(reportSuite, tc.getName());

                                if (rtc != null)
                                    rtc.addOutputLines(output);
                            }
                        }
                        if (!reportSuite.getTestcases().isEmpty()){
                            currentSuite.getTestcases().clear();
                            currentSuite.getTestcases().addAll(reportSuite.getTestcases());
                        }
                    }
                }
            }
            if (report == null){
                report = testSession.getReport(lastSuiteTime);
            }else{
                report.update(testSession.getReport(lastSuiteTime));
            }
            switch(state){
                case SUITE_STARTED:
                case TESTCASE_STARTED:
                    report.setAborted(true);
                default:
                    manager.displayReport(testSession, report, true);
            }
            report = null;
            lastSuiteTime = 0;
        }

    }

    /**
     */
    void buildFinished(final AntEvent event) {
        closePereviousReport();
        manager.sessionFinished(testSession);
    }

    private long lastSuiteTime = 0;

    /**
     * Notifies that a test suite was just started.
     *
     * @param  suiteName  name of the suite; or {@code null}
     *                    if the suite name is unknown
     */
    private void suiteStarted(final String suiteName) {
        closePereviousReport();
        TestSuite suite = new JUnitTestSuite(suiteName, testSession);
        testSession.addSuite(suite);
        manager.displaySuiteRunning(testSession, suite);
        state = State.SUITE_STARTED;
        platformSources = null;
    }
    
    private void suiteFinished(int total, int failures, int errors ,long time) {
        int addFail = failures;
        int addError = errors;
        int addPass = total - failures - errors;
        for(Testcase tc: testSession.getCurrentSuite().getTestcases()){
            switch(tc.getStatus()){
                case ERROR: addError--;break;
                case FAILED: addFail--;break;
                default: addPass--;
            }
        }
        for(int i=0; i<addPass; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.PASSED);
            testSession.addTestCase(tc);
        }
        for(int i=0; i<addFail; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.FAILED);
            testSession.addTestCase(tc);
        }
        for(int i=0; i<addError; i++){
            JUnitTestcase tc = new JUnitTestcase("Unknown", "Unknown", testSession); //NOI18N
            tc.setStatus(Status.ERROR);
            testSession.addTestCase(tc);
        }

        lastSuiteTime = time;
        state = State.SUITE_FINISHED;
    }

    private void testCaseStarted(String name){
        JUnitTestcase tc = new JUnitTestcase(name, "JUnit Test", testSession);
        testSession.addTestCase(tc); //NOI18N
        state = State.TESTCASE_STARTED;
    }

    private void testCaseFinished(){
        if (report == null){
            report = testSession.getReport(0);
        }else{
            report.update(testSession.getReport(0));
        }
        manager.displayReport(testSession, report, false);
        state = State.SUITE_STARTED;
    }

    //------------------ UPDATE OF DISPLAY -------------------
    
    /**
     */
    private void displayOutput(final String text, final boolean error) {
        manager.displayOutput(testSession,text, error);
        if (state == State.TESTCASE_STARTED){
            List<String> addedLines = new ArrayList<String>();
            addedLines.add(text);
            Testcase tc = testSession.getCurrentTestCase();
            if (tc != null){
                tc.addOutputLines(addedLines);
            }
        }
    }
    
    //--------------------------------------------------------
    
    /**
     */
    private boolean tryParsePlainHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderPlainPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String timeString = matcher.group(2);
            
            testcase = findTest(testSession.getCurrentSuite(), methodName);
            testcase.setTimeMillis(parseTime(timeString));
            
            return true;
        } else {
            return false;
        }
    }

    private JUnitTestcase findTest(TestSuite suite, String methodName){
        JUnitTestcase ret = null;
        for(Testcase tcase: suite.getTestcases()){
            if (tcase.getName().equals(methodName)){
                ret = (JUnitTestcase)tcase;
                break;
            }
        }
        return ret;
    }

    /**
     */
    private boolean tryParseBriefHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderBriefPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String clsName = matcher.group(2);
            boolean error = (matcher.group(3) == null);

            testcase = findTest(testSession.getCurrentSuite(), methodName);
            if (testcase == null){ // probably TestListener interface not reported test progress for some reason (for ex. debug mode)
                testcase = new JUnitTestcase(methodName, "JUnit test", testSession);
                testSession.addTestCase(testcase);
            }
            testcase.setClassName(clsName);
            Trouble trouble = testcase.getTrouble();
            if (trouble == null){
                trouble = new Trouble(error);
                testcase.setTrouble(trouble);
            }else{
                trouble.setError(error);
                trouble.setStackTrace(null);
            }

            return true;
        } else {
            return false;
        }
    }
    
    private File findReportFile() {
        File file = new File(resultsDir,
                             "TEST-" + testSession.getCurrentSuite().getName() + ".xml"); //NOI18N
        return (file.isFile() ? file : null);
    }

    /**
     */
    private boolean isValidReportFile(File reportFile) {
        if (!reportFile.canRead()) {
            return false;
        }
        
        long lastModified = reportFile.lastModified();
        long timeDelta = lastModified - timeOfSessionStart;
        
        final Logger logger = Logger.getLogger("org.netbeans.modules.junit.outputreader.timestamps");//NOI18N
        final Level logLevel = FINER;
        if (logger.isLoggable(logLevel)) {
            logger.log(logLevel, "Report file: " + reportFile.getPath());//NOI18N
            
            final GregorianCalendar timeStamp = new GregorianCalendar();
            
            timeStamp.setTimeInMillis(timeOfSessionStart);
            logger.log(logLevel, "Session start:    " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(GregorianCalendar.MILLISECOND)));//NOI18N
            
            timeStamp.setTimeInMillis(lastModified);
            logger.log(logLevel, "Report timestamp: " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(GregorianCalendar.MILLISECOND)));//NOI18N
        }
        
        if (timeDelta >= 0) {
            return true;
        }
        
        return -timeDelta <= timeOfSessionStart % 1000;
        
    }

    private JUnitTestSuite parseReportFile(File reportFile) {
        final long fileSize = reportFile.length();
        if ((fileSize < 0l) || (fileSize > MAX_REPORT_FILE_SIZE)) {
            return null;
        }

        JUnitTestSuite suite = null;
        try {
            suite = XmlOutputParser.parseXmlOutput(
                    new InputStreamReader(
                            new FileInputStream(reportFile),
                            "UTF-8"), testSession);                                  //NOI18N
        } catch (UnsupportedCharsetException ex) {
            assert false;
        } catch (SAXException ex) {
            /* This exception has already been handled. */
        } catch (IOException ex) {
            /*
             * Failed to read the report file - but we still have
             * the report built from the Ant output.
             */
            int severity = ErrorManager.INFORMATIONAL;
            ErrorManager errMgr = ErrorManager.getDefault();
            if (errMgr.isLoggable(severity)) {
                errMgr.notify(
                        severity,
                        errMgr.annotate(
                                ex,
                                "I/O exception while reading JUnit XML report file from JUnit: "));//NOI18N
            }
        }
        return suite;
    }

    /**
     */
    private static int parseNonNegativeInteger(String str)
            throws NumberFormatException {
        final int len = str.length();
        if ((len == 0) || (len > 8)) {
            throw new NumberFormatException();
        }
        
        char c = str.charAt(0);
        if ((c < '0') || (c > '9')) {
            throw new NumberFormatException();
        }
        int result = c - '0';
        
        if (len > 1) {
            for (char d : str.substring(1).toCharArray()) {
                if ((d < '0') || (d > '9')) {
                    throw new NumberFormatException();
                }
                result = 10 * result + (d - '0');
            }
        }
        
        return result;
    }

    private void addStackTraceLine(Testcase testcase, String line, boolean validateST){
        Trouble trouble = testcase.getTrouble();
        if ((trouble == null) || (line == null) || (line.length() == 0) || (line.equals("null"))){  //NOI18N
            return;
        }

        if (validateST){
            boolean valid = false;
            Pattern[] patterns = new Pattern[]{regexp.getCallstackLinePattern(),
                                               regexp.getComparisonHiddenPattern(),
                                               regexp.getFullJavaIdPattern()};
            for(Pattern pattern: patterns){
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()){
                    valid = true;
                    break;
                }
            }
            if (!valid){
                return;
            }
        }
 
        String[] stArray = trouble.getStackTrace();
        if (stArray == null){
            trouble.setStackTrace(new String[]{line});
            Matcher matcher = regexp.getComparisonPattern().matcher(line);
            if (matcher.matches()){
                trouble.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                            matcher.group(1)+matcher.group(2)+matcher.group(3),
                            matcher.group(4)+matcher.group(5)+matcher.group(6))
                );
                return;
            }
            matcher = regexp.getComparisonHiddenPattern().matcher(line);
            if (matcher.matches()){
                trouble.setComparisonFailure(
                        new Trouble.ComparisonFailure(
                            matcher.group(1),
                            matcher.group(2))
                );
                return;
            }

        } else {
            List<String> stList = new ArrayList(Arrays.asList(testcase.getTrouble().getStackTrace()));
            if (!line.startsWith(stList.get(stList.size()-1))){
                stList.add(line);
                trouble.setStackTrace(stList.toArray(new String[stList.size()]));
            }
        }
    }
}
