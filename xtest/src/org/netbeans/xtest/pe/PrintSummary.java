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
                log(" Passed: "+testRun.getTestsPass()+"  Failed: "+testRun.getTestsFail()
                +"  Errors: "+testRun.getTestsError()+"  Total: "+testRun.getTestsTotal());
                log(" Success Rate: "+formattedRate);
                // this is JDK 1.4 only :-(
                //File reportIndex = new File(resultsDir, PEConstants.INDEX_HTML_FILE);
                //log("\n Report URL: "+reportIndex.toURI().toString());
                String reportURL = "file://"+resultsDir.getPath().replace('\\','/')+"/index.html";
                log("");
                log(" Report URL: "+reportURL);
            }
        }
    }
    
    
    
}
