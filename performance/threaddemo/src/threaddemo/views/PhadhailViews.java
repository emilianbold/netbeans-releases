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

package threaddemo.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyVetoException;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeModel;
import threaddemo.views.looktree.LookTreeView;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import threaddemo.model.Phadhail;
import org.netbeans.api.nodes2looks.Nodes;
import org.openide.actions.PopupAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * Factory for views over Phadhail.
 * All views are automatically scrollable; you do not need to wrap them in JScrollPane.
 * @author Jesse Glick
 */
public class PhadhailViews {
    
    private PhadhailViews() {}
    
    private static Component nodeBasedView(Node root) {
        ExplorerPanel p = new ExplorerPanel();
        p.setLayout(new BorderLayout());
        p.add(new BeanTreeView(), BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(root);
        try {
            p.getExplorerManager().setSelectedNodes(new Node[] {root});
        } catch (PropertyVetoException pve) {
            pve.printStackTrace();
        }
        CallbackSystemAction a = (CallbackSystemAction)SystemAction.get(PopupAction.class);
        Object key = a.getActionMapKey();
        p.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("shift F10"), key);
        // XXX shouldn't TreeView bind this in the ActionMap automatically?
        p.getActionMap().put(key, a /*new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.err.println("S-F10!");
            }
        }*/);
        return p;
    }
    
    /** use Nodes API with an Explorer view */
    public static Component nodeView(Phadhail root) {
        return nodeBasedView(new PhadhailNode(root));
    }
    
    /** use Looks and Nodes API with an Explorer view */
    public static Component lookNodeView(Phadhail root) {
        return nodeBasedView(Nodes.node(root, null, new PhadhailLookSelector()));
    }
    
    /** use raw Looks API with a JTree */
    public static Component lookView(Phadhail root) {
        // XXX pending a stable API...
        return new JScrollPane(new LookTreeView(root, new PhadhailLookSelector()));
    }
    
    /** use Phadhail directly in a JTree */
    public static Component rawView(Phadhail root) {
        TreeModel model = new PhadhailTreeModel(root);
        JTree tree = new JTree(model) {
            // Could also use a custom TreeCellRenderer, but this is a bit simpler for now.
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Phadhail ph = (Phadhail)value;
                return ph.getPath();
            }
        };
        tree.setLargeModel(true);
        return new JScrollPane(tree);
    }
    
}
