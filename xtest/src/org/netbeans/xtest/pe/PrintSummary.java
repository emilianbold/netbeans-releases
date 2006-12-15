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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            report = XTestResultsReport.loadFromFile(reportFile);                        
        } catch (IOException ioe) {
            log("Unable to load results, caught IOException :"+ioe);
            log("WARNING: Probably no testbag corresponds to given attributes.");
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
                log("");
                boolean anyUnexpectedFailure = false;
                // check whether there were any unexpectedFailures (e.g. suites not finished, ...)
                if (testRun.xmlel_TestBag != null) {
                    for (int i = 0 ; i < testRun.xmlel_TestBag.length; i++) {
                        TestBag currentTestBag = testRun.xmlel_TestBag[i];
                        String unexpectedMessage = currentTestBag.xmlat_unexpectedFailure;
                        if (unexpectedMessage != null) {
                            // there was some problem
                            anyUnexpectedFailure = true;
                            log(" TestBag '"+currentTestBag.xmlat_name+"' in module "
                                +currentTestBag.xmlat_module
                                +" has encountered an unexpected failure : "+unexpectedMessage);
                        }
                    }
                }
                
                if (testRun.xmlel_ModuleError != null && testRun.xmlel_ModuleError.length > 0) {
                    anyUnexpectedFailure = true;
                    String modules = "";
                    for (int i=0; i<testRun.xmlel_ModuleError.length; i++) {
                        modules += (modules.equals("")?"":", ") + testRun.xmlel_ModuleError[i].getModule();
                    }
                    log("");
                    log(" Execution failed in modules: "+modules);
                }
                 
                File reportIndex = new File(resultsDir, PEConstants.INDEX_HTML_FILE);
                log("");
                log(" Report URL: "+reportIndex.toURI().toString());               
                
                if (failOnFailure()) {                   
                    if ((successRate < 1.0) | (anyUnexpectedFailure)) {
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
