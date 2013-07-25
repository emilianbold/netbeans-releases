/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.project.activity;

import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.Type;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.QueryAccessor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class TaskActivityDisplayer extends ActivityDisplayer {

    private TaskActivity activity;
    private final ProjectHandle<ODCSProject> projectHandle;
    private Action openIDEAction;

    public TaskActivityDisplayer(TaskActivity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity.getActivityDate(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        JLabel lblType = new JLabel(Utils.getActivityName(activity.getActivity().getActivityType()));
        panel.add(lblType, gbc);

        LinkLabel linkDisplayName = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action openAction = getOpenIDEAction();
                if (openAction == null || !openAction.isEnabled()) {
                    openAction = getOpenBrowserAction(getTaskUrl());
                }
                openAction.actionPerformed(new ActionEvent(TaskActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        linkDisplayName.setPopupActions(getOpenIDEAction(), getOpenBrowserAction(getTaskUrl()));
        String taskName = Utils.computeFitText(linkDisplayName, maxWidth, getTaskDisplayName(), false);
        linkDisplayName.setText(taskName);
        panel.add(linkDisplayName, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(), gbc);
        return panel;
    }

    @Override
    public JComponent getDetailsComponent() {
        Type activityType = activity.getActivity().getActivityType();
        if (activityType.equals(Type.COMMENTED)) {
            return commentDetailsPanel(activity.getActivity().getComment());
        } else if (activityType.equals(Type.UPDATED)) {
            return updateDetailsPanel(activity.getActivity().getFieldUpdates());
        } else {
            return null;
        }
    }

    @Override
    String getUserName() {
        return activity.getActivity().getAuthor().getRealname();
    }

    @Override
    public Icon getActivityIcon() {
        String activityType = Utils.getActivityName(activity.getActivity().getActivityType()).toLowerCase();
        String iconSuffix = activityType.split(" ")[0]; //NOI18N
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task_" + iconSuffix + ".png", true); //NOI18N
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task.png", true); //NOI18N
        }
        return icon;
    }

    private String getTaskDisplayName() {
        return activity.getActivity().getTask().getTaskType() + " "
                + activity.getActivity().getTask().getId() + ": " //NOI18N
                + activity.getActivity().getTask().getShortDescription();
    }

    private JComponent updateDetailsPanel(List<FieldUpdate> fieldUpdates) {
        String wikiLanguage = projectHandle.getTeamProject().getWikiLanguage();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridy = 0;

        gbc.gridx = 0;
        JLabel lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_FieldName"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OldValue"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_NewValue"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.weightx = 0.0;
        for (int i = 0; i < fieldUpdates.size(); i++) {
            gbc.gridy = i + 1;
            FieldUpdate update = fieldUpdates.get(i);
            gbc.gridx = 0;
            JLabel lblField = new JLabel(update.getFieldDescription());
            lblField.setVerticalAlignment(SwingConstants.TOP);
            panel.add(lblField, gbc);

            gbc.gridx = 1;
            WikiPanel lblOldValue = WikiUtils.getWikiPanel(wikiLanguage, false, false);
            String oldText = update.getOldValue();
            if (oldText.isEmpty()) {
                oldText = NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Empty");
            }
            lblOldValue.setWikiFormatText(oldText);
            panel.add(lblOldValue, gbc);

            gbc.gridx = 2;
            WikiPanel lblNewValue = WikiUtils.getWikiPanel(wikiLanguage, false, false);
            String newText = update.getOldValue();
            if (newText.isEmpty()) {
                newText = NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Empty");
            }
            lblNewValue.setWikiFormatText(newText);
            panel.add(lblNewValue, gbc);
        }

        return panel;
    }

    private JComponent commentDetailsPanel(Comment comment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Comment"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        String formatedText = comment.getWikiRenderedText();
        formatedText = "<html>" + formatedText + "</html>";
        JLabel lblComment = new JLabel(formatedText);
        panel.add(lblComment, gbc);
        return panel;
    }

    private Action getOpenIDEAction() {
        if (openIDEAction == null) {
            QueryAccessor<ODCSProject> queryAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getQueryAccessor(ODCSProject.class);
            final Action action;
            if (queryAccessor != null) {
                action = queryAccessor.getOpenTaskAction(projectHandle, activity.getActivity().getTask().getId().toString());
            } else {
                action = null;
            }
            openIDEAction = new AbstractAction(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OpenIDE")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isEnabled()) {
                        action.actionPerformed(e);
                    }
                }

                @Override
                public boolean isEnabled() {
                    return action != null;
                }
            };
        }
        return openIDEAction;
    }

    private String getTaskUrl() {
        return activity.getActivity().getTask().getUrl();
    }
}
