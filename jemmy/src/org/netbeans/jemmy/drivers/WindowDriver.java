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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with windows.
 */
public interface WindowDriver {

    /**
     * Activates a window.
     * @param oper Window operator.
     */
    public void activate(ComponentOperator oper);

    /**
     * Closes a window.
     * @param oper Window operator.
     */
    public void close(ComponentOperator oper);

    /**
     * Change window location.
     * @param oper Window operator.
     * @param x New x coordinate
     * @param y New y coordinate
     */
    public void move(ComponentOperator oper, int x, int y);

    /**
     * Change window size.
     * @param oper Window operator.
     * @param width New window width.
     * @param height New window height.
     */
    public void resize(ComponentOperator oper, int width, int height);
}
