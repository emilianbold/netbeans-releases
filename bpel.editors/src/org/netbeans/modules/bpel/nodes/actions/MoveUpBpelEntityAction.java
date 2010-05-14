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
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.ScopeHolder;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class MoveUpBpelEntityAction extends BpelNodeAction {
    
    private static final long serialVersionUID = 1L;

    protected String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_MoveUpEntityAction");
    }

    public ActionType getType() {
        return ActionType.MOVE_UP;
    }

    private boolean isMoveable(BpelEntity entity, BpelContainer parent) {
        if (entity == null || parent == null) {
            return false;
        }
        boolean canMove = false;
        
        if (parent instanceof ScopeHolder) {
            // TODO m
            canMove = false;
        } else if (parent instanceof CompositeActivity) {
            int childIndex = EditorUtil.getChildIndex(entity, (CompositeActivity) parent);
            if (childIndex > 0 ) {
                canMove = true;
            } else if (childIndex == 0) {
                // todo m
                canMove = false;
//                BpelContainer dad = parent.getParent();
//                canMove = dad != null && ! (dad instanceof Process);
            }
            
        } else if (parent instanceof ActivityHolder 
                && !(parent instanceof Process)) {
            // TODO m
            canMove = false;
        }
        
        return canMove;
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        boolean enabled = super.enable(bpelEntities);
        if (enabled) {
            enabled = isMoveable(bpelEntities[0], bpelEntities[0].getParent());
        }
        return enabled;
    }

    private void moveEntity(BpelEntity movingEntity, BpelContainer parent) {
        if (movingEntity == null || parent == null) {
            return;
        }

        if (parent instanceof ScopeHolder) {
// TODO m
//            BpelContainer dad = parent.getParent();
//            if ( dad != null ) {
//                if (dad instanceof CompositeActivity ) {
//                    int scopeHolderIndex = getChildIndex(parent
//                                                , (CompositeActivity) dad);
//                    BpelEntity movingEntityCopy = movingEntity.cut();
//                    ((CompositeActivity)dad).insertActivity(
//                            (ExtendableActivity) movingEntityCopy
//                            , scopeHolderIndex);
//                }
//            }
            
        } else if (parent instanceof CompositeActivity) {
            int childIndex = EditorUtil.getChildIndex(movingEntity, (CompositeActivity) parent);
            // TODO m
            if (childIndex > 0) {
                BpelEntity movingEntityCopy = movingEntity.cut();
                ((CompositeActivity)parent).insertActivity(
                        (ExtendableActivity) movingEntityCopy,
                        childIndex-1);
            } else if (childIndex == 0) {
                // TODO m
//                System.out.println("childIndex == 0");
            }
        } else if (parent instanceof ActivityHolder 
                && !(parent instanceof Process)) {
        }
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        if (!enable(bpelEntities)) {
            return;
        }

        
        final BpelContainer parent = bpelEntities[0].getParent();
        final BpelEntity movingEntity = bpelEntities[0];
        if (parent == null) {
            return;
        }
//        System.out.println("moveUpEntity - try to perform action...; ");

        
        Callable performActionCall =  new Callable<Object> () {
            public Object call() throws Exception {
                moveEntity(movingEntity, parent);
//                parent.getBpelModel().sync();
                return null;
            }
        };
        
        try {
            parent.getBpelModel().invoke(performActionCall , null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        
//        int curIndex = parent.indexOf(Copy.class,(Copy)bpelEntities[0]);
        
//        BpelEntity cuttedCopy = bpelEntities[0].cut();
//        ((Assign)parent.insertAssignChild(cuttedCopy,curIndex-1);
    }
}
