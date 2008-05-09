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
 * @author Marian Petras, Erno Mononen
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
    void displayMsgSessionFinished(String msg) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        rootNode.displayMessageSessionFinished(msg);
    }
    
    /**
     */
    @Override
    public void addNotify() {
        super.addNotify();
        
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
    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow();
    }

}
