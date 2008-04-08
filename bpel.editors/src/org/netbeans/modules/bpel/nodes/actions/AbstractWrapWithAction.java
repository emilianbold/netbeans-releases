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
import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.ScopeHolder;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class AbstractWrapWithAction<Wrapper extends Activity> extends BpelNodeAction implements WrapWithAction<Wrapper> {
    
    public AbstractWrapWithAction() {
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        if (! super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity entity = bpelEntities[0];
        BpelContainer parent = entity.getParent();
        return parent != null /* TODO r && isContainerSupportChildType(parent)*/;
    }

    public boolean isContainerSupportChildType(BpelContainer container) {
        if (container instanceof ScopeHolder) {
            return false;
        }
        return true;
    }
    
//    protected String getBundleName() {
//        return NbBundle.getMessage(BpelNodeAction.class, "CTL_WrapWithSequenceAction"); // NOI18N    
//    }

//    public ActionType getType() {
//        return ActionType.WRAP_WITH_SEQUENCE;
//    }

    private void addActivity(BpelContainer parent, ExtendableActivity child, int index) {
        if (parent instanceof ScopeHolder && child instanceof Scope) {
            addActivity((ScopeHolder)parent, (Scope)child);
        } else if (parent instanceof CompositeActivity) {
            addActivity((CompositeActivity)parent, child, index);
        } else if (parent instanceof ActivityHolder) {
            addActivity((ActivityHolder)parent, child);
        }
    }

    private void addActivity(ScopeHolder parent, Scope child) {
        parent.setScope(child);
    }

    private void addActivity(CompositeActivity parent, ExtendableActivity child, int index) {
        assert parent != null && child != null;

        if (index < 0) {
            return;
        }
        
        if (index >= parent.sizeOfActivities()) {
            parent.addActivity(child);
        } else {
            parent.insertActivity(child, index);
        }
    }
    
    private void addActivity(ActivityHolder parent, ExtendableActivity child) {
        parent.setActivity(child);
    }
    
    private int getActivityIndex(CompositeActivity parent, BpelEntity child) {
        assert parent != null && child != null;
        int activityIndex = -1;
        for (int i = 0; i < parent.sizeOfActivities(); i++) {
            if (child.equals(parent.getActivity(i))) {
                activityIndex = i;
                break;
            }
        }
        return activityIndex;
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        if (!enable(bpelEntities)) {
            return;
        }
        final BpelEntity entity = bpelEntities[0];
        
        Callable performActionCall =  new Callable<Object> () {
            public Object call() throws Exception {
                BpelContainer container = entity.getParent();
                BpelModel bpelModel = container.getBpelModel();
                // get entity index for composite Activity
                int activityIndex = -1;
                if (container instanceof CompositeActivity) {
                    activityIndex = getActivityIndex((CompositeActivity) container, entity);
                }
                
                Activity newWrapper = getWrapEntity(container);
                
                
                BpelEntity wrappedEntity = entity.cut();
                if (wrappedEntity instanceof ExtendableActivity) {
                    if (newWrapper instanceof ScopeHolder 
                            && !(entity instanceof Scope)) 
                    {
                            Scope scope = bpelModel.getBuilder().createScope();
                            scope.setActivity((ExtendableActivity) wrappedEntity);
                            wrappedEntity = scope;
                    }

                    if (container instanceof ScopeHolder && ! (newWrapper instanceof Scope)) {
                        if (bpelModel != null) {
                            addActivity(newWrapper, (ExtendableActivity)wrappedEntity, 0);
                            
                            wrappedEntity = newWrapper;
                            newWrapper = bpelModel.getBuilder().createScope();
                        }
                    }

                    
                    
                    addActivity(newWrapper, (ExtendableActivity)wrappedEntity, 0);
                    
                    addActivity(container, newWrapper, activityIndex);
                }
//                container.getBpelModel().sync();
                return null;
            }
        };
        
        try {
            entity.getBpelModel().invoke(performActionCall , null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

}
