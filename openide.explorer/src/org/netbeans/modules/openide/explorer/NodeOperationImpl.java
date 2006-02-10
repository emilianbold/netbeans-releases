/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openide.explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

/** 
 * Default implementation of node operations like show properties, etc.
 */
public final class NodeOperationImpl extends org.openide.nodes.NodeOperation {

    public boolean customize(Node node) {
        Component customizer = node.getCustomizer();
        if (customizer == null) {
            return false;
        }
        final JDialog d = new JDialog();
        d.setModal(false);
        d.setTitle(node.getDisplayName());
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(customizer, BorderLayout.CENTER);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.pack();
        d.setVisible(true);
        return true;
    }

    public void explore(Node n) {
        JDialog d = new JDialog();
        d.setTitle(n.getDisplayName());
        d.setModal(false);
        d.getContentPane().setLayout(new BorderLayout());
        EP p = new EP();
        p.getExplorerManager().setRootContext(n);
        p.setLayout(new BorderLayout());
        p.add(new JScrollPane(new BeanTreeView()), BorderLayout.CENTER);
        d.getContentPane().add(p, BorderLayout.CENTER);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.pack();
        d.setVisible(true);
    }

    public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top) throws UserCancelException {
        // XXX rootTitle and acceptor currently ignored
        JDialog d = new JDialog();
        d.setTitle(title);
        d.setModal(true);
        d.getContentPane().setLayout(new BorderLayout());
        EP p = new EP();
        p.getExplorerManager().setRootContext(root);
        p.setLayout(new BorderLayout());
        p.add(new JScrollPane(new BeanTreeView()), BorderLayout.CENTER);
        d.getContentPane().add(p, BorderLayout.CENTER);
        if (top != null) {
            d.getContentPane().add(top, BorderLayout.NORTH);
        }
        d.pack();
        d.setVisible(true);
        Node[] nodes = p.getExplorerManager().getSelectedNodes();
        d.dispose();
        return nodes;
    }

    public void showProperties(Node n) {
        showProperties(new Node[] {n});
    }

    public void showProperties(Node[] nodes) {
        PropertySheet ps = new PropertySheet();
        ps.setNodes(nodes);
        JDialog d = new JDialog();
        d.setTitle("Properties"); // XXX I18N
        d.setModal(true);
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(ps, BorderLayout.CENTER);
        d.pack();
        d.setVisible(true);
        d.dispose();
    }
    
    private static final class EP extends JPanel 
    implements org.openide.explorer.ExplorerManager.Provider {
        private org.openide.explorer.ExplorerManager em = new org.openide.explorer.ExplorerManager ();
        
        public org.openide.explorer.ExplorerManager getExplorerManager () {
            return em;
        }
    }
}
