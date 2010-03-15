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

import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.api.NBBugzillaUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class NBRepositorySupport extends BugzillaRepository {    

    private static final String NB_BUGZILLA_HOST = "netbeans.org";           // NOI18N
    public static final String NB_BUGZILLA_URL = "https://" + NB_BUGZILLA_HOST + "/bugzilla";           // NOI18N
    public static final String URL_NB_ORG_SIGNUP = "https://netbeans.org/people/signup";

    private static BugzillaRepository nbRepository;
    private static boolean isKenai;

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
        BugzillaRepository[] repos;
        if(nbRepository != null) {
            // check if repository wasn't removed since the last time it was used
            if(!isKenai) {
                repos = Bugzilla.getInstance().getRepositories();
                boolean registered = false;
                for (BugzillaRepository repo : repos) {
                    if(BugtrackingUtil.isNbRepository(repo)) {
                        registered = true;
                        break;
                    }
                }
                if(!registered) {
                    Bugzilla.getInstance().addRepository(nbRepository);
                }
            }
            return nbRepository;
        }
        repos = Bugzilla.getInstance().getRepositories();
        for (BugzillaRepository repo : repos) {
            if(BugtrackingUtil.isNbRepository(repo)) {
                return repo;
            }
        }

        if(KenaiUtil.isNetbeansKenaiRegistered()) {
                isKenai = true;
            // there is a nb kenai registered in the ide
            // create a new repo but _do not register_ in services
            nbRepository = createRepositoryIntern(); // XXX for now we keep a repository for each
                                                     //     nb kenai project. there will be no need
                                                     //     to create a new instance as soon as we will
                                                     //     have only one repository instance for all
                                                     //     kenai projects. see also issue #177578
        }

        if(nbRepository == null) {                              // no nb repo yet ...
            nbRepository = createRepositoryIntern();            // ... create ...
            Bugzilla.getInstance().addRepository(nbRepository); // ... and register in services/issue tracking
        } 

        return nbRepository;
    }

    private static BugzillaRepository createRepositoryIntern() {
        char[] password = NBBugzillaUtils.getNBPassword();
        final String username = NBBugzillaUtils.getNBUsername();
        return new BugzillaRepository("NetbeansRepository" + System.currentTimeMillis(),       // NOI18N
                      NbBundle.getMessage(NBRepositorySupport.class, "LBL_NBRepository"),      // NOI18N
                      NB_BUGZILLA_URL,
                      username,
                      password != null ? new String(password) : null,
                      null, null); // NOI18N
    }
}
