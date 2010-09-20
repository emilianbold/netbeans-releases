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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * TODO: handle initial git scan
 * TODO: handle FS events
 * @author ondra
 */
class FilesystemInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final Set<File> filesToRefresh = new HashSet<File>();

    private RequestProcessor.Task refreshTask;

    private static final RequestProcessor rp = new RequestProcessor("GitRefresh", 1, true);
    private final GitFolderEventsHandler gitFolderEventsHandler;
    private static final boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.git.autoRefreshDisabled", "false")); //NOI18N

    public FilesystemInterceptor () {
        cache = Git.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
        gitFolderEventsHandler = new GitFolderEventsHandler();
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

    // TODO: call from VCSInterceptor methods
    private void reScheduleRefresh (int delayMillis, Set<File> filesToRefresh) {
        // refresh all at once
        Git.STATUS_LOG.fine("reScheduleRefresh: adding " + filesToRefresh);
        boolean changed;
        synchronized (this.filesToRefresh) {
            changed = this.filesToRefresh.addAll(filesToRefresh);
        }
        if (changed) {
            refreshTask.schedule(delayMillis);
        }
    }

    private class GitFolderEventsHandler {
        private final HashMap<File, Set<File>> seenRoots = new HashMap<File, Set<File>>(5);

        private final HashSet<File> filesToInitialize = new HashSet<File>();
        private RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            @Override
            public void run() {
                initializeFiles();
            }
        });

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

        private void initializeFiles () {
            File file = null;
            while ((file = getFileToInitialize()) != null) {
                Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: {0}", file.getAbsolutePath()); //NOI18N
                // select repository root for the file and finds it's .git folder
                File repositoryRoot = Git.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    if (addSeenRoot(repositoryRoot, file)) {
                        reScheduleRefresh(4000, Collections.singleton(file));
                        // TODO: init cached timestamps, see mercurial
                    }
                }
            }
            Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: finished"); //NOI18N
        }
    }
}
