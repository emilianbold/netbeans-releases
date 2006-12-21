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

package org.netbeans.modules.vmd.api.model.presenters.actions;

import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Karol Harezlak
 */
public final class DeleteAction extends SystemAction {

    public static final String DISPLAY_NAME = NbBundle.getMessage(DeleteAction.class, "NAME_DeleteAction");
    
    public void actionPerformed(ActionEvent e) {
        final DesignDocument activeDocument = ActiveDocumentSupport.getDefault ().getActiveDocument ();
        if (activeDocument == null)
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                activeDocument.getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        DeleteSupport.invokeDirectUserDeletion (activeDocument);
                    }
                });
            }
        });
    }

    public boolean isEnabled() {
        final DesignDocument activeDocument = ActiveDocumentSupport.getDefault ().getActiveDocument ();
        if (activeDocument == null)
            return false;
        final boolean[] ret = new boolean[1];
        activeDocument.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                ret[0] = DeleteSupport.canDeleteAsUser (activeDocument);
            }
        });

        return ret[0];
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    public String getName() {
        return DISPLAY_NAME;
    }

}
