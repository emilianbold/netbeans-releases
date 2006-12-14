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

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface provides a resizing strategy.
 *
 * @author David Kaspar
 */
public interface ResizeStrategy {

    /**
     * Called after an user suggests a new boundary and before the suggested boundary is stored to a specified widget.
     * This allows to manipulate with a suggested boundary to perform snap-to-grid, locked-axis on any other resizing strategy.
     * @param widget the resized widget
     * @param originalBounds the original bounds of the resizing widget
     * @param suggestedBounds the bounds of the resizing widget suggested by an user (usually by a mouse cursor position)
     * @param controlPoint the control point that is used by an user for resizing
     * @return the new (optionally modified) boundary processed by the strategy
     */
    public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ResizeProvider.ControlPoint controlPoint);

}
