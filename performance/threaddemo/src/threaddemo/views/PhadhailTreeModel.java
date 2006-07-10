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

package threaddemo.views;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import threaddemo.model.Phadhail;

// XXX listen to changes in display name, delete, rename, new

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
        return ((Phadhail)parent).getChildren().get(index);
    }
    
    public int getChildCount(Object parent) {
        return ((Phadhail)parent).getChildren().size();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        return ((Phadhail)parent).getChildren().indexOf(child);
    }
    
    public boolean isLeaf(Object node) {
        return !((Phadhail)node).hasChildren();
    }
    
    public void addTreeModelListener(TreeModelListener l) {}
    
    public void removeTreeModelListener(TreeModelListener l) {}
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        assert false;
    }
    
}
