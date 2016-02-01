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
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.bugtracking.commons.TextUtils;
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

    private static final String PROP_COMMIT_REPOSITORY = "repository"; // NOI18N
    private static final String PROP_COMMITS_SIZE = "commits.size"; // NOI18N
    private static final String PROP_SIZE = "size"; // NOI18N
    private static final String PROP_COMMITS_HASH = "commits[%s].hash"; // NOI18N
    private static final String PROP_COMMITS_COMMENT = "commits[%s].comment"; // NOI18N
    private static final String PROP_COMMITS_AUTHOR = "commits[%s].author"; // NOI18N
    private static final String PROP_BRANCH = "branch"; // NOI18N
    private static final String PROP_BASE = "base"; // NOI18N
    private static final String PROP_HEAD = "head"; // NOI18N

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
            String cs = activity.getProperties().get(PROP_SIZE);
            int commitsSize = Integer.parseInt(cs);
            LinkLabel linkBranch = getBranchLink();
            if(commitsSize == 1) {
                LinkLabel linkCommit = getCommitLink(0);
                components = createMultipartTextComponents("FMT_PushedOne", linkCommit, linkBranch, repository); // NOI18N
            } else {
                components = createMultipartTextComponents("FMT_PushedMore", commitsSize, linkBranch, repository); // NOI18N 
            }
        } else { // the commit activity may come with null repository (perhaps when the repository is later deleted)
            components = new Component[] { new JLabel(NbBundle.getMessage(ScmActivityDisplayer.class, "FMT_PushedOne", getMinimizedCommitId(0))) }; // NOI18N
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

    protected LinkLabel getCommitLink(final int idx) {
        final String commitUrl = getCommitUrl(idx);
        final Action action = new OpenCommitAction(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_OpenIDE"), idx); // NOI18N
        LinkLabel linkCommit = new LinkLabel(getMinimizedCommitId(idx)) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action a = action;
                if (!action.isEnabled()) {
                    a = getOpenBrowserAction(commitUrl);
                }
                a.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        linkCommit.setPopupActions(action, getOpenBrowserAction(commitUrl));
        return linkCommit;
    }
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^.+@.+\\..+$"); // NOI18N
    private JComponent getAuthorLink(final int idx) {
        final String author = activity.getProperty(String.format(PROP_COMMITS_AUTHOR, idx));
        if(author == null) {
            return new JLabel();
        }
        if(EMAIL_PATTERN.matcher(author).matches()) { 
            return new LinkLabel(author) {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        
                        String comment = TextUtils.encodeURL(
                                            NbBundle.getMessage(ScmActivityDisplayer.class, "FMT_MailToSubject", // NOI18N
                                                activity.getProperty(String.format(PROP_COMMITS_HASH, idx)),
                                                skipLineBreaks(activity.getProperty(String.format(PROP_COMMITS_COMMENT, idx)))));
                        Desktop.getDesktop().mail(new URI("mailto:" + author + "?subject=" + comment)); // NOI18N
                    } catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(ActivityDisplayer.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            };
        } else {
            return new JLabel(author);            
        }
    }
    

    protected LinkLabel getBranchLink() {
        final String branch = activity.getProperty(PROP_BRANCH);
        final String branchUrl = getBranchUrl(branch);
        final Action action = new OpenBranchAction(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_OpenIDE")); // NOI18N
        LinkLabel linkBranch = new LinkLabel(branch) {
            @Override
            public void mouseClicked(MouseEvent e) {
                Action a = action;
                if (!a.isEnabled()) {
                    a = getOpenBrowserAction(branchUrl);
                }
                a.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
            }
        };
        linkBranch.setPopupActions(action, getOpenBrowserAction(branchUrl));
        return linkBranch;
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
        } else { // SCM push
            return new JLabel();
        }
    }

    @Override
    public JComponent getDetailsComponent() {
        if (isRepoActivity()) {
            String description = activity.getProperty(PROP_REPO_DESC);
            if (description != null && description.length() > 0) {
                return new JLabel("<html>" + description + "</html>"); // NOI18N
            }
        } else { // SCM push            
            String cs = activity.getProperties().get(PROP_COMMITS_SIZE);
            int commitsSize = Integer.parseInt(cs);
            String fs = activity.getProperties().get(PROP_SIZE);            
            int fullSize = Integer.parseInt(fs);
            if(commitsSize > 1) {
                JComponent cmp = new JPanel();
                cmp.setLayout(new BoxLayout(cmp, BoxLayout.Y_AXIS));
                cmp.setOpaque(false);
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.LINE_START;
                gbc.insets = new Insets(0, 5, 0, 0);
                gbc.gridheight = GridBagConstraints.REMAINDER;
                int topInset = 0;
                for (int i = 0; i < commitsSize; i++) {
                    JPanel p = new JPanel(new GridBagLayout());
                    p.setOpaque(false);
                    LinkLabel linkCommit = getCommitLink(i);
                    
                    JComponent authorComponent = getAuthorLink(i);
                    
                    String commentFull = activity.getProperty(String.format(PROP_COMMITS_COMMENT, i));
                    String comment = skipLineBreaks(commentFull);    
                    
                    JLabel commentLabel = new JLabel(comment);
                    commentLabel.setToolTipText(toHtml(commentFull));
                    
                    Component[] components = createMultipartTextComponents("LBL_CommitDesc", linkCommit, authorComponent, commentLabel); // NOI18N
                    
                    gbc.weightx = 0;
                    for (Component component : components) {
                        p.add(component, gbc);
                        if(component == authorComponent) {
                            gbc.insets = new Insets(topInset, 0, 0, 0);
                        } else {
                            gbc.insets = new Insets(topInset, 5, 0, 0);
                        }
                    }
                    gbc.weightx = 1;
                    p.add(new JLabel(), gbc);
                    
                    cmp.add(p);
                    if(i == 0) {
                        topInset = 5;
                    }
                }
                if(commitsSize < fullSize) {
                    JPanel p = new JPanel(new GridBagLayout());
                    p.setOpaque(false);
                            
                    final Action action = new OpenCommitIntervalAction(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_OpenIDE")); // NOI18N
                    final String compareUrl = getCompareUrl();
        
                    LinkLabel showAll = new LinkLabel(NbBundle.getMessage(ScmActivityDisplayer.class, "LBL_ShowAllCommits", fullSize)) { // NOI18N
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Action a = action;
                            if (!a.isEnabled()) {
                                a = getOpenBrowserAction(compareUrl);
                            }
                            a.actionPerformed(new ActionEvent(ScmActivityDisplayer.this, ActionEvent.ACTION_PERFORMED, null));
                        }
                    };
                    showAll.setPopupActions(action, getOpenBrowserAction(compareUrl));
                    
                    gbc.weightx = 0;
                    p.add(showAll, gbc);
                    gbc.weightx = 1;
                    p.add(new JLabel(), gbc);          
                    cmp.add(p);
                }
                return cmp;
            } 
        }
        return null;
    }

    static String skipLineBreaks(String text) {
        if(text == null || "".equals(text)) { // NOI18N
            return ""; // NOI18N
        }      
        String ret = text;
        while (ret.startsWith("\n") && !ret.isEmpty()) { // NOI18N
            ret = ret.substring(1);
        } 
        while (ret.endsWith("\n") && !ret.isEmpty()) { // NOI18N
            ret = ret.substring(0, ret.length() - 1);
        } 
        int idx = ret.indexOf("\n"); // NOI18N
        return idx > -1 ? 
                idx == ret.length() - 1 ? 
                    ret.substring(0, idx) : 
                    ret.substring(0, idx) + " ..." // NOI18N
                : ret;
    }

    @Override
    protected String getUserMail() {
        return getUserMailFromActivity(activity);
    }
    
    @Override
    String getUserName() {
        return getUserNameFromActivity(activity);
    }

    @Override
    public Icon getActivityIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/activity_commit.png", true); // NOI18N
        // TODO need a different icon for repository activities
    }

    private String getMinimizedCommitId(int idx) {
        String commitId = activity.getProperty(String.format(PROP_COMMITS_HASH, idx));            
            
        if (commitId != null && commitId.length() > 7) {
            return commitId.substring(0, 7);
        }
        return commitId;
    }

    private String getCommitUrl(int idx) {
        String url = Utils.getWebUrl(scmUrl);
        if (!url.endsWith("/")) { //NOI18N
            url += "/"; //NOI18N
        }
        url += activity.getProperty(PROP_COMMIT_REPOSITORY) + "/commit/" + activity.getProperty(String.format(PROP_COMMITS_HASH, idx)); // NOI18N
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
    
    private String getBranchUrl(String branch) {
        String url = Utils.getWebUrl(scmUrl);
        if (!url.endsWith("/")) { //NOI18N
            url += "/"; //NOI18N
        }
        url += activity.getProperty(PROP_COMMIT_REPOSITORY) + "/tree?revision=" + branch; // NOI18N
        return url;
    }
    
    private String getCompareUrl() {
        String url = Utils.getWebUrl(scmUrl);
        if (!url.endsWith("/")) { // NOI18N
            url += "/"; // NOI18N
        }
        url += activity.getProperty(PROP_COMMIT_REPOSITORY) + "/compare/" + activity.getProperty(PROP_BASE) + "..." + activity.getProperty(PROP_HEAD) + "?tab=commits"; // NOI18N
        return url;
    }

    private String toHtml(String commentFull) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>"); // NOI18N
        sb.append(commentFull.replaceAll("\n", "<br>")); // NOI18N
        sb.append("</html>"); // NOI18N
        return sb.toString();
    }

    private class OpenCommitIntervalAction extends AbstractOpenAction {
        public OpenCommitIntervalAction(String name) {
            super(name);
        }
        @Override
        protected Action getIdeAction(VCSAccessor accessor) {
            return accessor.getOpenHistoryAction(projectHandle,
                    activity.getProperty(PROP_COMMIT_REPOSITORY),
                    activity.getProperty(PROP_BASE),
                    activity.getProperty(PROP_HEAD));
        }
        @Override
        protected Action getBrowserAction() {
            return getOpenBrowserAction(getCompareUrl());
        }
    }
    
    private class OpenBranchAction extends AbstractOpenAction {
        public OpenBranchAction(String name) {
            super(name);
        }
        @Override
        protected Action getIdeAction(VCSAccessor accessor) {
            return accessor.getOpenHistoryBranchAction(projectHandle,
                    activity.getProperty(PROP_COMMIT_REPOSITORY),
                    activity.getProperty(PROP_BRANCH));
        }
        @Override
        protected Action getBrowserAction() {
            return getOpenBrowserAction(getBranchUrl(activity.getProperty(PROP_BRANCH)));
        }
    }
    
    private class OpenCommitAction extends AbstractOpenAction {
        private final int idx;
        public OpenCommitAction(String name, int idx) {
            super(name);
            this.idx = idx;
        }
        @Override
        protected Action getIdeAction(VCSAccessor accessor) {
            return accessor.getOpenHistoryAction(projectHandle,
                                    activity.getProperty(PROP_COMMIT_REPOSITORY),
                                    activity.getProperty(String.format(PROP_COMMITS_HASH, idx)));
        }
        @Override
        protected Action getBrowserAction() {
            return getOpenBrowserAction(getCommitUrl(idx));
        }
    }
    
    private abstract class AbstractOpenAction extends AbstractAction {

        private final VCSAccessor accessor;

        public AbstractOpenAction(String name) {
            super(name);
            accessor = VCSAccessor.getDefault();
        }
        
        protected abstract Action getIdeAction(VCSAccessor accessor);    
        protected abstract Action getBrowserAction();    

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (isEnabled()) {
                RequestProcessor.getDefault().post(() -> {
                    Action action = getIdeAction(accessor);
                    if (action == null) {
                        action = getBrowserAction();
                    }
                    final ActionListener fAction = action;
                    UIUtils.runInAWT(() -> {
                        fAction.actionPerformed(e);
                    });
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
