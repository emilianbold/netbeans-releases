/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.filesystems.FileUtil;
import java.util.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.mercurial.util.HgCommand;
import java.util.logging.Level;


/**
 * Central part of status management, deduces and caches statuses of files under version control.
 *
 * @author Maros Sandor
 */
public class FileStatusCache {
    
    /**
     * Indicates that status of a file changed and listeners SHOULD check new status
     * values if they are interested in this file.
     * The New value is a ChangedEvent object (old FileInformation object may be null)
     */
    public static final String PROP_FILE_STATUS_CHANGED = "status.changed"; // NOI18N
    
    /**
     * A special map saying that no file inside the folder is managed.
     */
    private static final Map<File, FileInformation> NOT_MANAGED_MAP = new NotManagedMap();
    
    public static final FileStatus REPOSITORY_STATUS_UNKNOWN  = null;
    
    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);
    private static final FileInformation FILE_INFORMATION_NEWLOCALLY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, false);
    public static final FileInformation FILE_INFORMATION_CONFLICT = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, false);
    public static final FileInformation FILE_INFORMATION_REMOVEDLOCALLY = new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, false);
    
    private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);
    
    /**
     * Caches status of files in memory ant on disk
     */
    private final Turbo turbo;
    
    private final String FILE_STATUS_MAP = DiskMapTurboProvider.ATTR_STATUS_MAP;
    
    private DiskMapTurboProvider    cacheProvider;
    
    private Mercurial     hg;
    
    FileStatusCache() {
        this.hg = Mercurial.getInstance();
        cacheProvider = new DiskMapTurboProvider();
        turbo = Turbo.createCustom(new CustomProviders() {
            private final Set providers = Collections.singleton(cacheProvider);
            public Iterator providers() {
                return providers.iterator();
            }
        }, 200, 5000);
    }
    
    // --- Public interface -------------------------------------------------
    
    /**
     * Lists <b>modified files</b> and all folders that are known to be inside
     * this folder. There are locally modified files present
     * plus any files that exist in the folder in the remote repository.
     *
     * @param dir folder to list
     * @return
     */
    public File [] listFiles(File dir) {
        Set<File> files = getScannedFiles(dir, null).keySet();
        return files.toArray(new File[files.size()]);
    }

    /**
     * Check if this context has at least one file with the passed in status
     *
     * @param context context to examine
     * @param includeStatus file status to check for
     * @param cached if set to <code>true</code>, only cached values will be checked otherwise it may call I/O operations
     * @return boolean true if this context contains at least one file with the includeStatus, false otherwise
     */
    public boolean containsFileOfStatus(VCSContext context, int includeStatus, boolean cached){
        Map<File, FileInformation> allFiles = cached ? cacheProvider.getCachedValues() : cacheProvider.getAllModifiedValues();
        if(allFiles == null){
            Mercurial.LOG.log(Level.FINE, "containsFileOfStatus(): allFiles == null"); // NOI18N
            return false;
        }

        Set<File> roots = context.getRootFiles();
        Set<File> exclusions = context.getExclusions();
        boolean bExclusions = exclusions != null && exclusions.size() > 0;
        boolean bContainsFile = false;
        
        for (Map.Entry<File, FileInformation> entry : allFiles.entrySet()) {
            File file = entry.getKey();
            FileInformation info = entry.getValue();
            if ((info.getStatus() & includeStatus) == 0) continue;
            for (File root : roots) {
                if (VersioningSupport.isFlat(root)) {
                    if (file.equals(root) || file.getParentFile().equals(root)) {
                        bContainsFile = true;
                        break;
                    }
                } else {
                    if (Utils.isAncestorOrEqual(root, file)) {
                        File fileRoot = hg.getRepositoryRoot(file);
                        File rootRoot = hg.getRepositoryRoot(root);
                        // Make sure that file is in same repository as root
                        if (rootRoot != null && rootRoot.equals(fileRoot)) {
                            bContainsFile = true;
                            break;
                        }
                    }
                }
            }
            // Check it is not an excluded file
            if (bContainsFile && bExclusions) {
                for (File excluded: exclusions){                    
                    if (!Utils.isAncestorOrEqual(excluded, file)) {
                        return true;
                    }
                }
            } else if (bContainsFile) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>This method returns both folders and files.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(VCSContext context, int includeStatus) {
        Set<File> set = new HashSet<File>();
        Map<File, FileInformation> allFiles = cacheProvider.getAllModifiedValues();
        if(allFiles == null){
            Mercurial.LOG.log(Level.FINE, "FileStatusCache: listFiles(): allFiles == null"); // NOI18N
            return new File[0];
        }

        for (Map.Entry<File, FileInformation> entry : allFiles.entrySet()) {
            File file = entry.getKey();
            FileInformation info = entry.getValue();
            if ((info.getStatus() & includeStatus) == 0) continue;
            Set<File> roots = context.getRootFiles();
            for (File root : roots) {
                if (VersioningSupport.isFlat(root)) {
                    if (file.equals(root) || file.getParentFile().equals(root)) {
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
                    }
                }
            }
        }
        if (context.getExclusions().size() > 0) {
            for (Iterator i = context.getExclusions().iterator(); i.hasNext();) {
                File excluded = (File) i.next();
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
    
    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(File[] roots, int includeStatus) {
        Set<File> set = new HashSet<File>();
        Map<File, FileInformation> allFiles = cacheProvider.getAllModifiedValues();
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
                        set.add(file);
                        break;
                    }
                }
            }
        }
        return set.toArray(new File[set.size()]);
    }
    
    /**
     * Determines the versioning status of a file. This method accesses disk and may block for a long period of time.
     *
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */
    public FileInformation getStatus(File file) {
        if (file.isDirectory() && (HgUtils.isAdministrative(file) || HgUtils.isIgnored(file)))
            return FileStatusCache.FILE_INFORMATION_EXCLUDED_DIRECTORY;
        File dir = file.getParentFile();
        if (dir == null) {
            return FileStatusCache.FILE_INFORMATION_NOTMANAGED; //default for filesystem roots
        }
        Map files = getScannedFiles(dir, null);
        if (files == FileStatusCache.NOT_MANAGED_MAP) return FileStatusCache.FILE_INFORMATION_NOTMANAGED;
        FileInformation fi = (FileInformation) files.get(file);
        if (fi != null) {
            return fi;
        }
        if (!exists(file)) return FileStatusCache.FILE_INFORMATION_UNKNOWN;
        if (file.isDirectory()) {
            return refresh(file, REPOSITORY_STATUS_UNKNOWN);
        } else {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
        }
    }
    
    /**
     * Looks up cached file status.
     *
     * @param file file to check
     * @return give file's status or null if the file's status is not in cache
     */
    @SuppressWarnings("unchecked") // Need to change turbo module to remove warning at source
    public FileInformation getCachedStatus(File file) {
        File parent = file.getParentFile();
        if (parent == null) return FileStatusCache.FILE_INFORMATION_NOTMANAGED_DIRECTORY;

        Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(parent, FILE_STATUS_MAP);
        FileInformation fi = files != null ? files.get(file) : null;
        if( fi != null) return fi;

        if (file.isDirectory()) {
            return FileStatusCache.FILE_INFORMATION_UPTODATE_DIRECTORY;
        }

        return fi;
    }
    
    private FileInformation refresh(File file, FileStatus repositoryStatus, 
            boolean forceChangeEvent) {
        Mercurial.LOG.log(Level.FINE, "refresh(): {0}", file); // NOI18N
        File dir = file.getParentFile();
        if (dir == null) {
            return FileStatusCache.FILE_INFORMATION_NOTMANAGED; //default for filesystem roots
        }
        Map<File, FileInformation> files = getScannedFiles(dir, null); // Has side effect of updating the cache
        if (files == FileStatusCache.NOT_MANAGED_MAP && repositoryStatus == FileStatusCache.REPOSITORY_STATUS_UNKNOWN) return FileStatusCache.FILE_INFORMATION_NOTMANAGED;
        FileInformation current = files.get(file);
        
        FileInformation fi = createFileInformation(file);
        
        if (FileStatusCache.equivalent(fi, current)) {
            if (forceChangeEvent) fireFileStatusChanged(file, current, fi);
            return fi;
        }
        
        // do not include uptodate files into cache, missing directories must be included
        if (current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
            if (forceChangeEvent) fireFileStatusChanged(file, current, fi);
            return fi;
        }
        
        file = FileUtil.normalizeFile(file);
        dir = FileUtil.normalizeFile(dir);
        Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(files);
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
            newFiles.remove(file);
            turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
        } else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
            newFiles.remove(file);
        } else {
            newFiles.put(file, fi);
        }
        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
        
        if (file.isDirectory() && needRecursiveRefresh(fi, current)) {
            File [] content = listFiles(file); // Has side effect of updating the cache
            for (int i = 0; i < content.length; i++) {
                refresh(content[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
        fireFileStatusChanged(file, current, fi);
        return fi;
    }
    
    private FileInformation createFileInformation(File file) {        
        return createFileInformation(file, true);
    }

    private FileInformation createFileInformation(File file, Boolean callStatus) {        
        Mercurial.LOG.log(Level.FINE, "createFileInformation(): {0} {1}", new Object[] {file, callStatus}); // NOI18N
        if (file == null)
            return FILE_INFORMATION_UNKNOWN;
        if (HgUtils.isAdministrative(file))
            return FILE_INFORMATION_EXCLUDED_DIRECTORY; // Excluded

        File rootManagedFolder = hg.getRepositoryRoot(file);
        if (rootManagedFolder == null)
            return FILE_INFORMATION_UNKNOWN; // Avoiding returning NOT_MANAGED dir or file
        
        if (file.isDirectory()) {
            if (HgUtils.isIgnored(file)) {
                return FILE_INFORMATION_EXCLUDED_DIRECTORY; // Excluded
            } else {
                return FILE_INFORMATION_UPTODATE_DIRECTORY; // Managed dir
            }
        }
        
        if (callStatus == false) {
            if (HgUtils.isIgnored(file)) {
                return FILE_INFORMATION_EXCLUDED; // Excluded
            } 
            return null;
        }

        FileInformation fi;
        try {
            fi = HgCommand.getSingleStatus(rootManagedFolder, file.getParent(), file.getName());
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.FINE, "createFileInformation() file: {0} {1}", new Object[] {file.getAbsolutePath(), ex.toString()}); // NOI18N
            return FILE_INFORMATION_UNKNOWN;
        }
        return fi;
        
    }
    
    /**
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output.
     *
     * <p>Note: it's not necessary if you use Subversion.getClient(), it
     * updates the cache automatically using onNotify(). It's not
     * fully reliable for removed files.
     *
     * @param file
     * @param repositoryStatus
     */
    public FileInformation refresh(File file, FileStatus repositoryStatus) {
        return refresh(file, repositoryStatus, false);
    }
 
    @SuppressWarnings("unchecked") // Need to change turbo module to remove warning at source
    public Map<File, FileInformation> getScannedFiles(File dir, Map<File, FileInformation> interestingFiles) {
        Map<File, FileInformation> files;

        files = (Map<File, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (isNotManagedByDefault(dir)) {
            if (interestingFiles == null) return FileStatusCache.NOT_MANAGED_MAP;
        }

        dir = FileUtil.normalizeFile(dir);
        files = scanFolder(dir, interestingFiles);
        turbo.writeEntry(dir, FILE_STATUS_MAP, files.size() == 0 ? null : files);
        //if(interestingFiles != null) {
        for (Map.Entry<File, FileInformation> entry : files.entrySet()) {
            File file = entry.getKey();
            FileInformation info = entry.getValue();
            if ((info.getStatus() & (FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) != 0) {
                fireFileStatusChanged(file, null, info);
            }
        }
        //}
        return files;
    }

    public void refreshFileStatus(File file, FileInformation fi, Map<File, FileInformation> interestingFiles ) {
        refreshFileStatus(file, fi, interestingFiles, false);
    }

    public void refreshFileStatus(File file, FileInformation fi, Map<File, FileInformation> interestingFiles, boolean alwaysFireEvent) {
        if(file == null || fi == null) return;
        File dir = file.getParentFile();
        if(dir == null) return;

        Map<File, FileInformation> files = getScannedFiles(dir, interestingFiles);

        if (files == null || files == FileStatusCache.NOT_MANAGED_MAP ) return;     
        FileInformation current = files.get(file);  
        if (FileStatusCache.equivalent(fi, current))  {
            if (FileStatusCache.equivalent(FILE_INFORMATION_NEWLOCALLY, fi)) {
                if (HgUtils.isIgnored(file)) {
                    Mercurial.LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is NotSharable", file.getAbsolutePath()); // NOI18N
                    fi = FILE_INFORMATION_EXCLUDED;
                } else {
                    if (alwaysFireEvent) {
                        fireFileStatusChanged(file, null, fi);
                    }
                    return;
                }
            } else if (!FileStatusCache.equivalent(FILE_INFORMATION_REMOVEDLOCALLY, fi)) {
                if (alwaysFireEvent) {
                    fireFileStatusChanged(file, null, fi);
                }
                return;
            }
        }
        if (FileStatusCache.equivalent(FILE_INFORMATION_NEWLOCALLY, fi)) {
            if (FileStatusCache.equivalent(FILE_INFORMATION_EXCLUDED, current)) {
                Mercurial.LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is Excluded", file.getAbsolutePath()); // NOI18N
                if (alwaysFireEvent) {
                    fireFileStatusChanged(file, null, fi);
                }
                return;
            } else if (current == null) {
                if (HgUtils.isIgnored(file)) {
                    Mercurial.LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but current is null and is not NotSharable", file.getAbsolutePath()); // NOI18N
                    fi = FILE_INFORMATION_EXCLUDED;
                 }
             }
        }
        file = FileUtil.normalizeFile(file);
        dir = FileUtil.normalizeFile(dir);
        Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(files);
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
            newFiles.remove(file);
            turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
        } else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
            newFiles.remove(file);
        } else {
            newFiles.put(file, fi);
        }
        assert newFiles.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);

        fireFileStatusChanged(file, current, fi);

        return;
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
        return e1 == e2 || e1 == null || e2 == null || FileStatusCache.equal(e1, e2);
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
    
    private boolean needRecursiveRefresh(FileInformation fi, FileInformation current) {
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED ||
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) return true;
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED ||
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) return true;
        return false;
    }
    
    /**
     * Refreshes status of the specified file or a specified directory. 
     *
     * @param file
     */
    @SuppressWarnings("unchecked") // Need to change turbo module to remove warning at source
    public void refreshAll(File root) {
        if (root.isDirectory()) {
            File repository = Mercurial.getInstance().getRepositoryRoot(root);
            if (repository == null) {
                return;
            }
            Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(root, FILE_STATUS_MAP);
            Map<File, FileInformation> interestingFiles;
            try {
                interestingFiles = HgCommand.getInterestingStatus(repository, root);
                for (Map.Entry<File, FileInformation> entry : interestingFiles.entrySet()) {
                    File file = entry.getKey();
                    FileInformation fi = entry.getValue();
                    Mercurial.LOG.log(Level.FINE, "refreshAll() file: {0} {1} ", new Object[] {file.getAbsolutePath(), fi}); // NOI18N
                    refreshFileStatus(file, fi, interestingFiles);
                }
                if (files != null) {
                    for (Map.Entry<File, FileInformation> entry : files.entrySet()) {
                        File file = entry.getKey();
                        if ((file.isFile() || !file.exists()) && !interestingFiles.containsKey(file)) {
                            // A file was in cache but is now up to date
                            Mercurial.LOG.log(Level.FINE, "refreshAll() uninteresting file: {0} {1}", new Object[] {file, entry.getValue()}); // NOI18N
                            refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        }
                    }
                }
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.FINE, "refreshAll() file: {0} {1} { 2} ", new Object[] {repository.getAbsolutePath(), root.getAbsolutePath(), ex.toString()}); // NOI18N
            }
        } else {
            refresh(root, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }

    /**
     * Refreshes all files under given roots in the cache.
     * @param rootFiles root files sorted under their's repository roots
     */
    void refreshAllRoots (Map<File, File> rootFiles) {
        for (Map.Entry<File, File> refreshEntry : rootFiles.entrySet()) {
            File repository = refreshEntry.getKey();
            File root = refreshEntry.getValue();
            if (Mercurial.LOG.isLoggable(Level.FINE)) {
                Mercurial.LOG.log(Level.FINE, "refreshAllRoots() root: {0}, repositoryRoot: {1} ", new Object[] {root.getAbsolutePath(), repository.getAbsolutePath()}); // NOI18N
            }
            Map<File, FileInformation> files = getAllModifiedFiles();
            Map<File, FileInformation> interestingFiles;
            try {
                // find all files with not up-to-date or ignored status
                interestingFiles = HgCommand.getInterestingStatus(repository, root);
                for (Map.Entry<File, FileInformation> interestingEntry : interestingFiles.entrySet()) {
                    // put the file's FI into the cache
                    File file = interestingEntry.getKey();
                    FileInformation fi = interestingEntry.getValue();
                    Mercurial.LOG.log(Level.FINE, "refreshAllRoots() file: {0} {1} ", new Object[] {file.getAbsolutePath(), fi}); // NOI18N
                    refreshFileStatus(file, fi, interestingFiles);
                }
                // clean all files originally in the cache but now beign up-to-date or obsolete (as ignored && deleted)
                for (Map.Entry<File, FileInformation> entry : files.entrySet()) {
                    File file = entry.getKey();
                    FileInformation fi = entry.getValue();
                    boolean exists = file.exists();
                    if (!interestingFiles.containsKey(file)             // file no longer has an interesting status
                            && Utils.isAncestorOrEqual(root, file)      // file is under the examined root
                            && ((fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0 && !exists || // file was ignored and is now deleted
                            (fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0 && (!exists || file.isFile()))) { // file is now up-to-date
                        Mercurial.LOG.log(Level.FINE, "refreshAllRoots() uninteresting file: {0} {1}", new Object[]{file, fi}); // NOI18N
                        // TODO better way to detect conflicts
                        if(HgCommand.existsConflictFile(file.getAbsolutePath())) {
                            refreshFileStatus(file, FileStatusCache.FILE_INFORMATION_CONFLICT, interestingFiles); // set the files status to 'IN CONFLICT'
                        } else {
                            refreshFileStatus(file, FileStatusCache.FILE_INFORMATION_UNKNOWN, interestingFiles); // remove the file from cache
                        }
                    }
                }
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.FINE, "refreshAll() file: {0} {1} {2} ", new Object[] {repository.getAbsolutePath(), root.getAbsolutePath(), ex.toString()}); // NOI18N
            }
        }
    }
    
    /**
     * Refreshes status of the specified file or all files inside the 
     * specified directory. 
     *
     * @param file
     */
    @SuppressWarnings("unchecked") // Need to change turbo module to remove warning at source
    public void refreshCached(File root) {
        if (root.isDirectory()) {
            File repository = Mercurial.getInstance().getRepositoryRoot(root);
            if (repository == null) {
                return;
            }
            File roots[] = new File[1];
            roots[0] = root;
            File [] files = listFiles(roots, ~0);
            if (files.length == 0) {
                return;
            } 
            Map<File, FileInformation> allFiles;
            try {
                allFiles = HgCommand.getAllStatus(repository, root);
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    FileInformation fi = allFiles.get(file);
                    if (fi == null) {
                        // We have a file in the cache which seems to have disappeared
                        // so remove it from the cache and fireFileStatusChanged
                        File parent = file.getParentFile();
                        
                        Map<File, FileInformation> oldFiles = (Map<File, FileInformation>) turbo.readEntry(parent, FILE_STATUS_MAP);
                        if(oldFiles != null) {
                            Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(oldFiles);
                            newFiles.remove(file);
                            turbo.writeEntry(parent, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
                        } else {
                            turbo.writeEntry(parent, FILE_STATUS_MAP, null);
                        }
                        fi = oldFiles != null ? oldFiles.get(file) : null;
                        fireFileStatusChanged(file, fi, FILE_INFORMATION_UNKNOWN);
                    } else {
                        refreshFileStatus(file, fi, null);
                    }
                }
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.FINE, "refreshCached() file: {0} {1} { 2} ", new Object[] {repository.getAbsolutePath(), root.getAbsolutePath(), ex.toString()}); // NOI18N
            }
        } else {
            refresh(root, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }

    /**
     * Refreshes status of all files inside given context. 
     *
     * @param ctx context to refresh
     */
    public void refreshCached(VCSContext ctx) {
        
        for (File root : ctx.getRootFiles()) {
            refreshCached(root);
        }
    }
    
    public void addToCache(Set<File> files) {
        FileInformation fi = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, null, false);
        HashMap <File, Map<File, FileInformation>> dirMap = new HashMap<File, Map<File, FileInformation>> (files.size());

        for (File file : files) {
            File parent = file.getParentFile();
            file = FileUtil.normalizeFile(file);
            parent = FileUtil.normalizeFile(parent);
            Map<File, FileInformation> currentDirMap = dirMap.get(parent);
            if (currentDirMap == null) {
                // 20 is a guess at number of files in a directory
                currentDirMap = new HashMap<File, FileInformation> (20);
                dirMap.put(parent, currentDirMap);
            }
            currentDirMap.put(file, fi);
        } 
        for (Map.Entry<File, Map<File, FileInformation>> entry : dirMap.entrySet()) {
            File dir = FileUtil.normalizeFile(entry.getKey());
            Map<File, FileInformation> currentDirMap = entry.getValue();
            turbo.writeEntry(dir, FILE_STATUS_MAP, currentDirMap);
        }
    }
    // --- Package private contract ------------------------------------------
    
    Map<File, FileInformation>  getAllModifiedFiles() {
        return cacheProvider.getAllModifiedValues();
    }

    /**
     * Returns only a cached map of modified files, will not access I/O.
     * @param changed out parameter. If the cached map is not up-of-date, changed[0] will be set to true, otherwise false
     * @return
     */
    Map<File, FileInformation> getAllModifiedFilesCached (final boolean changed[]) {
        changed[0] = cacheProvider.modifiedFilesChanged();
        return cacheProvider.getCachedValues();
    }
    
    // --- Private methods ---------------------------------------------------
    
    private boolean isNotManagedByDefault(File dir) {
        return !dir.exists();
    }
    
    /**
     * Scans all files in the given folder, computes and stores their CVS status.
     *
     * @param dir directory to scan
     * @return Map map to be included in the status cache (File => FileInformation)
     */
    private Map<File, FileInformation> scanFolder(File dir, Map<File, FileInformation> interestingFiles) {
        File [] files = dir.listFiles();
        if (files == null) {
            if (interestingFiles == null) {
                files = new File[0];
            } else {
                // filter only interesting files that belong directly to dir
                // by introducing refreshAllRoots interestingFiles may contain all files under repository root recursively
                // and we surely don't want all of them to be associated with dir
                Set<File> fileSet = new HashSet<File>(15);
                for (File file : interestingFiles.keySet()) {
                    if (dir.equals(file.getParentFile())) {
                        fileSet.add(file);
                    }
                }
                files = fileSet.toArray(new File[fileSet.size()]);
            }
        }
        Map<File, FileInformation> folderFiles = new HashMap<File, FileInformation>(files.length);
        
        Mercurial.LOG.log(Level.FINE, "scanFolder(): {0}", dir); // NOI18N
        if (HgUtils.isAdministrative(dir)) {
            folderFiles.put(dir, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
            return folderFiles;
        }
        
        File rootManagedFolder = hg.getRepositoryRoot(dir);
        if (rootManagedFolder == null){
            // Only interested in looking for Hg managed dirs
            for (File file : files) {
                if (file.isDirectory() && hg.getRepositoryRoot(file) != null){
                    if (HgUtils.isAdministrative(file) || HgUtils.isIgnored(file)){
                        Mercurial.LOG.log(Level.FINE, "scanFolder NotMng Ignored Dir {0}: exclude SubDir: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                        folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                    }else{
                        Mercurial.LOG.log(Level.FINE, "scanFolder NotMng Dir {0}: up to date Dir: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                        folderFiles.put(file, FILE_INFORMATION_UPTODATE_DIRECTORY);
                    }
                }
                // Do NOT put any unmanaged dir's (FILE_INFORMATION_NOTMANAGED_DIRECTORY) or 
                // files (FILE_INFORMATION_NOTMANAGED) into the folderFiles
            }
            return folderFiles;
        }
                
        boolean bInIgnoredDir = HgUtils.isIgnored(dir);
        if(bInIgnoredDir){
            for (File file : files) {
                if (HgUtils.isPartOfMercurialMetadata(file)) continue;
                
                if (file.isDirectory()) {
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                    Mercurial.LOG.log(Level.FINE, "scanFolder Mng Ignored Dir {0}: exclude SubDir: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                } else {
                    Mercurial.LOG.log(Level.FINE, "scanFolder Mng Ignored Dir {0}: exclude File: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED);
                }
            }
            return folderFiles;
        }
        
        if(!Mercurial.getInstance().isAvailable())
            return folderFiles;
        
        if(interestingFiles == null){
            try {
                long startTime = 0;
                if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
                    startTime = System.currentTimeMillis();
                    Mercurial.STATUS_LOG.fine("scanFolder: start for " + dir.getAbsolutePath());
                }
                interestingFiles = HgCommand.getInterestingStatus(rootManagedFolder, dir);
                if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
                    Mercurial.STATUS_LOG.fine("scanFolder: finishes for " + dir.getAbsolutePath() + " after " + (System.currentTimeMillis() - startTime));
                }
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.FINE, "scanFolder() getInterestingStatus Exception: dir: {0} {1}", new Object[]{dir.getAbsolutePath(), ex.toString()}); // NOI18N
                return folderFiles;
            }
        }
                
        if (interestingFiles == null) return folderFiles;
        
        for (File file : files) {
            if (HgUtils.isPartOfMercurialMetadata(file)) continue;
            
            if (file.isDirectory()) {
                if (HgUtils.isAdministrative(file) || HgUtils.isIgnored(file)) {
                    Mercurial.LOG.log(Level.FINE, "scanFolder Mng Dir {0}: exclude Dir: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                } else {
                    Mercurial.LOG.log(Level.FINE, "scanFolder Mng Dir {0}: up to date Dir: {1}", // NOI18N
                            new Object[]{dir.getAbsolutePath(), file.getName()});
                    folderFiles.put(file, FILE_INFORMATION_UPTODATE_DIRECTORY);
                }
            } else {
                FileInformation fi = interestingFiles.get(file);
                if (fi == null) {
                    // We have removed -i from HgCommand.getInterestingFiles
                    // so we might have a file we should be ignoring
                    fi = createFileInformation(file, false);
                }
                if (fi != null && fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE)
                    folderFiles.put(file, fi);
            }
        }
        return folderFiles;
    }

    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.removePropertyChangeListener(listener);
    }
    
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        listenerSupport.firePropertyChange(PROP_FILE_STATUS_CHANGED, null, new ChangedEvent(file, oldInfo, newInfo));
    }
    
    public void notifyFileChanged(File file) {
        fireFileStatusChanged(file, null, FILE_INFORMATION_UPTODATE);
    }
    
    private static final class NotManagedMap extends AbstractMap<File, FileInformation> {
        public Set<Entry<File, FileInformation>> entrySet() {
            return Collections.emptySet();
        }
    }
    
    public static class ChangedEvent {
        
        private File file;
        private FileInformation oldInfo;
        private FileInformation newInfo;
        
        public ChangedEvent(File file, FileInformation oldInfo, FileInformation newInfo) {
            this.file = file;
            this.oldInfo = oldInfo;
            this.newInfo = newInfo;
        }
        
        public File getFile() {
            return file;
        }
        
        public FileInformation getOldInfo() {
            return oldInfo;
        }
        
        public FileInformation getNewInfo() {
            return newInfo;
        }
    }
}
