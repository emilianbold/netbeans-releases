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
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class NBRepository extends BugzillaRepository {

    public static final String NB_BUGZILLA_URL = "https://netbeans.org/bugzilla";  // NOI18N

    private static BugzillaRepository instance;

    private NBRepository() {
        super("NetbeansRepository" + System.currentTimeMillis(), NbBundle.getMessage(NBRepository.class, "LBL_NBRepository"), NB_BUGZILLA_URL, null, null, null, null); // NOI18N
        // XXX get credentials from kenai or ex reporter
    }

    public static BugzillaRepository findInstance() {

        BugzillaRepository[] repos = Bugzilla.getInstance().getRepositories();
        for (BugzillaRepository repo : repos) {
            if(repo.getUrl().startsWith(NBRepository.NB_BUGZILLA_URL)) {
                return repo;
            }
        }

        Collection<Kenai> kenais = KenaiManager.getDefault().getKenais();
        for (Kenai kenai : kenais) {
            URL url = kenai.getUrl();
            if(NB_BUGZILLA_URL.startsWith(url.getProtocol() + "://" + url.getHost())) { // NOI18N
                // there is a nb kenai registered in the ide
                // create a new repo but _do not register_ in services
                instance = new NBRepository(); // XXX for now we keep a repository for each
                                               //     nb kenai project. there will be no need
                                               //     to create a new instance as soon as we will
                                               //     have only one repository instance for all
                                               //     kenai projects. see also issue #177578
            }
        }

        if(instance == null) {                              // no nb repo yet ...
            instance = new NBRepository();                  // ... create ...
            Bugzilla.getInstance().addRepository(instance); // ... and register in services/bugtracking
        }

        return instance;
    }

}
