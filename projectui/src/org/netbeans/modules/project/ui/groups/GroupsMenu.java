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

package org.netbeans.modules.project.ui.groups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Submenu listing available groups and offering some operations on them.
 * @author Jesse Glick
 */
public class GroupsMenu extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    private static final RequestProcessor RP = new RequestProcessor(GroupsMenu.class.getName());

    public GroupsMenu() {
        super(NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.label"));
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public JMenuItem getMenuPresenter() {
        return new Menu();
    }

    public JMenuItem getPopupPresenter() {
        return new Menu();
    }

    /**
     * The actual submenu (recreated each time it is displayed).
     */
    private static class Menu extends JMenu implements DynamicMenuContent {

        Menu() {
            Mnemonics.setLocalizedText(this, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.label"));
        }

        public JComponent[] getMenuPresenters() {
            // XXX can it wait to add menu items until it is posted?
            removeAll();
            final Group active = Group.getActiveGroup();
            // Create one menu item per group.
            for (final Group g : Group.allGroups()) {
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem(g.getName());
                if (g.equals(active)) {
                    mi.setSelected(true);
                    if (g.isPristine() && Group.isAdvancedMode()) {
                        mi.setEnabled(false);
                    }
                }
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Could be slow (if needs to load projects); don't block EQ.
                        RP.post(new Runnable() {
                            public void run() {
                                Group.setActiveGroup(g);
                            }
                        });
                    }
                });
                add(mi);
            }
            JMenuItem mi = new JRadioButtonMenuItem();
            Mnemonics.setLocalizedText(mi, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.no_group"));
            if (active == null) {
                mi.setSelected(true);
                if (OpenProjects.getDefault().getOpenProjects().length == 0 && Group.isAdvancedMode()) {
                    mi.setEnabled(false);
                }
            }
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Could be slow (if needs to load projects); don't block EQ.
                    RP.post(new Runnable() {
                        public void run() {
                            Group.setActiveGroup(null);
                        }
                    });
                }
            });
            add(mi);
            // Special menu items.
            addSeparator();
            mi = new JMenuItem();
            Mnemonics.setLocalizedText(mi, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_group"));
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newGroup();
                }
            });
            add(mi);
            if (active != null) {
                mi = new JMenuItem();
                Mnemonics.setLocalizedText(mi, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.properties", active.getName()));
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openProperties(active);
                    }
                });
                add(mi);
                mi = new JMenuItem();
                Mnemonics.setLocalizedText(mi, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.remove", active.getName()));
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        active.destroy();
                    }
                });
                add(mi);
            }
            mi = new JCheckBoxMenuItem();
            Mnemonics.setLocalizedText(mi, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.advanced"));
            mi.setSelected(Group.isAdvancedMode());
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Group.setAdvancedMode(!Group.isAdvancedMode());
                }
            });
            add(mi);
            return new JComponent[] {this};
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

    }

    /**
     * Create (and open) a new group.
     */
    private static void newGroup() {
        final AbstractNewGroupPanel panel = Group.isAdvancedMode() ? new NewGroupPanel() : new NewGroupPanelBasic();
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_title"));
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GroupsMenu.class));
        final JButton create = new JButton(NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_create"));
        create.setDefaultCapable(true);
        create.setEnabled(panel.isReady());
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (AbstractNewGroupPanel.PROP_READY.equals(evt.getPropertyName())) {
                    create.setEnabled(panel.isReady());
                }
            }
        });
        JButton cancel = new JButton(NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_cancel"));
        dd.setOptions(new Object[] {create, cancel});
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(create)) {
            final Group g = panel.create();
            RP.post(new Runnable() {
                public void run() {
                    Group.setActiveGroup(g);
                }
            });
        }
    }

    /**
     * Open a properties dialog for the group, according to its type.
     */
    private static void openProperties(Group g) {
        GroupEditPanel panel = g.createPropertiesPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.properties_title"));
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GroupsMenu.class));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            panel.applyChanges();
        }
    }

}
