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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.util;

import javax.swing.ComboBoxModel;

/**
 * Serves as a model for {@link DataComboBoxSupport}.
 *
 * @author Andrei Badea
 */
public interface DataComboBoxModel {

    /**
     * Returns the combo box model; cannot be null.
     */
    ComboBoxModel getListModel();

    /**
     * Returns the display name for the given item. The given item
     * is one of the items in the model returned by {@link #getListModel}.
     */
    String getItemDisplayName(Object item);

    /**
     * Returns the tooltip text for the given item. The given item
     * is one of the items in the model returned by {@link #getListModel}.
     */
    String getItemTooltipText(Object item);

    /**
     * Returns the text for the "Add item" item (used to add new items 
     * to the combo box).
     */
    String getNewItemDisplayName();

    /**
     * Invoked when the "Add item" is selected. This method should do 
     * whatever is necessary to retrieve the new item to be added (e.g.
     * by prompting the user) and add the new item to {@link #getListModel},
     * firing a contentsChanged event.
     */
    void newItemActionPerformed();
}
