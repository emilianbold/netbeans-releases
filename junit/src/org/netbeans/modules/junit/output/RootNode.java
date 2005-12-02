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

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Marian Petras
 */
final class RootNode extends AbstractNode implements ChangeListener {
    
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
    /** */
    private final Object lock = new Object();
    
    /**
     */
    private volatile boolean filtered = false;
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
    public RootNode() {
        super(new RootNodeChildren());
        children = (RootNodeChildren) getChildren();
        setName(name);   //used by tree cell renderer to recognize the root node
        setIconBaseWithExtension(
                "org/netbeans/modules/junit/output/res/empty.gif");     //NOI18N
    }
    
    /**
     */
    Object getLock() {
        return lock;
    }
    
    /**
     */
    void displayMessage(final String msg) {
        synchronized (lock) {
            this.message = msg;
            updateDisplayName();
        }
    }
    
    /**
     */
    void displayReport(final Report report) {
        
        /* May be called from various threads. */
        
        synchronized (lock) {
            if (report.isClosed()) {
                updateStatistics(report);
                updateDisplayName();
            } else {
                report.addChangeListener(WeakListeners.change(this, report));
            }
            children.displayReport(report);
        }
    }
    
    /**
     */
    void displayReports(final Collection/*<Report>*/ reports) {
        
        /* May be called from various threads. */
        
        synchronized (lock) {
            for (Iterator i = reports.iterator(); i.hasNext(); ) {
                final Report report = (Report) i.next();
                if (report.isClosed()) {
                   updateStatistics((Report) i.next());
                } else {
                   report.addChangeListener(WeakListeners.change(this, report));
                }
            }
            updateDisplayName();
            children.displayReports(reports);
        }
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
    public void stateChanged(final ChangeEvent e) {
        
        /* May be called from various threads. */
        
        final Report report = (Report) e.getSource();
        assert report.isClosed();
        
        synchronized (lock) {
            updateStatistics(report);
            updateDisplayName();
        }
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
        final Class bundleRefClass = getClass();
        String msg;

        if (totalTests == 0) {
            msg = null;
        } else if ((failures == 0) && (errors == 0)) {
            msg = NbBundle.getMessage(bundleRefClass,
                                      "MSG_TestsInfoAllOK",
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
        synchronized (lock) {
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
    
}
