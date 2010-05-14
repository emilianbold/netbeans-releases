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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class FaultHandlersNode extends BpelNode<FaultHandlers> {

    public FaultHandlersNode(FaultHandlers faultHandlers, Lookup lookup) {
        super(faultHandlers, lookup);
    }

    public FaultHandlersNode(FaultHandlers faultHandlers, Children children,
            Lookup lookup) 
    {
        super(faultHandlers, children, lookup);
    }
    
    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
//                ActionType.GO_TO_SOURCE,
//                ActionType.GO_TO_DIAGRAMM,
                ActionType.GO_TO,
                ActionType.SEPARATOR,
                ActionType.REMOVE
            };
        }

        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.REMOVE
        };
    }
    
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_CATCH,
            ActionType.ADD_CATCH_ALL
        };
    }

    public NodeType getNodeType() {
        return NodeType.FAULT_HANDLERS;
    }
     public String getHelpId() {
        return "orch_elements_scope_add_fault_handler"; //NOI18N
    }
}
