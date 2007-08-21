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

import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.screen.editor.EditedScreenSupport;
import org.netbeans.modules.vmd.screen.ScreenEditorView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Karol Harezlak
 */
public class DesignerEditAction extends SystemAction  {
    
    private boolean enabled;
    
    public void actionPerformed(ActionEvent e) {
        ProjectUtils.requestVisibility(ActiveViewSupport.getDefault().getActiveView().getContext(), ScreenEditorView.SCREEN_EDITOR_VIEW_DISPLAY_NAME);
        requestComponentVisibility();
    }
    
    public String getName() {
        return NbBundle.getMessage(DesignerEditAction.class, "NAME_EditAction"); //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public boolean isEnabled() {
        final DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        enabled = false;
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent component = getSelectedComponent(document);
                if (component == null)
                    return;
                EditDependencyPresenter presenter = component.getPresenter(EditDependencyPresenter.class);
                if (presenter == null)
                    return;
                enabled = presenter.isComponentEditable();
            }
        });
        
        return enabled;
    }
    
    protected void requestComponentVisibility() {
        final DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                final DesignComponent component = getSelectedComponent(document);
                if (component != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            document.getTransactionManager().readAccess(new Runnable() {
                                public void run() {
                                    EditedScreenSupport.getSupportForDocument(document).setEditedScreenComponentID (component.getComponentID());
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    protected DesignComponent getSelectedComponent(DesignDocument document) {
        if (document.getSelectedComponents().size() != 1)
            return null;
        DesignComponent component = document.getSelectedComponents().iterator().next();
        return component;
    }
}
