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

package org.netbeans.modules.git.remote.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.git.remote.Annotator;
import org.netbeans.modules.git.remote.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.remote.ui.diff.ExportUncommittedChangesAction;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for export actions.
 *
 */
public final class PatchesMenu extends DynamicMenu {
    private final ActionDestination dest;
    private final Lookup lkp;

    @NbBundle.Messages({
        "CTL_MenuItem_ExportMenu=&Patches",
        "CTL_MenuItem_ExportMenu.popup=Patches",
        "CTL_PopupName.PatchAction=Apply Diff Patch..."
    })
    public PatchesMenu (ActionDestination dest, Lookup lkp) {
        super(dest.equals(ActionDestination.MainMenu) 
                ? Bundle.CTL_MenuItem_ExportMenu()
                : Bundle.CTL_MenuItem_ExportMenu_popup());
        this.dest = dest;
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        Action patchAction = getPatchAction();
        if (dest.equals(ActionDestination.MainMenu)) {
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(ExportUncommittedChangesAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ExportCommitAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            if (patchAction != null) {
                menu.addSeparator();
                item = new JMenuItem();
                Actions.connect(item, patchAction, false);
                menu.add(item);
            }
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportUncommittedChangesAction.class), NbBundle.getMessage(ExportUncommittedChangesAction.class, "LBL_ExportUncommittedChangesAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(ExportCommitAction.class), NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            if (patchAction != null) {
                menu.addSeparator();
                item = menu.add(SystemActionBridge.createAction(patchAction, Bundle.CTL_PopupName_PatchAction(), lkp));
                org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            }
        }        
        return menu;
    }

    private Action getPatchAction () {
        return Actions.forID("Tools", "org.netbeans.modules.diff.PatchAction"); //NOI18N
    }
}
