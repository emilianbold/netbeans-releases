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

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interfaces provides a resizing strategy.
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
