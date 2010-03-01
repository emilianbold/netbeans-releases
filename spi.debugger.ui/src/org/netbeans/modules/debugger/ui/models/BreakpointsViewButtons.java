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

package org.netbeans.modules.debugger.ui.models;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.modules.debugger.ui.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.ui.models.BreakpointGroup.Group;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointsViewButtons {

    public static final String PREFERENCES_NAME = "variables_view"; // NOI18N
    public static final String SHOW_VALUE_AS_STRING = "show_value_as_string"; // NOI18N

    public static JButton createNewBreakpointActionButton() {
        JButton button = createButton(
                "org/netbeans/modules/debugger/resources/breakpointsView/NewBreakpoint.gif",
                NbBundle.getMessage (BreakpointsViewButtons.class, "Hint_New_Breakpoint")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AddBreakpointAction().actionPerformed(e);
            }
        });
        return button;
    }

    public static synchronized JButton createGroupSelectionButton() {
        final JButton button = createButton(
                "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointGroups_options_16.png",
                NbBundle.getMessage (BreakpointsViewButtons.class, "Hint_Select_bp_groups")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Properties props = Properties.getDefault().getProperties("Breakpoints");
                String[] groupNames = (String[]) props.getArray("Grouping", new String[] { Group.CUSTOM.name() });
                String brkpGroup;
                if (groupNames.length == 0) {
                    brkpGroup = Group.NO.name();
                } else if (groupNames.length > 1) {
                    brkpGroup = Group.NESTED.name();
                } else {
                    brkpGroup = groupNames[0];
                }
                JPopupMenu menu = new JPopupMenu(NbBundle.getMessage (BreakpointsViewButtons.class, "Lbl_bp_groups"));
                for (Group group : Group.values()) {
                    menu.add(createJRadioButtonMenuItem(group, brkpGroup));
                }
                menu.addSeparator();
                menu.add(createCheckBoxMenuItem("LBL_BreakpointsFromOpenProjectsOnly", BreakpointGroup.PROP_FROM_OPEN_PROJECTS, props));
                if (currentSessionHaveProjects()) {
                    menu.add(createCheckBoxMenuItem("LBL_BreakpointsFromCurrentDebugSessionOnly", BreakpointGroup.PROP_FROM_CURRENT_SESSION_PROJECTS, props));
                }
                menu.show(button, 16, 0);

            }
        });
        button.setVisible(false);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                boolean groupableBreakpoints = false;
                Breakpoint[] brkps = DebuggerManager.getDebuggerManager().getBreakpoints();
                for (Breakpoint b : brkps) {
                    if (b.getGroupProperties() != null) {
                        groupableBreakpoints = true;
                        break;
                    }
                }
                if (groupableBreakpoints) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisible(true);
                        }
                    });
                } else {
                    final boolean[] gb = new boolean[] { groupableBreakpoints };
                    DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerManagerAdapter() {
                        @Override
                        public void breakpointAdded(Breakpoint breakpoint) {
                            if (!gb[0] && breakpoint.getGroupProperties() != null) {
                                gb[0] = true;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        button.setVisible(true);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return button;
    }

    private static JRadioButtonMenuItem createJRadioButtonMenuItem(Group group, String brkpGroup) {
        JRadioButtonMenuItem gb = new JRadioButtonMenuItem(new GroupChangeAction(group));
        gb.setSelected(brkpGroup.equals(group.name()));
        return gb;
    }

    private static JCheckBoxMenuItem createCheckBoxMenuItem(String text, final String propName, final Properties props) {
        boolean selected = props.getBoolean(propName, true);
        text = NbBundle.getMessage(BreakpointsViewButtons.class, text);
        final JCheckBoxMenuItem chb = new JCheckBoxMenuItem(text, selected);
        chb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean selected = chb.isSelected();
                props.setBoolean(propName, selected);
            }
        });
        return chb;
    }

    private static boolean currentSessionHaveProjects() {
        // TODO: Perhaps the session could provide it's breakpoints directly somehow.
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (currentSession == null) {
            return false;
        }
        List<? extends Project> sessionProjects = currentSession.lookup(null, Project.class);
        return sessionProjects.size() > 0;
    }

    private static JButton createButton (String iconPath, String tooltip) {
        Icon icon = ImageUtilities.loadImageIcon(iconPath, false);
        final JButton button = new JButton(icon);
        // ensure small size, just for the icon
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        button.setPreferredSize(size);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }

    // **************************************************************************

    private static class GroupChangeAction extends AbstractAction {

        private Group group;

        public GroupChangeAction(Group group) {
            this.group = group;
            String name = "LBL_"+group.name()+"Group"; // NOI18N
            name = NbBundle.getMessage (BreakpointsViewButtons.class, name);
            putValue(Action.NAME, name);
        }


        public void actionPerformed(ActionEvent e) {
            if (group == Group.NESTED) {
                BreakpointNestedGroupsDialog bngd = new BreakpointNestedGroupsDialog();
                bngd.setDisplayedGroups((String[]) Properties.getDefault().
                        getProperties("Breakpoints").getArray("Grouping", new String[] { Group.CUSTOM.name() }));
                String title = NbBundle.getMessage(BreakpointNestedGroupsDialog.class, "BreakpointNestedGroupsDialog_title");
                Object res = DialogDisplayer.getDefault().notify(new DialogDescriptor(bngd, title, true, null));
                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    Properties.getDefault().getProperties("Breakpoints").
                            setArray("Grouping", bngd.getDisplayedGroups());
                }
            } else {
                Properties.getDefault().getProperties("Breakpoints").setArray("Grouping", new String[] { group.name() });
            }
        }

    }

}
