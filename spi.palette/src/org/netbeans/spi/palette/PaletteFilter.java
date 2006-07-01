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

import org.openide.util.Lookup;

/**
 * <p>A palette filter than can prevent some categories and/or items from being
 * displayed in palette's window.</p>
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
