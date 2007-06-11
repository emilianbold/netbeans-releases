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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.model.actions;

import org.netbeans.modules.vmd.api.model.presenters.actions.*;
import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.vmd.api.model.Presenter;

/**
 *
 * @author Karol Harezlak
 */
public abstract class ActionsPresenterForwarder extends ActionsPresenter {
    
    public static ActionsPresenterForwarder byReference(final String referencePropertyName, Class actionsToInherit) {
        return new ActionsPresenterForwarder(actionsToInherit) {
            protected DesignComponent getTargetComponent() {
                if (referencePropertyName == null)
                    throw new IllegalArgumentException("Argument referencePropertyName null"); //NOI18N
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
        Collection<? extends ActionsPresenter> presenters = getTargetComponent().getPresenters(ActionsPresenter.class);
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
