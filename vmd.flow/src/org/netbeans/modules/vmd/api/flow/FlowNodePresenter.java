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
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;

import java.util.Collection;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public abstract class FlowNodePresenter extends FlowPresenter implements FlowPresenter.FlowUIResolver {

    private FlowNodeDescriptor node;

    private boolean removeAdd;

    public final void resolveRemoveBadge () {
    }

    public final void resolveRemoveEdge () {
    }

    public final void resolveRemovePin () {
    }

    public final void resolveRemoveNode () {
        FlowNodeDescriptor newNode = isVisible () ? getNodeDescriptor () : null;
        removeAdd = ! equals (newNode, node);

        if (removeAdd) {
            if (node != null) {
                FlowScene scene = getScene ();
                scene.removeNode (node);
                scene.unregisterUI (node, this);
            }
            node = newNode;
        }
    }

    public final void resolveAddNode () {
        if (removeAdd) {
            if (node != null) {
                FlowScene scene = getScene ();
                scene.registerUI (node, this);
                scene.addNode (node);
                scene.updateBadges (node);
                scene.scheduleNodeDescriptorForOrdering (node);
            }
        }
    }

    public final void resolveAddPin () {
    }

    public final void resolveAddEdge () {
    }

    public final void resolveAddBadge () {
    }

    public final void resolveUpdate () {
        if (node != null) {
            FlowScene scene = getScene ();
            scene.getDecorator (node).update (node, scene);
        }
    }

    public final Collection<? extends FlowDescriptor> getFlowDescriptors () {
        return node != null ? Collections.singleton (node) : Collections.<FlowDescriptor>emptySet ();
    }

    public abstract FlowNodeDescriptor getNodeDescriptor ();

    public abstract FlowNodeDescriptor.NodeDecorator getDecorator ();

    public abstract FlowNodeDescriptor.NodeBehaviour getBehaviour ();

}
