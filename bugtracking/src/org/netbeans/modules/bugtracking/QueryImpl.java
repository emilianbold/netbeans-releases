/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * <p/>
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 * <p/>
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 * <p/>
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * <p/>
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 * <p/>
 * Contributor(s):
 * <p/>
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.IssueTable.IssueTableProvider;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiQueryProvider;
import org.netbeans.modules.bugtracking.kenai.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 *
 * @author Tomas Stupka
 */
public final class QueryImpl<Q, I>  {
    private final RepositoryImpl repository;
    private final QueryProvider<Q, I> queryProvider;
    private final IssueProvider<I> issueProvider;
    private Query query;
    private final Q data;
            
    QueryImpl(RepositoryImpl repository, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, Q data) {
        this.queryProvider = queryProvider;
        this.issueProvider = issueProvider;
        this.data = data;
        this.repository = repository;
    }
    
    public Query getQuery() {
        if(query == null) {
            query = APIAccessor.IMPL.createQuery(this);
        }
        return query;
    }

    public RepositoryImpl getRepositoryImpl() {
        return repository;
    }
    
    public Collection<IssueImpl> getIssues() {
        Collection<I> issues = queryProvider.getIssues(data);
        List<IssueImpl> ret = new ArrayList<IssueImpl>(issues.size());
        for (I i : issues) {
            IssueImpl issue = repository.getIssue(i);
            ret.add(issue); // XXX API cache
        }
        return ret;
    }

    /**
     * Returns all issues given by the last refresh for
     * which applies that their ID or summary contains the
     * given criteria string
     * XXX used only by issue table filter - move out from spi
     *
     * @param criteria
     * @return
     */
    // XXX Shouldn't be called while running
    // XXX move to simple search

    public Collection<IssueImpl> getIssues(String criteria) {
        return Collections.unmodifiableCollection(BugtrackingUtil.getByIdOrSummary(getIssues(), criteria));
    }
    
    /**
     * @param query
     */
    public static void openNew(RepositoryImpl repository) {
        QueryAction.openQuery(null, repository);
    }
    
    public void open(final boolean suggestedSelectionOnly, Query.QueryMode mode) {
        switch(mode) {
            case SHOW_ALL:
                queryProvider.getController(data).setMode(QueryController.QueryMode.SHOW_ALL);
                break;
            case SHOW_NEW_OR_CHANGED:
                queryProvider.getController(data).setMode(QueryController.QueryMode.SHOW_NEW_OR_CHANGED);
                break;
            default:
                throw new IllegalStateException("Unsupported mode " + mode);
        }
        QueryAction.openQuery(this, repository, suggestedSelectionOnly);
    }
    
    public boolean isSaved() {
        return queryProvider.isSaved(data);
    }

    public void remove() {
        queryProvider.remove(data);
    }
    
    public String getTooltip() {
        return queryProvider.getTooltip(data);
    }

    public boolean contains(String id) {
        return queryProvider.contains(data, id);
    }

    public void refresh() {
        queryProvider.refresh(data);
    }
    
    public String getDisplayName() {
        return queryProvider.getDisplayName(data);
    }

    public QueryController getController() {
        return queryProvider.getController(data);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        queryProvider.addPropertyChangeListener(data, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        queryProvider.removePropertyChangeListener(data, listener);                   
    }

    public void setContext(OwnerInfo info) {
        assert (queryProvider instanceof KenaiQueryProvider);
        if((queryProvider instanceof KenaiQueryProvider)) {
            ((KenaiQueryProvider<Q, I>)queryProvider).setOwnerInfo(data, info);
        }
    }

    public boolean needsLogin() {
        assert (queryProvider instanceof KenaiQueryProvider);
        if((queryProvider instanceof KenaiQueryProvider)) {
            return ((KenaiQueryProvider<Q, I>)queryProvider).needsLogin(data);
        } 
        return false;
    }

    public IssueTable getIssueTable() {
        QueryController controller = getController();
        if((controller instanceof IssueTableProvider)) {
            return ((IssueTableProvider)controller).getIssueTable();
        } 
        return null;
    }

    public boolean isData(Object obj) {
        return data == obj;
    }

}
