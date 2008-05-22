/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ruby.testrunner.ui;

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
    private Collection<Report> reports;
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
            reports = new ArrayList<Report>(10);
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
    void displayReports(final Collection<Report> newReports) {
        assert EventQueue.isDispatchThread();
        
        if (reports == null) {
            reports = new ArrayList<Report>(newReports);
        } else {
            reports.addAll(newReports);
        }
        
        if (!live) {
            for (Report report : reports) {
                updateStatistics(report);
            }
        } else {
            Node[] nodesToAdd;
            if (!filtered) {
                nodesToAdd = new Node[newReports.size()];
                int index = 0;
                for (Report report : newReports) {
                    updateStatistics(report);
                    nodesToAdd[index++] = createNode(report);
                }
                add(nodesToAdd);
            } else {
                List<Node> toAdd = new ArrayList<Node>(newReports.size());
                for (Report report : newReports) {
                    boolean isFailed = updateStatistics(report);
                    if (isFailed) {
                        toAdd.add(createNode(report));
                    }
                }
                if (!toAdd.isEmpty()) {
                    nodesToAdd = toAdd.toArray(new Node[toAdd.size()]);
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
    @Override
    protected void addNotify() {
        super.addNotify();
        
        live = true;                      //PENDING
        addAllMatchingNodes();
    }
    
    /**
     */
    @Override
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
            final Iterator<Report> i = reports.iterator();
            int index = 0;
            while (index < matchingNodesCount) {
                Report report = i.next();
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
