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
    private volatile boolean filtered = false;
    /** */
    private Collection/*<Report>*/ reports;
    /** */
    private volatile int passedSuites;
    /** */
    private volatile int failedSuites;
    /** */
    private volatile boolean live = false;  //PENDING - temporary (shld be 'false')
    
    /**
     * Creates a new instance of ReportRootNode
     */
    RootNodeChildren() {
        super();
    }
    
    /**
     */
    void displayReport(final Report report) {
        
        /*
         * May be called from various threads but is always called from
         * a synchronized block of RootNode.
         */
        
        if (reports == null) {
            reports = new ArrayList(10);
        }
        reports.add(report);
        
        boolean isPassedSuite = updateStatistics(report);
        
        if (live && !(filtered && isPassedSuite)) {
            add(new Node[] {createNode(report)});
        }
    }
    
    /**
     */
    void displayReports(final Collection/*<Report>*/ newReports) {
        
        /*
         * May be called from various threads but is always called from
         * a synchronized block of RootNode.
         */
        
        if (reports == null) {
            reports = new ArrayList(newReports);
        } else {
            reports.addAll(newReports);
        }
        
        if (!live) {
            return;
        }
        
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
    
    /**
     * Updates statistics of reports (passed/failed test suites).
     * It is called when a report node is about to be added.
     *
     * @param  report  report for which a node is to be added
     * @return  <code>true</code> if the report reports a passed test suite,
     *          <code>false</code> if the report reports a failed test suite
     */
    private boolean updateStatistics(final Report report) {
        
        /*
         * May be called from various threads but is always called from
         * a synchronized block of RootNode.
         */
        
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
        
        /* May be called from arbitrary thread. */
        
        super.addNotify();
        
        live = true;                      //PENDING
        addAllMatchingNodes();
    }
    
    /**
     */
    protected void removeNotify() {
        
        /* May be called from arbitrary thread. */
        
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
        final int nodesCount = filterOn ? failedSuites
                                        : failedSuites + passedSuites;
        if (nodesCount != 0) {
            final Node[] nodes = new Node[nodesCount];
            final Iterator i = reports.iterator();
            int remainder = nodesCount;
            for (int index = 0; index < nodesCount; ) {
                Report report = (Report) i.next();
                if (!filterOn || report.containsFailed()) {
                    nodes[index++] = createNode(report);
                }
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
    private Node createNode(final Report report) {
        return new TestsuiteNode(report);
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        
        /* May be called from arbitrary thread. */
        
        synchronized (((RootNode) getNode()).getLock()) {
            if (filtered == this.filtered) {
                return;
            }
            this.filtered = filtered;

            if (!live) {
                return;
            }
            if (filtered && (failedSuites == 0)) {
                return;
            }
            if (!filtered && (passedSuites == 0)) {
                return;
            }

            if (filtered) {
                removePassedSuites();
            } else {
                addPassedSuites();
            }
        }
    }
    
    /**
     */
    private void removePassedSuites() {
        
        /*
         * May be called from various threads but is always called from
         * a synchronized block of RootNode.
         */

        assert live;
        
        final Node[] nodesToRemove = new Node[passedSuites];
        final Iterator i = reports.iterator();
        
        Children.MUTEX.readAccess(new Runnable() {
            public void run() {
                final Node[] nodes = getNodes();
                for (int index = 0, nodesIndex = 0;
                            index < nodesToRemove.length;
                            nodesIndex++) {
                    Report report = (Report) i.next();
                    if (!report.containsFailed()) {
                        nodesToRemove[index++] = nodes[nodesIndex];
                    }
                }
                remove(nodesToRemove);
            }
        });
    }
    
    /**
     */
    private void addPassedSuites() {
        
        /*
         * May be called from various threads but is always called from
         * a synchronized block of RootNode.
         */

        assert live;
        
        Children.MUTEX.readAccess(new Runnable() {
            public void run() {
                removeAllNodes();
                addAllMatchingNodes();
            }
        });
    }
    
}
