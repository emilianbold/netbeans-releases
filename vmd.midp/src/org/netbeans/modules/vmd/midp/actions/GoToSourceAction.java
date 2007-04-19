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

package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.vmd.api.model.DesignDocument;

/**
 *
 * @author Karol Harezlak
 */
public final class GoToSourceAction extends SystemAction {
    
    public static final String DISPLAY_NAME = NbBundle.getMessage(GoToSourceAction.class, "NAME_GoToSourceAction"); //NOI18N
    
    public void actionPerformed(ActionEvent e) {
        final DesignComponent activeComponent = getActiveComponent();
        if (activeComponent == null)
            return;
        final CloneableEditorSupport[] editorSupport = new CloneableEditorSupport[1];
        activeComponent.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                GoToSourcePresenter presenter = activeComponent.getPresenter(GoToSourcePresenter.class);
                if (presenter == null)
                    return;
                
                DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(activeComponent.getDocument());
                if (context == null)
                    return;
                editorSupport[0] = context.getCloneableEditorSupport();
            }
        });
        
        if (editorSupport[0] == null)
            return;
        editorSupport[0].edit();
        
        activeComponent.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                GoToSourcePresenter presenter = activeComponent.getPresenter(GoToSourcePresenter.class);
                if (presenter == null)
                    return;
                
                StyledDocument document;
                try {
                    document = editorSupport[0].openDocument();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                    return;
                }
                if (document == null)
                    return;
                JEditorPane[] panes = editorSupport[0].getOpenedPanes();
                if (panes == null || panes.length < 1)
                    return;
                JEditorPane pane = panes[0];
                
                Iterable<GuardedSection> iterable = GuardedSectionManager.getInstance(document).getGuardedSections();
                for (GuardedSection section : iterable) {
                    if (presenter.matches(section)) {
                        pane.setCaretPosition(section.getCaretPosition().getOffset());
                        final DesignDocument d = ActiveDocumentSupport.getDefault().getActiveDocument();
                        return;
                    }
                }
            }
        });
    }
    
    public String getName() {
        return DISPLAY_NAME;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public boolean isEnabled() {
        final DesignComponent activeComponent = getActiveComponent();
        if (activeComponent == null)
            return false;
        final boolean[] ret = new boolean[] { false };
        activeComponent.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                ret[0] = activeComponent.getPresenter(GoToSourcePresenter.class) != null;
            }
        });
        return ret[0];
    }
    
    private DesignComponent getActiveComponent() {
        Collection<DesignComponent> components = ActiveDocumentSupport.getDefault().getActiveComponents();
        if (components.size() == 1) {
            Iterator<DesignComponent> iterator = components.iterator();
            if (iterator.hasNext())
                return iterator.next();
        }
        return null;
    }
    
   
    
}
