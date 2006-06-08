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

package org.openide.windows;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * When an implementation of this class is available in the global Lookup and
 * an object is being dragged over some parts of the main window of the IDE then
 * the window system may call methods of this class to decide whether it can
 * accept or reject the drag operation. And when the object is actually dropped
 * into the IDE then this class will be asked to handle the drop.
 *
 * @since 6.7
 *
 * @author S. Aubrecht
 */
public abstract class ExternalDropHandler {

    /**
     * @return True if the dragged object can be dropped into the IDE, false
     * if the DataFlavor(s) are not supported.
     */
    public abstract boolean canDrop( DropTargetDragEvent e );

    /**
     * This method is called when the dragged object is already dropped to decide
     * whether the drop can be accepted.
     *
     * @return True if the dropped object is supported (i.e. handleDrop method
     * can process the object), false otherwise.
     */
    public abstract boolean canDrop( DropTargetDropEvent e );

    /**
     * When an object is dropped into the IDE this method must process it (e.g.
     * open the dropped file in a new editor tab).
     *
     * @return True if the dropped object was processed successfully, false otherwise.
     */
    public abstract boolean handleDrop( DropTargetDropEvent e );
}
