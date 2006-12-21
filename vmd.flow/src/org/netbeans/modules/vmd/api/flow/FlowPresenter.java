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

package org.netbeans.modules.vmd.api.flow;

import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.flow.FlowAccessController;

import java.util.Collection;

/**
 * Note: Do not extends this class directly, use specific Flow*Presenters instead.
 * @author dave
 */
public abstract class FlowPresenter extends DynamicPresenter {

    private boolean visible = false;
    private FlowAccessController controller;

    // TODO - RequiresSuperCall annotation
    protected void notifyAttached (DesignComponent component) {
        controller = getComponent ().getDocument ().getListenerManager ().getAccessController (FlowAccessController.class);
        visible = true;
        controller.addDirtyPresenter (this);
    }

    // TODO - RequiresSuperCall annotation
    protected void notifyDetached (DesignComponent component) {
        visible = false;
        controller.addDirtyPresenter (this);
    }

    // TODO - RequiresSuperCall annotation
    protected boolean isVisible () {
        return visible;
    }

    public final FlowScene getScene () {
        return controller.getScene ();
    }

    protected final void designChanged (DesignEvent event) {
        firePresenterChanged ();
    }

    protected final void presenterChanged (PresenterEvent event) {
        controller.addDirtyPresenter (this);
    }

    public abstract Collection<? extends FlowDescriptor> getFlowDescriptors ();

    public abstract void updateDescriptors ();

    public abstract void resolveRemoveBadge ();

    public abstract void resolveRemoveEdge ();

    public abstract void resolveRemovePin ();

    public abstract void resolveRemoveNode ();

    public abstract void resolveAddNode ();

    public abstract void resolveAddPin ();

    public abstract void resolveAddEdge ();

    public abstract void resolveAddBadge ();

    public abstract void resolveUpdate ();

    static boolean equals (Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

    public interface FlowUIResolver {

        FlowDescriptor.Decorator getDecorator ();

        FlowDescriptor.Behaviour getBehaviour ();

    }

}
