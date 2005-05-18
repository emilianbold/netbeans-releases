/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 *//*
 * TabLayoutModel.java
 *
 * Created on May 16, 2003, 3:47 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;


/**
 * A model representing the visual layout of tabs in a TabDataModel as a set of
 * rectangles.  Used by BasicTabDisplayerUI and its subclasses to manage the layout of
 * tabs in the displayer.
 *
 * @author Tim Boudreau
 */
public interface TabLayoutModel {
    /**
     * Get the x coordinate of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The coordinate
     */
    public int getX(int index);

    /**
     * Get the y coordinate of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The coordinate
     */
    public int getY(int index);

    /**
     * Get the width of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The width
     */
    public int getW(int index);

    /**
     * Get the height of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The height
     */
    public int getH(int index);

    /**
     * Get the index of the tab in the data model for the supplied point.
     *
     * @param x X coordinate of a point representing a set of pixel coordinate in the space
     *          modeled by this layout model
     * @param y Y coordinate
     * @return The index into the data model of the tab displayed at the passed
     *         point or -1
     */
    public int indexOfPoint(int x, int y);

    // XXX DnD only
    /**
     * Gets the index of possibly dropped component (as a new tab).
     */
    public int dropIndexOfPoint(int x, int y);

    public void setPadding (Dimension d);
}
