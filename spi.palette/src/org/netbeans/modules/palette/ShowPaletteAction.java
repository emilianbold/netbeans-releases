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


package org.netbeans.modules.palette;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;



/** 
 * Opens Palette (Component Palette) TopComponent.
 *
 * @author S Aubrecht
 */
public class ShowPaletteAction extends AbstractAction {

    public ShowPaletteAction() {
        putValue(NAME, Utils.getBundleString("CTL_PaletteAction") );
        putValue(SMALL_ICON, new ImageIcon(
            Utilities.loadImage("org/netbeans/modules/form/resources/palette.png"))); // NOI18N
    }


    /** Opens component palette. */
    public void actionPerformed(ActionEvent evt) {
        // show ComponentPalette
        TopComponent palette = WindowManager.getDefault().findTopComponent("CommonPalette"); // NOI18N
        if( null == palette ) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                "Can not find CommonPalette component." )); // NOI18N
            return;
        }
        palette.open();
        palette.requestActive();
    }
}

