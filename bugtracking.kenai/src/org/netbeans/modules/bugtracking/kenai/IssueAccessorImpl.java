/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.kenai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.jira.JiraUpdater;
import org.netbeans.modules.bugtracking.kenai.spi.RecentIssue;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author joshis
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.class)
public class IssueAccessorImpl extends KenaiIssueAccessor {

    /**
     * Open a TC with an issue in the IDE
     * @param project Kenai project
     * @param issueID Issue identifier
     */
    @Override
    public void open(final KenaiProject project, final String issueID) {

        FakeJiraSupport support = FakeJiraSupport.get(project);
        if(support != null) {
            // this is a jira project
            if(!BugtrackingUtil.isJiraInstalled()) {
                if(JiraUpdater.notifyJiraDownload(support.getIssueUrl(issueID))) {
                    JiraUpdater.getInstance().downloadAndInstall();
                }
                return;
            }
        }

        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_GETTING_REPO"));
        handle.start();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final Repository repo = KenaiUtil.getRepository(KenaiProjectImpl.getInstance(project));
                handle.finish();
                try {
                    Issue.open(repo, issueID);
                } catch (NullPointerException e) {
                    //
                }
            }
        });
    }

    @Override
    public IssueHandle[] getRecentIssues() {
        Map<String, List<RecentIssue>> recentIssues = BugtrackingUtil.getAllRecentIssues();
        Repository[] knownRepos = BugtrackingUtil.getKnownRepositories(false);
        Map<String, Repository> repoMap = new HashMap<String, Repository>(knownRepos.length);
        for (Repository repository : knownRepos) {
            repoMap.put(repository.getID(), repository);
        }

        Map<String, KenaiProject> issueToKenaiProject = new HashMap<String, KenaiProject>();
        List<RecentIssue> retIssues = new ArrayList<RecentIssue>(5);
        for(Map.Entry<String, List<RecentIssue>> entry : recentIssues.entrySet()) {
            Repository repo = repoMap.get(entry.getKey());
            if(repo == null) {
                Support.LOG.fine("No repository available with ID " + entry.getKey()); // NOI18N
                continue;
            }
            KenaiProject kenaiProject = repo.getLookup().lookup(KenaiProject.class);
            if(kenaiProject == null) {
                continue;
            }

            for(RecentIssue ri : entry.getValue()) {
                if(retIssues.size() > 5) {
                    retIssues.remove(5);
                }
                if(retIssues.isEmpty()) {
                    retIssues.add(ri);
                    issueToKenaiProject.put(ri.getIssue().getID(), kenaiProject);
                } else {
                    for (int i = 0; i < retIssues.size(); i++) {
                        if(ri.getTimestamp() > retIssues.get(i).getTimestamp()) {
                            retIssues.add(i, ri);
                            issueToKenaiProject.put(ri.getIssue().getID(), kenaiProject);
                            break;
                        } else if (retIssues.size() < 5) {
                            retIssues.add(retIssues.size(), ri);
                            issueToKenaiProject.put(ri.getIssue().getID(), kenaiProject);
                            break;
                        }
                    }
                }
            }
        }

        List<IssueHandle> ret = new ArrayList<IssueHandle>(retIssues.size());
        for (RecentIssue recentIssue : retIssues) {
            KenaiProject kenaiProject = issueToKenaiProject.get(recentIssue.getIssue().getID());
            assert kenaiProject != null;
            ret.add(new IssueHandleImpl(recentIssue.getIssue(), kenaiProject));
        }
        return ret.toArray(new IssueHandle[ret.size()]);
    }

    @Override
    public IssueHandle[] getRecentIssues(KenaiProject project) {
        assert project != null;
        if(project == null) {
            return new IssueHandle[0];
        }
        Repository repo = KenaiUtil.getRepository(KenaiProjectImpl.getInstance(project), false);
        if(repo == null) {
            // looks like no repository was created for the project yet, and if there's no repository
            // then there can't be any recent issue for it...
            Support.LOG.fine("No issue tracker available for the given kenai project [" + project.getName() + "," + project.getDisplayName() + "]"); // NOI18N
            return new IssueHandle[0];
        }
        Collection<Issue> issues = BugtrackingUtil.getRecentIssues(repo);
        if(issues == null) {
            return new IssueHandle[0];
        }

        List<IssueHandle> ret = new ArrayList<IssueHandle>(issues.size());
        for (Issue issue : issues) {
            IssueHandleImpl ih = new IssueHandleImpl(issue, project);
            ret.add(ih);
        }
        return ret.toArray(new IssueHandle[ret.size()]);
    }

    private class IssueHandleImpl extends IssueHandle {
        private final Issue issue;
        private final KenaiProject project;

        public IssueHandleImpl(Issue issue, KenaiProject project) {
            this.issue = issue;
            this.project = project;
        }

        @Override
        public String getID() {
            return issue.getID();
        }

        @Override
        public KenaiProject getProject() {
            return project;
        }

        @Override
        public String getShortDisplayName() {
            return issue.getShortenedDisplayName();
        }

        @Override
        public String getDisplayName() {
            return issue.getDisplayName();
        }

        @Override
        public boolean isOpened() {
            return BugtrackingUtil.isOpened(issue);
        }

        @Override
        public boolean isShowing() {
            return BugtrackingUtil.isShowing(issue);
        }

    }
}
