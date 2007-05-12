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

package com.sun.rave.faces.lifecycle;

import java.beans.Beans;
import java.util.Hashtable;
import java.util.Iterator;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.LifecycleFactory;

public class CreatorLifeCycle {
    private Hashtable listeners; 

    public CreatorLifeCycle() {
        if (!Beans.isDesignTime()) {
            listeners = new Hashtable();
            LifecycleFactory lifecycleFactory = (LifecycleFactory)
                FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY); 
            String lifecycleId =
                FacesContext.getCurrentInstance().getExternalContext().getInitParameter(
                "javax.faces.lifecycle.LIFECYCLE_ID"); //NOI18N
            if (lifecycleId == null) {
                lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
            }
            lifecycleFactory.getLifecycle(lifecycleId).addPhaseListener(new PhaseListener() {
                public void afterPhase(PhaseEvent event) {
                    for (Iterator i = listeners.keySet().iterator(); i.hasNext();) {
                        LifeCycleListener listener = (LifeCycleListener)i.next();
                        UIViewRoot viewRoot = (UIViewRoot)listeners.get(listener);
                        if (viewRoot == FacesContext.getCurrentInstance().getViewRoot()) {
                            LifeCycleEvent lifeCycleEvent = new LifeCycleEvent(
                                event.getFacesContext(), event.getPhaseId());
                            if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
                                listener.postRestoreView(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
                                listener.postApplyRequestValues(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
                                listener.postProcessValidations(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
                                listener.postUpdateModelValues(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
                                listener.postInvokeApplication(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
                                listener.postRenderResponse(lifeCycleEvent);
                            }
                        }
                    }
                }
                public void beforePhase(PhaseEvent event) {
                    for (Iterator i = listeners.keySet().iterator(); i.hasNext();) {
                        LifeCycleListener listener = (LifeCycleListener)i.next();
                        UIViewRoot viewRoot = (UIViewRoot)listeners.get(listener);
                        if (viewRoot == FacesContext.getCurrentInstance().getViewRoot()) {
                            LifeCycleEvent lifeCycleEvent = new LifeCycleEvent(
                                event.getFacesContext(), event.getPhaseId());
                            if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
                                listener.preRestoreView(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {
                                listener.preApplyRequestValues(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.PROCESS_VALIDATIONS) {
                                listener.preProcessValidations(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
                                listener.preUpdateModelValues(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
                                listener.preInvokeApplication(lifeCycleEvent);
                            } else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
                                listener.preRenderResponse(lifeCycleEvent);
                            }
                        }
                    }
                }
                public PhaseId getPhaseId() {
                    return PhaseId.ANY_PHASE;
                }
            });
        }
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (!Beans.isDesignTime()) {
            listeners.put(listener, FacesContext.getCurrentInstance().getViewRoot());
        }
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        if (!Beans.isDesignTime()) {
            listeners.remove(listener);
        }
    }
}
