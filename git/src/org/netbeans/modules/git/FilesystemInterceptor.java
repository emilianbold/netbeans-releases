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

package org.netbeans.modules.git;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * @author ondra
 */
class FilesystemInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final Set<File> filesToRefresh = new HashSet<File>();
    private final Map<File, Set<File>> lockedRepositories = new HashMap<File, Set<File>>(5);

    private RequestProcessor.Task refreshTask, lockedRepositoryRefreshTask;

    private static final RequestProcessor rp = new RequestProcessor("GitRefresh", 1, true);
    private final GitFolderEventsHandler gitFolderEventsHandler;
    private static boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.git.autoRefreshDisabled", "false")); //NOI18N
    private static final String INDEX_FILE_NAME = "index"; //NOI18N
    private static final String HEAD_FILE_NAME = "HEAD"; //NOI18N
    private static final Logger LOG = Logger.getLogger(FilesystemInterceptor.class.getName());

    public FilesystemInterceptor () {
        cache = Git.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
        lockedRepositoryRefreshTask = rp.create(new LockedRepositoryRefreshTask());
        gitFolderEventsHandler = new GitFolderEventsHandler();
    }

    @Override
    public long refreshRecursively (File dir, long lastTimeStamp, List<? super File> children) {
        long retval = -1;
        if (GitUtils.DOT_GIT.equals(dir.getName())) {
            Git.STATUS_LOG.log(Level.FINER, "Interceptor.refreshRecursively: {0}", dir.getAbsolutePath()); //NOI18N
            children.clear();
            retval = gitFolderEventsHandler.refreshAdminFolder(dir);
        }
        return retval;
    }

    @Override
    public boolean beforeCreate (final File file, boolean isDirectory) {
        LOG.log(Level.FINE, "beforeCreate {0} - {1}", new Object[] { file, isDirectory }); //NOI18N
        if (GitUtils.isPartOfGitMetadata(file)) return false;
        if (!isDirectory && !file.exists()) {
            Git git = Git.getInstance();
            final File root = git.getRepositoryRoot(file);
            if (root == null) return false;
            try {
                git.getClient(root).reset(new File[] { file }, "HEAD", true, GitUtils.NULL_PROGRESS_MONITOR);
            } catch (GitException ex) {
                LOG.log(Level.INFO, "beforeCreate(): File: {0} {1}", new Object[] { file.getAbsolutePath(), ex.toString()}); //NOI18N
            }
            LOG.log(Level.FINER, "beforeCreate(): finished: {0}", file); // NOI18N
        }
        return false;
    }

    @Override
    public void afterCreate (final File file) {
        LOG.log(Level.FINE, "afterCreate {0}", file); //NOI18N
        // There is no point in refreshing the cache for ignored files.
        addToCreated(file);
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file));
        }
    }

    @Override
    public boolean beforeDelete (File file) {
        LOG.log(Level.FINE, "beforeDelete {0}", file); //NOI18N
        if (file == null) return false;
        if (GitUtils.isPartOfGitMetadata(file)) return false;

        // do not handle delete for ignored files
        return !cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED);
    }

    @Override
    public void doDelete (File file) throws IOException {
        LOG.log(Level.FINE, "doDelete {0}", file); //NOI18N
        if (file == null) return;
        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(file);
        try {
            if (GitUtils.getGitFolderForRoot(root).exists()) {
                git.getClient(root).remove(new File[] { file }, false, GitUtils.NULL_PROGRESS_MONITOR);
            } else if (file.exists()) {
                Utils.deleteRecursively(file);
                if (file.exists()) {
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_DeleteFailed", new Object[] { file, "" })); //NOI18N
                    throw ex;
                }
            }
            if (file.equals(root)) {
                // the whole repository was deleted -> release references to the repository folder
                refreshMetadataTimestamp(root);
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_DeleteFailed", new Object[] { file, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public void afterDelete(final File file) {
        LOG.log(Level.FINE, "afterDelete {0}", file); //NOI18N
        if (file == null) return;
        // we don't care about ignored files
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file));
        }
    }

    @Override
    public boolean beforeMove(File from, File to) {
        LOG.log(Level.FINE, "beforeMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return true;
        Git hg = Git.getInstance();
        return hg.isManaged(from) && hg.isManaged(to);
    }

    @Override
    public void doMove(final File from, final File to) throws IOException {
        LOG.log(Level.FINE, "doMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists() && !equalPathsIgnoreCase(from, to)) return;
        
        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(from);
        File dstRoot = git.getRepositoryRoot(to);
        try {
            if (root != null && root.equals(dstRoot) && !cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                // target does not lie under ignored folder and is in the same repo as src
                if (equalPathsIgnoreCase(from, to)) {
                    // must do rename --after because the files/paths equal on Win or Mac
                    if (!from.renameTo(to)) {
                        throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                    }
                    git.getClient(root).rename(from, to, true, GitUtils.NULL_PROGRESS_MONITOR);
                } else {
                    git.getClient(root).rename(from, to, false, GitUtils.NULL_PROGRESS_MONITOR);
                }
            } else {
                boolean result = from.renameTo(to);
                if (!result) {
                    throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                }
                if (root != null) {
                    git.getClient(root).remove(new File[] { from }, true, GitUtils.NULL_PROGRESS_MONITOR);
                }
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    private boolean equalPathsIgnoreCase (final File from, final File to) {
        return Utilities.isWindows() && from.equals(to) || Utilities.isMac() && from.getPath().equalsIgnoreCase(to.getPath());
    }

    @Override
    public void afterMove(final File from, final File to) {
        LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || !to.exists()) return;

        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(from).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(from));
        }
        addToCreated(to);
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(to));
        }
    }

    @Override
    public boolean beforeCopy (File from, File to) {
        LOG.log(Level.FINE, "beforeCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return true;
        Git git = Git.getInstance();
        return git.isManaged(from) && git.isManaged(to);
    }

    @Override
    public void doCopy (final File from, final File to) throws IOException {
        LOG.log(Level.FINE, "doCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return;

        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(from);
        File dstRoot = git.getRepositoryRoot(to);

        if (from.isDirectory()) {
            FileUtils.copyDirFiles(from, to);
        } else {
            FileUtils.copyFile(from, to);
        }

        if (root == null || cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // target lies under ignored folder, do not add it
            return;
        }
        try {
            if (root.equals(dstRoot)) {
                git.getClient(root).copyAfter(from, to, GitUtils.NULL_PROGRESS_MONITOR);
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_CopyFailed", new Object[] { from, to, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public void afterCopy (final File from, final File to) {
        LOG.log(Level.FINE, "afterCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (to == null) return;

        addToCreated(to);
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(to));
        }
    }

    @Override
    public void afterChange (final File file) {
        if (file.isDirectory()) return;
        LOG.log(Level.FINE, "afterChange {0}", new Object[] { file }); //NOI18N
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file));
        }
    }

    @Override
    public boolean isMutable(File file) {
        return GitUtils.isPartOfGitMetadata(file) || super.isMutable(file);
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new GitSearchHistorySupport(file);
        } else if("ProvidedExtensions.RemoteLocation".equals(attrName)) { //NOI18N
            File repoRoot = Git.getInstance().getRepositoryRoot(file);
            RepositoryInfo info = RepositoryInfo.getInstance(repoRoot);
            Map<String, GitRemoteConfig> remotes = info.getRemotes();
            StringBuilder sb = new StringBuilder();
            for (GitRemoteConfig rc : remotes.values()) {
                List<String> uris = rc.getUris();
                for (int i = 0; i < uris.size(); i++) {
                    sb.append(uris.get(i)).append(';');
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    /**
     * Checks if administrative folder for a repository with the file is registered.
     * @param file
     */
    void pingRepositoryRootFor(final File file) {
        if (!AUTOMATIC_REFRESH_ENABLED) {
            return;
        }
        gitFolderEventsHandler.initializeFor(file);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    Set<File> getSeenRoots (File repositoryRoot) {
        return gitFolderEventsHandler.getSeenRoots(repositoryRoot);
    }

    /**
     * Refreshes cached modification timestamp of the metadata for the given repository
     * @param repository
     */
    void refreshMetadataTimestamp (final File repository) {
        assert repository != null;
        if (repository == null) {
            return;
        }
        if (EventQueue.isDispatchThread()) {
            Git.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run () {
                    gitFolderEventsHandler.refreshIndexFileTimestamp(repository);
                }
            });
        } else {
            gitFolderEventsHandler.refreshIndexFileTimestamp(repository);
        }
    }

    private final Map<File, Long> createdFolders = new LinkedHashMap<File, Long>() {

        @Override
        public Long put (File key, Long value) {
            long t = System.currentTimeMillis();
            for (Iterator<Map.Entry<File, Long>> it = entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<File, Long> e = it.next();
                if (e.getValue() < t - 600000) { // keep for 10 minutes
                    it.remove();
                }
            }
            return super.put(key, value);
        }
        
    };
    private void addToCreated (File createdFile) {
        if (!GitModuleConfig.getDefault().getAutoIgnoreFiles() || !createdFile.isDirectory()) {
            // no need to keep files and no need to keep anything if auto-ignore-files is disabled
            return;
        }
        synchronized (createdFolders) {
            for (File f : createdFolders.keySet()) {
                if (Utils.isAncestorOrEqual(f, createdFile)) {
                    // just keep created roots, no children
                    return;
                }
            }
            createdFolders.put(createdFile, createdFile.lastModified());
        }
    }

    Collection<File> getCreatedFolders () {
        synchronized (createdFolders) {
            return new HashSet<File>(createdFolders.keySet());
        }
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Git.STATUS_LOG, "GitInterceptor.refreshTask")) { //NOI18N
                return;
            }
            Collection<File> files;
            synchronized (filesToRefresh) {
                files = new HashSet<File>(filesToRefresh);
                filesToRefresh.clear();
            }
            if (!"false".equals(System.getProperty("versioning.git.delayStatusForLockedRepositories"))) {
                files = checkLockedRepositories(files, false);
            }
            if (!files.isEmpty()) {
                cache.refreshAllRoots(files);
            }
            if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    private Collection<File> checkLockedRepositories (Collection<File> additionalFilesToRefresh, boolean keepCached) {
        List<File> retval = new LinkedList<File>();
        // at first sort the files under repositories
        Map<File, Set<File>> sortedFiles = GitUtils.sortByRepository(additionalFilesToRefresh);
        for (Map.Entry<File, Set<File>> e : sortedFiles.entrySet()) {
            Set<File> alreadyPlanned = lockedRepositories.get(e.getKey());
            if (alreadyPlanned == null) {
                alreadyPlanned = new HashSet<File>();
                lockedRepositories.put(e.getKey(), alreadyPlanned);
            }
            alreadyPlanned.addAll(e.getValue());
        }
        // return all files that do not belong to a locked repository 
        for (Iterator<Map.Entry<File, Set<File>>> it = lockedRepositories.entrySet().iterator(); it.hasNext();) {
            Map.Entry<File, Set<File>> entry = it.next();
            File repository = entry.getKey();
            if (!repository.exists()) {
                // repository does not exist, no need to keep it
                it.remove();
            } else if (GitUtils.isRepositoryLocked(repository)) {
                Git.STATUS_LOG.log(Level.FINE, "checkLockedRepositories(): Repository {0} locked, status refresh delayed", repository); //NOI18N
            } else {
                // repo not locked, add all files into the returned collection
                retval.addAll(entry.getValue());
                if (!keepCached) {
                    it.remove();
                }
            }
        }
        return retval;
    }

    private class LockedRepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            if (!checkLockedRepositories(Collections.<File>emptySet(), true).isEmpty()) {
                // there are some newly unlocked repositories to refresh
                refreshTask.schedule(0);
            } else if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    private void reScheduleRefresh (int delayMillis, Set<File> filesToRefresh) {
        // refresh all at once
        Set<File> filteredFiles = new HashSet<File>(filesToRefresh);
        for (Iterator<File> it = filteredFiles.iterator(); it.hasNext(); ) {
            if (GitUtils.isPartOfGitMetadata(it.next())) {
                it.remove();
            }
        }
        boolean changed;
        synchronized (this.filesToRefresh) {
            changed = this.filesToRefresh.addAll(filteredFiles);
        }
        if (changed) {
            Git.STATUS_LOG.log(Level.FINE, "reScheduleRefresh: adding {0}", filteredFiles);
            refreshTask.schedule(delayMillis);
        }
    }

    private static class GitFolderTimestamps {
        private final File indexFile;
        private final long indexFileTS;
        private final File headFile;
        private final long headFileTS;
        private final File refFile;
        private final long refFileTS;
        private final File gitFolder;

        public GitFolderTimestamps (File indexFile, File headFile, File refFile, File gitFolder) {
            this.indexFile = indexFile;
            this.indexFileTS = indexFile.lastModified();
            this.headFile = headFile;
            this.headFileTS = headFile.lastModified();
            this.refFile = refFile;
            this.refFileTS = refFile.lastModified();
            this.gitFolder = gitFolder;
        }
        
        private File getIndexFile () {
            return indexFile;
        }

        private boolean isNewer (GitFolderTimestamps other) {
            boolean newer = true;
            if (other != null) {
                newer = indexFileTS > other.indexFileTS || headFileTS > other.headFileTS;
            }
            return newer;
        }

        private File getGitFolder () {
            return gitFolder;
        }

        private boolean repositoryExists () {
            return indexFileTS > 0 || gitFolder.exists();
        }

        private boolean isOutdated () {
            // first check the index
            boolean upToDate = indexFileTS >= indexFile.lastModified();
            // then check the current head
            if (upToDate) {
                upToDate = headFileTS >= headFile.lastModified();
            }
            // if pointer to branch did not change, there could still be a commit to the same branch - in that case refs/heads/... file changed
            if (upToDate) {
                upToDate = refFileTS >= refFile.lastModified();
            }
            return !upToDate;
        }
    }
    
    private class GitFolderEventsHandler {
        private final HashMap<File, Set<File>> seenRoots = new HashMap<File, Set<File>>();
        private final HashMap<File, GitFolderTimestamps> timestamps = new HashMap<File, GitFolderTimestamps>(5);
        private final HashMap<File, FileChangeListener> gitFolderRLs = new HashMap<File, FileChangeListener>(5);

        private final HashSet<File> filesToInitialize = new HashSet<File>();
        private final RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            @Override
            public void run() {
                initializeFiles();
            }
        });

        private final HashSet<File> refreshedRepositories = new HashSet<File>(5);
        private final RequestProcessor.Task refreshOpenFilesTask = rp.create(new Runnable() {
            @Override
            public void run() {
                HashSet<File> repositories;
                synchronized (refreshedRepositories) {
                    repositories = new HashSet<File>(refreshedRepositories);
                    refreshedRepositories.clear();
                }
                Set<File> openFiles = Utils.getOpenFiles();
                for (Iterator<File> it = openFiles.iterator(); it.hasNext(); ) {
                    File file = it.next();
                    if (!repositories.contains(Git.getInstance().getRepositoryRoot(file))) {
                        it.remove();
                    }
                }
                if (!openFiles.isEmpty()) {
                    Git.getInstance().headChanged(openFiles);
                }
            }
        });
        private final GitRepositories gitRepositories = GitRepositories.getInstance();

        public void initializeFor (File file) {
            if (addFileToInitialize(file)) {
                initializingTask.schedule(500);
            }
        }

        private Set<File> getSeenRoots (File repositoryRoot) {
            Set<File> retval = new HashSet<File>();
            Set<File> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                retval.addAll(seenRootsForRepository);
            }
            return retval;
        }

        private boolean addSeenRoot (File repositoryRoot, File rootToAdd) {
            boolean added = false;
            Set<File> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                if (!seenRootsForRepository.contains(repositoryRoot)) {
                    // try to add the file only when the repository root is not yet registered
                    rootToAdd = FileUtil.normalizeFile(rootToAdd);
                    added = !GitUtils.prepareRootFiles(repositoryRoot, seenRootsForRepository, rootToAdd);
                }
            }
            return added;
        }

        private Set<File> getSeenRootsForRepository (File repositoryRoot) {
            synchronized (seenRoots) {
                 Set<File> seenRootsForRepository = seenRoots.get(repositoryRoot);
                 if (seenRootsForRepository == null) {
                     seenRoots.put(repositoryRoot, seenRootsForRepository = new HashSet<File>());
                 }
                 return seenRootsForRepository;
            }
        }

        private boolean addFileToInitialize(File file) {
            synchronized (filesToInitialize) {
                return filesToInitialize.add(file);
            }
        }

        private File getFileToInitialize () {
            File nextFile = null;
            synchronized (filesToInitialize) {
                Iterator<File> iterator = filesToInitialize.iterator();
                if (iterator.hasNext()) {
                    nextFile = iterator.next();
                    iterator.remove();
                }
            }
            return nextFile;
        }

        private GitFolderTimestamps scanGitFolderTimestamps (File gitFolder) {
            File indexFile = new File(gitFolder, INDEX_FILE_NAME);
            File headFile = new File(gitFolder, HEAD_FILE_NAME);
            GitBranch activeBranch = null;
            RepositoryInfo info = RepositoryInfo.getInstance(gitFolder.getParentFile());
            if (info != null) {
                info.refresh();
                activeBranch = info.getActiveBranch();
            }
            File refFile = headFile;
            if (activeBranch != null && !GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
                refFile = new File(gitFolder, (GitUtils.PREFIX_R_HEADS + activeBranch.getName()).replace("/", File.separator)); //NOI18N
            }
            return new GitFolderTimestamps(indexFile, headFile, refFile, gitFolder);
        }

        public void refreshIndexFileTimestamp (File repository) {
            refreshIndexFileTimestamp(scanGitFolderTimestamps(GitUtils.getGitFolderForRoot(repository)));
        }

        private void refreshIndexFileTimestamp (GitFolderTimestamps newTimestamps) {
            if (Utils.isAncestorOrEqual(new File(System.getProperty("java.io.tmpdir")), newTimestamps.getIndexFile())) { //NOI18N
                // skip repositories in temp folder
                return;
            }
            final File gitFolder = newTimestamps.getGitFolder();
            boolean exists = newTimestamps.repositoryExists();
            synchronized (timestamps) {
                if (exists && !newTimestamps.isNewer(timestamps.get(gitFolder))) {
                    // do not enter the filesystem module unless really need to
                    return;
                }
            }
            boolean add = false;
            boolean remove = false;
            synchronized (timestamps) {
                timestamps.remove(gitFolder);
                FileChangeListener list = gitFolderRLs.remove(gitFolder);
                if (exists) {
                    timestamps.put(gitFolder, newTimestamps);
                    if (list == null) {
                        final FileChangeListener fList = list = new FileChangeAdapter();
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                FileUtil.addRecursiveListener(fList, gitFolder);
                            }
                        });
                    }
                    gitFolderRLs.put(gitFolder, list);
                    add = true;
                } else {
                    if (list != null) {
                        final FileChangeListener fList = list;
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                FileUtil.removeRecursiveListener(fList, gitFolder);
                                // repository was deleted, we should refresh versioned parents
                                Git.getInstance().versionedFilesChanged();
                            }
                        });
                    }
                    Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolderTimestamp: {0} no longer exists", gitFolder.getAbsolutePath()); //NOI18N
                    remove = true;
                }
                if (remove) {
                    gitRepositories.remove(gitFolder.getParentFile());
                } else if (add) {
                    File repository = gitFolder.getParentFile();
                    if (!repository.equals(Git.getInstance().getRepositoryRoot(repository))) {
                        // guess this is needed, versionedFilesChanged might not have been called yet (see InitAction)
                        Git.getInstance().versionedFilesChanged();
                    }
                    gitRepositories.add(repository);
                }
            }
        }

        private void initializeFiles() {
            File file = null;
            while ((file = getFileToInitialize()) != null) {
                Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: {0}", file.getAbsolutePath()); //NOI18N
                // select repository root for the file and finds it's .git folder
                File repositoryRoot = Git.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    if (addSeenRoot(repositoryRoot, file)) {
                        // this means the repository has not yet been scanned, so scan it
                        Git.STATUS_LOG.log(Level.FINE, "initializeFiles: planning a scan for {0} - {1}", new Object[]{repositoryRoot.getAbsolutePath(), file.getAbsolutePath()}); //NOI18N
                        reScheduleRefresh(4000, Collections.singleton(file));
                        File gitFolder = GitUtils.getGitFolderForRoot(repositoryRoot);
                        boolean refreshNeeded = false;
                        synchronized (timestamps) {
                            if (!timestamps.containsKey(gitFolder)) {
                                if (new File(gitFolder, INDEX_FILE_NAME).canRead()) {
                                    timestamps.put(gitFolder, null);
                                    refreshNeeded = true;
                                }
                            }
                        }
                        if (refreshNeeded) {
                            refreshIndexFileTimestamp(scanGitFolderTimestamps(gitFolder));
                        }
                    }
                }
            }
            Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: finished"); //NOI18N
        }

        private long refreshAdminFolder (File gitFolder) {
            long lastModified = 0;
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                gitFolder = FileUtil.normalizeFile(gitFolder);
                Git.STATUS_LOG.log(Level.FINER, "refreshAdminFolder: special FS event handling for {0}", gitFolder.getAbsolutePath()); //NOI18N
                boolean refreshNeeded = false;
                GitFolderTimestamps cached;
                synchronized (timestamps) {
                    cached = timestamps.get(gitFolder);
                }
                if (cached == null || !cached.repositoryExists() || cached.isOutdated()) {
                    refreshIndexFileTimestamp(scanGitFolderTimestamps(gitFolder));
                    refreshNeeded = true;
                }
                if (refreshNeeded) {
                    File repository = gitFolder.getParentFile();
                    RepositoryInfo.refreshAsync(repository);
                    Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolder: planning repository scan for {0}", repository.getAbsolutePath()); //NOI18N
                    reScheduleRefresh(3000, getSeenRoots(repository)); // scan repository root
                    refreshOpenFiles(repository);
                }
            }
            return lastModified;
        }

        private void refreshOpenFiles (File repository) {
            boolean refreshPlanned;
            synchronized (refreshedRepositories) {
                refreshPlanned = !refreshedRepositories.add(repository);
            }
            if (refreshPlanned) {
                refreshOpenFilesTask.schedule(3000);
            }
        }
    }
    
    public class GitSearchHistorySupport extends SearchHistorySupport {
        public GitSearchHistorySupport(File file) {
            super(file);
        }
        @Override
        protected boolean searchHistoryImpl(final int line) throws IOException {
            File file = getFile();
            SearchHistoryAction.openSearch(Git.getInstance().getRepositoryRoot(file), file, file.getName(), line);
            return true;
        }

    }
}
