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
package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.soa.ui.nodes.ReflectionNodeFactory;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class NavigatorNodeFactory extends ReflectionNodeFactory<NodeType> {

    private static NavigatorNodeFactory INSTANCE = new NavigatorNodeFactory();
    
    public static NavigatorNodeFactory getInstance() {
        return INSTANCE;
    }
    
    private NavigatorNodeFactory() {
        super(6);
        //
        key2Class.put(NodeType.TRANSFORMMAP, TransformMapNode.class);
        key2Class.put(NodeType.SERVICE, ServiceNode.class);
        key2Class.put(NodeType.OPERATION, OperationNode.class);
        key2Class.put(NodeType.INVOKE, InvokeNode.class);
        key2Class.put(NodeType.TRANSFORM, TransformNode.class);
        key2Class.put(NodeType.PARAM, ParamNode.class);
    }

    public Node createNode(TMapComponent entity, Lookup lookup) {
        Node node = null;
        NodeType nodeType = NodeType.getNodeType(entity);
        if (nodeType != null) {
            node = createNode(nodeType, entity, lookup);
        }
        return node;
    }
    
    public Node getTransformMapNode(TMapModel model, Lookup lookup) {
        assert model != null && lookup != null;
        return createNode(NodeType.TRANSFORMMAP, model.getTransformMap(), lookup);
    }
    
    @Override
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        if (nodeType == null || ref == null || lookup == null) {
            return null;
        }
        

//        assert nodeType != null && ref != null && lookup != null;
        if (NodeType.UNKNOWN_TYPE.equals(nodeType)) {
            return createDefaultNode(ref, lookup);
        }
        
        Node node = null;
        switch (nodeType) {
            case TRANSFORMMAP:
//            assert ref instanceof TransformMap
//                    : "reference should be TransformMap type to create TransformMap type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new TransformMapChildren((TransformMap)ref,lookup),lookup);
//            break;
            case SERVICE:
//            assert ref instanceof Service
//                    : "reference should be Service type to create Service type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new ServiceChildren((Service)ref,lookup),lookup);
//            break;
            case OPERATION:
            case TRANSFORM:
//            assert ref instanceof Operation
//                    : "reference should be Operation type to create Operation type Node"; // NOI18N
//                node = super.createNode(nodeType,ref,
//                        new OperationChildren((Operation)ref,lookup),lookup);
                assert ref instanceof TMapComponent 
                        : "reference should be TMapComponent type to create TMapComponent type Node"; // NOI18N
                node = super.createNode(nodeType, ref, new TMapComponentNodeChildrenImpl((TMapComponent) ref, lookup), lookup);
                break;
            case PARAM:
                assert ref instanceof Param 
                        : "reference should be Param type to create Param type Node"; // NOI18N
                node = super.createNode(nodeType, ref, Children.LEAF, lookup);
                break;
            case INVOKE:
                assert ref instanceof Invoke 
                        : "reference should be Invoke type to create Invoke type Node"; // NOI18N
                node = super.createNode(nodeType, ref, Children.LEAF, lookup);
                break;
        }
        
        return node;
    }

    @Override
    public Node createNode(NodeType nodeType, Object ref, Children children, Lookup lookup) {
        return super.createNode(nodeType, ref, children, lookup);
    }
    
    // TODO add impl for default node
    private Node createDefaultNode(Object ref, Lookup lookup) {
        return null;
    } 
    
}
