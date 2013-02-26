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

package org.netbeans.modules.versioning.system.cvss.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.system.cvss.Annotator;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.BranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.MergeBranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.SwitchBranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.TagAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for copy actions.
 *
 * @author Ondra
 */
public final class BranchMenu extends DynamicMenu {
    private final ActionDestination dest;
    private final Lookup lkp;

    @NbBundle.Messages({
        "CTL_MenuItem_BranchMenu=&Branch/Tag",
        "CTL_MenuItem_BranchMenu.popup=Branch/Tag"
    })
    public BranchMenu (ActionDestination dest, Lookup lkp) {
        super(dest.equals(ActionDestination.MainMenu) 
                ? Bundle.CTL_MenuItem_BranchMenu()
                : Bundle.CTL_MenuItem_BranchMenu_popup());
        this.dest = dest;
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (dest.equals(ActionDestination.MainMenu)) {
            item = new JMenuItem();
            Action action = (Action) SystemAction.get(BranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            action = (Action) SystemAction.get(SwitchBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            item = new JMenuItem();
            Actions.connect(item, action, false);
            menu.add(item);
            
            action = (Action) SystemAction.get(MergeBranchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            item = new JMenuItem();
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            action = (Action) SystemAction.get(TagAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            item = new JMenuItem();
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(BranchAction.class), NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_Branch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SwitchBranchAction.class), NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_SwitchBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(MergeBranchAction.class), NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_MergeBranch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(TagAction.class), NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_Tag"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

}
