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

package org.netbeans.modules.bugtracking.team.spi;

import org.netbeans.modules.bugtracking.team.TeamRepositories;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.jira.JiraUpdater;
import org.netbeans.modules.bugtracking.team.spi.TeamBugtrackingConnector.BugtrackingType;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class TeamUtil {

    public static TeamAccessor[] getTeamAccessors() {
        return BugtrackingManager.getInstance().getTeamAccessors();
    }

    public static TeamAccessor getTeamAccessor (String url) {
        TeamAccessor accessor = null;
        for (TeamAccessor ka : getTeamAccessors()) {
            if (ka.isOwner(url)) {
                accessor = ka;
                break;
            }
        }
        return accessor;
    }

    /**
     * Returns true if logged into a team server, otherwise false.
     *
     * @return
     * @see isLoggedIn(java.lang.String)
     */
    public static boolean isLoggedIn(URL url) {
        return isLoggedIn(url.toString());
    }

    /**
     * @see TeamAccessor#isLoggedIn(java.lang.String)
     */
    public static boolean isLoggedIn(String url) {
        for (TeamAccessor ka : getTeamAccessors()) {
            if (ka.isLoggedIn(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given repository is a Team repository
     *
     * @param repo
     * @return
     */
    public static boolean isFromTeamServer(Repository repo) {
        return getTeamProject(repo) != null;
    }

    /**
     * @see TeamAccessor#getPasswordAuthentication(java.lang.String, boolean)
     */
    public static PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        for (TeamAccessor ka : getTeamAccessors()) {
            PasswordAuthentication pa = ka.getPasswordAuthentication(url, forceLogin);
            if (pa != null) {
                return pa;
            }
        }
        return null;
    }

    /**
     * Returns a RepositoryProvider coresponding to the given team url and a name. The url
     * might be either a team vcs repository, an issue or the team server url.
     * @param repositoryUrl
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String repositoryUrl) throws IOException {
        TeamProject project = getTeamProjectForRepository(repositoryUrl);
        return (project != null)
               ? getRepository(project)
               : null;        //not a team project repository
    }

    /**
     * Returns a RepositoryProvider coresponding to the given team url and a name. The url
     * might be either a team vcs repository, an issue or the team server url.
     *
     * @param url
     * @param projectName
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String url, String projectName) throws IOException {
        TeamProject p = getTeamProject(url, projectName);
        return p != null ? getRepository(p) : null;
    }

    /**
     * @see TeamRepositories#getRepository(org.netbeans.modules.bugtracking.team.spi.TeamProject)
     */
    public static Repository getRepository(TeamProject project) {
        RepositoryImpl impl = TeamRepositories.getInstance().getRepository(project);
        return impl != null ? impl.getRepository() : null;
    }

    /**
     * @see TeamRepositories#getRepository(org.netbeans.modules.bugtracking.team.spi.TeamProject, boolean)
     */
    public static Repository getRepository(TeamProject project, boolean forceCreate) {
        RepositoryImpl impl = TeamRepositories.getInstance().getRepository(project, forceCreate);
        return impl != null ? impl.getRepository() : null;
    }
    
    /**
     * @see TeamRepositories#getRepositories()
     */
    public static Collection<Repository> getRepositories(String connectorId, boolean pingOpenProjects, boolean onlyDashboardOpenProjects) {
        Collection<RepositoryImpl> impls = TeamRepositories.getInstance().getRepositories(pingOpenProjects, onlyDashboardOpenProjects);
        List<Repository> ret = new ArrayList<Repository>(impls.size());
        for (RepositoryImpl impl : impls) {
            if(connectorId.equals(impl.getConnectorId())) {
                ret.add(impl.getRepository());
            }
        }
        return ret;
    }

    /**
     * @see TeamAccessor#getProjectMembers(org.netbeans.modules.bugtracking.team.spi.TeamProject)
     */
    public static Collection<RepositoryUser> getProjectMembers(TeamProject kp) {
        for (TeamAccessor ka : getTeamAccessors()) {
            try {
                Collection<RepositoryUser> projectMembers = ka.getProjectMembers(kp);
                if (projectMembers != null) {
                    return projectMembers;
                }
            } catch (IOException ex) {
                BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            }
        }
        return Collections.EMPTY_LIST;
    }

    public static String getChatLink(String id) {
        return "ISSUE:" + id; // NOI18N
    }
    
    /**
     * @see TeamAccessor#isNetbeansTeamRegistered()
     */
    public static boolean isNBTeamServerRegistered() {
        for (TeamAccessor ka : getTeamAccessors()) {
            if (ka.isNBTeamServerRegistered()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see TeamAccessor#createUserWidget(java.lang.String, java.lang.String, java.lang.String)
     * @return may return null
     */
    public static JLabel createUserWidget (String url, String userName, String host, String chatMessage) {
        TeamAccessor ka = getTeamAccessor(url);
        assert ka != null; 
        return ka.createUserWidget(userName, host, chatMessage);
    }

    /**
     * @see TeamAccessor#getOwnerInfo(org.openide.nodes.Node)
     */
    public static OwnerInfo getOwnerInfo(Node node) {
        for (TeamAccessor ka : getTeamAccessors()) {
            OwnerInfo ownerInfo = ka.getOwnerInfo(node);
            if (ownerInfo != null) {
                return ownerInfo;
            }
        }
        return null;
    }

    /**
     * @see TeamAccessor#getOwnerInfo(java.io.File)
     */
    public static OwnerInfo getOwnerInfo(File file) {
        for (TeamAccessor ka : getTeamAccessors()) {
            OwnerInfo ownerInfo = ka.getOwnerInfo(file);
            if (ownerInfo != null) {
                return ownerInfo;
            }
        }
        return null;
    }

    /**
     * @see TeamAccessor#logTeamUsage(java.lang.Object[])
     */
    public static void logTeamUsage(String url, Object... parameters) {
        TeamAccessor ka = getTeamAccessor(url);
        if(ka != null) {
            ka.logTeamUsage(parameters);
        }
    }

    /**
     * @see TeamAccessor#getTeamProjectForRepository(java.lang.String)
     */
    public static TeamProject getTeamProjectForRepository(String repositoryUrl) throws IOException {
        for (TeamAccessor ka : getTeamAccessors()) {
            TeamProject kp = ka.getTeamProjectForRepository(repositoryUrl);
            if (kp != null) {
                return kp;
            }
        }
        return null;
    }

    /**
     * @see TeamAccessor#getTeamProject(java.lang.String, java.lang.String)
     */
    public static TeamProject getTeamProject(String url, String projectName) throws IOException {
        for (TeamAccessor ka : getTeamAccessors()) {
            TeamProject kp = ka.getTeamProject(url, projectName);
            if (kp != null) {
                return kp;
            }
        }
        return null;
    }

    /**
     * @see TeamAccessor#getDashboardProjects() 
     */
    public static TeamProject[] getDashboardProjects(boolean onlyOpened) {
        List<TeamProject> projs = new LinkedList<TeamProject>();
        for (TeamAccessor ka : getTeamAccessors()) {
            projs.addAll(Arrays.asList(ka.getDashboardProjects(onlyOpened)));
        }
        return projs.toArray(new TeamProject[projs.size()]);
    }

    public static Repository findNBRepository() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector c : connectors) {
            BugtrackingConnector bugtrackingConnector = c.getDelegate();
            if ((bugtrackingConnector instanceof TeamBugtrackingConnector)) {
                TeamBugtrackingConnector teamConnector = (TeamBugtrackingConnector) bugtrackingConnector;
                if(teamConnector.getType() == BugtrackingType.BUGZILLA) {
                    return teamConnector.findNBRepository(); // ensure repository exists
                }
            }
        }
        return null;
    }
    
    public static void addRepository(Repository repository) {
        RepositoryRegistry.getInstance().addRepository(APIAccessor.IMPL.getImpl(repository));
    }
    
    public static TeamProject getTeamProject(Repository repository) {
        return APIAccessor.IMPL.getImpl(repository).getTeamProject();
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

    public static BugtrackingType getType(Repository repo) {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector delegatingConnector : connectors) {
            if (delegatingConnector.getID().equals(APIAccessor.IMPL.getImpl(repo).getConnectorId())) {
                BugtrackingConnector bugtrackignConnector = delegatingConnector.getDelegate();
                assert bugtrackignConnector instanceof TeamBugtrackingConnector;
                return ((TeamBugtrackingConnector)bugtrackignConnector).getType();
            }
        }
        assert false : "no TeamSupport available for repository [" + repo.getDisplayName() + "]";  // NOI18N
        return null;
    }

    public static void closeQuery(Query query) {
        QueryAction.closeQuery(APIAccessor.IMPL.getImpl(query));
    }

    public static void createIssue(Repository repo) {
        IssueAction.createIssue(APIAccessor.IMPL.getImpl(repo));
    }

    public static void openNewQuery(Repository repository, final boolean suggestedSelectionOnly) {
        QueryAction.openQuery(null, APIAccessor.IMPL.getImpl(repository), suggestedSelectionOnly);
    }
    
    public static void openQuery(final Query query, Query.QueryMode mode, final boolean suggestedSelectionOnly) {
        QueryImpl queryImpl = APIAccessor.IMPL.getImpl(query);
        DashboardTopComponent.findInstance().select(queryImpl, true);
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
        Collection<RepositoryImpl> c = RepositoryRegistry.getInstance().getKnownRepositories(b);
        List<Repository> ret = new ArrayList<Repository>(c.size());
        for (RepositoryImpl impl : c) {
            ret.add(impl.getRepository());
        }
        return ret;
    }

    public static void addCacheListener(Issue issue, PropertyChangeListener l) {
        APIAccessor.IMPL.getImpl(issue).addIssueStatusListener(l);
    }
    
    public static void removeCacheListener(Issue issue, PropertyChangeListener l) {
        APIAccessor.IMPL.getImpl(issue).removeIssueStatusListener(l);
    }

    public static boolean isOpen(Issue issue) {
        return BugtrackingUtil.isOpened(APIAccessor.IMPL.getImpl(issue));
    }
    
    public static boolean isShowing(Issue issue) {
        return BugtrackingUtil.isOpened(APIAccessor.IMPL.getImpl(issue));
    }

    public static boolean notifyJiraDownload(String projectUrl) {
        return JiraUpdater.notifyJiraDownload(projectUrl);
    }

    public static void downloadAndInstallJira() {
        JiraUpdater.getInstance().downloadAndInstall();
    }
}
