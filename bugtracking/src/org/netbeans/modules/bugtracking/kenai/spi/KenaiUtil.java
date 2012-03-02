/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiBugtrackingConnector.BugtrackingType;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class KenaiUtil {

    public static KenaiAccessor getKenaiAccessor() {
        return BugtrackingManager.getInstance().getKenaiAccessor();
    }

    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     * @see isLoggedIn(java.lang.String)
     */
    public static boolean isLoggedIn(URL url) {
        return isLoggedIn(url.toString());
    }

    /**
     * @see KenaiAccessor#isLoggedIn(java.lang.String)
     */
    public static boolean isLoggedIn(String url) {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.isLoggedIn(url) : false;
    }

    /**
     * Returns true if the given repository is a Kenai repository
     *
     * @param repo
     * @return
     */
    public static boolean isKenai(Repository repo) {
        return APIAccessor.IMPL.getImpl(repo).getLookup().lookup(KenaiProject.class) != null;
    }

    /**
     * @see KenaiAccessor#getPasswordAuthentication(java.lang.String, boolean)
     */
    public static PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.getPasswordAuthentication(url, forceLogin) : null;
    }

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public static boolean showLogin() {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.showLogin() : false;
    }


    /**
     * Returns a RepositoryProvider coresponding to the given kenai url and a name. The url
     * might be either a kenai vcs repository, an issue or the kenai server url.
     * @param repositoryUrl
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String repositoryUrl) throws IOException {
        KenaiProject project = getKenaiProjectForRepository(repositoryUrl);
        return (project != null)
               ? getRepository(project)
               : null;        //not a Kenai project repository
    }

    /**
     * Returns a RepositoryProvider coresponding to the given kenai url and a name. The url
     * might be either a kenai vcs repository, an issue or the kenai server url.
     *
     * @param url
     * @param projectName
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String url, String projectName) throws IOException {
        KenaiProject p = getKenaiProject(url, projectName);
        return p != null ? getRepository(p) : null;
    }

    /**
     * @see KenaiRepositories#getRepository(org.netbeans.modules.bugtracking.kenai.spi.KenaiProject)
     */
    public static Repository getRepository(KenaiProject project) {
        return KenaiRepositories.getInstance().getRepository(project).getRepository();
    }

    /**
     * @see KenaiRepositories#getRepository(org.netbeans.modules.bugtracking.kenai.spi.KenaiProject, boolean)
     */
    public static Repository getRepository(KenaiProject project, boolean forceCreate) {
        return KenaiRepositories.getInstance().getRepository(project, forceCreate).getRepository();
    }

    /**
     * @see KenaiRepositories#getRepositories()
     */
    public static Collection<Repository> getRepositories(boolean pingOpenProjects) {
        Collection<RepositoryImpl> impls = KenaiRepositories.getInstance().getRepositories(pingOpenProjects);
        List<Repository> ret = new ArrayList<Repository>(impls.size());
        for (RepositoryImpl impl : impls) {
            ret.add(impl.getRepository());
        }
        return ret;
    }

    /**
     * @see KenaiAccessor#getProjectMembers(org.netbeans.modules.bugtracking.kenai.spi.KenaiProject)
     */
    public static Collection<RepositoryUser> getProjectMembers(KenaiProject kp) {
        KenaiAccessor ka = getKenaiAccessor();
        try {
            return ka != null ? ka.getProjectMembers(kp) : Collections.EMPTY_LIST;
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            return Collections.EMPTY_LIST;
        }
    }

    public static String getChatLink(String id) {
        return "ISSUE:" + id; // NOI18N
    }
    
    /**
     * Refreshes existing openend kenai queries
     */
    public static void refreshOpenedQueries() {
        Set<QueryTopComponent> tcs = QueryTopComponent.getOpenQueries(); // XXX updates also non kenai TC
        for (QueryTopComponent tc : tcs) {
            tc.updateSavedQueries();
        }
    }

    /**
     * @see KenaiAccessor#isNetbeansKenaiRegistered()
     */
    public static boolean isNetbeansKenaiRegistered() {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.isNetbeansKenaiRegistered() : false;
    }

    /**
     * @see KenaiAccessor#createUserWidget(java.lang.String, java.lang.String, java.lang.String)
     */
    public static JLabel createUserWidget(String userName, String host, String chatMessage) {
        KenaiAccessor ka = getKenaiAccessor();
        assert ka != null; 
        return ka.createUserWidget(userName, host, chatMessage);
    }

    /**
     * @see KenaiAccessor#getOwnerInfo(org.openide.nodes.Node)
     */
    public static OwnerInfo getOwnerInfo(Node node) {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.getOwnerInfo(node) : null;
    }

    /**
     * @see KenaiAccessor#getOwnerInfo(java.io.File)
     */
    public static OwnerInfo getOwnerInfo(File file) {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null ? ka.getOwnerInfo(file) : null;
    }

    /**
     * @see KenaiAccessor#logKenaiUsage(java.lang.Object[])
     */
    public static void logKenaiUsage(Object... parameters) {
        KenaiAccessor ka = getKenaiAccessor();
        if(ka != null) {
            ka.logKenaiUsage(parameters);
        }
    }

    /**
     * @see KenaiAccessor#getKenaiProjectForRepository(java.lang.String)
     */
    public static KenaiProject getKenaiProjectForRepository(String repositoryUrl) throws IOException {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null? ka.getKenaiProjectForRepository(repositoryUrl) : null;
    }

    /**
     * @see KenaiAccessor#getKenaiProject(java.lang.String, java.lang.String)
     */
    public static KenaiProject getKenaiProject(String url, String projectName) throws IOException {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null? ka.getKenaiProject(url, projectName) : null;
    }

    /**
     * @see KenaiAccessor#getDashboardProjects() 
     */
    public static KenaiProject[] getDashboardProjects() {
        KenaiAccessor ka = getKenaiAccessor();
        return ka != null? ka.getDashboardProjects() : new KenaiProject[0];
    }

    public static Repository findNBRepository() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector c : connectors) {
            BugtrackingConnector bugtrackingConnector = c.getDelegate();
            if ((bugtrackingConnector instanceof KenaiBugtrackingConnector)) {
                KenaiBugtrackingConnector kenaiConnector = (KenaiBugtrackingConnector) bugtrackingConnector;
                if(kenaiConnector.getType() == BugtrackingType.BUGZILLA) {
                    return kenaiConnector.findNBRepository(); // ensure repository exists
                }
            }
        }
        return null;
    }
    
    public static void addRepository(Repository repository) {
        RepositoryRegistry.getInstance().addRepository(APIAccessor.IMPL.getImpl(repository));
    }
    
    public static KenaiProject getKenaiProject(Repository repository) {
        return APIAccessor.IMPL.getImpl(repository).getLookup().lookup(KenaiProject.class);
    }

    public static Query getAllIssuesQuery(Repository repository) {
        return APIAccessor.IMPL.getImpl(repository).getAllIssuesQuery();
    }
    
    public static Query getMyIssuesQuery(Repository repository) {
        return APIAccessor.IMPL.getImpl(repository).getMyIssuesQuery();
    }

    public static boolean needsLogin(Query query) {
        return APIAccessor.IMPL.getImpl(query).needsLogin();
    }

    public static void setFilter(Query query, Filter filter) {
        APIAccessor.IMPL.getImpl(query).setFilter(filter);
    }

    public static BugtrackingType getType(Repository repo) {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector delegatingConnector : connectors) {
            if (delegatingConnector.getID().equals(APIAccessor.IMPL.getImpl(repo).getConnectorId())) {
                BugtrackingConnector bugtrackignConnector = delegatingConnector.getDelegate();
                assert bugtrackignConnector instanceof KenaiBugtrackingConnector;
                return ((KenaiBugtrackingConnector)bugtrackignConnector).getType();
            }
        }
        assert false : "no KenaiSupport available for repository [" + repo.getDisplayName() + "]";  // NOI18N
        return null;
    }

    public static Filter getAllFilter(Query query) {
        return Filter.getAllFilter(APIAccessor.IMPL.getImpl(query));
    }

    public static Filter getNotSeenFilter(Query query) {
        return Filter.getNotSeenFilter(APIAccessor.IMPL.getImpl(query));
    }

    public static Filter getNewFilter(Query query) {
        return Filter.getNewFilter(APIAccessor.IMPL.getImpl(query));
    }

    public static void closeQuery(Query query) {
        QueryAction.closeQuery(APIAccessor.IMPL.getImpl(query));
    }

    public static void createIssue(Repository repo) {
        IssueAction.createIssue(APIAccessor.IMPL.getImpl(repo));
    }

    public static void openQuery(final Query query, final Repository repository, final boolean suggestedSelectionOnly) {
        QueryAction.openQuery(
                query != null ? APIAccessor.IMPL.getImpl(query) : null, 
                repository != null ? APIAccessor.IMPL.getImpl(repository) : null, 
                suggestedSelectionOnly);
    }

    public static Collection<Issue> getRecentIssues(Repository repo) {
        Collection<IssueImpl> c = BugtrackingUtil.getRecentIssues(APIAccessor.IMPL.getImpl(repo));
        List<Issue> ret = new ArrayList<Issue>(c.size());
        for (IssueImpl impl : c) {
            ret.add(impl.getIssue());
        }
        return ret;
    }

    public static Collection<Repository> getKnownRepositories(boolean b) {
        Collection<RepositoryImpl> c = BugtrackingUtil.getKnownRepositories(b);
        List<Repository> ret = new ArrayList<Repository>(c.size());
        for (RepositoryImpl impl : c) {
            ret.add(impl.getRepository());
        }
        return ret;
    }

    public static void addCacheListener(Issue issue, PropertyChangeListener l) {
        IssueCacheUtils.addCacheListener(APIAccessor.IMPL.getImpl(issue), l);
    }
    
    public static void removeCacheListener(Issue issue, PropertyChangeListener l) {
        IssueCacheUtils.removeCacheListener(APIAccessor.IMPL.getImpl(issue), l);
    }

    public static boolean isOpen(Issue issue) {
        return BugtrackingUtil.isOpened(APIAccessor.IMPL.getImpl(issue));
    }
    
    public static boolean isShowing(Issue issue) {
        return BugtrackingUtil.isOpened(APIAccessor.IMPL.getImpl(issue));
    }
}
