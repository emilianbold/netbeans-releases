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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.casaeditor.navigator;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;


import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author Nathan Fiedler
 * @version 1.0
 */
public class CasaNavigatorContentPanel extends JPanel
implements ExplorerManager.Provider, HelpCtx.Provider, Runnable, PropertyChangeListener 
{
    
    static final long serialVersionUID = 9068382751477968023L;
    
    private CasaWrapperModel mCasaModel;
    private CasaDataObject mDataObject;
    private CasaNodeFactory mNodeFactory;
    private boolean isRequireRepaint;
    private ExplorerManager myExplorerManager;
    private TreeView treeView;
    
    private final JLabel notAvailableLabel = new JLabel(
            NbBundle.getMessage(CasaNavigatorContentPanel.class, "MSG_NotAvailable"));  // NOI18N
    
    
    public CasaNavigatorContentPanel() {
        setLayout(new BorderLayout());
        
        myExplorerManager = new ExplorerManager();
        treeView = new BeanTreeView();
        treeView.setRootVisible(true);
        treeView.setEnabled(true);
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeView.setDefaultActionAllowed(true);
        
        myExplorerManager.addPropertyChangeListener(this);
        
        notAvailableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailableLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); // NOI18N   
        notAvailableLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailableLabel.setOpaque(true);
    }
    
    
    public void addNotify() {
        super.addNotify();
    }
    
    public void removeNotify() {
        super.removeNotify();
    }
    
    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }
    
    public HelpCtx getHelpCtx() {
        if (myExplorerManager != null ) {
            Node[] selNodes = myExplorerManager.getSelectedNodes();
            if (selNodes != null && selNodes.length > 0) {
                HelpCtx helpCtx = selNodes[0].getHelpCtx();
                if (helpCtx != null) {
                    return helpCtx;
                }
            }
        }
        return new HelpCtx(CasaNavigatorContentPanel.class);
    }
    
    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        Runnable runner = new Runnable() {
            public void run() {
                propertyUpdate(propertyChangeEvent);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            runner.run();
        } else {
            SwingUtilities.invokeLater(runner);
        }
    }
    
    private void propertyUpdate(PropertyChangeEvent propertyChangeEvent) {
        String propertyName = propertyChangeEvent.getPropertyName();
        TopComponent navigatorTopComponent = 
                (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);

        if (
                propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES) &&
                navigatorTopComponent != null &&
                navigatorTopComponent != TopComponent.getRegistry().getActivated()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    selectActivatedNodes();
                }
            });
            
        } else if (
                propertyName.equals(ExplorerManager.PROP_SELECTED_NODES) &&
                navigatorTopComponent != null &&
                navigatorTopComponent == TopComponent.getRegistry().getActivated()) {
            Node[] filteredNodes = (Node[]) propertyChangeEvent.getNewValue();
            if (filteredNodes != null && filteredNodes.length >= 1) {
                // Set the active nodes for the parent TopComponent.
                navigatorTopComponent.setActivatedNodes(filteredNodes);
            }

        } else if (
                propertyName.startsWith(CasaWrapperModel.PROPERTY_PREFIX)) {
            // TODO unless this is a full reload, we should just add/remove 
            // the affected node instead of resetting the tree.
            showNavTree(true);
        }
    }

    public void navigate(CasaDataObject dataObject) {
        // get the model and create the new bpel logical tree in background
        mDataObject = dataObject;
        if (dataObject != null) {
            mCasaModel = dataObject.getEditorSupport().getModel();
        } else {
            mCasaModel = null;
        }
        if (mCasaModel == null) {
            showError();
        } else {
            mNodeFactory = new CasaNodeFactory(mDataObject, mCasaModel);
            mCasaModel.removePropertyChangeListener(this);
            mCasaModel.addPropertyChangeListener(this);
            showNavTree(false);
        }
    }
    
    private void showNavTree(boolean showImmediately) {
        removeAll();
        Node rootNode = mNodeFactory.createModelNode(mCasaModel);
        myExplorerManager.setRootContext(rootNode);
        add(treeView, BorderLayout.CENTER);
        if (showImmediately) {
            run();
        } else {
            EventQueue.invokeLater(this);
        }
        revalidate();
        repaint();
    }
    
    public void run() {
        // Initially expand root node and the folder nodes below it.
        Node rootNode = myExplorerManager.getRootContext();
        treeView.expandNode(rootNode);
        expandNodes(treeView, 1, rootNode);
            selectActivatedNodes();
        }
    
    public static void expandNodes(TreeView tv, int level, Node rootNode) {
        if (level == 0) return;
        Children children = rootNode.getChildren();
        if (children != null) {
            Node[]  nodes = children.getNodes();
            if (nodes != null) {
                for (int i= 0; i < nodes.length; i++) {
                    tv.expandNode(nodes[i]); //Expand node
                    expandNodes(tv, level - 1, nodes[i]); //expand children
                }
            }
        }
    }
    
    private void selectActivatedNodes() {
        Node[] activated = TopComponent.getRegistry().getActivatedNodes();
        Node activatedNode = null;
        for (Node node : activated) {
            if (node instanceof CasaNode) {
                // Select the first CasaNode in the list.
                activatedNode = node;
                break;
            }
        }
        if (activatedNode != null) {
            List<Node> selNodes = new ArrayList<Node>();
            findNavigatorNode(
                    myExplorerManager.getRootContext(), 
                    ((CasaNode) activatedNode).getData(),
                    selNodes);
            try {
                myExplorerManager.setSelectedNodes(selNodes.toArray(new Node[0]));
            } catch (PropertyVetoException pve) {
            }
        }
    }
    
    private void findNavigatorNode(
            Node parent, 
            Object datumToFind,
            List<Node> toSelect) {
        Children children = parent.getChildren();
        for (Node n : children.getNodes()) {
            if (n instanceof CasaNode) {
                CasaNode child = (CasaNode) n;
                if (child.getData() == datumToFind) {
                    toSelect.add(child);
                    // Only interested in finding one result.
                    return;
                }
                if (child.getChildren().getNodesCount() > 0) {
                    findNavigatorNode(child, datumToFind, toSelect);
                }
            }
        }
    }
    
    private void showError() {
        if (notAvailableLabel.isShowing()) {
            return;
        }
        removeAll();
        add(notAvailableLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
