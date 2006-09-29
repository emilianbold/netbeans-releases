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

import org.netbeans.api.visual.widget.ConnectionWidget;

import java.util.List;
import java.awt.*;

/**
 * This interface controls a move control point action.
 *
 * @author David Kaspar
 */
public interface MoveControlPointProvider {

    /**
     * Called to resolve control points of a connection widget for specified suggested change of a location of a control point specified by its index.
     * Usually used for supplying the move strategy of control points.
     * @param connectionWidget the connection widget
     * @param index the index of the control point which new location was suggested by an user
     * @param suggestedLocation the suggested location (by an user) of a control point specified by its index
     * @return the list of new control points of the connection widget
     */
    List<Point> locationSuggested (ConnectionWidget connectionWidget, int index, Point suggestedLocation);

}
