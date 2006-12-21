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
package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.modules.vmd.api.flow.FlowEdgePresenter;
import org.netbeans.modules.vmd.api.flow.visual.*;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;

import java.util.List;

/**
 * @author David Kaspar
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

    protected String getPinID () {
        return FlowIDSupport.createEventHandlerEdgeID (getComponent ());
    }

    protected boolean isVisible () {
        return super.isVisible ()  &&  getEventSourceComponent () != null;
    }

    protected DesignComponent getEventSourceComponent () {
        return getComponent ().readProperty (EventHandlerCD.PROP_EVENT_SOURCE).getComponent ();
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
        DesignComponent component = getComponent ();
        DesignComponent eventSource = getEventSourceComponent ();
        if (eventSource == null)
            return;
        DesignComponent target = getTargetComponent ();
        if (target == null)
            return;

        edgeDescriptor = new FlowEdgeDescriptor (
                getRepresentedComponent (),
                FlowIDSupport.createEventHandlerEdgeID (component),
                new FlowPinDescriptor (eventSource, FlowIDSupport.createEventSourcePinID (eventSource)), false,
                new FlowPinDescriptor (target, FlowIDSupport.createEventHandlerTargetPinID (component)), true
        );
    }

    protected final FlowNodeDescriptor getSourceNodeDescriptor (FlowPinDescriptor sourcePinDescriptor) {
        return FlowEventSourcePinPresenter.getNodeDescriptor (getEventSourceComponent ());
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

    public class EventHandlerEdgeDecoratorBehaviour implements FlowEdgeDescriptor.EdgeDecorator, FlowEdgeDescriptor.EdgeBehaviour {

        public Widget create (FlowEdgeDescriptor descriptor, FlowScene scene) {
            VMDConnectionWidget widget = new VMDConnectionWidget (scene, scene.createEdgeRouter ());
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
                DesignComponent eventSource = getEventSourceComponent ();
                if (eventSource != null)
                    MidpDocumentSupport.updateEventHandlerFromTarget (eventSource, replacementDescriptor != null ? replacementDescriptor.getRepresentedComponent () : null);
            }
        }

    }

}
