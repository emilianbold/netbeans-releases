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

package org.netbeans.modules.bugtracking.util;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.KenaiRepositoryUtils;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiProjectMember;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class KenaiUtil {

    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     */
    public static boolean isLoggedIn(URL url) {
        return isKenai(url.toString());
    }

    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     */
    public static boolean isLoggedIn(String url) {
        Kenai kenai = getKenai(url);
        return (kenai != null) && (kenai.getPasswordAuthentication() != null);
    }

    /**
     * Returns true if logged into kenai, otherwise false.
     *
     * @return
     */
    public static boolean isLoggedIn(Kenai kenai) {
        return kenai.getPasswordAuthentication() != null;
    }

    /**
     * Returns true if the given url belongs to a kenai project
     * 
     * @param url
     * @return
     */
    public static boolean isKenai(String url) {
        return KenaiProject.getNameForRepository(url) != null;
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
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials.
     *
     * @param url a {@link Kenai} instance url
     * @param forceLogin  forces a login if user not logged in
     * @return PasswordAuthentication
     */
    public static PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        Kenai kenai = getKenai(url);
        assert kenai != null : "no kenai for url : [" + url + "]";
        return getPasswordAuthentication(kenai, forceLogin);
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actuall
     * Kenai credentials.
     *
     * @param url a {@link Kenai} instance url
     * @param forceLogin  forces a login if user not logged in
     * @return PasswordAuthentication
     */
    public static PasswordAuthentication getPasswordAuthentication(Kenai kenai, boolean forceLogin) {
        PasswordAuthentication a = kenai.getPasswordAuthentication();
        if(a != null) {
            return a;
        }

        if(!forceLogin) {
            return null;
        }

        if(!showLogin()) {
            return null;
        }

        return kenai.getPasswordAuthentication();
    }

    /**
     * Opens the kenai login dialog.
     * @return true if login successfull, otherwise false
     */
    public static boolean showLogin() {
        return UIUtils.showLogin();
    }
    
    /**
     * Returns a kenai repository for a given kenai project
     * @param project kenai project
     * @return null if no repository exists for the given project
     */
    public static Repository getKenaiBugtrackingRepository( KenaiProject project) {
        return KenaiRepositoryUtils.getInstance().getRepository(project);
    }

    /**
     * Returns a kenai repository for a given kenai project's name
     * @param projectName kenai project name
     * @throws KenaiException
     * @return null if no repository exists for the given project's name
     */
    public static Repository getKenaiBugtrackingRepository (String url, String projectName) throws KenaiException {
        Kenai kenai = getKenai(url);
        assert kenai != null : "no kenai for url : [" + url + "]";
        KenaiProject p = kenai.getProject(projectName);
        return p != null ? getKenaiBugtrackingRepository(p) : null;
    }

    /**
     * Returns a kenai {@link Repository} for the kenai project the with given vcs url
     *
     * @param url
     * @return
     */
    public static Repository getKenaiRepository(String url) {
        KenaiProject kp = getKenaiProject(url);
        return kp == null ? null : KenaiRepositoryUtils.getInstance().getRepository(kp);
    }

    /**
     * Returns a URL of web location of a kenai project associated with given repository url
     * @param sourcesUrl url of a kenai vcs repository
     * @return web location of associated kenai project or null if no such project exists
     */
    public static String getProjectUrl (String sourcesUrl) {
        KenaiProject kp = getKenaiProject(sourcesUrl);
        return kp == null ? null : kp.getWebLocation().toString();
    }

    public static Collection<RepositoryUser> getProjectMembers(KenaiProject kp) {
        List<RepositoryUser> members = null;
        try {
            KenaiProjectMember[] kenaiMembers = kp.getMembers();
            members = new ArrayList<RepositoryUser>(kenaiMembers.length);
            for (KenaiProjectMember member : kenaiMembers) {
                KenaiUser user = member.getKenaiUser();
                members.add(new RepositoryUser(user.getUserName(), user.getFirstName()+" "+user.getLastName())); // NOI18N
            }
        } catch (KenaiException kex) {
            kex.printStackTrace();
        }
        if (members == null) {
            members = Collections.emptyList();
        }
        return members;
    }

    public static KenaiProject getKenaiProject(ProjectHandle ph) {        
        return ph.getKenaiProject();
    }

    public static String getChatLink(Issue issue) {
        return "ISSUE:" + issue.getID(); // NOI18N
    }

    private static KenaiProject getKenaiProject(String url) {
        KenaiProject kp;
        try {
            kp = KenaiProject.forRepository(url);
        } catch (KenaiException ex) {
            return null;
        }
        return kp;
    }

    public static Kenai getKenai(String url) {
        // 1st - full url match
        Kenai kenaiCandidate = KenaiManager.getDefault().getKenai(url);
        if (kenaiCandidate != null) {
            return kenaiCandidate;
        }
        // 2nd - VCS repository url match
        try {
            KenaiProject kp = KenaiProject.forRepository(url);
            if (kp != null) {
                return kp.getKenai();
            }
        } catch (KenaiException ex) {
            BugtrackingManager.LOG.log(Level.FINE, url, ex);
        }
        // 3rd - bugtracking issue url match
        for (Kenai kenai : KenaiManager.getDefault().getKenais()) {
            String kenaiUrl = kenai.getUrl().toString();
            if (url.startsWith(kenaiUrl)) {
                BugtrackingManager.LOG.log(Level.FINE, "getKenai: url {0} matches kenai url {1}", new String[] {url, kenaiUrl}); //NOI18N
                return kenai;
            }
        }
        return null;
    }

}
