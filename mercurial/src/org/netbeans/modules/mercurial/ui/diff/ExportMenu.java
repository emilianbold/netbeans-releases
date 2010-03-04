/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mercurial.ui.diff;

import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.ui.pull.PullAction;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.push.PushOtherAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for export actions.
 *
 * @author Ondra
 */
public final class ExportMenu extends AbstractAction implements DynamicMenuContent {

    public ExportMenu() {
        super(NbBundle.getMessage(ExportMenu.class, "CTL_MenuItem_ExportMenu"));
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return new JComponent [] { createMenu() };
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return new JComponent [] { createMenu() };
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // no operation
    }

    private JMenu createMenu() {
        JMenu menu = new JMenu(this);
        org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(ExportMenu.class, "CTL_MenuItem_ExportMenu")); // NOI18N
        
        JMenuItem item = menu.add(new SystemActionBridge(SystemAction.get(ExportDiffAction.class), NbBundle.getMessage(ExportDiffAction.class, "CTL_MenuItem_ExportDiff"))); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());

        item = menu.add(new SystemActionBridge(SystemAction.get(ExportDiffChangesAction.class), NbBundle.getMessage(ExportDiffChangesAction.class, "CTL_MenuItem_ExportDiffChanges"))); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        
        item = menu.add(new SystemActionBridge(SystemAction.get(ExportBundleAction.class), NbBundle.getMessage(ExportBundleAction.class, "CTL_MenuItem_ExportBundle"))); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        
        return menu;
    }
}
