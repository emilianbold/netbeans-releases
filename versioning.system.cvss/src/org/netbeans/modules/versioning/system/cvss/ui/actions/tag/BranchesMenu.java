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

import javax.swing.event.ChangeListener;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.awt.Actions;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class BranchesMenu extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    public BranchesMenu() {
        super(NbBundle.getMessage(BranchesMenu.class, "CTL_MenuItem_BranchesMenu"));
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
	// change listener seems to be the only thing to work correctly on macosx.
	// popup listener is not..
        menu.getModel().addChangeListener(new Change(menu));
	
        menu.setMnemonic(NbBundle.getMessage(BranchesMenu.class, "MNE_MenuItem_BranchesMenu").charAt(0));
        return menu;
    }

    private static void fillMenu(JPopupMenu menu) {
        menu.removeAll();
        menu.add(new Actions.MenuItem(SystemAction.get(BranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(SwitchBranchAction.class), true));
        menu.add(new Actions.MenuItem(SystemAction.get(MergeBranchAction.class), true));
    }


    private static class Change implements ChangeListener {
	private JMenu menu;
	
	public Change(JMenu menu) {
	    this.menu = menu;
	}
        public void stateChanged(javax.swing.event.ChangeEvent e) {
	    fillMenu(menu.getPopupMenu());
        }
    }
    
}

