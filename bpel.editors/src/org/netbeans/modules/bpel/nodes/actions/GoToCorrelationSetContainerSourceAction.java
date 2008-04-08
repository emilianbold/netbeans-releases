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
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 *
 * @author Vitaly Bychkov
 * @version 21 April 2006
 */
public class GoToCorrelationSetContainerSourceAction extends GoToSourceAction {
    private static final long serialVersionUID = 1L;

    public ActionType getType() {
        return ActionType.GO_TO_CORRSETCONTAINER_SOURCE;
    }

    
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelEntity corrSetContainer = ((BaseScope)bpelEntities[0])
        .getCorrelationSetContainer();
        super.performAction(corrSetContainer == null
            ? bpelEntities
            : new BpelEntity[] {corrSetContainer});
    }
    
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        return super.enable(bpelEntities)
        && bpelEntities[0] instanceof BaseScope;
    }
    
    
    public boolean isChangeAction() {
        return false;
    }    
}
