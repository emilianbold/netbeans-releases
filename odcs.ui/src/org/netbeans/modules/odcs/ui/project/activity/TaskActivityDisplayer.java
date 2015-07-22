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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import oracle.clouddev.server.profile.activity.client.api.Activity;
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

public final class TaskActivityDisplayer extends ActivityDisplayer {

    private final Activity activity;
    private final ProjectHandle<ODCSProject> projectHandle;
    private Action openIDEAction;

    private static final String PROP_ACTIVITY_TYPE = "activityType"; // NOI18N
    private static final String PROP_TASK_TYPE = "taskType"; // NOI18N
    private static final String PROP_TASK_ID = "taskId"; // NOI18N
    private static final String PROP_TASK_TITLE = "taskTitle"; // NOI18N
    private static final String PROP_COMMENT = "comment"; // NOI18N
    private static final String TYPE_COMMENTED = "COMMENTED"; // NOI18N
    private static final String TYPE_UPDATED = "UPDATED"; // NOI18N

    private static final String OLD_VALUE_FIELD_PREFIX = "previous_"; // NOI18N
    private static final String FIELD_PREFIX = "field_"; // NOI18N
    private static final String NO_VALUE = "DELETED"; // NOI18N

    public TaskActivityDisplayer(Activity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity.getTimestamp(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        final String taskId = activity.getProperty(PROP_TASK_ID);
        LinkLabel linkDisplayName = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action openAction = getOpenIDEAction();
                if (openAction == null || !openAction.isEnabled()) {
                    openAction = getOpenBrowserAction(getTaskUrl(taskId));
                }
                openAction.actionPerformed(new ActionEvent(TaskActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        linkDisplayName.setPopupActions(getOpenIDEAction(), getOpenBrowserAction(getTaskUrl(taskId)));
        String taskName = NbBundle.getMessage(TaskActivityDisplayer.class, "FMT_TaskDisplayName", // NOI18N
                   activity.getProperty(PROP_TASK_TYPE), taskId, activity.getProperty(PROP_TASK_TITLE));
        taskName = Utils.computeFitText(linkDisplayName, maxWidth, taskName, false);
        linkDisplayName.setText(taskName);

        String bundleKey = "FMT_Task_" + activity.getProperty(PROP_ACTIVITY_TYPE); // NOI18N
        try {
            return createMultipartTextComponent(bundleKey, linkDisplayName);
        } catch (MissingResourceException ex) {
            return createMultipartTextComponent("FMT_Task_Other", linkDisplayName); // NOI18N
        }
    }

    @Override
    public JComponent getDetailsComponent() {
        String activityType = activity.getProperty(PROP_ACTIVITY_TYPE);
        if (TYPE_COMMENTED.equals(activityType)) {
            return commentDetailsPanel(activity.getProperty(PROP_COMMENT));
        } else if (TYPE_UPDATED.equals(activityType)) {
            return updateDetailsPanel(activity.getProperties());
        } else {
            return null;
        }
    }

    @Override
    String getUserName() {
        return activity.getAuthor().getFullname();
    }

    @Override
    public Icon getActivityIcon() {
        String iconSuffix = activity.getProperty(PROP_ACTIVITY_TYPE).toLowerCase();
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task_" + iconSuffix + ".png", true); //NOI18N
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_task.png", true); //NOI18N
        }
        return icon;
    }

    private JComponent updateDetailsPanel(Map<String,String> properties) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 2, 3, 10);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridy = 0;
        gbc.gridx = 0;
        JLabel lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_FieldName")); // NOI18N
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OldValue")); // NOI18N
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 2;
        lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_NewValue")); // NOI18N
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(), gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH;

        int i = 0;
        for (Map.Entry<String,String> e : properties.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith(OLD_VALUE_FIELD_PREFIX)) {
                continue;
            }
            String fieldName = key.substring(OLD_VALUE_FIELD_PREFIX.length());
            String oldText = e.getValue();
            String newText = properties.get(FIELD_PREFIX + fieldName);

            gbc.gridy = i + 1;
            gbc.gridx = 0;
            JLabel lblField = new JLabel(fieldName); // TODO need display names for the field names
            lblField.setVerticalAlignment(SwingConstants.TOP);
            panel.add(lblField, gbc);

            gbc.gridx = 1;
            if (oldText == null || oldText.isEmpty() || oldText.equals(NO_VALUE)) {
                oldText = NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Empty"); // NOI18N
            }
            JLabel lblOldValue = new JLabel(oldText);
            panel.add(lblOldValue, gbc);

            gbc.gridx = 2;
            if (newText == null || newText.isEmpty() || newText.equals(NO_VALUE)) {
                newText = NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Empty"); // NOI18N
            }
            JLabel lblNewValue = new JLabel(newText);
            panel.add(lblNewValue, gbc);
            i++;
        }

        return panel;
    }

    private JComponent commentDetailsPanel(String comment) {
        if(comment == null) {
            return null;
        }
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel lbl = new JLabel(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_Comment")); // NOI18N
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        String wikiLanguage = projectHandle.getTeamProject().getWikiLanguage();
        WikiPanel commentPanel = WikiUtils.getWikiPanel(wikiLanguage, false, false);
        commentPanel.setWikiFormatText(comment);
        panel.add(commentPanel, gbc);
        return panel;
    }

    private Action getOpenIDEAction() {
        if (openIDEAction == null) {
            final Action action;
            QueryAccessor<ODCSProject> queryAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getQueryAccessor(ODCSProject.class);
            if (queryAccessor != null) {
                action = queryAccessor.getOpenTaskAction(projectHandle, activity.getProperty(PROP_TASK_ID));
            } else {
                action = null;
            }
            openIDEAction = new AbstractAction(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OpenIDE")) { // NOI18N
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

    private String getTaskUrl(String taskId) {
        return projectHandle.getTeamProject().getWebUrl() + "/task/" + taskId; // NOI18N
    }
}
