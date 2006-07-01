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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
