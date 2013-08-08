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

package org.netbeans.modules.project.ui.groups;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Submenu listing available groups and offering some operations on them.
 * @author Jesse Glick
 */
@ActionID(id = "org.netbeans.modules.project.ui.groups.GroupsMenu", category = "Project")
@ActionRegistration(displayName = "#GroupsMenu.label", lazy=false)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1100),
    @ActionReference(path = ProjectsRootNode.ACTIONS_FOLDER, position = 600, separatorAfter = 700)
})
@Messages("GroupsMenu.label=Project Gro&up")
public class GroupsMenu extends AbstractAction implements Presenter.Menu, Presenter.Popup {
    private static final int MAX_COUNT = 20;

    private static final RequestProcessor RP = new RequestProcessor(GroupsMenu.class.getName());
    private static final String HELPCTX = "org.netbeans.modules.project.ui.groups.GroupsMenu";

    public GroupsMenu() {
        super(GroupsMenu_label());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new Menu();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new Menu();
    }

    /**
     * The actual submenu (recreated each time it is displayed).
     */
    private static class Menu extends JMenu implements DynamicMenuContent {

        Menu() {
            Mnemonics.setLocalizedText(this, GroupsMenu_label());
        }

        @Messages({
            "GroupsMenu.no_group=(none)",
            "GroupsMenu.new_group=&New Group...",
            "# {0} - group display name", "GroupsMenu.properties=&Properties of \"{0}\"",
            "# {0} - group display name", "GroupsMenu.remove=&Remove \"{0}\"",
            "# {0} - group display name", "Delete_Confirm=Do you want to delete group \"{0}\"?",
            "GroupsMenu_more=&More groups...",
            "GroupsMenu_select=Select",
            "GroupsMenu_moreTitle=Select Project Group"
        })
        @Override public JComponent[] getMenuPresenters() {
            // XXX can it wait to add menu items until it is posted?
            removeAll();
            if (!OpenProjectList.getDefault().openProjectsAPI().isDone()) {
                //#214891 only show the groups when we have finishes opening the initial set of projects upon startup
                this.setEnabled(false);
                return new JComponent[] {this};
            }
            this.setEnabled(true);
            final Group active = Group.getActiveGroup();
            int counter = 0;
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
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Could be slow (if needs to load projects); don't block EQ.
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                Group.setActiveGroup(g, false);
                            }
                        });
                    }
                });
                add(mi);
                counter = counter + 1;
                if (counter > MAX_COUNT) {
                    //#216121
                    JMenuItem more = new JMenuItem();
                    Mnemonics.setLocalizedText(more, GroupsMenu_more());
                    more.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JList lst = new JList();
                            DefaultListModel model = new DefaultListModel();
                            for (final Group g : Group.allGroups()) {
                                model.addElement(g);
                            }
                            lst.setModel(model);
                            lst.setCellRenderer(new DefaultListCellRenderer() {

                                @Override
                                public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
                                    String text = ((Group)arg1).getName();
                                    return super.getListCellRendererComponent(arg0, text, arg2, arg3, arg4); //To change body of generated methods, choose Tools | Templates.
                                }
                            });
                            JScrollPane pane = new JScrollPane(lst);
                            JPanel pnl = new JPanel();
                            pnl.setLayout(new BorderLayout(12, 12));
                            pnl.add(pane);
                            pnl.setPreferredSize(new Dimension(300, 300));
                            String select = GroupsMenu_select();
                            NotifyDescriptor nd = new NotifyDescriptor(pnl, GroupsMenu_moreTitle(), NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, new Object[] {select, NotifyDescriptor.CANCEL_OPTION} , select);
                            if (select == DialogDisplayer.getDefault().notify(nd)) {
                                final Object o = lst.getSelectedValue();
                                if (o != null) {
                                    // Could be slow (if needs to load projects); don't block EQ.
                                    RP.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Group.setActiveGroup((Group)o, false);
                                        }
                                    });
                                }
                            }
                        }
                    });
                    add(more);
                    break;
                }
            }
            JMenuItem mi = new JRadioButtonMenuItem();
            Mnemonics.setLocalizedText(mi, GroupsMenu_no_group());
            if (active == null) {
                mi.setSelected(true);
                /* Was disliked by UI people:
                if (OpenProjects.getDefault().getOpenProjects().length == 0) {
                    mi.setEnabled(false);
                }
                 */
            }
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Could be slow (if needs to load projects); don't block EQ.
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            Group.setActiveGroup(null, false);
                        }
                    });
                }
            });
            add(mi);
            // Special menu items.
            addSeparator();
            mi = new JMenuItem();
            Mnemonics.setLocalizedText(mi, GroupsMenu_new_group());
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newGroup();
                }
            });
            add(mi);
            if (active != null) {
                mi = new JMenuItem();
                Mnemonics.setLocalizedText(mi, GroupsMenu_properties(active.getName()));
                mi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openProperties(active);
                    }
                });
                add(mi);
                mi = new JMenuItem();
                Mnemonics.setLocalizedText(mi, GroupsMenu_remove(active.getName()));
                mi.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NotifyDescriptor.Confirmation ask = new NotifyDescriptor.Confirmation(Delete_Confirm(active.getName()), NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(ask) == NotifyDescriptor.YES_OPTION) {
                            RP.post(new Runnable() {
                                @Override
                                public void run() {
                                    active.destroy();
                                }
                            });
                        }
                    }
                });
                add(mi);
            }
            return new JComponent[] {this};
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

    }

    /**
     * Create (and open) a new group.
     */
    @Messages({
        "GroupsMenu.new_title=Create New Group",
        "GroupsMenu.new_create=Create Group",
        "GroupsMenu.new_cancel=Cancel"
    })
    private static void newGroup() {
        final NewGroupPanel panel = new NewGroupPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, GroupsMenu_new_title());
        panel.setNotificationLineSupport(dd.createNotificationLineSupport());
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(HELPCTX));
        final JButton create = new JButton(GroupsMenu_new_create());
        create.setDefaultCapable(true);
        create.setEnabled(panel.isReady());
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NewGroupPanel.PROP_READY.equals(evt.getPropertyName())) {
                    create.setEnabled(panel.isReady());
                }
            }
        });
        JButton cancel = new JButton(GroupsMenu_new_cancel());
        dd.setOptions(new Object[] {create, cancel});
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(create)) {
            assert panel.isReady();
            final NewGroupPanel.Type type = panel.getSelectedType();
            final boolean autoSync = panel.isAutoSyncField();
            final boolean useOpen = panel.isUseOpenedField();
            final String name = panel.getNameField();
            final String masterProject = panel.getMasterProjectField();
            final String directory = panel.getDirectoryField();
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Group g = NewGroupPanel.create(type, name, autoSync, useOpen, masterProject, directory);
                    Group.setActiveGroup(g, true);
                }
            });
        }
    }

    /**
     * Open a properties dialog for the group, according to its type.
     */
    @Messages("GroupsMenu.properties_title=Project Group Properties")
    private static void openProperties(Group g) {
            Lookup context = Lookups.fixed(new Object[] { g, Utilities.ACCESSOR.createGroup(g.getName(), g.prefs()) });
            Dialog dialog = ProjectCustomizer.createCustomizerDialog("Projects/Groups/Customizer", //NOI18N
                                             context, 
                                             (String)null, 
                                             new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent ae) {
                                                    //noop
                                                }
                                            }, 
                                             new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent ae) {
                                                    //noop
                                                }
                                             }, new HelpCtx(HELPCTX));
            dialog.setTitle( GroupsMenu_properties_title() );
            dialog.setModal(true);
            dialog.setVisible(true);
    }

}
