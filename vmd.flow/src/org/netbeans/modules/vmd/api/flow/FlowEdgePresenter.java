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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.api.flow;

import org.netbeans.modules.vmd.api.flow.visual.*;
import org.netbeans.modules.vmd.api.model.Debug;

import java.util.Collection;
import java.util.Collections;

/**
 * This presenter manages a single edge.
 *
 * @author David Kaspar
 */
public abstract class FlowEdgePresenter extends FlowPresenter implements FlowPresenter.FlowUIResolver {

    private FlowPresenter.FlowUIResolver resolver = new FlowUIResolver() {
        public FlowDescriptor.Decorator getDecorator () { return FlowEdgePresenter.this.getDynamicPinDecorator (); }
        public FlowDescriptor.Behaviour getBehaviour () { return FlowEdgePresenter.this.getDynamicPinBehaviour (); }
    };

    private FlowEdgeDescriptor edge = null;

    private FlowEdgeDescriptor newEdge;
    private boolean removeAdd;

    public final void resolveRemoveBadge () {
    }

    public final void resolveRemoveEdge () {
        newEdge = isVisible () ? getEdgeDescriptor () : null;
        removeAdd = ! equals (edge, newEdge);

        if (removeAdd) {
            if (edge != null) {
                removeDettachSourcePin (edge);
                removeDettachTargetPin (edge);
                FlowScene scene = getScene ();
                scene.removeEdge (edge);
                scene.unregisterUI (edge, this);
            }
        }
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
        if (removeAdd) {
            edge = newEdge;
            if (edge != null) {
                FlowScene scene = getScene ();
                scene.registerUI (edge, this);
                scene.addEdge (edge);
                scene.updateBadges (edge);
                createAssignSourcePin (edge);
                createAssignTargetPin (edge);

            }
        } else if (edge != null && newEdge != null) {
            boolean sourceChanged = ! equals (edge.getSourcePinDescriptor (), newEdge.getSourcePinDescriptor ()) || edge.isDynamicSourcePin () != newEdge.isDynamicSourcePin ();
            boolean targetChanged = ! equals (edge.getTargetPinDescriptor (), newEdge.getTargetPinDescriptor ()) || edge.isDynamicTargetPin () != newEdge.isDynamicTargetPin ();

            if (sourceChanged)
                removeDettachSourcePin (edge);
            if (targetChanged)
                removeDettachTargetPin (edge);

            edge.update (newEdge);

            createAssignSourcePin (edge);
            createAssignTargetPin (edge);
        }
        newEdge = null;
    }

    public final void resolveAddBadge () {
    }

    public final void resolveUpdate () {
        FlowScene scene = getScene ();
        if (edge != null) {
            scene.getDecorator (edge).update (edge, scene);
            if (edge.isDynamicSourcePin ()) {
                FlowPinDescriptor sourcePin = edge.getSourcePinDescriptor ();
                scene.getDecorator (sourcePin).update (sourcePin, scene);
            }
            if (edge.isDynamicTargetPin ()) {
                FlowPinDescriptor targetPin = edge.getTargetPinDescriptor ();
                scene.getDecorator (targetPin).update (targetPin, scene);
            }
        }
    }

    private void createAssignSourcePin (FlowEdgeDescriptor edge) {
        FlowScene scene = getScene ();
        FlowPinDescriptor pin = edge.getSourcePinDescriptor ();
        if (pin != null) {
            if (edge.isDynamicSourcePin ()) {
                if (! scene.isObject (pin)) {
                    FlowNodeDescriptor node = getSourceNodeDescriptor (pin);
                    assert node != null;
                    scene.registerUI (pin, resolver);
                    scene.addPin (node, pin);
                    scene.updateBadges (pin);
                } else {
                    FlowPinDescriptor original = pin;
                    pin = (FlowPinDescriptor) scene.findStoredObject (pin);
                    if (pin == null)
                        Debug.warning ("Source pin not found", edge, original); // NOI18N
                }
            } else {
                FlowPinDescriptor original = pin;
                pin = (FlowPinDescriptor) scene.findStoredObject (pin);
                if (pin == null)
                    Debug.warning ("Source pin not found", edge, original); // NOI18N
            }
        }
        scene.setEdgeSource (edge, pin);
    }

    private void createAssignTargetPin (FlowEdgeDescriptor edge) {
        FlowScene scene = getScene ();
        FlowPinDescriptor pin = edge.getTargetPinDescriptor ();
        if (pin != null) {
            if (edge.isDynamicTargetPin ()) {
                if (! scene.isObject (pin)) {
                    FlowNodeDescriptor node = getTargetNodeDescriptor (pin);
                    assert node != null;
                    scene.registerUI (pin, resolver);
                    scene.addPin (node, pin);
                } else {
                    FlowPinDescriptor original = pin;
                    pin = (FlowPinDescriptor) scene.findStoredObject (pin);
                    if (pin == null)
                        Debug.warning ("Target pin not found", edge, original); // NOI18N
                }
            } else {
                FlowPinDescriptor original = pin;
                pin = (FlowPinDescriptor) scene.findStoredObject (pin);
                if (pin == null)
                    Debug.warning ("Target pin not found", edge, original); // NOI18N
            }
        }
        scene.setEdgeTarget (edge, pin);
    }

    private void removeDettachSourcePin (FlowEdgeDescriptor edge) {
        if (! edge.isDynamicSourcePin ())
            return;
        FlowScene scene = getScene ();
        FlowPinDescriptor pin = scene.getEdgeSource (edge);
        scene.removePin (pin);
        scene.unregisterUI (pin, resolver);
    }

    private void removeDettachTargetPin (FlowEdgeDescriptor edge) {
        if (! edge.isDynamicTargetPin ())
            return;
        FlowScene scene = getScene ();
        FlowPinDescriptor pin = scene.getEdgeTarget (edge);
        scene.removePin (pin);
        scene.unregisterUI (pin, resolver);
    }

    public final Collection<? extends FlowDescriptor> getFlowDescriptors () {
        return edge != null ? Collections.singleton (edge) : Collections.<FlowDescriptor>emptySet ();
    }

    protected abstract FlowNodeDescriptor getSourceNodeDescriptor (FlowPinDescriptor sourcePinDescriptor);

    protected abstract FlowNodeDescriptor getTargetNodeDescriptor (FlowPinDescriptor targetPinDescriptor);

    public abstract FlowEdgeDescriptor getEdgeDescriptor ();

    public abstract FlowEdgeDescriptor.EdgeDecorator getDecorator ();

    public abstract FlowEdgeDescriptor.EdgeBehaviour getBehaviour ();

    public abstract FlowPinDescriptor.PinDecorator getDynamicPinDecorator ();

    public abstract FlowPinDescriptor.PinBehaviour getDynamicPinBehaviour ();

}
