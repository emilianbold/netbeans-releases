/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 *
 * @author Tomas Stupka
 * 
 * @param <R> the implementation specific repository type
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 */
public final class BugtrackingFactory<R, Q, I> {
   
    /**
     * Creates a {@link Repository} instance configured with the given providers.
     * 
     * @param r a implementation specific repository 
     * @param repositoryProvider a {@link RepositoryProvider} to access the implementation specific repository.<br/> 
     *                           Is mandatory and cannot be null.
     * @param queryProvider a {@link QueryProvider} to access queries from the given repository.<br/>
     *                      Is mandatory and cannot be null.
     * @param issueProvider an {@link IssueProvider} to access issues from the given repository.<br/>
     *                      Is mandatory and cannot be null.
     * 
     * @return a {@link Repository} instance
     */
    public Repository createRepository(R r, 
            RepositoryProvider<R, Q, I> repositoryProvider, 
            QueryProvider<Q, I> queryProvider,
            IssueProvider<I> issueProvider) 
    {
        RepositoryInfo info = repositoryProvider.getInfo(r);
        if(info != null) {
            String repositoryId = info.getId();
            String connectorId = repositoryProvider.getInfo(r).getConnectorId();
            Repository repo = getRepository(connectorId, repositoryId);
            if(repo != null) {
                return repo;
            }
        }
        RepositoryImpl<R, Q, I> impl = new RepositoryImpl<R, Q, I>(r, repositoryProvider, queryProvider, issueProvider, null, null, null, null);
        return impl.getRepository();
    }
    
    /**
     * Creates a {@link Repository} instance configured with the given providers.
     * 
     * @param r a implementation specific repository 
     * @param repositoryProvider a {@link RepositoryProvider} to access the implementation specific repository.<br/> 
     *                           Is mandatory and cannot be null.   
     * @param queryProvider a {@link QueryProvider} to access queries from the given repository.<br/>
     *                      Is mandatory and cannot be null.
     * @param issueProvider an {@link IssueProvider} to access issues from the given repository.<br/>
     *                      Is mandatory and cannot be null.    
     * @param issueStatusProvider an {@link IssueStatusProvider} to provide status information 
     *                            of an implementation specific issue.<br/> 
     *                            Might be null.
     * @param issueSchedulingProvider an {@link IssueSchedulingProvider} to provide scheduling information 
     *                                of an implementation specific issue.<br/> 
     *                                Might be null.
     * @param issuePriorityProvider an {@link IssuePriorityProvider} to provide priority information 
     *                              of an implementation specific issue.<br/> 
     *                              Might be null.
     * @param issueFinder an {@link IssueFinder} to find issue references in text..<br/> 
     *                    Might be null.
     * 
     * @return a {@link Repository} instance
     */
    public Repository createRepository(R r, 
            RepositoryProvider<R, Q, I> repositoryProvider, 
            QueryProvider<Q, I> queryProvider,
            IssueProvider<I> issueProvider,
            IssueStatusProvider<I> issueStatusProvider,
            IssueSchedulingProvider<I> issueSchedulingProvider, 
            IssuePriorityProvider<I> issuePriorityProvider,
            IssueFinder issueFinder)
    {
        RepositoryInfo info = repositoryProvider.getInfo(r);
        if(info != null) {
            String repositoryId = info.getId();
            String connectorId = repositoryProvider.getInfo(r).getConnectorId();
            Repository repo = getRepository(connectorId, repositoryId);
            if(repo != null) {
                return repo;
            }
        }
        RepositoryImpl<R, Q, I> impl = new RepositoryImpl<R, Q, I>(r, repositoryProvider, queryProvider, issueProvider, issueStatusProvider, issueSchedulingProvider, issuePriorityProvider, issueFinder);
        return impl.getRepository();
    }
    
    public Repository getRepository(String connectorId, String repositoryId) {
        RepositoryImpl impl = RepositoryRegistry.getInstance().getRepository(connectorId, repositoryId);
        if(impl == null) {
            return null;
        }
        return impl.getRepository();
    }
    
    public boolean isOpen(Repository repository, Q q) {
        QueryImpl query = getQueryImpl(repository, q);
        return BugtrackingUtil.isOpened(query);
    }
    
    public void editQuery(Repository repository, Q q) {
        QueryImpl query = getQueryImpl(repository, q);
        if(query != null) {
            query.open(QueryController.QueryMode.EDIT);
        }
    }
    
    public void openIssue(Repository repository, I i) {
        IssueImpl issue = getIssueImpl(repository, i);
        if (issue != null) {
            issue.open();
        }
    }
    
    public void addToCategory(final Repository repository, final I i) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueImpl issue = getIssueImpl(repository, i);
                if (issue != null) {
                    DashboardTopComponent.findInstance().addTask(issue);
                }
            }
        });
    }
    
    public boolean editRepository(Repository repository, String errorMessage) {
        return BugtrackingUtil.editRepository(APIAccessor.IMPL.getImpl(repository), errorMessage);
    }

    public Image[] getPriorityIcons() {
        return IssuePrioritySupport.getIcons();
    }
    
    private QueryImpl getQueryImpl(Repository repository, Q q) {
        RepositoryImpl<R, Q, I> repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        QueryImpl impl = repositoryImpl.getQuery(q);
        if(impl == null) {
            return null;
        }
        return impl;
    }
    
    private IssueImpl getIssueImpl(Repository repository, I i) {
        RepositoryImpl<R, Q, I> repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        return repositoryImpl.getIssue(i);
    }
    
}
