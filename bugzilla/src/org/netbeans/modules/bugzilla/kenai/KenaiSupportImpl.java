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

package org.netbeans.modules.bugzilla.kenai;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiSupportImpl extends KenaiSupport {

    private final Set<KenaiRepository> repositories = new HashSet<KenaiRepository>();

    public KenaiSupportImpl() {
    }

    @Override
    public Repository createRepository(KenaiProject project) {
        if(project == null || project.getType() != BugtrackingType.BUGZILLA) {
            return null;
        }

        KenaiRepository repo = createKenaiRepository(project, project.getDisplayName(), project.getFeatureLocation());
        if(repo == null) {
            return null;
        }
        synchronized (repositories) {
            repositories.add(repo);
        }

        KenaiConfiguration kc = (KenaiConfiguration) repo.getConfiguration(); // force repo configuration init before controler populate
        if(kc.getRepositoryConfiguration(repo, false) == null) {
            // something went wrong, can't use the repo anyway => return null
            Bugzilla.LOG.fine("KenaiRepository.getRepositoryConfiguration() returned null for KenaiProject ["   // NOI18N
                    + project.getDisplayName() + "," + project.getName() + "]");                                // NOI18N
            return null;
        }
        return repo;
         
    }

    @Override
    public void setFilter(Query query, Filter filter) {
        if(query instanceof BugzillaQuery) { // XXX assert instead of if
            BugzillaQuery bq = (BugzillaQuery) query;
            bq.setFilter(filter);
        }
    }

    private KenaiRepository createKenaiRepository(KenaiProject kenaiProject, String displayName, String location) {
        final URL loc;
        try {
            loc = new URL(location);
        } catch (MalformedURLException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            return null;
        }

        String host = loc.getHost();
        int idx = location.indexOf(IBugzillaConstants.URL_BUGLIST);
        if (idx <= 0) {
            Bugzilla.LOG.warning("can't get issue tracker url from [" + displayName + ", " + location + "]"); // NOI18N
            return null;
        }
        String url = location.substring(0, idx);
        if (url.startsWith("http:")) { // XXX hack???                   // NOI18N
            url = "https" + url.substring(4);                           // NOI18N
        }
        String productParamUrl = null;
        String productAttribute = "product=";                           // NOI18N
        String product = null;
        int idxProductStart = location.indexOf(productAttribute);
        if (idxProductStart <= 0) {
            Bugzilla.LOG.warning("can't get issue tracker product from [" + displayName + ", " + location + "]"); // NOI18N
            return null;
        } else {
            int idxProductEnd = location.indexOf("&", idxProductStart); // NOI18N
            if(idxProductEnd > -1) {
                productParamUrl = location.substring(idxProductStart, idxProductEnd);
                product = location.substring(idxProductStart + productAttribute.length(), idxProductEnd);
            } else {
                productParamUrl = location.substring(idxProductStart);
                product = location.substring(idxProductStart + productAttribute.length());
            }
        }

        return new KenaiRepository(kenaiProject, displayName, url, host, productParamUrl, product);
    }

    @Override
    public Query getAllIssuesQuery(Repository repository) {
        assert repository instanceof KenaiRepository;
        return ((KenaiRepository)repository).getAllIssuesQuery();
    }

    @Override
    public Query getMyIssuesQuery(Repository repository) {
        assert repository instanceof KenaiRepository;
        return ((KenaiRepository)repository).getMyIssuesQuery();
    }

    @Override
    public BugtrackingType getType() {
        return BugtrackingType.BUGZILLA;
    }

    @Override
    public boolean needsLogin(Query query) {
        return query == ((KenaiRepository) query.getRepository()).getMyIssuesQuery();
    }

    @Override
    public void refresh(Query query, boolean synchronously) {
        assert query instanceof BugzillaQuery;
        BugzillaQuery bq = (BugzillaQuery) query;
        if(synchronously) {
            bq.refresh();
        } else {
            bq.getController().onRefresh();
        }
    }

}

