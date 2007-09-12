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
