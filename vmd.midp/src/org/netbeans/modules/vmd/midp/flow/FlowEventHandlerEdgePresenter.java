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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.flow.FlowEdgePresenter;
import org.netbeans.modules.vmd.api.flow.visual.*;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourceAction;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * 
 */
// TODO - extract abstract methods for acquiring source and target
// TODO - rewrite to get relationship to the EventSource just by looking at the parent component
public abstract class FlowEventHandlerEdgePresenter extends FlowEdgePresenter {

    private final EventHandlerPinDecoratorBehaviour pinCtrl = new EventHandlerPinDecoratorBehaviour ();
    private final EventHandlerEdgeDecoratorBehaviour edgeCtrl = new EventHandlerEdgeDecoratorBehaviour ();

    private FlowEdgeDescriptor edgeDescriptor;

    protected abstract DesignComponent getTargetComponent ();

    protected String getEdgeID () {
        return FlowIDSupport.createEventHandlerEdgeID (getComponent ());
    }

    protected String getTargetPinID (DesignComponent target) {
        return FlowIDSupport.createEventHandlerTargetPinID (getComponent (), target);
    }

    protected boolean isDynamicTargetPin () {
        return true;
    }

    protected boolean isVisible () {
        return super.isVisible ()  &&  getSourceComponent () != null;
    }

    protected DesignComponent getSourceComponent () {
        return getComponent ().readProperty (EventHandlerCD.PROP_EVENT_SOURCE).getComponent ();
    }

    protected String getSourcePinID (DesignComponent source) {
        return FlowIDSupport.createEventSourcePinID (source);
    }

    protected DesignComponent getRepresentedSourceComponent (DesignComponent source) {
        return source;
    }

    protected DesignComponent getRepresentedTargetComponent (DesignComponent target) {
        return target;
    }

    protected DesignComponent getRepresentedComponent () {
        return getComponent ();
    }

    protected DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addParentFilter (getComponent (), 1, false);
    }

    public final void updateDescriptors () {
        edgeDescriptor = null;
        if (! isVisible ())
            return;
        DesignComponent source = getSourceComponent ();
        if (source == null)
            return;
        DesignComponent target = getTargetComponent ();
        if (target == null)
            return;

        edgeDescriptor = new FlowEdgeDescriptor (
                getRepresentedComponent (),
                getEdgeID (),
                new FlowPinDescriptor (getRepresentedSourceComponent (source), getSourcePinID (source)), false,
                new FlowPinDescriptor (getRepresentedTargetComponent (target), getTargetPinID (target)), isDynamicTargetPin ()
        );
    }

    protected final FlowNodeDescriptor getSourceNodeDescriptor (FlowPinDescriptor sourcePinDescriptor) {
        return FlowEventSourcePinPresenter.getNodeDescriptor (getSourceComponent ());
    }

    protected final FlowNodeDescriptor getTargetNodeDescriptor (FlowPinDescriptor targetPinDescriptor) {
        DesignComponent targetComponent = getTargetComponent ();
        return new FlowNodeDescriptor (targetComponent, FlowIDSupport.createNodeID4SingleNode (targetComponent));
    }

    public final FlowEdgeDescriptor getEdgeDescriptor () {
        return edgeDescriptor;
    }

    public FlowEdgeDescriptor.EdgeDecorator getDecorator () {
        return edgeCtrl;
    }

    public FlowEdgeDescriptor.EdgeBehaviour getBehaviour () {
        return edgeCtrl;
    }

    public FlowPinDescriptor.PinDecorator getDynamicPinDecorator () {
        return pinCtrl;
    }

    public FlowPinDescriptor.PinBehaviour getDynamicPinBehaviour () {
        return pinCtrl;
    }

    public static class EventHandlerPinDecoratorBehaviour implements FlowPinDescriptor.PinDecorator, FlowPinDescriptor.PinBehaviour {

        public Widget createWidget (FlowPinDescriptor descriptor, FlowScene scene) {
            return null;
        }

        public Anchor createAnchor (FlowPinDescriptor descriptor, FlowScene scene) {
            FlowNodeDescriptor node = scene.getPinNode (descriptor);
            return scene.getDecorator (node).createAnchor (node, scene);
        }

        public void update (FlowPinDescriptor descriptor, FlowScene scene) {
        }

        public String getOrderCategory (FlowPinDescriptor descriptor) {
            return null;
        }

        public void updateBadges (FlowPinDescriptor descriptor, FlowScene scene, List<FlowBadgeDescriptor> badges) {
        }

        public boolean isConnectionSource (FlowPinDescriptor pin) {
            return false;
        }

        public boolean isConnectionTarget (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            return false;
        }

        public void createConnection (FlowPinDescriptor sourcePin, FlowDescriptor target) {
        }

    }

    public class EventHandlerEdgeDecoratorBehaviour implements FlowEdgeDescriptor.EdgeDecorator, FlowEdgeDescriptor.EdgeBehaviour, FlowDescriptor.EditActionBehaviour {

        public Widget create (FlowEdgeDescriptor descriptor, FlowScene scene) {
            VMDConnectionWidget widget = new VMDConnectionWidget (scene, VMDFactory.getNetBeans60Scheme ());
            widget.setRouter (scene.createEdgeRouter ());
            scene.addEdgeCommonActions (widget);
            widget.getActions ().addAction (scene.createMoveControlPointAction ());
            return widget;
        }

        public void update (FlowEdgeDescriptor descriptor, FlowScene scene) {
        }

        public void setSourceAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor sourceAnchor) {
            ConnectionWidget widget = (ConnectionWidget) scene.findWidget (descriptor);
            widget.setSourceAnchor (sourceAnchor);
        }

        public void setTargetAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor targetAnchor) {
            ConnectionWidget widget = (ConnectionWidget) scene.findWidget (descriptor);
            widget.setTargetAnchor (targetAnchor);
        }

        public boolean isSourceReconnectable (FlowEdgeDescriptor descriptor) {
            return false; // TODO - reconnecting source pin
        }

        public boolean isTargetReconnectable (FlowEdgeDescriptor descriptor) {
            return true;
        }

        public boolean isReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource) {
            if (reconnectingSource)
                return false; // TODO - reconnecting source pin
            else
                return MidpDocumentSupport.isCreatableEventHandlerTo (replacementDescriptor != null ? replacementDescriptor.getRepresentedComponent () : null);
        }

        public void setReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource) {
            // TODO - reconnecting source pin
            if (! reconnectingSource) {
                DesignComponent eventSource = getSourceComponent ();
                if (eventSource != null)
                    MidpDocumentSupport.updateEventHandlerFromTarget (eventSource, replacementDescriptor != null ? replacementDescriptor.getRepresentedComponent () : null);
            }
        }

        public void edit (FlowDescriptor descriptor) {
            Collection<? extends ActionsPresenter> presenters = descriptor.getRepresentedComponent ().getPresenters (ActionsPresenter.class);
            for (ActionsPresenter presenter : presenters) {
                for (Action action : presenter.getActions ()) {
                    if (action instanceof GoToSourceAction) {
                        if (action.isEnabled ())
                            action.actionPerformed (null);
                        return;
                    }
                }
            }
        }

    }

}
