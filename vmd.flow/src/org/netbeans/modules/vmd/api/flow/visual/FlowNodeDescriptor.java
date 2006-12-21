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

import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class FlowNodeDescriptor extends FlowDescriptor {

    public FlowNodeDescriptor (DesignComponent representedComponent, String descriptorID) {
        super (representedComponent, descriptorID);
    }

    public interface NodeDecorator extends Decorator {

        Widget createWidget (FlowNodeDescriptor descriptor, FlowScene scene);

        void update (FlowNodeDescriptor descriptor, FlowScene scene);

        void attachPinWidget (FlowNodeDescriptor descriptor, FlowScene scene, Widget pinWidget);

        Anchor createAnchor (FlowNodeDescriptor node, FlowScene scene);

        DesignComponent getComponentWithPinOrderPresenters ();

        void orderPins (FlowNodeDescriptor node, FlowScene scene, Map<String, List<FlowPinDescriptor>> categories);

    }

    public interface NodeBehaviour extends Behaviour {

    }

}
