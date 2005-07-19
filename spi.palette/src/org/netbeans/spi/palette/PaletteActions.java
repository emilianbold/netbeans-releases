/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.spi.palette;

import java.awt.event.InputEvent;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * <p>An interface implemented by palette clients to provide custom actions
 * for popup menus and actions for import of new items.</p>
 *
 * <p><b>Important: This SPI is still under development.</b></p>
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
}
