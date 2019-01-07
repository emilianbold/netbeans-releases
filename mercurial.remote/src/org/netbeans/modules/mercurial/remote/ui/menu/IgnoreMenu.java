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

package org.netbeans.modules.mercurial.remote.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.remote.MercurialAnnotator;
import org.netbeans.modules.mercurial.remote.ui.commit.ExcludeFromCommitAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.remote.ui.ignore.IgnoreAction;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for ignore/exclude actions.
 *
 * 
 */
@Messages({
    "CTL_MenuItem_IgnoreMenu=&Ignore",
    "CTL_MenuItem_IgnoreMenu.popupName=Ignore"
})
public final class IgnoreMenu extends DynamicMenu implements Presenter.Popup {

    private final Lookup lkp;
    private final VCSContext ctx;

    public IgnoreMenu (Lookup lkp, VCSContext ctx) {
        super(Bundle.CTL_MenuItem_IgnoreMenu());
        this.lkp = lkp;
        this.ctx = ctx;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, Bundle.CTL_MenuItem_IgnoreMenu());
            item = new JMenuItem();
            Action action = SystemAction.get(IgnoreAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(ExcludeFromCommitAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Ignore"), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            ExcludeFromCommitAction exclude = SystemAction.get(ExcludeFromCommitAction.class);
            item = menu.add(SystemActionBridge.createAction(exclude, NbBundle.getMessage(MercurialAnnotator.class,
                    exclude.getActionStatus(ctx) == ExcludeFromCommitAction.INCLUDING
                            ? "CTL_PopupMenuItem_IncludeInCommit" //NOI18N
                            : "CTL_PopupMenuItem_ExcludeFromCommit", ctx), lkp, MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_IgnoreMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
