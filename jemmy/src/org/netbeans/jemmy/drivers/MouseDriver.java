/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to simulate mouse operations.
 */
public interface MouseDriver {

    /**
     * Presses mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param mouseButton
     * @param modifiers
     */
    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Releases mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param mouseButton
     * @param modifiers
     */
    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Clicks mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param clickCount How many times to click.
     * @param mouseButton
     * @param modifiers
     * @param mouseClick Time between pressing and releasing mouse.
     */
    public void clickMouse(ComponentOperator oper, int x, int y, int clickCount, int mouseButton, 
			   int modifiers, Timeout mouseClick);

    /**
     * Moves mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     */
    public void moveMouse(ComponentOperator oper, int x, int y);

    /**
     * Drags mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param mouseButton
     * @param modifiers
     */
    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Performs drag'n'drop.
     * @param oper Component operator.
     * @param start_x Relative x coordinate of start point.
     * @param start_y Relative y coordinate of start point.
     * @param end_x Relative x coordinate of end point.
     * @param end_y Relative y coordinate of end point.
     * @param mouseButton
     * @param modifiers
     * @param before Time to sleep after taking (before dragging)
     * @param after Time to sleep before dropping (after dragging)
     */
    public void dragNDrop(ComponentOperator oper, int start_x, int start_y, int end_x, int end_y, 
			  int mouseButton, int modifiers, Timeout before, Timeout after);

    /**
     * Moves mouse inside a component.
     * @param oper Component operator.
     */
    public void enterMouse(ComponentOperator oper);

    /**
     * Moves mouse outside a component.
     * @param oper Component operator.
     */
    public void exitMouse(ComponentOperator oper);
}
