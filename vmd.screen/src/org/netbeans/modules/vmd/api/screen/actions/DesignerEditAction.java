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
        if (document == null) {
            return enabled;
        }
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
