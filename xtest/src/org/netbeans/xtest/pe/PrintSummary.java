/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PrintSummary.java
 *
 * Created on November 5, 2002, 9:11 PM
 */

package org.netbeans.xtest.pe;


import org.apache.tools.ant.*;
import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;


/**
 *
 * @author  breh
 */
public class PrintSummary extends Task {
    
    /** Creates a new instance of PrintSummary */
    public PrintSummary() {
    }
    
    private File resultsDir;
    
    private XTestResultsReport report;
    
    public void setResultsDir(File resultsDir) {
        this.resultsDir = resultsDir;
    }
    
    
    private boolean failOnFailure = false;
    public void setFailOnFailure(boolean fail) {
        failOnFailure=fail;
    }
    
    public boolean failOnFailure() {
        return failOnFailure;
    }
    
    public void execute () throws BuildException {
        
        if (!resultsDir.isDirectory()) {
            log("Cannot print out results summary, supplied result directory is not valid");
            return;
        }
        XTestResultsReport report = null;
        try {
            File reportFile = new File(ResultsUtils.getXMLResultDir(resultsDir),PEConstants.TESTREPORT_XML_FILE);
            report = XTestResultsReport.loadReportFromFile(reportFile);                        
        } catch (IOException ioe) {
            log("Unable to load results, caught IOException :"+ioe);
            return;
        } catch (ClassNotFoundException cnfe) {            
            log("Unable to load results, caught ClassNotFoundException :"+cnfe);
            return;
        }
        
        if (report != null) {
            if (report.xmlel_TestRun != null) {
                TestRun testRun = report.xmlel_TestRun[report.xmlel_TestRun.length-1];
                if (testRun == null) {
                    log("Unable to find results. Weird ");
                }
                // print out the values
                double successRate = 0.0;
                if (testRun.getTestsTotal() != 0) {
                    successRate = ((double)testRun.getTestsPass())/((double)testRun.getTestsTotal());
                }
                String formattedRate = (new java.text.DecimalFormat("##0.00%")).format(successRate);
                log("");
                log(" Test Results Summary (from the current test run):");
                log("");
                log(" Expected Passes: "+(testRun.getTestsPass()-testRun.getTestsUnexpectedPass())+
                "  Unexpected Passes: "+testRun.getTestsUnexpectedPass());
                log(" Expected Fails: "+testRun.getTestsExpectedFail()+
                "  Unexpected Fails: "+(testRun.getTestsFail()-testRun.getTestsExpectedFail())+
                "  Errors: "+testRun.getTestsError());
                log(" Total: "+testRun.getTestsTotal()+
                "  Success Rate: "+formattedRate);
                // this is JDK 1.4 only :-(
                File reportIndex = new File(resultsDir, PEConstants.INDEX_HTML_FILE);
                log("");
                log(" Report URL: "+reportIndex.toURI().toString());
                
                if (failOnFailure()) {
                    if (successRate < 100.0) {
                        throw new BuildException("Some of the tests failed or there were some errors");
                    }
                }
                
            } else {
                throw new BuildException("Cannot find TestRun. results are probably broken");
            }
        } else {
            throw new BuildException("Cannot load test report");
        }
    }

    
}
