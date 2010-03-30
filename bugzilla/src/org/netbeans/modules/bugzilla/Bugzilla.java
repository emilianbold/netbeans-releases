/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla;

import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClientManager;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport;
import org.netbeans.modules.bugzilla.issue.BugzillaIssueProvider;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.kenai.KenaiSupportImpl;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class Bugzilla {

    private BugzillaRepositoryConnector brc;
    private Set<BugzillaRepository> repositories;

    private static final Object REPOSITORIES_LOCK = new Object();
    private static Bugzilla instance;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugzilla.Bugzilla"); // NOI18N

    private RequestProcessor rp;
    private BugzillaCorePlugin bcp;
    private BugzillaClientManager clientManager;

    private KenaiSupport kenaiSupport;
    private BugzillaConnector connector;

    private Bugzilla() {
        ModuleLifecycleManager.instantiated = true;
        bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        // up to mylyn 3.3.1 it is esential not to create the BugzillaRepositoryConnector
        // before the BugzillaCorePlugin was started. Otherwise they won't be configured together
        // in the BugzillaRepositoryConnector-s constructor
        brc = new BugzillaRepositoryConnector();
        clientManager = getRepositoryConnector().getClientManager();

        // lazy ping tasklist issue provider to load issues ...
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                BugzillaIssueProvider.getInstance();
            }
        });
    }

    public static synchronized Bugzilla getInstance() {
        if(instance == null) {
            instance = new Bugzilla();
        }
        return instance;
    }

    void shutdown() {
        try {
            bcp.stop(null); // forces persisting of repository configuration
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public KenaiSupport getKenaiSupport() {
        if(kenaiSupport == null) {
            kenaiSupport = new KenaiSupportImpl();
        }
        return kenaiSupport;
    }
    
    public BugzillaRepositoryConnector getRepositoryConnector() {
        return brc;
    }

    public RepositoryConfiguration getRepositoryConfiguration(BugzillaRepository repository, boolean forceRefresh) throws CoreException, MalformedURLException {
        getClient(repository); // XXX mylyn 3.1.1 workaround. initialize the client, otherwise the configuration will be downloaded twice
        RepositoryConfiguration rc = brc.getRepositoryConfiguration(repository.getTaskRepository(), forceRefresh, new NullProgressMonitor());
        return rc;
    }

    /**
     * Returns a BugzillaClient for the given repository
     * @param repository
     * @return
     * @throws java.net.MalformedURLException
     * @throws org.eclipse.core.runtime.CoreException
     */
    public BugzillaClient getClient(BugzillaRepository repository) throws MalformedURLException, CoreException {
        return clientManager.getClient(repository.getTaskRepository(), new NullProgressMonitor());
    }

    /**
     * Returns the request processor for common tasks in bugzilla.
     * Do not use this when accesing a remote repository.
     * 
     * @return
     */
    public final RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Bugzilla", 1, true); // NOI18N
        }
        return rp;
    }

    public void addRepository(BugzillaRepository repository) {
        assert repository != null;
        if(repository instanceof KenaiRepository) {
            // we don't store kenai repositories - XXX  shouldn't be even called
            return;
        }
        synchronized(REPOSITORIES_LOCK) {
            getStoredRepositories().add(repository);
            BugzillaConfig.getInstance().putRepository(repository.getID(), repository);
        }
        getConnector().fireRepositoriesChanged();
    }

    public BugzillaConnector getConnector() {
        if (connector == null) {
            connector = Lookup.getDefault().lookup(BugzillaConnector.class);
        }
        return connector;
    }

    public void removeRepository(BugzillaRepository repository) {
        synchronized(REPOSITORIES_LOCK) {
            getStoredRepositories().remove(repository);
            BugzillaConfig.getInstance().removeRepository(repository.getID());
        }
        getConnector().fireRepositoriesChanged();
    }

    public BugzillaRepository[] getRepositories() {
        synchronized(REPOSITORIES_LOCK) {
            Set<BugzillaRepository> s = getStoredRepositories();
            return s.toArray(new BugzillaRepository[s.size()]);
        }
    }

    private Set<BugzillaRepository> getStoredRepositories() {
        if (repositories == null) {
            repositories = new HashSet<BugzillaRepository>();
            String[] names = BugzillaConfig.getInstance().getRepositories();
            if (names == null || names.length == 0) {
                return repositories;
            }
            for (String name : names) {
                BugzillaRepository repo = BugzillaConfig.getInstance().getRepository(name);
                if (repo != null) {
                    repositories.add(repo);
                }
            }
        }
        return repositories;
    }

}
