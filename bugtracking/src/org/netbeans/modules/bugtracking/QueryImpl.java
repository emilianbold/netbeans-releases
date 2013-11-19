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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public final class QueryImpl<Q, I>  {
    
    private final RepositoryImpl<?, Q, I> repository;
    private final QueryProvider<Q, I> queryProvider;
    private final IssueProvider<I> issueProvider;
    private Query query;
    private final Q data;
    private final IssueContainerIntern issueContainer;
    private boolean wasRefreshed = false;
    QueryImpl(RepositoryImpl repository, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider, Q data) {
        this.queryProvider = queryProvider;
        this.issueProvider = issueProvider;
        this.data = data;
        this.repository = repository;
        this.issueContainer = new IssueContainerIntern();
        
        queryProvider.setIssueContainer(data, SPIAccessor.IMPL.createIssueContainer(issueContainer));
    }
    
    public synchronized Query getQuery() {
        if(query == null) {
            query = APIAccessor.IMPL.createQuery(this);
        }
        return query;
    }

    public RepositoryImpl<?, Q, I> getRepositoryImpl() {
        return repository;
    }
    
    public Collection<IssueImpl> getIssues() {
        return issueContainer.getIssues();
    }

    /**
     * @param query
     */
    public static void openNew(RepositoryImpl repository) {
        QueryAction.createNewQuery(repository);
    }
    
    public void open(QueryController.QueryMode mode) {
        QueryAction.openQuery(this, repository, mode);
    }
    
    public boolean canRemove() {
        return queryProvider.canRemove(data);
    }
    
    public void remove() {
        queryProvider.remove(data);
    }
    
    public boolean canRename() {
        return queryProvider.canRename(data);
    }
    
    public void rename(String newName) {
        queryProvider.rename(data, newName);
    }
    
    public String getTooltip() {
        return queryProvider.getTooltip(data);
    }

    public void refresh() {
        queryProvider.refresh(data);
    }
    
    public boolean wasRefreshed() {
        return wasRefreshed;
    }
    
    public String getDisplayName() {
        return queryProvider.getDisplayName(data);
    }

    public QueryController getController() {
        return queryProvider.getController(data);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        issueContainer.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        issueContainer.removePropertyChangeListener(listener);
    }

    public void setContext(OwnerInfo info) {
        repository.setQueryContext(data, info);
    }

    public boolean isData(Object obj) {
        return data == obj;
    }

    public boolean providesMode(QueryController.QueryMode queryMode) {
        QueryController controller = queryProvider.getController(data);
        return controller != null ? controller.providesMode(queryMode) : false;
    }
    
    Q getData() {
        return data;
    }

    public static final String EVENT_QUERY_STARTED = "bugtracking.query.started";
    public static final String EVENT_QUERY_FINISHED = "bugtracking.query.finished";
    private class IssueContainerIntern implements IssueContainerImpl<I> {
        private final Set<IssueImpl> issueImpls = new HashSet<IssueImpl>();
        @Override
        public void refreshingStarted() {
            wasRefreshed = true;
            support.firePropertyChange(EVENT_QUERY_STARTED, null, null);
        }

        @Override
        public void refreshingFinished() {
            support.firePropertyChange(EVENT_QUERY_FINISHED, null, null);
        }

        @Override
        public void add(I... issues) {
            for (I i : issues) {
                IssueImpl issue = repository.getIssue(i);
                if(issue != null) {
                    issueImpls.add(issue);
                }
            }
        }

        @Override
        public void remove(I... issues) {
            for (I i : issues) {
                IssueImpl issue = repository.getIssue(i);
                if(issue != null) {
                    issueImpls.remove(issue);
                }            
            }
        }
        
        Collection<IssueImpl> getIssues() {
            return Collections.unmodifiableCollection(issueImpls);
        }
        
        private final PropertyChangeSupport support = new PropertyChangeSupport(data);
        
        void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);                   
        }
    
    }
}
