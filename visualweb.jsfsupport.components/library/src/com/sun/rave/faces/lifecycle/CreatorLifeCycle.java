/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
