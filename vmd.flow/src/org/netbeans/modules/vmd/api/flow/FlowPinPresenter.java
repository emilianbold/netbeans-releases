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

import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;

import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public abstract class FlowPinPresenter extends FlowPresenter implements FlowPresenter.FlowUIResolver {

    private FlowNodeDescriptor node = null;
    private FlowPinDescriptor pin = null;

    public final void resolveRemoveBadge () {
    }

    public final void resolveRemoveEdge () {
    }

    public final void resolveRemovePin () {
        FlowNodeDescriptor newNode;
        FlowPinDescriptor newPin;
        if (isVisible ()) {
            newNode = getNodeDescriptor ();
            newPin = newNode != null ? getPinDescriptor () : null;
        } else {
            newNode = null;
            newPin = null;
        }

        if (! equals (node, newNode) || ! equals (pin, newPin)) {
            if (pin != null) {
                FlowScene scene = getScene ();
                scene.removePin (pin);
                scene.unregisterUI (pin, this);
                scene.scheduleNodeDescriptorForOrdering (node);
            }
            node = newNode;
            pin = newPin;
        }
    }

    public final void resolveRemoveNode () {
    }

    public final void resolveAddNode () {
    }

    public final void resolveAddPin () {
        if (pin != null) {
            FlowScene scene = getScene ();
            if (! scene.isObject (pin)) {
                scene.registerUI (pin, this);
                scene.addPin (node, pin);
                scene.updateBadges (pin);
                scene.scheduleNodeDescriptorForOrdering (node);
            }
        }
    }

    public final void resolveAddEdge () {
    }

    public final void resolveAddBadge () {
    }

    public final void resolveUpdate () {
        if (pin != null)
            getScene ().getDecorator (pin).update (pin, getScene ());
    }

    public final Collection<? extends FlowDescriptor> getFlowDescriptors () {
        return pin != null ? Collections.singleton (pin) : Collections.<FlowDescriptor>emptySet ();
    }

    protected abstract FlowNodeDescriptor getNodeDescriptor ();

    protected abstract FlowPinDescriptor getPinDescriptor ();

    public abstract FlowPinDescriptor.PinDecorator getDecorator ();

    public abstract FlowPinDescriptor.PinBehaviour getBehaviour ();

}
