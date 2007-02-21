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

package org.netbeans.modules.soa.mapper.common.palette;

import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;

import org.openide.ErrorManager;
import org.openide.nodes.Index;

/**
 *  Popup menu for the functoid palette.
 *
 * @author Tientien Li
 */
class PalettePopupMenu extends JPopupMenu {

    /**
     * the palette root node
     */
    private PaletteNode mPalNode;

    /**
     * Constructor to create a Palette Popup Menu
     *
     *
     * @param palNode the root palette node
     *
     */
    public PalettePopupMenu(PaletteNode palNode) {

        mPalNode = palNode;

        javax.swing.JMenuItem menuItem;
        java.util.ResourceBundle bundle = PaletteManager.getBundle();

        menuItem = new javax.swing.JMenuItem(
                bundle.getString("CTL_CreateCategory")); // NOI18N

        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCategory();
            }
        });
        add(menuItem);
        addSeparator();

        menuItem = new javax.swing.JMenuItem(
                bundle.getString("CTL_OrderCategories")); // NOI18N

        menuItem.setEnabled(mPalNode.getCookie(Index.class) != null);
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reorderCategories();
            }
        });
        add(menuItem);
    }

    /**
     * reorder palette Categories on the list
     *
     *
     */
    private void reorderCategories() {

        Index order = (Index) mPalNode.getCookie(Index.class);

        if (order != null) {
            order.reorder();
        }
    }

    /**
     * create a new palette Category
     *
     *
     */
    private void createCategory() {

        try {
            mPalNode.createNewCategory();
        } catch (java.io.IOException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) {    // NOI18N
                e.printStackTrace(System.err);
            }

            ErrorManager.getDefault().notify(e);
        }
    }
}
