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

import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * WARNING - it has to be assured that there is a child component twice in the children or a propertyValue used. Otherwise ID will be no longer unique.
 * @author David Kaspar
 */
// TODO - redesign IDs
public final class FlowIDSupport {

    public static String createNodeID4SingleNode (DesignComponent nodeComponent) {
        return "componentNode" + nodeComponent.getComponentID (); // NOI18N
    }

    public static String createEventSourcePinID (DesignComponent eventSourceComponent) {
        return "eventSourcePin" + eventSourceComponent.getComponentID (); // NOI18N
    }

    public static String createEventHandlerEdgeID (DesignComponent eventHandlerComponent) {
        return "eventHandlerEdge" + eventHandlerComponent.getComponentID (); // NOI18N
    }

    public static String createEventHandlerTargetPinID (DesignComponent eventHandlerComponent, DesignComponent targetComponent) {
        return "eventHandlerTargetPin" + eventHandlerComponent.getComponentID () + "at" + targetComponent.getComponentID (); // NOI18N
    }

    public static String createEventHandlerPinBadgeID (DesignComponent eventHandlerComponent) {
        return "eventHandlerPinBadge" + eventHandlerComponent.getComponentID (); // NOI18N
    }

    public static String createEventSourcePinBadgeID (DesignComponent eventSourceComponent, String pinBadgeID) {
        return "eventSourcePinBadge" + eventSourceComponent.getComponentID () + pinBadgeID; // NOI18N
    }

    public static String createSwitchDisplayableEventHandlerEdgeID (DesignComponent eventHandlerComponent) {
        return "eventHandlerEdge" + eventHandlerComponent.getComponentID (); // NOI18N
    }

    public static String createSwitchDisplayableEventHandlerForwardEdgeID (DesignComponent eventHandlerComponent) {
        return "eventHandlerForwardEdge" + eventHandlerComponent.getComponentID (); // NOI18N
    }
}
