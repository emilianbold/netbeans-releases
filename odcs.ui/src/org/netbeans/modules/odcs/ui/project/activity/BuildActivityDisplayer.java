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

import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails.BuildResult;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.team.server.ui.spi.BuildHandle;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class BuildActivityDisplayer extends ActivityDisplayer {

    private final BuildActivity activity;
    private final ProjectHandle<ODCSProject> projectHandle;
    private Action openJobIDEAction;
    private Action openBuildIDEAction;
    private LinkLabel linkBuildNumber;
    private LinkLabel linkJobName;
    private ProjectPropertyListener projectPropertyListener;

    public BuildActivityDisplayer(BuildActivity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        super(activity.getActivityDate(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
        projectPropertyListener = new ProjectPropertyListener();
    }

    @Override
    public JComponent getTitleComponent() {
        BuildDetails buildDetails = activity.getBuildDetails();
        JPanel panel = new JPanel(new GridBagLayout());

        linkBuildNumber = new LinkLabel(NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Build") + " " + buildDetails.getNumber()) { //NOI18N
            @Override
            public void mouseClicked(MouseEvent e) {
                Action openAction = getOpenBuildIDEAction(false);
                if (openAction == null || !openAction.isEnabled()) {
                    openAction = getOpenBrowserAction(getBuildUrl());
                }
                openAction.actionPerformed(new ActionEvent(BuildActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 3, 0, 0);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        panel.add(linkBuildNumber, gbc);

        panel.add(new JLabel(NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Of")), gbc); //NOI18N

        linkJobName = new LinkLabel(activity.getJobSummary().getName()) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action openAction = getOpenJobIDEAction(false);
                if (openAction == null || !openAction.isEnabled()) {
                    openAction = getOpenBrowserAction(getJobUrl());
                }
                openAction.actionPerformed(new ActionEvent(BuildActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        setPopupAction();
        panel.add(linkJobName, gbc);
        panel.add(new JLabel(NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Resulted", getResultText())), gbc); //NOI18N

        return panel;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        BuildDetails buildDetails = activity.getBuildDetails();
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridheight = GridBagConstraints.REMAINDER;

        JLabel lblCause = new JLabel(buildDetails.getCause() + "."); //NOI18N
        panel.add(lblCause, gbc);

        JLabel lblTime = new JLabel(getBuildDurationText(buildDetails.getDuration()));
        gbc.insets = new Insets(0, 5, 0, 0);
        panel.add(lblTime, gbc);

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
        String iconSuffix = getResultText();
        Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_" + iconSuffix + ".png", true); //NOI18N
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_build_unknown.png", true); //NOI18N
        }
        return icon;
    }

    private String getBuildDurationText(Long duration) {
        double dur = (double) duration / (double) 1E3;
        String units;
        if (dur <= 120.0) {
            units = NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Sec"); //NOI18N
        } else {
            dur = dur / 60.0;
            units = NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_Minute"); //NOI18N
        }
        return NbBundle.getMessage(BuildActivityDisplayer.class, "LBL_BuildDuration", (int) dur, units); //NOI18N
    }

    @Override
    String getUserName() {
        return "";
    }

    private String getResultText() {
        BuildResult result = activity.getBuildDetails().getResult();
        String resultName = "unknown";
        if (result != null) {
            resultName = result.name().toLowerCase();
        }
        return resultName;
    }

    private Action getOpenJobIDEAction(boolean force) {
        if (openJobIDEAction == null || force) {
            BuilderAccessor<ODCSProject> buildAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getBuildAccessor(ODCSProject.class);
            final Action action;
            if (buildAccessor != null) {
                action = buildAccessor.getJob(projectHandle, activity.getJobSummary().getName()).getDefaultAction();
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
            BuilderAccessor<ODCSProject> buildAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getBuildAccessor(ODCSProject.class);
            final Action action;
            if (buildAccessor != null) {
                String buildId = Integer.toString(activity.getBuildDetails().getNumber());
                BuildHandle build = buildAccessor.getJob(projectHandle, activity.getJobSummary().getName()).getBuild(buildId);
                action = build != null ? build.getDefaultAction() : null;
            } else {
                action = null;
            }
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
        return activity.getJobSummary().getUrl();
    }

    private String getBuildUrl() {
        return activity.getBuildDetails().getUrl();
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
