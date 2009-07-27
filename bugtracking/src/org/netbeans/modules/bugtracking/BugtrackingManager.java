/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bugtracking;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.bugtracking.BugtrackingRuntime;
import org.netbeans.modules.bugtracking.kenai.KenaiRepositories;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 * Top level class that manages issues from all repositories.  
 * 
 * @author Maros Sandor
 */
public final class BugtrackingManager implements LookupListener {
    
    private static final BugtrackingManager instance = new BugtrackingManager();

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    public final static String EVENT_REPOSITORIES_CHANGED = "bugtracking.repositories.changed"; // NOI18N

    private boolean                 initialized;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugracking.BugtrackingManager"); // NOI18N

    private RequestProcessor rp = new RequestProcessor("Bugtracking manager", 1); // NOI18N

    /**
     * Holds all registered connectors.
     */
    private final Collection<BugtrackingConnector> connectors = new ArrayList<BugtrackingConnector>(2);

    /**
     * Result of Lookup.getDefault().lookup(new Lookup.Template<RepositoryConnector>(RepositoryConnector.class));
     */
    private final Lookup.Result<BugtrackingConnector> connectorsLookup;

    public static BugtrackingManager getInstance() {
        instance.init();
        return instance;
    }

    private BugtrackingManager() {
        connectorsLookup = Lookup.getDefault().lookup(new Lookup.Template<BugtrackingConnector>(BugtrackingConnector.class));
    }

    /**
     *
     * Returns all known repositories incl. the Kenai ones
     *
     * @return repositories
     */
    public Repository[] getKnownRepositories() {
        Repository[] kenaiRepos = KenaiRepositories.getInstance().getRepositories();
        Repository[] otherRepos = getRepositories();
        Repository[] ret = new Repository[kenaiRepos.length + otherRepos.length];
        System.arraycopy(kenaiRepos, 0, ret, 0, kenaiRepos.length);
        System.arraycopy(otherRepos, 0, ret, kenaiRepos.length, otherRepos.length);
        return ret;
    }

    /**
     * Returns all user defined repositories
     * @return
     */
    public Repository[] getRepositories() {
        List<Repository> repos = new ArrayList<Repository>(10);
        BugtrackingConnector[] conns = getConnectors();
        for (BugtrackingConnector bc : conns) {
            Repository[] rs = bc.getRepositories();
            if(rs != null) {
                for (Repository r : rs) {
                    repos.add(r);
                }
            }
        }
        return repos.toArray(new Repository[repos.size()]);
    }

    public RequestProcessor getRequestProcessor() {
        return rp;
    }

    private synchronized void init() {
        if (initialized) return;

        connectorsLookup.addLookupListener(this);
        refreshConnectors();

        BugtrackingRuntime.getInstance(); // force init
        LOG.fine("Bugtracking manager initialized"); // NOI18N
        initialized = true;
    }

    public BugtrackingConnector[] getConnectors() {
        synchronized(connectors) {
            return connectors.toArray(new BugtrackingConnector[connectors.size()]);
        }
    }

    public void resultChanged(LookupEvent ev) {
        refreshConnectors();
    }

    private void refreshConnectors() {
        Collection<? extends BugtrackingConnector> conns = connectorsLookup.allInstances();
        if(LOG.isLoggable(Level.FINER)) {
            for (BugtrackingConnector repository : conns) {
                LOG.finer("registered provider: " + repository.getDisplayName()); // NOI18N
            }
        }
        synchronized (connectors) {
            connectors.clear();
            connectors.addAll(conns);
        }
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void fireRepositoriesChanged() {
        // XXX should be connectors responsibility
        changeSupport.firePropertyChange(EVENT_REPOSITORIES_CHANGED, null, null);
    }
}
