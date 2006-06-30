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

