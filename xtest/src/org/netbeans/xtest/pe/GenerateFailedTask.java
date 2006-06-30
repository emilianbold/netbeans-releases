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

import java.io.File;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.xtest.harness.MTestConfig;
import org.netbeans.xtest.harness.Testbag;
import org.netbeans.xtest.harness.Testbag.InExclude;
import org.netbeans.xtest.harness.Testbag.Patternset;
import org.netbeans.xtest.harness.Testbag.Testset;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.util.FileUtils;
import org.netbeans.xtest.xmlserializer.XMLSerializer;
import org.netbeans.xtest.util.SerializeDOM;

/** Generate config with failed tests from the last test run. New config
 * is written to output directory and named cfg-testtype-failed.xml.
 */
public class GenerateFailedTask extends Task {
    
    /** Suffix added to original config file name. */
    private static final String FAILED_CONFIG_SUFFIX = "-failed";
    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("GenerateFailedTask."+message);
    }
    
    /** Directory where to store generated config. */
    private File outputDir;
    
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    /** Report root Directory. */
    private File inputDir;
    
    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }
    
    public void execute() throws BuildException {
        try {
            log("Generating failed tests config");
            int inputDirType = ResultsUtils.resolveResultsDir(inputDir);
            if(inputDirType != ResultsUtils.TESTREPORT_DIR) {
                log("cannot regenerate (not a suitable input dir)");
            } else {
                generateFailedConfig(inputDir, outputDir);
            }
        } catch (Exception e) {
            log("Exception in GenerateFailedTask:"+e);
            e.printStackTrace(System.err);
        }
    }
    
    /** Generate config with failed tests from the last test run. New config
     * is written to output directory and named cfg-testtype-failed.xml.
     */
    private void generateFailedConfig(File reportRoot, File outputDir) throws Exception {
        // get the root XTestResultsReport
        File xtrFile = new File(ResultsUtils.getXMLResultDir(reportRoot, false),PEConstants.TESTREPORT_FAILURES_XML_FILE);
        FileUtils.checkFileIsFile(xtrFile);
        // get reference to output file
        FileUtils.checkFileIsDirectory(outputDir);
        // get the results report
        XTestResultsReport report = XTestResultsReport.loadFromFile(xtrFile);
        TestRun[] testRuns = report.xmlel_TestRun;
        if(testRuns == null) {
            log("No failed tests found.");
            return;
        }
        // find the last test run
        String lastTestRunID = "";
        int lastTestRunIDIndex = 0;
        for(int i=0;i<testRuns.length;i++) {
            if(testRuns[i].xmlat_runID.compareTo(lastTestRunID) > 1) {
                lastTestRunID = testRuns[i].xmlat_runID;
                lastTestRunIDIndex = i;
            }
        }
        TestRun testRun = testRuns[lastTestRunIDIndex];
        TestBag[] testBags = testRun.xmlel_TestBag;
        if(testBags == null) {
            log("No failed test bags.");
            return;
        }
        // get original test config file (expects test type is the same for all test bags)
        File configFile = new File(reportRoot.getParentFile(), "cfg-"+testBags[0].getTestType()+".xml");
        log("Input config file: "+configFile);
        MTestConfig mconfig = MTestConfig.loadConfig(configFile);
        ArrayList failedTestbags = new ArrayList();
        for(int i=0;i<testBags.length;i++) {
            // find test bag within original config
            Testbag configTestbag = findConfigTestbag(mconfig, testBags[i].getName());
            ArrayList includes = new ArrayList();
            UnitTestSuite[] testSuites = testBags[i].xmlel_UnitTestSuite;
            for(int j=0;j<testSuites.length;j++) {
                UnitTestCase[] testCases = testSuites[j].xmlel_UnitTestCase;
                for(int k=0;k<testCases.length;k++) {
                    String testCase = testCases[k].getClassName()+".class/"+testCases[k].getName();
                    InExclude include = new InExclude();
                    include.setName(testCase);
                    includes.add(include);
                }
            }
            // add all includes to test bag
            Patternset patternset = new Patternset();
            patternset.setIncludes((InExclude[])includes.toArray(new InExclude[includes.size()]));
            Testset testset = configTestbag.getTestsets()[0];
            testset.setPatternsets(new Patternset[] {patternset});
            configTestbag.setTestsets(new Testset[] {testset});
            // add test bag to list of failed test bags
            failedTestbags.add(configTestbag);
        } // test bag
        // only failed test bags should stay in mconfig
        mconfig.setTestbags((Testbag[])failedTestbags.toArray(new Testbag[failedTestbags.size()]));
        // save new config to file
        // new filename = original-failed.xml
        String excludedConfigFilename = new StringBuffer(configFile.getName()).insert(configFile.getName().lastIndexOf('.'), FAILED_CONFIG_SUFFIX).toString();
        File outputFile = new File(outputDir, excludedConfigFilename);
        log("Output config file: "+outputFile);
        // save new config file
        SerializeDOM.serializeToFile(XMLSerializer.toDOMDocument(mconfig), outputFile);
    }
    
    /** Finds test bag with given name in master config. If not found
     * returns null.
     */
    private static Testbag findConfigTestbag(MTestConfig mconfig, String name) {
        Testbag[] configTestbags = mconfig.getTestbags();
        for(int i=0;i<configTestbags.length;i++) {
            if(configTestbags[i].getName().equals(name)) {
                return configTestbags[i];
            }
        }
        return null;
    }
}


