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

package org.netbeans.modules.vmd.api.screen.actions;

import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.screen.editor.EditedScreenSupport;
import org.netbeans.modules.vmd.screen.ScreenAccessController;

/**
 *
 * @author Karol Harezlak
 */
public final class DesignerEditParentAction extends DesignerEditAction {
    
    public DesignerEditParentAction() {
    }
    
    protected void requestComponentVisibility() {
        final DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = getSelectedComponent(document);
                final DesignComponent parentComponent = component != null ? component.getParentComponent() : null;
                if (parentComponent != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            document.getTransactionManager().readAccess(new Runnable() {
                                public void run() {
                                    EditedScreenSupport.getSupportForDocument(document).setEditedScreenComponentID (parentComponent.getComponentID());
                                }
                            });
                        }
                    });
                }
            }
        });
    }

}
