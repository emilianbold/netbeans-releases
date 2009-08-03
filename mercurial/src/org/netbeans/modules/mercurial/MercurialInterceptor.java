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
import org.netbeans.modules.versioning.util.Utils;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mercurial.util.HgUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
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
    private final HashSet<FileObject> dirStates = new HashSet<FileObject>(5);

    public MercurialInterceptor() {
        cache = Mercurial.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask());
    }

    public boolean beforeDelete(File file) {
        Mercurial.LOG.fine("beforeDelete " + file);
        if (file == null) return false;
        if (HgUtils.isPartOfMercurialMetadata(file)) return false;

        // we don't care about ignored files
        // IMPORTANT: false means mind checking the sharability as this might cause deadlock situations
        if(HgUtils.isIgnored(file, false)) return false; // XXX what about other events?
        return true;
    }

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

    public boolean beforeMove(File from, File to) {
        Mercurial.LOG.fine("beforeMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return true;
        
        Mercurial hg = Mercurial.getInstance();
        if (hg.isManaged(from)) {
            return hg.isManaged(to);
        }
        return super.beforeMove(from, to);
    }

    public void doMove(final File from, final File to) throws IOException {
        Mercurial.LOG.fine("doMove " + from + "->" + to);
        if (from == null || to == null || to.exists()) return;
        
        if (SwingUtilities.isEventDispatchThread()) {

            Mercurial.LOG.log(Level.INFO, "Warning: launching external process in AWT", new Exception().fillInStackTrace()); // NOI18N
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        hgMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };

            Mercurial.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }

            // end of hack

        } else {
            hgMoveImplementation(from, to);
        }
    }

    private void hgMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        final Mercurial hg = Mercurial.getInstance();
        final File root = hg.getRepositoryRoot(srcFile);
        final File dstRoot = hg.getRepositoryRoot(dstFile);
        if (root == null) return;

        RequestProcessor rp = hg.getRequestProcessor(root);

        Mercurial.LOG.log(Level.FINE, "hgMoveImplementation(): File: {0} {1}", new Object[] {srcFile, dstFile}); // NOI18N

        srcFile.renameTo(dstFile);
        Runnable moveImpl = new Runnable() {
            public void run() {
                OutputLogger logger = OutputLogger.getLogger(root.getAbsolutePath());
                try {
                    if (root.equals(dstRoot)) {
                        HgCommand.doRenameAfter(root, srcFile, dstFile, logger);
                    }
                } catch (HgException e) {
                    Mercurial.LOG.log(Level.FINE, "Mercurial failed to rename: File: {0} {1}", new Object[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()}); // NOI18N
                } finally {
                    logger.closeLog();
                }
            }
        };

        rp.post(moveImpl);
    }

    public void afterMove(final File from, final File to) {
        Mercurial.LOG.fine("afterMove " + from + "->" + to);
        if (from == null || to == null || !to.exists()) return;
        if (to.isDirectory()) return;

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
                final Throwable innerT[] = new Throwable[1];
                Runnable outOfAwt = new Runnable() {
                    public void run() {
                        try {
                            List<File> revertFiles = new ArrayList<File>();
                            revertFiles.add(file);
                            HgCommand.doRevert(root, revertFiles, null, false, logger);
                        } catch (Throwable t) {
                            innerT[0] = t;
                        }
                    }
                };

                Mercurial.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
                if (innerT[0] != null) {
                    Mercurial.LOG.log(Level.FINE, "beforeCreate(): File: {0} {1}", new Object[] {file.getAbsolutePath(), innerT[0].toString()}); // NOI18N
                }
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): afterWaitFinished: {0}", file); // NOI18N
                logger.closeLog();
                file.delete();
            }
        }
        return false;
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        Mercurial.LOG.fine("doCreate " + file + " " + isDirectory);
        super.doCreate(file, isDirectory);
    }

    public void afterCreate(final File file) {
        Mercurial.LOG.fine("afterCreate " + file);
        if (file.isDirectory()) return;
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(800, file.getParentFile());
        }
    }
    
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
                    try {
                        File repository = Mercurial.getInstance().getRepositoryRoot(file);
                        if (repository == null) {
                            return;
                        }
                        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                        cache.refreshCached(file);
                        StatusAction.refreshFile(file, repository, null, cache);
                    } catch (HgException ex) {
                        ExceptionHandler eh = new ExceptionHandler(ex);
                        eh.notifyException();
                    }
                }
            };
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    private String getRemoteRepository(File file) {
        if(file == null) return null;
        String remotePath = HgRepositoryContextCache.getInstance().getPullDefault(file);
        if(remotePath == null || remotePath.trim().equals("")) {
            Mercurial.LOG.log(Level.FINE, "No defalt pull available for managed file : [" + file + "]");
            remotePath = HgRepositoryContextCache.getInstance().getPushDefault(file);

            Mercurial.LOG.log(Level.WARNING, "No defalt pull or push available for managed file : [" + file + "]");
        }
        if(remotePath != null) {
            remotePath = remotePath.trim();
            remotePath = HgUtils.removeHttpCredentials(remotePath);
            if(remotePath.equals("")) {
                // return null if empty
                remotePath = null;
            }
        }
        return remotePath;
    }

    public Boolean isRefreshScheduled(File file) {
        return filesToRefresh.contains(file);
    }

    private void reScheduleRefresh(int delayMillis, File fileToRefresh) {
        if (!"false".equals(System.getProperty("mercurial.onEventRefreshRoot"))) { //NOI18N
            // refresh all at once
            Mercurial.STATUS_LOG.fine("reScheduleRefresh: adding " + fileToRefresh.getAbsolutePath());
            if (HgUtils.isPartOfMercurialMetadata(fileToRefresh)) {
                if ("dirstate".equals(fileToRefresh.getName())) {
                    // XXX handle dirstate events
                    Mercurial.STATUS_LOG.fine("special FS event handling for " + fileToRefresh.getAbsolutePath());
                }
            } else {
                filesToRefresh.add(fileToRefresh);
            }
        } else {
            // refresh one by one
            File parent = fileToRefresh.getParentFile();
            if (!filesToRefresh.contains(parent)) {
                if (!filesToRefresh.offer(parent)) {
                    Mercurial.LOG.log(Level.FINE, "reScheduleRefresh failed to add to filesToRefresh queue {0}", fileToRefresh);
                }
            }
        }
        refreshTask.schedule(delayMillis);
    }

    void pingRepositoryRootFor(final File file) {
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                File repositoryRoot = Mercurial.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    File dirstate = new File(new File(repositoryRoot, ".hg"), "dirstate"); //NOI18N
                    FileObject fo = FileUtil.toFileObject(dirstate);
                    synchronized (dirStates) {
                        if (!dirStates.contains(fo)) {
                            reScheduleRefresh(2000, repositoryRoot); // the whole clone
                            if (fo != null && fo.isValid() && fo.isData()) {
                                dirStates.add(fo);
                            }
                        }
                    }
                }
            }
        });
    }

    private class RefreshTask implements Runnable {
        public void run() {
            Thread.interrupted();
            File fileToRefresh;
            if (!"false".equals(System.getProperty("mercurial.onEventRefreshRoot"))) { //NOI18N
                // fill a fileset with all the modified files
                HashSet<File> files = new HashSet<File>(filesToRefresh.size());
                File file;
                while ((file = filesToRefresh.poll()) != null) {
                    files.add(file);
                }
                refreshAll(files);
            } else if ((fileToRefresh = filesToRefresh.poll()) != null) {
                Mercurial.LOG.log(Level.INFO, "RefreshTask called refreshAll {0}", fileToRefresh);
                cache.refreshAll(fileToRefresh);
                fileToRefresh = filesToRefresh.peek();
                if (fileToRefresh != null) {
                    refreshTask.schedule(0);
                }
            }
        }
    }

    /**
     * Prepares refresh candidates and calls the cache refresh
     * @param files
     */
    private void refreshAll (final Set<File> files) {
        long startTime = 0;
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            Mercurial.STATUS_LOG.fine("refreshAll: starting for " + files.size() + " files.");
        }
        if (files.isEmpty()) {
            return;
        }
        HashMap<File, File> rootFiles = new HashMap<File, File>(5);
        File firstFile = files.iterator().next();
        File firstRepository = Mercurial.getInstance().getRepositoryRoot(firstFile);
        assert firstRepository != null;
        rootFiles.put(firstRepository, FileUtil.normalizeFile(firstFile));

        for (File file : files) {
            boolean added = false;
            file = FileUtil.normalizeFile(file);
            for (Map.Entry<File, File> entry : rootFiles.entrySet()) {
                File ancestorCandidate = entry.getValue();
                File childCandidate = file;
                added = true;
                if (childCandidate.equals(ancestorCandidate)) {
                    // file has already been inserted
                    break;
                }
                if (file.getAbsolutePath().length() < entry.getValue().getAbsolutePath().length()) {
                    // entry path is too short to be the file's parent
                    ancestorCandidate = file;
                    childCandidate = entry.getValue();
                }
                if (!Utils.isAncestorOrEqual(ancestorCandidate, childCandidate)) {
                    ancestorCandidate = Utils.getCommonParent(childCandidate, ancestorCandidate);
                }
                if (ancestorCandidate == entry.getValue()) {
                    // already added
                    break;
                } else if (ancestorCandidate != null && entry.getKey().equals(Mercurial.getInstance().getRepositoryRoot(ancestorCandidate))) {
                    // file is under the repository root
                    entry.setValue(ancestorCandidate);
                    break;
                } else {
                    added = false;
                }
            }
            if (!added) {
                // not added yet
                File repository = Mercurial.getInstance().getRepositoryRoot(file);
                assert repository != null;
                rootFiles.put(repository, FileUtil.normalizeFile(file));
            }
        }
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            Mercurial.STATUS_LOG.fine("refreshAll: starting status scan for " + rootFiles.values() + " after " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
        }
        if (!rootFiles.isEmpty()) {
            cache.refreshAllRoots(rootFiles);
        }
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            Mercurial.STATUS_LOG.fine("refreshAll: finishes status scan after " + (System.currentTimeMillis() - startTime));
        }
    }
}
