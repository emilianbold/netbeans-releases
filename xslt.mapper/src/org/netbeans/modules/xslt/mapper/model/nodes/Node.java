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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;

import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;


public abstract class Node {
    
    
    
    
    private XsltMapper mapper;
    private IMapperNode mMapperNode;
    protected Object myDataObject;
    
    
    public Node(Object dataObject, XsltMapper mapper) {
        this.myDataObject = dataObject;
        this.mapper = mapper;
    }
    
    
    
    /**
     * Get the GUI Node for this Node.
     * @return
     */
    public IMapperNode getMapperNode() {
        return this.mMapperNode;
    }
    
    public void setMapperNode(IMapperNode node) {
        this.mMapperNode = node;
    }
    
    public XsltMapper getMapper(){
        return mapper;
    }
    
    
    public List<Node> getNextNodes(){
        IMapperNode node = getMapperNode();
        
        return (node != null ) ?
            buildNodeList( node.getNextNodes()) :
            new ArrayList<Node>();
    }
    /** Builds the list of all upstream nodes
     * if some pin is not connected, returns null at that position
     **/ 
    public List<Node> getAllPreviousNodes(){
        List<IMapperNode> mapperNodes = new ArrayList<IMapperNode>();
        IMapperNode node = getMapperNode();
        if (node instanceof IMethoidNode){
            IMapperNode n = ((IMethoidNode) node).getFirstNode();
            while(n != null){
                if (n instanceof IFieldNode){
                    IFieldNode field = (IFieldNode) n;
                    if(field.isInput()){
                        List prevs = field.getPreviousNodes();
                        if (prevs.isEmpty()){
                            mapperNodes.add(null);
                        } else {
                            mapperNodes.add((IMapperNode) prevs.get(0));
                        }
                        
                    }
                    
                }
                n =  ((IMethoidNode) node).getNextNode((IMapperNode) n);
            }
            
            
        }
        return buildNodeList(mapperNodes);
    }
    
    // TODO A documentation is strongly required for this method.
    // Sometimes it returns not empty list with a null element.
    // It looks strange, and it comes to many NPEs.
    public List<Node> getPreviousNodes(){
        IMapperNode node = getMapperNode();
        
        return (node != null ) ?
            buildNodeList( node.getPreviousNodes()) :
            new ArrayList<Node>();
        
    }
    
    private List<Node> buildNodeList(List mapperNodes){
        ArrayList<Node> result = new ArrayList<Node>();
        if (mapperNodes != null){
            for (Object n: mapperNodes){
                if (n instanceof IFieldNode){
                    result.add((Node) ((IMapperNode) n).getGroupNode().getNodeObject());
                } else if (n instanceof IMapperTreeNode){
                    result.add(TreeNode.getNode((IMapperTreeNode) n));
                } else {
                    result.add(null);
                }
                
            }
        }
        return result;
    }
    
    public abstract IMapperNode getOutputNode();
    public abstract IMapperNode getInputNode(Node node_from);
    
    
    
    /**
     * Returns element of domain-specific object model, associated with this Node
     * @returns AXIComponent, XSLComponent or XPAthexpression
     **/
    
    public Object getDataObject() {
        return this.myDataObject;
    }
    
//    public void setSataObject(Object myDataObject) {
//        this.myDataObject = myDataObject;
//    }
    
    /**
     * Gets the name of the node.
     *
     * @return String The name of the node
     */
    
    public String getName() {
        return "UNKNOWN NODE";  // NOI18N
    }
    
    public abstract void accept(NodeVisitor visitor);
}
