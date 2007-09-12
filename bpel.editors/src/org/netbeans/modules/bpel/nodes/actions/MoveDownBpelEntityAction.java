/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes.actions;

import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.editors.api.utils.Util;
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
public class MoveDownBpelEntityAction extends BpelNodeAction {
    
    private static final long serialVersionUID = 1L;

    protected String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, "CTL_MoveDownEntityAction");
    }

    public ActionType getType() {
        return ActionType.MOVE_DOWN;
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
            int childIndex = Util.getChildIndex(entity, (CompositeActivity) parent);
            int maxActivitiesIndex = ((CompositeActivity) parent).sizeOfActivities()-1;
            if (childIndex >= 0 && childIndex < maxActivitiesIndex) {
                canMove = true;
            } else if (childIndex == maxActivitiesIndex) {
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
    
//    private int getChildIndex(BpelEntity child, CompositeActivity parent) {
//        assert child != null && parent != null;
//        int childIndex = -1;
//        for (int i = 0; i < parent.sizeOfActivities(); i++) {
//            if (child.equals(parent.getActivity(i))) {
//                childIndex = i;
//                break;
//            }
//        }
//        return childIndex;
//    }
//    
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
// TODO a
        } else if (parent instanceof CompositeActivity) {
            int maxActivitiesIndex = ((CompositeActivity) parent).sizeOfActivities()-1;
            int childIndex = Util.getChildIndex(movingEntity, (CompositeActivity) parent);
            // TODO m
            if (childIndex >= 0 && childIndex < maxActivitiesIndex) {
                BpelEntity movingEntityCopy = movingEntity.cut();
                ((CompositeActivity)parent).insertActivity(
                        (ExtendableActivity) movingEntityCopy,
                        childIndex+1);
            } else if (childIndex == maxActivitiesIndex) {
                // TODO m
//                System.out.println("childIndex == activitiesLength");
            }
        } else if (parent instanceof ActivityHolder 
                && !(parent instanceof Process)) {
// TODO a
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
        
        if (!enable(bpelEntities)) {
            return;
        }
        
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
    }
}
