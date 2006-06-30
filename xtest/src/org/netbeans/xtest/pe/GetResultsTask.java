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

package org.netbeans.xtest.pe;


import org.apache.tools.ant.*;
import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import javax.xml.transform.*;
import org.netbeans.xtest.util.XSLUtils;

/**
 * Get summary results in text form from given results dir. Set property
 * if some test failed.
 */
public class GetResultsTask extends Task {

    public GetResultsTask() {
    }

    private File resultsDir;
    private String summaryProperty;
    private boolean onlyWhenFailed = false;
    private boolean ignoreExpected = true;
    private String mailReportProperty;
    
    private XTestResultsReport report;
    
    public void setResultsDir(File resultsDir) {
        this.resultsDir = resultsDir;
    }
    
    public void setSummaryProperty(String summaryProperty) {
        this.summaryProperty = summaryProperty;
    }
    
    public void setOnlyWhenFailed(boolean onlyWhenFailed) {
        this.onlyWhenFailed = onlyWhenFailed;
    }

    public void setMailReportProperty(String mailReportProperty) {
        this.mailReportProperty = mailReportProperty;
    }

    public void setIgnoreExpected(boolean ignoreExpected) {
        this.ignoreExpected = ignoreExpected;
    }
    
    
    
    public void execute() throws BuildException {
        
        if (resultsDir == null || !resultsDir.isDirectory()) 
            throw new BuildException("Supplied result directory is not valid");
        if (mailReportProperty == null) 
            throw new BuildException("Property mailReportProperty is empty");
            
        XTestResultsReport report = null;
        try {
            File reportFile = new File(ResultsUtils.getXMLResultDir(resultsDir),PEConstants.TESTREPORT_XML_FILE);
            report = XTestResultsReport.loadFromFile(reportFile);
        } catch (IOException ioe) {
            log("Unable to load results, caught IOException :"+ioe);
            return;
        } catch (ClassNotFoundException cnfe) {
            log("Unable to load results, caught ClassNotFoundException :"+cnfe);
            return;
        }
        
        boolean failed = false;
        if (report != null) {
            if (report.xmlel_TestRun != null) {
                TestRun testRun = report.xmlel_TestRun[report.xmlel_TestRun.length-1];
                if (testRun == null) {
                    log("Unable to find results. Weird ");
                    return;
                }
                // print out the values
                double successRate = 0.0;
                if (testRun.getTestsTotal() != 0) {
                    successRate = ((double)testRun.getTestsPass())/((double)testRun.getTestsTotal());
                }
                String formattedRate = (new java.text.DecimalFormat("##0.00%")).format(successRate);
                long unexpectedFails = testRun.getTestsFail()-testRun.getTestsExpectedFail();
                String message = formattedRate;
                if ((testRun.getTestsPass()-testRun.getTestsUnexpectedPass()) > 0)
                    message += ", "+(testRun.getTestsPass()-testRun.getTestsUnexpectedPass())+" exp.pass";
                if (testRun.getTestsUnexpectedPass() > 0)
                    message += ", "+testRun.getTestsUnexpectedPass()+" unexp.pass";
                if (testRun.getTestsExpectedFail() > 0)
                    message += ", "+testRun.getTestsExpectedFail()+" exp.fail";
                if (unexpectedFails > 0)
                    message += ", "+unexpectedFails+" unexp.fail";
                if (testRun.getTestsError() > 0)
                    message += ", "+testRun.getTestsError()+" error";

                if (summaryProperty != null) {
                    getProject().setProperty(this.summaryProperty, message);
                }
                // set property only if some tests failed
                if(ignoreExpected) {
                    // ignore expected fails
                    if(unexpectedFails != 0 || testRun.getTestsError() != 0) {
                        failed = true;
                    }
                } else {
                    if(testRun.getTestsFail() != 0 || testRun.getTestsError() != 0) {
                        failed = true;
                    }
                }
            }
        }
        
        try {
            if (!onlyWhenFailed || (onlyWhenFailed && failed)) {
                File txtResults = transformToTxtReport(resultsDir, onlyWhenFailed);

                if (mailReportProperty != null) {
                   getProject().setProperty(mailReportProperty, txtResults.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("Exception thrown during creating txt report: "+e.getMessage(),e);
        }
        
    }
    
     public File transformToTxtReport(File resultsDir, boolean onlyFailures) throws TransformerException, IOException, ClassNotFoundException {
        XTestResultsReport wholeReport = ResultsUtils.loadXTestResultsReport(resultsDir, true);
        File txtresultsDir = new File(resultsDir, PEConstants.TXTRESULTS_DIR);
        if (!txtresultsDir.exists())
            txtresultsDir.mkdirs();
        File txtResult = new File(txtresultsDir, PEConstants.TESTREPORT_TXT_FILE);

        String xtestHome = getProject().getProperty("xtest.home");
        if (xtestHome == null)
            throw new BuildException ("xtest.home is not set!");
        Transformer transformer = TransformXMLTask.getTransformer(XSLUtils.getXSLFile(new File(xtestHome),"txtreport.xsl"));
        transformer.setParameter("onlyFailures", Boolean.toString(onlyFailures));
        TransformXMLTask.transform(wholeReport.toDocument(), txtResult, transformer);
        return txtResult;
    }
}
