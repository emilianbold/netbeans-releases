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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import oracle.clouddev.server.profile.activity.client.api.Author;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.project.LinkLabel;
import org.netbeans.modules.odcs.ui.spi.VCSAccessor;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.SourceHandle;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class ScmActivityDisplayer extends ActivityDisplayer implements ActivityTypes {

    private final Activity activity;
    private final String scmUrl;
    private final ProjectHandle<ODCSProject> projectHandle;
    private Action openAction;

    private static final String PROP_SHA = "sha"; // commit ID // NOI18N
    private static final String PROP_COMMIT_REPOSITORY = "repository"; // NOI18N
    private static final String PROP_COMMENT = "comment"; // NOI18N
    private static final String PROP_USERNAME = "commitLogFullName"; // NOI18N

    private static final String PROP_REPO_NAME = "repoName"; // NOI18N
    private static final String PROP_REPO_DESC = "repoDescription"; // NOI18N
    private static final String PROP_REPO_OPERATION_TYPE = "type"; // NOI18N
    private static final String REPO_CREATED = "CREATED"; // NOI18N
    private static final String REPO_DELETED = "DELETED"; // NOI18N

    public ScmActivityDisplayer(Activity activity, ProjectHandle<ODCSProject> projectHandle, String scmUrl, int maxWidth) {
        super(activity.getTimestamp(), maxWidth);
        this.activity = activity;
        this.projectHandle = projectHandle;
        this.scmUrl = scmUrl;
    }

    private boolean isRepoActivity() {
        return activity.getType().equals(SCM_REPO);
    }

    @Override
    public JComponent getTitleComponent() {
        JComponent titlePanel = super.getTitleComponent();
        if (isRepoActivity()) {
            return titlePanel;
        }
        // This is a SCM commit activity
        String repository = activity.getProperty(PROP_COMMIT_REPOSITORY);
        Component[] components;
        if (repository != null) {
            LinkLabel linkCommit = new LinkLabel(getMinimizedCommitId()) {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Action action = getOpenIDEAction();
                    if (action == null || !action.isEnabled()) {
                        action = getOpenBrowserAction(getCommitUrl());
                    }
                    action.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
                }
            };
            linkCommit.setPopupActions(getOpenIDEAction(), getOpenBrowserAction(getCommitUrl()));

            components = createMultipartTextComponents("FMT_Committed", linkCommit, repository); // NOI18N
        } else { // the commit activity may come with null repository (perhaps when the repository is later deleted)
            components = new Component[] { new JLabel(NbBundle.getMessage(ScmActivityDisplayer.class, "FMT_CommittedNoRepo", getMinimizedCommitId())) }; // NOI18N
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridheight = GridBagConstraints.REMAINDER;
        for (Component comp : components) {
            titlePanel.add(comp, gbc);
        }
        return titlePanel;
    }

    @Override
    public JComponent getShortDescriptionComponent() {
        if (isRepoActivity()) {
            String type = activity.getProperty(PROP_REPO_OPERATION_TYPE);
            final String repoName = activity.getProperty(PROP_REPO_NAME);
            if (REPO_DELETED.equals(type)) {
                return createMultipartTextComponent("FMT_RepositoryDeleted", repoName); // NOI18N
            } else {
                LinkLabel linkRepo = new LinkLabel(repoName) {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Action action = getOpenBrowserAction(getRepositoryUrl());
                        action.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
                    }
                };
                Action cloneAction = getCloneSourcesAction();
                if (cloneAction != null) {
                    linkRepo.setPopupActions(getOpenBrowserAction(getRepositoryUrl()), cloneAction);
                }
                if (REPO_CREATED.equals(type)) {
                    return createMultipartTextComponent("FMT_RepositoryCreated", linkRepo); // NOI18N
                } else {
                    return createMultipartTextComponent("FMT_RepositoryModified", linkRepo); // NOI18N
                }
            }
        } else { // SCM commit
            return new JLabel("<html>" + activity.getProperty(PROP_COMMENT) + "</html>"); // NOI18N
        }
    }

    @Override
    public JComponent getDetailsComponent() {
        if (isRepoActivity()) {
            String description = activity.getProperty(PROP_REPO_DESC);
            if (description != null && description.length() > 0) {
                return new JLabel("<html>" + description + "</html>"); // NOI18N
            }
        }
        return null;
    }

    @Override
    String getUserName() {
        if (isRepoActivity()) {
            Author author = activity.getAuthor();
            return author != null ? author.getFullname() : "SYSTEM"; // NOI18N
        } else {
            return activity.getProperty(PROP_USERNAME);
        }
    }

    @Override
    public Icon getActivityIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_commit.png", true); // NOI18N
        // TODO need a different icon for repository activities
    }

    private String getMinimizedCommitId() {
        String commitId = activity.getProperty(PROP_SHA);
        if (commitId != null && commitId.length() > 7) {
            return commitId.substring(0, 7);
        }
        return commitId;
    }

    private String getCommitUrl() {
        String url = Utils.getWebUrl(scmUrl);
        if (!url.endsWith("/")) { //NOI18N
            url += "/"; //NOI18N
        }
        url += activity.getProperty(PROP_COMMIT_REPOSITORY) + "/commit/" + activity.getProperty(PROP_SHA); // NOI18N
        return url;
    }

    private String getRepositoryUrl() {
        String url = Utils.getWebUrl(scmUrl);
        if (!url.endsWith("/")) { //NOI18N
            url += "/"; //NOI18N
        }
        url += activity.getProperty(PROP_REPO_NAME) + "/tree?revision=master"; // NOI18N
        return url;
    }

    private Action getOpenIDEAction() {
        if (openAction == null) {
            openAction = new OpenAction(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_OpenIDE")); // NOI18N
        }
        return openAction;
    }

    private class OpenAction extends AbstractAction {

        private final VCSAccessor accessor;

        public OpenAction(String name) {
            super(name);
            accessor = VCSAccessor.getDefault();
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (isEnabled()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Action action = accessor.getOpenHistoryAction(projectHandle,
                                                                      activity.getProperty(PROP_COMMIT_REPOSITORY),
                                                                      activity.getProperty(PROP_SHA));
                        if (action == null) {
                            action = getOpenBrowserAction(getCommitUrl());
                        }
                        final ActionListener fAction = action;
                        UIUtils.runInAWT(new Runnable() {
                            @Override
                            public void run() {
                                fAction.actionPerformed(e);
                            }
                        });
                    }
                });
            }
        }

        @Override
        public boolean isEnabled() {
            return accessor != null;
        }
    }

    private Action getCloneSourcesAction() {
        String repoName = activity.getProperty(PROP_REPO_NAME);
        VCSAccessor accessor = org.netbeans.modules.odcs.ui.spi.VCSAccessor.getDefault();
        List<SourceHandle> sources = accessor.getSources(projectHandle);
        for (SourceHandle sh : sources) {
            if (sh.getDisplayName().equals(repoName)) {
                return accessor.getOpenSourcesAction(sh);
            }
        }
        return null;
    }
}
