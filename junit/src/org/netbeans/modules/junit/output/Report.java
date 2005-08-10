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

    File antScript;
    String suiteClassName;
    String[] outputStd;
    String[] outputErr;
    int totalTests;
    int failures;
    int errors;
    int elapsedTimeMillis;
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
        final String className = testcase.className;
        final String mapKey = (className != null)
                              ? className
                              : Report.TestcaseGroup.NO_NAME;
        //assert testcase.className != null;
        
        TestcaseGroup group = null;
        try {
            group = (TestcaseGroup) testcaseGroupMap.get(mapKey);
        } catch (Exception ex) {
            return;
        }
        if (group == null) {
            testcaseGroupMap.put(mapKey, group = new TestcaseGroup(className));
        }
        group.addTestcase(testcase);
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
        mergeTestcases(testcaseGroupMap, report.testcaseGroupMap);
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
        /* append testcases: */
        boolean thisEmpty = thisMap.isEmpty();
        boolean thatEmpty = thatMap.isEmpty();
        if (!thatEmpty) {
            if (thisEmpty) {
                thisMap.putAll(thatMap);
            } else {
                mergeTestcaseGroups(thisMap, thatMap);
            }
        }
    }
    
    /**
     */
    private static void mergeTestcaseGroups(
                                final Map/*<String, TestcaseGroup>*/ first,
                                final Map/*<String, TestcaseGroup>*/ sec) {
        for (Iterator i = first.values().iterator(); i.hasNext(); ) {
            final TestcaseGroup a = (TestcaseGroup) i.next();
            Object o = sec.remove(a.className);
            if (o != null) {
                a.testcases.addAll(((TestcaseGroup) o).testcases);
            }
        }
    }
    
}
