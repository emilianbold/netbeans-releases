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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;


/**
 * Central part of status management, deduces and caches statuses of files under version control.
 *
 * @author Ondra Vrabec
 */
public class FileStatusCacheNewGeneration extends FileStatusCache {
    
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);
    private static final FileInformation FILE_INFORMATION_NEWLOCALLY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, false);

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.fileStatusCacheNewGeneration"); //NOI18N

    private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);
    private Mercurial     hg;
    private final Map<File, FileInformation> cachedFiles;
    private final RequestProcessor rp = new RequestProcessor("Mercurial.cacheNG", 1, true);
    private Map<File, FileInformation> modifiedFiles = null;
    
    FileStatusCacheNewGeneration() {
        this.hg = Mercurial.getInstance();
        cachedFiles = new HashMap<File, FileInformation>();
    }

    private void handleIgnoredFiles(final Set<File> files) {
        Runnable outOfAWT = new Runnable() {
            public void run() {
                for (File f : files) {
                    if (HgUtils.isIgnored(f, true)) {
                        refreshFileStatus(f, f.isDirectory() ? FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED, Collections.EMPTY_MAP, true);
                    }
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            rp.post(outOfAWT);
        } else {
            outOfAWT.run();
        }
    }

    private FileInformation checkForIgnoredFile (File file) {
        FileInformation fi = null;
        if (HgUtils.isIgnored(file, false)) {
            fi = FILE_INFORMATION_EXCLUDED;
        } else {
            handleIgnoredFiles(Collections.singleton(file));
        }
        return fi;
    }

    private FileInformation getInfo(File file) {
        synchronized (cachedFiles) {
            return cachedFiles.get(file);
        }
    }

    private void setInfo (File file, FileInformation info) {
        synchronized (cachedFiles) {
            cachedFiles.put(file, info);
            modifiedFiles = null;
        }
    }

    private void removeInfo (File file) {
        synchronized (cachedFiles) {
            cachedFiles.remove(file);
            modifiedFiles = null;
        }
    }

    @Override
    public FileInformation getStatus(File file) {
        boolean isDirectory = file.isDirectory();
        if (isDirectory && (HgUtils.isAdministrative(file) || HgUtils.isIgnored(file)))
            return FILE_INFORMATION_EXCLUDED_DIRECTORY;
        FileInformation fi = getInfo(file);
        if (fi == null) {
            if (!exists(file)) {
                fi = FILE_INFORMATION_UNKNOWN;
            } else if (HgUtils.isIgnored(file)) {
                fi = FILE_INFORMATION_EXCLUDED;
            } else if (isDirectory) {
                fi = refresh(file, REPOSITORY_STATUS_UNKNOWN);
            } else {
                fi = FILE_INFORMATION_UPTODATE;
            }
        }
        return fi;
    }

    @Override
    public FileInformation getCachedStatus(File file) {
        FileInformation info = getInfo(file);
        LOG.log(Level.FINER, "getCachedStatus for file {0}: {1}", new Object[] {file, info}); //NOI18N
        if (info == null) {
            if (hg.isManaged(file)) {
                hg.getMercurialInterceptor().pingRepositoryRootFor(file);
                info = checkForIgnoredFile(file);
                if (file.isDirectory()) {
                    info = createFolderFileInformation(file, info == null ? null : FILE_INFORMATION_EXCLUDED_DIRECTORY);
                } else {
                    if (info == null) {
                        info = FILE_INFORMATION_UPTODATE;
                    }
                }
            } else {
                info = file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }
            LOG.log(Level.FINER, "getCachedStatus: default for file {0}: {1}", new Object[] {file, info}); //NOI18N
        }
        return info;
    }

    private FileInformation createFolderFileInformation (File folder, FileInformation fi) {
        FileInformation info;
        // must lock, so possibly elsewhere-created information is not overwritten
        synchronized (cachedFiles) {
            info = getInfo(folder);
            if (info == null) {
                // create an uptodate directory
                info = fi == null ? new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true) : fi;
                setInfo(folder, info);
            }
        }
        return info;
    }

    // XXX probably useless method, use refresh instead
    @Override
    public Map<File, FileInformation> getScannedFiles(File dir, Map<File, FileInformation> interestingFiles) {
        getStatus(dir);
        return null;
    }

    @Override
    Map<File, FileInformation>  getAllModifiedFiles() {
        synchronized (cachedFiles) {
            Map<File, FileInformation> allModifiedFiles = modifiedFiles;
            if (allModifiedFiles == null) {
                allModifiedFiles = new HashMap<File, FileInformation>(cachedFiles.size());
                for (Map.Entry<File, FileInformation> e : cachedFiles.entrySet()) {
                    if ((e.getValue().getStatus() & FileInformation.STATUS_VERSIONED_UPTODATE) == 0) {
                        allModifiedFiles.put(e.getKey(), e.getValue());
                    }
                }
            }
            modifiedFiles = Collections.unmodifiableMap(allModifiedFiles);
            return modifiedFiles;
        }
    }

    /**
     * Refreshes all files under given roots in the cache.
     * @param rootFiles root files sorted under their's repository roots
     */
    @Override
    void refreshAllRoots (Map<File, File> rootFiles) {
        for (Map.Entry<File, File> refreshEntry : rootFiles.entrySet()) {
            File repository = refreshEntry.getKey();
            File root = refreshEntry.getValue();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "refreshAllRoots() root: {0}, repositoryRoot: {1} ", new Object[] {root.getAbsolutePath(), repository.getAbsolutePath()}); // NOI18N
            }
            Map<File, FileInformation> interestingFiles;
            try {
                // find all files with not up-to-date or ignored status
                interestingFiles = HgCommand.getInterestingStatus(repository, root);
                for (Map.Entry<File, FileInformation> interestingEntry : interestingFiles.entrySet()) {
                    // put the file's FI into the cache
                    File file = interestingEntry.getKey();
                    FileInformation fi = interestingEntry.getValue();
                    LOG.log(Level.FINE, "refreshAllRoots() file: {0} {1} ", new Object[] {file.getAbsolutePath(), fi}); // NOI18N
                    refreshFileStatus(file, fi, interestingFiles);
                }
                // clean all files originally in the cache but now being up-to-date or obsolete (as ignored && deleted)
                for (Map.Entry<File, FileInformation> entry : getAllModifiedFiles().entrySet()) {
                    File file = entry.getKey();
                    FileInformation fi = entry.getValue();
                    boolean exists = file.exists();
                    if (!interestingFiles.containsKey(file) // file no longer has an interesting status
                            && Utils.isAncestorOrEqual(root, file) // file is under the examined root
                            && ((fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0 && !exists || // file was ignored and is now deleted
                            (fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0 && (!exists || file.isFile()))) { // file is now up-to-date
                        LOG.log(Level.FINE, "refreshAllRoots() uninteresting file: {0} {1}", new Object[]{file, fi}); // NOI18N
                        // TODO better way to detect conflicts
                        if (HgCommand.existsConflictFile(file.getAbsolutePath())) {
                            refreshFileStatus(file, FILE_INFORMATION_CONFLICT, interestingFiles); // set the files status to 'IN CONFLICT'
                        } else {
                            refreshFileStatus(file, FILE_INFORMATION_UNKNOWN, interestingFiles); // remove the file from cache
                        }
                    }
                }
            } catch (HgException ex) {
                LOG.log(Level.INFO, "refreshAll() file: {0} {1} {2} ", new Object[] {repository.getAbsolutePath(), root.getAbsolutePath(), ex.toString()}); //NOI18N
            }
        }
    }

    @Override
    public FileInformation refresh(File file, FileStatus repositoryStatus) {
        File repositoryRoot = hg.getRepositoryRoot(file);
        FileInformation fi;
        if (repositoryRoot == null) {
            if (file.isDirectory()) {
                fi = FILE_INFORMATION_NOTMANAGED_DIRECTORY;
            } else {
                fi = FILE_INFORMATION_NOTMANAGED;
            }
        } else {
            refreshAllRoots(Collections.singletonMap(repositoryRoot, file));
            fi = getCachedStatus(file);
        }
        return fi;
    }

    @Override
    public void refreshFileStatus(File file, FileInformation fi, Map<File, FileInformation> interestingFiles, boolean alwaysFireEvent) {
        if(file == null || fi == null) return;
        
        FileInformation current = getInfo(file);
        if (equivalent(fi, current))  {
            if (equivalent(FILE_INFORMATION_NEWLOCALLY, fi)) {
                if (HgUtils.isIgnored(file)) {
                    LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is NotSharable", file.getAbsolutePath()); // NOI18N
                    fi = FILE_INFORMATION_EXCLUDED;
                } else {
                    if (alwaysFireEvent) {
                        fireFileStatusChanged(file, null, fi);
                    }
                    return;
                }
            } else if (!equivalent(FILE_INFORMATION_REMOVEDLOCALLY, fi)) {
                if (alwaysFireEvent) {
                    fireFileStatusChanged(file, null, fi);
                }
                return;
            }
        }
        if (equivalent(FILE_INFORMATION_NEWLOCALLY, fi)) {
            if (equivalent(FILE_INFORMATION_EXCLUDED, current)) {
                LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is Excluded", file.getAbsolutePath()); // NOI18N
                if (alwaysFireEvent) {
                    fireFileStatusChanged(file, null, fi);
                }
                return;
            } else if (current == null) {
                if (HgUtils.isIgnored(file)) {
                    LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but current is null and is not NotSharable", file.getAbsolutePath()); // NOI18N
                    fi = FILE_INFORMATION_EXCLUDED;
                }
            }
        }
        file = FileUtil.normalizeFile(file);
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
            removeInfo(file);
        } else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
            removeInfo(file);
        } else {
            setInfo(file, fi);
        }
        
        fireFileStatusChanged(file, current, fi);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.removePropertyChangeListener(listener);
    }

    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        File parent = file;
        FileInformation info;
        int counterUp = -1, counterDown = -1;
        if (newInfo != null) {
            if ((newInfo.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                counterUp = FileInformation.COUNTER_CONFLICTED_FILES;
            } else if ((newInfo.getStatus() & (FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) != 0) {
                counterUp = FileInformation.COUNTER_DELETED_FILES;
            } else if ((newInfo.getStatus() & (FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) != 0) {
                counterUp = FileInformation.COUNTER_NEW_FILES;
            } else if ((newInfo.getStatus() & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY) != 0) {
                counterUp = FileInformation.COUNTER_MODIFIED_FILES;
            }
        }
        if (oldInfo != null) {
            if ((oldInfo.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                counterDown = FileInformation.COUNTER_CONFLICTED_FILES;
            } else if ((oldInfo.getStatus() & (FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) != 0) {
                counterDown = FileInformation.COUNTER_DELETED_FILES;
            } else if ((oldInfo.getStatus() & (FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) != 0) {
                counterDown = FileInformation.COUNTER_NEW_FILES;
            } else if ((oldInfo.getStatus() & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY) != 0) {
                counterDown = FileInformation.COUNTER_MODIFIED_FILES;
            }
        }
        boolean direct = true;
        if (counterUp != -1 || counterDown != -1) {
            while ((parent = parent.getParentFile()) != null && (info = getCachedStatus(parent)) != null && (info.getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                boolean fireFolderChange = false;
                if (counterUp != -1 && info.addToCounter(counterUp, 1, direct)) {
                    fireFolderChange = true;
                }
                if (counterDown != -1 && info.addToCounter(counterDown, -1, direct)) {
                    fireFolderChange = true;
                }
                if (fireFolderChange) {
                    listenerSupport.firePropertyChange(PROP_FILE_STATUS_CHANGED, null, new ChangedEvent(parent, null, info));
                }
                direct = false;
            }
        }
        listenerSupport.firePropertyChange(PROP_FILE_STATUS_CHANGED, null, new ChangedEvent(file, oldInfo, newInfo));
    }

    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }

    /**
     * Two FileInformation objects are equivalent if their status contants are equal AND they both reperesent a file (or
     * both represent a directory) AND Entries they cache, if they can be compared, are equal.
     *
     * @param other object to compare to
     * @return true if status constants of both object are equal, false otherwise
     */
    private static boolean equivalent(FileInformation main, FileInformation other) {
        if (other == null || main.getStatus() != other.getStatus() || main.isDirectory() != other.isDirectory()) return false;

        FileStatus e1 = main.getStatus(null);
        FileStatus e2 = other.getStatus(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     *
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */
    private static boolean equal(FileStatus e1, FileStatus e2) {
        // TODO: use your own logic here
        return true;
    }

    // XXX should be here
    @Override
    public File [] listFiles(File dir) {
        Set<File> set = new HashSet<File>();
        Map<File, FileInformation> allFiles = getAllModifiedFiles();
        for (Map.Entry<File, FileInformation> entry : allFiles.entrySet()) {
            File file = entry.getKey();
            FileInformation info = entry.getValue();
            if (!info.isDirectory() && dir.equals(file.getParentFile())) {
                set.add(file);
            }
        }
        return set.toArray(new File[set.size()]);
    }

    // XXX called only from ResolveConflictsAction so i guess no exclusion test is needed (as is in FileStatusCache
    // cached argument not needed
    @Override
    public boolean containsFileOfStatus(VCSContext context, int includeStatus, boolean cached){
        assert (includeStatus & FileInformation.STATUS_VERSIONED_CONFLICT) != 0;
        Set<File> roots = context.getRootFiles();

        for (File root : roots) {
            FileInformation info = getCachedStatus(root);
            if (info.getCounter(FileInformation.COUNTER_CONFLICTED_FILES, VersioningSupport.isFlat(root)) > 0) {
                return true;
            }
        }
        return false;
    }



    // XXX should be here
    @Override
    public File [] listFiles(VCSContext context, int includeStatus) {
        Set<File> roots = context.getRootFiles();
        Set<File> set = listFilesIntern(roots.toArray(new File[roots.size()]), includeStatus);
        if (context.getExclusions().size() > 0) {
            for (File excluded : context.getExclusions()) {
                for (Iterator j = set.iterator(); j.hasNext();) {
                    File file = (File) j.next();
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        j.remove();
                    }
                }
            }
        }
        return set.toArray(new File[set.size()]);
    }

    // XXX should be here
    @Override
    public File [] listFiles(File[] roots, int includeStatus) {
        Set<File> listedFiles = listFilesIntern(roots, includeStatus);
        return listedFiles.toArray(new File[listedFiles.size()]);
    }

    private Set<File> listFilesIntern(File[] roots, int includeStatus) {
        Set<File> set = new HashSet<File>();
        Map<File, FileInformation> allFiles = getAllModifiedFiles();
        for (Map.Entry<File, FileInformation> entry : allFiles.entrySet()) {
            File file = entry.getKey();
            FileInformation info = entry.getValue();
            if ((info.getStatus() & includeStatus) == 0) continue;
            for (int j = 0; j < roots.length; j++) {
                File root = roots[j];
                if (VersioningSupport.isFlat(root)) {
                    if (file.getParentFile().equals(root)) {
                        set.add(file);
                        break;
                    }
                } else {
                    if (Utils.isAncestorOrEqual(root, file)) {
                        File fileRoot = hg.getRepositoryRoot(file);
                        File rootRoot = hg.getRepositoryRoot(root);
                        // Make sure that file is in same repository as root
                        if (rootRoot != null && rootRoot.equals(fileRoot)) {
                            set.add(file);
                            break;
                        }
                        break;
                    }
                }
            }
        }
        return set;
    }

    @Override
    public void refreshFileStatus(File file, FileInformation fi, Map<File, FileInformation> interestingFiles ) {
        refreshFileStatus(file, fi, interestingFiles, false);
    }

    // XXX delete eventually
    public void refreshAll(File root) {
        // nothing
    }

    // XXX evaluate
    @Override
    public void refreshCached(File root) {
        refreshAllRoots(Collections.singletonMap(hg.getRepositoryRoot(root), root));
    }

    // XXX should be here
    @Override
    public void refreshCached(VCSContext ctx) {
        for (File root : ctx.getRootFiles()) {
            refreshCached(root);
        }
    }

    // XXX should be here, called after hg init
    @Override
    public void addToCache(Set<File> files) {
        if (files.size() > 0) {
            hg.getMercurialInterceptor().pingRepositoryRootFor(files.iterator().next());
        }
    }

    // XXX eventually delete
    @Override
    Map<File, FileInformation> getAllModifiedFilesCached (final boolean changed[]) {
        changed[0] = false;
        return getAllModifiedFiles();
    }

    // XXX should be here
    @Override
    public void notifyFileChanged(File file) {
        fireFileStatusChanged(file, null, FILE_INFORMATION_UPTODATE);
    }
}
