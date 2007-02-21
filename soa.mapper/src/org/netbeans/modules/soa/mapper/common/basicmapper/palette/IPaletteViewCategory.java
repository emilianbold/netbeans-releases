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

package org.netbeans.modules.soa.mapper.common.basicmapper.palette;

import java.util.Collection;

/**
 * IPaletteViewCategory repersents a group of view items together.
 *
 * @Created on Jun 8, 2004
 * @author sleong
 * @version 2.0
 */
public interface IPaletteViewCategory
    extends IPaletteViewItem {

    /**
     * Add a palette view item to this category
     *
     * @param item the new palette view item to add to this category
     */
    public void addViewItem (IPaletteViewItem item);

    /**
     * Insert a palette view item to this category.
     * 
     * @param item  the new palette view item to add to this category
     * @param index the position of the new palette view item.
     */
    public void insertViewItem (IPaletteViewItem item, int index);
    
    /**
     * Remove the palette view item from the specified index.
     * 
     * @param index the index of the palette view item to be removed.
     */
    public void removeViewItem (int index);

    /**
     * Remove the specified palette view item from this category.
     *  
     * @param item  the palette view item to be removed from this category.
     */
    public void removeViewItem (IPaletteViewItem item);
    
    /**
     * Return a collection of all the view items from this category.
     * 
     * @return a collection of all the view items from this category.
     */
    public Collection getViewItems();
    
    /**
     * Return the palette view item from the specified index. 
     * 
     * @param i the index to be search
     *
     * @return the palette view item from the specified index.
     */
    public IPaletteViewItem getViewItem (int i);
    
    /**
     * Return the position of the specified palette view item of a category.
     *   
     * @param item the palette view item to be search on. 
     * 
     * @return  the position of the specified palette view item of a category.
     */
    public int getViewItemIndex (IPaletteViewItem item);
    
    /**
     * Return the number of view items in this category.
     * 
     * @return the number of view items in this category.
     */
    public int getViewItemCount();
    
    public void close();
}
