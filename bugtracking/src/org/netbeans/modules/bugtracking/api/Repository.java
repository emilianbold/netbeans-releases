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

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiRepositoryProvider;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.ui.nodes.RepositoryNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * 
 * Represents a bug tracking repository (server)
 * 
 * @author Tomas Stupka
 */
public final class Repository {

    private RepositoryNode node;
        
    static {
        APIAccessorImpl.createAccesor();
    }

    private final Bind<?, ?, ?> bind;
    private final DelegatingConnector connector;

    <R, Q, I> Repository(BugtrackingConnector connector, R r, RepositoryProvider<R, Q, I> repositoryProvider, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider) {
        this.connector = findDelegatingConnector(connector);
        this.bind = new Bind(repositoryProvider, queryProvider, issueProvider, r);
    }

    private DelegatingConnector findDelegatingConnector(BugtrackingConnector connector) {
        if(connector == null) {
            BugtrackingManager.LOG.log(Level.WARNING, "Repository init with null connector");
            return null;
        }
        if(connector instanceof DelegatingConnector) {
            return (DelegatingConnector) connector;
        }
        DelegatingConnector[] conns = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector dc : conns) {
            if(dc.getDelegate() == connector) {
                return dc;
            }
        }
        BugtrackingManager.LOG.log(Level.WARNING, "No delegate for {0}", connector.getClass().getName());
        return null;
    }
    
    /**
     * Returns a {@link Node} representing this repository
     * 
     * @return
     * @deprecated 
     */
    public final Node getNode() {
        if(node == null) {
            node = new RepositoryNode(this);
        }
        return node;
    }    
    
    RepositoryProvider getProvider() {
        return bind.repositoryProvider;
    }
    
    /**
     * Returns the icon for this repository
     * @return
     */
    public Image getIcon() {
        return bind.getIcon();
    }

    /**
     * Returns the display name for this repository
     * @return
     */
    public String getDisplayName() {
        return bind.getInfo().getDisplayName();
    }

    /**
     * Returns the tooltip for this repository
     * @return
     */
    public String getTooltip() {
        return bind.getInfo().getTooltip();
    }

    /**
     * Returns a unique ID for this repository
     * 
     * @return
     */
    public String getId() { // XXX API its either Id or ID
        return bind.getInfo().getId();
    }

    /**
     * Returns the repositories url
     * @return
     */
    public String getUrl() {
        return bind.getInfo().getUrl();
    }
    
    public Lookup getLookup() {
        return bind.getLookup();
    }

    /**
     * Returns an issue with the given ID
     *
     * XXX add flag refresh
     *
     * @param id
     * @return
     */
    public Issue getIssue(String id) {
        return bind.getIssue(id);
    }
    
    Query findQuery(Object q) {
        return bind.getQuery(q);
    }
    
    Issue findIssue(Object i) {
        return bind.getIssue(i);
    }
    
    Object getData() {
        return bind.r;
    }

    Query createNewQuery() {
        return bind.createNewQuery();
    }

    Issue createNewIssue() {
        return bind.createNewIssue();
    }
    
    DelegatingConnector getConnector() {
        return connector;
    }

    Collection<Issue> simpleSearch(String criteria) {
        return bind.simpleSearch(criteria);
    }

    public Collection<Query> getQueries() {
        return bind.getQueries();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        bind.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        bind.removePropertyChangeListener(listener);
    }

    Query getAllIssuesQuery() {
        return bind.getAllIssuesQuery();
    }
    
    Query getMyIssuesQuery() {
        return bind.getAllIssuesQuery();
    }
    
    private final class Bind<R, Q, I> {
        private final RepositoryProvider<R, Q, I> repositoryProvider;
        private final IssueProvider<I> issueProvider;
        private final QueryProvider<Q, I> queryProvider;
        private final R r;
        
        public Bind(RepositoryProvider<R, Q, I> repositoryProvider, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, R r) {
            this.repositoryProvider = repositoryProvider;
            this.issueProvider = issueProvider;
            this.queryProvider = queryProvider;
            this.r = r;
        }

        Query createNewQuery() {
            return getQuery(repositoryProvider.createQuery(r));
        }

        Issue createNewIssue() {
            I issueData = repositoryProvider.createIssue(r);
            return getIssue(issueData);
        }    
        
        public RepositoryInfo getInfo() {
            return repositoryProvider.getInfo(r);
        }
        
        public Image getIcon() {
            return repositoryProvider.getIcon(r);
        }

        public String getDisplayName() {
            return repositoryProvider.getInfo(r).getDisplayName();
        }

        public String getTooltip() {
            return repositoryProvider.getInfo(r).getTooltip();
        }

        public String getId() { // XXX API its either Id or ID
            return repositoryProvider.getInfo(r).getId();
        }
        
        public String getUrl() {
            return repositoryProvider.getInfo(r).getUrl();
        }
        
        public Issue getIssue(String id) {
            return getIssue(issueProvider.createFor(id)); // XXX API cache me
        }
        
        private Map<I, Issue> issueMap = new WeakHashMap<I, Issue>();
        synchronized Issue getIssue(Object o) {
            I i = (I) o;
            Issue issue = issueMap.get(i);
            if(issue == null) {
                issue = new Issue(Repository.this, issueProvider, i);
                issueMap.put(i, issue);
            }
            return issue;
        }

        private Map<Q, Query> queryMap = new WeakHashMap<Q, Query>();
        private Query getQuery(Object o) {
            Q q = (Q) o;
            Query query = queryMap.get(q);
            if(query == null) {
                query = new Query(Repository.this, queryProvider, issueProvider, q);
                queryMap.put(q, query);
            }
            return query;
        }

        private Collection<Issue> simpleSearch(String criteria) {
            Collection<I> issues = repositoryProvider.simpleSearch(r, criteria);
            List<Issue> ret = new ArrayList<Issue>(issues.size());
            for (I i : issues) {
                ret.add(getIssue(i));
            }
            return ret;
        }

        private Collection<Query> getQueries() {
            Collection<Q> queries = repositoryProvider.getQueries(r);
            List<Query> ret = new ArrayList<Query>(queries.size());
            for (Q q : queries) {
                ret.add(getQuery(q));
            }
            return ret;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            repositoryProvider.addPropertyChangeListener(r, listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            repositoryProvider.removePropertyChangeListener(r, listener);
        }

        private Lookup getLookup() {
            return repositoryProvider.getLookup(r);
        }

        private Query getAllIssuesQuery() {
            assert KenaiRepositoryProvider.class.isAssignableFrom(repositoryProvider.getClass());
            return ((KenaiRepositoryProvider<R, Q, I>)repositoryProvider).getAllIssuesQuery(r);
        }
        
        private Query getMyIssuesQuery() {
            assert KenaiRepositoryProvider.class.isAssignableFrom(repositoryProvider.getClass());
            return ((KenaiRepositoryProvider<R, Q, I>)repositoryProvider).getMyIssuesQuery(r);
        }
    }
    
    
}

