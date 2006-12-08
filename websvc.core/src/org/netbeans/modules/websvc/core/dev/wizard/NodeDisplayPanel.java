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
package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;


/**
 *
 * @author ChrisWebster
 */
public class NodeDisplayPanel extends JPanel implements ExplorerManager.Provider {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ExplorerManager manager = new ExplorerManager();
    
    /** Creates a new instance of NodeDisplayPanel */
    public NodeDisplayPanel(Node rootNode) {
        BeanTreeView btv = new BeanTreeView();
        btv.setRootVisible(false);
        btv.setDefaultActionAllowed(false);
        btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        manager.setRootContext(rootNode);
        Node[] rootChildren = rootNode.getChildren().getNodes();
        for (int i = 0; i < rootChildren.length; i++) {
            btv.expandNode(rootChildren[i]);
        }
        manager.addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                firePropertyChange();
            }
        });
        setLayout(new BorderLayout());
        add(btv, BorderLayout.CENTER);
        btv.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_NodeTreeView"));
        btv.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_NodeTreeView"));
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    private void firePropertyChange() {
        pcs.firePropertyChange(ExplorerManager.PROP_NODE_CHANGE, null, null);
    }
    
    public Node[] getSelectedNodes() {
        return manager.getSelectedNodes();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
