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

package org.netbeans.modules.mercurial.ui.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.ui.pull.FetchAction;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.ui.pull.PullOtherAction;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.push.PushOtherAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class ShareMenu extends DynamicMenu implements Presenter.Menu {
    private final Lookup lookup;

    public ShareMenu () {
        this(null);
    }
    
    public ShareMenu (Lookup lookup) {
        super(NbBundle.getMessage(ShareMenu.class, "CTL_MenuItem_ShareMenu"));
        this.lookup = lookup;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        if (lookup == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(ShareMenu.class, "CTL_MenuItem_ShareMenu")); // NOI18N

            JMenuItem item = menu.add(new SystemActionBridge(SystemAction.get(PushAction.class), SystemAction.get(PushAction.class).getName(), MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(new SystemActionBridge(SystemAction.get(PushOtherAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_PushOther"), MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(new SystemActionBridge(SystemAction.get(PullAction.class), SystemAction.get(PullAction.class).getName(), MercurialAnnotator.ACTIONS_PATH_PREFIX));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(new SystemActionBridge(SystemAction.get(PullOtherAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_PullOther"), MercurialAnnotator.ACTIONS_PATH_PREFIX)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        } else {
            JMenuItem item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Push"), lookup));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Pull"), lookup));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(FetchAction.class), NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_Fetch"), lookup));
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }
        return menu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = createMenu();
        org.openide.awt.Mnemonics.setLocalizedText(menu, menu.getText());
        enableMenu(menu);
        return menu;
    }
}
