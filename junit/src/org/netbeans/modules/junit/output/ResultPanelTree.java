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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.accessibility.AccessibleContext;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class ResultPanelTree extends JPanel
                            implements ExplorerManager.Provider,
                                       PropertyChangeListener {

    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;
    /** root node of the tree */
    private final ReportNode rootNode;
    /** */
    private final BeanTreeView treeView;
    /** should the results be filtered (only failures and errors displayed)? */
    private boolean filtered = false;
    /** */
    private ChangeListener changeListener;
    /** */
    private ChangeEvent changeEvent;
    /** */
    private final Report report;
    /**
     * holds an instance of RegexpUtils so that implementations of nodes
     * displayed in the tree always get the same instance when they ask for one
     * using RegexpUtils.getInstance().
     */
    private final RegexpUtils regexpUtils = RegexpUtils.getInstance();

    ResultPanelTree(final Report report) {
        super(new java.awt.BorderLayout());
        
        add(treeView = new ResultTreeView(), java.awt.BorderLayout.CENTER);
        
        this.report = report;

        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(rootNode = createRootNode());
        explorerManager.addPropertyChangeListener(this);

        initAccessibility();
    }
    
    /**
     */
    private void initAccessibility() {
        AccessibleContext accessCtx;

        accessCtx = getAccessibleContext();
        accessCtx.setAccessibleName(
               NbBundle.getMessage(getClass(), "ACSD_ResultPanelTree"));//NOI18N
        accessCtx.setAccessibleDescription(
               NbBundle.getMessage(getClass(), "ACSN_ResultPanelTree"));//NOI18N

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
    void viewOpened() {
        assert EventQueue.isDispatchThread();

        //PENDING:
        //selectAndActivateNode(rootNode);
    }

    /**
     */
    private ReportNode createRootNode() {
        return new ReportNode(report);
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
    

}
