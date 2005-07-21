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

import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.awt.Actions;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class BranchesMenu extends AbstractAction implements Presenter.Menu, Presenter.Popup, PopupMenuListener {

    private static final ResourceBundle loc = NbBundle.getBundle(BranchesMenu.class);

    public BranchesMenu() {
        super(loc.getString("CTL_MenuItem_BranchesMenu"));
    }

    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent ev) {
        // no operation
    }

    public JMenuItem getMenuPresenter() {
        return createMenu();
    }

    public JMenuItem getPopupPresenter() {
        return createMenu();
    }

    private JMenu createMenu() {
        JMenu menu = new JMenu(this);
        menu.getPopupMenu().addPopupMenuListener(this);
        menu.setMnemonic(loc.getString("MNE_MenuItem_BranchesMenu").charAt(0));
        return menu;
    }

    private void fillMenu(JPopupMenu menu) {
        menu.removeAll();
        menu.add(new Actions.MenuItem(SystemAction.get(BranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(SwitchBranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(MergeBranchAction.class), true));
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JPopupMenu menu = (JPopupMenu) e.getSource();
        fillMenu(menu);
    }
}

