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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author ondra
 */
class FilesystemInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final Set<File> filesToRefresh = new HashSet<File>();

    private RequestProcessor.Task refreshTask;

    private static final RequestProcessor rp = new RequestProcessor("GitRefresh", 1, true);
    private final GitFolderEventsHandler gitFolderEventsHandler;
    private static boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.git.autoRefreshDisabled", "false")); //NOI18N
    private static final String INDEX_FILE_NAME = "index";
    private static final Logger LOG = Logger.getLogger(FilesystemInterceptor.class.getName());

    public FilesystemInterceptor () {
        cache = Git.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
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
                git.getClient(root).reset(new File[] { file }, "HEAD", ProgressMonitor.NULL_PROGRESS_MONITOR);
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
                git.getClient(root).remove(new File[] { file }, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
            } else if (file.exists()) {
                file.delete();
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
        if (from == null || to == null || to.exists()) return;
        
        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(from);
        File dstRoot = git.getRepositoryRoot(to);
        if (root == null) return;
        try {
            if (root.equals(dstRoot)) {
                git.getClient(root).rename(from, to, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
            } else {
                boolean result = from.renameTo(to);
                if (!result) {
                    throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                }
                git.getClient(root).remove(new File[] { from }, true, ProgressMonitor.NULL_PROGRESS_MONITOR);
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public void afterMove(final File from, final File to) {
        LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || !to.exists()) return;

        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(from).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(from));
        }
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

        if (root == null) return;
        try {
            if (root.equals(dstRoot)) {
                git.getClient(root).copyAfter(from, to, ProgressMonitor.NULL_PROGRESS_MONITOR);
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
    void refreshMetadataTimestamp (File repository) {
        assert repository != null;
        if (repository == null) {
            return;
        }
        File indexFile = new File(GitUtils.getGitFolderForRoot(repository), INDEX_FILE_NAME);
        gitFolderEventsHandler.refreshIndexFileTimestamp(indexFile, indexFile.lastModified());
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Git.STATUS_LOG, "GitInterceptor.refreshTask")) { //NOI18N
                return;
            }
            Set<File> files;
            synchronized (filesToRefresh) {
                files = new HashSet<File>(filesToRefresh);
                filesToRefresh.clear();
            }
            cache.refreshAllRoots(files);
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
    
    private class GitFolderEventsHandler {
        private final HashMap<File, Set<File>> seenRoots = new HashMap<File, Set<File>>();
        private final HashMap<File, Long> indexFiles = new HashMap<File, Long>(5);
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

        /**
         *
         * @param indexFile
         * @param timestamp new timestamp, value 0 and missing indexFile.getParentFile() will remove the item from the cache
         */
        private void refreshIndexFileTimestamp (File indexFile, long timestamp) {
            if (Utils.isAncestorOrEqual(new File(System.getProperty("java.io.tmpdir")), indexFile)) { //NOI18N
                // skip repositories in temp folder
                return;
            }
            final File gitFolder = FileUtil.normalizeFile(indexFile.getParentFile());
            boolean exists = timestamp > 0 || gitFolder.exists();
            synchronized (indexFiles) {
                Long ts;
                if (exists && (ts = indexFiles.get(indexFile)) != null && ts >= timestamp) {
                    // do not enter the filesystem module unless really need to
                    return;
                }
            }
            boolean add = false;
            boolean remove = false;
            synchronized (indexFiles) {
                indexFiles.remove(indexFile);
                FileChangeListener list = gitFolderRLs.remove(gitFolder);
                if (exists) {
                    indexFiles.put(indexFile, Long.valueOf(timestamp));
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
                    Git.STATUS_LOG.fine("refreshAdminFolderTimestamp: " + indexFile.getAbsolutePath() + " no longer exists"); //NOI18N
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
                        Git.STATUS_LOG.fine("initializeFiles: planning a scan for " + repositoryRoot.getAbsolutePath() + " - " + file.getAbsolutePath()); //NOI18N
                        reScheduleRefresh(4000, Collections.singleton(file));
                        synchronized (indexFiles) {
                            File indexFile = FileUtil.normalizeFile(new File(GitUtils.getGitFolderForRoot(repositoryRoot), INDEX_FILE_NAME));
                            if (!indexFiles.containsKey(indexFile)) {
                                if (indexFile.isFile()) {
                                    indexFiles.put(indexFile, null);
                                    refreshIndexFileTimestamp(indexFile, indexFile.lastModified());
                                }
                            }
                        }
                    }
                }
            }
            Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: finished"); //NOI18N
        }

        private long refreshAdminFolder (File gitFolder) {
            long lastModified = 0;
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                File indexFile = FileUtil.normalizeFile(new File(gitFolder, INDEX_FILE_NAME));
                Git.STATUS_LOG.finer("refreshAdminFolder: special FS event handling for " + indexFile.getAbsolutePath()); //NOI18N
                lastModified = indexFile.lastModified();
                Long lastCachedModified = null;
                synchronized (indexFiles) {
                    lastCachedModified = indexFiles.get(indexFile);
                    if (lastCachedModified == null || lastCachedModified < lastModified || lastModified == 0) {
                        refreshIndexFileTimestamp(indexFile, lastModified);
                        lastCachedModified = null;
                    }
                }
                if (lastCachedModified == null) {
                    File repository = gitFolder.getParentFile();
                    RepositoryInfo.refreshAsync(repository);
                    Git.STATUS_LOG.fine("refreshAdminFolder: planning repository scan for " + repository.getAbsolutePath()); //NOI18N
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
}
