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
package org.netbeans.modules.vmd.api.flow.visual;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * @author David Kaspar
 */
public final class FlowEdgeDescriptor extends FlowDescriptor {

    private FlowPinDescriptor sourcePinDescriptor;
    private boolean dynamicSourcePin;
    private FlowPinDescriptor targetPinDescriptor;
    private boolean dynamicTargetPin;

    public FlowEdgeDescriptor (DesignComponent representedComponent, String descriptorID, FlowPinDescriptor sourcePinDescriptor, boolean dynamicSourcePin, FlowPinDescriptor targetPinDescriptor, boolean dynamicTargetPin) {
        super (representedComponent, descriptorID);
        this.sourcePinDescriptor = sourcePinDescriptor;
        this.dynamicSourcePin = dynamicSourcePin;
        this.targetPinDescriptor = targetPinDescriptor;
        this.dynamicTargetPin = dynamicTargetPin;
    }

    public void update (FlowEdgeDescriptor edge) {
        sourcePinDescriptor = edge.sourcePinDescriptor;
        dynamicSourcePin = edge.dynamicSourcePin;
        targetPinDescriptor = edge.targetPinDescriptor;
        dynamicTargetPin = edge.dynamicTargetPin;
    }

    public FlowPinDescriptor getSourcePinDescriptor () {
        return sourcePinDescriptor;
    }

    public boolean isDynamicSourcePin () {
        return dynamicSourcePin;
    }

    public FlowPinDescriptor getTargetPinDescriptor () {
        return targetPinDescriptor;
    }

    public boolean isDynamicTargetPin () {
        return dynamicTargetPin;
    }

    public interface EdgeDecorator extends Decorator {

        Widget create (FlowEdgeDescriptor descriptor, FlowScene scene);

        void update (FlowEdgeDescriptor descriptor, FlowScene scene);

        void setSourceAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor sourceAnchor);

        void setTargetAnchor (FlowEdgeDescriptor descriptor, FlowScene scene, Anchor targetAnchor);

    }

    public interface EdgeBehaviour extends Behaviour {

        boolean isSourceReconnectable (FlowEdgeDescriptor descriptor);

        boolean isTargetReconnectable (FlowEdgeDescriptor descriptor);

        boolean isReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource);

        void setReplacement (FlowEdgeDescriptor descriptor, FlowDescriptor replacementDescriptor, boolean reconnectingSource);

    }

}
