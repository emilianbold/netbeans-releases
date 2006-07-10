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

import javax.swing.tree.TreePath;

/**
 * Path from root to a lower "bottom" node in the look tree.
 * @author Jesse Glick
 */
public final class LookTreePath extends TreePath {

    private final LookTreeNode bottom;

    public LookTreePath(LookTreeNode node) {
        bottom = node;
    }

    public Object[] getPath() {
        int c = getPathCount();
        Object[] path = new Object[c];
        LookTreeNode n = bottom;
        for (int i = c - 1; i >= 0; i--) {
            path[i] = n;
            n = n.getParent();
        }
        return path;
    }
    
    public int getPathCount() {
        LookTreeNode n = bottom;
        int i = 0;
        while (n != null) {
            i++;
            n = n.getParent();
        }
        return i;
    }
    
    public Object getPathComponent(int x) {
        int c = getPathCount();
        LookTreeNode n = bottom;
        for (int i = 0; i < c - x - 1; i++) {
            n = n.getParent();
        }
        return n;
    }
    
    public TreePath pathByAddingChild(Object child) {
        return new LookTreePath((LookTreeNode)child);
    }
    
    public boolean equals(Object o) {
        return (o instanceof LookTreePath) &&
            ((LookTreePath)o).bottom == bottom;
    }
    
    public int hashCode() {
        return bottom.hashCode();
    }
    
    public Object getLastPathComponent() {
        return bottom;
    }
    
    public TreePath getParentPath() {
        LookTreeNode p = bottom.getParent();
        if (p != null) {
            return new LookTreePath(p);
        } else {
            return null;
        }
    }
    
}

