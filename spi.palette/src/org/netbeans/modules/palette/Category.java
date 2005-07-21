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

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Interface representing palette category.
 *
 * @author S. Aubrecht
 */
public interface Category {
    
    /**
     * Category's internal name (id).
     */
    String getName();
    
    /**
     * Category's display name - user editable.
     */
    String getDisplayName();
    
    /**
     * Short description for tooltips.
     */
    String getShortDescription();
    
    /**
     * Icon
     */
    Image getIcon(int type);
    
    /**
     * Actions for category's popup menu.
     */
    Action[] getActions();

    /**
     * Category items.
     */
    Item[] getItems();
    
    void addCategoryListener( CategoryListener listener );
    
    void removeCategoryListener( CategoryListener listener );
    
    Transferable getTransferable();
    
    Lookup getLookup();
    
    boolean moveItem( Item source, Item target, boolean moveBefore );
    
    boolean dropItem( Transferable dropItem, int dndAction, Item target, boolean dropBefore );
    
    boolean dragOver( DropTargetDragEvent e );
}
