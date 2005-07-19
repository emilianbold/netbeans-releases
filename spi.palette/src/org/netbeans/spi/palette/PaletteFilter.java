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

import org.openide.util.Lookup;

/**
 * <p>A palette filter than can prevent some categories and/or items from being
 * displayed in palette's window.</p>
 *
 * <p><b>Important: This SPI is still under development.</b></p>
 *
 * @author S. Aubrecht
 */
public abstract class PaletteFilter {
        
    /**
     * @param lkp Lookup representing a palette category.
     * @return True if the category should be displayed in palette's window, false otherwise.
     */
    public abstract boolean isValidCategory( Lookup lkp );
    
    /**
     * @param lkp Lookup representing a palette item.
     * @return True if the item should be displayed in palette's window, false otherwise.
     */
    public abstract boolean isValidItem( Lookup lkp );
}
