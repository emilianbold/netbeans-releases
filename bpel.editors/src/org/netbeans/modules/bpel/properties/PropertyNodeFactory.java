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
package org.netbeans.modules.bpel.properties;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
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
import org.netbeans.modules.bpel.nodes.GlobalElementNode;
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
import org.netbeans.modules.bpel.nodes.FaultMessageNode;
import org.netbeans.modules.bpel.nodes.AssignNode;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.CatchAllNode;
import org.netbeans.modules.bpel.nodes.CatchNode;
import org.netbeans.modules.bpel.nodes.CompensateNode;
import org.netbeans.modules.bpel.nodes.CompensateScopeNode;
import org.netbeans.modules.bpel.nodes.CompensationHandlerNode;
import org.netbeans.modules.bpel.nodes.ElseIfNode;
import org.netbeans.modules.bpel.nodes.ElseNode;
import org.netbeans.modules.bpel.nodes.EmptyNode;
import org.netbeans.modules.bpel.nodes.ValidateNode;
import org.netbeans.modules.bpel.nodes.EventHandlersNode;
import org.netbeans.modules.bpel.nodes.FaultHandlersNode;
import org.netbeans.modules.bpel.nodes.FlowNode;
import org.netbeans.modules.bpel.nodes.ForEachNode;
import org.netbeans.modules.bpel.nodes.InvokeNode;
import org.netbeans.modules.bpel.nodes.OnAlarmNode;
import org.netbeans.modules.bpel.nodes.OnMessageNode;
import org.netbeans.modules.bpel.nodes.PartnerLinkNode;
import org.netbeans.modules.bpel.nodes.PickNode;
import org.netbeans.modules.bpel.nodes.ReceiveNode;
import org.netbeans.modules.bpel.nodes.RepeatUntilNode;
import org.netbeans.modules.bpel.nodes.ReplyNode;
import org.netbeans.modules.bpel.nodes.ScopeNode;
import org.netbeans.modules.bpel.nodes.SequenceNode;
import org.netbeans.modules.bpel.nodes.ExitNode;
import org.netbeans.modules.bpel.nodes.FaultNode;
import org.netbeans.modules.bpel.nodes.GlobalComplexTypeNode;
import org.netbeans.modules.bpel.nodes.GlobalSimpleTypeNode;
import org.netbeans.modules.bpel.nodes.IfNode;
import org.netbeans.modules.bpel.nodes.ImportContainerNode;
import org.netbeans.modules.bpel.nodes.ImportNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.ImportWsdlNode;
import org.netbeans.modules.bpel.nodes.OnAlarmEventNode;
import org.netbeans.modules.bpel.nodes.OnEventNode;
import org.netbeans.modules.bpel.nodes.ReThrowNode;
import org.netbeans.modules.bpel.nodes.RefResourceNode;
import org.netbeans.modules.bpel.nodes.TerminationHandlerNode;
import org.netbeans.modules.bpel.nodes.ThenNode;
import org.netbeans.modules.bpel.nodes.ThrowNode;
import org.netbeans.modules.bpel.nodes.VariableReferenceNode;
import org.netbeans.modules.bpel.nodes.WaitNode;
import org.netbeans.modules.bpel.nodes.WhileNode;
import org.netbeans.modules.soa.ui.nodes.ReflectionNodeFactory;

/**
 * Constructs BPEL Nodes 
 *
 * @author nk160297
 */
public class PropertyNodeFactory extends ReflectionNodeFactory<NodeType> {
    
    private static PropertyNodeFactory instance = new PropertyNodeFactory();
    
    public static PropertyNodeFactory getInstance() {
        return instance;
    }
    
    private PropertyNodeFactory() {
        super(80);
        //
        key2Class.put(NodeType.PROCESS, BpelProcessNode.class);
        //
        key2Class.put(NodeType.SCOPE, ScopeNode.class);
        key2Class.put(NodeType.SEQUENCE, SequenceNode.class);
        key2Class.put(NodeType.FLOW, FlowNode.class);
        key2Class.put(NodeType.WHILE, WhileNode.class);
        key2Class.put(NodeType.IF, IfNode.class);
        key2Class.put(NodeType.ELSE_IF, ElseIfNode.class);
        key2Class.put(NodeType.ELSE, ElseNode.class);
        key2Class.put(NodeType.THEN, ThenNode.class);
        //
        key2Class.put(NodeType.EMPTY, EmptyNode.class);
        key2Class.put(NodeType.VALIDATE, ValidateNode.class);
        key2Class.put(NodeType.INVOKE, InvokeNode.class);
        key2Class.put(NodeType.RECEIVE, ReceiveNode.class);
        key2Class.put(NodeType.REPLY, ReplyNode.class);
        key2Class.put(NodeType.PICK, PickNode.class);
        key2Class.put(NodeType.ASSIGN, AssignNode.class);
        key2Class.put(NodeType.WAIT, WaitNode.class);
        key2Class.put(NodeType.THROW, ThrowNode.class);
        key2Class.put(NodeType.RETHROW, ReThrowNode.class);
        key2Class.put(NodeType.EXIT, ExitNode.class);
        key2Class.put(NodeType.COMPENSATE, CompensateNode.class);
        key2Class.put(NodeType.COMPENSATE_SCOPE, CompensateScopeNode.class);
        key2Class.put(NodeType.CATCH, CatchNode.class);
        key2Class.put(NodeType.CATCH_ALL, CatchAllNode.class);
        key2Class.put(NodeType.TERMINATION_HANDLER,
                TerminationHandlerNode.class);
        key2Class.put(NodeType.COMPENSATION_HANDLER, 
                CompensationHandlerNode.class);
        key2Class.put(NodeType.EVENT_HANDLERS, 
                EventHandlersNode.class);
        key2Class.put(NodeType.FAULT_HANDLERS, 
                FaultHandlersNode.class);
        //
        key2Class.put(NodeType.PARTNER_LINK, PartnerLinkNode.class);
        key2Class.put(NodeType.PARTNER_LINK_TYPE, PartnerLinkTypeNode.class);
        key2Class.put(NodeType.PARTNER_ROLE, PartnerRoleNode.class);
        key2Class.put(NodeType.QUERY, QueryNode.class);
        //
        key2Class.put(NodeType.ALARM_HANDLER, OnAlarmNode.class);
        key2Class.put(NodeType.ALARM_EVENT_HANDLER, OnAlarmEventNode.class);
        key2Class.put(NodeType.MESSAGE_HANDLER, OnMessageNode.class);
        key2Class.put(NodeType.ON_EVENT, OnEventNode.class);
        //
        key2Class.put(NodeType.MESSAGE_TYPE, MessageTypeNode.class);
        key2Class.put(NodeType.WSDL_FILE, WsdlFileNode.class);
        key2Class.put(NodeType.FAULT_MESSAGE, FaultMessageNode.class);
        key2Class.put(NodeType.SCHEMA_FILE, SchemaFileNode.class);
        //
        key2Class.put(NodeType.VARIABLE_CONTAINER, VariableContainerNode.class);
        key2Class.put(NodeType.VARIABLE, VariableNode.class);
        key2Class.put(NodeType.VARIABLE_REFERENCE, VariableReferenceNode.class);
        key2Class.put(NodeType.CORRELATION, CorrelationNode.class);
        key2Class.put(NodeType.CORRELATION_P, CorrelationPNode.class);
        key2Class.put(NodeType.CORRELATION_SET_CONTAINER, CorrelationSetContainerNode.class);
        key2Class.put(NodeType.CORRELATION_SET, CorrelationSetNode.class);
        key2Class.put(NodeType.CORRELATION_PROPERTY, CorrelationPropertyNode.class);
        key2Class.put(NodeType.CORRELATION_PROPERTY_ALIAS, PropertyAliasNode.class);
        key2Class.put(NodeType.MESSAGE_PART, MessagePartNode.class);
        key2Class.put(NodeType.FROM_PART, FromPartNode.class);
        key2Class.put(NodeType.TO_PART, ToPartNode.class);
        key2Class.put(NodeType.FROM, FromNode.class);
        key2Class.put(NodeType.TO, ToNode.class);
        //
        key2Class.put(NodeType.COPY, CopyNode.class);
        key2Class.put(NodeType.IMPORT, ImportNode.class);
        key2Class.put(NodeType.IMPORT_SCHEMA, ImportSchemaNode.class);
        key2Class.put(NodeType.IMPORT_WSDL, ImportWsdlNode.class);
        key2Class.put(NodeType.IMPORT_CONTAINER, ImportContainerNode.class);
        //
        key2Class.put(NodeType.MESSAGE_EXCHANGE, MessageExchangeNode.class);
        key2Class.put(NodeType.MESSAGE_EXCHANGE_CONTAINER
                , MessageExchangeContainerNode.class);
        key2Class.put(NodeType.REPEAT_UNTIL, RepeatUntilNode.class);
        key2Class.put(NodeType.FOR_EACH, ForEachNode.class);
        //
        key2Class.put(NodeType.FAULT, FaultNode.class);
        //
        key2Class.put(NodeType.EMBEDDED_SCHEMA, EmbeddedSchemaNode.class);
        //
        key2Class.put(NodeType.DEFAULT_BPEL_ENTITY_NODE
                , DefaultBpelEntityNode.class);
        key2Class.put(NodeType.BOOLEAN_EXPR, BooleanExprNode.class);
        key2Class.put(NodeType.COMPLETION_CONDITION
                , CompletionConditionNode.class);
        //
        key2Class.put(NodeType.GLOBAL_COMPLEX_TYPE, GlobalComplexTypeNode.class);
        key2Class.put(NodeType.GLOBAL_SIMPLE_TYPE, GlobalSimpleTypeNode.class);
        key2Class.put(NodeType.GLOBAL_ELEMENT, GlobalElementNode.class);
        key2Class.put(NodeType.REFERENCED_RESOURCE, RefResourceNode.class);
        //
        // key2Class.put(NodeType., Node.class);
        //
    }
}
