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
package org.netbeans.modules.bpel.properties;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.nodes.BooleanExprNode;
import org.netbeans.modules.bpel.nodes.CompletionConditionNode;
import org.netbeans.modules.bpel.nodes.CopyNode;
import org.netbeans.modules.bpel.nodes.CorrelationNode;
import org.netbeans.modules.bpel.nodes.CorrelationPNode;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetContainerNode;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.nodes.DefaultBpelEntityNode;
import org.netbeans.modules.bpel.nodes.EmbeddedSchemaNode;
import org.netbeans.modules.bpel.nodes.FromNode;
import org.netbeans.modules.bpel.nodes.FromPartNode;
import org.netbeans.modules.bpel.nodes.MessageExchangeContainerNode;
import org.netbeans.modules.bpel.nodes.MessageExchangeNode;
import org.netbeans.modules.bpel.nodes.MessagePartNode;
import org.netbeans.modules.bpel.nodes.MessageTypeNode;
import org.netbeans.modules.bpel.nodes.PartnerLinkTypeNode;
import org.netbeans.modules.bpel.nodes.PartnerRoleNode;
import org.netbeans.modules.bpel.nodes.PropertyAliasNode;
import org.netbeans.modules.bpel.nodes.QueryNode;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.ToNode;
import org.netbeans.modules.bpel.nodes.ToPartNode;
import org.netbeans.modules.bpel.nodes.VariableContainerNode;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.bpel.nodes.WsdlFileNode;
import org.netbeans.modules.bpel.nodes.AnnotationNode;
import org.netbeans.modules.bpel.nodes.AssignNode;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.CatchAllNode;
import org.netbeans.modules.bpel.nodes.CatchNode;
import org.netbeans.modules.bpel.nodes.CompensateNode;
import org.netbeans.modules.bpel.nodes.CompensateScopeNode;
import org.netbeans.modules.bpel.nodes.CompensationHandlerNode;
import org.netbeans.modules.bpel.nodes.DataObjectNode;
import org.netbeans.modules.bpel.nodes.ElseIfNode;
import org.netbeans.modules.bpel.nodes.ElseNode;
import org.netbeans.modules.bpel.nodes.EmbeddedSchemasFolderNode;
import org.netbeans.modules.bpel.nodes.EmptyNode;
import org.netbeans.modules.bpel.nodes.EventHandlersNode;
import org.netbeans.modules.bpel.nodes.FaultHandlersNode;
import org.netbeans.modules.bpel.nodes.FlowNode;
import org.netbeans.modules.bpel.nodes.ForEachNode;
import org.netbeans.modules.bpel.nodes.InvokeNode;
import org.netbeans.modules.bpel.nodes.OnAlarmNode;
import org.netbeans.modules.bpel.nodes.OnMessageNode;
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.nodes.PickNode;
import org.netbeans.modules.bpel.nodes.PoolNode;
import org.netbeans.modules.bpel.nodes.ReceiveNode;
import org.netbeans.modules.bpel.nodes.RepeatUntilNode;
import org.netbeans.modules.bpel.nodes.ReplyNode;
import org.netbeans.modules.bpel.nodes.ScopeNode;
import org.netbeans.modules.bpel.nodes.SequenceNode;
import org.netbeans.modules.bpel.nodes.ExitNode;
import org.netbeans.modules.bpel.nodes.FaultNode;
import org.netbeans.modules.bpel.nodes.IfNode;
import org.netbeans.modules.bpel.nodes.ImportContainerNode;
import org.netbeans.modules.bpel.nodes.ImportNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.ImportWsdlNode;
import org.netbeans.modules.bpel.nodes.OnAlarmEventNode;
import org.netbeans.modules.bpel.nodes.OnEventNode;
import org.netbeans.modules.bpel.nodes.StereotypeGroupNode;
import org.netbeans.modules.bpel.nodes.TerminationHandlerNode;
import org.netbeans.modules.bpel.nodes.ThenNode;
import org.netbeans.modules.bpel.nodes.ThrowNode;
import org.netbeans.modules.bpel.nodes.WaitNode;
import org.netbeans.modules.bpel.nodes.WhileNode;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * It's implied here that the Node has a constructors with at least one
 * from two possible parameters set. The cases are following:
 *  -- Assign reference, Lookup lookup
 *  -- Assign reference, Children children, Lookup lookup
 *
 * @author nk160297
 */
public class PropertyNodeFactory implements NodeFactory {
    
    private static PropertyNodeFactory instance = new PropertyNodeFactory();
    
    public static PropertyNodeFactory getInstance() {
        return instance;
    }
    
    private Map<NodeType, Class<? extends Node>> constant2Class;
    // private Map<Class<? extends Pattern>, NodeType> pattern2Class;
    
//    /**
//     * map of SchemaTreeViewNode by QName
//     */
//    private Map<QName,SchemaTreeViewNode> typeNodeMap =
//            new HashMap<QName,SchemaTreeViewNode>();
    
    /** Creates a new instance of PropertyNodeFactoryImpl. */
    private PropertyNodeFactory() {
        constant2Class = new HashMap<NodeType,  Class<? extends Node>>(30);
        // pattern2Class = new HashMap<Class<? extends Pattern>,  NodeType>(30);
        //
        constant2Class.put(NodeType.PROCESS, BpelProcessNode.class);
        //
        constant2Class.put(NodeType.SCOPE, ScopeNode.class);
        constant2Class.put(NodeType.SEQUENCE, SequenceNode.class);
        constant2Class.put(NodeType.FLOW, FlowNode.class);
        constant2Class.put(NodeType.WHILE, WhileNode.class);
        constant2Class.put(NodeType.IF, IfNode.class);
        constant2Class.put(NodeType.ELSE_IF, ElseIfNode.class);
        constant2Class.put(NodeType.ELSE, ElseNode.class);
        constant2Class.put(NodeType.THEN, ThenNode.class);
        //
        constant2Class.put(NodeType.EMPTY, EmptyNode.class);
        constant2Class.put(NodeType.INVOKE, InvokeNode.class);
        constant2Class.put(NodeType.RECEIVE, ReceiveNode.class);
        constant2Class.put(NodeType.REPLY, ReplyNode.class);
        constant2Class.put(NodeType.PICK, PickNode.class);
        constant2Class.put(NodeType.ASSIGN, AssignNode.class);
        constant2Class.put(NodeType.WAIT, WaitNode.class);
        constant2Class.put(NodeType.THROW, ThrowNode.class);
        constant2Class.put(NodeType.EXIT, ExitNode.class);
        constant2Class.put(NodeType.COMPENSATE, CompensateNode.class);
        constant2Class.put(NodeType.COMPENSATE_SCOPE, CompensateScopeNode.class);
        constant2Class.put(NodeType.CATCH, CatchNode.class);
        constant2Class.put(NodeType.CATCH_ALL, CatchAllNode.class);
        constant2Class.put(NodeType.TERMINATION_HANDLER,
                TerminationHandlerNode.class);
        constant2Class.put(NodeType.COMPENSATION_HANDLER, 
                CompensationHandlerNode.class);
        constant2Class.put(NodeType.EVENT_HANDLERS, 
                EventHandlersNode.class);
        constant2Class.put(NodeType.FAULT_HANDLERS, 
                FaultHandlersNode.class);
        //
        constant2Class.put(NodeType.POOL, PoolNode.class);
        constant2Class.put(NodeType.PARTNER_LINK, PartnerLinkNode.class);
        constant2Class.put(NodeType.PARTNER_LINK_TYPE, PartnerLinkTypeNode.class);
        constant2Class.put(NodeType.PARTNER_ROLE, PartnerRoleNode.class);
        constant2Class.put(NodeType.QUERY, QueryNode.class);
        constant2Class.put(NodeType.DATA_OBJECT, DataObjectNode.class);
        constant2Class.put(NodeType.ANNOTATION, AnnotationNode.class);
        //
        constant2Class.put(NodeType.ALARM_HANDLER, OnAlarmNode.class);
        constant2Class.put(NodeType.ALARM_EVENT_HANDLER, OnAlarmEventNode.class);
        constant2Class.put(NodeType.MESSAGE_HANDLER, OnMessageNode.class);
        constant2Class.put(NodeType.ON_EVENT, OnEventNode.class);
        //
        constant2Class.put(NodeType.MESSAGE_TYPE, MessageTypeNode.class);
        constant2Class.put(NodeType.WSDL_FILE, WsdlFileNode.class);
        constant2Class.put(NodeType.SCHEMA_FILE, SchemaFileNode.class);
        //
        constant2Class.put(NodeType.VARIABLE_CONTAINER, VariableContainerNode.class);
        constant2Class.put(NodeType.VARIABLE, VariableNode.class);
        constant2Class.put(NodeType.CORRELATION, CorrelationNode.class);
        constant2Class.put(NodeType.CORRELATION_P, CorrelationPNode.class);
        constant2Class.put(NodeType.CORRELATION_SET_CONTAINER, CorrelationSetContainerNode.class);
        constant2Class.put(NodeType.CORRELATION_SET, CorrelationSetNode.class);
        constant2Class.put(NodeType.CORRELATION_PROPERTY, CorrelationPropertyNode.class);
        constant2Class.put(NodeType.CORRELATION_PROPERTY_ALIAS, PropertyAliasNode.class);
        constant2Class.put(NodeType.MESSAGE_PART, MessagePartNode.class);
        constant2Class.put(NodeType.FROM_PART, FromPartNode.class);
        constant2Class.put(NodeType.TO_PART, ToPartNode.class);
        constant2Class.put(NodeType.FROM, FromNode.class);
        constant2Class.put(NodeType.TO, ToNode.class);
        //
        constant2Class.put(NodeType.COPY, CopyNode.class);
        constant2Class.put(NodeType.IMPORT, ImportNode.class);
        constant2Class.put(NodeType.IMPORT_SCHEMA, ImportSchemaNode.class);
        constant2Class.put(NodeType.IMPORT_WSDL, ImportWsdlNode.class);
        constant2Class.put(NodeType.IMPORT_CONTAINER, ImportContainerNode.class);
        //
        constant2Class.put(NodeType.MESSAGE_EXCHANGE, MessageExchangeNode.class);
        constant2Class.put(NodeType.MESSAGE_EXCHANGE_CONTAINER
                , MessageExchangeContainerNode.class);
        constant2Class.put(NodeType.REPEAT_UNTIL, RepeatUntilNode.class);
        constant2Class.put(NodeType.FOR_EACH, ForEachNode.class);
        //
        constant2Class.put(NodeType.STEREOTYPE_GROUP, StereotypeGroupNode.class);
        constant2Class.put(NodeType.FAULT, FaultNode.class);
        //
        constant2Class.put(NodeType.EMBEDDED_SCHEMAS_FOLDER, EmbeddedSchemasFolderNode.class);
        constant2Class.put(NodeType.EMBEDDED_SCHEMA, EmbeddedSchemaNode.class);
        //
        constant2Class.put(NodeType.DEFAULT_BPEL_ENTITY_NODE
                , DefaultBpelEntityNode.class);
        constant2Class.put(NodeType.BOOLEAN_EXPR, BooleanExprNode.class);
        constant2Class.put(NodeType.COMPLETION_CONDITION
                , CompletionConditionNode.class);
        //
        // constant2Class.put(NodeType., Node.class);
        //
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(NodeType nodeType, Object reference,
            Object diagramRef, Lookup lookup) {
        assert nodeType != null && reference != null;
        //
        return createNode(nodeType, reference, diagramRef, null, lookup);
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(NodeType nodeType, Object reference,
            Object diagramRef, Children children, Lookup lookup) {
        assert nodeType != null && reference != null;
        //
        Class<? extends Node> nodeClass = constant2Class.get(nodeType);
        //
        if (nodeClass == null) {
            return null;
        }
        //
        // Put the reference to diagram object into the lookup.
        if (diagramRef != null) {
            lookup = new ExtendedLookup(lookup, diagramRef);
        }
        //
        Node newNode = Node.EMPTY;
        try {
            //
            // Here the reflection is used intensively
            // Try to find constructors with 2 and 3 parameters at first
            Constructor<? extends Node> constr2Params = null;
            Constructor<? extends Node> constr3Params = null;
            //
            Class[] params2 = new Class[] {reference.getClass(), Lookup.class};
            Class[] params3 = new Class[] {reference.getClass(), Children.class, Lookup.class};
            //
            Constructor[] constArr = nodeClass.getConstructors();
            for (Constructor constr : constArr) {
                Class<?>[] paramClassArr = constr.getParameterTypes();
                if (constr2Params == null &&
                        isAssignable(params2, paramClassArr)) {
                    constr2Params = constr;
                }
                if (constr3Params == null &&
                        isAssignable(params3, paramClassArr)) {
                    constr3Params = constr;
                }
            }
            //
            if (children == null)  {
                if (constr2Params != null) {
                    // Call the constructor without children parameter
                    // This is the normal branch
                    newNode = constr2Params.newInstance(
                            new Object[] {reference, lookup});
                } else if (constr3Params != null) {
                    // Call the constructor with children parameter with
                    // the Children.LEAF value
                    newNode = constr3Params.newInstance(
                            new Object[] {reference, Children.LEAF, lookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            } else {
                if (constr3Params != null) {
                    // Call the constructor with children parameter
                    // This is the normal branch
                    newNode = constr3Params.newInstance(
                            new Object[] {reference, children, lookup});
                } else if (constr2Params != null) {
                    // Call the constructor without children parameter
                    newNode = constr2Params.newInstance(
                            new Object[] {reference, lookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        //
//        // Set Preferred action if it exists in the lookup
//        PreferredActionProvider actionProvider =
//                (PreferredActionProvider)lookup.
//                lookup(PreferredActionProvider.class);
//        if (actionProvider != null) {
//            NodeAction action = actionProvider.getPreferredAction();
//            if (action != null) {
//                if (newNode instanceof AbstractNode) {
//                    ((AbstractNode)newNode).setDefaultAction(action);
//                } else {
//                    // if the node doesn't support default action then wrap it
//                    newNode = new ActionProxyNode(newNode, action);
//                }
//            }
//        }
        //


        return newNode;
    }
    
    public Class<? extends Node> getNodeClass(NodeType nodeType) {
        return constant2Class.get(nodeType);
    }
    
    /**
     * Checks if the classes from source array are assignable to the
     * corresponding classes from target array.
     * Both arrays has to have the same quantity of elements.
     */
    private boolean isAssignable(Class<?>[] source, Class<?>[] target) {
        if (source == null || target == null || source.length != target.length) {
            return false;
        }
        //
        for (int index = 0; index < source.length; index++) {
            if (!target[index].isAssignableFrom(source[index])) {
                return false;
            }
        }
        //
        return true;
    }

}
