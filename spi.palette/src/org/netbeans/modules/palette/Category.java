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
    
    boolean dropItem( Transferable dropItem, int dndAction, Item target, boolean dropBefore );
    
    boolean dragOver( DropTargetDragEvent e );
}
