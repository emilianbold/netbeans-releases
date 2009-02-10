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

import org.eclipse.mylyn.commons.net.WebUtil;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.mylyn.internal.tasks.core.RepositoryExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager;
import org.eclipse.mylyn.internal.tasks.core.externalization.IExternalizationParticipant;
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
    
    private final String            DATA_DIRECTORY = "issues";
    
    private boolean                 initialized;
    private File                    cacheStore;
    private ExternalizationManager  externalizationManager;
    private TaskRepositoryManager   repositoryManager;
    private Set<Repository>         repos;

    public static Logger LOG = Logger.getLogger("org.netbeans.modules.issues.IssueManager");

    /**
     * Holds all registered connectors.
     */
    private final Collection<BugtrackingConnector> connectors = new ArrayList<BugtrackingConnector>(2);

    /**
     * Default bugtracking request processor
     */
    private RequestProcessor rp;

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
     * Returns all known repositories
     *
     * @return repositories
     */
    public Repository[] getRepositories() {
        if(repos == null) {
            initRepos();
        }
        return repos.toArray(new Repository[repos.size()]);
    }

    public RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Baqtracking tasks", 10); // XXX throughput???
        }
        return rp;
    }

    private synchronized void init() {
        if (initialized) return;

        connectorsLookup.addLookupListener(this);
        refreshConnectors();

        WebUtil.init();
        
        initCacheStore();
        externalizationManager = new ExternalizationManager(cacheStore.getAbsolutePath());

        repositoryManager = new TaskRepositoryManager();
        IExternalizationParticipant repositoryParticipant = new RepositoryExternalizationParticipant(externalizationManager, repositoryManager);
        externalizationManager.addParticipant(repositoryParticipant);

//        taskList = new TaskList();
//        repositoryModel = new RepositoryModel(taskList, repositoryManager);
//        taskListExternalizer = new TaskListExternalizer(repositoryModel, repositoryManager);
////        TaskListElementImporter taskListImporter = new TaskListElementImporter(repositoryManager, repositoryModel);
//
//        taskListSaveParticipant = new TaskListExternalizationParticipant(repositoryModel, taskList,
//                taskListExternalizer, externalizationManager, repositoryManager);
//        externalizationManager.addParticipant(taskListSaveParticipant);
//        taskList.addChangeListener(taskListSaveParticipant);
//
//        taskActivityManager = new TaskActivityManager(repositoryManager, taskList);
//        taskActivityManager.addActivationListener(taskListSaveParticipant);
     
//        taskListManager = new TaskListManager(taskList, taskListSaveParticipant, taskListImporter);

        

        LOG.fine("Issue manager initialized");
        initialized = true;
    }

    public void addRepo(Repository repo) {
        repos.add(repo);
    }

    private void initCacheStore() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        if (userDir != null) {
            cacheStore = new File(new File(new File (userDir, "var"), "cache"), DATA_DIRECTORY); // NOI18N
        } else {
            File cachedir = FileUtil.toFile(org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot());
            cacheStore = new File(cachedir, DATA_DIRECTORY);
        }
        cacheStore.mkdirs();
    }

    public BugtrackingConnector[] getConnectors() {
        synchronized(connectors) {
            return connectors.toArray(new BugtrackingConnector[connectors.size()]);
        }
    }

    public void resultChanged(LookupEvent ev) {
        refreshConnectors();
    }

    /**
     * Read stored non-kenai repositories and retrieve known kenai ones (XXX)
     */
    private void initRepos() {
        repos = new HashSet<Repository>(10); 
        BugtrackingConnector[] conns = getConnectors();
        for (BugtrackingConnector bc : conns) {
            Repository[] rs = bc.getSavedRepositories();
            if(rs != null) {
                for (Repository r : rs) {
                    repos.add(r);
                }
            }
        }
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

}
