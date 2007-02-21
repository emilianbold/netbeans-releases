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

import java.awt.Component;

import java.util.Collection;
import java.util.ResourceBundle;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;

/**
 * <p>
 *
 * Title: IPaletteView </p> <p>
 *
 * Description: Describe a generic palette view of the mapper. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public interface IPaletteView {

    /**
     * Return the view manager of the mapper.
     *
     * @return   the view manager of the mapper.
     */
    public IBasicViewManager getViewManager();

    /**
     * Set the palette manager for this palette.
     *
     * @param paletteMgr  the palette manager for this palette.
     */
    public void setPaletteManager(IPaletteManager paletteMgr);

    /**
     * Return the palette manager for this palette.
     *
     * @return   the palette manager for this palette.
     */
    public IPaletteManager getPaletteManager();

    /**
     * Return the resource bundle to be used for the palette items.
     *
     * @return   the resource bundle to be used for the palette items.
     */
    public ResourceBundle getBundle();

    /**
     * Set the resource bundle to be used for the palette item.
     *
     * @param bundle  the resource bundle to be used for the palette item.
     * @param loader  the class loader to load the bundle resources.
     */
    public void setBundle(ResourceBundle bundle, Class loader);

    /**
     * Return the AWT component as the viewiable object of this palette view.
     *
     * @return the AWT component as the viewiable object of this palette view.
     */
    public Component getViewComponent();
    
    /**
     * Return the component representing the palette.
     */
    public Component getPaletteComponent();
    
    /**
     * Return an optional periphery component for the given constraints.
     */
    public Component getPeripheryComponent(Object constraints);
    
    /**
     * Set an optional periphery component, at the location given by the
     * following constraints.
     * The constraints must be one of BorderLayout.EAST|WEST|NORTH|SOUTH
     */
    public void setPeripheryComponent(Component component, Object constraints);
    
    /**
     * Add a IPaletteViewItem to this palette view.
     *
     * @param item  the IPaletteViewItem to be added.
     */
    public void addItem(IPaletteViewItem item);

    /**
     * Remove a IPaletteViewItem from this palette view.
     *
     * @param item  the IPaletteViewItem to be removed.
     */
    public void removeItem(IPaletteViewItem item);

    /**
     * Return the number of item in this palette view.
     *
     * @return   the number of item in this palette view.
     */
    public int getItemCount();

    /**
     * Return all the items in a collection object.
     *
     * @return   all the items in a collection object.
     */
    public Collection getAllItems();

    /**
     * Set the palette view object factory to create palette view item.
     *
     * @param factory  the factory of this palette view.
     */
    public void setFactory(IPaletteViewObjectFactory factory);

    /**
     * Return the palette view object factory to create palette view item.
     *
     * @return   the palette view object factory to create palette view item.
     */
    public IPaletteViewObjectFactory getFactory();

    /**
     * Find and return the palette view item repersenting the specified palette
     * item.
     *
     * @param item  the palette item to be matched.
     * @return      the palette view item repersenting the specified palette
     *      item.
     */
    public IPaletteViewItem findPaletteViewItem(IPaletteItem item);
    
    /**
     * Find and return the IMethoid repersenting the specified palette
     * item in canvas. This method is preferred when need to find
     *what IMethoid operator is needs to be created on canvas.
     * This method is not tied to GUI palette and so does not
     *have timing issues and palettte initialize issues as with
     *findPaletteViewItem(IPaletteItem item)
     *
     * @param item  the palette item to be matched.
     * @return      the palette view item repersenting the specified palette
     *      item.
     */
    public IMethoid findMethoid(IPaletteItem item);

    /**
     * Add a sparator in between palette items.
     */
    public void addItemSeparator();

    /**
     * Return true if the user selected a palette item from dialog should
     * request the coorsponding new Methoid in the mapper, false otherwise.
     *
     * @return   true if the user selected a palette item from dialog should
     *      request the coorsponding new Methoid in the mapper, false otherwise.
     */
    public boolean getSelectedOnRequest();

    /**
     * Sets if the user selected a palette item from dialog should request the
     * coorspending new Methoid in the mapper.
     *
     * @param selectedOnRequest  set to true if the user selected a palette item
     *      from dialog should request the coorspending new Methoid in the
     *      mapper, false otherwise.
     */
    public void setSelectedOnRequest(boolean selectedOnRequest);


    /**
     * Close this palette view, release any system resource.
     */
    public void close();
}
