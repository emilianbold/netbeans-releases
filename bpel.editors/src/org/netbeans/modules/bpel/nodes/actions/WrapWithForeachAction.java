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
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WrapWithForeachAction extends AbstractWrapWithAction<ForEach> {
    
    public WrapWithForeachAction() {
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
//        return bpelEntities[0] instanceof Scope;
        return true;
    }

    protected String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_WrapWithForeachAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.WRAP_WITH_FOREACH;
    }

    public Class<? extends BpelEntity> getWrapEntityType() {
        return ForEach.class;
    }

    public Activity getWrapEntity(BpelContainer parent) {
        ForEach forEach = parent.getBpelModel().getBuilder().createForEach();
        forEach.setParallel(TBoolean.NO);
        
        try {
            forEach.setCounterName(forEach.getName() + "Counter"); // NOI18N
        } catch (VetoException ex) {
            // Somebody does not like this counter name 
            // or property is not supported or something else.
            // Anyway we unable to determine cause of problem, 
            // so will ignore this exception.
        }
        
        return  forEach;
    }
    
}
