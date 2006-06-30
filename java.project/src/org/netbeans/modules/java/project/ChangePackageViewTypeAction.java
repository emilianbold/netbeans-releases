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
