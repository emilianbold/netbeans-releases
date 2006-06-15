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
import java.util.Collection;
import java.util.Iterator;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class RootNode extends AbstractNode {
    
    /** */
    static final String name = "JUnit results root node";               //NOI18N
    
    /** constant meaning "information about passed tests not displayed" */
    static final int ALL_PASSED_ABSENT = 0;
    /** constant meaning "information about some passed tests not displayed" */
    static final int SOME_PASSED_ABSENT = 1;
    /** constant meaning "information about all passed tests displayed */
    static final int ALL_PASSED_DISPLAYED = 2;

    /**
     */
    private final RootNodeChildren children;
    
    /**
     */
    private volatile boolean filtered;
    /** */
    private volatile String message;
    
    private volatile int totalTests = 0;
    private volatile int failures = 0;
    private volatile int errors = 0;
    private volatile int elapsedTimeMillis = 0;
    private volatile int detectedPassedTests = 0;
    
    
    /**
     * Creates a new instance of RootNode
     */
    public RootNode(final boolean filtered) {
        super(new RootNodeChildren(filtered));
        this.filtered = filtered;
        children = (RootNodeChildren) getChildren();
        setName(name);   //used by tree cell renderer to recognize the root node
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/empty.gif");     //NOI18N
    }
    
    /**
     */
    void displayMessage(final String msg) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        this.message = msg;
        updateDisplayName();
    }
    
    /**
     * Displays a message that a given test suite is running.
     *
     * @param  suiteName  name of the running test suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     *
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        children.displaySuiteRunning(suiteName);
    }
    
    /**
     */
    TestsuiteNode displayReport(final Report report) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        updateStatistics(report);
        updateDisplayName();
        return children.displayReport(report);
    }
    
    /**
     */
    void displayReports(final Collection/*<Report>*/ reports) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        for (Iterator i = reports.iterator(); i.hasNext(); ) {
            final Report report = (Report) i.next();
            updateStatistics(report);
        }
        updateDisplayName();
        children.displayReports(reports);
    }
    
    /**
     */
    private void updateStatistics(final Report report) {
        totalTests += report.totalTests;
        failures += report.failures;
        errors += report.errors;
        detectedPassedTests += report.detectedPassedTests;
        elapsedTimeMillis += report.elapsedTimeMillis;
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        assert EventQueue.isDispatchThread();
        
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        Children children = getChildren();
        if (children != Children.LEAF) {
            ((RootNodeChildren) children).setFiltered(filtered);
        }
    }
    
    /**
     */
    private void updateDisplayName() {
        assert EventQueue.isDispatchThread();
        
        final Class bundleRefClass = getClass();
        String msg;

        if (totalTests == 0) {
            msg = null;
        } else if ((failures == 0) && (errors == 0)) {
            msg = NbBundle.getMessage(bundleRefClass,
                                      "MSG_TestsInfoAllOK",             //NOI18N
                                      new Integer(totalTests));
        } else {
            String passedTestsInfo = NbBundle.getMessage(
                    bundleRefClass,
                    "MSG_PassedTestsInfo",                              //NOI18N
                    new Integer(totalTests - failures - errors));
            String failedTestsInfo = (failures == 0)
                                     ? null
                                     : NbBundle.getMessage(
                                            bundleRefClass,
                                            "MSG_FailedTestsInfo",      //NOI18N
                                            new Integer(failures));
            String errorTestsInfo = (errors == 0)
                                    ? null
                                    : NbBundle.getMessage(
                                            bundleRefClass,
                                            "MSG_ErrorTestsInfo",       //NOI18N
                                            new Integer(errors));
            if ((failedTestsInfo == null) || (errorTestsInfo == null)) {
                msg = NbBundle.getMessage(bundleRefClass,
                                          "MSG_TestsOneIssueType",      //NOI18N
                                          passedTestsInfo,
                                          failedTestsInfo != null
                                                ? failedTestsInfo
                                                : errorTestsInfo);
            } else {
                msg = NbBundle.getMessage(bundleRefClass,
                                          "MSG_TestsFailErrIssues",     //NOI18N
                                          passedTestsInfo,
                                          failedTestsInfo,
                                          errorTestsInfo);
            }
        }

        if (totalTests != 0) {
            assert msg != null;
            final int successDisplayedLevel = getSuccessDisplayedLevel();
            switch (successDisplayedLevel) {
                case SOME_PASSED_ABSENT:
                    msg += ' ';
                    msg += NbBundle.getMessage(
                                        bundleRefClass,
                                        "MSG_SomePassedNotDisplayed");  //NOI18N
                    break;
                case ALL_PASSED_ABSENT:
                    msg += ' ';
                    msg += NbBundle.getMessage(
                                        bundleRefClass,
                                        "MSG_PassedNotDisplayed");      //NOI18N
                    break;
                case ALL_PASSED_DISPLAYED:
                    break;
                default:
                    assert false;
                    break;
            }
        }
        
        if (this.message != null) {
            if (msg == null) {
                msg = this.message;
            } else {
                msg = msg + ' ' + message;
            }
        }

        setDisplayName(msg);
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
    
}
