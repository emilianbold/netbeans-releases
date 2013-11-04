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

import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.team.TeamRepositories;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.jira.JiraUpdater;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector.BugtrackingType;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.team.spi.TeamAccessorUtils;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class TeamUtil {

    /**
     * Returns a Repository corresponding to the given team url and a name. The url
     * might be either a team vcs repository, an issue or the team server url.
     * @param repositoryUrl
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String repositoryUrl) throws IOException {
        TeamProject project = TeamAccessorUtils.getTeamProjectForRepository(repositoryUrl);
        return (project != null)
               ? getRepository(project)
               : null;        //not a team project repository
    }

    /**
     * Returns a Repository corresponding to the given team url and a name. The url
     * might be either a team vcs repository, an issue or the team server url.
     *
     * @param url
     * @param projectName
     * @return
     * @throws IOException
     */
    public static Repository getRepository(String url, String projectName) throws IOException {
        TeamProject p = TeamAccessorUtils.getTeamProject(url, projectName);
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
    
    public static TeamProject getTeamProject(Repository repo) {
        return TeamRepositories.getInstance().getTeamProject(APIAccessor.IMPL.getImpl(repo));
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
    
    public static void setFirmAssociations(File[] files, Repository repository) {
        BugtrackingOwnerSupport.getInstance().setFirmAssociations(files, APIAccessor.IMPL.getImpl(repository));
    }
    
    public static boolean isShowing(Issue issue) {
        IssueTopComponent tc = IssueTopComponent.find(APIAccessor.IMPL.getImpl(issue), false);
        return tc != null ? tc.isOpened() : false;
    }

    public static void downloadAndInstallJira(String projectUrl) {
        JiraUpdater.getInstance().downloadAndInstall(projectUrl);
    }

}
