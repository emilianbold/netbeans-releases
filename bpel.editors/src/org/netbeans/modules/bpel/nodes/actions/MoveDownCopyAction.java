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
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.openide.actions.MoveUpAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class MoveDownCopyAction extends BpelNodeAction {
    
    private static final long serialVersionUID = 1L;

    protected String getBundleName() {
        return NbBundle.getMessage(MoveDownCopyAction.class, "CTL_MoveDownCopyAction");
    }

    public ActionType getType() {
        return ActionType.MOVE_COPY_DOWN;
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities) || !(bpelEntities[0] instanceof Copy)) {
            return false;
        }
        
        BpelContainer parent = ((Copy)bpelEntities[0]).getParent();
        if (parent == null || !(parent instanceof Assign)) {
            return false;
        }
        
        int curIndex = ((Assign)parent).indexOf(Copy.class,(Copy)bpelEntities[0]);
        if (curIndex >= 0 && curIndex < (((Assign)parent).sizeOfAssignChildren()-1)) {
            return true;
        }
        return false;
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        if (!enable(bpelEntities)) {
            return;
        }

        BpelContainer parent = ((Copy)bpelEntities[0]).getParent();
        if (parent == null || !(parent instanceof Assign)) {
            return;
        }
        
        int curIndex = ((Assign)parent).indexOf(Copy.class,(Copy)bpelEntities[0]);
        
        Copy cuttedCopy = (Copy)bpelEntities[0].cut();
        ((Assign)parent).insertAssignChild(cuttedCopy,curIndex+1);
    }
}
