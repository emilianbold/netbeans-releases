/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.project.ui.groups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
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
                    /* Was disliked by UI people:
                    if (g.isPristine()) {
                        mi.setEnabled(false);
                    }
                     */
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
                /* Was disliked by UI people:
                if (OpenProjects.getDefault().getOpenProjects().length == 0) {
                    mi.setEnabled(false);
                }
                 */
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
                        NotifyDescriptor.Confirmation ask = new NotifyDescriptor.Confirmation(org.openide.util.NbBundle.getMessage(GroupsMenu.class, "Delete_Confirm", active.getName()), NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(ask) == NotifyDescriptor.YES_OPTION) {
                            active.destroy();
                        }
                    }
                });
                add(mi);
            }
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
        final NewGroupPanel panel = new NewGroupPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_title"));
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GroupsMenu.class));
        final JButton create = new JButton(NbBundle.getMessage(GroupsMenu.class, "GroupsMenu.new_create"));
        create.setDefaultCapable(true);
        create.setEnabled(panel.isReady());
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NewGroupPanel.PROP_READY.equals(evt.getPropertyName())) {
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
        panel.setNotificationLineSupport(dd.createNotificationLineSupport());
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(GroupsMenu.class));
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            panel.applyChanges();
        }
    }

}
