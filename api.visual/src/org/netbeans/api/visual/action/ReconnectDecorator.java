/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface decorates a reconnect action.
 *
 * @author David Kaspar
 */
public interface ReconnectDecorator {

    /**
     * Creates an anchor for a specified replacement widget (of a connection source or target which is going to be reconnected).
     * @param replacementWidget the replacement widget
     * @return the anchor
     */
    Anchor createReplacementWidgetAnchor (Widget replacementWidget);

    /**
     * Creates a floating anchor for a specified location when there is no replacement a widget
     * @param location the scene location
     * @return the floating anchor; usually FixedAnchor
     */
    Anchor createFloatAnchor (Point location);

}
