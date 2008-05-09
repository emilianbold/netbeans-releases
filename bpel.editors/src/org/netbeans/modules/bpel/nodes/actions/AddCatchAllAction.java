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
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.openide.util.NbBundle;

/**
 *
 * @author aa160298
 */
public class AddCatchAllAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;

    public AddCatchAllAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddCatchAllAction")); // NOI18N
    }    

    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddCatchAllAction"); // NOI18N
    }
    
    
    public ActionType getType() {
        return ActionType.ADD_CATCH_ALL;
    }
    
    
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelEntity bpelEntity = bpelEntities[0];
        
        CatchAll newCatchAll = bpelEntity.getBpelModel().getBuilder()
                .createCatchAll();
        
        if (bpelEntity instanceof Invoke) {
            Invoke invoke = (Invoke) bpelEntity;
            invoke.setCatchAll(newCatchAll);
        } if (bpelEntity instanceof FaultHandlers) {
            ((FaultHandlers) bpelEntity).setCatchAll(newCatchAll);
        }
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        if (bpelEntity instanceof Invoke) {
            Invoke invoke = (Invoke) bpelEntity;
            return (invoke.getCatchAll() == null);
        } else if (bpelEntity instanceof FaultHandlers) {
            return ((FaultHandlers) bpelEntity).getCatchAll() == null;
        }
        return false;
    }
}
