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
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface decorates a connect action.
 *
 * @author David Kaspar
 */
public interface ConnectDecorator {

    /**
     * Creates a connection widget that is temporarily used for visualization of a connection while user is dragging (creating) it.
     * @param scene the scene where the connection widget will be used
     * @return the connection widget
     */
    ConnectionWidget createConnectionWidget (Scene scene);

    /**
     * Creates a source anchor for a specified source widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param sourceWidget the source widget
     * @return the source anchor
     */
    Anchor createSourceAnchor (Widget sourceWidget);

    /**
     * Creates a target anchor for a specified target widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param targetWidget the source widget
     * @return the target anchor
     */
    Anchor createTargetAnchor (Widget targetWidget);

    /**
     * Creates a floating anchor which will be used when the connection target is not attached to any widget. The anchor will be used at the temporary connection widget created by the createConnectionWidget method.
     * @param location the scene location of the mouse cursor
     * @return the floating anchor; usually FixedAnchor
     */
    Anchor createFloatAnchor (Point location);

}
