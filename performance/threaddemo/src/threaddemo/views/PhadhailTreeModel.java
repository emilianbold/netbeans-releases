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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import threaddemo.model.Phadhail;

/**
 * Tree model displaying phadhails directly.
 * @author Jesse Glick
 */
final class PhadhailTreeModel implements TreeModel {
    
    private final Phadhail root;
    
    public PhadhailTreeModel(Phadhail root) {
        this.root = root;
    }
    
    public Object getRoot() {
        return root;
    }
    
    public Object getChild(Object parent, int index) {
        return ((Phadhail)parent).getChildren()[index];
    }
    
    public int getChildCount(Object parent) {
        return ((Phadhail)parent).getChildren().length;
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        Phadhail[] kids = ((Phadhail)parent).getChildren();
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] == child) {
                return i;
            }
        }
        return -1;
    }
    
    public boolean isLeaf(Object node) {
        return !((Phadhail)node).hasChildren();
    }
    
    public void addTreeModelListener(TreeModelListener l) {}
    
    public void removeTreeModelListener(TreeModelListener l) {}
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new IllegalStateException();
    }
    
}
