/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.spi.palette;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 * <p>An abstract class implemented by palette clients to implement drag and drop
 * of new items into the palette window and to customize the default Transferable 
 * instance of items being dragged from the palette window to editor area.</p>
 *
 * <p>Client's can support multiple DataFlavors that may help to enable/disable the drop
 * when dragging an item over different editor area parts that allow only certain
 * item types to be dropped into them.</p>
 *
 * <p><b>Important: This SPI is still under development.</b></p>
 *
 * @author S. Aubrecht
 */
public abstract class DragAndDropHandler {

    /**
     * Add your own custom DataFlavor as need to suppor drag-over a different
     * parts of editor area.
     *
     * @param t Item's default Transferable.
     * @param item Palette item's Lookup.
     *
     */
    public abstract void customize( ExTransferable t, Lookup item );
    
    /**
     * @param targetCategory Lookup of the category under the drop cursor.
     * @param flavors Supported DataFlavors.
     * @param dndAction Drop action type.
     *
     * @return True if the given category can accept the item being dragged.
     */
    public abstract boolean canDrop( Lookup targetCategory, DataFlavor[] flavors, int dndAction );
    
    /**
     * Perform the drop operation and add the dragged item into the given category.
     *
     * @param targetCategory Lookup of the category that accepts the drop.
     * @param item Transferable holding the item being dragged.
     * @param dndAction Drag'n'drop action type.
     * @param dropIndex Zero-based position where the dragged item should be dropped.
     *
     * @return True if the drop has been successful, false otherwise.
     */
    public abstract boolean doDrop( Lookup targetCategory, Transferable item, int dndAction, int dropIndex );
}
