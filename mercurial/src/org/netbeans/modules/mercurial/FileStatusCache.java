/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial;

import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.mercurial.Mercurial;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.mercurial.util.HgCommand;
import java.util.logging.Level;
import org.netbeans.api.queries.SharabilityQuery;


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
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    public static final FileInformation FILE_INFORMATION_CONFLICT = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, false);
    
    private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);
    
    /**
     * Caches status of files in memory ant on disk
     */
    private final Turbo turbo;
    
    private final String FILE_STATUS_MAP = DiskMapTurboProvider.ATTR_STATUS_MAP;
    
    private DiskMapTurboProvider    cacheProvider;
    
    private Mercurial     hg;
    
    private Set<FileSystem> filesystemsToRefresh;
    
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
        Set<File> files = getScannedFiles(dir).keySet();
        return files.toArray(new File[files.size()]);
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
        Map allFiles = cacheProvider.getAllModifiedValues();
        if(allFiles == null){
            Mercurial.LOG.log(Level.FINE, "FileStatusCache: listFiles(): allFiles == null"); // NOI18N
            return new File[0];
        }

        for (Iterator i = allFiles.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) allFiles.get(file);
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
                        set.add(file);
                        break;
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
        Map allFiles = cacheProvider.getAllModifiedValues();
        for (Iterator i = allFiles.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) allFiles.get(file);
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
        if (hg.isAdministrative(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE)
            return FileStatusCache.FILE_INFORMATION_EXCLUDED_DIRECTORY;
        File dir = file.getParentFile();
        if (dir == null) {
            return FileStatusCache.FILE_INFORMATION_NOTMANAGED; //default for filesystem roots
        }
        Map files = getScannedFiles(dir);
        if (files == FileStatusCache.NOT_MANAGED_MAP) return FileStatusCache.FILE_INFORMATION_NOTMANAGED;
        FileInformation fi = (FileInformation) files.get(file);
        if (fi != null) {
            return fi;
        }
        if (!exists(file)) return FileStatusCache.FILE_INFORMATION_UNKNOWN;
        if (file.isDirectory()) {
            return refresh(file, REPOSITORY_STATUS_UNKNOWN);
        } else {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE
, false);
        }
    }
    
    /**
     * Looks up cached file status.
     *
     * @param file file to check
     * @return give file's status or null if the file's status is not in cache
     */
    FileInformation getCachedStatus(File file) {
        file = file.getParentFile();
        if (file == null) return FileStatusCache.FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(file, FILE_STATUS_MAP);
        return files != null ? files.get(file) : null;
    }
    
    private FileInformation refresh(File file, FileStatus repositoryStatus, 
            boolean forceChangeEvent) {
        File dir = file.getParentFile();
        if (dir == null) {
            return FileStatusCache.FILE_INFORMATION_NOTMANAGED; //default for filesystem roots
        }
        Map<File, FileInformation> files = getScannedFiles(dir);
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
        assert newFiles.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
        
        if (file.isDirectory() && needRecursiveRefresh(fi, current)) {
            File [] content = listFiles(file);
            for (int i = 0; i < content.length; i++) {
                refresh(content[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
        fireFileStatusChanged(file, current, fi);
        return fi;
    }
    
    private FileInformation createFileInformation(File file) {        
        Mercurial.LOG.log(Level.FINE, "createFileInformation(): {0}", file); // NOI18N
        if (file == null)
            return FILE_INFORMATION_UNKNOWN;
        if (hg.isAdministrative(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE)
            return FILE_INFORMATION_EXCLUDED_DIRECTORY; // Excluded dir

        File rootManagedFolder = hg.getTopmostManagedParent(file);        
        if (rootManagedFolder == null)
            return FILE_INFORMATION_UNKNOWN; // Avoiding returning NOT_MANAGED dir or file
        
        if (file.isDirectory())
            return FILE_INFORMATION_UPTODATE_DIRECTORY; // Managed dir
        
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
    
    public FileInformation refreshForce(File file, FileStatus repositoryStatus) {
        return refresh(file, repositoryStatus, true);
    }
    
    /**
     * Refreshes the status of the file given the FileInformation
     *
     * @param file - file whose status is to be refreshed
     * @param fi - file information to refresh too
     */
    public void refreshFileStatus(File file, FileInformation fi) {
        if(file == null || fi == null) return;
        File dir = file.getParentFile();
        if(dir == null) return;

        Map<File, FileInformation> files = getScannedFiles(dir);
        if (files == null || files == FileStatusCache.NOT_MANAGED_MAP ) return;     
        
        FileInformation current = files.get(file);  
        if (FileStatusCache.equivalent(fi, current)) return;
        
        files.put(file, fi);

        dir = FileUtil.normalizeFile(dir);
        assert files.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, files);

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
     * Refreshes information about a given file or directory ONLY if its status is already cached. The
     * only exception are non-existing files (new-in-repository) whose statuses are cached in all cases.
     *
     * @param file
     * @param repositoryStatus
     */
    public void refreshCached(File file, FileStatus repositoryStatus) {
        refresh(file, repositoryStatus);
    }
    
    /**
     * Refreshes status of all files inside given context. Files that have some remote status, eg. REMOTELY_ADDED
     * are brought back to UPTODATE.
     *
     * @param ctx context to refresh
     */
    public void refreshCached(VCSContext ctx) {
        
        File [] files = listFiles(ctx, ~0);
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
    
    // --- Package private contract ------------------------------------------
    
    Map<File, FileInformation>  getAllModifiedFiles() {
        return cacheProvider.getAllModifiedValues();
    }
    
    /**
     * Refreshes given directory and all subdirectories.
     *
     * @param dir directory to refresh
     */
    void directoryContentChanged(File dir) {
        Map originalFiles = (Map) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (originalFiles != null) {
            for (Iterator i = originalFiles.keySet().iterator(); i.hasNext();) {
                File file = (File) i.next();
                refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }
    
    /**
     * Cleans up the cache by removing or correcting entries that are no longer valid or correct.
     */
    void cleanUp() {
        Map files = cacheProvider.getAllModifiedValues();
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) files.get(file);
            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                // remove entries that were excluded but no longer exist
                // cannot simply call refresh on excluded files because of 'excluded on server' status
                if (!exists(file)) {
                    refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
            }
        }
    }
    
    // --- Private methods ---------------------------------------------------
    
    private Map<File, FileInformation> getScannedFiles(File dir) {
        Map<File, FileInformation> files;
        
        files = (Map<File, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (isNotManagedByDefault(dir)) {
            return FileStatusCache.NOT_MANAGED_MAP;
        }
        
        // scan and populate cache with results
        
        dir = FileUtil.normalizeFile(dir);
        files = scanFolder(dir);
        assert files.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, files);
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = files.get(file);
            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                fireFileStatusChanged(file, null, info);
            }
        }
        return files;
    }
    
    private boolean isNotManagedByDefault(File dir) {
        return !dir.exists();
    }
    
    /**
     * Scans all files in the given folder, computes and stores their CVS status.
     *
     * @param dir directory to scan
     * @return Map map to be included in the status cache (File => FileInformation)
     */
    private Map<File, FileInformation> scanFolder(File dir) {
        File [] files = dir.listFiles();
        if (files == null) files = new File[0];
        Map<File, FileInformation> folderFiles = new HashMap<File, FileInformation>(files.length);
        
        Mercurial.LOG.log(Level.FINE, "scanFolder(): {0}", dir); // NOI18N
        if (hg.isAdministrative(dir)) {
            folderFiles.put(dir, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
            return folderFiles;
        }
        
        File rootManagedFolder = hg.getTopmostManagedParent(dir);
        if (rootManagedFolder == null){
            // Only interested in looking for Hg managed dirs
            for (File file : files) {
                if (file.isDirectory() && hg.getTopmostManagedParent(file) != null){
                    if (hg.isAdministrative(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE)
                        folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                    else
                        folderFiles.put(file, FILE_INFORMATION_UPTODATE_DIRECTORY);
                }
                // Do NOT put any unmanaged dir's (FILE_INFORMATION_NOTMANAGED_DIRECTORY) or 
                // files (FILE_INFORMATION_NOTMANAGED) into the folderFiles
            }
            return folderFiles;
        }

        Map<File, FileInformation> interestingFiles;
        // Map<File, FileInformation> removedOrDeletedFiles;
        try {
            interestingFiles = HgCommand.getInterestingStatus(rootManagedFolder, dir);
            // removedOrDeletedFiles = HgCommand.getRemovedDeletedStatus(rootManagedFolder,dir);
            //interestingFiles = HgCommand.getAllInterestingStatus(rootManagedFolder);
            //removedOrDeletedFiles = HgCommand.getAllRemovedDeletedStatus(rootManagedFolder);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.FINE, "scanFolder() dir: {0} {1}", new Object[] {dir.getAbsolutePath(), ex.toString()}); // NOI18N
            return folderFiles;
        }
                
        // TODO: Deal with removed and deleted files the repository is tracking but are not on the filesystem
        // Code below does work, but seem to blow up further down the line with deserilaisation problems
        // presuambly the cache is trying to deserialise the files I've just added  - need to figure out how
        // stop it doing so.
        /*
         if (!removedOrDeletedFiles.isEmpty()){
            for (Iterator i = removedOrDeletedFiles.keySet().iterator(); i.hasNext();) {
                File file = (File) i.next();
                FileInformation fi = removedOrDeletedFiles.get(file);
                if (fi != null && fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE)
                    folderFiles.put(file, fi);
            }
        }
        */
        
        if (interestingFiles.isEmpty()) return folderFiles;

        boolean bInIgnoredDir = SharabilityQuery.getSharability(dir) == SharabilityQuery.NOT_SHARABLE;
        if(bInIgnoredDir){
            for (File file : files) {
                if (HgUtils.isPartOfMercurialMetadata(file)) continue;
                
                if (file.isDirectory()) {
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                } else {
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED);
                }
            }
            return folderFiles;
        }
        
        for (File file : files) {
            if (HgUtils.isPartOfMercurialMetadata(file)) continue;
            
            if (file.isDirectory()) {
                if (hg.isAdministrative(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE)
                    folderFiles.put(file, FILE_INFORMATION_EXCLUDED_DIRECTORY); // Excluded dir
                else
                    folderFiles.put(file, FILE_INFORMATION_UPTODATE_DIRECTORY);
            } else {
                FileInformation fi = interestingFiles.get(file);
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
    
    public void refreshDirtyFileSystems() {
        Set<FileSystem> filesystems = getFilesystemsToRefresh();
        FileSystem[]  filesystemsToRefresh = new FileSystem[filesystems.size()];
        synchronized (filesystems) {
            filesystemsToRefresh = filesystems.toArray(new FileSystem[filesystems.size()]);
            filesystems.clear();
        }
        for (int i = 0; i < filesystemsToRefresh.length; i++) {
            // don't call refresh() in synchronized (filesystems). It may lead to a deadlock.
            filesystemsToRefresh[i].refresh(true);
        }
    }
    
    private Set<FileSystem> getFilesystemsToRefresh() {
        if(filesystemsToRefresh == null) {
            filesystemsToRefresh = new HashSet<FileSystem>();
        }
        return filesystemsToRefresh;
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
