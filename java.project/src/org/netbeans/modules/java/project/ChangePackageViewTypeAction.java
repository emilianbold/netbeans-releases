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

package org.netbeans.modules.java.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Popup menu in Projects tab permitting you to change the package view type.
 * @author Jesse Glick
 */
public final class ChangePackageViewTypeAction extends AbstractAction implements Presenter.Popup {
    
    public ChangePackageViewTypeAction() {}

    public void actionPerformed(ActionEvent e) {
        assert false : e;
    }

    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, NbBundle.getMessage(ChangePackageViewTypeAction.class, "LBL_change_package_type"));
        menu.add(createChoice(PackageViewSettings.TYPE_PACKAGE_VIEW, NbBundle.getMessage(ChangePackageViewTypeAction.class, "ChangePackageViewTypeAction_list")));
        menu.add(createChoice(PackageViewSettings.TYPE_TREE, NbBundle.getMessage(ChangePackageViewTypeAction.class, "ChangePackageViewTypeAction_tree")));
        return menu;
    }
    
    private JMenuItem createChoice(final int type, String label) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        Mnemonics.setLocalizedText(item, label);
        item.setSelected(PackageViewSettings.getDefault().getPackageViewType() == type);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PackageViewSettings.getDefault().setPackageViewType(type);
            }
        });
        return item;
    }
    
}
