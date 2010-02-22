/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
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
        return repo.getLookup().lookup(KenaiProject.class) != null;
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
     * Returns a Repository coresponding to the given kenai url and a name. The url
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
     * Returns a Repository coresponding to the given kenai url and a name. The url
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
        return KenaiRepositories.getInstance().getRepository(project);
    }

    /**
     * @see KenaiRepositories#getRepository(org.netbeans.modules.bugtracking.kenai.spi.KenaiProject, boolean)
     */
    public static Repository getRepository(KenaiProject project, boolean forceCreate) {
        return KenaiRepositories.getInstance().getRepository(project, forceCreate);
    }

    /**
     * @see KenaiRepositories#getRepositories()
     */
    public static Repository[] getRepositories(boolean pingOpenProjects) {
        return KenaiRepositories.getInstance().getRepositories(pingOpenProjects);
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

    public static String getChatLink(Issue issue) {
        return "ISSUE:" + issue.getID(); // NOI18N
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

}
