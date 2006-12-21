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

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class FlowPinDescriptor extends FlowDescriptor {

    private ArrayList<FlowBadgeDescriptor> badges = new ArrayList<FlowBadgeDescriptor> ();

    public FlowPinDescriptor (DesignComponent representedComponent, String descriptorID) {
        super (representedComponent, descriptorID);
    }

    public interface PinDecorator extends Decorator {

        Widget createWidget (FlowPinDescriptor descriptor, FlowScene scene);

        Anchor createAnchor (FlowPinDescriptor descriptor, FlowScene scene);

        void update (FlowPinDescriptor descriptor, FlowScene scene);

        String getOrderCategory (FlowPinDescriptor descriptor);

    }

    public interface PinBehaviour extends Behaviour {

        boolean isConnectionSource (FlowPinDescriptor pin);

        boolean isConnectionTarget (FlowPinDescriptor sourcePin, FlowDescriptor target);

        void createConnection (FlowPinDescriptor sourcePin, FlowDescriptor target);

    }

}
