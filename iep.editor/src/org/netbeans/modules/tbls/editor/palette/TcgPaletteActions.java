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


package org.netbeans.modules.tbls.editor.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.util.Lookup;

/*
 * TcgPaletteActions.java
 *
 * Created on November 20, 2005, 1:54 PM
 *
 * @author Bing Lu
 */
public class TcgPaletteActions extends PaletteActions {
    
    /** Creates a new instance of FormPaletteProvider */
    public TcgPaletteActions() {
    }

    /**
     * @return An array of action that will be used to construct buttons for import
     * of new palette item in palette manager window.
     *
     */
    public Action[] getImportActions() {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param category Lookup representing palette's category.
     *
     * @return Custom actions to be added to the top of default popup menu for the given category.
     */
    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param item Lookup representing palette's item.
     *
     * @return Custom actions to be added to the top of the default popup menu for the given palette item.
     */
    public Action[] getCustomItemActions(Lookup item) {
        return new Action[0]; //TODO implement this
    }

    /**
     * @return Custom actions to be added to the top of palette's default popup menu.
     */
    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    /**
     * @param item Lookup representing palette's item.
     *
     * @return An action to be invoked when user double-clicks the item in the
     * palette (e.g. insert item at editor's default location).
     * Return null to disable preferred action for this item.
     */
    public Action getPreferredAction( Lookup item ) {
        return null;
    }
}