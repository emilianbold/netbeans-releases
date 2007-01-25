/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.xslt.mapper.model.nodes;

import java.awt.Color;
import java.awt.Image;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionType;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author radval
 *
 */
public abstract class TreeNode extends Node {
    
    private TreeNode parent;
    private List<TreeNode> children;
    
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
        }
        return children;
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
 
    public IMapperNode getMapperNode(){
        if (super.getMapperNode() == null) {
            super.setMapperNode(getMapper().getMapperNode(this));
        }
        return super.getMapperNode();
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
    
    public ActionDescriptor<ActionType>[] getActionDescriptorArr() {
        return null;
    }
    
    public boolean isSourceViewNode() {
        return ((IMapperTreeNode)getMapperNode()).isSourceTreeNode();
    }
}
