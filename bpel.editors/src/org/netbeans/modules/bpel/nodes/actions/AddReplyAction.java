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
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.ScopeHolder;
import org.openide.ErrorManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class AddReplyAction extends AddPaletteActivityAction {
    private static final long serialVersionUID = 1L;

    public AddReplyAction() {
        super("webservice/reply"); // NOI18N
    }    
    
    protected String getBundleName() {
        return NodeType.REPLY.getDisplayName(); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_REPLY;
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        return super.enable(bpelEntities)
        && bpelEntities[0] instanceof BpelContainer;
    }

    protected ExtendableActivity getPaletteActivity(BpelModel model) {
        return model.getBuilder().createReply();
    }

}
