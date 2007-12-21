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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewItem;
import org.openide.util.NbBundle;

/**
 * <p>
 *
 * Title: CollapseAllButton</p> <p>
 *
 * Description: CollapseAllButton provides a JButton for the palette view to
 * performs canvas collapse all nodes.<p>
 *
 * <p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public class CollapseAllButton
     extends JButton
     implements IPaletteViewItem, ActionListener {

    /**
     * Palette view that contains this button
     */
    private IPaletteView mView;

    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicMapperPalette.class.getName());

    /**
     * Constructor for the AutoLayoutButton object, with specified palette view
     * that contains this button.
     *
     * @param paletteView  palette view that contains this button
     */
    public CollapseAllButton(IPaletteView paletteView) {
        mView = paletteView;
        setIcon(new ImageIcon(CollapseAllButton.class.getResource("Collapse_All.png")));
        setToolTipText(NbBundle.getBundle(getClass()).getString("TOOLTIP_Palette_CollapseAll_Button"));
        addActionListener(this);
    }

    /**
     * Perform canvas auto layout.
     *
     * @param e  the action event of this button.
     */
    public void actionPerformed(ActionEvent e) {
        if (this.isEnabled() && this.isVisible()) {
            mView.getViewManager().getCanvasView().getCanvas().collapseAllNode();
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
        return null;
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
