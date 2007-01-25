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

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;


public abstract class Node {
    
    
    
    
    private XsltMapper mapper;
    private IMapperNode mMapperNode;
    private Object dataObject;

    
    public Node(Object dataObject, XsltMapper mapper) {
        this.dataObject = dataObject;
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

    
    public List<Node> getNextNodes() {
        ArrayList<Node> result = new ArrayList<Node>();
        
        IMapperNode node = getMapperNode();
        
        for (Object n: node.getNextNodes()){
            result.add((Node) ((IMapperNode) n).getNodeObject());
        }
        return result;
    }
    
    public List<Node> getPreviousNodes(){
        ArrayList<Node> result = new ArrayList<Node>();
        
        IMapperNode node = getMapperNode();
        
        for (Object n: node.getPreviousNodes()){
            result.add((Node) ((IMapperNode) n).getNodeObject());
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
        return this.dataObject;
    }
    
//    public void setSataObject(Object dataObject) {
//        this.dataObject = dataObject;
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
