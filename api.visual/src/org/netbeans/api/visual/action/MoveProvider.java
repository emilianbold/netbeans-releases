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
 * This interface controls move action.
 *
 * @author David Kaspar
 */
public interface MoveProvider {

    /**
     * Called to nofity about the start of movement of a specified widget.
     * @param widget the moving widget
     */
    void movementStarted (Widget widget);

    /**
     * Called to notify about the end of movement of a specified widget.
     * @param widget the moved widget
     */
    void movementFinished (Widget widget);

    /**
     * Called to acquire a origin location against which the movement will be calculated.
     * Usually it is a value of the Widget.getLocation method.
     * @param widget the moving widget
     * @return the origin location
     */
    Point getOriginalLocation (Widget widget);

    /**
     * Called to set a new location of a moved widget. The new location is based on the location returned by getOriginalLocation method.
     * Usually it is implemented as the Widget.setPreferredLocation method call.
     * @param widget the moved widget
     * @param location the new location
     */
    void setNewLocation (Widget widget, Point location);

}
