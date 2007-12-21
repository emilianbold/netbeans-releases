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

package org.netbeans.modules.soa.mapper.basicmapper.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewItem;

/**
 * <p>
 *
 * Title: </p> CategoryButton <p>
 *
 * Description: </p> CategoryButton provides JButton for palette view to show
 * dialog of the specified category in the palette manager.<p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public class CategoryButton
     extends JButton
     implements IPaletteViewItem, ActionListener {

    /**
     * the categroy for this button
     */
    private IPaletteCategory mCategory;

    /**
     * the palette manager, the model
     */
    private IPaletteManager mModel;

    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicMapperPalette.class.getName());

    /**
     * Initialize a categroy button with the specified categroy
     *
     * @param category  the specified categroy of this button.
     * @param model     the palette manager of the palette view that contains
     *      this category button
     * @param icon      the icon of this category button
     */
    public CategoryButton(IPaletteCategory category, IPaletteManager model, Icon icon) {
        mCategory = category;
        mModel = model;
        if (icon != null) {
            setIcon(icon);
        } else {
            try {
                setIcon(new ImageIcon(CategoryButton.class.getResource("category_10x16.png")));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to find the default category icon file: category_10x16.png", e);
            }
        }
        setMargin(new Insets(0, 0, 0, 0));
        setBackground(Color.white);
        addActionListener(this);
    }

    /**
     * Show the PaletteManager dialog.
     *
     * @param e  the action event of this button.
     */
    public void actionPerformed(ActionEvent e) {
        if (this.isEnabled() && this.isVisible()) {
            mModel.showDialog(mCategory);
        }
    }

    /**
     * Return the Java AWT component as the viewiable object of this palette
     * view item.
     *
     * @return   the Java AWT component as the viewiable object of this palette
     *      view item.
     */
    public Component getViewComponent() {
        return this;
    }

    /**
     * Return the palette item in another form of object repersentation
     *
     * @return   the palette item in another form of object repersentation
     */
    public Object getItemObject() {
        return mCategory;
    }

    /**
     * Return null.
     *
     * @return   no drag and drop operation, alwayas return null.
     */
    public Object getTransferableObject() {
        return null;
    }

    /**
     * Set the transferable object for drag and drop opertaion, this methoid is
     * not applicable to this button.
     *
     * @param obj  the transferable object
     */
    public void setTransferableObject(Object obj) { }

}
