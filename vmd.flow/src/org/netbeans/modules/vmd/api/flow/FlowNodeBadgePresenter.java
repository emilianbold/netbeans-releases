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

import org.netbeans.modules.vmd.api.flow.visual.*;

import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public abstract class FlowNodeBadgePresenter extends FlowPresenter implements FlowPresenter.FlowUIResolver {

    private FlowNodeDescriptor node = null;
    private FlowBadgeDescriptor badge = null;

    private FlowNodeDescriptor oldNode;
    private boolean removeAdd;

    public final void resolveRemoveBadge () {
        FlowScene scene = getScene ();

        FlowNodeDescriptor newNode;
        FlowBadgeDescriptor newBadge;
        if (isVisible ()) {
            newNode = getNodeDescriptor ();
            newBadge = newNode != null ? getPinBadgeDescriptor () : null;
        } else {
            newNode = null;
            newBadge = null;
        }

        removeAdd = ! equals (node, newNode)  ||  ! equals (badge, newBadge);

        if (removeAdd) {
            if (badge != null) {
                scene.removeBadge (node, badge);
                scene.unregisterUI (badge, this);
            }
            oldNode = node;
            node = newNode;
            badge = newBadge;
        } else
            oldNode = null;
    }

    public final void resolveRemoveEdge () {
    }

    public final void resolveRemovePin () {
    }

    public final void resolveRemoveNode () {
    }

    public final void resolveAddNode () {
    }

    public final void resolveAddPin () {
    }

    public final void resolveAddEdge () {
    }

    public final void resolveAddBadge () {
        if (removeAdd) {
            if (badge != null) {
                FlowScene scene = getScene ();
                scene.registerUI (badge, this);
                scene.addBadge (oldNode, badge);
            }
        }
    }

    public final void resolveUpdate () {
        FlowScene scene = getScene ();
        if (node != null)
            scene.updateBadges (node);
        if (oldNode != null)
            scene.updateBadges (oldNode);
        oldNode = null;
    }

    public final Collection<? extends FlowDescriptor> getFlowDescriptors () {
        return Collections.emptySet ();
    }

    protected abstract FlowNodeDescriptor getNodeDescriptor ();

    protected abstract FlowPinDescriptor getPinDescriptor ();

    protected abstract FlowBadgeDescriptor getPinBadgeDescriptor ();

    public abstract FlowBadgeDescriptor.BadgeDecorator getDecorator ();

    public abstract FlowBadgeDescriptor.BadgeBehaviour getBehaviour ();

}
