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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class BranchesMenu extends AbstractAction implements DynamicMenuContent {

    public BranchesMenu() {
        super(NbBundle.getMessage(BranchesMenu.class, "CTL_MenuItem_BranchesMenu"));
    }

    public JComponent[] getMenuPresenters() {
        return new JComponent [] { createMenu() };
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return new JComponent [] { createMenu() };
    }

    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent ev) {
        // no operation
    }

    private JMenu createMenu() {
        JMenu menu = new JMenu(this);
        menu.add(new Actions.MenuItem(SystemAction.get(BranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(SwitchBranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(MergeBranchAction.class), true));
        menu.setMnemonic(NbBundle.getMessage(BranchesMenu.class, "MNE_MenuItem_BranchesMenu").charAt(0));
        return menu;
    }
}
