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

import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.UndoRedoSupport;

/**
 *
 * @author Tomas Stupka
 */
public final class BugtrackingFactory<R, Q, I> {
   
    /**
     * 
     * @param info
     * @param r
     * @param rp
     * @param ip
     * @param qp
     * @return 
     */
    public Repository createRepository(R r, 
            RepositoryProvider<R, Q, I> rp, 
            QueryProvider<Q, I> qp,
            IssueProvider<I> ip) 
    {
        RepositoryInfo info = rp.getInfo(r);
        if(info != null) {
            String repositoryId = info.getId();
            String connectorId = rp.getInfo(r).getConnectorId();
            Repository repo = getRepository(connectorId, repositoryId);
            if(repo != null) {
                return repo;
            }
        }
        RepositoryImpl<R, Q, I> impl = new RepositoryImpl<R, Q, I>(r, rp, qp, ip);
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
        Query query = getQuery(repository, q);
        return BugtrackingUtil.isOpened(APIAccessor.IMPL.getImpl(query));
    }
    
    public void openQuery(Repository repository, Q q) {
        Query query = getQuery(repository, q);
        if(query != null) {
            query.open(false);
        }
    }
    
    public void openIssue(Repository repository, I i) {
        Issue issue = getIssue(repository, i);
        if (issue != null) {
            issue.open();
        } 
    }
    
    public UndoRedoSupport getUndoRedoSupport(Repository repository, I i) {
        return UndoRedoSupport.getSupport(getIssueImpl(null, i));
    }
    
    private Query getQuery(Repository repository, Q q) {
        RepositoryImpl<R, Q, I> repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        QueryImpl impl = repositoryImpl.getQuery(q);
        if(impl == null) {
            return null;
        }
        return impl.getQuery();
    }
    
    private Issue getIssue(Repository repository, I i) {
        IssueImpl impl = getIssueImpl(repository, i);
        return impl != null ? impl.getIssue() : null;
    }   
    
    private IssueImpl getIssueImpl(Repository repository, I i) {
        RepositoryImpl<R, Q, I> repositoryImpl = APIAccessor.IMPL.getImpl(repository);
        return repositoryImpl.getIssue(i);
    }
    
}
