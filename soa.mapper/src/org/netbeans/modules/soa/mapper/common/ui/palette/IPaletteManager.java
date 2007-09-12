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

package org.netbeans.modules.soa.mapper.common.ui.palette;

import java.beans.PropertyChangeListener;

/**
 * PaletteManager interface class for accessing Palette dialog, categories,
 * and functoid items.
 *
 * @author Tientien Li
 */
public interface IPaletteManager {

    /** Field PROP_MODE           */
    public static final String PROP_MODE = "Palette_mode";

    /** Field PROP_SELECTEDITEM           */
    public static final String PROP_SELECTEDITEM = "Palette_selectedItem";

    /** Field PROP_CHECKEDITEM           */
    public static final String PROP_CHECKEDITEM = "Palette_checkedItem";

    /** Field PROP_CHECKEDITEM           */
    public static final String PROP_UNCHECKEDITEM = "Palette_uncheckedItem";

    /** Field PROP_INITIALIZED           */
    public static final String PROP_INITIALIZED = "Palette_initialized";

    /**
     * Set the folder of this palette manager going to initialize from.
     *
     * @param folderName the folder where the data reading from.
     */
    void setFolder (String folderName);

    /**
     * Method isInitialized
     *
     *
     * @return true if initialization complete
     *
     */
    boolean isInitialized();

    /**
     * get All Categories
     *
     *
     * @return the array of all palette categories
     *
     */
    IPaletteCategory[] getAllCategories();

    /**
     * get Selected Category
     *
     *
     * @return the selected category
     *
     */
    IPaletteCategory getSelectedCategory();

    /**
     * show Dialog
     *
     *
     */
    void showDialog();

    /**
     * show Dialog with the specific category tab selected
     *
     *
     * @param category the selected category
     *
     */
    void showDialog(IPaletteCategory category);

    /**
     * add Property ChangeListener
     *
     *
     * @param l the property change listener
     *
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * remove Property ChangeListener
     *
     *
     * @param l the property change listener
     *
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * get Category Items
     *
     *
     * @param category the selected category
     *
     * @return the array of category items
     *
     */
    IPaletteItem[] getCategoryItems(IPaletteCategory category);

    /**
     * get Selected Item Indices in a category
     *
     *
     * @param category the selected category
     *
     * @return the array of selected item indices
     *
     */
    int[] getCategorySelectedItemIndices(IPaletteCategory category);

    /**
     * select All items within a category
     *
     *
     * @param category the selected category
     *
     */
    void selectAll(IPaletteCategory category);

    /**
     * clear All items within a category
     *
     *
     * @param category the selected category
     *
     */
    void clearAll(IPaletteCategory category);

    /**
     * select an Item within a category
     *
     *
     * @param category the selected category
     * @param item the selected item
     *
     */
    void selectItem(IPaletteCategory category, IPaletteItem item);

    /**
     * clear an Item within a category
     *
     *
     * @param category the selected category
     * @param item the selected item
     *
     */
    void clearItem(IPaletteCategory category, IPaletteItem item);

    /**
     *  set the current application Frame.
     *
     * @param component   the application component
     */
    void setFrame(java.awt.Component component);
}
