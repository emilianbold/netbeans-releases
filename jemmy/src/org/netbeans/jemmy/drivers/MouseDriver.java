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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
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
     * @param mouseButton mouse button (<code>InputEvent.BUTTON*_MASK</code> field)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void pressMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Releases mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param mouseButton mouse button (<code>InputEvent.BUTTON*_MASK</code> field)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void releaseMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Clicks mouse.
     * @param oper Component operator.
     * @param x Relative x coordinate.
     * @param y Relative y coordinate.
     * @param clickCount How many times to click.
     * @param mouseButton mouse button (<code>InputEvent.BUTTON*_MASK</code> field)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
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
     * @param mouseButton mouse button (<code>InputEvent.BUTTON*_MASK</code> field)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
     */
    public void dragMouse(ComponentOperator oper, int x, int y, int mouseButton, int modifiers);

    /**
     * Performs drag'n'drop.
     * @param oper Component operator.
     * @param start_x Relative x coordinate of start point.
     * @param start_y Relative y coordinate of start point.
     * @param end_x Relative x coordinate of end point.
     * @param end_y Relative y coordinate of end point.
     * @param mouseButton mouse button (<code>InputEvent.BUTTON*_MASK</code> field)
     * @param modifiers a combination of <code>InputEvent.*_MASK</code> fields.
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
