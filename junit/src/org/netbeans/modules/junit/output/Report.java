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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.openide.filesystems.FileObject;

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
    Collection<FileObject> classpathSourceRoots;
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
    private Collection<Testcase> tests;
    private boolean closed = false;
    
    /**
     */
    Report(String suiteClassName) {
        this.suiteClassName = suiteClassName;
        this.antScript = antScript;
        this.tests = new ArrayList<Testcase>(10);
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
    Collection<Testcase> getTests() {
        
        /*
         * May be called both from the EventDispatch thread and
         * from other threads!
         *
         * TestSuiteNodeChildren.setFiltered() ... EventDispatch thread
         * TestSuiteNodeChildren.addNotify() ... EventDispatch thread or else
         */
        
        //PENDING - should be synchronized
        if (tests.isEmpty()) {
            final Collection<Testcase> emptyList = Collections.emptyList();
            return emptyList;
        } else {
            return new ArrayList<Testcase>(tests);
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
        Trouble nestedTrouble;
        
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
