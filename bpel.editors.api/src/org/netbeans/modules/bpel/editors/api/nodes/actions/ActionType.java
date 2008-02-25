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
package org.netbeans.modules.bpel.editors.api.nodes.actions;

/**
 *
 * @author Vitaly Bychkov
 */
public enum ActionType {
    SEPARATOR,
    REMOVE,
    DELETE_BPEL_EXT_FROM_WSDL,
    DELETE_PROPERTY_ACTION,
    SHOW_POPERTY_EDITOR,
    ADD_CATCH,
    ADD_CATCH_ALL,
    ADD_COMPENSATION_HANDLER,
    ADD_TERMINATION_HANDLER,
    ADD_EVENT_HANDLERS,
    ADD_FAULT_HANDLERS,
    ADD_ON_ALARM,
    ADD_ON_MESSAGE,
    ADD_ON_EVENT,
    ADD_SCHEMA_IMPORT,
    ADD_WSDL_IMPORT,
    ADD_ELSE_IF,
    ADD_ELSE,
    INSERT_ELSE_IF_AFTER,
    INSERT_ELSE_IF_BEFORE,
    MOVE_ELSE_IF_RIGHT,
    MOVE_ELSE_IF_LEFT,
    SWAP_ELSE_IF_WITH_MAIN,
    ADD_VARIABLE,
    ADD_CORRELATION_SET,
    DEFINE_CORRELATION,
    
    ADD_COPY_RULE,
    ADD_PROPERTY,
    
    ADD_INVOKE,
    ADD_PARTNER_LINK,
    ADD_RECEIVE,
    ADD_REPLY,
    
    ADD_ASSIGN,
    ADD_EMPTY,
    ADD_WAIT,
    ADD_THROW,
    ADD_RETHROW,
    ADD_EXIT,
    
    ADD_IF,
    ADD_WHILE,
    ADD_FOREACH,
    ADD_REPEAT_UNTIL,
    ADD_PICK,
    ADD_FLOW,
    ADD_SEQUENCE,
    ADD_SCOPE,
    ADD_COMPENSATE,
    ADD_COMPENSATE_SCOPE,

    OPEN_IN_EDITOR,
    OPEN_PL_IN_EDITOR,
    GO_TO_SOURCE,
    GO_TO_CORRSETCONTAINER_SOURCE,
    GO_TO_VARCONTAINER_SOURCE, 
    GO_TO_TYPE_SOURCE,
    GO_TO_MSG_EX_CONTAINER_SOURCE,
    ADD_MESSAGE_EXCHANGE,
    ADD_NEWTYPES,
    TOGGLE_BREAKPOINT,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_COPY_UP,
    MOVE_COPY_DOWN,
    CYCLE_MEX,
    PROPERTIES,
    ADD_PROPERTY_TO_WSDL,
    ADD_PROPERTY_ALIAS_TO_WSDL,
    GO_TO,
    GO_TO_LOGGING,
    SHOW_BPEL_MAPPER,
    GO_TO_WSDL_SOURCE,
    FIND_USAGES,
    GO_TO_DIAGRAMM,
    WRAP,
    WRAP_WITH_SEQUENCE,
    WRAP_WITH_SCOPE,
    WRAP_WITH_FLOW,
    WRAP_WITH_WHILE,
    WRAP_WITH_FOREACH,
    WRAP_WITH_REPEAT_UNTIL,
            
    ADD_FROM_PALETTE,
    ADD_WEB_SERVICE_ACTIVITIES,
    ADD_BASIC_ACTIVITIES,
    ADD_STRUCTURED_ACTIVITIES,
    CHANGE_ORDER_ACTION;
}
