/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
class APIAccessorImpl extends APIAccessor {

    static void createAccesor() {
        if (IMPL == null) {
            IMPL = new APIAccessorImpl();
        }
    }

    @Override
    public RepositoryProvider convert(Repository repo) {
        return repo.getProvider();
    }

    @Override
    public IssueProvider convert(Issue issue) {
        return issue.getProvider();
    }
    
    @Override
    public <R, Q, I> Repository create(BugtrackingConnector connector, R r, RepositoryProvider<R, Q, I> rp, IssueProvider<I> ip, QueryProvider<Q, I> qp) {
        return new Repository(connector, r, rp, qp, ip);
    }

    @Override
    public RepositoryController getController(Repository repo) {
        return repo.getProvider().getController(repo.getData());
    }

    @Override
    public BugtrackingController getController(Query query) {
        return query.getProvider().getController(query.getData());
    }
    
    @Override
    public BugtrackingController getController(Issue issue) {
        return issue.getProvider().getController(issue.getData());
    }

    @Override
    public Collection<Issue> simpleSearch(Repository repository, String criteria) {
        return repository.simpleSearch(criteria);
    }

    @Override
    public Collection<Query> getQueries(Repository repo) {
        return repo.getQueries();
    }

    @Override
    public DelegatingConnector getConnector(Repository repo) {
        return repo.getConnector();
    }

    @Override
    public void removed(Repository repository) {
        convert(repository).remove(repository.getData());
    }

    @Override
    public RepositoryInfo getInfo(Repository repository) {
        return convert(repository).getInfo(repository.getData());
    }

    @Override
    public QueryProvider convert(Query query) {
        return query.getProvider();
    }

    @Override
    public Issue createNewIssue(Repository repository) {
        return repository.createNewIssue();
    }

    @Override
    public Query createNewQuery(Repository repository) {
        return repository.createNewQuery();
    }

    @Override
    public Issue findIssue(Repository repository, Object i) {
        return repository.findIssue(i);
    }

    @Override
    public Query findQuery(Repository repository, Object i) {
        return repository.findQuery(i);
    }

    @Override
    public String getConnectorId(Repository repository) {
        return repository.getConnector().getID();
    }

    @Override
    public boolean isSaved(Query query) {
        return query.isSaved();
    }

    @Override
    public boolean contains(Query query, String id) {
        return query.contains(id);
    }

    @Override
    public Lookup getLookup(Repository repository) {
        return repository.getLookup();
    }

    @Override
    public void setContext(Query query, Node[] context) {
        query.setContext(context);
    }
    
    @Override
    public void setContext(Issue issue, Node[] context) {
        issue.setContext(context);
    }

    @Override
    public void setFilter(Query query, Filter filter) {
        query.setFilter(filter);
    }

    @Override
    public boolean needsLogin(Query query) {
        return query.needsLogin();
    }

    @Override
    public void refresh(Query query, boolean synchronously) {
        query.refresh(synchronously);
    }

    @Override
    public Query getAllIssuesQuery(Repository repository) {
        return repository.getAllIssuesQuery();
    }

    @Override
    public Query getMyIssuesQuery(Repository repository) {
        return repository.getMyIssuesQuery();
    }

    @Override
    public void applyChanges(Repository repository)  throws IOException {
        repository.applyChanges();
    }

}
