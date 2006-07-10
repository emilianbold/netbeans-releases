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

package threaddemo.views.looktree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.spi.looks.LookSelector;

/**
 * Tree model displaying a tree of represented objects using looks.
 * @author Jesse Glick
 */
final class LookTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(LookTreeModel.class.getName());
    
    private final Object rootObject;
    private final LookSelector sel;
    private LookTreeNode root;
    private final List<TreeModelListener> listeners;
    
    public LookTreeModel(Object root, LookSelector sel) {
        listeners = new ArrayList<TreeModelListener>();
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
        for (TreeModelListener l : listeners) {
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
        for (TreeModelListener l : listeners) {
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
