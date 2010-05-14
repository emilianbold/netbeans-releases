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
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 28 March 2006
 *
 */
public class AddEventHandlersAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;

    
    public AddEventHandlersAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddEventHandlersAction")); // NOI18N
    }

    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddEventHandlersAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_EVENT_HANDLERS;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        EventHandlers newElem = bpelEntities[0].getBpelModel()
        .getBuilder().createEventHandlers();
        ((BaseScope)bpelEntities[0]).setEventHandlers(newElem);
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        if (bpelEntity instanceof BaseScope) {
            return ((BaseScope) bpelEntity).getEventHandlers() == null;
        }
        
        return false;
    }
}
