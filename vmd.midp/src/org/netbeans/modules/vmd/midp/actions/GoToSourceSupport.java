/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                        final JEditorPane pane = panes[0];
                        pane.setVisible(true);
                        Iterable<GuardedSection> iterable = GuardedSectionManager.getInstance(document).getGuardedSections();
                        
                        for (final GuardedSection section : iterable) {
                            if (presenter[0].matches(section)) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        pane.setCaretPosition(section.getCaretPosition().getOffset());
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
}