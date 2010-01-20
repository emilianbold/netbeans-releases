/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.repository;

import java.net.URL;
import java.util.Collection;
import java.util.prefs.Preferences;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class NBRepositorySupport extends BugzillaRepository {

    private static final String NBBUGZILLA_PREFERENCES = "org/netbeans/modules/nbbugzilla";  // NOI18N
    private static final String NB_BUGZILLA_PASSWORD = "nbbugzilla.password";                // NOI18N
    private static final String NB_BUGZILLA_USERNAME = "nbbugzilla.username";                // NOI18N

    private static final String NB_BUGZILLA_HOST = "netbeans.org";           // NOI18N
    private static final String NB_BUGZILLA_URL = "https://" + NB_BUGZILLA_HOST + "/bugzilla";           // NOI18N

    private static Preferences nbUserPrefs;

    private static BugzillaRepository nbRepository;

    /**
     * Goes through all known bugzilla repositories and returns either the first 
     * which is registered for a netbeans.org url or creates a new with the netbeans
     * bugzilla url and registeres it under services/issue tracking. 
     * In case there is a non kenai and a kenai repository available, the non kenai will
     * be returned. User credentials will be reused in case that netbeans.org was
     * already accessed via exception reporter in the past and a new repository
     * was created.
     *
     * @return a BugzillaRepository
     */
    public static BugzillaRepository findNbRepository() {
        BugzillaRepository[] repos = Bugzilla.getInstance().getRepositories();
        for (BugzillaRepository repo : repos) {
            if(BugtrackingUtil.isNbRepository(repo)) {
                return repo;
            }
        }

        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        for (Kenai kenai : kenais) {
            URL url = kenai.getUrl();
            if(BugtrackingUtil.isNbRepository(url.toString())) { 
                // there is a nb kenai registered in the ide
                // create a new repo but _do not register_ in services
                nbRepository = createRepositoryIntern(); // XXX for now we keep a repository for each
                                                         //     nb kenai project. there will be no need
                                                         //     to create a new instance as soon as we will
                                                         //     have only one repository instance for all
                                                         //     kenai projects. see also issue #177578
            }
        }

        if(nbRepository == null) {                              // no nb repo yet ...
            nbRepository = createRepositoryIntern();            // ... create ...
            Bugzilla.getInstance().addRepository(nbRepository); // ... and register in services/issue tracking
        }

        return nbRepository;
    }

    /**
     * Returns a stored netbeans.org username or null otherwise
     * @return
     */
    public static String getNBUsername() {
        String user = getNBPref().get(NB_BUGZILLA_USERNAME, ""); // NOI18N
        return user.equals("") ? null : user;
    }

    /**
     * Returns a stored netbeans.org password or null otherwise
     * @return
     */
    public static char[] getNBPassword() {
        return Keyring.read(NB_BUGZILLA_PASSWORD);
    }

    /**
     * Returns a stored netbeans.org password or null otherwise
     * @return
     */
    public static String getNBPasswordString() {
        char[] psswd = getNBPassword();
        return psswd == null ? null : new String(psswd);
    }

    /**
     * Stores a netbeans.org username
     */
    public static void setNBUsername(String user) {
        getNBPref().put(NB_BUGZILLA_USERNAME, user); // NOI18N
    }

    /**
     * Stores a netbeans.org password
     */
    public static void setNBPassword(char[] psswd) {
        if(psswd == null) {
            Keyring.delete(NB_BUGZILLA_PASSWORD);
        } else {
            Keyring.save(
                NB_BUGZILLA_PASSWORD,
                psswd,
                NbBundle.getMessage(
                    NBRepositorySupport.class,
                    "NBRepositorySupport.password_keyring_description",         // NOI18N
                    NB_BUGZILLA_HOST));
        }
    }

    public static String setNBPassword() {
        char[] psswd = Keyring.read(NB_BUGZILLA_PASSWORD);
        return psswd == null ? null : new String(psswd);
    }

    private static BugzillaRepository createRepositoryIntern() {
        return new BugzillaRepository("NetbeansRepository" + System.currentTimeMillis(),       // NOI18N
                      NbBundle.getMessage(NBRepositorySupport.class, "LBL_NBRepository"),      // NOI18N
                      NB_BUGZILLA_URL,
                      getNBUsername(), getNBPasswordString(),
                      null, null); // NOI18N
    }

    private static Preferences getNBPref() {
        if (nbUserPrefs == null) {
            nbUserPrefs = org.openide.util.NbPreferences.root().node(NBBUGZILLA_PREFERENCES);
        }
        return nbUserPrefs;
    }
}
