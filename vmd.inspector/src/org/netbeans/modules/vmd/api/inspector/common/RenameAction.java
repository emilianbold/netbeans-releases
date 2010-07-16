/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.api.inspector.common;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Karol Harezlak
 */
/**
 * This class provides GUI to change DesignComponent display name and DesignComponent's 
 * property which keeps information about DesignCompoent name. It needs to be attache to the DesignComponent
 * through ActionsPresenter.
 */
public final class RenameAction extends SystemAction implements ActionContext {

    public static final String DISPLAY_NAME = NbBundle.getMessage(RenameAction.class, "NAME_RenameAction"); //NOI18N
    private NotifyDescriptor.InputLine descriptor;
    private boolean canRename;
    private WeakReference<DesignComponent> component;

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (component == null) {
                    return;
                }
                final DesignComponent c = component.get();
                if (c == null) {
                    return;
                }
                c.getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        InfoPresenter presenter = c.getPresenter(InfoPresenter.class);
                        if (presenter == null) {
                            Debug.warning("No necessary presenter for this operation - component: " + c); //NOI18N
                            return;
                        }
                        getDialogDescriptor().setInputText(presenter.getEditableName());
                        DialogDisplayer.getDefault().notify(getDialogDescriptor());
                        if (((Integer) descriptor.getValue()) == 0 && descriptor.getInputText().trim().length() > 0) {
                            presenter.setEditableName(descriptor.getInputText().trim());
                        }
                    }
                });
            }
        });
    }

    private NotifyDescriptor.InputLine getDialogDescriptor() {
        if (descriptor != null) {
            return descriptor;
        }

        descriptor = new NotifyDescriptor.InputLine(NbBundle.getMessage(RenameAction.class, "TITLE_RenameQuestion"), NbBundle.getMessage(RenameAction.class, "TITLE_RenameDialog")); //NOI18N

        return descriptor;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        if (component == null) {
            return false;
        }
        final DesignComponent c = component.get();
        if (c == null) {
            return false;
        }
        c.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                if (c.getDocument().getSelectedComponents().size() > 1) {
                    canRename = false;
                    return;
                }
                InspectorFolderPresenter presenter = c.getPresenter(InspectorFolderPresenter.class);
                if (presenter != null) {
                    canRename = presenter.getFolder().canRename();
                } else {
                    canRename = false;
                }
            }
        });

        return canRename;
    }

    public String getName() {
        return DISPLAY_NAME;
    }

    public void setComponent(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }
}
