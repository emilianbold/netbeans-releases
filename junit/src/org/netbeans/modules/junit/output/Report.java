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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
final class Report {

    String suiteClassName;
    String[] outputStd;
    String[] outputErr;
    int totalTests;
    int failures;
    int errors;
    int elapsedTimeMillis;
    List/*<Testcase>*/ testcases;
    
    /**
     */
    Report(String suiteClassName) {
        this.suiteClassName = suiteClassName;
    }
    
    /**
     */
    void reportTestcase(Testcase testcase) {
        if (testcases == null) {
            testcases = new ArrayList(64);
        }
        testcases.add(testcase);
    }
    
    /**
     */
    void appendReport(Report report) {
        suiteClassName = NbBundle.getMessage(getClass(),
                                             "COMPOUND_SUITE");         //NOI18N
        totalTests = appendCount(totalTests, report.totalTests);
        failures = appendCount(failures, report.failures);
        errors = appendCount(errors, report.errors);
        elapsedTimeMillis = appendCount(elapsedTimeMillis,
                                        report.elapsedTimeMillis);
        outputStd = appendOutput(outputStd, report.outputStd);
        outputErr = appendOutput(outputErr, report.outputErr);
        testcases = appendTestcaseList(testcases, report.testcases);
    }
    
    /**
     */
    private static String getTestID(String testClsName, String testMethodName) {
        return testClsName + '.' + testMethodName;
    }
    
    
    /**
     */
    static final class Testcase {
        String className;
        String name;
        int timeMillis;
        Trouble trouble;
    }
    
    /**
     */
    static final class Trouble {
        
        private final boolean error;
        String message;
        String exceptionClsName;
        String[] stackTrace;
        
        /**
         */
        Trouble(boolean error) {
            this.error = error;
        }
        
        /** */
        boolean isError() {
            return error;
        }
        
    }
    
    
    /**
     */
    private static int appendCount(int top, int curr) {
        if ((top > 0) && (curr < 0)) {
            top = -top - 1;
        }
        top += (curr >= 0) ? curr
                           : curr + 1;
        return top;
    }
    
    /**
     */
    private static String[] appendOutput(final String[] top,
                                         final String[] curr) {
        if ((top == null) || (top.length == 0)) {
            return curr;
        }
        if ((curr == null) || (curr.length == 0)) {
            return top;
        }
        final String[] result = new String[top.length + curr.length];
        System.arraycopy(top, 0, result, 0, top.length);
        System.arraycopy(curr, 0, result, top.length, curr.length);
        return result;
    }
    
    /**
     */
    private List/*<Testcase>*/ appendTestcaseList(List/*<Testcase>*/ top,
                                                  List/*<Testcase>*/ curr) {
        if ((top == null) || top.isEmpty()) {
            return curr;
        }
        if ((curr == null) || curr.isEmpty()) {
            return top;
        }
        top.addAll(curr);
        return top;
    }
    
}
