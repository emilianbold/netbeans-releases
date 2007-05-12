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

public interface LifeCycleListener {
    public void preRestoreView(LifeCycleEvent event);
    public void postRestoreView(LifeCycleEvent event);
    public void preApplyRequestValues(LifeCycleEvent event);
    public void postApplyRequestValues(LifeCycleEvent event);
    public void preProcessValidations(LifeCycleEvent event);
    public void postProcessValidations(LifeCycleEvent event);
    public void preUpdateModelValues(LifeCycleEvent event);
    public void postUpdateModelValues(LifeCycleEvent event);
    public void preInvokeApplication(LifeCycleEvent event);
    public void postInvokeApplication(LifeCycleEvent event);
    public void preRenderResponse(LifeCycleEvent event);
    public void postRenderResponse(LifeCycleEvent event);
}
