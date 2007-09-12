/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
