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

package org.netbeans.modules.soa.mapper.common.ui.palette;

import java.beans.PropertyChangeListener;

/**
 * PaletteManager interface class for accessing Palette dialog, categories,
 * and functoid items.
 *
 * @author Tientien Li
 */
public interface IPaletteManager {

    /** Field PROP_MODE           */
    public static final String PROP_MODE = "Palette_mode";

    /** Field PROP_SELECTEDITEM           */
    public static final String PROP_SELECTEDITEM = "Palette_selectedItem";

    /** Field PROP_CHECKEDITEM           */
    public static final String PROP_CHECKEDITEM = "Palette_checkedItem";

    /** Field PROP_CHECKEDITEM           */
    public static final String PROP_UNCHECKEDITEM = "Palette_uncheckedItem";

    /** Field PROP_INITIALIZED           */
    public static final String PROP_INITIALIZED = "Palette_initialized";

    /**
     * Set the folder of this palette manager going to initialize from.
     *
     * @param folderName the folder where the data reading from.
     */
    void setFolder (String folderName);

    /**
     * Method isInitialized
     *
     *
     * @return true if initialization complete
     *
     */
    boolean isInitialized();

    /**
     * get All Categories
     *
     *
     * @return the array of all palette categories
     *
     */
    IPaletteCategory[] getAllCategories();

    /**
     * get Selected Category
     *
     *
     * @return the selected category
     *
     */
    IPaletteCategory getSelectedCategory();

    /**
     * show Dialog
     *
     *
     */
    void showDialog();

    /**
     * show Dialog with the specific category tab selected
     *
     *
     * @param category the selected category
     *
     */
    void showDialog(IPaletteCategory category);

    /**
     * add Property ChangeListener
     *
     *
     * @param l the property change listener
     *
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * remove Property ChangeListener
     *
     *
     * @param l the property change listener
     *
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * get Category Items
     *
     *
     * @param category the selected category
     *
     * @return the array of category items
     *
     */
    IPaletteItem[] getCategoryItems(IPaletteCategory category);

    /**
     * get Selected Item Indices in a category
     *
     *
     * @param category the selected category
     *
     * @return the array of selected item indices
     *
     */
    int[] getCategorySelectedItemIndices(IPaletteCategory category);

    /**
     * select All items within a category
     *
     *
     * @param category the selected category
     *
     */
    void selectAll(IPaletteCategory category);

    /**
     * clear All items within a category
     *
     *
     * @param category the selected category
     *
     */
    void clearAll(IPaletteCategory category);

    /**
     * select an Item within a category
     *
     *
     * @param category the selected category
     * @param item the selected item
     *
     */
    void selectItem(IPaletteCategory category, IPaletteItem item);

    /**
     * clear an Item within a category
     *
     *
     * @param category the selected category
     * @param item the selected item
     *
     */
    void clearItem(IPaletteCategory category, IPaletteItem item);

    /**
     *  set the current application Frame.
     *
     * @param component   the application component
     */
    void setFrame(java.awt.Component component);
}
