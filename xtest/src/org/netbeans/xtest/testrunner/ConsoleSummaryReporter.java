/*
 *
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

/*
 * ConsoleSummaryReporter.java
 *
 * Created on November 1, 2001, 3:34 PM
 */

package org.netbeans.xtest.testrunner;


import java.io.*;

import junit.framework.*;

import java.text.*;
import org.netbeans.junit.NbTest;

/**
 *
 * @author  mb115822
 * @version 
 */
 
public class ConsoleSummaryReporter implements JUnitTestListener {

    // output stream
    private PrintWriter pw;
    // text formatters
    DecimalFormat percentFormatter = new DecimalFormat("##0.00%");
    DecimalFormat timeFormatter = new DecimalFormat("#######0.000");
    
    // suiteStartTime
    private long suiteStartTime;
    
    private int expectedFailCount = 0;
    
    private int xtestErrorManagerCorrection = 0;
    private boolean failureAlreadySet;
    
    public ConsoleSummaryReporter() {
        this.pw = new PrintWriter(System.out);
    }
    
    public ConsoleSummaryReporter(PrintWriter pw) {
        this.pw = pw;
    }
    
    public ConsoleSummaryReporter(PrintStream ps) {
        this.pw = new PrintWriter(ps);
    }

    

    public void startTestSuite(TestSuite suite) {
        suiteStartTime = System.currentTimeMillis();
        expectedFailCount = 0;
        pw.println("- test suite "+suite.getName()+" started");
        pw.flush();
    }

    public void endTestSuite(TestSuite suite, TestResult suiteResult) {
        long suiteDelta = System.currentTimeMillis() - suiteStartTime;
        int failCount = suiteResult.failureCount()-xtestErrorManagerCorrection;
        int passCount = suiteResult.runCount()-failCount-suiteResult.errorCount();
        if (passCount < 0) {
            passCount = 0;
        }
        if (passCount != suiteResult.runCount()) {
            pw.println("- test suite "+suite.getName()+" FAILED");
            pw.println("- time elapsed: "+timeFormatter.format(suiteDelta/1000.0)+" seconds");            
            pw.print("- passed: "+passCount+"  failed: "+failCount);
            if (expectedFailCount > 0)
                pw.print(" (incl. "+expectedFailCount+" expected)");
            pw.println("  errors: "+suiteResult.errorCount()+"  total: "+suiteResult.runCount());
            double successRate = 0;
            if (suiteResult.runCount() != 0) {
                successRate = ((double)passCount)/((double)suiteResult.runCount());
            }
            pw.println("- success rate: "+percentFormatter.format(successRate));
        } else {
            pw.println("- test suite "+suite.getName()+" passed: "+passCount+" test(s)");
            pw.println("- time elapsed: "+timeFormatter.format(suiteDelta/1000.0)+" seconds");
        }
        pw.print('\n');
        pw.flush();
    }
    
    

	// empty
    public void startTest(Test t) {
        failureAlreadySet = false;
    }

	// empty
    public void endTest(Test test) {}
    
	// empty
    public void addFailure(Test test, AssertionFailedError t) {
        failureAlreadySet = true;
        if (test instanceof NbTest) {
           if (((NbTest)test).getExpectedFail() != null)
               expectedFailCount++;
        }
    }

	// empty
    public void addError(Test test, Throwable t) {
        if(failureAlreadySet) {
            // error added by XTestErrorManager and we have to subtract one fail
            xtestErrorManagerCorrection++;
        }
    }

	// empty    
    public void setOutputFile(java.io.File outFile) {}

    
}
