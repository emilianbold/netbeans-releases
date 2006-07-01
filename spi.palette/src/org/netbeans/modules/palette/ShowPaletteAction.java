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
            Utilities.loadImage("org/netbeans/modules/palette/resources/palette.png"))); // NOI18N
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

