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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.model.actions;

import org.netbeans.modules.vmd.api.model.presenters.actions.*;
import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public abstract class ActionsPresenterForwarder extends ActionsPresenter {
    
    public static ActionsPresenterForwarder byReference(final String referencePropertyName, Class actionsToInherit) {
        return new ActionsPresenterForwarder(actionsToInherit) {
            protected DesignComponent getTargetComponent() {
                if (referencePropertyName == null)
                    throw new IllegalArgumentException("Argument referencePropertyName is null"); //NOI18N
                return getComponent().readProperty(referencePropertyName).getComponent();
            }
        };
    }
    
    public static ActionsPresenterForwarder byParent(Class actionsToInherit) {
        return new ActionsPresenterForwarder(actionsToInherit) {
            protected DesignComponent getTargetComponent() {
                return getComponent().getParentComponent();
            }
        };
    }
    
    private Class actionToInherit;
    private WeakReference<DesignComponent> targetComponent;
    
    private ActionsPresenterForwarder(Class actionToInherit) {
        this.actionToInherit = actionToInherit;
    }
    
    public List<Action> getActions() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                targetComponent = new WeakReference<DesignComponent>(getTargetComponent());
            }
        });
        
        if (targetComponent == null)
            throw new IllegalStateException( getComponent() +" has no target component"); //NOI18N
        
        //Action[] allActions = ActionsSupport.createActionsArray(targetComponent.get());
        
        List<Action> filteredActions = null;
        // Fix for IZ#143558 - NullPointerException at org.netbeans.modules.vmd.model.actions.ActionsPresenterForwarder.getActions
        if ( targetComponent.get() == null ){
            return Collections.emptyList();
        }
        Collection<? extends ActionsPresenter> presenters = targetComponent.get().getPresenters(ActionsPresenter.class);
        for (ActionsPresenter presenter : presenters ) {
            List<Action> pa = presenter.getActions();
            for (Action action : pa) {
                if (action == null)
                    continue;
                
                if (actionToInherit == action.getClass()) {
                    if (filteredActions == null)
                        filteredActions = new ArrayList<Action> ();
                    filteredActions.add(action);
                }
            }
        }
        return filteredActions;
    }
    
    public DesignComponent getRelatedComponent() {
        return getComponent();
    }
    
    public Integer getOrder() {
        getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                targetComponent = new WeakReference<DesignComponent>(getTargetComponent());
            }
        });
        
        if (targetComponent == null)
            throw new IllegalStateException( getComponent() +" has no target component"); //NOI18N
        
        Collection<? extends ActionsPresenter> presenters = targetComponent.get().getPresenters(ActionsPresenter.class);
        
        for (ActionsPresenter presenter : presenters ) {
            List<Action> pa = presenter.getActions();
            for (Action action : pa) {
                if (action == null)
                    continue;
                
                if (actionToInherit == action.getClass()) {
                    return presenter.getOrder();
                }
            }
        }
        
        return null;
    }
    
    protected abstract DesignComponent getTargetComponent();
    
}
