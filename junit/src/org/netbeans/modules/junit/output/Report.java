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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
     * listeners which listens for this report to be closed
     *
     * @see  #setChangeListener
     */
    private List/*<ChangeListener>*/ changeListeners;
    
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
        //PENDING - should be synchronized
        tests.add(test);
        
        if (test.trouble == null) {
            detectedPassedTests++;
        }
    }
    
    /**
     */
    void update(Report report) {
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
    void close() {
        if (closed) {
            ErrorManager.getDefault().log(
                    ErrorManager.WARNING,
                    "Closing an already closed report: " + suiteClassName);
            return;
        }
        
        closed = true;
        
        fireChange();
    }
    
    /**
     */
    boolean isClosed() {
        return closed;
    }
    
    /**
     */
    void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList/*<ChangeListener>*/(2);
        }
        changeListeners.add(l);
    }
    
    /**
     */
    void removeChangeListener(ChangeListener l) {
        if ((changeListeners != null)
                && changeListeners.remove(l)
                && changeListeners.isEmpty()) {
            changeListeners = null;
        }
    }
    
    /**
     */
    private void fireChange() {
        if (changeListeners != null) {
            final ChangeEvent event = new ChangeEvent(this);
            for (Iterator/*<ChangeListener>*/ i = changeListeners.iterator();
                    i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(event);
            }
        }
        changeListeners = null;
    }
    
    /**
     */
    Collection getTests() {
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
