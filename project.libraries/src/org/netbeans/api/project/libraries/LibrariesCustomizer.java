/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.project.libraries;


import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import java.awt.Dialog;

/** Provides method for opening Libraries customizer
 *
 */
public final class LibrariesCustomizer {

    private LibrariesCustomizer () {
    }

    /**
     * Shows libraries customizer
     * @param activeLibrary if not null the activeLibrary is selected in the opened customizer
     * @return true if user pressed OK and libraries were sucessfully modified
     */
    public static boolean showCustomizer (Library activeLibrary) {
        org.netbeans.modules.project.libraries.ui.LibrariesCustomizer  customizer =
                new org.netbeans.modules.project.libraries.ui.LibrariesCustomizer ();
        if (activeLibrary != null)
            customizer.setSelectedLibrary (activeLibrary.getLibraryImplementation ());
        DialogDescriptor descriptor = new DialogDescriptor (customizer,NbBundle.getMessage(LibrariesCustomizer.class,
                "TXT_LibrariesManager"));
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog (descriptor);
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                return customizer.apply();
            }
            else {
                customizer.cancel();
            }
        } finally {
            if (dlg != null)
                dlg.dispose();
        }
        return false;
    }
}

