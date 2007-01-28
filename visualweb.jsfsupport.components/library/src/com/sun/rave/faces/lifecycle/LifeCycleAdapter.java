/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.lifecycle;

public class LifeCycleAdapter implements LifeCycleListener {
    public void preRestoreView(LifeCycleEvent event) {}
    public void postRestoreView(LifeCycleEvent event) {}
    public void preApplyRequestValues(LifeCycleEvent event) {}
    public void postApplyRequestValues(LifeCycleEvent event) {}
    public void preProcessValidations(LifeCycleEvent event) {}
    public void postProcessValidations(LifeCycleEvent event) {}
    public void preUpdateModelValues(LifeCycleEvent event) {}
    public void postUpdateModelValues(LifeCycleEvent event) {}
    public void preInvokeApplication(LifeCycleEvent event) {}
    public void postInvokeApplication(LifeCycleEvent event) {}
    public void preRenderResponse(LifeCycleEvent event) {}
    public void postRenderResponse(LifeCycleEvent event) {}
}
