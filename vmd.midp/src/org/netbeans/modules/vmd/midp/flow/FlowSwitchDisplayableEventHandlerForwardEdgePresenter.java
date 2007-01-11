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
 *
 */

package org.netbeans.modules.vmd.midp.flow;

import org.netbeans.modules.vmd.api.flow.visual.FlowDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowEdgeDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.handlers.SwitchDisplayableEventHandlerCD;

/**
 * @author David Kaspar
 */
public final class FlowSwitchDisplayableEventHandlerForwardEdgePresenter extends FlowEventHandlerEdgePresenter {

    private final EventHandlerForwardEdgeDecoratorBehaviour edgeCtrl = new EventHandlerForwardEdgeDecoratorBehaviour ();

    protected DesignComponent getTargetComponent () {
        return getComponent ().readProperty (SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE).getComponent ();
    }

    protected String getEdgeID () {
        return FlowIDSupport.createSwitchDisplayableEventHandlerForwardEdgeID (getComponent ());
    }

    protected DesignComponent getSourceComponent () {
        return getComponent ().readProperty (SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent ();
    }

    protected String getSourcePinID (DesignComponent source) {
        return FlowIDSupport.createEventHandlerTargetPinID (getComponent (), source);
    }

    protected DesignComponent getRepresentedSourceComponent (DesignComponent source) {
        return getComponent ();
    }

    protected String getTargetPinID (DesignComponent target) {
        return FlowIDSupport.createEventHandlerTargetForwardPinID (getComponent (), target);
    }

    protected DesignComponent getRepresentedTargetComponent (DesignComponent target) {
        return getComponent ();
    }

    protected boolean isVisible () {
        DesignComponent component = getComponent ();
        return super.isVisible ()  && component.getParentComponent () != null  &&  component.readProperty (SwitchDisplayableEventHandlerCD.PROP_ALERT).getComponent () != null;
    }

    public FlowEdgeDescriptor.EdgeDecorator getDecorator () {
        return edgeCtrl;
    }

    public FlowEdgeDescriptor.EdgeBehaviour getBehaviour () {
        return edgeCtrl;
    }

    public class EventHandlerForwardEdgeDecoratorBehaviour extends EventHandlerEdgeDecoratorBehaviour {

        public boolean isSourceReconnectable (FlowEdgeDescriptor descriptor) {
            return false;
        }

        public boolean isTargetReconnectable (FlowEdgeDescriptor descriptor) {
            return true;
        }

        public boolean isReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource) {
            assert ! reconnectingSource;
            DesignComponent targetComponent = replacementDescriptor != null ? replacementDescriptor.getRepresentedComponent () : null;
            return targetComponent != null  &&  getComponent ().getDocument ().getDescriptorRegistry ().isInHierarchy (DisplayableCD.TYPEID, targetComponent.getType ());
        }

        public void setReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource) {
            assert ! reconnectingSource;
            getComponent ().writeProperty (SwitchDisplayableEventHandlerCD.PROP_DISPLAYABLE, PropertyValue.createComponentReference (replacementDescriptor.getRepresentedComponent ()));
        }

    }

}
