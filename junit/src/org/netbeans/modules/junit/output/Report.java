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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.NbBundle;

/**
 * Data structure (model) of results of JUnit task results.
 * The data are built by the {@link JUnitOutputReader}.
 *
 * @see  JUnitOutputReader
 * @author  Marian Petras
 */
final class Report {
    
    /** constant meaning "information about passed tests not displayed" */
    static final int ALL_PASSED_ABSENT = 0;
    /** constant meaning "information about some passed tests not displayed" */
    static final int SOME_PASSED_ABSENT = 1;
    /** constant meaning "information about all passed tests displayed */
    static final int ALL_PASSED_DISPLAYED = 2;

    File antScript;
    String suiteClassName;
    String[] outputStd;
    String[] outputErr;
    int totalTests;
    int failures;
    int errors;
    int elapsedTimeMillis;
    /**
     * number of recognized (by the parser) passed test reports
     */
    int detectedPassedTests;
    private final Map/*<String, TestcaseGroup>*/ testcaseGroupMap;
    final Collection testcaseGroups;
    
    /**
     */
    Report(String suiteClassName) {
        this.suiteClassName = suiteClassName;
        this.antScript = antScript;
        testcaseGroupMap = new TreeMap/*<String, TestcaseGroup>*/();
        testcaseGroups = testcaseGroupMap.values();
    }
    
    /**
     */
    void reportTestcase(Testcase testcase) {
        final String className = (testcase.className != null)
                                 ? testcase.className
                                 : Report.TestcaseGroup.NO_NAME;
        
        TestcaseGroup group = null;
        try {
            group = (TestcaseGroup) testcaseGroupMap.get(className);
        } catch (Exception ex) {
            return;
        }
        if (group == null) {
            testcaseGroupMap.put(className,
                                 group = new TestcaseGroup(className));
        }
        group.addTestcase(testcase);
        
        if (testcase.trouble == null) {
            detectedPassedTests++;
        }
    }
    
    /**
     */
    void appendReport(Report report) {
        suiteClassName = NbBundle.getMessage(getClass(),
                                             "COMPOUND_SUITE");         //NOI18N
        totalTests = appendCount(totalTests, report.totalTests);
        failures = appendCount(failures, report.failures);
        errors = appendCount(errors, report.errors);
        detectedPassedTests += report.detectedPassedTests;
        elapsedTimeMillis = appendCount(elapsedTimeMillis,
                                        report.elapsedTimeMillis);
        outputStd = appendOutput(outputStd, report.outputStd);
        outputErr = appendOutput(outputErr, report.outputErr);
        mergeTestcases(testcaseGroupMap, report.testcaseGroupMap);
    }
    
    /**
     * Returns information whether information about passed tests is displayed.
     *
     * @return  one of constants <code>ALL_PASSED_DISPLAYED</code>,
     *                           <code>SOME_PASSED_ABSENT</code>,
     *                           <code>ALL_PASSED_ABSENT</code>
     */
    int getSuccessDisplayedLevel() {
        int reportedPassedTestsCount = totalTests - failures - errors;
        if (detectedPassedTests >= reportedPassedTestsCount) {
            return ALL_PASSED_DISPLAYED;
        } else if (detectedPassedTests == 0) {
            return ALL_PASSED_ABSENT;
        } else {
            return SOME_PASSED_ABSENT;
        }
    }
    
    /**
     *
     */
    static final class TestcaseGroup {
        static final String NO_NAME = new String();
        final String className;
        private final List/*<Testcase>*/ testcases;
        private boolean containsFailed = false;
        private boolean containsPassed = false;
        
        /**
         */
        TestcaseGroup(final String className) {
            this.className = className;
            testcases = new ArrayList/*<Testcase>*/(8);
        }
        
        private void addTestcase(final Testcase testcase) {
            testcases.add(testcase);
            final boolean isFailed = (testcase.trouble != null);
            containsFailed |= isFailed;
            containsPassed |= !isFailed;
        }
        
        Collection getTestcases() {
            return Collections.unmodifiableList(testcases);
        }
        
        boolean containsFailed() {
            return containsFailed;
        }
        
        boolean containsPassed() {
            return containsPassed;
        }
        
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
    private static void mergeTestcases(Map/*<String, TestcaseGroup>*/ thisMap,
                                       Map/*<String, TestcaseGroup>*/ thatMap) {
        if (thatMap.isEmpty()) {
            return;
        }
        
        if (!thisMap.isEmpty()) {
            for (Iterator i = thisMap.values().iterator(); i.hasNext(); ) {
                final TestcaseGroup g = (TestcaseGroup) i.next();
                Object o = thatMap.remove(g.className);
                if (o != null) {
                    g.testcases.addAll(((TestcaseGroup) o).testcases);
                }
            }
        }
        thisMap.putAll(thatMap);
    }
    
}
