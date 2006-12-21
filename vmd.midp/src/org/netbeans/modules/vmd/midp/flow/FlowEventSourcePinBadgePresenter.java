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

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - add dependency to a presenterChanged on eventSource component
public abstract class FlowEventSourcePinBadgePresenter extends FlowPinBadgePresenter {

    private final EventSourcePinBadgeDecorator ctrl = new EventSourcePinBadgeDecorator ();

    private FlowBadgeDescriptor pinBadgeDescriptor;
    private Image badge;
    private String pinBadgeID;
    private int order;

    public FlowEventSourcePinBadgePresenter (Image badge, String pinBadgeID, int order) {
        this.badge = badge;
        this.pinBadgeID = pinBadgeID;
        this.order = order;
    }

    protected final FlowNodeDescriptor getNodeDescriptor () {
        return FlowEventSourcePinPresenter.getNodeDescriptor (getComponent ());
    }

    public final FlowPinDescriptor getPinDescriptor () {
        return FlowEventSourcePinPresenter.getPinDescriptor (getComponent ());
    }

    protected abstract boolean isBadgeAvailable ();

    public FlowBadgeDescriptor getPinBadgeDescriptor () {
        return pinBadgeDescriptor;
    }

    public final void updateDescriptors () {
        DesignComponent component = getComponent ();
        pinBadgeDescriptor = isBadgeAvailable () ? new FlowBadgeDescriptor (component, FlowIDSupport.createEventSourcePinBadgeID (component, pinBadgeID)) : null;
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

    private class EventSourcePinBadgeDecorator implements FlowBadgeDescriptor.BadgeDecorator, FlowBadgeDescriptor.BadgeBehaviour {
        public int getOrder (FlowBadgeDescriptor descriptor) {
            return FlowEventSourcePinBadgePresenter.this.order;
        }

        public Image getImage (FlowBadgeDescriptor descriptor) {
            return FlowEventSourcePinBadgePresenter.this.badge;
        }

    }

}
