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

package org.netbeans.modules.xslt.mapper.model.nodes;

import java.awt.Color;
import java.awt.Image;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author radval
 *
 */
public abstract class TreeNode extends Node {
    
    private TreeNode parent;
    private List<TreeNode> children;
    
    private List<TreeNode> oldChildren;
    
    protected abstract List<TreeNode> loadChildren();
    
    public abstract AXIComponent getType();
    
    public TreeNode(Component component, XsltMapper mapper){
        super(component, mapper);
    }
    /**
     * Gets the parent node for this node.
     *
     * @return the parent node, or null if none
     */
    public TreeNode getParent(){
        return this.parent;
    }
    
    
    public void setParent(TreeNode parent){
        this.parent = parent;
    }
    

    
    public List<TreeNode> getChildren(){
        if (children == null){
            children = loadChildren();
            
            /**
             * The most important part in whole project
             * when tree is being reloaded, try to reuse all nodes as much as possible
             */
            if (oldChildren != null){
                for (int pos1 = 0; pos1 < children.size(); pos1++){
                    TreeNode new_child = children.get(pos1);
                    
                    for (int pos2 = 0; pos2 < oldChildren.size(); pos2++){
                        TreeNode old_child = oldChildren.get(pos2);
                        if (old_child == null){
                            continue;
                        }
                        
                        if (new_child.getDataObject() == old_child.getDataObject()){
                            children.set(pos1, old_child);
                            oldChildren.set(pos2, null);
                            break;
                        }
                    }
                    
                    
                }
                for (TreeNode oldChild : oldChildren){
                    
                    if (oldChild != null){
                        oldChild.removeFromTree();
                    }
                }
                oldChildren = null;
            }
            
        }
        return children;
    }
    
    public void reload(){
        this.oldChildren = this.children;
        this.children = null;
        if ( this.oldChildren  != null){
            for (TreeNode n: this.oldChildren){
                n.reload();
            }
        }
        
    }
    
    
    
    /**
     * Whether this tree node is mappable.
     * A tree node that is not mappable cannot be linked to or from.
     */
    public abstract boolean isMappable();
    
    /**
     * The current highlight color, i.e. from a search.
     */
    public Color getHighlightColor(){
        return new Color(0);
    }
    
    /**
     * This Icon can be used to represent the node in a tree view.
     */
    public Image getIcon() {
        return null;
    }
    
    public String getName() {
        return toString();
    }
    
    /**
     * Does quite the same like the getName() method does. 
     * It is intended to provide different HTML text depend on the parameter value.
     */
    public String getName(boolean selected) {
        return getName();
    }
    
    public static TreeNode getNode(IMapperTreeNode node){
        return (TreeNode) node.getPath().getLastPathComponent();
    }
    public IMapperNode getMapperNode(){
        
        return getMapper().getMapperNode(this);
    }
    public void setMapperNode(IMapperNode node){
        
        assert false:"Linking with tree nodes is not allowed. Tree nodes created/removed dynamically";
    }
    
    public static TreePath getTreePath(TreeNode node){
        if (node.getParent() != null){
            return getTreePath(node.getParent()).pathByAddingChild(node);
        }
        return new TreePath(node);
    }
    
    public IMapperNode getOutputNode() {
        return this.getMapperNode();
    }
    
    public IMapperNode getInputNode(Node node) {
        return this.getMapperNode();
    }
    
    public JPopupMenu constructPopupMenu() {
        return null;
    }
    
    public boolean isSourceViewNode() {
        return (((IMapperTreeNode)getMapperNode()) != null) && ((IMapperTreeNode)getMapperNode()).isSourceTreeNode();
    }
    
    private void removeFromTree(){
        
        getMapper().getBuilder().destroyDiagramRecursive(this);
        
        if (oldChildren != null){
            for (TreeNode child: oldChildren){
                child.removeFromTree();
            }
        }
        parent = null;
        children = null;
        oldChildren = null;
    }
}
