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

package threaddemo.views.looktree;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import org.netbeans.spi.looks.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Tree model displaying a tree of represented objects using looks.
 * @author Jesse Glick
 */
final class LookTreeModel implements TreeModel {
    
    private static final Logger logger = Logger.getLogger(LookTreeModel.class.getName());
    
    private final Object rootObject;
    private final LookSelector sel;
    private LookTreeNode root;
    private final List listeners; // List<TreeModelListener>
    
    public LookTreeModel(Object root, LookSelector sel) {
        listeners = new ArrayList();
        this.rootObject = root;
        this.sel = sel;
    }
    
    public void addNotify() {
        root = LookTreeNode.createRoot(rootObject, sel, this);
        fireChildrenChange(root);
    }
    
    public void removeNotify() {
        root.forgetEverything();
        root = null;
    }
    
    public Object getRoot() {
        return root;
    }
    
    public Object getChild(Object parent, int index) {
        LookTreeNode n = (LookTreeNode)parent;
        return n.getChild(index);
    }
    
    public int getChildCount(Object parent) {
        LookTreeNode n = (LookTreeNode)parent;
        //logger.log(Level.FINER, "childCount of {0} is {1}", new Object[] {parent, n.getChildren().size()});
        return n.getChildCount();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        LookTreeNode n = (LookTreeNode)parent;
        return n.getIndexOfChild((LookTreeNode)child);
    }
    
    public boolean isLeaf(Object node) {
        LookTreeNode n = (LookTreeNode)node;
        return n.isLeaf();
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        LookTreeNode n = (LookTreeNode)path.getLastPathComponent();
        try {
            n.getLook().rename( n.getData(), (String)newValue, n.getLookup() );
            // XXX cell renderer does not adjust size to match new value...
        } catch (IOException e) {
            // More or less normal.
            logger.info(e.toString());
        }
    }
    
    void fireDisplayChange(LookTreeNode source) {
        if (listeners.isEmpty()) {
            return;
        }
        LookTreeNode parent = source.getParent();
        TreePath path = findPath(parent != null ? parent : source);
        int[] childIndices = parent != null ? new int[] {getIndexOfChild(parent, source)} : null;
        Object[] children = parent != null ? new Object[] {source} : null;
        TreeModelEvent ev = new TreeModelEvent(this, path, childIndices, children);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            TreeModelListener l = (TreeModelListener)it.next();
            l.treeNodesChanged(ev);
        }
    }
    
    void fireChildrenChange(LookTreeNode source) {
        logger.log(Level.FINER, "fireChildrenChange: {0}", source);
        if (listeners.isEmpty()) {
            return;
        }
        // XXX this is crude, could try to actually compute added/removed children...
        TreePath path = (source == root) ? null : findPath(source.getParent());
        TreeModelEvent ev = new TreeModelEvent(this, path, null, null);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            TreeModelListener l = (TreeModelListener)it.next();
            logger.log(Level.FINER, "firing: {0} to {1}", new Object[] {ev, l});
            l.treeStructureChanged(ev);
        }
    }
    
    private TreePath findPath(LookTreeNode node) {
        /*
        ArrayList l = new ArrayList(20);
        for (LookTreeNode n = node; n != null; n = n.getParent()) {
            l.add(n);
        }
        Collections.reverse(l);
        return new TreePath(l.toArray());
         */
        return new LookTreePath(node);
    }
    
}
