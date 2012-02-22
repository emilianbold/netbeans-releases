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

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
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
public abstract class APIAccessor {
    
    public static APIAccessor IMPL;
    
    static {
        // invokes static initializer of Repository.class
        // that will assign value to the IMPL field above
        Class c = Repository.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }  
    
    public abstract RepositoryProvider convert(Repository repo);
    public abstract <I> IssueProvider<I> convert(Issue Issue);
    public abstract <Q, I> QueryProvider<Q, I> convert(Query Issue);
    
    public abstract Issue findIssue(Repository repository, Object i);
    
    public abstract Query findQuery(Repository repository, Object q);
    
    public abstract RepositoryController getController(Repository repo);
    
    public abstract <R, Q, I> Repository create(BugtrackingConnector connector, R r, RepositoryProvider<R, Q, I> rp, IssueProvider<I> ip, QueryProvider<Q, I> qp);
    
    public abstract BugtrackingController getController(Query query);
    public abstract BugtrackingController getController(Issue issue);

    public abstract Issue createNewIssue(Repository repo);

    public abstract Query createNewQuery(Repository repo);

    public abstract Collection<Issue> simpleSearch(Repository repository, String criteria);

    public abstract Collection<Query> getQueries(Repository repo);

    public abstract DelegatingConnector getConnector(Repository repo);

    public abstract void removed(Repository repository);

    public abstract String getConnectorId(Repository repository);
    public abstract RepositoryInfo getInfo(Repository repository);

    public abstract boolean isSaved(Query query);
    public abstract boolean contains(Query query, String id);
    public abstract Lookup getLookup(Repository repository);

    public abstract void setContext(Query query, Node[] context);
    public abstract void setContext(Issue issue, Node[] context);

    public abstract void applyChanges(Repository repository) throws IOException;
    
    /********************************************************************************
     * Kenai
     *******************************************************************************/ 
    
    public abstract void setFilter(Query query, Filter filter);
    public abstract boolean needsLogin(Query query);
    public abstract void refresh(Query query, boolean synchronously);
    public abstract Query getAllIssuesQuery(Repository repository);
    public abstract Query getMyIssuesQuery(Repository repository);

    
}
