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

package org.netbeans.modules.soa.ui.tree.impl;

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
public class TreePathComparator implements Comparator<TreePath> {

    private TreeModel mTreeModel;
    
    public TreePathComparator(TreeModel treeModel) {
        mTreeModel = treeModel;
    }
    
    public int compare(TreePath path1, TreePath path2) {
        List<Object> nodeList1 = getInversedPathList(path1);
        List<Object> nodeList2 = getInversedPathList(path2);
        //
        Iterator<Object> nodeListItr1 = nodeList1.iterator();
        Iterator<Object> nodeListItr2 = nodeList2.iterator();
        //
        Object n1 = null;
        Object n2 = null;
        Object commonParent = null;
        while (nodeListItr1.hasNext() && nodeListItr2.hasNext()) {
            //
            // If the previous nodes were the same (n1 == n2) then 
            // now they are the common parent (commonParent == n1 == n2)
            commonParent = n1; 
            //
            n1 = nodeListItr1.next();
            n2 = nodeListItr2.next();
            //
            if (n1 != n2) {
                break;
            }
        }
        //
        assert (n1 != null) && (n2 != null);
        //
        if (commonParent == null) {
            // The previous while does only one step! 
            // nodeListItr1.hasNext() == false || nodeListItr2.hasNext() == false
            //
            boolean list1HasNext = nodeListItr1.hasNext();
            boolean list2HasNext = nodeListItr2.hasNext();
            //
            // Both nodes are the same and both are root
            if (!list1HasNext && !list2HasNext) {
                if (n1 == n2) {
                    return 0; // The roots are the same
                } else {
                    assert false : "trying to compare TreePath from different trees"; // NOI18N
                }
            } else if (list1HasNext) {
                return 1; // the second is the root but the first isn't
            } else if (list2HasNext) {
                return -1; // the first is the root but the secon isn't
            }
        }
        //
        assert commonParent != null;
        //
        int n1Index = mTreeModel.getIndexOfChild(commonParent, n1);
        int n2Index = mTreeModel.getIndexOfChild(commonParent, n2);
        //
        return n1Index - n2Index;
    }
    
    public List<Object> getInversedPathList(TreePath treePath) {
        LinkedList<Object> result = new LinkedList<Object>();
        //
        TreePath path = treePath;
        while (path != null) {
            Object obj = path.getLastPathComponent();
            result.addFirst((Object)obj);
            path = path.getParentPath();
        }
        //
        return result;
    }
    
}
