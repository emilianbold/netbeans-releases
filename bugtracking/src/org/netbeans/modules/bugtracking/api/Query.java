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
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiQueryProvider;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Stupka
 */
public final class Query {
    private Bind<?, ?> bind;
    private final Repository repository;

    <Q, I> Query(Repository repository, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, Q data) {
        this.repository = repository;
        this.bind = new Bind(queryProvider, issueProvider, data);
        queryProvider.addPropertyChangeListener(data, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(QueryProvider.EVENT_QUERY_REMOVED.equals(evt.getPropertyName()) ||
                   QueryProvider.EVENT_QUERY_SAVED.equals(evt.getPropertyName())) 
                {
                    Query.this.repository.fireQueryListChanged();
                }
            }
        });
    }

    public boolean isSaved() {
        return bind.isSaved();
    }

    public String getTooltip() {
        return bind.getTooltip();
    }

    public Repository getRepository() {
        return repository;
    }

    public Collection<Issue> getIssues(int includeStatus) {
        return Collections.unmodifiableCollection(bind.getIssues(includeStatus));
    }
    
    public Collection<Issue> getIssues() {
        return Collections.unmodifiableCollection(bind.getIssues());
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

    public Collection<Issue> getIssues(String criteria) {
        return Collections.unmodifiableCollection(BugtrackingUtil.getByIdOrSummary(getIssues(), criteria));
    }
    
    public String getDisplayName() {
        return bind.getDisplayName();
    }

    BugtrackingController getController() {
        return bind.getController();
    }

    /**
     * @param query
     */
    public static void openNew(Repository repository) {
        QueryAction.openQuery(null, repository);
    }
    
    public void open(final boolean suggestedSelectionOnly) {
        QueryAction.openQuery(this, repository, suggestedSelectionOnly);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        bind.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        bind.removePropertyChangeListener(listener);                   
    }

    boolean contains(String id) {
        return bind.contains(id);
    }
    
    QueryProvider getProvider() {
        return bind.queryProvider;
    }

    Object getData() {
        return bind.data;
    }

    void setContext(Node[] context) {
        bind.setContext(context);
    }

    void setFilter(Filter filter) {
        bind.setFilter(filter);
    }

    boolean needsLogin() {
        return bind.needsLogin();
    }
    
    void refresh(boolean synchronously) {
        bind.refresh(synchronously);
    }

    private final class Bind<Q, I> {
        private final QueryProvider<Q, I> queryProvider;
        private final Q data;
        private final IssueProvider<I> issueProvider;

        public Bind(QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, Q data) {
            this.queryProvider = queryProvider;
            this.issueProvider = issueProvider;
            this.data = data;
        }
        
        public boolean isSaved() {
            return queryProvider.isSaved(data);
        }
        
        public String getTooltip() {
            return queryProvider.getTooltip(data);
        }

        public boolean contains(String id) {
            return queryProvider.contains(data, id);
        }
        
        public Collection<Issue> getIssues(int includeStatus) {
            Collection<I> issues = queryProvider.getIssues(data);
            List<Issue> ret = new ArrayList<Issue>(issues.size());
            for (I i : issues) {
                Issue issue = repository.findIssue(i);
                int status = IssueCacheUtils.getStatus(issue);
                if((includeStatus & status) != 0) {
                    ret.add(issue); // XXX API cache
                }
            }
            return ret;
        }
        
        public Collection<Issue> getIssues() {
            return getIssues(~0);
        }

        public String getDisplayName() {
            return queryProvider.getDisplayName(data);
        }

        public BugtrackingController getController() {
            return queryProvider.getController(data);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            queryProvider.addPropertyChangeListener(data, listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            queryProvider.removePropertyChangeListener(data, listener);                   
        }

        private void setContext(Node[] context) {
            queryProvider.setContext(data, context);
        }

        private void setFilter(Filter filter) {
            assert KenaiQueryProvider.class.isAssignableFrom(queryProvider.getClass());
            ((KenaiQueryProvider<Q, I>)queryProvider).setFilter(data, filter);
        }

        private boolean  needsLogin() {
            assert KenaiQueryProvider.class.isAssignableFrom(queryProvider.getClass());
            return ((KenaiQueryProvider<Q, I>)queryProvider).needsLogin(data);
        }

        private void refresh(boolean synchronously) {
            assert KenaiQueryProvider.class.isAssignableFrom(queryProvider.getClass());
            ((KenaiQueryProvider<Q, I>)queryProvider).refresh(data, synchronously);
        }

    }
}
