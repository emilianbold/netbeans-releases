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

import java.awt.Image;

/**
 * PaletteItemNode interface class for accessing the attribute of a single
 * palette element.
 *
 * @author Tientien Li
 */
public interface IPaletteItem {

    /** Field Attribute Checked           */
    public static final String ATTR_CHECKED = "Checked";

    /**
     * get item Icon
     *
     *
     * @return item icon image
     *
     */
    Image getIcon();

    /**
     * get item Name
     *
     *
     * @return item name
     *
     */
    String getName();  // i18N localized...

    /**
     * get ToolTip
     *
     *
     * @return tool tip
     *
     */
    String getToolTip();  // i18N localized...

    /**
     * get Item Attribute value
     *
     *
     * @param attr the name of selected attribute
     *
     * @return the attribute value
     *
     */
    Object getItemAttribute(String attr);    // Attributes from XML
}
