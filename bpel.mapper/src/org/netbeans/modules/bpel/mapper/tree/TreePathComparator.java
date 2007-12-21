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

package org.netbeans.modules.bpel.mapper.tree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Compares two tree nodes by their order in tree (above, beneath)
 *
 * @author nk160297
 */
class TreePathComparator implements Comparator<TreePath> {

    private TreeModel mTreeModel;
    
    public TreePathComparator(TreeModel treeModel) {
        mTreeModel = treeModel;
    }
    
    public int compare(TreePath path1, TreePath path2) {
        List<MapperTreeNode> nodeList1 = getInversedPathList(path1);
        List<MapperTreeNode> nodeList2 = getInversedPathList(path2);
        //
        Iterator<MapperTreeNode> nodeListItr1 = nodeList1.iterator();
        Iterator<MapperTreeNode> nodeListItr2 = nodeList2.iterator();
        //
        MapperTreeNode n1 = null;
        MapperTreeNode n2 = null;
        MapperTreeNode parent = null;
        while (nodeListItr1.hasNext() && nodeListItr2.hasNext()) {
            n1 = nodeListItr1.next();
            n2 = nodeListItr2.next();
            Object dObj1 = n1.getDataObject();
            Object dObj2 = n2.getDataObject();
            //
            if (dObj1.equals(dObj2)) {
                parent = n1;
            } else {
                break;
            }
        }
        //
        assert (n1 != null) && (n2 != null);
        //
        assert parent == n2.getParent();
        //
        int n1Index = mTreeModel.getIndexOfChild(parent, n1);
        int n2Index = mTreeModel.getIndexOfChild(parent, n2);
        //
        return n1Index - n2Index;
    }
    
    public List<MapperTreeNode> getInversedPathList(TreePath treePath) {
        LinkedList<MapperTreeNode> result = new LinkedList<MapperTreeNode>();
        //
        TreePath path = treePath;
        while (path != null) {
            Object obj = path.getLastPathComponent();
            assert obj instanceof MapperTreeNode;
            result.addFirst((MapperTreeNode)obj);
            path = path.getParentPath();
        }
        //
        return result;
    }
    
    
}
