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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.commons.JiraUpdater;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.bugtracking.kenai.Bundle.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
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
            JiraUpdater.getInstance().downloadAndInstall(support.getIssueUrl(issueID));
        }

        final ProgressHandle handle = ProgressHandleFactory.createHandle(LBL_GETTING_REPO());
        handle.start();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Repository repo = Util.getTeamRepository(project.getKenai().getUrl().toString(), project.getName());
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
        List<Issue> recentIssues = Util.getRecentIssues();
        List<IssueHandle> ret = new LinkedList<IssueHandle>();
        for (int i = 0; i < recentIssues.size() || i < 5; i++) {
            ret.add(new IssueHandleImpl(recentIssues.get(i)));
        }
        return ret.toArray(new IssueHandle[ret.size()]);
    }

    @Override
    public IssueHandle[] getRecentIssues(KenaiProject project) {
        assert project != null;
        if(project == null) {
            return new IssueHandle[0];
        }
        Repository repo = Util.getTeamRepository(project.getKenai().getUrl().toString(), project.getName());
        if(repo == null) {
            // ???
            Support.LOG.log(Level.FINE, "No issue tracker available for the given kenai project [{0},{1}]", new Object[]{project.getName(), project.getDisplayName()}); // NOI18N
            return new IssueHandle[0];
        }
        Collection<Issue> recentIssues = Util.getRecentIssues();
        if(recentIssues == null) {
            return new IssueHandle[0];
        }

        Collection<Issue> issues = new LinkedList<Issue>();
        for (Issue issue : recentIssues) {
            Repository recentRepo = issue.getRepository();
            if(recentRepo.getId().equals(repo.getId()) && recentRepo.getUrl().equals(repo.getUrl())) {
                issues.add(issue);
            }
        }        
                
        List<IssueHandle> ret = new ArrayList<IssueHandle>(issues.size());
        for (Issue issue : issues) {
            IssueHandleImpl ih = new IssueHandleImpl(issue);
            ret.add(ih);
        }
        return ret.toArray(new IssueHandle[ret.size()]);
    }

    private class IssueHandleImpl extends IssueHandle {
        private final Issue issue;

        public IssueHandleImpl(Issue issue) {
            this.issue = issue;
        }

        @Override
        public String getID() {
            return issue.getID();
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
            Mode editor = WindowManager.getDefault().findMode("editor"); //NOI18N
            TopComponent[] tcs = editor.getTopComponents();
            for (TopComponent tc : tcs) {
                if(issue == tc.getLookup().lookup(Issue.class)) {
                    return tc.isShowing();
                }
            }
            return false;
        }

    }
}
