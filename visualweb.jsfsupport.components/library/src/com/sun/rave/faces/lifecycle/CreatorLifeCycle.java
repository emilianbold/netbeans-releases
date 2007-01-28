/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
