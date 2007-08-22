/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;
import static java.util.Calendar.MILLISECOND;
import static org.netbeans.modules.junit.output.RegexpUtils.END_OF_TEST_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.NESTED_EXCEPTION_PREFIX;
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
    
    private static final int MAX_REPORT_FILE_SIZE = 1 << 19;    //512 kBytes
    /** number of progress bar workunits */
    private static final int PROGRESS_WORKUNITS = 1 << 15;   //sqrt(Integer.MAX)
    /** */
    private static final int INITIAL_PROGRESS = PROGRESS_WORKUNITS / 100;
    /** */
    private static final int UPDATE_DELAY = 300;    //milliseconds
    
    /** */
    private static final String XML_FORMATTER_CLASS_NAME
            = "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";//NOI18N
    
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
    
    private final Logger progressLogger;
    
    /** */
    private RegexpUtils regexp = RegexpUtils.getInstance();
    
    /** */
    private Report topReport;
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
    private StringBuffer xmlOutputBuffer;
    
    /**
     * Are we reading standard output or standard error output?
     * This variable is used only when reading output from the test cases
     * (when {@link #outputBuffer} is non-<code>null</code>).
     * If <code>true</code>, standard output is being read,
     * if <code>false</code>, standard error output is being read.
     */
    private boolean readingOutputReport;
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
    }
    
    /**
     */
    private void log(String msg) {
        progressLogger.log(Level.FINER, msg);
    }
    
    /**
     */
    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        
        log("VERBOSE: " + msg);
        if (msg.startsWith(TEST_LISTENER_PREFIX)) {
            String testListenerMsg = msg.substring(TEST_LISTENER_PREFIX.length());
            if (testListenerMsg.startsWith(TESTS_COUNT_PREFIX)) {
                String countStr = testListenerMsg.substring(TESTS_COUNT_PREFIX.length());
                try {
                    int count = parseNonNegativeInteger(countStr);
                    if (count > 0) {
                        expectedOneSuiteTests = count;
                        log("number of tests: " + expectedOneSuiteTests);
                    }
                } catch (NumberFormatException ex) {
                    assert expectedOneSuiteTests == 0;
                }
                return;
            }
            if (testListenerMsg.startsWith(START_OF_TEST_PREFIX)) {
                String restOfMsg = testListenerMsg.substring(START_OF_TEST_PREFIX.length());
                if ((restOfMsg.length() == 0)
                        || !Character.isLetterOrDigit(restOfMsg.charAt(0))) {
                    log("test started");
                }
                return;
            }
            if (testListenerMsg.startsWith(END_OF_TEST_PREFIX)) {
                String restOfMsg = testListenerMsg.substring(END_OF_TEST_PREFIX.length());
                if ((restOfMsg.length() == 0)
                        || !Character.isLetterOrDigit(restOfMsg.charAt(0))) {
                    log("test finished");
                    executedOneSuiteTests++;
                    updateProgress();
                }
                return;
            }
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
            return;
        }
        log("NORMAL:  " + msg);
        
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
        //<editor-fold defaultstate="collapsed" desc="if (readingOutputReport) ...">
        if (readingOutputReport) {
            if (msg.startsWith(OUTPUT_DELIMITER_PREFIX)) {
                Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
                if (matcher.matches() && (matcher.group(1) == null)) {
                    readingOutputReport = false;
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
                    setClasspathSourceRoots();
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
            readingOutputReport = true;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="XML_DECL_PREFIX">
        else if (expectXmlReport && msg.startsWith(XML_DECL_PREFIX)) {
            Matcher matcher = regexp.getXmlDeclPattern().matcher(msg.trim());
            if (matcher.matches()) {
                suiteStarted(null);
                
                xmlOutputBuffer = new StringBuffer(4096);
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
                
                try {
                    report.totalTests = Integer.parseInt(matcher.group(1));
                    report.failures = Integer.parseInt(matcher.group(2));
                    report.errors = Integer.parseInt(matcher.group(3));
                    report.elapsedTimeMillis
                            = regexp.parseTimeMillis(matcher.group(4));
                } catch (NumberFormatException ex) {
                    //if the string matches the pattern, this should not happen
                    assert false;
                }
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
        else {
            displayOutput(msg,
                          event.getLogLevel() == AntEvent.LOG_WARN);
        }
        //</editor-fold>
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
        String dirName = null;
        
        final String taskName = event.getTaskName();
        if (taskName != null) {
            if (taskName.equals("junit")) {                             //NOI18N
                dirName = determineJunitTaskResultsDir(event);
            } else if (taskName.equals("java")) {                       //NOI18N
                dirName = determineJavaTaskResultsDir(event);
            }
        }
        
        if (dirName != null) {
            resultsDir = new File(dirName);
            if (!resultsDir.isAbsolute()) {
                resultsDir = new File(event.getProperty("basedir"),     //NOI18N
                                      dirName);
            }
            if (!resultsDir.exists() || !resultsDir.isDirectory()) {
                resultsDir = null;
            }
        } else {
            resultsDir = null;
        }
        
        return resultsDir;
    }
    
    /**
     */
    private static String determineJunitTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        
        String dirName = null;
        boolean hasXmlFileOutput = false;
        
        for (TaskStructure taskChild : taskStruct.getChildren()) {
            String taskChildName = taskChild.getName();
            if (taskChildName.equals("batchtest")                       //NOI18N
                    || taskChildName.equals("test")) {                  //NOI18N
                String dirAttr = taskChild.getAttribute("todir");       //NOI18N
                dirName = (dirAttr != null)
                          ? event.evaluate(dirAttr)
                          : ".";                                        //NOI18N
                            /* default is the current directory (Ant manual) */
                
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
                        || "true".equals(event.evaluate(useFileAttr))) {//NOI18N
                        hasXmlFileOutput = true;
                    }
                }
            }
        }
        
        return hasXmlFileOutput ? dirName : null;
    }
    
    /**
     */
    private static String determineJavaTaskResultsDir(final AntEvent event) {
        final TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct == null) {
            return null;
        }
        
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
                            int lastSlashIndex = fullReportFileName.lastIndexOf('/');
                            String dirName = (lastSlashIndex != -1)
                                              ? fullReportFileName.substring(0, lastSlashIndex)
                                              : ".";                    //NOI18N
                            if (dirName.length() != 0) {
                                return dirName;
                            }
                        }
                    }
                }
            }
        }
            
        return null;
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
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher
     * with -cp) and stores it in the current report.
     * <!-- copied from JavaAntLogger -->
     * <!-- XXX: move to class Report -->
     */
    private void setClasspathSourceRoots() {
        
        /* Copied from JavaAntLogger */
        
        if (report == null) {
            return;
        }
        
        if (report.classpathSourceRoots != null) {      //already set
            return;
        }
        
        if (report.classpath == null) {
            return;
        }
        
        Collection<FileObject> sourceRoots = new LinkedHashSet<FileObject>();
        final StringTokenizer tok = new StringTokenizer(report.classpath,
                                                        File.pathSeparator);
        while (tok.hasMoreTokens()) {
            String binrootS = tok.nextToken();
            File f = FileUtil.normalizeFile(new File(binrootS));
            URL binroot;
            try {
                binroot = f.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            if (FileUtil.isArchiveFile(binroot)) {
                URL root = FileUtil.getArchiveRoot(binroot);
                if (root != null) {
                    binroot = root;
                }
            }
            FileObject[] someRoots = SourceForBinaryQuery
                                     .findSourceRoots(binroot).getRoots();
            sourceRoots.addAll(Arrays.asList(someRoots));
        }

        if (report.platformSources != null) {
            sourceRoots.addAll(Arrays.asList(report.platformSources.getRoots()));
        } else {
            // no platform found. use default one:
            JavaPlatform platform = JavaPlatform.getDefault();
            // in unit tests the default platform may be null:
            if (platform != null) {
                sourceRoots.addAll(
                        Arrays.asList(platform.getSourceFolders().getRoots()));
            }
        }
        report.classpathSourceRoots = sourceRoots;
        
        /*
         * The following fields are no longer necessary
         * once the source classpath is defined:
         */
        report.classpath = null;
        report.platformSources = null;
    }
    
    /**
     * Notifies that a test (Ant) task was just started.
     *
     * @param  expectedSuitesCount  expected number of test suites going to be
     *                              executed by this task
     */
    void testTaskStarted(int expectedSuitesCount, boolean expectXmlOutput) {
        this.expectXmlReport = expectXmlOutput;
        
        final boolean willBeDeterminateProgress = (expectedSuitesCount > 0);
        if (progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "MSG_ProgressMessage"));//NOI18N
            
            if (willBeDeterminateProgress) {
                this.expectedSuitesCount = expectedSuitesCount;
                progressHandle.start(PROGRESS_WORKUNITS);
                progressHandle.progress(INITIAL_PROGRESS);      // 1 %
            } else {
                progressHandle.start();
            }
        } else if (willBeDeterminateProgress) {
            if (!isDeterminateProgress) {
                progressHandle.switchToDeterminate(PROGRESS_WORKUNITS);
            }
            this.expectedSuitesCount += expectedSuitesCount;
            updateProgress();
        } else if (isDeterminateProgress /* and will be indeterminate */ ) {
            progressHandle.switchToIndeterminate();
        }//else
            //is indeterminate and will be indeterminate - no change
         //
        isDeterminateProgress = willBeDeterminateProgress;
        
        Manager.getInstance().testStarted(session,
                                          sessionType);
    }
    
    /**
     */
    void testTaskFinished() {
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
        assert progressHandle != null;
        
        int progress = getProcessedWorkunits();
        if (progressLogger.isLoggable(Level.FINER)) {
            log("------ Progress: "                                     //NOI18N
                + String.format("%3d%%",
                                100 * progress / PROGRESS_WORKUNITS));  //NOI18N
        }
        if (progress > INITIAL_PROGRESS) {
            progressHandle.progress(progress);
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
            log("--- Suites: " + executedSuitesCount + " / " + expectedSuitesCount);
            log("--- Tests:  " + executedOneSuiteTests + " / " + expectedOneSuiteTests);
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
            finishReport(event.getException());
            Manager.getInstance().sessionFinished(session,
                                                  sessionType);
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
            int progress = getProcessedWorkunits();
            if (progress > INITIAL_PROGRESS) {
                progressHandle.progress(stepMessage, progress);
            }
        } else {
            progressHandle.progress(stepMessage);
        }
                
        Manager.getInstance().displaySuiteRunning(session,
                                                  sessionType,
                                                  suiteName);
        return report;
    }
    
    /**
     */
    private void suiteFinished(final Report report) {
        executedSuitesCount++;
        
        Manager.getInstance().displayReport(session, sessionType, report);
    }
    
    /**
     */
    void finishReport(final Throwable exception) {
        if (waitingForIssueStatus) {
            assert testcase != null;
            
            report.reportTest(testcase);
        }
        closePreviousReport();
        
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
        Manager.getInstance().displayOutput(session, sessionType, text, error);
    }
    
    //--------------------------------------------------------
    
    /**
     */
    private boolean tryParsePlainHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderPlainPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            int timeMillis = regexp.parseTimeMillisNoNFE(matcher.group(2));
            
            testcase = new Report.Testcase();
            testcase.className = null;
            testcase.name = methodName;
            testcase.timeMillis = timeMillis;
            
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
        final Matcher matcher = regexp.getTestcaseHeaderBriefPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String clsName = matcher.group(2);
            boolean error = (matcher.group(3) == null);

            testcase = new Report.Testcase();
            testcase.className = clsName;
            testcase.name = methodName;
            testcase.timeMillis = -1;

            trouble = (testcase.trouble = new Report.Trouble(error));
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     */
    private void closePreviousReport() {
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
        } else if ((report != null) && (report.resultsDir != null)) {
            /*
             * We have parsed the output but it seems that we also have
             * an XML report file available - let's use it:
             */
            
            File reportFile = new File(
                              report.resultsDir,
                              "TEST-" + report.suiteClassName + ".xml");//NOI18N
            if (reportFile.exists() && isValidReportFile(reportFile)) {
                final long fileSize = reportFile.length();
                if ((fileSize > 0l) && (fileSize <= MAX_REPORT_FILE_SIZE)) {
                    try {
                        Report fileReport;
                        fileReport = XmlOutputParser.parseXmlOutput(
                                new InputStreamReader(
                                        new FileInputStream(reportFile),
                                        "UTF-8"));                      //NOI18N
                        report.update(fileReport);
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
                }
            }
        }
        if (report != null) {
            suiteFinished(report);
        }
        
        xmlOutputBuffer = null;
        readingOutputReport = false;
        testcase = null;
        trouble = null;
        troubleParser = null;
        report = null;
        testsuiteStatsKnown = false;
    }
    
    /**
     */
    private boolean isValidReportFile(File reportFile) {
        if (!reportFile.isFile() || !reportFile.canRead()) {
            return false;
        }
        
        long lastModified = reportFile.lastModified();
        long timeDelta = lastModified - timeOfSessionStart;
        
        final Logger logger = Logger.getLogger("org.netbeans.modules.junit.outputreader.timestamps");//NOI18N
        final Level logLevel = Level.FINER;
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
