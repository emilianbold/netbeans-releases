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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * @author David Kaspar
 */
public abstract class FlowDescriptor {

    private DesignComponent representedComponent;
    private String descriptorID;

    protected FlowDescriptor (DesignComponent representedComponent, String descriptorID) {
        assert representedComponent != null && descriptorID != null;
        this.representedComponent = representedComponent;
        this.descriptorID = descriptorID;
    }

    public final DesignComponent getRepresentedComponent () {
        return representedComponent;
    }

    public final String getDescriptorID () {
        return descriptorID;
    }

    public final boolean equals (Object o) {
        if (this == o)
            return true;
        if (o == null || getClass () != o.getClass ())
            return false;
        FlowDescriptor desc = (FlowDescriptor) o;
        return representedComponent == desc.representedComponent && descriptorID.equals (desc.descriptorID);
    }

    public final int hashCode () {
        return 29 * representedComponent.hashCode () + descriptorID.hashCode ();
    }

    public String toString () {
        return getClass ().getSimpleName () + ":" + representedComponent.getComponentID () + ":" + descriptorID; // NOI18N
    }

    public interface Decorator {

    }

    public interface Behaviour {

    }

    public interface BadgeDecorator extends Decorator {

        void updateBadges (FlowDescriptor descriptor, FlowScene scene, List<FlowBadgeDescriptor> badges);

    }

    public interface AcceptActionBehaviour extends Behaviour {

        boolean isAcceptable (FlowDescriptor descriptor, Transferable transferable);

        void accept (FlowDescriptor descriptor, Transferable transferable);

    }

    public interface SelectActionBehaviour extends Behaviour {

        boolean select (FlowDescriptor descriptor, int modifiers);

    }

    public interface RenameActionBehaviour extends Behaviour {

        boolean isEditable (FlowDescriptor descriptor);

        String getText (FlowDescriptor descriptor);

        void setText (FlowDescriptor descriptor, String text);

    }

    public interface EditActionBehaviour extends Behaviour {

        void edit (FlowDescriptor descriptor);
        
    }

    // HINT - FlowDescriptor.KeyActionBehaviour is used for FlowScene/rootComponent only
    public interface KeyActionBehaviour extends Behaviour {

        boolean keyPressed (WidgetAction.WidgetKeyEvent e);

    }

}
