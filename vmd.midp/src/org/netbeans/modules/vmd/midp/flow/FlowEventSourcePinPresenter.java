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
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.modules.vmd.api.flow.FlowPinPresenter;
import org.netbeans.modules.vmd.api.flow.visual.*;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - rewrite to query target port to generate EventHandler component
// TODO - rewrite to use InfoPresenter
public abstract class FlowEventSourcePinPresenter extends FlowPinPresenter {

    private EventSourcePinDecoratorBehaviour ctrl = new EventSourcePinDecoratorBehaviour ();

    private FlowPinDescriptor pinDescriptor;

    protected abstract DesignComponent getComponentForAttachingPin ();

    protected abstract String getDisplayName ();

    protected abstract String getOrder ();

    protected boolean canRename () {
        return false;
    }

    protected String getRenameName () {
        return getDisplayName ();
    }

    protected void setRenameName (String name) {
    }

    protected String getPinID () {
        return FlowIDSupport.createEventSourcePinID (getComponent ());
    }

    protected boolean isVisible () {
        return super.isVisible ()  && getComponentForAttachingPin () != null;
    }

    protected final FlowNodeDescriptor getNodeDescriptor () {
        DesignComponent component = getComponentForAttachingPin ();
        FlowInfoNodePresenter presenter = component.getPresenter (FlowInfoNodePresenter.class);
        return presenter != null && presenter.isVisible () ? presenter.getNodeDescriptor () : null;
    }

    public final void updateDescriptors () {
        pinDescriptor = isVisible () ? new FlowPinDescriptor (getComponent (), getPinID ()) : null;
    }

    public final FlowPinDescriptor getPinDescriptor () {
        return pinDescriptor;
    }

    public FlowPinDescriptor.PinDecorator getDecorator () {
        return ctrl;
    }

    public FlowPinDescriptor.PinBehaviour getBehaviour () {
        return ctrl;
    }

    protected DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addComponentFilter (getComponent (), false).addHierarchyFilter (getComponent (), false);
    }

    public static FlowNodeDescriptor getNodeDescriptor (DesignComponent eventSourceComponent) {
        if (eventSourceComponent == null)
            return null;
        FlowEventSourcePinPresenter presenter = eventSourceComponent.getPresenter (FlowEventSourcePinPresenter.class);
        return presenter != null  &&  presenter.isVisible () ? presenter.getNodeDescriptor () : null;
    }

    public static FlowPinDescriptor getPinDescriptor (DesignComponent eventSourceComponent) {
        if (eventSourceComponent == null)
            return null;
        FlowEventSourcePinPresenter presenter = eventSourceComponent.getPresenter (FlowEventSourcePinPresenter.class);
        return presenter != null ? presenter.pinDescriptor : null;
    }

    protected class EventSourcePinDecoratorBehaviour implements FlowPinDescriptor.PinDecorator, FlowPinDescriptor.PinBehaviour, FlowDescriptor.BadgeDecorator, FlowDescriptor.AcceptActionBehavior, FlowDescriptor.RenameActionBehaviour {

        public Widget createWidget (FlowPinDescriptor descriptor, FlowScene scene) {
            VMDPinWidget vmdPinWidget = new VMDPinWidget (scene);
            scene.addPinCommonActions (vmdPinWidget);
            Widget pinNameWidget = vmdPinWidget.getPinNameWidget ();
            vmdPinWidget.getActions ().addAction (ActionFactory.createForwardKeyEventsAction (pinNameWidget, null));
            pinNameWidget.getActions ().addAction (scene.createRenameAction ());
            return vmdPinWidget;
        }

        public Anchor createAnchor (FlowPinDescriptor descriptor, FlowScene scene) {
            FlowNodeDescriptor node = scene.getPinNode (descriptor);
            Widget nodeWidget = scene.findWidget (node);
            Anchor anchor = AnchorFactory.createDirectionalAnchor (scene.findWidget (descriptor), AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            return ((VMDNodeWidget) nodeWidget).createAnchorPin (anchor);
        }

        public void update (FlowPinDescriptor descriptor, FlowScene scene) {
            VMDPinWidget widget = (VMDPinWidget) scene.findWidget (descriptor);
            widget.setPinName (FlowEventSourcePinPresenter.this.getDisplayName ());
        }

        public String getOrderCategory (FlowPinDescriptor descriptor) {
            return getOrder ();
        }

        public void updateBadges (FlowDescriptor descriptor, FlowScene scene, List<FlowBadgeDescriptor> badges) {
            VMDPinWidget widget = (VMDPinWidget) scene.findWidget (descriptor);
            ArrayList<Image> images = new ArrayList<Image> ();
            for (FlowBadgeDescriptor badge : badges) {
                Image image = scene.getDecorator (badge).getImage (badge);
                if (image != null)
                    images.add (image);
            }
            widget.setGlyphs (images);
        }

        public boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable) {
            return AcceptSupport.isAcceptable (descriptor.getRepresentedComponent (), transferable);
        }

        public void accept (FlowDescriptor descriptor, Transferable transferable) {
            AcceptSupport.accept (descriptor.getRepresentedComponent (), transferable);
        }

        public boolean isConnectionSource (FlowPinDescriptor pin) {
            return true;
        }

        public boolean isConnectionTarget (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            if (target == null)
                return true;
            return MidpDocumentSupport.isCreatableEventHandlerTo (target.getRepresentedComponent ());
        }

        public void createConnection (FlowPinDescriptor sourcePin, FlowDescriptor target) {
            MidpDocumentSupport.updateEventHandlerFromTarget (getComponent (), target != null ? target.getRepresentedComponent () : null);
        }

        public boolean isEditable (FlowDescriptor descriptor) {
            return FlowEventSourcePinPresenter.this.canRename ();
        }

        public String getText (FlowDescriptor descriptor) {
            return FlowEventSourcePinPresenter.this.getRenameName ();
        }

        public void setText (FlowDescriptor descriptor, String text) {
            FlowEventSourcePinPresenter.this.setRenameName (text);
        }

    }

}
