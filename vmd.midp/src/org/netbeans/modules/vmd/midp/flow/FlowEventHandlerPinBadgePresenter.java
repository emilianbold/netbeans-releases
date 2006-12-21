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

import org.netbeans.modules.vmd.api.flow.FlowPinBadgePresenter;
import org.netbeans.modules.vmd.api.flow.visual.FlowBadgeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowPinDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - add dependency to a presenterChanged on eventSource component
public class FlowEventHandlerPinBadgePresenter extends FlowPinBadgePresenter {

    private FlowBadgeDescriptor pinBadgeDescriptor;
    private final EventHandlerPinBadgeDecoratorBehaviour ctrl = new EventHandlerPinBadgeDecoratorBehaviour ();
    private Image badge;
    private int order;

    public FlowEventHandlerPinBadgePresenter (Image badge, int order) {
        this.badge = badge;
        this.order = order;
    }

    protected DesignComponent getEventSourceComponent () {
        return getComponent ().readProperty (EventHandlerCD.PROP_EVENT_SOURCE).getComponent ();
    }

    protected final FlowNodeDescriptor getNodeDescriptor () {
        return FlowEventSourcePinPresenter.getNodeDescriptor (getEventSourceComponent ());
    }

    public final FlowPinDescriptor getPinDescriptor () {
        return FlowEventSourcePinPresenter.getPinDescriptor (getEventSourceComponent ());
    }

    public FlowBadgeDescriptor getPinBadgeDescriptor () {
        return pinBadgeDescriptor;
    }

    protected boolean isBadgeAvailable () {
        return true;
    }

    public final void updateDescriptors () {
        if (! isBadgeAvailable ()) {
            pinBadgeDescriptor = null;
        } else {
            DesignComponent component = getComponent ();
            pinBadgeDescriptor = new FlowBadgeDescriptor (component, FlowIDSupport.createEventHandlerPinBadgeID (component));
        }
    }

    protected DesignEventFilter getEventFilter () {
        return new DesignEventFilter ().addComponentFilter (getComponent (), false).addHierarchyFilter (getComponent (), false);
    }

    public FlowBadgeDescriptor.BadgeDecorator getDecorator () {
        return ctrl;
    }

    public FlowBadgeDescriptor.BadgeBehaviour getBehaviour () {
        return ctrl;
    }

    private class EventHandlerPinBadgeDecoratorBehaviour implements FlowBadgeDescriptor.BadgeDecorator, FlowBadgeDescriptor.BadgeBehaviour {

        public int getOrder (FlowBadgeDescriptor descriptor) {
            return FlowEventHandlerPinBadgePresenter.this.order;
        }

        public Image getImage (FlowBadgeDescriptor descriptor) {
            return FlowEventHandlerPinBadgePresenter.this.badge;
        }

    }

}
