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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.team.server.ui.spi.BuildHandle;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.JobHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class BuildActivityDisplayer extends ActivityDisplayer {

    private static final String PROP_RESULT = "result"; // NOI18N
    private static final String PROP_DURATION = "duration"; // NOI18N
    private static final String PROP_NUMBER = "number"; // NOI18N
    private static final String PROP_JOB_NAME = "jobName"; // NOI18N

    final Activity activity;
    final ProjectHandle<ODCSProject> projectHandle;

    private Action openJobIDEAction;
    private Action openBuildIDEAction;
    private LinkLabel linkBuildNumber;
    private LinkLabel linkJobName;
    private ProjectPropertyListener projectPropertyListener;

    public BuildActivityDisplayer(Activity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity.getTimestamp(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
        projectPropertyListener = new ProjectPropertyListener();
    }

    @Override
    public JComponent getTitleComponent() {
        String buildNumber = activity.getProperty(PROP_NUMBER);
        String jobName = activity.getProperty(PROP_JOB_NAME);
        JComponent comp = createMultipartTextComponent("FMT_Build", createBuildLink(buildNumber), createJobLink(jobName)); // NOI18N
        setPopupAction();
        return comp;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridheight = GridBagConstraints.REMAINDER;

        String result = activity.getProperty(PROP_RESULT);
        JLabel resultLabel = new JLabel();
        try {
            resultLabel.setText(NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Build_"+result)); // NOI18N
        } catch (MissingResourceException ex) {
            resultLabel.setText(NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_BuildUnknown")); // NOI18N
        }
        panel.add(resultLabel, gbc);

        JLabel durationLabel = new JLabel(getBuildDurationText());
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(durationLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(), gbc);
        return panel;
    }

    @Override
    public JComponent getDetailsComponent() {
        return null;
    }

    @Override
    public Icon getActivityIcon() {
        String iconSuffix = activity.getProperty(PROP_RESULT).toLowerCase();
        Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_" + iconSuffix + ".png", true); //NOI18N
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_unknown.png", true); //NOI18N
        }
        return icon;
    }

    LinkLabel createBuildLink(String buildNumber) {
        if (linkBuildNumber == null) {
            linkBuildNumber = new LinkLabel(buildNumber) {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Action openAction = getOpenBuildIDEAction(false);
                    if (openAction == null || !openAction.isEnabled()) {
                        openAction = getOpenBrowserAction(getBuildUrl());
                    }
                    openAction.actionPerformed(new ActionEvent(BuildActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
                }
            };
        }
        return linkBuildNumber;
    }

    LinkLabel createJobLink(String jobName) {
        if (linkJobName == null) {
            int i = jobName != null ? jobName.indexOf('.') : -1;
            if (i > 0) {
                jobName = jobName.substring(i+1);
            }
            linkJobName = new LinkLabel(jobName) {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Action openAction = getOpenJobIDEAction(false);
                    if (openAction == null || !openAction.isEnabled()) {
                        openAction = getOpenBrowserAction(getJobUrl());
                    }
                    openAction.actionPerformed(new ActionEvent(BuildActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
                }
            };
        }
        return linkJobName;
    }

    private String getBuildDurationText() {
        String durationStr = activity.getProperty(PROP_DURATION);
        long duration;
        try {
            duration = Long.parseLong(durationStr);
        } catch (Exception ex) {
            return NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_DurationUnknown"); // NOI18N
        }
        double dur = (double) duration / (double) 1E3;
        String units;
        if (dur <= 120.0) {
            units = NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Sec"); //NOI18N
        } else {
            dur = dur / 60.0;
            units = NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Minute"); //NOI18N
        }
        return NbBundle.getMessage(BuildActivityDisplayer.class, "FMT_BuildDuration", (int) dur, units); //NOI18N
    }

    @Override
    String getUserName() {
        return "";
    }

    private Action getOpenJobIDEAction(boolean force) {
        if (openJobIDEAction == null || force) {
            BuilderAccessor<ODCSProject> buildAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getBuildAccessor(ODCSProject.class);
            final Action action;
            if (buildAccessor != null) {
                JobHandle job = buildAccessor.getJob(projectHandle, activity.getProperty(PROP_JOB_NAME));
                action = job != null ? job.getDefaultAction() : null;
            } else {
                action = null;
            }
            openJobIDEAction = new AbstractAction(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OpenIDE")) {
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
        return openJobIDEAction;
    }

    private Action getOpenBuildIDEAction(boolean force) {
        if (openBuildIDEAction == null || force) {
            Action a = null;
            BuilderAccessor<ODCSProject> buildAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getBuildAccessor(ODCSProject.class);
            if (buildAccessor != null) {
                JobHandle job = buildAccessor.getJob(projectHandle, activity.getProperty(PROP_JOB_NAME));
                if (job != null) {
                    String buildId = activity.getProperty(PROP_NUMBER);
                    BuildHandle build = job.getBuild(buildId);
                    if (build != null) {
                        a = build.getDefaultAction();
                    }
                }
            }
            final Action action = a;
            openBuildIDEAction = new AbstractAction(NbBundle.getMessage(TaskActivityDisplayer.class, "LBL_OpenIDE")) {
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
        return openBuildIDEAction;
    }

    private String getJobUrl() {
        String jobName = TextUtils.encodeURL(activity.getProperty(PROP_JOB_NAME));
        return projectHandle.getTeamProject().getWebUrl() + "/build/job/" + jobName; // NOI18N
    }

    private String getBuildUrl() {
        String jobName = TextUtils.encodeURL(activity.getProperty(PROP_JOB_NAME));
        String buildNumber = activity.getProperty(PROP_NUMBER);
        return projectHandle.getTeamProject().getWebUrl() + "/build/job/" + jobName + "/build/" + buildNumber; // NOI18N
    }

    private void setPopupAction() {
        Action jobIDEAction = getOpenJobIDEAction(true);
        linkJobName.setPopupActions(jobIDEAction, getOpenBrowserAction(getJobUrl()));
        Action buildIDEAction = getOpenBuildIDEAction(true);
        linkBuildNumber.setPopupActions(buildIDEAction, getOpenBrowserAction(getBuildUrl()));
        if (!jobIDEAction.isEnabled() || !buildIDEAction.isEnabled()) {
            projectHandle.addPropertyChangeListener(projectPropertyListener);
        }
    }

    private void removeProjectListener() {
        projectHandle.removePropertyChangeListener(projectPropertyListener);
    }

    private class ProjectPropertyListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ProjectHandle.PROP_BUILD_LIST)) {
                if (SwingUtilities.isEventDispatchThread()) {
                    actionsAvailable();
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            actionsAvailable();
                        }
                    });
                }
            }
        }

        private void actionsAvailable(){
            removeProjectListener();
            setPopupAction();
        }
    }
}
