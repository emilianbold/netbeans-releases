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

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.util.*;

/**
 * An interface for palette contents.
 *
 * @author S. Aubrecht
 */
public interface Model {
    
    /**
     * Interested parties should listent to changes of this property to be 
     * notified when the selected item has changed.
     */
    public static final String PROP_SELECTED_ITEM = "selectedItem";
    
    String getName();
    
    /**
     * @return Palette categories.
     */
    Category[] getCategories();
            
    /**
     * @return Actions for palette's popup menu.
     */
    Action[] getActions();
    
    void addModelListener( ModelListener listener );
    
    void removeModelListener( ModelListener listener );
    
    /**
     * @return The item currently selected in the palette or null if no item is selected.
     */
    Item getSelectedItem();
    
    /**
     * @return The category that owns the currently selected item.
     */
    Category getSelectedCategory();
    
    /**
     * Select new item and category.
     *
     * @param category New category to be selected or null.
     * @param item New item to be selected or null.
     */
    void setSelectedItem( Lookup category, Lookup item );
    
    /**
     * Ensure no item is selected.
     */
    void clearSelection();
    
    void refresh();
    
    void showCustomizer( Settings settings );
    
    Lookup getRoot();
    
    boolean moveCategory( Category source, Category target, boolean moveBefore );
}
