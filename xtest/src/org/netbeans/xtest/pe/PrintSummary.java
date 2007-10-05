/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
