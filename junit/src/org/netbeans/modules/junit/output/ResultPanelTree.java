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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class ResultPanelTree extends JPanel
                            implements ExplorerManager.Provider,
                                       PropertyChangeListener {

    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(
            ResultPanelTree.class);
    
    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;
    /** root node of the tree */
    private final RootNode rootNode;
    /** */
    private final ResultTreeView treeView;
    /** should the results be filtered (only failures and errors displayed)? */
    private boolean filtered = false;
    /** */
    private ChangeListener changeListener;
    /** */
    private ChangeEvent changeEvent;
    /**
     * holds an instance of RegexpUtils so that implementations of nodes
     * displayed in the tree always get the same instance when they ask for one
     * using RegexpUtils.getInstance().
     */
    private final RegexpUtils regexpUtils = RegexpUtils.getInstance();
    /** */
    private final ResultDisplayHandler displayHandler;

    ResultPanelTree(ResultDisplayHandler displayHandler) {
        super(new java.awt.BorderLayout());
        treeView = new ResultTreeView();
        treeView.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TestResults"));
        treeView.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TestResults"));
        add(treeView, java.awt.BorderLayout.CENTER);
        
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode = new RootNode(filtered));
        explorerManager.addPropertyChangeListener(this);

        initAccessibility();
        
        this.displayHandler = displayHandler;
    }
    
    /**
     */
    private void initAccessibility() {
        AccessibleContext accessCtx;

        accessCtx = getAccessibleContext();
        accessCtx.setAccessibleName(
               NbBundle.getMessage(getClass(), "ACSN_ResultPanelTree"));//NOI18N
        accessCtx.setAccessibleDescription(
               NbBundle.getMessage(getClass(), "ACSD_ResultPanelTree"));//NOI18N

        accessCtx = treeView.getHorizontalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
               NbBundle.getMessage(getClass(),
                                   "ACSN_HorizontalScrollbar"));        //NOI18N

        accessCtx = treeView.getVerticalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
               NbBundle.getMessage(getClass(),
                                   "ACSN_HorizontalScrollbar"));        //NOI18N

    }
    
    /**
     */
    void displayMsg(String msg) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        rootNode.displayMessage(msg);
    }
    
    /**
     */
    public void addNotify() {
        super.addNotify();
        
        final Object[] pendingOutput;
        
        displayHandler.setTreePanel(this);
    }
    
    /**
     * Displays a message about a running suite.
     *
     * @param  suiteName  name of the running suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        rootNode.displaySuiteRunning(suiteName);
    }
    
    /**
     */
    void displayReport(final Report report) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        TestsuiteNode node = rootNode.displayReport(report);
        if ((node != null) && report.containsFailed()) {
            treeView.expandReportNode(node);
        }
    }
    
    /**
     * @param  reports  non-empty list of reports to be displayed
     */
    void displayReports(final List<Report> reports) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        if (reports.size() == 1) {
            displayReport(reports.get(0));
        } else {
            rootNode.displayReports(reports);
        }
    }
    
    /**
     */
    int getSuccessDisplayedLevel() {
        return rootNode.getSuccessDisplayedLevel();
    }
    
    /**
     */
    void viewOpened() {
        assert EventQueue.isDispatchThread();

        //PENDING:
        //selectAndActivateNode(rootNode);
    }

    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        
        this.filtered = filtered;
        
        rootNode.setFiltered(filtered);
    }

    /**
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(
                        e.getPropertyName())) {
            nodeSelectionChanged();
        }
    }

    /**
     */
    private void nodeSelectionChanged() {
        assert EventQueue.isDispatchThread();

        fireChange();
    }

    /**
     */
    void setChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();

        this.changeListener = l;
        if (changeListener == null) {
            changeEvent = null;
        } else if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
        }
    }
    
    /**
     */
    private void fireNodeSelectionChange() {
        fireChange();
    }

    /**
     */
    private void fireChange() {
        assert EventQueue.isDispatchThread();

        if (changeListener != null) {
            changeListener.stateChanged(changeEvent);
        }
    }

    /**
     */
    Node[] getSelectedNodes() {
        return explorerManager.getSelectedNodes();
    }

    /**
     * Selects and activates a given node.
     * Selects a given node in the tree.
     * If the nodes cannot be selected and/or activated,
     * clears the selection (and notifies that no node is currently
     * activated).
     * 
     * @param  node  node to be selected and activated
     */
    private void selectAndActivateNode(final Node node) {
        Node[] nodeArray = new Node[] {node};
        try {
            explorerManager.setSelectedNodes(nodeArray);
            fireNodeSelectionChange();
        } catch (PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            nodeArray = new Node[0];
            try {
                explorerManager.setSelectedNodes(nodeArray);
                fireNodeSelectionChange();
            } catch (PropertyVetoException ex2) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex2);
            }
        }
    }

    /**
     */
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    /**
     */
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

}
