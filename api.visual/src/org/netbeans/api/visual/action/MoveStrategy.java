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
 * This interfaces provides a movement strategy.
 *
 * @author David Kaspar
 */
public interface MoveStrategy {

    /**
     * Called after an user suggests a new location and before the suggested location is stored to a specified widget.
     * This allows to manipulate with a suggested location to perform snap-to-grid, locked-axis on any other movement strategy.
     * @param widget the moved widget
     * @param originalLocation the original location specified by the MoveProvider.getOriginalLocation method
     * @param suggestedLocation the location suggested by an user (usually by a mouse cursor position)
     * @return the new (optional modified) location processed by the strategy
     */
    Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation);

}
