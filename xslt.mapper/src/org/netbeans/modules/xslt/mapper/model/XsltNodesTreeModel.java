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
package org.netbeans.modules.xslt.mapper.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author nk160297
 */
public abstract class XsltNodesTreeModel implements TreeModel {
    
    private TreeNode myRootNode;
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    private XsltMapper mapper;
    public XsltNodesTreeModel(XsltMapper mapper) {
        this.mapper = mapper;
        resetRoot();
    }
    
   
    public abstract TreeNode loadRoot();
    
    public XsltMapper getMapper(){
        return this.mapper;
    }
    
    public Object getRoot() {
        return myRootNode;
    }
    
    public void resetRoot(){
        TreeNode newRootNode = loadRoot();
        if (newRootNode != null && 
            myRootNode!=null &&
            newRootNode.getDataObject() == myRootNode.getDataObject()){
            return;
        }
        myRootNode = newRootNode;
    }
    public int getIndexOfChild(Object parent, Object requiredChild) {
        assert parent instanceof TreeNode;
        assert requiredChild instanceof TreeNode;
        //
        List<TreeNode> children = ((TreeNode)parent).getChildren();
        
        return children.indexOf(requiredChild);
        
    }
    
    public Object getChild(Object parent, int index) {
        assert parent instanceof TreeNode;
        
        //
        List<TreeNode> children = ((TreeNode)parent).getChildren();
        return children.get(index);
    }
    
    public boolean isLeaf(Object node) {
        assert node instanceof TreeNode;
        //
        return ((TreeNode) node).getChildren().isEmpty();
    }
    
    public int getChildCount(Object parent) {
        assert parent instanceof TreeNode;
        //
        return ((TreeNode) parent).getChildren().size();
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing for a while
    }

    public void fireTreeChanged(TreePath tp) {
        TreeModelEvent event = new TreeModelEvent(this, tp);
        for(TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }


    
}
