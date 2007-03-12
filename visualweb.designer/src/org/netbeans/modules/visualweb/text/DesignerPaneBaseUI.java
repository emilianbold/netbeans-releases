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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.text;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.plaf.ComponentUI;


/**
 * Text editor user interface
 *
 * @author  Timothy Prinzing
 * @version 1.31 01/23/03
 */
public abstract class DesignerPaneBaseUI extends ComponentUI {
//    /**
//     * Converts the given location in the model to a place in
//     * the view coordinate system.
//     *
//     * @param pos  the local location in the model to translate >= 0
//     * @return the coordinates as a rectangle
//     * @exception BadLocationException  if the given position does not
//     *   represent a valid location in the associated document
//     */
//    public abstract Rectangle modelToView(/*DesignerPaneBase t,*/ Position pos);
//
//    /**
//     * Converts the given place in the view coordinate system
//     * to the nearest representative location in the model.
//     *
//     * @param pt  the location in the view to translate.  This
//     *   should be in the same coordinate system as the mouse
//     *   events.
//     * @return the offset from the start of the document >= 0
//     */
//    public abstract Position viewToModel(DesignerPaneBase t, Point pt);

    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST,
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
    public abstract Position getNextVisualPositionFrom(DesignerPaneBase t, Position pos,
        int direction);
}
