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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TreeEditorView extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider,
        PropertyChangeListener/*, HelpCtx.Provider*/ {
    
    /**
     * 
     */
    private static final long serialVersionUID = -6844839168489591934L;
    
    private BeanTreeView btv;
    
    private ExplorerManager explorerManager;
    private transient Lookup lookup;
    
    public static final String PROP_VALID_NODE_SELECTED = "PROP_VALID_NODE_SELECTED";//NOI18N
    
    public static final String PROP_DUPLICATE_NODE_SELECTED = "PROP_DUPLICATE_NODE_SELECTED";//NOI18N
    
    private Node mRootNode;
    
    private WSDLModel mModel;

    public TreeEditorView(WSDLModel model) {
        this.mModel = model;
        initGUI();
    }
    
    private void initGUI() {
        setLayout(new BorderLayout());
        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible( true );
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
        btv.setPopupAllowed( true );
        add(btv, BorderLayout.CENTER);

        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction,
                ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction,
                ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction,
                ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", //NOI18N
                ExplorerUtils.actionDelete(explorerManager, false));
        lookup = ExplorerUtils.createLookup(explorerManager, map);

        // Must do this when the component is in the UI tree.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                //mRootNode = new AbstractNode(new Children.Array());
                populateRootNode(mModel.getDefinitions());
                //Initially expand root node and the folder nodes below it.
                btv.expandNode(mRootNode);
                Utility.expandNodes(btv, 1, mRootNode);
                try {
                    explorerManager.setSelectedNodes(new Node[] {mRootNode});
                } catch (PropertyVetoException pve) {
                }
            }
        });
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    private void populateRootNode(Definitions definitions) {
        if (definitions != null) {
            if (mRootNode == null) {
                Node dNode = NodesFactory.getInstance().create(definitions);
                mRootNode = dNode;
            }
            explorerManager.setRootContext( mRootNode );
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length > 0) {
                //nodes[0].getChildren().getNodes(true);
                TopComponent tc = findParentTopComponent();
                // We cannot assume that we are visible, so check for null.
                if (tc != null) {
                    tc.setActivatedNodes(nodes);
                }
            }
        }
    }

    /**
     * Finds the TopComponent that contains us.
     *
     * @return  the parent TopComponent.
     */
    private TopComponent findParentTopComponent() {
        return (TopComponent) SwingUtilities.getAncestorOfClass(
                TopComponent.class, this);
    }
    
    public void showComponent(WSDLComponent sc) {
        List<Node> path = UIUtilities.findPathFromRoot(
                getExplorerManager().getRootContext(), sc);
        if (path == null || path.isEmpty()) {
            return;
        }
        Node node = path.get(path.size() - 1);
        try {
            getExplorerManager().setExploredContextAndSelection(
                    node, new Node[] { node });
        } catch (PropertyVetoException pve) {
        }
    }
    
    public void showComponent(SchemaComponent sc) {
        List<Node> path = UIUtilities.findPathFromRoot(
                getExplorerManager().getRootContext(), sc, mModel);
        if (path == null || path.isEmpty()) {
            return;
        }
        Node node = path.get(path.size() - 1);
        try {
            getExplorerManager().setExploredContextAndSelection(
                    node, new Node[] { node });
        } catch (PropertyVetoException pve) {
        }
    }
    
// IZ 96828: suppress help for nodes, just use WSDL view help topic.
//    public HelpCtx getHelpCtx() {
//        HelpCtx ctx = new HelpCtx(TreeEditorView.class);
//        Node[] selNodes = getExplorerManager().getSelectedNodes();
//        if (selNodes != null && selNodes.length > 0) {
//            for (Node node : selNodes) {
//                if (node.getHelpCtx() != null) {
//                    return node.getHelpCtx();
//                }
//            }
//        }
//        return ctx;
//    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        btv.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return btv.requestFocusInWindow();
    }
}
