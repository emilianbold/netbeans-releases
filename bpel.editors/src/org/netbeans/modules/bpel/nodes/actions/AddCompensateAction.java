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

import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandler;
import org.netbeans.modules.bpel.model.api.TerminationHandler;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class AddCompensateAction extends AddPaletteActivityAction {
    private static final long serialVersionUID = 1L;

    public AddCompensateAction() {
        super("basic/compensate"); // NOI18N
    }    
    
    protected String getBundleName() {
        return NodeType.COMPENSATE.getDisplayName(); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_COMPENSATE;
    }
    
    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        return super.enable(bpelEntities) && isSupportedParent(bpelEntities[0]);
    }

    private boolean isSupportedParent(BpelEntity bpelEntity) {
        if (bpelEntity == null || bpelEntity instanceof Process) {
            return false;
        }
        
        if (bpelEntity instanceof CompensationHandler
                        || bpelEntity instanceof TerminationHandler
                        || bpelEntity instanceof FaultHandler) 
        {
            return true;
        }
        
        bpelEntity = bpelEntity.getParent();
        return isSupportedParent(bpelEntity);
        
    }
    
    protected ExtendableActivity getPaletteActivity(BpelModel model) {
        return model.getBuilder().createCompensate();
    }

}
