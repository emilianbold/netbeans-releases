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

package org.netbeans.modules.jira.kenai;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiProject;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.query.JiraQuery;
import org.openide.util.Exceptions;

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
        if(project == null || project.getType() != BugtrackingType.JIRA) {
            return null;
        }

        String location = project.getFeatureLocation().toString();
        final URL loc;
        try {
            loc = new URL(project.getWebLocation().toString());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        String host = loc.getHost();
        int idx = location.indexOf("/browse/");
        if (idx <= 0) {
            Jira.LOG.warning("can't get issue tracker url from [" + project.getName() + ", " + location + "]"); // NOI18N
            return null;
        }
        String url = location.substring(0, idx);
        if (url.startsWith("http:")) { // XXX hack???                   // NOI18N
            url = "https" + url.substring(4);                           // NOI18N
        }

        String product = location.substring(idx + "/browse/".length()); // NOI18N

        KenaiRepository repo = new KenaiRepository(project, project.getDisplayName(), url, host, product);
        if(repo.getConfiguration() == null) {
            // something went wrong, can't use the repo anyway => return null
            Jira.LOG.fine("KenaiRepository.getRepositoryConfiguration() returned null for KenaiProject ["   // NOI18N
                    + project.getDisplayName() + "," + project.getName() + "]");                            // NOI18N
            return null;
        }
        synchronized (repositories) {
            repositories.add(repo);
        }
        return repo;
        
    }

    @Override
    public void setFilter(Query query, Filter filter) {
        if(query instanceof JiraQuery) {
            ((JiraQuery)query).setFilter(filter);
        }
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
        return BugtrackingType.JIRA;
    }

    @Override
    public boolean needsLogin(Query query) {
        return query == ((KenaiRepository) query.getRepository()).getMyIssuesQuery();
    }
}

