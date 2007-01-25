/*
 * SourceTreeModel.java
 *
 * Created on 19 Декабрь 2006 г., 19:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model;

import java.util.List;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;

/**
 *
 * @author nk160297
 */
public class XsltNodesTreeModel implements TreeModel {
    
    private TreeNode myRootNode;
    private EventListenerList listenerList = new EventListenerList();

    public XsltNodesTreeModel() {
    }
    
    public XsltNodesTreeModel(TreeNode rootNode) {
        myRootNode = rootNode;
    }
    
    public void setRootNode(TreeNode rootNode) {
        myRootNode = rootNode;
    }
    
    public Object getRoot() {
        return myRootNode;
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
        listenerList.remove(TreeModelListener.class, l);
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing for a while
    }
    
}
