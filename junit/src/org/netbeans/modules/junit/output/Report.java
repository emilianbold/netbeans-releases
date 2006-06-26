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

package org.netbeans.modules.junit.output;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;

/**
 * Data structure (model) of results of JUnit task results.
 * The data are built by the {@link JUnitOutputReader}.
 *
 * @see  JUnitOutputReader
 * @author  Marian Petras
 */
final class Report {
    
    File antScript;
    File resultsDir;
    String suiteClassName;
    String classpath;
    ClassPath platformSources;
    Collection/*<FileObject>*/ classpathSourceRoots;
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
    private Collection/*<Testcase>*/ tests;
    private boolean closed = false;
    
    /**
     */
    Report(String suiteClassName) {
        this.suiteClassName = suiteClassName;
        this.antScript = antScript;
        this.tests = new ArrayList(10);
    }
    
    /**
     */
    void reportTest(Testcase test) {
        
        /* Called from the AntLogger thread */
        
        //PENDING - should be synchronized
        tests.add(test);
        
        if (test.trouble == null) {
            detectedPassedTests++;
        }
    }
    
    /**
     */
    void update(Report report) {
        
        /* Called from the AntLogger thread */
        
        //PENDING - should be synchronized
        
        //this.antScript = report.antScript;    - KEEP DISABLED!!!
        this.resultsDir = report.resultsDir;
        this.suiteClassName = report.suiteClassName;
        this.outputStd = report.outputStd;
        this.outputErr = report.outputErr;
        this.totalTests = report.totalTests;
        this.failures = report.failures;
        this.errors = report.errors;
        this.elapsedTimeMillis = report.elapsedTimeMillis;
        this.detectedPassedTests = report.detectedPassedTests;
        this.tests = report.tests;
    }
    
    /**
     */
    Collection getTests() {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        //PENDING - should be synchronized
        if (tests.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            return new ArrayList(tests);
        }
    }
    
    /**
     */
    boolean containsFailed() {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        return (failures + errors) != 0;
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
    
}
