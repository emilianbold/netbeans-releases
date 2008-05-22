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

package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;

import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.nodes.children.ActivityNodeChildren;
import org.netbeans.modules.bpel.nodes.children.ChildrenType;
import org.netbeans.modules.bpel.nodes.children.EventHandlersChildren;
import org.netbeans.modules.bpel.nodes.children.IfChildren;
import org.netbeans.modules.bpel.nodes.children.ImportContainerChildren;
import org.netbeans.modules.bpel.nodes.children.ImportWsdlChildren;
import org.netbeans.modules.bpel.nodes.children.ImportWsdlPropertyChildren;
import org.netbeans.modules.bpel.nodes.children.PickChildren;
import org.netbeans.modules.bpel.nodes.children.ProcessChildren;
import org.netbeans.modules.bpel.nodes.children.ThenChildren;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 05 April 2006
 */
public class NavigatorNodeFactory implements NodeFactory<NodeType> {
    
    private static NavigatorNodeFactory instance = new NavigatorNodeFactory();
    
    public static NavigatorNodeFactory getInstance() {
        return instance;
    }
    
    private NavigatorNodeFactory() {
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(BpelEntity reference, Lookup lookup) {
        return this.createNode(org.netbeans.modules.bpel.editors.api.EditorUtil.getBasicNodeType((BpelEntity)reference)
                , reference, ChildrenType.DEFAULT_CHILD, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object reference, Lookup lookup) {
        return this.createNode(nodeType, reference
                , ChildrenType.DEFAULT_CHILD, lookup);
    }
    
    /*
     * Navigator node factory doesn't support external childs
     * use PropertyNodeFactory for it
     * @throw UnsupportedOperationException
     */
    public Node createNode(NodeType nodeType, Object reference, 
            Children children, Lookup lookup) {
//        throw new UnsupportedOperationException();
        return Node.EMPTY;
    }
    
    public Node createNode(NodeType nodeType, Object reference, 
            ChildrenType childType, Lookup lookup) {
        
        // TODO r
        if (nodeType == null
                || nodeType.equals(NodeType.UNKNOWN_TYPE)) 
        {
//            System.out.println("nodetype is null or unknown");
            return null;
        }
        
        // TODO m || r 
        // set correct factory to lookup
        NodeFactory factory = (NodeFactory)lookup.lookup(NodeFactory.class);
        if (factory == null || !(factory.equals(this))) {
            lookup = new ExtendedLookup(lookup,this);
        }
        
        if (childType == null) {
            childType = ChildrenType.DEFAULT_CHILD;
        }
        
        Node newNode = null;
        switch (nodeType) {
            // Creation of the nodes with the specific Children should be here!
            case PROCESS:
                assert reference instanceof Process
                        : "reference should be Process type to create Process type Node"; // NOI18N
                newNode = PropertyNodeFactory.getInstance()
                .createNode(nodeType,reference, 
                        new ProcessChildren((Process)reference,lookup),lookup);
                break;
                
            case IF :
                assert reference instanceof If
                        : "reference should be If type to create If type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference, new IfChildren((If)reference,
                        lookup), lookup);
                break;
                
            case THEN :
                assert reference instanceof If
                        : "reference should be If type to create Then type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference, new ThenChildren((If)reference,
                        lookup), lookup);
                break;
                
            case EVENT_HANDLERS :
                assert reference instanceof EventHandlers
                        : "reference should be EventHandlers type to create EventHandlers type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,reference
                        , new EventHandlersChildren((EventHandlers)reference, lookup), lookup);
                break;

            case PICK :
                assert reference instanceof Pick
                        : "reference should be Pick type to create Pick type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,reference
                        , new PickChildren((Pick)reference, lookup), lookup);
                break;
                
            case SCOPE :
                assert reference instanceof Scope
                        : "reference should be Scope type to create Scope type Node"; // NOI18N
                if (childType.equals(ChildrenType.SCOPE_VARIABLES_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, new BaseScopeVariableChildren((Scope)reference,
                            lookup), lookup);
                } else if (childType.equals(ChildrenType.SCOPE_CORRELATIONS_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, new BaseScopeCorrelationChildren((Scope)reference,
                            lookup), lookup);
                } else if (childType.equals(ChildrenType.SCOPE_MESSAGE_EXCHANGES_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType
                            , reference
                            , new BaseScopeMessageExchangeChildren((Scope)reference
                            , lookup), lookup);
                } else {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, new BaseScopeChildren((Scope)reference,
                            lookup), lookup);
                }
                break;
                
            case VARIABLE_CONTAINER :
                assert reference instanceof BaseScope
                        : "reference should be BaseScope type to create VariableContainer type Node"; // NOI18N
                newNode = createVariableContainerNode((BaseScope)reference,lookup);
                break;
                
            case MESSAGE_EXCHANGE_CONTAINER :
                assert reference instanceof BaseScope
                        : "reference should be BaseScope type to create MessageExchangeContainer type Node"; // NOI18N
                newNode = createMessageExchangeContainerNode((BaseScope)reference,lookup);
                break;

            case IMPORT_CONTAINER :
                assert reference instanceof Process
                    : "reference should be Process type to create ImportContainer type Node"; // NOI18N
                Children wsdlChildren = new ImportContainerChildren((Process)reference, lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference
                    , wsdlChildren, lookup);
                break;

            case IMPORT_SCHEMA :
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference
                    , lookup);
                break;
                
            case IMPORT_WSDL :
                assert reference instanceof Import
                    : "reference should be Import type to create Import_Wsdl type Node"; // NOI18N
                Children propChildren = new ImportWsdlChildren((Import)reference, lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference
                        , propChildren , lookup);
                break;

            case PARTNER_LINK_TYPE :
                assert reference instanceof PartnerLinkType 
                    : "reference should be PartnerLinkType type to create PartnerLinkType type Node"; // NOI18N
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference
                    ,Children.LEAF , lookup);
                break;

            case CORRELATION_SET_CONTAINER :
                assert reference instanceof BaseScope
                        : "reference should be Scope type to create CorrelationSet type Node"; // NOI18N
                newNode = createCorrelationSetNode((BaseScope)reference,lookup);
                break;

            case CORRELATION_PROPERTY :
                assert reference instanceof CorrelationProperty 
                        : "reference should be CorrelationProperty type to creaet CorrelationProperty type Node"; // NOI18N
                CorrelationProperty corrProp = (CorrelationProperty)reference;
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, 
                    new ImportWsdlPropertyChildren(
                            corrProp.getModel()
                            ,corrProp
                            ,lookup
                        )
                    , lookup);
                break;
                
            case CORRELATION_PROPERTY_ALIAS :
                assert reference instanceof PropertyAlias 
                        : "reference should be Scope type to create CorrelationSet type Node"; // NOI18N
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, lookup);
                break;

            case VARIABLE :
                NodesTreeParams nodesTreeParams = new NodesTreeParams();
                nodesTreeParams.setLeafNodeClasses(VariableNode.class);
                Lookup contextLookup = new ExtendedLookup(lookup, nodesTreeParams);
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference
                    , contextLookup);
                break;
            case CORRELATION_SET :
                Children children = new CorrelationSetNode.MyChildren(
                        (CorrelationSet)reference,lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, children, lookup);
                break;
            case MESSAGE_EXCHANGE :
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, lookup);
                break;
            case ALARM_HANDLER :
            case ALARM_EVENT_HANDLER:
            case ASSIGN :
            case CATCH :
            case CATCH_ALL :
            case ELSE_IF :
            case ELSE :
            case FLOW :
            case FOR_EACH :
            case MESSAGE_HANDLER :
            case ON_EVENT :
            case INVOKE :
            case RECEIVE :
            case FAULT_HANDLERS :
            case COMPENSATION_HANDLER :
            case TERMINATION_HANDLER :
            case REPLY :
            case SEQUENCE :
            case WHILE :
            case REPEAT_UNTIL :
                ActivityNodeChildren activityChildren = new ActivityNodeChildren(
                        (BpelContainer)reference,lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, activityChildren, 
                        new ExtendedLookup(lookup, activityChildren.getIndex()));
                break;
                
            case COPY :
            case COMPENSATE :
            case COMPENSATE_SCOPE :
            case CORRELATION :
            case CORRELATION_P :
            case EMPTY :
            case EXIT :
            case FROM_PART:
            case TO_PART :
            case WAIT :
            case RETHROW :
            case THROW :
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,
                        reference, Children.LEAF, lookup);
                break;
            // TODO m (add Operation subnodes)
            case PARTNER_LINK :
                assert reference instanceof PartnerLink;
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,
                        reference, lookup);
                break;
                
////////            default:
////////                if (reference instanceof BpelContainer) {
////////                    newNode = delegate.createNode(nodeType,reference,null
////////                        ,new ActivityNodeChildren((BpelContainer)reference,lookup), lookup);
////////                } else {
////////                    newNode = delegate.createNode(nodeType, reference, null, lookup);;
////////                }
//////////                newNode = delegate.createNode(nodeType, reference, null, children, lookup);
////////                break;
        }
        //
        //remove assertion until all neccessary node types will be created
//        assert newNode != null : "The new node has to be created"; //NOI18N
        
        return newNode;
    }
    
    public Node getProcessNode(BpelModel bpelModel, Lookup lookup) {
        if (bpelModel == null || lookup == null) {
            return Node.EMPTY;
        }
        Process processEntity = bpelModel.getProcess();
        if (processEntity == null) {
            return Node.EMPTY;
        }
        
        Node processNode = createNode(NodeType.PROCESS,
                processEntity,
                lookup);
        
        return processNode == null ? Node.EMPTY : processNode;
    }
    
    private Node createVariableContainerNode(BaseScope baseScope, Lookup lookup) {
        Node node = Node.EMPTY;
        if (baseScope == null || lookup == null) {
            return node;
        }
        
        if (baseScope instanceof Process) {
            node = PropertyNodeFactory.getInstance().createNode(NodeType.VARIABLE_CONTAINER,
                    baseScope,
                    new BaseScopeVariableChildren(baseScope, lookup),
                    lookup);
            //        }
            
        } else {
            NodesTreeParams nodesTreeParams = new NodesTreeParams();
            nodesTreeParams.setLeafNodeClasses(VariableNode.class);
            Lookup contextLookup = new ExtendedLookup(lookup, nodesTreeParams);
            VariableContainer variableContainer = baseScope.getVariableContainer();
            // don't show variableContainer node in case variableContainer is null
            //        if (variableContainer != null) {
            node = PropertyNodeFactory.getInstance().createNode(NodeType.VARIABLE_CONTAINER,
                    baseScope,
                    contextLookup);
            //        }
        }
        
        return node;
    }
    
    private Node createMessageExchangeContainerNode(BaseScope baseScope, Lookup lookup) {
        Node node = Node.EMPTY;
        if (baseScope == null || lookup == null) {
            return node;
        }
        
        if (baseScope instanceof Process) {
            node = PropertyNodeFactory.getInstance().createNode(NodeType.MESSAGE_EXCHANGE_CONTAINER,
                    baseScope,
                    new BaseScopeMessageExchangeChildren(baseScope, lookup),
                    lookup);
            //        }
            
        } else {
            node = PropertyNodeFactory.getInstance().createNode(NodeType
                    .MESSAGE_EXCHANGE_CONTAINER, baseScope, lookup);
        }
        
        return node;
    }

    private Node createCorrelationSetNode(BaseScope baseScope, Lookup lookup) {
        Node node = Node.EMPTY;
        if (baseScope == null || lookup == null) {
            return node;
        }
        
        if (baseScope instanceof Process) {
            node = PropertyNodeFactory.getInstance().createNode(NodeType
                    .CORRELATION_SET_CONTAINER, baseScope, 
                    new BaseScopeCorrelationChildren(baseScope,lookup), lookup);
        } else {
            node = PropertyNodeFactory.getInstance().createNode(NodeType
                    .CORRELATION_SET_CONTAINER, baseScope, lookup);
        }
        
        return node;
    }
    
    private List<Node> createCorrelationNodes(CorrelationsHolder corrHolder, Lookup lookup) {
        List<Node> nodes = new ArrayList<Node>();
        if (corrHolder == null || lookup == null) {
            return nodes;
        }
        
        CorrelationContainer corrContainer = corrHolder.getCorrelationContainer();
        if (corrContainer == null) {
            return nodes;
        }
        Correlation[] corrs = corrContainer.getCorrelations();
        for (Correlation corr : corrs) {
            if (corr != null) {
                nodes.add(createNode(NodeType.CORRELATION,
                        corr,
                        Children.LEAF,
                        lookup
                        ));
            }
        }
        return nodes;
        
    }
}
