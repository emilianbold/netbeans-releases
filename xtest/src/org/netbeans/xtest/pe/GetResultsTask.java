/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
    private String isFailedproperty;
    private boolean onlyUnexpected = true;
    private String mailReportProperty;
    
    private XTestResultsReport report;
    
    public void setResultsDir(File resultsDir) {
        this.resultsDir = resultsDir;
    }
    
    public void setSummaryProperty(String summaryProperty) {
        this.summaryProperty = summaryProperty;
    }
    
    public void setIsFailedProperty(String isFailedproperty) {
        this.isFailedproperty = isFailedproperty;
    }

    public void setMailReportProperty(String mailReportProperty) {
        this.mailReportProperty = mailReportProperty;
    }
    
    public void setOnlyUnexpected(boolean onlyUnexpected) {
        this.onlyUnexpected = onlyUnexpected;
    }
    
    
    
    public void execute() throws BuildException {
        
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

                getProject().setProperty(this.summaryProperty, message);
                // set property only if some tests failed
                if(onlyUnexpected) {
                    // ignore expected fails
                    if(unexpectedFails != 0 || testRun.getTestsError() != 0) {
                        getProject().setProperty(this.isFailedproperty, "true");
                    }
                } else {
                    if(testRun.getTestsFail() != 0 || testRun.getTestsError() != 0) {
                        getProject().setProperty(this.isFailedproperty, "true");
                    }
                }
            }
        }
        
        try {

            File txtResults = transformToTxtReport(resultsDir);
            
            if (mailReportProperty != null) {
               getProject().setProperty(mailReportProperty, txtResults.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("Exception thrown during creating txt report: "+e.getMessage(),e);
        }
        
    }
    
     public File transformToTxtReport(File resultsDir) throws TransformerException, IOException, ClassNotFoundException {
        XTestResultsReport wholeReport = ResultsUtils.loadXTestResultsReport(resultsDir, true);
        
        File txtresultsDir = new File(resultsDir, PEConstants.TXTRESULTS_DIR);
        if (!txtresultsDir.exists())
            txtresultsDir.mkdirs();
        File txtResult = new File(txtresultsDir, PEConstants.TESTREPORT_TXT_FILE);

        String xtestHome = getProject().getProperty("xtest.home");
        if (xtestHome == null)
            throw new BuildException ("xtest.home is not set!");
        Transformer transformer = TransformXMLTask.getTransformer(XSLUtils.getXSLFile(new File(xtestHome),"txtreport.xsl"));
        TransformXMLTask.transform(wholeReport.toDocument(), txtResult, transformer);
        return txtResult;
    }
}
