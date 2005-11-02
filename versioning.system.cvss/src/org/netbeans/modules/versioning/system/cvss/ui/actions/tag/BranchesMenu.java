/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
