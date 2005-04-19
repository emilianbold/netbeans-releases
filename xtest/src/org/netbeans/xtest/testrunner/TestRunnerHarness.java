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

package org.netbeans.xtest.testrunner;

import java.io.*;

/**
 * Class which controls the execution from outer space - it runs
 * in the same VM as harness and is reponsible for launching tested products
 *
 * @author  mb115822
 */
public class TestRunnerHarness {
    
    // whole testbag is run in a single VM instance
    public static final String TESTRUN_MODE_TESTBAG="testbag";
    // each test class (testsuite) is run in a single VM instance
    public static final String TESTRUN_MODE_TESTSUITE="testsuite";
    // all tests are run internally (in the same VM as the class which starts the test run)
    public static final String TESTRUN_MODE_CURRENT_VM="currentVM";
    
    public static final String TESTLIST_FILENAME = "testrunnerharness.testlist";
    
    private static final boolean DEBUG = false;
    
    private JUnitTestRunnerProperties testList;
    private TestBoardLauncher launcher;
    private File workDir;
    private String testMode;
    
    /** Creates a new instance of TestRunnerHarness */
    public TestRunnerHarness(TestBoardLauncher boardLauncher, File workDir, String testMode) throws IOException {
        this.launcher = boardLauncher;
        this.workDir = workDir;
        this.testList = loadJUnitTestRunnerProperties(workDir);
        this.testMode = testMode;
    }
    
    
    // loads test runner properties from file available at ${xtest.work}/
    public static JUnitTestRunnerProperties loadJUnitTestRunnerProperties(File workDir) throws IOException {
        File testRunnerPropertyFile = new File(workDir, TESTLIST_FILENAME);
        return JUnitTestRunnerProperties.load(testRunnerPropertyFile);
    }
       
    
    public void runTests() {
        if (DEBUG) System.out.println("TestRunnerHarness : "+this);
        if (TESTRUN_MODE_TESTBAG.equalsIgnoreCase(testMode)) {
            // testbag mode
            launchTests(testList);
        } else if (TESTRUN_MODE_TESTSUITE.equalsIgnoreCase(testMode)) {
            // testsuite mode
            JUnitTestRunnerProperties[] dividedTestLists = testList.divideByTests();
            if (DEBUG) System.out.println("Divided to "+dividedTestLists.length+" lists");
            for (int i = 0; i < dividedTestLists.length; i++) {                
                if (DEBUG) System.out.println("List "+i+":"+dividedTestLists[i]);
                if (DEBUG) dividedTestLists[i].list(System.out);
                launchTests(dividedTestLists[i]);
            }
        } else if (TESTRUN_MODE_CURRENT_VM.equalsIgnoreCase(testMode)) {
            // same vm mode - not supported right now
            throw new RuntimeException("Current VM mode not yet supported by TestRunner");
        } else {            
            // unupported mode            
            throw new RuntimeException("Unkonwn test run mode :"+testList.getTestRunType());
        }
    }
    
    public void launchTests(JUnitTestRunnerProperties testList) {
        try {
            launcher.launchTestBoard(testList);
        } catch (TestBoardLauncherException tble) {
            System.err.println("Cannot start tests. Reson: "+tble.getMessage());
        }
    }
    
}
