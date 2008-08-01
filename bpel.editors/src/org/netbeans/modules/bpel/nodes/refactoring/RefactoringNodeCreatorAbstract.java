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
package org.netbeans.modules.bpel.nodes.refactoring;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public abstract class RefactoringNodeCreatorAbstract
        implements RefactoringNodeCreator {

    public RefactoringNodeCreatorAbstract() {
    }
    
    protected Node getInternalNode(UsageNodeType nodeType, Object reference) {
        if (!isSupported(nodeType)) {
            return null;
        }
        
        PropertyNodeFactory propertyNodeFactory = PropertyNodeFactory
                .getInstance();
        Node node = null;
        NodeType bpelNodeType = null;
        if (reference instanceof BPELExtensibilityComponent) {
            bpelNodeType = getBpelExtensionElementNodeType(
                        (BPELExtensibilityComponent)reference
                    );
        } else if (reference instanceof BpelEntity) {
            bpelNodeType = 
                    org.netbeans.modules.bpel.editors.api.EditorUtil.getBasicNodeType((BpelEntity) reference);
            if (bpelNodeType == NodeType.VARIABLE_CONTAINER 
                    || bpelNodeType == NodeType.VARIABLE_CONTAINER 
                    || bpelNodeType == NodeType.CORRELATION_SET_CONTAINER
                    || bpelNodeType == NodeType.MESSAGE_EXCHANGE_CONTAINER ) 
            {
                reference = ((BpelEntity)reference).getParent();
            }
            
            if (bpelNodeType == null || bpelNodeType == NodeType.UNKNOWN_TYPE) {
                bpelNodeType = NodeType.DEFAULT_BPEL_ENTITY_NODE;
            }
        }
//        
//        if (! (reference instanceof BpelEntity)) {
//            if (reference instanceof BPELExtensibilityComponent) {
//                NodeType bpelExtNodeType = 
//                        getBpelExtensionElementNodeType((BPELExtensibilityComponent)reference);
//                if (bpelExtNodeType != null) {
//                    node = propertyNodeFactory.createNode(
//                            bpelExtNodeType,reference,null,
//                            Children.LEAF, Lookup.getDefault());
//                }
//            }
//            
//            return node;
//        }
//        
//        org.netbeans.modules.bpel.design.nodes.NodeType bpelNodeType =
//                NavigatorNodeFactory.getBasicNodeType((BpelEntity) reference);
        
        if (bpelNodeType != null) {
            
            node = propertyNodeFactory.createNode(
                    bpelNodeType, reference, 
                    Children.LEAF, Lookup.getDefault());
//            if (bpelNodeType == org.netbeans.modules.bpel.design.nodes
//                                     .NodeType.UNKNOWN_TYPE) 
//            {
//                node = propertyNodeFactory.createNode(
//                        bpelNodeType,reference,null,
//                        Children.LEAF, Lookup.getDefault());
//            } else {
//            node = propertyNodeFactory.createNode(
//                    bpelNodeType,reference,null,
//                    Children.LEAF, Lookup.getDefault());
//            }
        }
        return node;
    }
    
    private NodeType getBpelExtensionElementNodeType(BPELExtensibilityComponent component) {
        if (component instanceof PropertyAlias) {
            return NodeType.CORRELATION_PROPERTY_ALIAS;
        }

        if (component instanceof PartnerLinkType) {
            return NodeType.PARTNER_LINK_TYPE;
        }
        
        if (component instanceof CorrelationProperty) {
            return NodeType.CORRELATION_PROPERTY;
        }
        
        if (component instanceof Role) {
            return NodeType.PARTNER_ROLE;
        }
        
        if (component instanceof Query) {
            return NodeType.QUERY;
        }
        
        return NodeType.UNKNOWN_TYPE;
    }
}
