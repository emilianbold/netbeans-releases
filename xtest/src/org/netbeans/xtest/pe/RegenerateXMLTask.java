/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * RegenerateXMLTask.java
 *
 * Created on November 13, 2001, 6:48 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.util.FileUtils;
import java.util.*;
import org.w3c.dom.*;
import org.netbeans.xtest.util.SerializeDOM;

// move ant task - i'm lazy to write my own :-)))
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;

/**
 *
 * @author  mb115822
 * @version 
 */
public class RegenerateXMLTask extends Task{


    
    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("RegenerateXMLTask."+message);
    }
    
    
    /** Creates new AggregatorTask */
    public RegenerateXMLTask() {
    }
    
    
    
    private File outputDir;
    
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    private File inputDir;
    
    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }
    
    private boolean serverUsage = false;

    public void setServerUsage(boolean serverUsage) {
        this.serverUsage = serverUsage;
    }
    
    public TestBag getTestBag() throws Exception {
        return ResultsUtils.getTestBag(new File(inputDir,PEConstants.TESTBAG_XML_FILE));
    }
    
    
    
    public SystemInfo getSystemInfo() {
        try {
            Document doc = ResultsUtils.getDOMDocFromFile(new File(inputDir,"systeminfo.xml"));
            XMLBean xmlBean = XMLBean.getXMLBean(doc);    
            if (xmlBean instanceof SystemInfo) {
                return (SystemInfo)xmlBean;
            } else {
                return new SystemInfo();
            }
        } catch (Exception e) {
            return new SystemInfo();
        }
    }
    
    public TestRun getTestRun() {
       return ResultsUtils.getTestRun(new File(inputDir,PEConstants.TESTRUN_XML_FILE));
    }
    
    
    public XTestResultsReport getXTestResultsReport()    {
        return ResultsUtils.getXTestResultsReport(new File(inputDir,PEConstants.TESTREPORT_XML_FILE));
    }    
    
    public UnitTestSuite[] getUnitTestSuites() throws Exception {
         File suiteDir = new File(inputDir,"suites");
         return ResultsUtils.getUnitTestSuites(suiteDir);
    }
    

    
    public void execute () throws BuildException {
        try {
            log("Regenerating report's XMLs");
                        
            Project antProject = getProject();
            String fullrun = antProject.getProperty("xtest.fullrun");
            // set xtest.machine to be used in SystemInfo class. Test4U sets this property.
            String xtestMachine = antProject.getProperty("xtest.machine");
            if(xtestMachine != null) {
                System.setProperty("xtest.machine", xtestMachine);
            }
         
            // lets regenerate 
            int inputDirType = ResultsUtils.resolveResultsDir(inputDir);
            switch (inputDirType) {
                case ResultsUtils.TESTBAG_DIR:
                case ResultsUtils.TESTRUN_DIR:
                case ResultsUtils.TESTREPORT_DIR:
                    regenerateXMLs(inputDir,false,false,serverUsage);
                    break;
                case ResultsUtils.UNKNOWN_DIR:
                    regenerateTestReport(inputDir,false,false,serverUsage);
                    break;
                default:
                    log("cannot regenerate (not a suitable input dir)");
            }
            
        } catch (Exception e) {
            log("Exception in RegenerateXMLTask:"+e);
            e.printStackTrace(System.err);
        }
    }
    
    public static void regenerateXMLs(File rootDir, boolean fullRegenerate, boolean produceBigReportOnly) throws Exception {
        regenerateXMLs(rootDir, fullRegenerate, produceBigReportOnly, false);
    }
    
    public static void regenerateXMLs(File rootDir, boolean fullRegenerate, boolean produceBigReportOnly, boolean serverUsage) throws Exception {
         debugInfo("regenerateXMLs(rootDir="+rootDir+", fullRegenerate="+fullRegenerate+
                ", produceBigReportOnly="+produceBigReportOnly+")");
         int rootDirType = ResultsUtils.resolveResultsDir(rootDir);
            switch (rootDirType) {
                case ResultsUtils.TESTBAG_DIR:
                    regenerateTestBag(rootDir,fullRegenerate,produceBigReportOnly,serverUsage);
                    break;
                case ResultsUtils.TESTRUN_DIR:
                    regenerateTestRun(rootDir,fullRegenerate,produceBigReportOnly,serverUsage);
                    break;
                case ResultsUtils.TESTREPORT_DIR:
                    regenerateTestReport(rootDir,fullRegenerate,produceBigReportOnly,serverUsage);
                    break;
                default:
                    throw new IOException("regenerateXMLs: specified directory is not recognized: "+rootDir);                    
            }
            
    }
    
    // regenerates testbag.xml if out of sync with stored suites in suites subdir
    public static TestBag regenerateTestBag(File testBagRoot, boolean fullRegenerate, boolean produceBigReportOnly) throws Exception {
        return regenerateTestBag(testBagRoot, fullRegenerate, produceBigReportOnly, false);
    }
    
    // regenerates testbag.xml if out of sync with stored suites in suites subdir
    public static TestBag regenerateTestBag(File testBagRoot, boolean fullRegenerate, boolean produceBigReportOnly, boolean serverUsage) throws Exception {
        debugInfo("regenerateTestBag(testBagRoot="+testBagRoot+", fullRegenerate="+fullRegenerate+
            ", produceBigReportOnly="+produceBigReportOnly+")");
        File testBagResultDir = new File(testBagRoot,PEConstants.XMLRESULTS_DIR);
        File testBagFile = new File(testBagResultDir,PEConstants.TESTBAG_XML_FILE);
        File testBagFailuresFile = new File(testBagResultDir,PEConstants.TESTBAG_FAILURES_XML_FILE);
        File testBagPerformanceFile = new File(testBagResultDir,PEConstants.TESTBAG_PERFORMANCE_XML_FILE);
        TestBag testBag = (TestBag)ResultsUtils.getTestBag(testBagFile);
        debugInfo("regenerateTestBag(): got testBag from testbag.xml");
        // now scan the directory and get a list of test suites available in suite directory
        if ((!fullRegenerate)&(!produceBigReportOnly)) {
            // check whether suite files and suites contained in testbag match
            String[] suitesInFiles = ResultsUtils.listSuites(new File(testBagResultDir,PEConstants.TESTSUITES_SUBDIR));
            debugInfo("regenerateTestBag(): checking whether testbag is out of sync with suites dir");
            if (testBag.xmlel_UnitTestSuite!=null) {
                if (suitesInFiles.length!=testBag.xmlel_UnitTestSuite.length) {
                    debugInfo("regenerateTestBag(): both suites differ in length");
                    boolean suitesDiffer = false;
                    for (int i=0;i<suitesInFiles.length;i++) {
                        boolean found = false;
                        for (int j=0;(j<testBag.xmlel_UnitTestSuite.length)&(!found);j++) {
                            if (testBag.xmlel_UnitTestSuite[j].xmlat_name.equals(suitesInFiles[i])) {
                                found = true;
                            }
                        }                        
                        if (!found) {
                            suitesDiffer = true;
                            debugInfo("regenerateTestBag(): have to regenerate, suite "+
                                        suitesInFiles[i]+ " does not exist in XML");
                            break;
                        }
                    }
                    if (!suitesDiffer) {
                        debugInfo("regenerateTestBag(): suites are in sync - don't regenerate");
                        return testBag;
                    }
                } else {
                    debugInfo("regenerateTestBag(): lenghts of suitesInFiles and testBag.xmlel_UnitTestSuite differs - have to regenerate");
                }
            } else {
                debugInfo("regenerateTestBag(): testBag does not contain any suite, have to regenerate");
            }
        }
        
        debugInfo("regenerateTestBag(): regenerating testbag.xml");       
        
        File suitesDir = new File(testBagResultDir,PEConstants.TESTSUITES_SUBDIR);
        UnitTestSuite[] testSuites = ResultsUtils.getUnitTestSuites(suitesDir); 
        UnitTestSuite[] failingTestSuites = new UnitTestSuite[testSuites.length];
        UnitTestSuite[] performanceTestSuites = new UnitTestSuite[testSuites.length];
        // clean old values
        testBag.xmlat_testsPass = 0;
        testBag.xmlat_testsFail = 0;
        testBag.xmlat_testsError = 0;
        testBag.xmlat_testsUnexpectedPass = 0;
        testBag.xmlat_testsExpectedFail = 0;
        testBag.xmlat_testsTotal = 0;
        testBag.xmlat_time = 0;
        // remove testcases from suites - we don't need them in testbag.xml and
        // count new values
        for (int i=0;i<testSuites.length;i++) {            
            // compute new values            
            testBag.xmlat_testsPass+=testSuites[i].xmlat_testsPass;
            testBag.xmlat_testsFail+=testSuites[i].xmlat_testsFail;
            testBag.xmlat_testsUnexpectedPass+=testSuites[i].xmlat_testsUnexpectedPass;
            testBag.xmlat_testsExpectedFail+=testSuites[i].xmlat_testsExpectedFail;
            testBag.xmlat_testsError+=testSuites[i].xmlat_testsError;
            testBag.xmlat_testsTotal+=testSuites[i].xmlat_testsTotal;
            testBag.xmlat_time+=testSuites[i].xmlat_time;
            // if we generate also failures
            if (!produceBigReportOnly) {                
                failingTestSuites[i] = testSuites[i];
                performanceTestSuites[i] = testSuites[i];
            }
        }

        // adding workdir into crashed suites
        if (!produceBigReportOnly) { 
            boolean failure = false;
            File[] suiteFiles = FileUtils.listFiles(suitesDir,null,".xml");
            for (int i=0; i< suiteFiles.length; i++) {
                boolean modified = false;
                UnitTestSuite testSuite = ResultsUtils.getUnitTestSuite(suiteFiles[i]);
                debugInfo("regenerateTestBag(): adding workdir;  suite " + testSuite.getName());
                // only suites with unexpected failure
                if (testSuite.xmlat_unexpectedFailure != null) {
                    failure = true;
                    if (testSuite.xmlel_UnitTestCase != null) {
                       for (int u=0; u<testSuite.xmlel_UnitTestCase.length; u++) {
                          UnitTestCase testCase = testSuite.xmlel_UnitTestCase[u];
                          debugInfo("regenerateTestBag(): adding workdir;  testcase " + testCase.getName());
                          // only testcase with unknown result
                          if (testCase != null && testCase.xmlat_result.equals(UnitTestCase.TEST_UNKNOWN)) {
                               if (testCase.xmlat_workdir == null) {
                                   String workdir = testCase.xmlat_class.replace('.',File.separatorChar)
                                                    + File.separator + testCase.xmlat_name;
                                   File workdirFile = new File(testBagRoot,"user" + File.separator + workdir);
                                   debugInfo("regenerateTestBag(): adding workdir; new workdir="+workdir);
                                   if (workdirFile.exists()) {
                                        debugInfo("regenerateTestBag(): adding workdir; workdir exist");
                                        testCase.xmlat_workdir = workdir;
                                        modified = true;
                                   }
                               }
                          }
                       }
                    }
                }
                if (modified) {
                    SerializeDOM.serializeToFile(testSuite.toDocument(),suiteFiles[i]);
                }
            }
            if (failure && testBag.xmlat_unexpectedFailure == null) 
                testBag.xmlat_unexpectedFailure = "Some suites did not finish correctly:";
                
        }

        // assign id to this testbag
        testBag.xmlat_bagID = testBagRoot.getName();
        
        // check if there is IDE user directory
        File ideUserDir = new File(testBagRoot,PEConstants.IDE_USERDIR_LOCATION);
        if (ideUserDir.isDirectory()) {
            testBag.xmlat_ideUserDir = true;
        } else {
            testBag.xmlat_ideUserDir = false;
        }
        
        
        //
        // now serialize new regenerated testbag (only if not producing big report !!!)
        if (!produceBigReportOnly) {                                
            debugInfo("regenerateTestBag(): serializing new testbag with failures");            
            // delete all passing testcases!!!
            for (int i=0;i<failingTestSuites.length;i++) {
                UnitTestCase[] testCases = testSuites[i].xmlel_UnitTestCase;
                if (failingTestSuites[i].xmlat_testsPass != failingTestSuites[i].xmlat_testsTotal) {
                    for (int j=0;j<testCases.length;j++) {
                        if (testCases[j].xmlat_result.equals("pass")) {
                            testCases[j] = null;
                        } else {
                            testCases[j].xml_pcdata = null;
                            testCases[j].xml_cdata = null;
                        }
                    }
                } else {
                    failingTestSuites[i] = null;
                }
            }                    
            testBag.xmlel_UnitTestSuite = failingTestSuites;
            SerializeDOM.serializeToFile(testBag.toDocument(),testBagFailuresFile);
            
            // delete the rest of testcases
            for (int i=0;i<testSuites.length;i++) {
                testSuites[i].xmlel_UnitTestCase = null;
            }
            
            //
            // now serialize performance results
            if (!serverUsage) {
                // delete all testsuites without performance data
                boolean perf_data_present = false;
                for (int i=0;i<performanceTestSuites.length;i++) {
                    if (performanceTestSuites[i].xmlel_Data == null ||
                       (performanceTestSuites[i].xmlel_Data.length == 1 && performanceTestSuites[i].xmlel_Data[0].xmlel_PerformanceData == null))
                        performanceTestSuites[i] = null;
                    else 
                        perf_data_present = true;
                }                    
                if (perf_data_present) {
                    testBag.xmlel_UnitTestSuite = performanceTestSuites;
                    SerializeDOM.serializeToFile(testBag.toDocument(),testBagPerformanceFile);
                } else {
                    testBagPerformanceFile.delete();   
                }
            }

            debugInfo("regenerateTestBag(): serializing new testbag");
            
            testBag.xmlel_UnitTestSuite = testSuites;
            SerializeDOM.serializeToFile(testBag.toDocument(),testBagFile);
            
        }
        
        testBag.xmlel_UnitTestSuite = testSuites;
        return testBag;
    }     
    
    
    // regenerate testrun
    public static TestRun regenerateTestRun(File testRunRoot, boolean fullRegenerate, boolean produceBigReportOnly) throws Exception {
        return regenerateTestRun(testRunRoot, fullRegenerate, produceBigReportOnly, false);
    }
    
    // regenerate testrun
    public static TestRun regenerateTestRun(File testRunRoot, boolean fullRegenerate, boolean produceBigReportOnly, boolean serverUsage) throws Exception {
        debugInfo("regenerateTestRun(testRunRoot="+testRunRoot+", fullRegenerate="+fullRegenerate+
            ", produceBigReportOnly="+produceBigReportOnly+")");
        File testRunResultsDir = new File(testRunRoot,PEConstants.XMLRESULTS_DIR);
        if (!testRunResultsDir.exists()) {
            if (!testRunResultsDir.mkdirs()) {
                throw new IOException("Cannot create directory:"+testRunResultsDir);
            }
        }
        File testRunFile = new File(testRunResultsDir,PEConstants.TESTRUN_XML_FILE);
        File testRunFailuresFile = new File(testRunResultsDir,PEConstants.TESTRUN_FAILURES_XML_FILE);
        File testRunPerformanceFile = new File(testRunResultsDir,PEConstants.TESTRUN_PERFORMANCE_XML_FILE);
        TestRun testRun = (TestRun)ResultsUtils.getTestRun(testRunFile);
        debugInfo("regenerateTestRun(): regenerating testrun.xml");
        File[] testBagsDirs = ResultsUtils.listTestBags(testRunRoot);
        TestBag[] testBags = new TestBag[testBagsDirs.length];       
        // now try to regenerate testbags first, then regenerate testRun;       
        debugInfo("regenerateTestRun(): regenerating child TestBags");
        for (int i=0;i<testBagsDirs.length;i++) {
            testBags[i] = regenerateTestBag(testBagsDirs[i],fullRegenerate,produceBigReportOnly, serverUsage);            
            debugInfo("regenerateTestRun(): succesfully regenerated testBag "+testBagsDirs[i].getName());
        }
        // now regenerate our stuff
        // because we actually have our testBags already - we don't have to
        // perfrom any check against the testRun - we just
        // replace testBags in TestRun object (sure, we will take care of includeChildren stuff)
     
        testRun.xmlat_testsPass = 0;
        testRun.xmlat_testsFail = 0;
        testRun.xmlat_testsUnexpectedPass = 0;
        testRun.xmlat_testsExpectedFail = 0;
        testRun.xmlat_testsError = 0;
        testRun.xmlat_testsTotal = 0;
        testRun.xmlat_time = 0;
        // count new values
        for (int i=0;i<testBags.length;i++) {
            // compute new values
            testRun.xmlat_testsPass+=testBags[i].xmlat_testsPass;
            testRun.xmlat_testsFail+=testBags[i].xmlat_testsFail;
            testRun.xmlat_testsUnexpectedPass+=testBags[i].xmlat_testsUnexpectedPass;
            testRun.xmlat_testsExpectedFail+=testBags[i].xmlat_testsExpectedFail;
            testRun.xmlat_testsError+=testBags[i].xmlat_testsError;
            testRun.xmlat_testsTotal+=testBags[i].xmlat_testsTotal;
            testRun.xmlat_time+=testBags[i].xmlat_time;
        }
        testRun.xmlel_TestBag = testBags;
        // give the run ID
        testRun.xmlat_runID = testRunRoot.getName();
        
        // check if there is log directory (available when running from 
        //  instance )
        File logDir = new File(testRunRoot,PEConstants.ANT_LOGDIR_LOCATION);
        if (logDir.isDirectory()) {
            testRun.xmlat_antLogs = true;
        } else {
            testRun.xmlat_antLogs = false;
        }
        
        
        // now serialize the new test run
        if (!produceBigReportOnly) {
            TestBag[] testBagsWithFailures = new TestBag[testBagsDirs.length];
            debugInfo("regenerateTestRun(): loading testbags with failures");
            for (int i=0;i<testRun.xmlel_TestBag.length;i++) {
                TestBag aTestBag = testRun.xmlel_TestBag[i];
                if (aTestBag.xmlat_testsTotal != aTestBag.xmlat_testsPass) {
                   testBagsWithFailures[i] = ResultsUtils.getTestBag(new File(testBagsDirs[i],PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_FAILURES_XML_FILE));
                }                
            }
            debugInfo("regenerateTestRun(): serializing new testrun with failures");
            testRun.xmlel_TestBag = testBagsWithFailures;
            SerializeDOM.serializeToFile(testRun.toDocument(),testRunFailuresFile);             
            
            if (!serverUsage) {
                // serialize performance results
                TestBag[] testBagsWithPerformance = new TestBag[testBagsDirs.length];
                debugInfo("regenerateTestRun(): loading testbags with performance");
                boolean exist_perf = false;
                for (int i=0;i<testRun.xmlel_TestBag.length;i++) {
                    File testbagfile = new File(testBagsDirs[i],PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_PERFORMANCE_XML_FILE);
                    if (testbagfile.exists()) {
                        testBagsWithPerformance[i] = ResultsUtils.getTestBag(testbagfile);
                        exist_perf = true;
                    }
                }
                debugInfo("regenerateTestRun(): serializing new testrun with performance");
                testRun.xmlel_TestBag = testBagsWithPerformance;
                SerializeDOM.serializeToFile(testRun.toDocument(),testRunPerformanceFile);
            }
                        
            // here delete all suites - no longer needed
            for (int i=0; i<testBags.length;i++) {
                testBags[i].xmlel_UnitTestSuite = null;
            }
            debugInfo("regenerateTestRun(): serializing new testrun");
            testRun.xmlel_TestBag = testBags;
            SerializeDOM.serializeToFile(testRun.toDocument(),testRunFile);        
        }               
        return testRun;
    }
    
    
    // regenerate testreport
    public static XTestResultsReport regenerateTestReport(File testReportRoot, boolean fullRegenerate, boolean produceBigReportOnly) throws Exception {
        return regenerateTestReport(testReportRoot, fullRegenerate, produceBigReportOnly, false);
    }
    
    // regenerate testreport
    public static XTestResultsReport regenerateTestReport(File testReportRoot, boolean fullRegenerate, boolean produceBigReportOnly, boolean serverUsage) throws Exception {
        debugInfo("regenerateTestReport(testReportRoot="+testReportRoot+", fullRegenerate="+fullRegenerate+
            ", produceBigReportOnly="+produceBigReportOnly+")");
        File testReportResultsDir = new File(testReportRoot,PEConstants.XMLRESULTS_DIR);
        if (!testReportResultsDir.exists()) {
            if (!testReportResultsDir.mkdirs()) {
                throw new IOException("Cannot create directory:"+testReportResultsDir);
            }
        }
        File testReportFile = new File(testReportResultsDir,PEConstants.TESTREPORT_XML_FILE);
        XTestResultsReport testReport = (XTestResultsReport)ResultsUtils.getXTestResultsReport(testReportFile);
        if (testReport.xmlat_host==null) {
            testReport.xmlat_host = SystemInfo.getCurrentHost();        
        }        
        debugInfo("regenerateTestReport(): regenerating testreport.xml");
        File[] testRunsDirs = ResultsUtils.listTestRuns(testReportRoot);
        TestRun[] testRuns = new TestRun[testRunsDirs.length];
        // now try to regenerate testbags first, then regenerate testRun;       
        debugInfo("regenerateTestReport(): regenerating child TestRuns");
        for (int i=0;i<testRunsDirs.length;i++) {
            testRuns[i] = regenerateTestRun(testRunsDirs[i],fullRegenerate,produceBigReportOnly, serverUsage);
            debugInfo("regenerateTestReport(): succesfully regenerated testRun "+testRunsDirs[i].getName());
        }
        // now regenerate our stuff
        // because we actually have our testRuns ready - we don't have to
        // perfrom any check against the testReport - we just
        // replace testRuns in TestReport object (sure, we will take care of includeChildren stuff)
     
        TreeSet brokenModules = new TreeSet();
        
        testReport.xmlat_testsPass = 0;
        testReport.xmlat_testsFail = 0;
        testReport.xmlat_testsUnexpectedPass = 0;
        testReport.xmlat_testsExpectedFail = 0;
        testReport.xmlat_testsError = 0;
        testReport.xmlat_testsTotal = 0;
        testReport.xmlat_time = 0;
        testReport.xmlat_fullReport = produceBigReportOnly;
        // count new values
        for (int i=0;i<testRuns.length;i++) {            
            // compute new values
            testReport.xmlat_testsPass+=testRuns[i].xmlat_testsPass;
            testReport.xmlat_testsFail+=testRuns[i].xmlat_testsFail;
            testReport.xmlat_testsUnexpectedPass+=testRuns[i].xmlat_testsUnexpectedPass;
            testReport.xmlat_testsExpectedFail+=testRuns[i].xmlat_testsExpectedFail;
            testReport.xmlat_testsError+=testRuns[i].xmlat_testsError;
            testReport.xmlat_testsTotal+=testRuns[i].xmlat_testsTotal;
            testReport.xmlat_time+=testRuns[i].xmlat_time;
            
            if (testRuns[i].xmlel_ModuleError != null && testRuns[i].xmlel_ModuleError.length > 0) {
                    for (int j=0; j<testRuns[i].xmlel_ModuleError.length; j++) {
                        brokenModules.add(testRuns[i].xmlel_ModuleError[j].getModule());
                    }
            }
            
        }
        if (!brokenModules.isEmpty()) {
            String modules = "";
            Iterator iter = brokenModules.iterator();
            while (iter.hasNext()) {
                modules += (modules.equals("")?"":",") + (String)iter.next();
            }
            testReport.xmlat_brokenModules = modules;
        }
        testReport.xmlel_TestRun = testRuns;
        // try to set the correct date
        if (testReport.xmlat_timeStamp==null) {
            testReport.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        }
        // if not any system info was generated we have to do it ourselves
        if (testReport.xmlel_SystemInfo==null) {
            testReport.xmlel_SystemInfo = new SystemInfo[] {new SystemInfo()};
        }
        // do we generate also failures file (so we don't generate full report)
        if (!produceBigReportOnly) {
            File testReportFailuresFile = new File(testReportResultsDir,PEConstants.TESTREPORT_FAILURES_XML_FILE);
            TestRun[] testRunsWithFailures = new TestRun[testRunsDirs.length];
            debugInfo("regenerateTestReport(): loading testruns with failures");
            for (int i=0;i<testReport.xmlel_TestRun.length;i++) {
                TestRun aTestRun = testReport.xmlel_TestRun[i];
                if (aTestRun.xmlat_testsTotal != aTestRun.xmlat_testsPass) {
                   testRunsWithFailures[i] = ResultsUtils.getTestRun(new File(testRunsDirs[i],PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTRUN_FAILURES_XML_FILE));
                }                
            }
            debugInfo("regenerateTestReport(): serializing new testrun with failures");
            testReport.xmlel_TestRun = testRunsWithFailures;
            SystemInfo[] si = testReport.xmlel_SystemInfo;
            testReport.xmlel_SystemInfo = null;
            SerializeDOM.serializeToFile(testReport.toDocument(),testReportFailuresFile);          
            
            if (!serverUsage) {
                // serialize performance data
                File testReportPerformanceFile = new File(testReportResultsDir,PEConstants.TESTREPORT_PERFORMANCE_XML_FILE);
                TestRun[] testRunsWithPerformance = new TestRun[testRunsDirs.length];
                debugInfo("regenerateTestReport(): loading testruns with performance");
                for (int i=0;i<testReport.xmlel_TestRun.length;i++) {
                    testRunsWithPerformance[i] = ResultsUtils.getTestRun(new File(testRunsDirs[i],PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTRUN_PERFORMANCE_XML_FILE));
                }
                debugInfo("regenerateTestReport(): serializing new testrun with performance");
                testReport.xmlel_TestRun = testRunsWithPerformance;
                SerializeDOM.serializeToFile(testReport.toDocument(),testReportPerformanceFile);
            }
            
            testReport.xmlel_SystemInfo = si;
        }
        
        // now serialize the new test run
        debugInfo("regenerateTestReport(): serializing new testreport");
        testReport.xmlel_TestRun = testRuns;
        SerializeDOM.serializeToFile(testReport.toDocument(),testReportFile);        
        return testReport;
    }
    
}


