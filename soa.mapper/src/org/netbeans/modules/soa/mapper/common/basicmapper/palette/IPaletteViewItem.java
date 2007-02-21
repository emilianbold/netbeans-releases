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
import javax.swing.Icon;

/**
 * <p>
 *
 * Title: IPaletteViewItem </p> <p>
 *
 * Description: Describe a view on each palette item </p> <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public interface IPaletteViewItem {

    /**
     * Retrun the icon of the palette item
     *
     * @return   the icon of the palette item
     */
    public Icon getIcon();

    /**
     * Return the tooptip text of the palette item
     *
     * @return   the tooptip text of the palette item
     */
    public String getToolTipText();

    /**
     * Return the palette item in another form of object repersentation
     *
     * @return   the palette item in another form of object repersentation
     */
    public Object getItemObject();

    /**
     * Return the transferable object for drag and drop opertaion.
     *
     * @return   the transferable object for drag and drop opertaion.
     */
    public Object getTransferableObject();

    /**
     * Set the transferable object for drag and drop opertaion.
     *
     * @param obj  the transferable object for drag and drop opertaion.
     */
    public void setTransferableObject(Object obj);

    /**
     * Return true if the palette item is visible, false otherwise.
     *
     * @return   true if the palette item is visible, false otherwise.
     */
    public boolean isVisible();

    /**
     * Set the visiblility of this palette item.
     *
     * @param isVisible  true if this palette item is visible, false otherwise.
     */
    public void setVisible(boolean isVisible);

    /**
     * Return the Java AWT component as the viewiable object of this palette
     * view item.
     *
     * @return   the Java AWT component as the viewiable object of this palette
     *      view item.
     */
    public Component getViewComponent();
}
