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
package org.netbeans.modules.bpel.nodes.actions;

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
    ADD_EXIT,    
    
    ADD_IF,
    ADD_WHILE,
    ADD_FOREACH,
    ADD_REPEAT_UNTIL,
    ADD_PICK,    
    ADD_FLOW,    
    ADD_SEQUENCE,    
    ADD_SCOPE,    

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
