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
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiRepositoryProvider;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.nodes.RepositoryNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


/**
 * 
 * Represents a bug tracking repository (server)
 * 
 * @author Tomas Stupka
 */
public class RepositoryImpl<R, Q, I> {

    private final PropertyChangeSupport support;
    private RepositoryNode node;
        
    private final RepositoryProvider<R, Q, I> repositoryProvider;
    private final IssueProvider<I> issueProvider;
    private final QueryProvider<Q, I> queryProvider;
    private final R r;

    private Map<I, IssueImpl> issueMap = new HashMap<I, IssueImpl>();
    private Map<Q, QueryImpl> queryMap = new HashMap<Q, QueryImpl>();
    private Repository repository;
    
    public RepositoryImpl(R r, RepositoryProvider<R, Q, I> repositoryProvider, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider) {
        this.repositoryProvider = repositoryProvider;
        this.issueProvider = issueProvider;
        this.queryProvider = queryProvider;
        this.r = r;
        support = new PropertyChangeSupport(this);
        repositoryProvider.addPropertyChangeListener(r, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(RepositoryProvider.EVENT_QUERY_LIST_CHANGED.equals(evt.getPropertyName())) {
                    fireQueryListChanged();
                }
            }
        });
    }
    
    public Repository getRepository() {
        if(repository == null) {
            repository = APIAccessor.IMPL.createRepository(this);
        }
        return repository;
    }
    
    /**
     * Returns the icon for this repository
     * @return
     */
    public Image getIcon() {
        return repositoryProvider.getIcon(r);
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
    
    /**
     * Returns the display name for this repository
     * @return
     */
    public String getDisplayName() {
        return repositoryProvider.getInfo(r).getDisplayName();
    }

    /**
     * Returns the tooltip for this repository
     * @return
     */
    public String getTooltip() {
        return repositoryProvider.getInfo(r).getTooltip();
    }
    
    /**
     * Returns a unique ID for this repository
     * 
     * @return
     */
    public String getId() { // XXX API its either Id or ID
        return getInfo().getId();
    }

    public RepositoryInfo getInfo() {
        return repositoryProvider.getInfo(r);
    }
        
    /**
     * Returns the repositories url
     * @return
     */
    public String getUrl() {
        return getInfo().getUrl();
    }
    
    public Lookup getLookup() {
        return repositoryProvider.getLookup(r);
    }

    /**
     * Returns an issue with the given ID
     *
     * XXX add flag refresh
     *
     * @param id
     * @return
     */
    public Collection<IssueImpl> getIssueImpls(String... ids) {
        I[] is = repositoryProvider.getIssues(r, ids);
        if(is == null || is.length == 0) {
            return Collections.emptyList();
        }
        List<IssueImpl> ret = new ArrayList<IssueImpl>(is.length);
        for (I i : is) {
            IssueImpl impl = getIssue(i);
            if(impl != null) {
                ret.add(impl);
            }
        }
        return ret;
    }
    
    R getData() {
        return r;
    }

    public QueryImpl createNewQuery() {
        return getQuery(repositoryProvider.createQuery(r));
    }

    public IssueImpl createNewIssue() {
        I issueData = repositoryProvider.createIssue(r);
        return getIssue(issueData);
    }   

    public RepositoryProvider<R, Q, I> getProvider() {
        return repositoryProvider;
    }
    
    public Collection<IssueImpl> simpleSearch(String criteria) {
        Collection<I> issues = repositoryProvider.simpleSearch(r, criteria);
        List<IssueImpl> ret = new ArrayList<IssueImpl>(issues.size());
        for (I i : issues) {
            ret.add(getIssue(i));
        }
        return ret;
    }

    public Collection<QueryImpl> getQueries() {
        Collection<Q> queries = repositoryProvider.getQueries(r);
        List<QueryImpl> ret = new ArrayList<QueryImpl>(queries.size());
        for (Q q : queries) {
            ret.add(getQuery(q));
        }
        return ret;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Notify listeners on this repository that a query was either removed or saved
     * XXX make use of new/old value
     */
    void fireQueryListChanged() {
        support.firePropertyChange(Repository.EVENT_QUERY_LIST_CHANGED, null, null);
    }

    /**
     * Notify listeners on this repository that some of repository's attributes have changed.
     * @param oldValue map of old attributes
     * @param newValue map of new attributes
     */
    void fireAttributesChanged (java.util.Map<String, Object> oldAttributes, java.util.Map<String, Object> newAttributes) {
        LinkedList<String> equalAttributes = new LinkedList<String>();
        // find unchanged values
        for (Map.Entry<String, Object> e : newAttributes.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            Object oldValue = oldAttributes.get(key);
            if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
                equalAttributes.add(key);
            }
        }
        // remove unchanged values
        for (String equalAttribute : equalAttributes) {
            if (oldAttributes != null) {
                oldAttributes.remove(equalAttribute);
            }
            newAttributes.remove(equalAttribute);
        }
        if (!newAttributes.isEmpty()) {
            support.firePropertyChange(new java.beans.PropertyChangeEvent(this, Repository.EVENT_ATTRIBUTES_CHANGED, oldAttributes, newAttributes));
        }        
    }
    
    public void applyChanges() throws IOException {
        HashMap<String, Object> oldAttributes = createAttributesMap();
        repositoryProvider.getController(getData()).applyChanges();
        HashMap<String, Object> newAttributes = createAttributesMap();
        fireAttributesChanged(oldAttributes, newAttributes);
    }
    
    private HashMap<String, Object> createAttributesMap () {
        HashMap<String, Object> attributes = new HashMap<String, Object>(2);
        // XXX add more if requested
        if(getInfo() != null) {
            attributes.put(Repository.ATTRIBUTE_DISPLAY_NAME, getDisplayName());
            attributes.put(Repository.ATTRIBUTE_URL, getUrl());
        }
        return attributes;
    }

    public String getConnectorId() {
        return getInfo().getConnectorId();
    }

    public synchronized IssueImpl getIssue(I i) {
        if(i == null) {
            return null;
        }        
        IssueImpl issue = issueMap.get(i);
        if(issue == null) {
            issue = new IssueImpl(RepositoryImpl.this, issueProvider, i);
            issueMap.put(i, issue);
        }
        return issue;
    }

    public QueryImpl getQuery(Q q) {
        if(q == null) {
            return null;
        }
        QueryImpl query = queryMap.get(q);
        if(query == null) {
            query = new QueryImpl(RepositoryImpl.this, queryProvider, issueProvider, q);
            queryMap.put(q, query);
        }
        return query;
    }

    public Query getAllIssuesQuery() {
        assert KenaiRepositoryProvider.class.isAssignableFrom(repositoryProvider.getClass());
        Q q = ((KenaiRepositoryProvider<R, Q, I>) repositoryProvider).getAllIssuesQuery(r);
        QueryImpl queryImpl = getQuery(q);
        return queryImpl != null ? queryImpl.getQuery() : null;
    }

    public Query getMyIssuesQuery() {
        assert KenaiRepositoryProvider.class.isAssignableFrom(repositoryProvider.getClass());
        Q q = ((KenaiRepositoryProvider<R, Q, I>) repositoryProvider).getMyIssuesQuery(r);
        QueryImpl queryImpl = getQuery(q);
        return queryImpl != null ? queryImpl.getQuery() : null;
    }

    public void remove() {
        repositoryProvider.remove(r);
        RepositoryRegistry.getInstance().removeRepository(this);
    }

    public RepositoryController getController() {
        return repositoryProvider.getController(r);
    }

}

