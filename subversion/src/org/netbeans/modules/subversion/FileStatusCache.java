/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.subversion;

import java.io.File;
import java.util.regex.*;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import java.util.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileSystem;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.ISVNStatus;

/**
 * Central part of CVS status management, deduces and caches statuses of files under version control.
 * 
 * @author Maros Sandor
 */
public class FileStatusCache implements ISVNNotifyListener {

    /**
     * Indicates that status of a file changed and listeners SHOULD check new status 
     * values if they are interested in this file.
     * First parameter: File whose status changes
     * Second parameter: old FileInformation object, may be null
     * Third parameter: new FileInformation object
     */
    public static final Object EVENT_FILE_STATUS_CHANGED = new Object();

    /**
     * A special map saying that no file inside the folder is managed.
     */ 
    private static final Map<File, FileInformation> NOT_MANAGED_MAP = new NotManagedMap();
       
    public static final ISVNStatus REPOSITORY_STATUS_UNKNOWN  = null;

    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects 
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    /**
     * Auxiliary conflict file siblings
     * After update: *.r#, *.mine
     * After merge: *.working, *.merge-right.r#, *.metge-left.r#
     */
    private static final Pattern auxConflictPattern = Pattern.compile("(.*?)\\.((r\\d+)|(mine)|" + // NOI18N
        "(working)|(merge-right\\.r\\d+)|((merge-left.r\\d+)))$"); // NOI18N

    /*
     * Holds three kinds of information: what folders we have scanned, what files we have found
     * and what statuses of these files are.
     * If a directory is not found as a key in the map, we have not scanned it yet.
     * If it has been scanned, it maps to a Set of files that were found somehow out of sync with the
     * repository (have any other status then up-to-date). In case all files are up-to-date, it maps
     * to Collections.EMPTY_MAP. Entries in this map are created as directories are scanne, are never removed and
     * are updated by the refresh method.
     */

    private final Turbo     turbo;
    
    /**
     * Identifies attribute that holds information about all non STATUS_VERSIONED_UPTODATE files.
     *
     * <p>Key type: File identifying a folder
     * <p>Value type: Map&lt;File, FileInformation>
     */
    private final String FILE_STATUS_MAP = DiskMapTurboProvider.ATTR_STATUS_MAP;

    private DiskMapTurboProvider    cacheProvider;
    
    private Subversion     svn;
    
    private Set<FileSystem> filesystemsToRefresh;
    
    private RequestProcessor.Task refreshFilesystemsTask;    
    
    FileStatusCache() {
        this.svn = Subversion.getInstance();
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
     * plus any files that exist in the folder in the remote repository. It
     * returns all folders, including CVS folders.
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
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(Context context, int includeStatus) {
        Set<File> set = new HashSet<File>();
        Map allFiles = cacheProvider.getAllModifiedValues();
        for (Iterator i = allFiles.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) allFiles.get(file);
            if ((info.getStatus() & includeStatus) == 0) continue;
            File [] roots = context.getRootFiles();
            for (int j = 0; j < roots.length; j++) {
                File root = roots[j];
                if (VersioningSupport.isFlat(root)) {
                    if (file.equals(root) || file.getParentFile().equals(root)) {
                        set.add(file);
                        break;
                    }
                } else {
                    if (SvnUtils.isParentOrEqual(root, file)) {
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
                    if (SvnUtils.isParentOrEqual(excluded, file)) {
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
                    if (SvnUtils.isParentOrEqual(root, file)) {
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
        if (svn.isAdministrative(file)) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        File dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) return FILE_INFORMATION_NOTMANAGED;
        FileInformation fi = (FileInformation) files.get(file);
        if (fi != null) {
            return fi;            
        }
        if (!exists(file)) return FILE_INFORMATION_UNKNOWN;
        if (file.isDirectory()) {
            return refresh(file, REPOSITORY_STATUS_UNKNOWN);
        } else {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
        }
    }

    /**
     * Looks up cacnehed file status.
     * 
     * @param file file to check
     * @return give file's status or null if the file's status is not in cache
     */ 
    FileInformation getCachedStatus(File file) {
        File parent = file.getParentFile();
        if (parent == null) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(parent, FILE_STATUS_MAP);
        return files != null ? files.get(file) : null;
    }

    private FileInformation refresh(File file, ISVNStatus repositoryStatus, boolean forceChangeEvent) {
        File dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map<File, FileInformation> files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP && repositoryStatus == REPOSITORY_STATUS_UNKNOWN) return FILE_INFORMATION_NOTMANAGED;
        FileInformation current = files.get(file);
        
        ISVNStatus status = null;
        try {
            SvnClient client = Subversion.getInstance().getClient(false);
            status = client.getSingleStatus(file);
            if (status != null && SVNStatusKind.UNVERSIONED.equals(status.getTextStatus())) {
                status = null;
            }
        } catch (SVNClientException e) {
            // svnClientAdapter does not return SVNStatusKind.UNVERSIONED!!!
            // unversioned resource is expected getSingleStatus()
            // does not return SVNStatusKind.UNVERSIONED but throws exception instead            
            // instead of throwing exception
            if (SvnClientExceptionHandler.isUnversionedResource(e.getMessage()) == false) {
                // missing or damaged entries
                // or ignored file
                SvnClientExceptionHandler.notifyException(e, false, false);
            }
        }
        FileInformation fi = createFileInformation(file, status, repositoryStatus);
        if (equivalent(fi, current)) {
            if (forceChangeEvent) fireFileStatusChanged(file, current, fi);
            return fi;
        }
        // do not include uptodate files into cache, missing directories must be included
        if (current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
            if (forceChangeEvent) fireFileStatusChanged(file, current, fi);
            return fi;
        }

        File [] content = null;                
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN && 
           current != null && current.isDirectory() && ( current.getStatus() == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || 
                                                         current.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY )) 
        {
            // - if the file was deleted then all it's children have to be refreshed.
            // - we have to list the children before the turbo.writeEntry() call 
            //   as that unfortunatelly tends to purge them from the cache 
            content = listFiles(new File[] {file}, ~0);
        } 
        
        file = FileUtil.normalizeFile(file);
        dir = FileUtil.normalizeFile(dir);
        Map<File, FileInformation> newFiles = new HashMap<File, FileInformation>(files);
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
            newFiles.remove(file);
            turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
        }
        else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
            newFiles.remove(file);
        } else {
            newFiles.put(file, fi);
        }
        assert newFiles.containsKey(dir) == false;
        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);
          
        if(content == null && file.isDirectory() && needRecursiveRefresh(fi, current)) {
            content = listFiles(file);
        }
        if ( content != null ) {
            for (int i = 0; i < content.length; i++) {
                refresh(content[i], REPOSITORY_STATUS_UNKNOWN);
            }
        }
        
        fireFileStatusChanged(file, current, fi);
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
    public FileInformation refresh(File file, ISVNStatus repositoryStatus) {
        return refresh(file, repositoryStatus, false);
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
        
        ISVNStatus e1 = main.getEntry(null);
        ISVNStatus e2 = other.getEntry(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     * 
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */ 
    private static boolean equal(ISVNStatus e1, ISVNStatus e2) {
        long r1 = -1;
        if (e1 != null) {
            SVNRevision r = e1.getRevision();
            r1 = r != null ? e1.getRevision().getNumber() : r1;
        }

        long r2 = -2;
        if (e2 != null) {
            SVNRevision r = e1.getRevision();
            r2 = r != null ? e2.getRevision().getNumber() : r2;
        }
        
        if ( r1 != r2 ) {
            return false;
        }
        return e1.getUrl() == e2.getUrl() || 
                e1.getUrl() != null && e1.getUrl().equals(e2.getUrl());
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
    public void refreshCached(File file, ISVNStatus repositoryStatus) {
        refresh(file, repositoryStatus);
    }

    /**
     * Refreshes status of all files inside given context. Files that have some remote status, eg. REMOTELY_ADDED
     * are brought back to UPTODATE.
     * 
     * @param ctx context to refresh
     */ 
    public void refreshCached(Context ctx) {
        
        File [] files = listFiles(ctx, ~0);
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            refreshCached(file, REPOSITORY_STATUS_UNKNOWN);
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
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
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
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                // remove entries that were excluded but no longer exist
                // cannot simply call refresh on excluded files because of 'excluded on server' status
                if (!exists(file)) {
                    refresh(file, REPOSITORY_STATUS_UNKNOWN);
                }
            }
        }
    }
        
    // --- Private methods ---------------------------------------------------

    private Map<File, FileInformation> getScannedFiles(File dir) {
        Map<File, FileInformation> files;

        // there are 2nd level nested admin dirs (.svn/tmp, .svn/prop-base, ...)

        if (svn.isAdministrative(dir)) {
            return NOT_MANAGED_MAP;
        }
        File parent = dir.getParentFile();
        if (parent != null && svn.isAdministrative(parent)) {
            return NOT_MANAGED_MAP;
        }

        files = (Map<File, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (isNotManagedByDefault(dir)) {
            return NOT_MANAGED_MAP; 
        }

        // scan and populate cache with results

        dir = FileUtil.normalizeFile(dir);
        files = scanFolder(dir);    // must not execute while holding the lock, it may take long to execute
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

        ISVNStatus [] entries = null;
        try {
            SvnClient client = Subversion.getInstance().getClient(true); 
            if (Subversion.getInstance().isManaged(dir)) {
                entries = client.getStatus(dir, false, false); 
            }
        } catch (SVNClientException e) {
            // no or damaged entries
            //ErrorManager.getDefault().annotate(e, "Can not status " + dir.getAbsolutePath() + ", guessing it...");  // NOI18N
            SvnClientExceptionHandler.notifyException(e, false, false);
        }

        if (entries == null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (svn.isAdministrative(file)) continue;
                FileInformation fi = createFileInformation(file, null, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }
        } else {
            Set<File> localFiles = new HashSet<File>(Arrays.asList(files));
            for (int i = 0; i < entries.length; i++) {
                ISVNStatus entry = entries[i];
                File file = new File(entry.getPath());
                if (file.equals(dir)) {
                    continue;
                }
                localFiles.remove(file);
                if (svn.isAdministrative(file)) {
                    continue;
                }
                FileInformation fi = createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }

            Iterator it = localFiles.iterator();
            while (it.hasNext()) {
                File localFile = (File) it.next();
                FileInformation fi = createFileInformation(localFile, null, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(localFile, fi);
                }
            }
        }

        return folderFiles;
    }

    /**
     * Examines a file or folder and computes its status. 
     * 
     * @param status entry for this file or null if the file is unknown to subversion
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(File file, ISVNStatus status, ISVNStatus repositoryStatus) {
        if (status == null || status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            if (!svn.isManaged(file)) {
                return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }
            return createMissingEntryFileInformation(file, repositoryStatus);
        } else {
            return createVersionedFileInformation(file, status, repositoryStatus);
        }
    }

    /**
     * Examines a file or folder that has an associated CVS entry. 
     * 
     * @param file file/folder to examine
     * @param status status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createVersionedFileInformation(File file, ISVNStatus status, ISVNStatus repositoryStatus) {

        SVNStatusKind kind = status.getTextStatus();
        SVNStatusKind pkind = status.getPropStatus();

        int remoteStatus = 0;
        if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
            if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.MODIFIED
            || repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.MODIFIED) {
                remoteStatus = FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;
            } else if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.DELETED
            /*|| repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.DELETED*/) {
                remoteStatus = FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
            } else if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.ADDED
            /*|| repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.ADDED*/) {
                // solved in createMissingfileInformation
            } else if (repositoryStatus.getRepositoryTextStatus() == null
            && repositoryStatus.getRepositoryPropStatus() == null) {
                // no remote change at all
            } else {
                // TODO systematically handle all repository statuses
                // so far above were observed....
                // XXX
                System.err.println("SVN.FSC: unhandled repository status: " + file.getAbsolutePath()); // NOI18N
                System.err.println("\ttext: " + repositoryStatus.getRepositoryTextStatus()); // NOI18N
                System.err.println("\tprop: " + repositoryStatus.getRepositoryPropStatus()); // NOI18N
            }
        }
        
        if (status. getLockOwner() != null) {
            remoteStatus = FileInformation.STATUS_LOCKED | remoteStatus;
        }
        
        if (SVNStatusKind.NONE.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.NORMAL.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.MODIFIED.equals(pkind)) {
            if (SVNStatusKind.NORMAL.equals(kind)) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | remoteStatus, status);
            }
        } else if (SVNStatusKind.CONFLICTED.equals(pkind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else {
            throw new IllegalArgumentException("Unknown prop status: " + status.getPropStatus()); // NOI18N
        }


        if (SVNStatusKind.NONE.equals(kind)) {
            return FILE_INFORMATION_UNKNOWN;
        } else if (SVNStatusKind.NORMAL.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus, status);
        } else if (SVNStatusKind.MODIFIED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.ADDED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.DELETED.equals(kind)) {                    
            return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.UNVERSIONED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.MISSING.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.REPLACED.equals(kind)) {            
            // XXX  create new status constant? Is it neccesary to visualize
            // this status or better to use this simplyfication?
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | remoteStatus, status);
        } else if (SVNStatusKind.MERGED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_MERGE | remoteStatus, status);
        } else if (SVNStatusKind.CONFLICTED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.OBSTRUCTED.equals(kind)) {            
            // TODO: create new status constant?
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.IGNORED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED | remoteStatus, status);
        } else if (SVNStatusKind.INCOMPLETE.equals(kind)) {            
            // TODO: create new status constant?
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT | remoteStatus, status);
        } else if (SVNStatusKind.EXTERNAL.equals(kind)) {            
            // TODO: create new status constant?
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus, status);
        } else {        
            throw new IllegalArgumentException("Unknown text status: " + status.getTextStatus()); // NOI18N
        }
    }

    static String statusText(ISVNStatus status) {
        return "file: " + status.getTextStatus().toString() + " copied: " + status.isCopied() + " prop: " + status.getPropStatus().toString(); // NOI18N
    }

    /**
     * Examines a file or folder that does NOT have an associated Subversion status. 
     * 
     * @param file file/folder to examine
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createMissingEntryFileInformation(File file, ISVNStatus repositoryStatus) {
        
        // ignored status applies to whole subtrees
        boolean isDirectory = file.isDirectory();
        int parentStatus = getStatus(file.getParentFile()).getStatus();
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return isDirectory ? 
                FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
        }
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
            if (isDirectory) {
                // Working directory roots (aka managed roots). We already know that isManaged(file) is true
                return SvnUtils.isPartOfSubversionMetadata(file) ? 
                    FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_UPTODATE_DIRECTORY;
            } else {
                return FILE_INFORMATION_NOTMANAGED;
            }
        }

        // mark auxiliary after-update conflict files as ignored
        // C source.java
        // I source.java.mine
        // I source.java.r45
        // I source.java.r57
        //
        // XXX:svnClientAdapter design:  why is not it returned from getSingleStatus() ?
        //
        // after-merge conflicts (even svn st does not recognize as ignored)
        // C source.java
        // ? source.java.working
        // ? source.jave.merge-right.r20
        // ? source.java.merge-left.r0
        //
        // XXX:svn-cli design:  why is not it returned from getSingleStatus() ?
        
        String name = file.getName();
        Matcher m = auxConflictPattern.matcher(name);
        if (m.matches()) {
            File dir = file.getParentFile();
            if (dir != null) {
                String masterName = m.group(1);
                File master = new File(dir, masterName);
                if (master.isFile()) {
                    return FILE_INFORMATION_EXCLUDED;
                }
            }
        }
        
        if (file.exists()) {
            if (Subversion.getInstance().isIgnored(file)) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, file.isDirectory());
            } else {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, file.isDirectory());
            }
        } else {
            if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
                if (repositoryStatus.getRepositoryTextStatus() == SVNStatusKind.ADDED) {
                    boolean folder = repositoryStatus.getNodeKind() == SVNNodeKind.DIR;
                    return new FileInformation(FileInformation.STATUS_VERSIONED_NEWINREPOSITORY, folder);
                }
            }
            return FILE_INFORMATION_UNKNOWN;
        }
    }

    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
    
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        listenerSupport.fireVersioningEvent(EVENT_FILE_STATUS_CHANGED, new Object [] { file, oldInfo, newInfo });
    }

    public void setCommand(int command) {
        // boring ISVNNotifyListener event
    }

    public void logCommandLine(String commandLine) {
        // boring ISVNNotifyListener event
    }

    public void logMessage(String message) {
        // boring ISVNNotifyListener event
    }

    public void logError(String message) {
        // boring ISVNNotifyListener event
    }

    public void logRevision(long revision, String path) {
        // boring ISVNNotifyListener event
    }

    public void logCompleted(String message) {
        // boring ISVNNotifyListener event
    }

    public void onNotify(final File path, final SVNNodeKind kind) {
        // notifications from the svnclientadapter may be caused by a synchronously handled FS event. 
        // The thing is that we have to prevent reentrant calls on the FS api ...
        Utils.post(new Runnable() {
            public void run() {
                onNotifyImpl(path, kind);
            }
        });                        
    }

    private void onNotifyImpl(File path, SVNNodeKind kind) {
        if (path == null) {  // on kill
            return;
        }

        // I saw "./"
        path = FileUtil.normalizeFile(path);

        // ISVNNotifyListener event
        // invalidate cached status
        // force event: an updated file changes status from uptodate to uptodate but its entry changes
        refresh(path, REPOSITORY_STATUS_UNKNOWN, true);
        
        // collect the filesystems to notify them in SvnClientInvocationHandler about the external change
        for (;;) {
            FileObject fo = FileUtil.toFileObject(path);
            if (fo != null) {
                try {
                  Set<FileSystem> filesystems = getFilesystemsToRefresh();
                  synchronized (filesystems) {
                    filesystems.add(fo.getFileSystem());
                  }
                } catch (FileStateInvalidException e) {
                    // ignore invalid filesystems
                }
                break;
            } else {
                path = path.getParentFile();
                if (path == null) break;
            }
        }
    }
                
    public void refreshDirtyFileSystems() {
        if(refreshFilesystemsTask == null) {
           RequestProcessor rp = new RequestProcessor();
           refreshFilesystemsTask = rp.create(new Runnable() {
                public void run() {
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
           }); 
        }
        refreshFilesystemsTask.schedule(200);
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
}
