/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.tools.ant.module.spi.AntSession;
import org.xml.sax.SAXException;

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
    
    /** */
    private final AntSession session;
    /** */
    private final File antScript;
    
    /** */
    private RegexpUtils regexp = RegexpUtils.getInstance();
    
    /** */
    private List/*<String>*/ outputBuffer;
    
    /** */
    private Report topReport;
    /** */
    private Report report;
    /** */
    private Report.Testcase testcase;
    /** */
    private Report.Trouble trouble;
    
    /** */
    private List/*<String>*/ callstackBuffer;
    
    /** */
    private StringBuffer xmlOutputBuffer;
    
    /**
     * Are we reading standard output or standard error output?
     * This variable is used only when reading output from the test cases
     * (when {@link #outputBuffer} is non-<code>null</code>).
     * If <code>true</code>, standard output is being read,
     * if <code>false</code>, standard error output is being read.
     */
    private boolean readingStdOut;
    /** */
    private boolean lastHeaderBrief;
    /** */
    private boolean waitingForIssueStatus;

    /** Creates a new instance of JUnitOutputReader */
    JUnitOutputReader(final AntSession session) {
        this.session = session;
        antScript = session.getOriginatingScript();
    }
    
    /**
     */
    void messageLogged(final String msg) {
        if (msg == null) {
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
                report.reportTestcase(testcase);
                waitingForIssueStatus = false;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (xmlOutputBuffer != null) ...">
        if (xmlOutputBuffer != null) {
            xmlOutputBuffer.append(msg).append('\n');
            if (msg.equals("</testsuite>")) {                           //NOI18N
                closePreviousReport();
                try {
                    String xmlOutput = xmlOutputBuffer.toString();
                    xmlOutputBuffer = null;     //allow GC before parsing XML
                    report = XmlOutputParser.parseXmlOutput(xmlOutput);
                    report.antScript = antScript;
                } catch (SAXException ex) {
                    /* initialization of the parser failed, ignore the output */
                }
            }
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (outputBuffer != null) ...">
        if (outputBuffer != null) {
            if (msg.startsWith(RegexpUtils.OUTPUT_DELIMITER_PREFIX)) {
                Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
                if (matcher.matches() && (matcher.group(1) == null)) {
                    flushOutput();
                    return;
                }
            }
            outputBuffer.add(msg);
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (trouble != null) ...">
        if (trouble != null) {
            Matcher matcher;
            if (trouble.exceptionClsName == null) {
                matcher = regexp.getTestcaseExceptionPattern().matcher(msg);
                if (matcher.matches()) {
                    trouble.exceptionClsName = matcher.group(1);
                    String exceptionMsg = matcher.group(2);
                    if (exceptionMsg != null) {
                        trouble.message = exceptionMsg;
                    }
                }
                return;   //ignore other texts until we get exception class name
            }
            String trimmed = RegexpUtils.specialTrim(msg);
            if (trimmed.length() == 0) {
                if (callstackBuffer != null) {
                    
                    /* finalize the trouble and clear buffers: */
                    trouble.stackTrace = (String[]) callstackBuffer.toArray(
                                            new String[callstackBuffer.size()]);
                    callstackBuffer = null;
                }
                report.reportTestcase(testcase);

                trouble = null;
                testcase = null;
                return;
            }
            if (trimmed.startsWith(RegexpUtils.CALLSTACK_LINE_PREFIX)) {
                matcher = regexp.getCallstackLinePattern().matcher(msg);
                if (matcher.matches()) {
                    if (callstackBuffer == null) {
                        callstackBuffer = new ArrayList(8);
                    }
                    callstackBuffer.add(
                            trimmed.substring(
                                   RegexpUtils.CALLSTACK_LINE_PREFIX.length()));
                    return;
                }
            }
            if ((callstackBuffer == null) && (trouble.message != null)) {
                trouble.message = trouble.message + '\n' + msg;
            }
            /* else: just ignore the text */
            return;
        }//</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="TESTCASE_PREFIX">
        if (msg.startsWith(RegexpUtils.TESTCASE_PREFIX)) {

            if (report == null) {
                return;
            }
            
            String header = msg.substring(RegexpUtils.TESTCASE_PREFIX.length());
            
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
        else if (msg.startsWith(RegexpUtils.OUTPUT_DELIMITER_PREFIX)) {

            if (report == null) {
                return;
            }
            
            Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
            if (matcher.matches()) {
                outputBuffer = new ArrayList/*<String>*/(8);
                
                String delimTypeStr = matcher.group(1);
                assert delimTypeStr.equals(RegexpUtils.STDOUT_LABEL)
                       || delimTypeStr.equals(RegexpUtils.STDERR_LABEL);
                readingStdOut = delimTypeStr.equals(RegexpUtils.STDOUT_LABEL);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="XML_DECL_PREFIX">
        else if (msg.startsWith(RegexpUtils.XML_DECL_PREFIX)) {
            Matcher matcher = regexp.getXmlDeclPattern().matcher(msg.trim());
            if (matcher.matches()) {
                xmlOutputBuffer = new StringBuffer(4096);
                xmlOutputBuffer.append(msg);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_PREFIX">
        else if (msg.startsWith(RegexpUtils.TESTSUITE_PREFIX)) {
            String suiteName = msg.substring(RegexpUtils.TESTSUITE_PREFIX
                                             .length());
            if (regexp.getFullJavaIdPattern().matcher(suiteName).matches()){
                closePreviousReport();
                report = new Report(suiteName);
                report.antScript = antScript;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_STATS_PREFIX">
        else if (msg.startsWith(RegexpUtils.TESTSUITE_STATS_PREFIX)) {

            if (report == null) {
                return;
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
        }//</editor-fold>
    }
    
    /**
     */
    void finishReport(final Throwable exception) {
        if (waitingForIssueStatus) {
            assert testcase != null;
            
            report.reportTestcase(testcase);
        }
        closePreviousReport();
        
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
    }
    
    /**
     */
    Report getReport() {
        return topReport;
    }
    
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
     * Flushes the output buffer to the report's permanent storage
     * and then nulls the output buffer.
     */
    private void flushOutput() {
        assert report != null;
        assert outputBuffer != null;
        
        String output[];
        if (outputBuffer.size() != 0) {
            output = (String[])
                     outputBuffer.toArray(new String[outputBuffer.size()]);
        } else {
            output = null;
        }
        
        if (readingStdOut) {
            report.outputStd = output;
        } else {
            report.outputErr = output;
        }
        
        outputBuffer = null;
    }
    
    /**
     */
    private void closePreviousReport() {
        if (report == null) {                       //no previous report
            return;
        }
        
        callstackBuffer = null;
        xmlOutputBuffer = null;
        outputBuffer = null;
        testcase = null;
        trouble = null;
        
        if (topReport == null) {
            topReport = report;
        } else {
            topReport.appendReport(report);
        }
        report = null;
    }
    
}
