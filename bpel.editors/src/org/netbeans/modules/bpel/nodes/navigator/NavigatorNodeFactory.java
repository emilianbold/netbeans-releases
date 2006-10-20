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

package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;

import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.Exit;
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
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.editors.controls.NodesTreeParams;
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
public class NavigatorNodeFactory implements NodeFactory {
    
    private static NavigatorNodeFactory instance = new NavigatorNodeFactory();
    
    protected static Map<Class<? extends BpelEntity>, NodeType> BPELENTITY_NODETYPE_MAP;
    
    static {
        BPELENTITY_NODETYPE_MAP = new HashMap();
        BPELENTITY_NODETYPE_MAP.put(Process.class, NodeType.PROCESS);
        BPELENTITY_NODETYPE_MAP.put(FaultHandlers.class, NodeType.FAULT_HANDLERS);

        
        BPELENTITY_NODETYPE_MAP.put(Assign.class, NodeType.ASSIGN);
        BPELENTITY_NODETYPE_MAP.put(Compensate.class, NodeType.COMPENSATE);
        BPELENTITY_NODETYPE_MAP.put(CompensateScope.class, NodeType.COMPENSATE_SCOPE);
        BPELENTITY_NODETYPE_MAP.put(Copy.class, NodeType.COPY);
        BPELENTITY_NODETYPE_MAP.put(Empty.class, NodeType.EMPTY);
        BPELENTITY_NODETYPE_MAP.put(Flow.class, NodeType.FLOW);
        BPELENTITY_NODETYPE_MAP.put(Import.class, NodeType.IMPORT);
        BPELENTITY_NODETYPE_MAP.put(Invoke.class, NodeType.INVOKE);
        BPELENTITY_NODETYPE_MAP.put(Pick.class, NodeType.PICK);
        BPELENTITY_NODETYPE_MAP.put(OnMessage.class, NodeType.MESSAGE_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(OnEvent.class, NodeType.ON_EVENT);
        BPELENTITY_NODETYPE_MAP.put(OnAlarmEvent.class, NodeType.ALARM_EVENT_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(OnAlarmPick.class, NodeType.ALARM_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(Receive.class, NodeType.RECEIVE);
        BPELENTITY_NODETYPE_MAP.put(Reply.class, NodeType.REPLY);
        BPELENTITY_NODETYPE_MAP.put(Scope.class, NodeType.SCOPE);
        BPELENTITY_NODETYPE_MAP.put(Sequence.class, NodeType.SEQUENCE);
        BPELENTITY_NODETYPE_MAP.put(If.class, NodeType.IF);
        BPELENTITY_NODETYPE_MAP.put(Exit.class, NodeType.EXIT);
        BPELENTITY_NODETYPE_MAP.put(Throw.class, NodeType.THROW);
        BPELENTITY_NODETYPE_MAP.put(Wait.class, NodeType.WAIT);
        BPELENTITY_NODETYPE_MAP.put(While.class, NodeType.WHILE);
        BPELENTITY_NODETYPE_MAP.put(RepeatUntil.class, NodeType.REPEAT_UNTIL);
        BPELENTITY_NODETYPE_MAP.put(ForEach.class, NodeType.FOR_EACH);
        
        BPELENTITY_NODETYPE_MAP.put(Variable.class, NodeType.VARIABLE);
        BPELENTITY_NODETYPE_MAP.put(VariableContainer.class, NodeType.VARIABLE_CONTAINER);

        BPELENTITY_NODETYPE_MAP.put(MessageExchange.class
                , NodeType.MESSAGE_EXCHANGE);
        BPELENTITY_NODETYPE_MAP.put(MessageExchangeContainer.class
                , NodeType.MESSAGE_EXCHANGE_CONTAINER);

        BPELENTITY_NODETYPE_MAP.put(CorrelationSetContainer.class
                , NodeType.CORRELATION_SET_CONTAINER);
        BPELENTITY_NODETYPE_MAP.put(CorrelationSet.class, NodeType.CORRELATION_SET);
        BPELENTITY_NODETYPE_MAP.put(Correlation.class, NodeType.CORRELATION);
        BPELENTITY_NODETYPE_MAP.put(PatternedCorrelation.class, NodeType.CORRELATION_P);
        BPELENTITY_NODETYPE_MAP.put(Catch.class, NodeType.CATCH);
        BPELENTITY_NODETYPE_MAP.put(CompensatableActivityHolder.class, NodeType.CATCH_ALL);
        BPELENTITY_NODETYPE_MAP.put(CompensationHandler.class, NodeType.COMPENSATION_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(TerminationHandler.class, NodeType.TERMINATION_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(EventHandlers.class, NodeType.EVENT_HANDLERS);
        BPELENTITY_NODETYPE_MAP.put(PartnerLink.class, NodeType.PARTNER_LINK);
        BPELENTITY_NODETYPE_MAP.put(ElseIf.class, NodeType.ELSE_IF);
        BPELENTITY_NODETYPE_MAP.put(Else.class, NodeType.ELSE);
        BPELENTITY_NODETYPE_MAP.put(FromPart.class, NodeType.FROM_PART);
        BPELENTITY_NODETYPE_MAP.put(ToPart.class, NodeType.TO_PART);
        
        //
        BPELENTITY_NODETYPE_MAP.put(BooleanExpr.class, NodeType.BOOLEAN_EXPR);
        BPELENTITY_NODETYPE_MAP.put(From.class, NodeType.FROM);
        BPELENTITY_NODETYPE_MAP.put(To.class, NodeType.TO);
        BPELENTITY_NODETYPE_MAP.put(CompletionCondition.class
                , NodeType.COMPLETION_CONDITION);

    }
    
    public static NavigatorNodeFactory getInstance() {
        return instance;
    }
    
    private NavigatorNodeFactory() {
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(BpelEntity reference, Lookup lookup) {
        return this.createNode(getBasicNodeType((BpelEntity)reference)
                , reference, null, ChildrenType.DEFAULT_CHILD, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object reference, Lookup lookup) {
        return this.createNode(nodeType, reference
                , null, ChildrenType.DEFAULT_CHILD, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object reference,
            Object diagramRef, Lookup lookup) {
        return this.createNode(nodeType, reference, diagramRef
                , ChildrenType.DEFAULT_CHILD, lookup);
    }
    
    /*
     * Navigator node factory doesn't support external childs
     * use PropertyNodeFactory for it
     * @throw UnsupportedOperationException
     */
    public Node createNode(NodeType nodeType, Object reference
            , Object diagramRef, Children children, Lookup lookup) {
//        throw new UnsupportedOperationException();
        return Node.EMPTY;
    }
    
    public Node createNode(NodeType nodeType, Object reference
            , Object diagramRef, ChildrenType childType, Lookup lookup) {
        
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
                .createNode(nodeType,reference, null,
                        new ProcessChildren((Process)reference,lookup),lookup);
                break;
                
            case IF :
                assert reference instanceof If
                        : "reference should be If type to create If type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference, null, new IfChildren((If)reference,
                        lookup), lookup);
                break;
                
            case THEN :
                assert reference instanceof If
                        : "reference should be If type to create Then type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference, null, new ThenChildren((If)reference,
                        lookup), lookup);
                break;
                
            case EVENT_HANDLERS :
                assert reference instanceof EventHandlers
                        : "reference should be EventHandlers type to create EventHandlers type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                        , new EventHandlersChildren((EventHandlers)reference, lookup), lookup);
                break;

            case PICK :
                assert reference instanceof Pick
                        : "reference should be Pick type to create Pick type Node"; // NOI18N
                newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                        , new PickChildren((Pick)reference, lookup), lookup);
                break;
                
            case SCOPE :
                assert reference instanceof Scope
                        : "reference should be Scope type to create Scope type Node"; // NOI18N
                if (childType.equals(ChildrenType.SCOPE_VARIABLES_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, null , new BaseScopeVariableChildren((Scope)reference,
                            lookup), lookup);
                } else if (childType.equals(ChildrenType.SCOPE_CORRELATIONS_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, null , new BaseScopeCorrelationChildren((Scope)reference,
                            lookup), lookup);
                } else if (childType.equals(ChildrenType.SCOPE_MESSAGE_EXCHANGES_CHILD)) {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType
                            , reference, null 
                            , new BaseScopeMessageExchangeChildren((Scope)reference
                            , lookup), lookup);
                } else {
                    newNode =  PropertyNodeFactory.getInstance().createNode(nodeType,
                            reference, null , new BaseScopeChildren((Scope)reference,
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
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                    , wsdlChildren, lookup);
                break;

            case IMPORT_SCHEMA :
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                    , lookup);
                break;
                
            case IMPORT_WSDL :
                assert reference instanceof Import
                    : "reference should be Import type to create Import_Wsdl type Node"; // NOI18N
                Children propChildren = new ImportWsdlChildren((Import)reference, lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType,reference, null
                        , propChildren , lookup);
                break;

            case PARTNER_LINK_TYPE :
                assert reference instanceof PartnerLinkType 
                    : "reference should be PartnerLinkType type to create PartnerLinkType type Node"; // NOI18N
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
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
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                    , new ImportWsdlPropertyChildren(
                            corrProp.getModel()
                            ,corrProp
                            ,lookup
                        )
                    , lookup);
                break;
                
            case CORRELATION_PROPERTY_ALIAS :
                assert reference instanceof PropertyAlias 
                        : "reference should be Scope type to create CorrelationSet type Node"; // NOI18N
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                    , lookup);
                break;

            case VARIABLE :
                NodesTreeParams nodesTreeParams = new NodesTreeParams();
                nodesTreeParams.setLeafNodeClasses(VariableNode.class);
                Lookup contextLookup = new ExtendedLookup(lookup, nodesTreeParams);
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,reference, null
                    , contextLookup);
                break;
            case CORRELATION_SET :
                Children children = new CorrelationSetNode.MyChildren(
                        (CorrelationSet)reference,lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, null, children, lookup);
                break;
            case MESSAGE_EXCHANGE :
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, null, lookup);
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
                Children activityChildren = new ActivityNodeChildren(
                        (BpelContainer)reference,lookup);
                newNode = PropertyNodeFactory.getInstance().createNode(
                        nodeType, reference, null, activityChildren, lookup);
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
            case THROW :
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,
                        reference, null, Children.LEAF, lookup);
                break;
            // TODO m (add Operation subnodes)
            case PARTNER_LINK :
                assert reference instanceof PartnerLink;
                newNode = PropertyNodeFactory.getInstance().createNode(nodeType,
                        reference, null, null, lookup);
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
    
    public static NodeType getBasicNodeType(BpelEntity bpelEntity) {
        if (bpelEntity == null || bpelEntity.getElementType() == null) {
            return null;
        }
        NodeType entityType = BPELENTITY_NODETYPE_MAP.get(bpelEntity.getElementType());
        if (entityType == null /*|| !isBasicActivity(entityType)*/) {
            return NodeType.UNKNOWN_TYPE;
        }
        return  entityType;
    }
    
    private Node createVariableContainerNode(BaseScope baseScope, Lookup lookup) {
        Node node = Node.EMPTY;
        if (baseScope == null || lookup == null) {
            return node;
        }
        
        if (baseScope instanceof Process) {
            node = PropertyNodeFactory.getInstance().createNode(NodeType.VARIABLE_CONTAINER,
                    baseScope,
                    null,
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
                    null,
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
                    null,
                    new BaseScopeMessageExchangeChildren(baseScope, lookup),
                    lookup);
            //        }
            
        } else {
            node = PropertyNodeFactory.getInstance().createNode(NodeType
                    .MESSAGE_EXCHANGE_CONTAINER, baseScope, null, lookup);
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
                    .CORRELATION_SET_CONTAINER, baseScope, null
                    , new BaseScopeCorrelationChildren(baseScope,lookup), lookup);
        } else {
            node = PropertyNodeFactory.getInstance().createNode(NodeType
                    .CORRELATION_SET_CONTAINER, baseScope, null, lookup);
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
                        null,
                        Children.LEAF,
                        lookup
                        ));
            }
        }
        return nodes;
        
    }
}
