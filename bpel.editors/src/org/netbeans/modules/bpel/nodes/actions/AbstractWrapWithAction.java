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
