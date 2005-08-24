/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
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
