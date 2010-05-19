/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperTreeNode;
import org.netbeans.modules.xml.wsdl.model.Message;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface ConnectionConstraint {
    boolean canConnect(TreePath treePath, SourcePin source, 
            TargetPin target, TreePath oldTreePath, Link oldLink);
    
    class Access {
        private Access() {
        }
        
        public static ConnectionConstraint getGeneralConstraint(
                WlmMapperModel model)
        {
            return new GeneralConstraint(model);
        }

        public static ConnectionConstraint getMVarConstraint() {
            return new MVarConstraint();
        }
    }

    class GeneralConstraint implements ConnectionConstraint {

        private WlmMapperModel myModel;
        
        private GeneralConstraint(WlmMapperModel model) {
            myModel = model;
        }

        public boolean canConnect(TreePath treePath, SourcePin source,
                                  TargetPin target, TreePath oldTreePath,
                                  Link oldLink) 
        {
            assert myModel != null;
            MapperSwingTreeModel rModel = myModel.getRightTreeModel();
            MapperSwingTreeModel lModel = myModel.getLeftTreeModel();
            if (rModel == null || lModel == null) {
                return false;
            }
            

        if (oldTreePath != null && !oldTreePath.equals(treePath)) {
            // Reconnect
            // link to another graph is not allowed for a while
            if (!(oldLink.getSource() instanceof TreeSourcePin)) {
                return false;
            }
        }
        
        boolean result = true;
       
        if (target instanceof Graph) {
            if (!rModel.isConnectable(treePath)) {
                result = false;
            }
            //
            if (((Graph) target).hasOutgoingLinks()) {
                // The target tree node already has a connected link
                result =((Graph) target).getOutgoingLink() == oldLink ;
            }
       
        }
        SourcePin oldSource = null;
        TargetPin oldTarget = null;
         if (oldLink != null) {
            oldSource = oldLink.getSource();
            oldTarget = oldLink.getTarget();
            oldLink.setSource(null);
            oldLink.setTarget(null);
        }
        //
        if (source instanceof TreeSourcePin) {
            TreePath sourceTreePath = ((TreeSourcePin) source).getTreePath();
            if (!lModel.isConnectable(sourceTreePath)) {
                result = false;
            }
        }
        //
        // Check there is only one outgoing link
        if (source instanceof Vertex) {
            Link outgoingLink = ((Vertex) source).getOutgoingLink();
            if (outgoingLink != null) {
                result = false;
            }
        }
        
        //
        if (target instanceof VertexItem) {
            // Check the item doesn't have incoming link yet
            Link ingoingLink = ((VertexItem) target).getIngoingLink();
            if (ingoingLink != null) {
                result = false;
            }
            //
            // Check connection 2 vertexes 
            if (source instanceof Vertex) {
                //
                // Trying connect the vertex to itself isn't allowed
                Vertex targetVertex = ((VertexItem) target).getVertex();
                if (targetVertex == source) {
                    result = false;
                }
                // Check cyclic dependences
                if (WlmMapperUtils.areVertexDependent((Vertex) source, targetVertex)) {
                    result = false;
                }
            }
        }
        //
        if (oldLink != null) {
            oldLink.setSource(oldSource);
            oldLink.setTarget(oldTarget);
        }
        return result;
        }
    }
    
    class MVarConstraint implements ConnectionConstraint {

        public boolean canConnect(TreePath treePath, SourcePin source,
                                  TargetPin target, TreePath oldTreePath,
                                  Link oldLink) 
        {
            // fix for issue 178488
            // new
            if (source instanceof TreeSourcePin) {
                TreePath tPath = ((TreeSourcePin)source).getTreePath();
                Object node = tPath.getLastPathComponent();
                if (node instanceof MapperTreeNode) {
                    Object dataObj = ((MapperTreeNode)node).getDataObject();
                    if (isVariable(dataObj)) {
                        return false;
                    }
                }
            }

            if (target instanceof Graph) {
                Object node = treePath.getLastPathComponent();
                if (node instanceof MapperTreeNode) {
                    Object dataObj = ((MapperTreeNode)node).getDataObject();
                    if (isVariable(dataObj)) {
                        return false;
                    }
                }
            }

            // old
            if (target instanceof VertexItem) {
                if (source instanceof TreeSourcePin) {
                    if (isFirstArgumentOfGetVariableProperty(
                            (VertexItem) target)) 
                    {
                        return true;
                    }
                    TreePath tPath = ((TreeSourcePin)source).getTreePath();
                    Object node = tPath.getLastPathComponent();
                    if (node instanceof MapperTreeNode) {
                        Object dataObj = ((MapperTreeNode)node).getDataObject();
                        if (isMessageVariable(dataObj)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private boolean isVariable(Object obj) {
            return (obj instanceof VariableDeclaration);
        }
        
        private boolean isMessageVariable(Object obj) {
            if (! (obj instanceof VariableDeclaration)) {
                return false;
            }
            VariableDeclaration var = (VariableDeclaration)obj;
            return var.getTypeClass() == Message.class;
        }
        
        private boolean isFirstArgumentOfGetVariableProperty(
                VertexItem vertexItem) 
        {
            Vertex vertex = vertexItem.getVertex();
            
            return ((vertex instanceof Function)) 
//                    && (vertex.getDataObject() == BpelXPathExtFunctionMetadata
//                            .GET_VARIABLE_PROPERTY_METADATA)
                    && (vertex.getItemIndex(vertexItem) == 0);
        }
    }
}
