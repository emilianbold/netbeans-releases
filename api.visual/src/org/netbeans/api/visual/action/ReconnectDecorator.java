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
