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
import javax.swing.KeyStroke;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.Process;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 23 March 2006
 *
 */
public class DeleteAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    private static final String DELETE_KEYSTROKE = "DELETE"; // NOI18N
    
    public DeleteAction() {
        super();
        putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(DELETE_KEYSTROKE));
    }
    
    protected String getBundleName() {
        return NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"); // NOI18N
    }

    /**
     * Used just to declare public scope instead protected
     */
    @Override
    public boolean enable(Node[] nodes) {
        return super.enable(nodes);
    }

    /**
     * Used just to declare public scope instead protected
     */
    @Override
    public void performAction(Node[] nodes) {
        super.performAction(nodes);
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        if (!enable(bpelEntities)) {
            return;
        }
        for (BpelEntity entity : bpelEntities) {
            assert entity != null;
            BpelContainer parent = entity.getParent();
            assert parent != null;
            parent.remove(entity);
            
            if ((parent instanceof Else) || (entity instanceof Correlation)) {
                BpelContainer grandParent = parent.getParent();
                if (grandParent != null){
                    grandParent.remove(parent);
                }
            }
        }
    }
    
    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        return super.enable(bpelEntities)
        && !(bpelEntities[0] instanceof Process);
    }
    
    public ActionType getType() {
        return ActionType.REMOVE;
    }
}
