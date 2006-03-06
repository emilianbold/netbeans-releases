/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.client.ExceptionInformation;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.subversion.settings.MetadataAttic;
import org.netbeans.modules.subversion.util.FlatFolder;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;

import java.io.*;
import java.util.*;
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
    private static final Map NOT_MANAGED_MAP = new NotManagedMap();
        
    private static final int STATUS_MISSING =  
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY; 
    
    public static final ISVNStatus REPOSITORY_STATUS_UNKNOWN  = null;

    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects 
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

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
    private File [] listFiles(File dir) {
        Set files = getScannedFiles(dir).keySet();
        return (File[]) files.toArray(new File[files.size()]);
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files. This method
     * returns no folders.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(Context context, int includeStatus) {
        Set set = new HashSet();
        Map allFiles = cacheProvider.getAllModifiedValues();
        for (Iterator i = allFiles.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) allFiles.get(file);
            if (info.isDirectory() || (info.getStatus() & includeStatus) == 0) continue;
            File [] roots = context.getRootFiles();
            for (int j = 0; j < roots.length; j++) {
                File root = roots[j];
                if (root instanceof FlatFolder) {
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
        return (File[]) set.toArray(new File[set.size()]);
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
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     * 
     * @param file
     * @param repositoryStatus
     */ 
    public FileInformation refresh(File file, ISVNStatus repositoryStatus) {
        File dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) return FILE_INFORMATION_NOTMANAGED;
        FileInformation current = (FileInformation) files.get(file);
        
        ISVNStatus status = null;
        try {
            ISVNClientAdapter client = Subversion.getInstance().getClient();
            status = client.getSingleStatus(file);
            if (status != null && SVNStatusKind.UNVERSIONED.equals(status.getTextStatus())) {
                status = null;
            }
        } catch (SVNClientException e) {
            ExceptionInformation ei = new ExceptionInformation(e);            
            if (ei.isUnversionedResource()) {  
                // no or damaged entries
                // or ignored file
                e.printStackTrace();
            }
        }
        FileInformation fi = createFileInformation(file, status, repositoryStatus);
        if (equivalent(fi, current)) return fi;
        // do not include uptodate files into cache, missing directories must be included
        if (current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
            return fi;
        }

        file = FileUtil.normalizeFile(file);
        dir = FileUtil.normalizeFile(dir);
        Map newFiles = new HashMap(files);
        if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
            newFiles.remove(file);
            turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
        }
        else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
            newFiles.remove(file);
        } else {
            newFiles.put(file, fi);
        }
        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.size() == 0 ? null : newFiles);

        if (file.isDirectory() && needRecursiveRefresh(fi, current)) {
            File [] content = listFiles(file);
            for (int i = 0; i < content.length; i++) {
                refresh(content[i], REPOSITORY_STATUS_UNKNOWN);
            }
        }
        fireFileStatusChanged(file, current, fi);
        return fi;
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
        if (e1.getRevision().getNumber() != e2.getRevision().getNumber()) {
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
     * Scans given directory and performs two tasks: 1) refreshes all cached file statuses, 2) removes from cache
     * all files having one of the {@link STATUS_MISSING} status.  
     *  
     * @param dir directory to cleanup
     */ 
    public void clearVirtualDirectoryContents(File dir, boolean recursive, File [] exclusions) {
        Map files = (Map) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files == null) {
           return;
        }
        Set set = new HashSet(files.keySet());
        Map newMap = null;
        outter: for (Iterator i = set.iterator(); i.hasNext();) {
            File file = (File) i.next();
            if (exclusions != null) {
                for (int j = 0; j < exclusions.length; j++) {
                    if (SvnUtils.isParentOrEqual(exclusions[j], file)) continue outter; 
                }
            }
            if (recursive && file.isDirectory()) {
                clearVirtualDirectoryContents(file, true, exclusions);
            }
            FileInformation fi = refresh(file, REPOSITORY_STATUS_UNKNOWN);
            if ((fi.getStatus() & STATUS_MISSING) != 0) {
                if (newMap == null) newMap = new HashMap(files);
                newMap.remove(file);
            }
        }
        if (newMap != null) {
            dir = FileUtil.normalizeFile(dir);
            turbo.writeEntry(dir, FILE_STATUS_MAP, newMap);
        }
    }

    // --- Package private contract ------------------------------------------
    
    Map getAllModifiedFiles() {
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

    private Map getScannedFiles(File dir) {
        Map files;       
        if (svn.isAdministrative(dir)) return NOT_MANAGED_MAP;
        files = (Map) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (isNotManagedByDefault(dir)) {
            return NOT_MANAGED_MAP; 
        }

        // scan and populate cache with results

        dir = FileUtil.normalizeFile(dir);
        files = scanFolder(dir);    // must not execute while holding the lock, it may take long to execute
        turbo.writeEntry(dir, FILE_STATUS_MAP, files);
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) files.get(file);
            if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) fireFileStatusChanged(file, null, info);
        }
        return files;
    }

    private boolean isNotManagedByDefault(File dir) {
        return !dir.exists() && MetadataAttic.getMetadata(dir) == null;
    }

    /**
     * Scans all files in the given folder, computes and stores their CVS status. 
     * 
     * @param dir directory to scan
     * @return Map map to be included in the status cache (File => FileInformation)
     */ 
    private Map scanFolder(File dir) {
        File [] files = dir.listFiles();
        if (files == null) files = new File[0];
        Map folderFiles = new HashMap(files.length);

        ISVNStatus [] entries = null;
        try {
            ISVNClientAdapter client = Subversion.getInstance().getClient(); // XXX use methodcall with repository url if server contact is needed...
            if (Subversion.getInstance().isManaged(dir)) {
                entries = client.getStatus(dir, false, false);  // XXX should contact server: , true);
            }
        } catch (SVNClientException e) {
            // no or damaged entries
            ErrorManager.getDefault().annotate(e, "Can not status " + dir.getAbsolutePath() + ", guessing it...");  // NOI18N
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
            for (int i = 0; i < entries.length; i++) {
                ISVNStatus entry = entries[i];
                File file = new File(entry.getPath());
                FileInformation fi = createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
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

//        System.err.println("File: "  + file.getAbsolutePath() + " \nstatus: " + statusText(status));  // XXX remove

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
                // TODO systematically handle all statuses
                // XXX text: replaced
                System.err.println("SVN.FSC: unhandled repository status: " + file.getAbsolutePath());
                System.err.println("\ttext: " + repositoryStatus.getRepositoryTextStatus());
                System.err.println("\tprop: " + repositoryStatus.getRepositoryPropStatus());
            }
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
            throw new IllegalArgumentException("Unknown prop status: " + status.getPropStatus());
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
            // TODO: create new status constant?
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
            throw new IllegalArgumentException("Unknown text status: " + status.getTextStatus());
        }
    }

    static String statusText(ISVNStatus status) {
        return "file: " + status.getTextStatus().toString() + " copied: " + status.isCopied() + " prop: " + status.getPropStatus().toString();
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
                return isInsideSubversionMetadata(file) ? 
                    FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_UPTODATE_DIRECTORY;
            } else {
                return FILE_INFORMATION_NOTMANAGED;
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
                    // XXX fill repositoryStatus.getNodeKind() from svn info in CmdLineClientAdapter
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

    public void onNotify(File path, SVNNodeKind kind) {

        if (path == null) {  // on kill
            return;
        }

        // I saw "./"
        path = FileUtil.normalizeFile(path);

        // ISVNNotifyListener event
        // invalidate cached status
        refresh(path, REPOSITORY_STATUS_UNKNOWN);

        // notify FS about the extrenal change
        FileObject fo = FileUtil.toFileObject(path);
        if (fo != null) {
            fo.refresh();
        }
    }

    private boolean isInsideSubversionMetadata(File file) {
        return file.getPath().indexOf(File.separatorChar + ".svn" + File.separatorChar) != -1;
    }

    private static final class NotManagedMap extends AbstractMap {
        public Set entrySet() {
            return Collections.EMPTY_SET;
        }
    }
}
