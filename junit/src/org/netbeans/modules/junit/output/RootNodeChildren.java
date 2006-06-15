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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class RootNodeChildren extends Children.Array {
    
    /** */
    private volatile boolean filtered;
    /** */
    private Collection/*<Report>*/ reports;
    /** */
    private volatile int passedSuites;
    /** */
    private volatile int failedSuites;
    /** */
    private volatile boolean live = false;
    /** */
    private String runningSuiteName;
    /** */
    private TestsuiteNode runningSuiteNode;
    
    /**
     * Creates a new instance of ReportRootNode
     */
    RootNodeChildren(final boolean filtered) {
        super();
        this.filtered = filtered;
    }
    
    /**
     * Displays a node with a message about a test suite running.
     *
     * @param  suiteName  name of the running test suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        
        /*
         * Called from the EventDispatch thread.
         */
        
        assert EventQueue.isDispatchThread();
        assert runningSuiteName == null;
        assert runningSuiteNode == null;
        
        runningSuiteName = suiteName;
        
        if (live) {
            runningSuiteNode = new TestsuiteNode(suiteName, filtered);
            add(new Node[] {runningSuiteNode});
        }
        
        assert runningSuiteName != null;
        assert (runningSuiteNode != null) == live;
    }
    
    /**
     */
    TestsuiteNode displayReport(final Report report) {
        assert EventQueue.isDispatchThread();
        assert (runningSuiteNode != null)
               == (live && (runningSuiteName != null));
        
        TestsuiteNode correspondingNode;
        
        if (reports == null) {
            reports = new ArrayList(10);
        }
        reports.add(report);

        final boolean isPassedSuite = updateStatistics(report);
        
        if (runningSuiteName != null) {
            runningSuiteName = null;
            
            if (live) {
                if (filtered && isPassedSuite) {
                    remove(new Node[] {runningSuiteNode});
                    correspondingNode = null;
                } else {
                    runningSuiteNode.displayReport(report);
                    correspondingNode = runningSuiteNode;
                }
                runningSuiteNode = null;
            } else {
                correspondingNode = null;
            }
        } else {
            if (live && !(filtered && isPassedSuite)) {
                add(new Node[] {
                    correspondingNode = createNode(report)});
            } else {
                correspondingNode = null;
            }
        }
        
        assert runningSuiteName == null;
        assert runningSuiteNode == null;
        
        return correspondingNode;
    }
    
    /**
     */
    void displayReports(final Collection/*<Report>*/ newReports) {
        assert EventQueue.isDispatchThread();
        
        if (reports == null) {
            reports = new ArrayList(newReports);
        } else {
            reports.addAll(newReports);
        }
        
        if (!live) {
            for (Iterator it = reports.iterator(); it.hasNext(); ) {
                updateStatistics((Report) it.next());
            }
        } else {
            Node[] nodesToAdd;
            if (!filtered) {
                nodesToAdd = new Node[newReports.size()];
                int index = 0;
                for (Iterator it = newReports.iterator(); it.hasNext(); index++) {
                    Report report = (Report) it.next();
                    updateStatistics(report);
                    nodesToAdd[index] = createNode(report);
                }
                add(nodesToAdd);
            } else {
                List toAdd = new ArrayList(newReports.size());
                for (Iterator it = newReports.iterator(); it.hasNext(); ) {
                    Report report = (Report) it.next();
                    boolean isFailed = updateStatistics(report);
                    if (isFailed) {
                        toAdd.add(createNode(report));
                    }
                }
                if (!toAdd.isEmpty()) {
                    nodesToAdd = (Node[]) toAdd.toArray(new Node[toAdd.size()]);
                    add(nodesToAdd);
                }
            }
        }
    }
    
    /**
     * Updates statistics of reports (passed/failed test suites).
     * It is called when a report node is about to be added.
     *
     * @param  report  report for which a node is to be added
     * @return  <code>true</code> if the report reports a passed test suite,
     *          <code>false</code> if the report reports a failed test suite
     */
    private boolean updateStatistics(final Report report) {
        
        /* Called from the EventDispatch thread */
        
        final boolean isPassedSuite = !report.containsFailed();
        if (isPassedSuite) {
            passedSuites++;
        } else {
            failedSuites++;
        }
        return isPassedSuite;
    }
    
    // PENDING - synchronization
    
    /**
     */
    protected void addNotify() {
        super.addNotify();
        
        live = true;                      //PENDING
        addAllMatchingNodes();
    }
    
    /**
     */
    protected void removeNotify() {
        super.removeNotify();
        
        remove(getNodes());               //PENDING
        live = false;
    }
    
    /**
     * Adds all nodes matching the current filter (if the filter is enabled)
     * or all nodes generally (if the filter is off).
     */
    private void addAllMatchingNodes() {
        final boolean filterOn = filtered;
        final int matchingNodesCount = filterOn ? failedSuites
                                                : failedSuites + passedSuites;
        final int nodesCount = (runningSuiteNode != null)
                               ? matchingNodesCount + 1
                               : matchingNodesCount;
        if (nodesCount != 0) {
            final Node[] nodes = new Node[nodesCount];
            final Iterator i = reports.iterator();
            int index = 0;
            while (index < matchingNodesCount) {
                Report report = (Report) i.next();
                if (!filterOn || report.containsFailed()) {
                    nodes[index++] = createNode(report);
                }
            }
            if (runningSuiteNode != null) {
                nodes[index++] = runningSuiteNode;
            }
            add(nodes);
        }
    }
    
    /**
     */
    private void removeAllNodes() {
        remove(getNodes());
    }
    
    /**
     */
    private TestsuiteNode createNode(final Report report) {
        return new TestsuiteNode(report, filtered);
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        assert EventQueue.isDispatchThread();
        
        if (filtered == this.filtered) {
            return;
        }
        this.filtered = filtered;
        
        if (!live) {
            return;
        }
        if (passedSuites == 0) {
            return;
        }

        if (filtered) {
            removePassedSuites();
        } else {
            addPassedSuites();
        }
    }
    
    /**
     */
    private void removePassedSuites() {
        assert EventQueue.isDispatchThread();
        assert live;
        
        final Node[] nodesToRemove = new Node[passedSuites];
        final Node[] nodes = getNodes();
        int nodesIndex = 0;
        for (int index = 0;
                    index < nodesToRemove.length;
                    nodesIndex++) {
            TestsuiteNode node = (TestsuiteNode) nodes[nodesIndex];
            Report report = node.getReport();
            if (report == null) {
                continue;
            }
            if (!report.containsFailed()) {
                nodesToRemove[index++] = node;
            } else {
                node.setFiltered(filtered);
            }
        }
        while (nodesIndex < nodes.length) {
            Report report;
            assert (report = ((TestsuiteNode) nodes[nodesIndex]).getReport())
                           == null
                   || report.containsFailed();
            ((TestsuiteNode) nodes[nodesIndex++]).setFiltered(filtered);
        }
        remove(nodesToRemove);
    }
    
    /**
     */
    private void addPassedSuites() {
        assert EventQueue.isDispatchThread();
        assert live;
        
        removeAllNodes();
        addAllMatchingNodes();
    }
    
}
