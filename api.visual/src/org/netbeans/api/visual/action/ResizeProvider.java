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

/**
 * @author David Kaspar
 */
public interface ResizeProvider {

    /**
     * This enum represents a control point of a resize action.
     */
    // TODO - could be moved to ResizeStrategy interface, where it is used
    public enum ControlPoint {

        TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT

    }

    /**
     * Called to notify about the start of resizing.
     * @param widget the resizing widget
     */
    void resizingStarted (Widget widget);

    /**
     * Called to notify about the finish of resizing.
     * @param widget the resized widget
     */
    void resizingFinished (Widget widget);

}
