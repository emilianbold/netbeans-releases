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

package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceLibraryCustomizer {
    
    private PersistenceLibraryCustomizer() {
    }
    
    public static boolean showCustomizer() {
        LibraryImplementation libImpl = LibrariesSupport.createLibraryImplementation(PersistenceLibrarySupport.LIBRARY_TYPE, PersistenceLibrarySupport.VOLUME_TYPES);
        PersistenceLibraryPanel customizer = new PersistenceLibraryPanel(libImpl);
        final DialogDescriptor descriptor = new DialogDescriptor(customizer,NbBundle.getMessage(PersistenceLibraryCustomizer.class, "TXT_PersistenceLibrariesManager"));
        customizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PersistenceLibraryPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        descriptor.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        customizer.checkValidity();
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                customizer.apply();
                return true;
            }
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return false;
    }
    
}
