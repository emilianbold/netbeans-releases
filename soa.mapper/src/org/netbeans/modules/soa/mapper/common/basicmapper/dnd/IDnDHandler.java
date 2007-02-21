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

package org.netbeans.modules.soa.mapper.common.basicmapper.dnd;

import java.awt.Cursor;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetListener;

/**
 * <p>
 *
 * Title: </p>IDnDHandler <p>
 *
 * Description: </p>IDnDHandler provides convience object to override the dnd
 * on the mapper view component. <p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 */

public interface IDnDHandler {

    /**
     * Gets the dragGestureListener attribute of the IDnDHandler object
     *
     * @return   The dragGestureListener value
     */
    public DragGestureListener getDragGestureListener();

    /**
     * Gets the dragSourceListener attribute of the IDnDHandler object
     *
     * @return   The dragSourceListener value
     */
    public DragSourceListener getDragSourceListener();

    /**
     * Gets the dropTargetListener attribute of the IDnDHandler object
     *
     * @return   The dropTargetListener value
     */
    public DropTargetListener getDropTargetListener();

    /**
     * Return an int representing the type of action used in this Drag
     * operation.
     *
     * @return   an int representing the type of action used in this Drag
     *      operation. See <code>java.awt.dnd.DnDConstants</code> for a list of
     *      available drag actions.
     */
    public int getDragAction();

    /**
     * Return the cursor to use when start draging on the component of this
     * handler.
     *
     * @return   the cursor to use when start draging. See <code>java.awt.dnd.DragSource</code>
     *      for a list of avaiable drag cursor.
     */
    public Cursor getDragCursor();

    /**
     * Close this handler and release any system resource.
     */
    public void releaseHandler();
}
