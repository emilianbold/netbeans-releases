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
package org.netbeans.modules.bpel.mapper.model;

import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.xpath.mapper.model.ConnectionConstraint;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface BpelConnectionConstraints {
    
    public class PlConstraint implements ConnectionConstraint {

        public boolean canConnect(TreePath treePath, SourcePin source,
                                  TargetPin target, TreePath oldTreePath,
                                  Link oldLink) 
        {
            if (target instanceof VertexItem) {
                if (source instanceof TreeSourcePin) {
                    TreePath tPath = ((TreeSourcePin)source).getTreePath();
                    Object node = tPath.getLastPathComponent();
                    if (node instanceof MapperTreeNode) {
                        Object dataObj = ((MapperTreeNode)node).getDataObject();
                        if (dataObj instanceof PartnerLink || dataObj instanceof Roles) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    public class MVarConstraint implements ConnectionConstraint {

        public boolean canConnect(TreePath treePath, SourcePin source,
                                  TargetPin target, TreePath oldTreePath,
                                  Link oldLink) 
        {
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
        
        private boolean isMessageVariable(Object obj) {
            if (! (obj instanceof Variable)) {
                return false;
            }
            Variable var = (Variable)obj;
            return var.getMessageType() != null;
        }
        
        private boolean isFirstArgumentOfGetVariableProperty(
                VertexItem vertexItem) 
        {
            Vertex vertex = vertexItem.getVertex();
            
            return ((vertex instanceof Function)) 
                    && (vertex.getDataObject() == BpelXPathExtFunctionMetadata
                            .GET_VARIABLE_PROPERTY_METADATA)
                    && (vertex.getItemIndex(vertexItem) == 0);
        }
    }
}
