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


package org.netbeans.spi.palette;

import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * <p>An interface implemented by palette clients to provide custom actions
 * for popup menus and actions for import of new items.</p>
 *
 * @author S. Aubrecht.
 */
public abstract class PaletteActions {

    /**
     * @return An array of action that will be used to construct buttons for import
     * of new palette item in palette manager window.
     *
     */
    public abstract Action[] getImportActions();
    
    /**
     * @return Custom actions to be added to the top of palette's default popup menu.
     */
    public abstract Action[] getCustomPaletteActions();
    
    /**
     * @param category Lookup representing palette's category.
     *
     * @return Custom actions to be added to the top of default popup menu for the given category.
     */
    public abstract Action[] getCustomCategoryActions( Lookup category );
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return Custom actions to be added to the top of the default popup menu for the given palette item.
     */
    public abstract Action[] getCustomItemActions( Lookup item );
    
    
    /**
     * @param item Lookup representing palette's item.
     *
     * @return An action to be invoked when user double-clicks the item in the
     * palette (e.g. insert item at editor's default location).
     * Return null to disable preferred action for this item.
     */
    public abstract Action getPreferredAction( Lookup item );
    
    /**
     * An action that will be invoked as part of the palette refresh logic,
     * for example when user chooses "Refresh" in palette's popup menu. Can be null.
     * The action properties (label, icon) are not displayed to the user, the Palette module
     * will provide its own.
     * @return Custom refresh action or null.
     * @since 1.9
     */
    public Action getRefreshAction() {
        return null;
}
}
