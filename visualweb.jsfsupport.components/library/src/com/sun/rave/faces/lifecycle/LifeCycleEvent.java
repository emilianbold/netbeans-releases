/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.lifecycle;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

public class LifeCycleEvent {
    private FacesContext facesContext;
    private PhaseId phaseId;

    public LifeCycleEvent(FacesContext facesContext, PhaseId phaseId) {
        this.facesContext = facesContext;
        this.phaseId      = phaseId;
    }

    public FacesContext getFacesContext() {
        return facesContext;
    }

    public PhaseId getPhaseId() {
        return phaseId;
    }
}
