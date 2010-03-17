/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial;

import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.mercurial.util.HgUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.modules.mercurial.util.HgSearchHistorySupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * @author Maros Sandor
 */
public class MercurialInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private ConcurrentLinkedQueue<File> filesToRefresh = new ConcurrentLinkedQueue<File>();

    private RequestProcessor.Task refreshTask;

    private static final RequestProcessor rp = new RequestProcessor("MercurialRefresh", 1, true);
    private final HgFolderEventsHandler hgFolderEventsHandler;
    private static final boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.mercurial.autoRefreshDisabled", "false")); //NOI18N

    public MercurialInterceptor() {
        cache = Mercurial.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
        hgFolderEventsHandler = new HgFolderEventsHandler();
    }

    @Override
    public boolean beforeDelete(File file) {
        Mercurial.LOG.fine("beforeDelete " + file);
        if (file == null) return false;
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;

        // we don't care about ignored files
        // IMPORTANT: false means mind checking the sharability as this might cause deadlock situations
        if(HgUtils.isIgnored(file, false)) return false; // XXX what about other events?
        return true;
    }

    @Override
    public void doDelete(File file) throws IOException {
        // XXX runnig hg rm for each particular file when removing a whole firectory might no be neccessery:
        //     just delete it via file.delete and call, group the files in afterDelete and schedule a delete
        //     fo the parent or for a bunch of files at once. 
        Mercurial.LOG.fine("doDelete " + file);
        if (file == null) return;
        Mercurial hg = Mercurial.getInstance();
        File root = hg.getRepositoryRoot(file);
        try {
            file.delete();
            HgCommand.doRemove(root, file, null);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.FINE, "doDelete(): File: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
        }  
    }

    @Override
    public void afterDelete(final File file) {
        Mercurial.LOG.fine("afterDelete " + file);
        if (file == null) return;
        // we don't care about ignored files
        // IMPORTANT: false means mind checking the sharability as this might cause deadlock situations
        if(HgUtils.isIgnored(file, false)) {
            if (Mercurial.LOG.isLoggable(Level.FINER)) {
                Mercurial.LOG.log(Level.FINE, "skipping afterDelete(): File: {0} is ignored", new Object[] {file.getAbsolutePath()}); // NOI18N
            }
            return;
        }
        reScheduleRefresh(800, file);
    }

    @Override
    public boolean beforeMove(File from, File to) {
        Mercurial.LOG.fine("beforeMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return true;
        
        Mercurial hg = Mercurial.getInstance();
        if (hg.isManaged(from)) {
            return hg.isManaged(to);
        }
        return super.beforeMove(from, to);
    }

    @Override
    public void doMove(final File from, final File to) throws IOException {
        Mercurial.LOG.fine("doMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return;
        if (SwingUtilities.isEventDispatchThread()) {
            Mercurial.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace()); // NOI18N
        }
        hgMoveImplementation(from, to);
    }

    private void hgMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        final Mercurial hg = Mercurial.getInstance();
        final File root = hg.getRepositoryRoot(srcFile);
        final File dstRoot = hg.getRepositoryRoot(dstFile);
        if (root == null) return;

        Mercurial.LOG.log(Level.FINE, "hgMoveImplementation(): File: {0} {1}", new Object[] {srcFile, dstFile}); // NOI18N

        boolean result = srcFile.renameTo(dstFile);
        if (!result) {
            Mercurial.LOG.log(Level.INFO, "Cannot rename file {0} to {1}", new Object[] {srcFile, dstFile});
        }
        // no need to do rename after in a background thread, code requiring the bg thread (see #125673) no more exists
        OutputLogger logger = OutputLogger.getLogger(root.getAbsolutePath());
        try {
            if (root.equals(dstRoot)) {
                HgCommand.doRenameAfter(root, srcFile, dstFile, logger);
            }
        } catch (HgException e) {
            Mercurial.LOG.log(Level.FINE, "Mercurial failed to rename: File: {0} {1}", new Object[]{srcFile.getAbsolutePath(), dstFile.getAbsolutePath()}); // NOI18N
        } finally {
            logger.closeLog();
        }
    }

    @Override
    public void afterMove(final File from, final File to) {
        Mercurial.LOG.fine("afterMove " + from + "->" + to);
        if (from == null || to == null || !to.exists()) return;

        File parent = from.getParentFile();
        // There is no point in refreshing the cache for ignored files.
        if (parent != null && !HgUtils.isIgnored(parent, false)) {
            reScheduleRefresh(800, from);
        }
        // target needs to refreshed, too
        parent = to.getParentFile();
        // There is no point in refreshing the cache for ignored files.
        if (parent != null && !HgUtils.isIgnored(parent, false)) {
            reScheduleRefresh(800, to);
        }
    }
    
    @Override
    public boolean beforeCreate(final File file, boolean isDirectory) {
        Mercurial.LOG.fine("beforeCreate " + file + " " + isDirectory);
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;
        if (!isDirectory && !file.exists()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): LocallyDeleted: {0}", file); // NOI18N
                Mercurial hg = Mercurial.getInstance();
                final File root = hg.getRepositoryRoot(file);
                if (root == null) return false;
                final OutputLogger logger = Mercurial.getInstance().getLogger(root.getAbsolutePath());
                try {
                    List<File> revertFiles = new ArrayList<File>();
                    revertFiles.add(file);
                    HgCommand.doRevert(root, revertFiles, null, false, logger);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, "beforeCreate(): File: {0} {1}", new Object[]{file.getAbsolutePath(), ex.toString()}); // NOI18N
                }
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): afterWaitFinished: {0}", file); // NOI18N
                logger.closeLog();
                file.delete();
            }
        }
        return false;
    }

    @Override
    public void doCreate(File file, boolean isDirectory) throws IOException {
        Mercurial.LOG.fine("doCreate " + file + " " + isDirectory);
        super.doCreate(file, isDirectory);
    }

    @Override
    public void afterCreate(final File file) {
        Mercurial.LOG.fine("afterCreate " + file);
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(800, file);
        }
    }
    
    @Override
    public void afterChange(final File file) {
        if (file.isDirectory()) return;
        Mercurial.LOG.log(Level.FINE, "afterChange(): {0}", file);      //NOI18N
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(800, file);
        }
    }

    @Override
    public Object getAttribute(final File file, String attrName) {
        if("ProvidedExtensions.RemoteLocation".equals(attrName)) {
            return getRemoteRepository(file);
        } else if("ProvidedExtensions.Refresh".equals(attrName)) {
            return new Runnable() {
                public void run() {
                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    cache.refresh(file);
                }
            };
        } else if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new HgSearchHistorySupport(file);
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    private String getRemoteRepository(File file) {
        return HgUtils.getRemoteRepository(file);
    }

    public Boolean isRefreshScheduled(File file) {
        return filesToRefresh.contains(file);
    }

    private void reScheduleRefresh(int delayMillis, File fileToRefresh) {
        // refresh all at once
        Mercurial.STATUS_LOG.fine("reScheduleRefresh: adding " + fileToRefresh.getAbsolutePath());
        if (HgUtils.isPartOfMercurialMetadata(fileToRefresh)) {
            if (HgUtils.HG_FOLDER_NAME.equals(fileToRefresh.getParentFile().getName())
                    && isHandledHgFolderFile(fileToRefresh)) {
                hgFolderEventsHandler.handleHgFolderEvent(fileToRefresh);
            }
        } else {
            filesToRefresh.add(fileToRefresh);
        }
        refreshTask.schedule(delayMillis);
    }

    private void reScheduleRefresh (int delayMillis, Set<File> filesToRefresh) {
        // refresh all at once
        Mercurial.STATUS_LOG.fine("reScheduleRefresh: adding " + filesToRefresh);
        this.filesToRefresh.addAll(filesToRefresh);
        refreshTask.schedule(delayMillis);
    }

    /**
     * Checks if administrative folder for a repository with the file is registered.
     * If it is not yet registered, this creates a fileobject for the .hg and keeps a reference to it.
     * @param file
     */
    void pingRepositoryRootFor(final File file) {
        if (!AUTOMATIC_REFRESH_ENABLED) {
            return;
        }
        hgFolderEventsHandler.initializeFor(file);
    }

    /**
     * Refreshes cached modification timestamp of hg administrative folder file for the given repository
     * @param repository
     */
    void refreshHgFolderTimestamp(File repository) {
        assert repository != null;
        if (repository == null) {
            return;
        }
        File hgFolder = HgUtils.getHgFolderForRoot(repository);
        hgFolderEventsHandler.refreshHgFolderTimestamp(hgFolder, hgFolder.lastModified());
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    Set<File> getSeenRoots (File repositoryRoot) {
        return hgFolderEventsHandler.getSeenRoots(repositoryRoot);
    }

    private class RefreshTask implements Runnable {
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Mercurial.STATUS_LOG, "MercurialInterceptor.refreshTask")) { //NOI18N
                return;
            }
            // fill a fileset with all the modified files
            HashSet<File> files = new HashSet<File>(filesToRefresh.size());
            File file;
            while ((file = filesToRefresh.poll()) != null) {
                files.add(file);
            }
            cache.refreshAllRoots(files);
        }
    }

    private static final HashSet<String> HANDLED_HGFOLDER_FILES = new HashSet<String>(Arrays.asList(new String[] {
        "branch",                                                       //NOI18N
        "dirstate",                                                     //NOI18N
        "undo.branch",                                                  //NOI18N
        "undo.dirstate"                                                 //NOI18N
    }));

    private static boolean isHandledHgFolderFile (File file) {
        return HANDLED_HGFOLDER_FILES.contains(file.getName());
    }

    private class HgFolderEventsHandler {
        private final HashMap<File, Long> hgFolders = new HashMap<File, Long>(5);
        private final HashMap<File, Set<File>> seenRoots = new HashMap<File, Set<File>>(5);
        private final WeakHashMap<File, FileObject[]> hgFolderChildren = new WeakHashMap<File, FileObject[]>(5);

        private final HashSet<File> filesToInitialize = new HashSet<File>();
        private RequestProcessor rp = new RequestProcessor("MercurialInterceptorEventsHandlerRP", 1); //NOI18N
        private RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            public void run() {
                initializeFiles();
            }
        });
        private RequestProcessor.Task refreshOpenFilesTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Set<File> openFiles = Utils.getOpenFiles();
                for (File file : openFiles) {
                    Mercurial.getInstance().notifyFileChanged(file);
                }
            }
        });

        /**
         *
         * @param hgFolder
         * @param timestamp new timestamp, value 0 will remove the item from the cache
         */
        private void refreshHgFolderTimestamp(File hgFolder, long timestamp) {
            boolean exists = timestamp > 0 || hgFolder.exists();
            synchronized (hgFolders) {
                Long ts;
                if (exists && (ts = hgFolders.get(hgFolder)) != null && ts >= timestamp) {
                    // do not enter the filesystem module unless really need to
                    return;
                }
            }
            FileObject hgFolderFO = exists ? FileUtil.toFileObject(hgFolder) : null;
            synchronized (hgFolders) {
                hgFolders.remove(hgFolder);
                if (exists) {
                    hgFolders.put(hgFolder, Long.valueOf(timestamp));
                    // must also keep references to children, so they are refreshed, too
                    // and FS events are fired for them
                    Set<FileObject> children = new HashSet<FileObject>();
                    for (FileObject fo : hgFolderFO.getChildren()) {
                        if (fo.isValid() && fo.isData()) {
                            children.add(fo);
                        }
                    }
                    // also add the .hg folder itself - needed when there is not yet a dirstate file (e.g. right after hg init)
                    // and to fire an event when a file is added to .hg folder
                    children.add(hgFolderFO);
                    hgFolderChildren.put(hgFolder, children.toArray(new FileObject[children.size()]));
                } else {
                    Mercurial.STATUS_LOG.fine("refreshHgFolderTimestamp: " + hgFolder.getAbsolutePath() + " no longer exists"); //NOI18N
                }
            }
        }

        private void handleHgFolderEvent(File hgFolderFile) {
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("mercurial.handleDirstateEvents", "true"))) { //NOI18N
                File hgFolder = FileUtil.normalizeFile(hgFolderFile.getParentFile());
                Mercurial.STATUS_LOG.fine("handleHgFolderEvent: special FS event handling for " + hgFolderFile.getAbsolutePath()); //NOI18N
                long lastModified = hgFolder.lastModified();
                Long lastCachedModified = null;
                synchronized (hgFolders) {
                    lastCachedModified = hgFolders.get(hgFolder);
                    if (lastCachedModified == null || lastCachedModified < lastModified || lastModified == 0) {
                        refreshHgFolderTimestamp(hgFolder, lastModified);
                        lastCachedModified = null;
                    }
                }
                if (lastCachedModified == null) {
                    File repository = hgFolder.getParentFile();
                    Mercurial.STATUS_LOG.fine("handleDirstateEvent: planning repository scan for " + repository.getAbsolutePath()); //NOI18N
                    reScheduleRefresh(3000, getSeenRoots(repository)); // scan repository root
                    refreshOpenFilesTask.schedule(3000);
                }
            }
        }

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
            boolean alreadyAdded = true;
            Set<File> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                if (!seenRootsForRepository.contains(repositoryRoot)) {
                    // try to add the file only when the repository root is not yet registered
                    rootToAdd = FileUtil.normalizeFile(rootToAdd);
                    alreadyAdded = HgUtils.prepareRootFiles(repositoryRoot, seenRootsForRepository, rootToAdd);
                }
            }
            return alreadyAdded;
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
                // select repository root for the file and finds it's .hg folder
                File repositoryRoot = Mercurial.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    File hgFolder = HgUtils.getHgFolderForRoot(repositoryRoot);
                    if (!addSeenRoot(repositoryRoot, file)) {
                        synchronized (hgFolders) {
                            // this means the repository has not yet been scanned, so scan it
                            Mercurial.STATUS_LOG.fine("pingRepositoryRootFor: planning a scan for " + repositoryRoot.getAbsolutePath() + " - " + file.getAbsolutePath()); //NOI18N
                            reScheduleRefresh(4000, file);
                            if (!hgFolders.containsKey(hgFolder)) {
                                if (hgFolder.isDirectory()) {
                                    // however there might be NO .hg folder, especially for just initialized repositories
                                    // so keep the reference only for existing and valid .hg folders
                                    hgFolders.put(hgFolder, null);
                                    refreshHgFolderTimestamp(hgFolder, hgFolder.lastModified());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
