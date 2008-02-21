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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 28 March 2006
 */
public class AddOnAlarmAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_FOR_VALUE = "'P0Y0M0DT0H0M0S'"; //NOI18N

    
    public AddOnAlarmAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddOnAlarmAction")); // NOI18N
    }    

    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddOnAlarmAction"); // NOI18N
    }
    
    
    public ActionType getType() {
        return ActionType.ADD_ON_ALARM;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelEntity bpelEntity = bpelEntities[0];
        BPELElementsBuilder builder = bpelEntity.getBpelModel().getBuilder();
        For newFor = builder.createFor();
        try {
            newFor.setContent(DEFAULT_FOR_VALUE);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        if (bpelEntity instanceof Pick) {
            OnAlarmPick newOnAlarmPick = builder.createOnAlarmPick();
            newOnAlarmPick.setTimeEvent(newFor);
            
            ((Pick) bpelEntity).addOnAlarm(newOnAlarmPick);
        } else if (bpelEntity instanceof EventHandlers) {
            OnAlarmEvent newOnAlarmEvent = builder.createOnAlarmEvent();
            newOnAlarmEvent.setTimeEvent(newFor);
            ((EventHandlers) bpelEntity).addOnAlarm(newOnAlarmEvent);
        }
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        return (bpelEntity instanceof Pick) 
                || (bpelEntity instanceof EventHandlers);
    }
}
