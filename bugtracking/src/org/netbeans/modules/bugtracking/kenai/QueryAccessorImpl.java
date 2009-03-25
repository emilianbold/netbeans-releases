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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.kenai.ui.spi.QueryAccessor.class)
public class QueryAccessorImpl extends QueryAccessor implements PropertyChangeListener {

    final private Map<String, ProjectHandleListener> projectListeners = new HashMap<String, ProjectHandleListener>();

    public QueryAccessorImpl() {
        Kenai.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public List<QueryHandle> getQueries(ProjectHandle project) {
        Repository repo = KenaiRepositories.getInstance().getRepository(project, this);
        if(repo == null) {
            // XXX log this inconvenience
            return Collections.emptyList();
        }
        List<QueryHandle> queries = getQueryHandles(repo);
        ProjectHandleListener pl;
        synchronized(projectListeners) {
            pl = projectListeners.get(project.getId());
        }
        if(pl != null) {
            project.removePropertyChangeListener(pl);
        }
        pl = new ProjectHandleListener(project, queries);
        project.addPropertyChangeListener(pl);
        synchronized(projectListeners) {
            projectListeners.put(project.getId(), pl);
        }
        return Collections.unmodifiableList(queries);
    }

    List<QueryHandle> getQueryHandles(Repository repo) {
        Query[] queries = repo.getQueries();
        if(queries == null || queries.length == 0) {
            // XXX is this possible - at least preset queries
            return Collections.emptyList();
        }
        List<QueryHandle> ret = new ArrayList<QueryHandle>();
        for (Query q : queries) {
            QueryHandle qh = new QueryHandleImpl(q);
            ret.add(qh);
        }
        return ret;
    }

    @Override
    public List<QueryResultHandle> getQueryResults(QueryHandle query) {
        if(query instanceof QueryHandleImpl) {
            QueryHandleImpl qh = (QueryHandleImpl) query;
            qh.refreshIfFirstTime();
            return Collections.unmodifiableList(qh.getQueryResults());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ActionListener getFindIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project, this);
        if(repo == null) {
            return null;
        }
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        QueryAction.openKenaiQuery(null, repo);
                    }
                });          
            }
        };
    }


    @Override
    public ActionListener getCreateIssueAction(ProjectHandle project) {
        final Repository repo = KenaiRepositories.getInstance().getRepository(project, this);
        if(repo == null) {
            return null;
        }
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() { // XXX add post method to BM
                    public void run() {
                        IssueAction.openIssue(null, repo);
                    }
                });
            }
        };
    }


    @Override
    public ActionListener getOpenQueryResultAction(QueryResultHandle result) {
        if(result instanceof QueryResultHandle) {
            return (ActionListener) result;
        } else {
            return null;
        }
    }

    @Override
    public ActionListener getDefaultAction(QueryHandle query) {
        if(query instanceof QueryHandleImpl) {
            return (ActionListener) query;
        } else {
            return null;
        }
    }

    void fireQueriesChanged(ProjectHandle project, List<QueryHandle> newQueryList) {
        fireQueryListChanged(project, newQueryList);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Kenai.PROP_LOGIN) && evt.getNewValue() == null) {
            ProjectHandleListener[] pls;
            synchronized(projectListeners) {
                pls = projectListeners.values().toArray(new ProjectHandleListener[projectListeners.values().size()]);
            }
            for (ProjectHandleListener pl : pls) {
                pl.closeQueries();
            }
        }
    }
    
    private class ProjectHandleListener implements PropertyChangeListener {
        private List<QueryHandle> queries;
        private ProjectHandle ph;
        public ProjectHandleListener(ProjectHandle ph, List<QueryHandle> queries) {
            this.queries = queries;
            this.ph = ph;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ProjectHandle.PROP_CLOSE)) {
                closeQueries();
            }
        }
        public void closeQueries() {
            for (QueryHandle qh : queries) {
                QueryAction.closeQuery(((QueryHandleImpl) qh).getQuery());
            }
            synchronized (projectListeners) {
                projectListeners.remove(ph.getId());
            }
        }
    }
}
