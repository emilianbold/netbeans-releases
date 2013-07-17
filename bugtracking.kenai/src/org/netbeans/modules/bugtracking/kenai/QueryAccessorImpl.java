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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.kenai.FakeJiraSupport.FakeJiraQueryHandle;
import org.netbeans.modules.bugtracking.kenai.FakeJiraSupport.FakeJiraQueryResultHandle;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.QueryHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ui.spi.QueryAccessor.class)
public class QueryAccessorImpl extends QueryAccessor<KenaiProject> {

    public QueryAccessorImpl() {
    }

    @Override
    public Class<KenaiProject> type() {
        return KenaiProject.class;
    }
    
    @Override
    public QueryHandle getAllIssuesQuery(ProjectHandle<KenaiProject> projectHandle) {
        Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(projectHandle.getTeamProject()));
        if(repo == null) {
            FakeJiraSupport jira = FakeJiraSupport.get(projectHandle);
            if (jira != null) {
                return jira.getAllIssuesQuery();
            }
            // XXX log this inconvenience
            return null;
        }

        if(!TeamUtil.isFromTeamServer(repo)) {
            return null;
        }

        KenaiHandler handler = Support.getInstance().getKenaiHandler(projectHandle, this);
        handler.registerRepository(repo, projectHandle);
        Query allIssuesQuery = TeamUtil.getAllIssuesQuery(repo);
        if(allIssuesQuery == null) {
            return null;
        }
        List<QueryHandle> queries = handler.getQueryHandles(projectHandle, allIssuesQuery);
        assert queries.size() == 1;
        handler.registerProject(projectHandle, queries);

        return queries.get(0);
    }

    @Override
    public List<QueryHandle> getQueries(ProjectHandle<KenaiProject> projectHandle) {
        Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(projectHandle.getTeamProject()));
        if(repo == null) {
            return getQueriesForNoRepo(projectHandle);
        }

        KenaiHandler handler = Support.getInstance().getKenaiHandler(projectHandle, this);
        // listen on repository events - e.g. a changed query list
        handler.registerRepository(repo, projectHandle);
        List<QueryHandle> queryHandles = handler.getQueryHandles(repo, projectHandle);
        // listen on project events - e.g. project closed
        handler.registerProject(projectHandle, queryHandles);

        return Collections.unmodifiableList(queryHandles);
    }

    @Override
    public List<QueryResultHandle> getQueryResults(QueryHandle queryHandle) {
        if(queryHandle instanceof QueryHandleImpl) {
            QueryHandleImpl qh = (QueryHandleImpl) queryHandle;
            qh.refreshIfNeeded();
            return Collections.unmodifiableList(qh.getQueryResults());
        } else if(queryHandle instanceof FakeJiraQueryHandle) {
            FakeJiraQueryHandle jqh = (FakeJiraQueryHandle) queryHandle;
            return jqh.getQueryResults();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Action getFindIssueAction(ProjectHandle<KenaiProject> projectHandle) {
        final Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(projectHandle.getTeamProject()));
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.get(projectHandle);
            if(jira != null) {
                return new ActionWrapper(jira.getOpenProjectListener());
            }
            return null;
        }
        return Support.getInstance().getKenaiHandler(projectHandle, this).getFindIssuesAction(repo);
    }

    @Override
    public Action getCreateIssueAction(ProjectHandle<KenaiProject> projectHandle) {
        final Repository repo = TeamUtil.getRepository(TeamProjectImpl.getInstance(projectHandle.getTeamProject()));
        if(repo == null) {
            // XXX dummy jira impl to open the jira page in a browser
            FakeJiraSupport jira = FakeJiraSupport.get(projectHandle);
            if(jira != null) {
                return new ActionWrapper(jira.getCreateIssueListener());
            }
            return null;
        }
        return Support.getInstance().getKenaiHandler(projectHandle, this).getCreateIssueAction(repo);
    }

    @Override
    public Action getOpenTaskAction (final ProjectHandle<KenaiProject> project, final String taskId) {
        final KenaiIssueAccessor impl = KenaiIssueAccessor.getDefault();
        return new AbstractAction() {

            @Override
            public void actionPerformed (ActionEvent e) {
                impl.open(project.getTeamProject(), taskId);
            }
        };
    }

    @Override
    public Action getOpenQueryResultAction(QueryResultHandle result) {
        if(result instanceof QueryResultHandleImpl ||
           result instanceof FakeJiraQueryResultHandle)
        {
            return new ActionWrapper((ActionListener) result);
        } else {
            return null;
        }
    }

    @Override
    public Action getDefaultAction(QueryHandle query) {
        if(query instanceof QueryHandleImpl ||
           query instanceof FakeJiraQueryHandle)
        {
            return new ActionWrapper((ActionListener) query);
        } else {
            return null;
        }
    }

    void fireQueriesChanged(ProjectHandle project, List<QueryHandle> newQueryList) {
        fireQueryListChanged(project, newQueryList);
    }

    private List<QueryHandle> getQueriesForNoRepo(ProjectHandle project) {
        FakeJiraSupport jira = FakeJiraSupport.get(project);
        if (jira != null) {
            return jira.getQueries();
        }
        // XXX log this inconvenience
        return Collections.emptyList();
    }

    @Override
    public boolean hasTasks(ProjectHandle<KenaiProject> project) {
        return true;
    }

    private static class ActionWrapper extends AbstractAction {
        private final ActionListener al;

        public ActionWrapper( ActionListener al ) {
            this.al = al;
        }
        public void actionPerformed(ActionEvent e) {
           al.actionPerformed(e);
        }
    }

}
