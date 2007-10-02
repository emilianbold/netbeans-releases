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
package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.io.IOException;

/**
 *
 * @author Karol Harezlak
 */
public final class GoToSourceSupport {
    
    private GoToSourceSupport() {
    }

    public static void goToSourceOfComponent(final DesignComponent component) {
        if (component == null) {
            return;
        }

        final DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(component.getDocument());
        if (context == null) {
            return;
        }

        final CloneableEditorSupport[] editorSupport = new CloneableEditorSupport[1];
        final GoToSourcePresenter[] presenter = new GoToSourcePresenter[1];
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                presenter[0] = component.getPresenter(GoToSourcePresenter.class);
                if (presenter[0] != null) {
                    editorSupport[0] = context.getCloneableEditorSupport();
                }
            }
        });

        if (editorSupport[0] == null) {
            return;
        }

        // issue#116006
        IOSupport.forceUpdateCode(context.getDataObject());

        editorSupport[0].edit();

        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                StyledDocument document = null;
                try {
                    document = editorSupport[0].openDocument();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
                
                if (document != null) {
                    ProjectUtils.requestVisibility(ActiveViewSupport.getDefault().getActiveView().getContext(), ProjectUtils.getSourceEditorViewDisplayName());
                    JEditorPane[] panes = editorSupport[0].getOpenedPanes();
                    
                    if (panes != null && panes.length >= 1) {
                        JEditorPane pane = panes[0];
                        pane.setVisible(true);
                        Iterable<GuardedSection> iterable = GuardedSectionManager.getInstance(document).getGuardedSections();
                        
                        for (GuardedSection section : iterable) {
                            if (presenter[0].matches(section)) {
                                pane.setCaretPosition(section.getCaretPosition().getOffset());
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
}