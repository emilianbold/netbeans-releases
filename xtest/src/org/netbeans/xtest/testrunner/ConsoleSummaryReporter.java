/*
 *
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
