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
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;


public class CanvasNode extends Node {
    
    protected List<Node> mInputList = new ArrayList<Node>();
    protected List<Node> mOutputList = new ArrayList<Node>();
    
    
    public CanvasNode(XPathExpression dataObject,  XsltMapper mapper) {
        super((Object)dataObject, mapper);
    }
    
    public IMapperNode getOutputNode() {
        IMethoidNode node = (IMethoidNode) getMapperNode();
        if (node != null) {
            return (IMapperNode) node.getOutputFieldNodes().get(0);
        }
        return null;
    }
    
    public IMapperNode getInputNode(Node node_from) {
        
        IMethoidNode node = (IMethoidNode) getMapperNode();
        if (node != null) {
            for(Object fn: node.getInputFieldNodes()){
                if (fn instanceof IFieldNode){
                    Object data = ((IFieldNode) fn).getNodeObject();
                    if (data == node_from){
                        return (IFieldNode) fn;
                    }
                }
            }
        }
        return null;
    }
    
    
    
    public void addInput(Node input) {
        this.mInputList.add(input);
    }
    
    public void addOutput(Node output) {
        this.mOutputList.add(output);
    }
    /**
     * List of input Node which are
     * connected to this operator
     * @return
     */
    public List<Node> getInputs() {
        return this.mInputList;
    }
    
    /**
     * List of output Node where this node
     * is connected.
     * @return
     */
    public List<Node> getOutputs() {
        return this.mOutputList;
    }
    
    public void removeInput(Node input) {
        if(this.mInputList.contains(input)) {
            this.mInputList.remove(input);
        }
    }
    
    public void removeOutput(Node output) {
        if(this.mOutputList.contains(output)) {
            this.mOutputList.remove(output);
        }
    }
    
    protected void visitInputs(NodeVisitor visitor) {
        for(Node n: getInputs()) {
            n.accept(visitor);
        }
    }
    
    protected void visitOutputs(NodeVisitor visitor) {
        
        for(Node n: getOutputs()) {
            n.accept(visitor);
        }
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
