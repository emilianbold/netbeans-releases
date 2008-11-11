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
import java.io.StringReader;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.junit.output.antutils.AntProject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;
import static java.util.Calendar.MILLISECOND;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static org.netbeans.modules.junit.output.RegexpUtils.ADD_ERROR_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.ADD_FAILURE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.END_OF_TEST_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.OUTPUT_DELIMITER_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.START_OF_TEST_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTCASE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TEST_LISTENER_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTS_COUNT_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTSUITE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.TESTSUITE_STATS_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.XML_DECL_PREFIX;

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
    /** number of progress bar workunits */
    private static final int PROGRESS_WORKUNITS = (1 << 15) / 100 * 100;    //sqrt(Integer.MAX), rounded down to hundreds
    /** */
    private static final int INITIAL_PROGRESS = PROGRESS_WORKUNITS / 100;
    
    /** */
    private static final String XML_FORMATTER_CLASS_NAME
            = "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N

    /** */
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    
    /**
     * Does Ant provide detailed information about the currently running test
     * and its output?
     */
    private boolean testListenerInfoAvailable = false;
    /**
     * number of tests to be executed in the current test suite
     * 
     * @see  #executedOneSuiteTests
     */
    private int expectedOneSuiteTests = 0;
    /**
     * number of tests executed within the current suite so far
     * 
     * @see  #expectedOneSuiteTests
     */
    private int executedOneSuiteTests = 0;
    /**
     * number of test suites that are going to be executed
     *
     * @see  #executedSuitesCount
     */
    private int expectedSuitesCount = 0;
    /**
     * number of test suites executed so far
     *
     * @see  #expectedSuitesCount
     */
    private int executedSuitesCount = 0;
    /**
     * did we already get statistics of tests/failures/errors for the current
     * report?
     */
    private boolean testsuiteStatsKnown = false;   //see issue #74979
    
    /** */
    private final AntSession session;
    /** */
    private final TaskType sessionType;
    /**
     * handle to the progress indicator
     */
    private ProgressHandle progressHandle;
    /**
     * whether the progress handle is in determinate mode
     *
     * @see  #progressHandle
     */
    private boolean isDeterminateProgress;
    /**
     * stores progress currently displayed in the progress bar
     */
    private int lastProgress = 0;
    /** */
    private MessageFormat progressStepFormatSuiteName;
    /** */
    private MessageFormat progressStepFormatAnonymous;
    /** whether XML report is expected */
    private boolean expectXmlReport;
    /** */
    private final File antScript;
    /** */
    private final long timeOfSessionStart;
    
    private final Logger LOG;
    private final Logger progressLogger;
    
    /** */
    private RegexpUtils regexp = RegexpUtils.getInstance();
    
    ///** */
    //private Report topReport;
    /** */
    private Report report;
    /** */
    private Report.Testcase testcase;
    /** */
    private Report.Trouble trouble;
    /** */
    private TroubleParser troubleParser;
    /** */
    private String suiteName;
    
    /** */
    private StringBuilder xmlOutputBuffer;
    
    /**
     * Are we reading standard output or standard error output?
     * This variable is used only when reading output from the test cases
     * (when {@link #outputBuffer} is non-<code>null</code>).
     */
    private boolean readingSuiteOutputSummary;
    /** */
    private boolean lastHeaderBrief;
    /** */
    private boolean waitingForIssueStatus;
    /** */
    private final Manager manager = Manager.getInstance();
    /** */
    private String classpath;
    /** */
    private ClassPath platformSources;
    
    
    /** Creates a new instance of JUnitOutputReader */
    JUnitOutputReader(final AntSession session,
                      final TaskType sessionType,
                      final long timeOfSessionStart) {
        this.session = session;
        this.sessionType = sessionType;
        this.antScript = session.getOriginatingScript();
        this.timeOfSessionStart = timeOfSessionStart;
        
        this.progressLogger = Logger.getLogger(
                "org.netbeans.modules.junit.outputreader.progress");    //NOI18N
        this.LOG = Logger.getLogger(getClass().getName());
    }
    
    /**
     */
    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            LOG.finer("VERBOSE MSG: <null>");                           //NOI18N
            return;
        }

        if (LOG.isLoggable(FINER)) {
            LOG.finer("VERBOSE MSG: \"" + msg + '"');                   //NOI18N
        }
        if (progressLogger.isLoggable(FINEST)) {
            progressLogger.finest("VERBOSE: " + msg);                   //NOI18N
        }
        if (msg.startsWith(TEST_LISTENER_PREFIX)) {
            if (report == null) {
                return;
            }
            testListenerInfoAvailable = true;
            String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
            if (testListenerMsg.startsWith(TESTS_COUNT_PREFIX)) {
                String countStr = testListenerMsg.substring(TESTS_COUNT_PREFIX.length());
                try {
                    int count = parseNonNegativeInteger(countStr);
                    if (count > 0) {
                        expectedOneSuiteTests = count;
                        if (progressLogger.isLoggable(FINER)) {
                            progressLogger.finer("expected # of tests in a suite: " + expectedOneSuiteTests);
                        }
                    }
                } catch (NumberFormatException ex) {
                    assert expectedOneSuiteTests == 0;
                }
                return;
            }

            int leftBracketIndex = testListenerMsg.indexOf('(');
            if (leftBracketIndex == -1) {
                return;
            }

            final String shortMsg = testListenerMsg.substring(0, leftBracketIndex);
            if (shortMsg.equals(START_OF_TEST_PREFIX)) {
                boolean testStarted = false;
                String restOfMsg = testListenerMsg.substring(START_OF_TEST_PREFIX.length());
                if (restOfMsg.length() == 0) {
                    testStarted = true;
                } else {
                    char firstChar = restOfMsg.charAt(0);
                    char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                    if ((firstChar == '(') && (lastChar == ')')) {
                        testcase = new Report.Testcase();
                        testcase.name = restOfMsg.substring(1, restOfMsg.length() - 1);
                        testcase.timeMillis = Report.Testcase.NOT_FINISHED_YET;
                        testStarted = true;
                    } else if (!Character.isLetterOrDigit(firstChar)) {
                        testStarted = true;
                    }
                }
                if (testStarted) {
                    progressLogger.finest("test started");              //NOI18N
                }
                return;
            }
            if (shortMsg.equals(END_OF_TEST_PREFIX)) {
                boolean testFinished = false;
                String restOfMsg = testListenerMsg.substring(END_OF_TEST_PREFIX.length());
                if (restOfMsg.length() == 0) {
                    testFinished = true;
                } else {
                    char firstChar = restOfMsg.charAt(0);
                    char lastChar = restOfMsg.charAt(restOfMsg.length() - 1);
                    if ((firstChar == '(') && (lastChar == ')')) {
                        String name = restOfMsg.substring(1, restOfMsg.length() - 1);
                        if (name.equals(testcase.name)) {
                            testFinished = true;
                        }
                    } else if (!Character.isLetterOrDigit(firstChar)) {
                        testFinished = true;
                    }
                }
                if (testFinished) {
                    if (testcase != null) {
                        testcase.timeMillis = Report.Testcase.TIME_UNKNOWN;
                        report.reportTest(testcase, Report.InfoSource.VERBOSE_MSG);
                        testcase = null;
                    }
                    executedOneSuiteTests++;
                    if (expectedOneSuiteTests < executedOneSuiteTests) {
                        expectedOneSuiteTests = executedOneSuiteTests;
                    }
                    progressLogger.finest("test finished");             //NOI18N
                    updateProgress();
                }
                return;
            }
            if (shortMsg.equals(ADD_FAILURE_PREFIX)
                    || shortMsg.equals(ADD_ERROR_PREFIX)) {
                if (testcase == null) {
                    return;
                }
                int lastCharIndex = testListenerMsg.length() - 1;
                if (testListenerMsg.charAt(lastCharIndex) != ')') {
                    return;
                }
                String insideBrackets = testListenerMsg.substring(
                                                        shortMsg.length() + 1,
                                                        lastCharIndex);
                int commaIndex = insideBrackets.indexOf(',');
                String testName = (commaIndex == -1)
                                  ? insideBrackets
                                  : insideBrackets.substring(0, commaIndex);
                if (!testName.equals(testcase.name)) {
                    return;
                }
                testcase.trouble = new Report.Trouble(shortMsg.equals(ADD_ERROR_PREFIX));
                if (commaIndex != -1) {
                    int errMsgStart;
                    if (Character.isSpaceChar(insideBrackets.charAt(commaIndex + 1))) {
                        errMsgStart = commaIndex + 2;
                    } else {
                        errMsgStart = commaIndex + 1;
                    }
                    String troubleMsg = insideBrackets.substring(errMsgStart);
                    if (!troubleMsg.equals("null")) {                   //NOI18N
                        testcase.trouble.message = troubleMsg;
                    }
                }
            }
            return;
        }
        
        /* Look for classpaths: */

        /* Code copied from JavaAntLogger */

        Matcher matcher;

        matcher = RegexpUtils.CLASSPATH_ARGS.matcher(msg);
        if (matcher.find()) {
            this.classpath = matcher.group(1);
        }
        // XXX should also probably clear classpath when taskFinished called
        matcher = RegexpUtils.JAVA_EXECUTABLE.matcher(msg);
        if (matcher.find()) {
            String executable = matcher.group(1);
            ClassPath platformSrcs = findPlatformSources(executable);
            if (platformSrcs != null) {
                this.platformSources = platformSrcs;
            }
        }
    }
    
    /**
     */
    void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            LOG.finer("NORMAL: <null>");                                //NOI18N
            return;
        }

        if (LOG.isLoggable(FINER)) {
            LOG.finer("NORMAL: \"" + msg + '"');                        //NOI18N
        }
        if (progressLogger.isLoggable(FINEST)) {
            progressLogger.finest("NORMAL:  " + msg);                   //NOI18N
        }
        
        if (testListenerInfoAvailable
                && (report != null) && !report.isSuiteFinished()
                && (testcase != null)) {
            displayOutput(msg, event.getLogLevel() == AntEvent.LOG_WARN);
            return;
        }

        //<editor-fold defaultstate="collapsed" desc="if (waitingForIssueStatus) ...">
        if (waitingForIssueStatus) {
            assert testcase != null;
            
            Matcher matcher = regexp.getTestcaseIssuePattern().matcher(msg);
            if (matcher.matches()) {
                boolean error = (matcher.group(1) == null);
            
                trouble = (testcase.trouble = new Report.Trouble(error));
                waitingForIssueStatus = false;
                return;
            } else {
                report.reportTest(testcase);
                waitingForIssueStatus = false;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (xmlOutputBuffer != null) ...">
        if (xmlOutputBuffer != null) {
            xmlOutputBuffer.append(msg).append('\n');
            if (msg.equals("</testsuite>")) {                           //NOI18N
                closePreviousReport();
            }
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (readingSuiteOutputSummary) ...">
        if (readingSuiteOutputSummary) {
            if (msg.startsWith(OUTPUT_DELIMITER_PREFIX)) {
                Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
                if (matcher.matches() && (matcher.group(1) == null)) {
                    readingSuiteOutputSummary = false;
                }
            }
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (trouble != null) ...">
        if (trouble != null) {
            if (troubleParser == null) {
                troubleParser = new TroubleParser(trouble, regexp);
            }
            if (troubleParser.processMessage(msg)) {
                troubleParser = null;
                
                if ((trouble.stackTrace != null) && (trouble.stackTrace.length != 0)) {
                    report.setClasspathSourceRoots();
                }

                if (trouble.isFakeError()) {
                    trouble.error = false;

                    /* fix also the statistics: */
                    report.errors--;
                    report.failures++;
                }

                report.reportTest(testcase);
                
                trouble = null;
                testcase = null;
            }
            return;
        }//</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="TESTCASE_PREFIX">
        if (msg.startsWith(TESTCASE_PREFIX)) {

            if (report == null) {
                return;
            }
            
            String header = msg.substring(TESTCASE_PREFIX.length());
            
            boolean success =
                lastHeaderBrief
                ? tryParseBriefHeader(header)
                    || !(lastHeaderBrief = !tryParsePlainHeader(header))
                : tryParsePlainHeader(header)
                    || (lastHeaderBrief = tryParseBriefHeader(header));
            if (success) {
                waitingForIssueStatus = !lastHeaderBrief;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="OUTPUT_DELIMITER_PREFIX">
        else if (msg.startsWith(OUTPUT_DELIMITER_PREFIX)
                && regexp.getOutputDelimPattern().matcher(msg).matches()) {
            if (report == null) {
                return;
            }
            readingSuiteOutputSummary = true;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="XML_DECL_PREFIX">
        else if (expectXmlReport && msg.startsWith(XML_DECL_PREFIX)) {
            Matcher matcher = regexp.getXmlDeclPattern().matcher(msg.trim());
            if (matcher.matches()) {
                suiteStarted(null);
                
                xmlOutputBuffer = new StringBuilder(4096);
                xmlOutputBuffer.append(msg);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_PREFIX">
        else if (msg.startsWith(TESTSUITE_PREFIX)) {
            suiteName = msg.substring(TESTSUITE_PREFIX.length());
            if (regexp.getFullJavaIdPattern().matcher(suiteName).matches()){
                suiteStarted(suiteName);
                report.resultsDir = determineResultsDir(event);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_STATS_PREFIX">
        else if (msg.startsWith(TESTSUITE_STATS_PREFIX)) {

            if (report == null) {
                return;
            }
            if (testsuiteStatsKnown) {
                return;                     //see issue #74979
            }
            
            Matcher matcher = regexp.getSuiteStatsPattern().matcher(msg);
            if (matcher.matches()) {
                assert report != null;
                
                report.markSuiteFinished();
                try {
                    report.totalTests = Integer.parseInt(matcher.group(1));
                    report.failures = Integer.parseInt(matcher.group(2));
                    report.errors = Integer.parseInt(matcher.group(3));
                } catch (NumberFormatException ex) {
                    //if the string matches the pattern, this should not happen
                    assert false;
                }
                report.elapsedTimeMillis = parseTime(matcher.group(4));
            }
            testsuiteStatsKnown = true;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Test ... FAILED">
        else if ((suiteName != null)
                && msg.startsWith("Test ")                              //NOI18N
                && msg.endsWith(" FAILED")                              //NOI18N
                && msg.equals("Test " + suiteName + " FAILED")) {       //NOI18N
            suiteName = null;
            //PENDING - stop the timer (if any)?
            //PENDING - perform immediate update (if necessary)?
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="output">
        else if (!testListenerInfoAvailable) {
            displayOutput(msg,
                          event.getLogLevel() == AntEvent.LOG_WARN);
        }
        //</editor-fold>
    }

    /**
     */
    private int parseTime(String timeString) {
        int timeMillis;
        try {
            double seconds = numberFormat.parse(timeString).doubleValue();
            timeMillis = Math.round((float) (seconds * 1000.0));
        } catch (ParseException ex) {
            timeMillis = Report.Testcase.TIME_UNKNOWN;
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

        File resultsDir = (todirPath != ".") ? new File(todirPath)      //NOI18N
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
    private Report createReport(final String suiteName) {
        Report report = new Report(suiteName);
        report.antScript = antScript;
        
        report.classpath = classpath;
        report.platformSources = platformSources;
        
        this.classpath = null;
        this.platformSources = null;
        
        return report;
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
        if (progressLogger.isLoggable(FINER)) {
            progressLogger.finer("EXPECTED # OF SUITES: "
                                 + expectedSuitesCount);
        }

        this.expectXmlReport = expectXmlOutput;
        
        final boolean willBeDeterminateProgress = (expectedSuitesCount > 0);
        if (progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "MSG_ProgressMessage"));//NOI18N
            
            if (willBeDeterminateProgress) {
                this.expectedSuitesCount = expectedSuitesCount;
                progressHandle.start(PROGRESS_WORKUNITS);
                progressHandle.progress(INITIAL_PROGRESS);      // 1 %
                lastProgress = INITIAL_PROGRESS;
            } else {
                progressHandle.start();
            }
        } else if (willBeDeterminateProgress) {
            if (!isDeterminateProgress) {
                progressHandle.switchToDeterminate(PROGRESS_WORKUNITS);
            }
            if (progressLogger.isLoggable(FINER)) {
                progressLogger.finer("                    - total # of suites: "
                                     + (this.expectedSuitesCount + expectedSuitesCount));
            }
            this.expectedSuitesCount += expectedSuitesCount;

            /*
             * This is necessary in order to ensure that the subsequent
             * progress update computes the correct value:
             */
            expectedOneSuiteTests = 0;
            executedOneSuiteTests = 0;

            updateProgress();
        } else if (isDeterminateProgress /* and will be indeterminate */ ) {
            progressHandle.switchToIndeterminate();
            lastProgress = 0;
        }//else
            //is indeterminate and will be indeterminate - no change
         //
        isDeterminateProgress = willBeDeterminateProgress;
        
        manager.testStarted(session, sessionType);
    }
    
    /**
     */
    void testTaskFinished() {
        closePreviousReport();

        progressLogger.finer("ACTUAL # OF SUITES: " + executedSuitesCount);

        expectedSuitesCount = executedSuitesCount;
        if (isDeterminateProgress) {
            /*
             * The above assignment statement might set expectedSuitesCount
             * to zero which would cause a "division by zero" exception
             * if method updateProgress() was called. That's why we bypass
             * the method and set the progress bar to 100% directly.
             */
            progressHandle.progress(PROGRESS_WORKUNITS);
        }
    }
    
    /**
     * Updates the progress bar according to the current values of
     * {@link #executedSuitesCount} and {@link #expectedSuitesCount}.
     */
    private void updateProgress() {
        updateProgress(null);
    }

    private void updateProgress(String message) {
        assert progressHandle != null;

        if (isDeterminateProgress) {
            int progress = getProcessedWorkunits();
            if (progressLogger.isLoggable(FINER)) {
                progressLogger.finer(
                    "------ Progress: "                                     //NOI18N
                    + String.format("%3d%%",
                                    100 * progress / PROGRESS_WORKUNITS));  //NOI18N
            }
            if (progress < INITIAL_PROGRESS) {
                progress = INITIAL_PROGRESS;
            }
            if (progress != lastProgress) {
                if (progress < lastProgress) {
                    /* hack to allow decrease of progress: */
                    progressHandle.switchToIndeterminate();
                    progressHandle.switchToDeterminate(PROGRESS_WORKUNITS);
                }
                lastProgress = progress;
                if (message != null) {
                    progressHandle.progress(message, progress);
                } else {
                    progressHandle.progress(progress);
                }
            } else if (message != null) {
                progressHandle.progress(message);
            }
        } else if (message != null) {
            progressHandle.progress(message);
        }
    }
    
    /**
     * Updates the progress message - displays name of the running suite.
     * 
     * @param  suiteName  name of the running suite, or {@code null}
     */
    private String getProgressStepMessage(String suiteName) {
        String msg;
        
        if (isDeterminateProgress) {
            MessageFormat messageFormat;
            Object[] messageParams;
            if (suiteName != null) {
                if (progressStepFormatSuiteName == null) {
                    progressStepFormatSuiteName = new MessageFormat(
                            NbBundle.getMessage(
                                  getClass(),
                                  "MSG_ProgressStepMessage"));          //NOI18N
                }
                messageFormat = progressStepFormatSuiteName;
                messageParams = new Object[] {suiteName,
                                              executedSuitesCount + 1,
                                              expectedSuitesCount};
            } else {
                if (progressStepFormatAnonymous == null) {
                    progressStepFormatAnonymous = new MessageFormat(
                            NbBundle.getMessage(
                                  getClass(),
                                  "MSG_ProgressStepMessageAnonymous")); //NOI18N
                }
                messageFormat = progressStepFormatAnonymous;
                messageParams = new Object[] {executedSuitesCount + 1,
                                              expectedSuitesCount};
            }
            msg = messageFormat.format(messageParams, new StringBuffer(), null)
                  .toString();
        } else {
            msg = (suiteName != null) ? suiteName : "";                 //NOI18N
        }
        return msg;
    }
    
    /**
     *
     */
    private int getProcessedWorkunits() {
        try {
            if (progressLogger.isLoggable(FINEST)) {
                progressLogger.finest("--- Suites: " + executedSuitesCount + " / " + expectedSuitesCount);
                progressLogger.finest("--- Tests:  " + executedOneSuiteTests + " / " + expectedOneSuiteTests);
            }
            int units = executedSuitesCount * PROGRESS_WORKUNITS
                        / expectedSuitesCount;
            if (expectedOneSuiteTests > 0) {
                units += (executedOneSuiteTests * PROGRESS_WORKUNITS)
                         / (expectedSuitesCount * expectedOneSuiteTests);
            }
            return units;
        } catch (Exception ex) {
            return 0;
        }
    }
    
    /**
     */
    void buildFinished(final AntEvent event) {
        try {
            buildFinished(event.getException());

            if (report != null) {
                closePreviousReport(true);  //true ... interrupted
            }

            manager.sessionFinished(session, sessionType);
        } finally {
            progressHandle.finish();
        }
    }
    
    /**
     * Notifies that a test suite was just started.
     *
     * @param  suiteName  name of the suite; or {@code null}
     *                    if the suite name is unknown
     */
    private Report suiteStarted(final String suiteName) {
        closePreviousReport();
        report = createReport(suiteName);
        
        String stepMessage = getProgressStepMessage(suiteName);
        expectedOneSuiteTests = 0;
        executedOneSuiteTests = 0;
        if (expectedSuitesCount <= executedSuitesCount) {
            expectedSuitesCount = executedSuitesCount + 1;
        }
        if (executedSuitesCount != 0) {
            updateProgress(stepMessage);
        } else {
            progressHandle.progress(stepMessage);
        }
                
        manager.displaySuiteRunning(session, sessionType, suiteName);
        return report;
    }
    
    /**
     */
    private void suiteFinished(final Report report, boolean interrupted) {
        if (progressLogger.isLoggable(FINER)) {
            progressLogger.finer("actual # of tests in a suite: " + executedOneSuiteTests);
        }
        executedSuitesCount++;
        
        manager.displayReport(session, sessionType, report);
    }
    
    private void buildFinished(final Throwable exception) {
        //<editor-fold defaultstate="collapsed" desc="disabled code">
        //PENDING:
        /*
        int errStatus = ResultWindow.ERR_STATUS_OK;
        if (exception != null) {
            if (exception instanceof java.lang.ThreadDeath) {
                errStatus = ResultWindow.ERR_STATUS_INTERRUPTED;
            } else {
                errStatus = ResultWindow.ERR_STATUS_EXCEPTION;
            }
        }
         */
        
        /*
        //PENDING: final int status = errStatus;
        Mutex.EVENT.postWriteRequest(new Runnable() {
            public void run() {
                //PENDING:
                //ResultWindow resultView = ResultWindow.getInstance();
                //resultView.displayReport(topReport, status, antScript);
                
                final TopComponent resultWindow = ResultWindow.getDefault();
                resultWindow.open();
                resultWindow.requestActive();
            }
        });
         */
        //</editor-fold>
    }
    
    //------------------ UPDATE OF DISPLAY -------------------
    
    /**
     */
    private void displayOutput(final String text, final boolean error) {
        manager.displayOutput(session, sessionType, text, error);
    }
    
    //--------------------------------------------------------
    
    /**
     */
    private boolean tryParsePlainHeader(String testcaseHeader) {
        assert report != null;
        final Matcher matcher = regexp.getTestcaseHeaderPlainPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String timeString = matcher.group(2);
            
            testcase = report.findTest(methodName);
            testcase.className = null;
            testcase.timeMillis = parseTime(timeString);
            
            trouble = null;
            troubleParser = null;
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     */
    private boolean tryParseBriefHeader(String testcaseHeader) {
        assert report != null;
        final Matcher matcher = regexp.getTestcaseHeaderBriefPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String clsName = matcher.group(2);
            boolean error = (matcher.group(3) == null);

            testcase = report.findTest(methodName);
            testcase.className = clsName;
            testcase.timeMillis = -1;

            trouble = (testcase.trouble = new Report.Trouble(error));
            
            return true;
        } else {
            return false;
        }
    }
    
    private void closePreviousReport() {
        closePreviousReport(false);
    }

    private void closePreviousReport(boolean interrupted) {
        if (xmlOutputBuffer != null) {
            try {
                String xmlOutput = xmlOutputBuffer.toString();
                xmlOutputBuffer = null;     //allow GC before parsing XML
                Report xmlReport;
                xmlReport = XmlOutputParser.parseXmlOutput(
                                                new StringReader(xmlOutput));
                report.update(xmlReport);
            } catch (SAXException ex) {
                /* initialization of the parser failed, ignore the output */
            } catch (IOException ex) {
                assert false;           //should not happen (StringReader)
            }
        } else if (report != null) {
            if (interrupted) {
                if (testcase != null) {
                    report.reportTest(testcase, Report.InfoSource.VERBOSE_MSG);
                    testcase = null;
                }
            } else {
                if (waitingForIssueStatus) {
                    assert testcase != null;
                    report.reportTest(testcase);
                }
                if (report.resultsDir != null) {
                    File reportFile = findReportFile();
                    if ((reportFile != null) && isValidReportFile(reportFile)) {
                        Report fileReport = parseReportFile(reportFile);
                        if (fileReport != null) {
                            report.update(fileReport);
                        }
                    }
                }
            }
            suiteFinished(report, interrupted);
        }
        
        xmlOutputBuffer = null;
        readingSuiteOutputSummary = false;
        testcase = null;
        trouble = null;
        troubleParser = null;
        report = null;
        testsuiteStatsKnown = false;
    }

    private File findReportFile() {
        File file = new File(report.resultsDir,
                             "TEST-" + report.suiteClassName + ".xml"); //NOI18N
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
            logger.log(logLevel, "Session start:    " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(MILLISECOND)));//NOI18N
            
            timeStamp.setTimeInMillis(lastModified);
            logger.log(logLevel, "Report timestamp: " + String.format("%1$tT.%2$03d", timeStamp, timeStamp.get(MILLISECOND)));//NOI18N
        }
        
        if (timeDelta >= 0) {
            return true;
        }
        
        /*
         * Normally we would return 'false' here, but:
         * 
         * We must take into account that modification timestamps of files
         * usually do not hold milliseconds, just seconds.
         * The worst case we must accept is that the session started
         * on YYYY.MM.DD hh:mm:ss.999 and the file was saved exactly in the same
         * millisecond but its time stamp is just YYYY.MM.DD hh:mm:ss, i.e
         * 999 milliseconds earlier.
         */
        return -timeDelta <= timeOfSessionStart % 1000;
        
//        if (timeDelta < -999) {
//            return false;
//        }
//        
//        final GregorianCalendar sessStartCal = new GregorianCalendar();
//        sessStartCal.setTimeInMillis(timeOfSessionStart);
//        int sessStartMillis = sessStartCal.get(MILLISECOND);
//        if (timeDelta < -sessStartMillis) {
//            return false;
//        }
//        
//        final GregorianCalendar fileModCal = new GregorianCalendar();
//        fileModCal.setTimeInMillis(lastModified);
//        if (fileModCal.get(MILLISECOND) != 0) {
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

    private static Report parseReportFile(File reportFile) {
        final long fileSize = reportFile.length();
        if ((fileSize < 0l) || (fileSize > MAX_REPORT_FILE_SIZE)) {
            return null;
        }

        Report fileReport = null;
        try {
            fileReport = XmlOutputParser.parseXmlOutput(
                    new InputStreamReader(
                            new FileInputStream(reportFile),
                            "UTF-8"));                                  //NOI18N
            fileReport.markSuiteFinished();
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
        return fileReport;
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
    
}
