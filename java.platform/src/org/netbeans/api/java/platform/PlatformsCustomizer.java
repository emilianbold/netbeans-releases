/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.platform;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.*;

public final class PlatformsCustomizer {

    private PlatformsCustomizer () {

    }


    /**
     * Shows platforms customizer
     * @param  platform which should be seelcted, may be null
     * @return boolean for future extension, currently always true
     */
    public static boolean showCustomizer (JavaPlatform platform) {
        org.netbeans.modules.java.platform.ui.PlatformsCustomizer  customizer =
                new org.netbeans.modules.java.platform.ui.PlatformsCustomizer (platform);
        javax.swing.JButton close = new javax.swing.JButton(NbBundle.getMessage(PlatformsCustomizer.class,"CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PlatformsCustomizer.class,"AD_Close"));
        DialogDescriptor descriptor = new DialogDescriptor (customizer,NbBundle.getMessage(PlatformsCustomizer.class,
                "TXT_PlatformsManager"), true, new Object[] {close},close,DialogDescriptor.DEFAULT_ALIGN, new HelpCtx (PlatformsCustomizer.class),null); // NOI18N
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog (descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }
        return true;
    }

}
