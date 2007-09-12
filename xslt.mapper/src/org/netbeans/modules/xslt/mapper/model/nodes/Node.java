/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
