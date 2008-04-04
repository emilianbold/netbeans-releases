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

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;

/**
 * Constants supporting the Process Execution View.
 * 
 * @author Kirill Sorokin
 */
public class Constants {
    // View ////////////////////////////////////////////////////////////////////
    public static final String VIEW_NAME = "ProcessExecutionView"; // NOI18N
    
    // Icons ///////////////////////////////////////////////////////////////////
    public static final String PEM_ICON_BASE = 
            "org/netbeans/modules/" + // NOI18N
            "bpel/debugger/ui/resources/image/execution/"; // NOI18N
    
    public static final String PSM_ICON_BASE = 
            PEM_ICON_BASE + "grayed-out/"; // NOI18N
    
    public static final String ICON_UNKNOWN = 
            "DEFAULT_BPEL_ENTITY_NODE"; // NOI18N
    
    public static final String ROOT_ICON = 
            PSM_ICON_BASE + ICON_UNKNOWN;
            
    // Colors //////////////////////////////////////////////////////////////////
    public static Color NOT_YET_EXECUTED = 
            Color.GRAY;
    
    public static Color STARTED_COLOR = 
            new Color(0, 128, 0);
    
    public static Color COMPLETED_COLOR = 
            null;
    
    // Conversion //////////////////////////////////////////////////////////////
    public static Map<String, String> myIconByTag = 
            new HashMap<String, String>();
    
    public static Map<String, String> myLabelByTag = 
            new HashMap<String, String>();
    
    // Utility methods /////////////////////////////////////////////////////////
    public static Color getColor(
            final PemEntity pemEntity) {
        
        switch (pemEntity.getState()) {
            case STARTED :
                return STARTED_COLOR;
                
            case COMPLETED :
                return COMPLETED_COLOR;
                
            case UNKNOWN :
            default :
                return STARTED_COLOR;
        }
    }
    
    public static boolean isBold(
            final PemEntity pemEntity) {
        
        final ProcessExecutionModel.Branch currentBranch = 
                pemEntity.getModel().getCurrentBranch();
        
        if (currentBranch == null) {
            return false;
        }
        
        final String currentBranchId = currentBranch.getId();
        final String pemBranchId = pemEntity.getBranchId();
        final boolean isInCurrentBranch = pemBranchId != null ?
                pemBranchId.equals(currentBranchId) : false;
        
        if (isInCurrentBranch && 
                (pemEntity.getState() == PemEntity.State.STARTED)) {
            return true;
        }
        
        return false;
    }
    
    public static String makeLabel(String tag, String name) {
        String label;
        if (name != null && !name.equals("")) {
            label = name;
        } else {
            label = myLabelByTag.get(tag);
            
            if (label == null || label.equals("")) {
                label = tag;
            }
        }
        return label;
    }
    
    // Init ////////////////////////////////////////////////////////////////////
    static {
        myIconByTag.put("assign", "ASSIGN");
        myIconByTag.put("catch", "CATCH");
        myIconByTag.put("catchAll", "CATCH_ALL");
        myIconByTag.put("compensate", "COMPENSATE");
        myIconByTag.put("compensateScope", "COMPENSATE_SCOPE");
        myIconByTag.put("compensationHandler", "COMPENSATION_HANDLER");
        //myIconByTag.put("condition", "CONDITION");
        myIconByTag.put("copy", "COPY"); 
        myIconByTag.put("else", "ELSE");
        myIconByTag.put("elseif", "ELSE_IF");
        myIconByTag.put("empty", "EMPTY");
        myIconByTag.put("eventHandlers", "EVENT_HANDLERS");
        myIconByTag.put("exit", "EXIT");
        myIconByTag.put("faultHandlers", "FAULT_HANDLERS");
        myIconByTag.put("flow", "FLOW");
        myIconByTag.put("forEach", "FOR_EACH");
        myIconByTag.put("if", "IF");
        myIconByTag.put("invoke", "INVOKE");
        myIconByTag.put("onAlarm", "ALARM_EVENT_HANDLER");
        myIconByTag.put("onMessage", "MESSAGE_HANDLER");
        myIconByTag.put("onEvent", "ON_EVENT");
        myIconByTag.put("pick", "PICK");
        myIconByTag.put("process", "PROCESS");
        myIconByTag.put("receive", "RECEIVE");
        myIconByTag.put("repeatUntil", "REPEAT_UNTIL");
        myIconByTag.put("reply", "REPLY");
        myIconByTag.put("scope", "SCOPE");
        myIconByTag.put("sequence", "SEQUENCE");
        myIconByTag.put("terminationHandler", "TERMINATION_HANDLER");
        myIconByTag.put("then", "THEN");
        myIconByTag.put("throw", "THROW");
        myIconByTag.put("wait", "WAIT");
        myIconByTag.put("while", "WHILE");
        
        myLabelByTag.put("assign", "Assign");
        myLabelByTag.put("catch", "Catch");
        myLabelByTag.put("condition", "Condition");
        myLabelByTag.put("catchAll", "Catch All");
        myLabelByTag.put("compensate", "Compensate");
        myLabelByTag.put("compensateScope", "Compensate Scope");
        myLabelByTag.put("compensationHandler", "Compensation Handler");
        myLabelByTag.put("copy", "Copy");
        myLabelByTag.put("else", "Else");
        myLabelByTag.put("elseif", "Else If");
        myLabelByTag.put("empty", "Empty");
        myLabelByTag.put("eventHandlers", "Event Handlers");
        myLabelByTag.put("exit", "Exit");
        myLabelByTag.put("faultHandlers", "Fault Handlers");
        myLabelByTag.put("flow", "Flow");
        myLabelByTag.put("forEach", "For Each");
        myLabelByTag.put("if", "If");
        myLabelByTag.put("invoke", "Invoke");
        myLabelByTag.put("onAlarm", "Alarm Handler");
        myLabelByTag.put("onMessage", "Message Handler");
        myLabelByTag.put("onEvent", "On Event");
        myLabelByTag.put("pick", "Pick");
        myLabelByTag.put("process", "Process");
        myLabelByTag.put("receive", "Receive");
        myLabelByTag.put("repeatUntil", "Repeat Until");
        myLabelByTag.put("reply", "Reply");
        myLabelByTag.put("scope", "Scope");
        myLabelByTag.put("sequence", "Sequence");
        myLabelByTag.put("terminationHandler", "Termination Handler");
        myLabelByTag.put("then", "Then");
        myLabelByTag.put("throw", "Throw");
        myLabelByTag.put("wait", "Wait");
        myLabelByTag.put("while", "While");
    }
}
