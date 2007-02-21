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

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;

/**
 * <p>
 *
 * Title: </p> IPaletteViewObjectFactory<p>
 *
 * Description: </p> IPaletteViewObjectFactory provides interfaces to define the
 * implemenation of the IPaletteViewItem for the mapper palette view.<p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public interface IPaletteViewObjectFactory {

    /**
     * Return a newly create Palette view item by the specified palette item
     * from Palette manager.
     *
     * @param item  the palette item model
     * @return      a newly create Palette view item by the specified palette
     *      item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteItem(IPaletteItem item);

    /**
     * Return a newly create palette category view item by the specified palette
     * category item from Palette manager.
     *
     * @param category  Description of the Parameter
     * @return          a newly create palette category view item by the
     *      specified palette category item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteCategoryItem(IPaletteCategory category);

    /**
     * Return a palette view item that performs auto layout for this mapper.
     *
     * @return   a palette view item that performs auto layout for this mapper.
     */
    public IPaletteViewItem createAutoLayoutItem();

    /**
     * Return a palette view item that performs expand all group nodes for this mapper.
     *
     * @return   a palette view item that performs expand all group nodes for this mapper.
     */
    public IPaletteViewItem createExpandAllNodesItem();

    /**
     * Return a palette view item that performs collapse all group nodes for this mapper.
     *
     * @return   a palette view item that performs collapse all group nodes for this mapper.
     */
    public IPaletteViewItem createCollapseAllNodesItem();
    
    /**
     * Return a palette view item that performs collapse all group nodes for this mapper.
     *
     * @return   a palette view item that performs collapse all group nodes for this mapper.
     */
    public IPaletteViewItem createDeleteSelectedNodesItem();
}
