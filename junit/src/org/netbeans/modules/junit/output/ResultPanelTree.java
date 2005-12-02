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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Marian Petras
 */
final class ResultPanelTree extends JPanel
                            implements ExplorerManager.Provider,
                                       PropertyChangeListener,
                                       ItemListener {

    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;
    /** root node of the tree */
    private final RootNode rootNode;
    /** */
    private final ResultTreeView treeView;
    /** */
    private AbstractButton btnFilter;
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
        
        add(treeView = new ResultTreeView(), java.awt.BorderLayout.CENTER);
        
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode = createRootNode());
        explorerManager.addPropertyChangeListener(this);

        initFilter();
        initAccessibility();
        
        this.displayHandler = displayHandler;
    }
    
    /**
     */
    private void initFilter() {
        btnFilter = new JToggleButton(new ImageIcon(
                Utilities.loadImage(
                    "org/netbeans/modules/junit/output/res/filter.png", //NOI18N
                    true)));
        btnFilter.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_FilterButton"));  //NOI18N
        btnFilter.setEnabled(false);
        
        updateFilter();
        btnFilter.addItemListener(this);
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
    AbstractButton getFilterButton() {
        return btnFilter;
    }
    
    /**
     */
    private void updateFilter() {
        final boolean filtered = btnFilter.isSelected();
        String key = filtered
                     ? "MultiviewPanel.btnFilter.showAll.tooltip"       //NOI18N
                     : "MultiviewPanel.btnFilter.showFailures.tooltip"; //NOI18N
        setFiltered(filtered);
        btnFilter.setToolTipText(NbBundle.getMessage(getClass(), key));
    }
    
    /**
     */
    public void itemStateChanged(ItemEvent e) {
        /* called when the Filter button is toggled. */
        updateFilter();
    }
    
    /**
     */
    void displayMsg(String msg) {
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
     */
    void displayReport(final Report report) {
        rootNode.displayReport(report);
        if (report.containsFailed()) {
            treeView.expandNodes(rootNode);     //PENDING - is it what you want?
        }
        
        btnFilter.setEnabled(
             rootNode.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }
    
    /**
     */
    void displayReports(final List/*<Report>*/ reports) {
        final int count = reports.size();
        if (count == 0) {
            return;
        }
        
        if (count == 1) {
            displayReport((Report) reports.get(0));
        } else {
            rootNode.displayReports(reports);
        }
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
    private RootNode createRootNode() {
        return new RootNode();
    }
    
    /**
     */
    void setFiltered(final boolean filtered) {
        if (filtered == this.filtered) {
            return;
        }
        
        this.filtered = filtered;
        
        if (rootNode != null) {
            rootNode.setFiltered(filtered);
        }
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
