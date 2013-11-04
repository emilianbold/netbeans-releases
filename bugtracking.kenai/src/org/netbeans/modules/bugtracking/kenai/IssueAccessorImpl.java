/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.kenai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.team.spi.RecentIssue;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.bugtracking.kenai.Bundle.*;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

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
    @Messages("LBL_GETTING_REPO=Contacting remote repository...")
    public void open(final KenaiProject project, final String issueID) {

        FakeJiraSupport support = FakeJiraSupport.get(project);
        if(support != null) {
            // this is a jira project
            TeamUtil.downloadAndInstallJira(support.getIssueUrl(issueID));
        }

        final ProgressHandle handle = ProgressHandleFactory.createHandle(LBL_GETTING_REPO());
        handle.start();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(project));
                handle.finish();
                try {
                    if(issueID != null) {
                        Util.openIssue(repo, issueID);
                    } else {
                        Util.createNewIssue(repo);
                    }
                } catch (NullPointerException e) {
                    //
                }
            }
        });
    }

    @Override
    public IssueHandle[] getRecentIssues() {
        Collection<RecentIssue> recentIssues = Util.getRecentIssues();

        Map<String, TeamProjectImpl> issueToKenaiProject = new HashMap<String, TeamProjectImpl>();
        List<RecentIssue> retIssues = new ArrayList<RecentIssue>(5);
        for(RecentIssue ri : recentIssues) {
            Issue issue = ri.getIssue();
            Repository repo = issue.getRepository();
            TeamProjectImpl kenaiProject = (TeamProjectImpl) TeamUtil.getTeamProject(repo);
            if(kenaiProject == null) {
                continue;
            }
            if(retIssues.size() > 5) {
                retIssues.remove(5);
            }
            if(retIssues.isEmpty()) {
                retIssues.add(ri);
                issueToKenaiProject.put(issue.getID(), kenaiProject);
            } else {
                for (int i = 0; i < retIssues.size(); i++) {
                    if(ri.getTimestamp() > retIssues.get(i).getTimestamp()) {
                        retIssues.add(i, ri);
                        issueToKenaiProject.put(issue.getID(), kenaiProject);
                        break;
                    } else if (retIssues.size() < 5) {
                        retIssues.add(retIssues.size(), ri);
                        issueToKenaiProject.put(issue.getID(), kenaiProject);
                        break;
                    }
                }
            }
        }

        List<IssueHandle> ret = new ArrayList<IssueHandle>(retIssues.size());
        for (RecentIssue ri : retIssues) {
            TeamProjectImpl kenaiProject = issueToKenaiProject.get(ri.getIssue().getID());
            assert kenaiProject != null;
            ret.add(new IssueHandleImpl(ri.getIssue(), kenaiProject.getProject()));
        }
        return ret.toArray(new IssueHandle[ret.size()]);
    }

    @Override
    public IssueHandle[] getRecentIssues(KenaiProject project) {
        assert project != null;
        if(project == null) {
            return new IssueHandle[0];
        }
        Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(project), false);
        if(repo == null) {
            // looks like no repository was created for the project yet, and if there's no repository
            // then there can't be any recent issue for it...
            Support.LOG.log(Level.FINE, "No issue tracker available for the given kenai project [{0},{1}]", new Object[]{project.getName(), project.getDisplayName()}); // NOI18N
            return new IssueHandle[0];
        }
        Collection<RecentIssue> recentIssues = Util.getRecentIssues();
        if(recentIssues == null) {
            return new IssueHandle[0];
        }

        Collection<Issue> issues = new LinkedList<Issue>();
        for (RecentIssue ri : recentIssues) {
            Repository recentRepo = ri.getIssue().getRepository();
            if(recentRepo.getId().equals(repo.getId()) && recentRepo.getUrl().equals(repo.getUrl())) {
                issues.add(ri.getIssue());
            }
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
        public boolean isShowing() {
            return TeamUtil.isShowing(issue);
        }

    }
}
