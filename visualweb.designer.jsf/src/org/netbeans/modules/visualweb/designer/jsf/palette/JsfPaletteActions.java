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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.designer.jsf.palette;

import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.util.Lookup;

/**
 *
 * @author joelle
 */

// XXX Moved from designer/DesignerPaletteActions.
/**
 * <code>PaletteActions</code> used by jsf palette controller.
 *
 * @author Joelle Lam
 */
public class JsfPaletteActions extends PaletteActions {

    String paletteFolderName;

    /** Creates a new instance of FormPaletteProvider */
    public JsfPaletteActions(String paletteFolderName) {
        this.paletteFolderName = paletteFolderName;
    }

    public Action[] getImportActions() {
        return new Action[]{};
    }

    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomItemActions(Lookup item) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    public Action getPreferredAction(Lookup item) {
        //This is the double click action.
        return new CreatorPaletteInsertAction(item);
    }
}
