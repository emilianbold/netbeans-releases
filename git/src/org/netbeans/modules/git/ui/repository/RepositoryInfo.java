/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.git.ui.repository;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public class RepositoryInfo {

    /**
     * fired when the active branch for the repository changed. Old and new values are instances of {@link GitBranch}.
     */
    public static final String PROPERTY_ACTIVE_BRANCH = "prop.activeBranch"; //NOI18N
    /**
     * fired when the HEAD changes, old and new values are instances of {@link GitBranch}.
     */
    public static final String PROPERTY_HEAD = "prop.head"; //NOI18N
    /**
     * fired when repository state changes, old and new values are instances of {@link GitRepositoryState}.
     */
    public static final String PROPERTY_STATE = "prop.state"; //NOI18N
    /**
     * fired when a set of known branches changes (a branch is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitBranch&gt;.
     */
    public static final String PROPERTY_BRANCHES = "prop.branches"; //NOI18N
    /**
     * fired when a set of known tags changes (a tag is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitTag&gt;.
     */
    public static final String PROPERTY_TAGS = "prop.tags"; //NOI18N
    /**
     * fired when a set of known remotes changes (a remote is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitRemoteConfig&gt;.
     */
    public static final String PROPERTY_REMOTES = "prop.remotes"; //NOI18N

    private final Reference<File> rootRef;
    private static final WeakHashMap<File, RepositoryInfo> cache = new WeakHashMap<File, RepositoryInfo>(5);
    private static final Logger LOG = Logger.getLogger(RepositoryInfo.class.getName());
    private static final RequestProcessor rp = new RequestProcessor("RepositoryInfo", 1, true); //NOI18N
    private static final RequestProcessor.Task refreshTask = rp.create(new RepositoryRefreshTask());
    private static final Set<RepositoryInfo> repositoriesToRefresh = new HashSet<RepositoryInfo>(2);
    private final PropertyChangeSupport propertyChangeSupport;
    private final Map<String, GitBranch> branches;
    private final Map<String, GitTag> tags;
    private final Map<String, GitRemoteConfig> remotes;

    private GitBranch activeBranch;
    private GitRepositoryState repositoryState;
    private final String name;
    private static final Set<String> logged = Collections.synchronizedSet(new HashSet<String>());
    
    private RepositoryInfo (File root) {
        this.rootRef = new WeakReference<File>(root);
        this.name = root.getName();
        this.branches = new LinkedHashMap<String, GitBranch>();
        this.tags = new HashMap<String, GitTag>();
        this.remotes = new HashMap<String, GitRemoteConfig>();
        this.activeBranch = GitBranch.NO_BRANCH_INSTANCE;
        this.repositoryState = GitRepositoryState.SAFE;
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Do NOT call from EDT
     * @param repositoryRoot existing repository root
     * @return null if repositoryRoot is not an existing git repository
     */
    public static RepositoryInfo getInstance (File repositoryRoot) {
        RepositoryInfo info = null;
        // this should return alwaus the same instance, so the cache can be implemented as a weak map.
        File repositoryRootSingleInstance = Git.getInstance().getRepositoryRoot(repositoryRoot);
        if (repositoryRoot.equals(repositoryRootSingleInstance)) {
            boolean refresh = false;
            synchronized (cache) {
                info = cache.get(repositoryRootSingleInstance);
                if (info == null) {
                    cache.put(repositoryRootSingleInstance, info = new RepositoryInfo(repositoryRootSingleInstance));
                    refresh = true;
                }
            }
            if (refresh) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    LOG.log(Level.FINE, "getInstance (): had to schedule an async refresh for {0}", repositoryRoot); //NOI18N
                    refreshAsync(repositoryRoot);
                } else {
                    info.refresh();
                }
            }
        }
        return info;
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public void refresh () {
        assert !java.awt.EventQueue.isDispatchThread();
        File root = rootRef.get();
        GitClient client = null;
        try {
            if (root == null) {
                LOG.log(Level.WARNING, "refresh (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refresh (): starting for {0}", root); //NOI18N
                client = Git.getInstance().getClient(root);
                // get all needed information at once before firing events. Thus we supress repeated annotations' refreshing
                Map<String, GitBranch> newBranches = client.getBranches(true, GitUtils.NULL_PROGRESS_MONITOR);
                setBranches(newBranches);
                Map<String, GitTag> newTags = client.getTags(GitUtils.NULL_PROGRESS_MONITOR, false);
                setTags(newTags);
                try {
                    refreshRemotes(client);
                } catch (GitException ex) {
                    LOG.log(logged.add(root.getAbsolutePath() + ex.getMessage()) ? Level.INFO : Level.FINE, null, ex);
                }
                GitRepositoryState newState = client.getRepositoryState(GitUtils.NULL_PROGRESS_MONITOR);
                // now set new values and fire events when needed
                setActiveBranch(newBranches);
                setRepositoryState(newState);
            }
        } catch (GitException ex) {
            Level level = root.exists() ? Level.INFO : Level.FINE; // do not polute the message log with messages concerning temporary or deleted repositories
            LOG.log(level, null, ex);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public void refreshRemotes () throws GitException {
        assert !java.awt.EventQueue.isDispatchThread();
        GitClient client = null;
        try {
            File root = rootRef.get();
            if (root == null) {
                LOG.log(Level.WARNING, "refreshRemotes (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refreshRemotes (): starting for {0}", root); //NOI18N
                client = Git.getInstance().getClient(root);
                refreshRemotes(client);
            }
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }
    
    private void setActiveBranch (Map<String, GitBranch> branches) throws GitException {
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            if (e.getValue().isActive()) {
                GitBranch oldActiveBranch = activeBranch;
                activeBranch = e.getValue();
                if (oldActiveBranch == null || !oldActiveBranch.getName().equals(activeBranch.getName())) {
                    LOG.log(Level.FINE, "active branch changed: {0} --- {1}", new Object[] { rootRef, activeBranch.getName() }); //NOI18N
                    propertyChangeSupport.firePropertyChange(PROPERTY_ACTIVE_BRANCH, oldActiveBranch, activeBranch);
                }
                if (oldActiveBranch == null || !oldActiveBranch.getId().equals(activeBranch.getId())) {
                    LOG.log(Level.FINE, "current HEAD changed: {0} --- {1}", new Object[] { rootRef, activeBranch.getId() }); //NOI18N
                    propertyChangeSupport.firePropertyChange(PROPERTY_HEAD, oldActiveBranch, activeBranch);
                }
            }
        }
    }

    private void setRepositoryState (GitRepositoryState repositoryState) {
        GitRepositoryState oldState = this.repositoryState;
        this.repositoryState = repositoryState;
        if (!repositoryState.equals(oldState)) {
            LOG.log(Level.FINE, "repository state changed: {0} --- {1}", new Object[] { oldState, repositoryState }); //NOI18N
            propertyChangeSupport.firePropertyChange(PROPERTY_STATE, oldState, repositoryState);
        }
    }

    private void setBranches (Map<String, GitBranch> newBranches) {
        Map<String, GitBranch> oldBranches;
        boolean changed = false;
        synchronized (branches) {
            oldBranches = new LinkedHashMap<String, GitBranch>(branches);
            branches.clear();
            branches.putAll(newBranches);
            changed = !equalsBranches(oldBranches, newBranches);
        }
        if (changed) {
            propertyChangeSupport.firePropertyChange(PROPERTY_BRANCHES, Collections.unmodifiableMap(oldBranches), Collections.unmodifiableMap(new HashMap<String, GitBranch>(newBranches)));
        }
    }

    private void setTags (Map<String, GitTag> newTags) {
        Map<String, GitTag> oldTags;
        boolean changed = false;
        synchronized (tags) {
            oldTags = new HashMap<String, GitTag>(tags);
            if (!equalsTags(oldTags, newTags)) {
                tags.clear();
                tags.putAll(newTags);
                changed = true;
            }
        }
        if (changed) {
            propertyChangeSupport.firePropertyChange(PROPERTY_TAGS, Collections.unmodifiableMap(oldTags), Collections.unmodifiableMap(new HashMap<String, GitTag>(newTags)));
        }
    }

    private void setRemotes (Map<String, GitRemoteConfig> newRemotes) {
        Map<String, GitRemoteConfig> oldRemotes;
        boolean changed = false;
        synchronized (remotes) {
            oldRemotes = new HashMap<String, GitRemoteConfig>(remotes);
            if (!equalsRemotes(oldRemotes, newRemotes)) {
                remotes.clear();
                remotes.putAll(newRemotes);
                changed = true;
            }
        }
        if (changed) {
            propertyChangeSupport.firePropertyChange(PROPERTY_REMOTES, Collections.unmodifiableMap(oldRemotes), Collections.unmodifiableMap(new HashMap<String, GitRemoteConfig>(newRemotes)));
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * May be <code>null</code> if repository could not be initialized
     * @return 
     */
    public GitBranch getActiveBranch () {
        return activeBranch;
    }

    public GitRepositoryState getRepositoryState () {
        return repositoryState;
    }

    public String getName () {
        return name;
    }
    
    public Map<String, GitBranch> getBranches () {
        synchronized (branches) {
            return new LinkedHashMap<String, GitBranch>(branches);
        }
    }

    public Map<String, GitTag> getTags () {
        synchronized (tags) {
            return new HashMap<String, GitTag>(tags);
        }
    }

    public Map<String, GitRemoteConfig> getRemotes () {
        synchronized (remotes) {
            return new HashMap<String, GitRemoteConfig>(remotes);
        }
    }

    public static void refreshAsync (File repositoryRoot) {
        RepositoryInfo info = null;
        synchronized (cache) {
            info = cache.get(repositoryRoot);
        }
        if (info != null) {
            boolean start = false;
            synchronized (repositoriesToRefresh) {
                start = repositoriesToRefresh.add(info);
            }
            if (start) {
                LOG.log(Level.FINE, "Planning refresh for {0}", repositoryRoot); //NOI18N
                refreshTask.schedule(3000);
            }
        }
    }

    private static boolean equalsRemotes (Map<String, GitRemoteConfig> oldRemotes, Map<String, GitRemoteConfig> newRemotes) {
        boolean retval = oldRemotes.size() == newRemotes.size() && oldRemotes.keySet().equals(newRemotes.keySet());
        if (retval) {
            for (Map.Entry<String, GitRemoteConfig> e : oldRemotes.entrySet()) {
                GitRemoteConfig oldRemote = e.getValue();
                GitRemoteConfig newRemote = newRemotes.get(e.getKey());
                if (!(oldRemote.getFetchRefSpecs().equals(newRemote.getFetchRefSpecs()) && oldRemote.getPushRefSpecs().equals(newRemote.getPushRefSpecs()) && 
                        oldRemote.getUris().equals(newRemote.getUris()) && oldRemote.getPushUris().equals(newRemote.getPushUris()))) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }
    
    private static boolean equalsBranches (Map<String, GitBranch> oldBranches, Map<String, GitBranch> newBranches) {
        boolean retval = oldBranches.size() == newBranches.size() && oldBranches.keySet().equals(newBranches.keySet());
        if (retval) {
            for (Map.Entry<String, GitBranch> e : oldBranches.entrySet()) {
                GitBranch oldBranch = e.getValue();
                GitBranch newBranch = newBranches.get(e.getKey());
                if (!oldBranch.getId().equals(newBranch.getId())) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }

    private static boolean equalsTags (Map<String, GitTag> oldTags, Map<String, GitTag> newTags) {
        boolean retval = oldTags.size() == newTags.size() && oldTags.keySet().equals(newTags.keySet());
        if (retval) {
            for (Map.Entry<String, GitTag> e : oldTags.entrySet()) {
                GitTag oldTag = e.getValue();
                GitTag newTag = newTags.get(e.getKey());
                if (!(oldTag.getMessage().equals(newTag.getMessage())
                        && oldTag.getTagId().equals(newTag.getTagId())
                        && oldTag.getTagName().equals(newTag.getTagName())
                        && oldTag.getTaggedObjectId().equals(newTag.getTaggedObjectId())
                        && oldTag.getTaggedObjectType().equals(newTag.getTaggedObjectType())
                        && oldTag.getTagger().toString().equals(newTag.getTagger().toString()))) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }

    private void refreshRemotes (GitClient client) throws GitException {
        Map<String, GitRemoteConfig> newRemotes = client.getRemotes(GitUtils.NULL_PROGRESS_MONITOR);
        setRemotes(newRemotes);
    }

    private static class RepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            RepositoryInfo info;
            while ((info = getNextRepositoryInfo()) != null) {
                info.refresh();
            }
        }

        private RepositoryInfo getNextRepositoryInfo () {
            RepositoryInfo info = null;
            synchronized (repositoriesToRefresh) {
                Iterator<RepositoryInfo> it = repositoriesToRefresh.iterator();
                if (it.hasNext()) {
                    info = it.next();
                    it.remove();
                }
            }
            return info;
        }
    }
}
