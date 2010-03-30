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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central part of CVS status management, deduces and caches statuses of files under version control.
 * 
 * @author Maros Sandor
 */
public class FileStatusCache {

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
        
    public static final int REPOSITORY_STATUS_UNKNOWN   = 0;
    public static final int REPOSITORY_STATUS_UPDATED   = 'U';
    public static final int REPOSITORY_STATUS_PATCHED   = 'P';
    public static final int REPOSITORY_STATUS_MODIFIED  = 'M';
    public static final int REPOSITORY_STATUS_CONFLICT  = 'C';
    public static final int REPOSITORY_STATUS_MERGEABLE = 'G';
    public static final int REPOSITORY_STATUS_ADDED     = 'A';
    public static final int REPOSITORY_STATUS_REMOVED   = 'R';
    public static final int REPOSITORY_STATUS_REMOVED_REMOTELY   = 'Y';
    public static final int REPOSITORY_STATUS_UPTODATE  = 65536;

    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects 
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    private final CvsVersioningSystem   cvs;
    private final CvsLiteAdminHandler   sah;

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

    private DiskMapTurboProvider cacheProvider;
    
    private final List<RefreshRequest> refreshRequests = new ArrayList<RefreshRequest>(10);
    
    private final RequestProcessor.Task refreshRequestsTask;

    FileStatusCache(CvsVersioningSystem cvsVersioningSystem) {
        this.cvs = cvsVersioningSystem;
        refreshRequestsTask = org.netbeans.modules.versioning.util.Utils.createTask(new RefreshRequestRunnable());
        sah = (CvsLiteAdminHandler) cvs.getAdminHandler();

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
        Set<File> files = getScannedFiles(dir).keySet();
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
        if (context.getRootFiles().length == 0) {
            // do not load call cacheProvider.getAllModifiedValues() unless really necessary
            // an empty context means also an empty return array, see the loginc bellow
            return new File[0];
        }
        Set<File> set = new HashSet<File>();
        Map allFiles = cacheProvider.getAllModifiedValues();
        for (Iterator i = allFiles.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            FileInformation info = (FileInformation) allFiles.get(file);
            if (info.isDirectory() || (info.getStatus() & includeStatus) == 0) continue;
            File [] roots = context.getRootFiles();
            for (int j = 0; j < roots.length; j++) {
                File root = roots[j];
                if (VersioningSupport.isFlat(root)) {
                    if (file.getParentFile().equals(root)) {
                        set.add(file);
                        break;
                    }
                } else {
                    if (Utils.isParentOrEqual(root, file)) {
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
                    if (Utils.isParentOrEqual(excluded, file)) {
                        j.remove();
                    }
                }
            }
        }
        return (File[]) set.toArray(new File[set.size()]);
    }

    /**
     * Determines the CVS status of a file. This method accesses disk and may block for a long period of time.
     * 
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */ 
    public FileInformation getStatus(File file) {
        if (file == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "File shouldn't be null"); //NOI18N
            return FILE_INFORMATION_NOTMANAGED;
        }
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVS)) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
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
    
    public void refresh(File file, int repositoryStatus, boolean forceChangeEvent) {
        refreshLater(new RefreshRequest(file, repositoryStatus, forceChangeEvent));
    }

    private void refreshLater(RefreshRequest request) {
        synchronized(refreshRequests) {
            refreshRequests.add(request);
        }
        refreshRequestsTask.schedule(777);
    }

    /**
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     * 
     * @param file
     * @param repositoryStatus
     * @param forceChangeEvent true to force the cache to fire the change event even if the status of the file has not changed
     */ 
    public FileInformation refreshNow(File file, int repositoryStatus, boolean forceChangeEvent) {
        File dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map<File, FileInformation> files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) return FILE_INFORMATION_NOTMANAGED;
        FileInformation current = (FileInformation) files.get(file);
        Entry entry = null;
        try {
            entry = cvs.getAdminHandler().getEntry(file);
        } catch (IOException e) {
            // no entry for this file
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        FileInformation fi = createFileInformation(file, entry, repositoryStatus);
        
        // #117933: if the repository status is UNKNOWN and the file already has some remote status, retain it
        if ((fi.getStatus() & FileInformation.STATUS_REMOTE_CHANGE) == 0  && repositoryStatus == REPOSITORY_STATUS_UNKNOWN 
                && current != null && (current.getStatus() & FileInformation.STATUS_REMOTE_CHANGE) != 0) {
            if ((fi.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                fi = new FileInformation(FileInformation.STATUS_VERSIONED_MERGE, fi.isDirectory());
            } else {
                if (current.getStatus() == FileInformation.STATUS_VERSIONED_MERGE) {
                    fi = new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY, current.isDirectory());
                } else {
                    fi = current;
                }
            }
        }
        
        if (equivalent(fi, current)) {
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
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     * 
     * @param file
     * @param repositoryStatus
     */ 
    public FileInformation refresh(File file, int repositoryStatus) {
        return refreshNow(file, repositoryStatus, false);
    }

    /**
     * Creates local file information. This method does not use cache, it always reads status from disk.
     *
     * @param file
     * @return FileInformation
     */
    FileInformation createFileInformation(File file) {
        Entry entry = null;
        try {
            entry = cvs.getAdminHandler().getEntry(file);
        } catch (IOException e) {
            // no entry for this file
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
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
        Entry e1 = main.getEntry(null);
        Entry e2 = other.getEntry(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     * 
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */ 
    private static boolean equal(Entry e1, Entry e2) {
        if (!e1.getRevision().equals(e2.getRevision())) return false;
        return e1.getStickyInformation() == e2.getStickyInformation() || 
                e1.getStickyInformation() != null && e1.getStickyInformation().equals(e2.getStickyInformation());
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
    public void refreshCached(File file, int repositoryStatus) {
        refresh(file, repositoryStatus);
    }

    /**
     * Refreshes status of all files in the given directory.  
     *  
     * @param dir directory to cleanup
     */ 
    public void clearVirtualDirectoryContents(File dir, boolean recursive, File [] exclusions) {
        Map<File, FileInformation> files = cacheProvider.getAllModifiedValues();
        outter : for (Map.Entry<File, FileInformation> entry : files.entrySet()) {
            File file = entry.getKey();
            if (exclusions != null) {
                for (int j = 0; j < exclusions.length; j++) {
                    if (Utils.isParentOrEqual(exclusions[j], file)) continue outter; 
                }
            }
            if (!Utils.isParentOrEqual(dir, file)) continue;
            if (!recursive && !dir.equals(file.getParentFile())) continue;
            refresh(file, REPOSITORY_STATUS_UNKNOWN);
        }
    }

    // --- Package private contract ------------------------------------------
    
    Map<File, FileInformation> getAllModifiedFiles() {
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
                refresh(file, REPOSITORY_STATUS_UNKNOWN, false);
            }
        }
    }
    
    // --- Private methods ---------------------------------------------------

    private Map<File, FileInformation> getScannedFiles(File dir) {
        Map<File, FileInformation> files;
        if (dir.getName().equals(CvsVersioningSystem.FILENAME_CVS)) return NOT_MANAGED_MAP;
        files = (Map<File, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) return files;
        if (!dir.exists()) {
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

        Entry [] entries = null;
        try {
            entries = sah.getEntriesAsArray(dir);
        } catch (IOException e) {
            // no or damaged entries
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filename = file.getName();
            if (filename.equals(CvsVersioningSystem.FILENAME_CVS)) continue;
            Entry entry = getEntry(dir, entries, file);
            FileInformation fi = createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
            // directories are always in cache for listFiles() to work
            if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                folderFiles.put(file, fi);
            }
        }

        if (entries != null) {
            outter : for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];
                File entryFile = new File(dir, entry.getName());
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    if (entryFile.equals(file)) {
                        continue outter;
                    }
                }
                FileInformation fi = createFileInformation(entryFile, entry, REPOSITORY_STATUS_UNKNOWN);
                folderFiles.put(entryFile, fi);
            }
        }
        return folderFiles;
    }

    /**
     * Searches array of Entries for the given filename.
     *
     * @param entries array of Entries, may be null
     * @param file file to search for
     * @return corresponding entry or null
     */
    private Entry getEntry(File dir, Entry[] entries, File file) {
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];
                File entryFile = new File(dir, entry.getName());
                if (file.equals(entryFile)) return entry;
            }
        }
        return null;
    }

    /**
     * Examines a file or folder and computes its CVS status. 
     * 
     * @param file file/folder to examine
     * @param entry CVS entry for this file or null if the file does not have a corresponding entry in CVS/Entries
     * @param repositoryStatus status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(File file, Entry entry, int repositoryStatus) {
        if (entry == null) {
            if (!cvs.isManaged(file)) {
                if (file.exists()) {
                    return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
                } else {
                    return FILE_INFORMATION_UNKNOWN;
                }
            }
            return createMissingEntryFileInformation(file, repositoryStatus);            
        } else {
            cvs.setNotUnignored(file);
            return createVersionedFileInformation(entry, file, repositoryStatus);            
        }
    }

    /**
     * Examines a file or folder that has an associated CVS entry. 
     * 
     * @param entry entry of the file/folder
     * @param file file/folder to examine
     * @param repositoryStatus status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createVersionedFileInformation(Entry entry, File file, int repositoryStatus) {
        if (entry.isDirectory()) {
            if (file.exists()) {
                if (new File(file, CvsVersioningSystem.FILENAME_CVS).isDirectory()) {
                    return FILE_INFORMATION_UPTODATE_DIRECTORY;
                } else {
                    return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, true);
                }
            } else {
                return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY, true);
            }
        }
        if (entry.isNewUserFile()) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, entry, false);
        } else if (entry.isUserFileToBeRemoved()) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, entry, false);
        } else {
            if (!exists(file)) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY, entry, false);                
            }
            if (repositoryStatus == REPOSITORY_STATUS_UPTODATE) {
                if (!entryTimestampMatches(entry,  file)) {
                    entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(file.lastModified())));
                    try {
                        sah.setEntry(file, entry);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
                return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, entry, false);
            } else if (repositoryStatus == REPOSITORY_STATUS_UPDATED || repositoryStatus == REPOSITORY_STATUS_PATCHED) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY, entry, false);
            } else if (repositoryStatus == REPOSITORY_STATUS_MODIFIED) {
                FileInformation fi = new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, entry, false);
                return fi;
            } else if (repositoryStatus == REPOSITORY_STATUS_CONFLICT) {
                if (isLocalConflict(entry, file)) {
                    return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, entry, false);
                } else {
                    return new FileInformation(FileInformation.STATUS_VERSIONED_MERGE, entry, false);
                }
            } else if (repositoryStatus == REPOSITORY_STATUS_MERGEABLE) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_MERGE, entry, false);
            } else if (repositoryStatus == REPOSITORY_STATUS_REMOVED_REMOTELY) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY, entry, false);
            } else if (repositoryStatus == REPOSITORY_STATUS_UNKNOWN || repositoryStatus == '?') {
                if (exists(file)) {
                    if (isLocalConflict(entry, file)) {
                        return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, entry, false);
                    } else if (entryTimestampMatches(entry,  file)) {
                        return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, entry, false);
                    } else {
                        FileInformation fi = new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, entry, false);
                        return fi;
                    }                    
                } else {
                    return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY, entry, false);                    
                }
            }
        }
        throw new IllegalArgumentException("Unknown repository status: " + (char)repositoryStatus); // NOI18N
    }

    private boolean isLocalConflict(Entry entry, File file) {
        return exists(file) && entry.hadConflicts() && entryTimestampMatches(entry, file);
    }

    /**
     * Examines a file or folder that does NOT have an associated CVS entry. 
     * 
     * @param file file/folder to examine
     * @param repositoryStatus status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createMissingEntryFileInformation(File file, int repositoryStatus) {
        boolean isDirectory = file.isDirectory();
        int parentStatus = getStatus(file.getParentFile()).getStatus();
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return isDirectory ? FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
        }
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
            if (isDirectory) {
                // Working directory roots (aka managed roots). We already know that cvs.isManaged(file) is true
                return isInsideCvsMetadata(file) ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_UPTODATE_DIRECTORY;
            } else {
                return FILE_INFORMATION_NOTMANAGED;
            }
        }

        // TODO: workaround for bug 71635, sometimes it happens that a new directory with 'A' status does not have an Entry, the reason is unknown 
        if (repositoryStatus == REPOSITORY_STATUS_ADDED && isDirectory) {
            return FILE_INFORMATION_UPTODATE_DIRECTORY;
        }
        if (repositoryStatus == REPOSITORY_STATUS_UNKNOWN || repositoryStatus == '?') {
            if (exists(file)) {
                if (cvs.isIgnored(file)) {
                    return isDirectory ? FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
                }        
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                return new FileInformation(FileInformation.STATUS_UNKNOWN, false);
            }                    
        } else if (repositoryStatus == REPOSITORY_STATUS_UPDATED) {
            if (file.exists()) {
                // the file should be fetched from server but it already exists locally, this will create a conflict
                return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, isDirectory);
            } else {
                return new FileInformation(FileInformation.STATUS_VERSIONED_NEWINREPOSITORY, isDirectory);
            }
        } else if (repositoryStatus == REPOSITORY_STATUS_UPTODATE) {
            if (parentStatus == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                // server ignores new files on a branch, check this special case  
                if (exists(file) && Utils.getSticky(file) != null) {
                    return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
                }
                // server marks this file as uptodate and it does not have an entry, the file is probably listed in CVSROOT/cvsignore
                return cvs.isUnignored(file) ? new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory) : new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, isDirectory);
            }
        } else if (repositoryStatus == REPOSITORY_STATUS_REMOVED_REMOTELY) {
            if (exists(file)) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                return FILE_INFORMATION_UNKNOWN;
            }
        } else if (repositoryStatus == REPOSITORY_STATUS_CONFLICT) {
            // happens for files that exist locally and are also in repository
            // CVS reports: cvs.exe update: move away THIS_FILE; it is in the way
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, false);
        }
        throw new IllegalArgumentException("Unknown repository status: " + (char)repositoryStatus + " for: " + file.getAbsolutePath()); // NOI18N
    }

    private boolean isInsideCvsMetadata(File file) {
        return file.getAbsolutePath().indexOf(File.separator + CvsVersioningSystem.FILENAME_CVS + File.separator) != -1;
    }

    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }

    private boolean entryTimestampMatches(Entry entry, File file) {
        Date d = entry.getLastModified();
        if (d == null) return false;
        long t0 = d.getTime();
        long t1 = file.lastModified() / 1000 * 1000;
        if (TimeZone.getDefault().inDaylightTime(entry.getLastModified())) {
            t1 -= TimeZone.getDefault().getDSTSavings();
        }
        return t0 == t1 || t0 - t1 == 3600000 || t1 - t0 == 3600000;
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

    public FileInformation getCachedStatus(File file) {
        file = file.getParentFile();
        if (file == null) return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        Map<File, FileInformation> files = (Map<File, FileInformation>) turbo.readEntry(file, FILE_STATUS_MAP);
        return files != null ? files.get(file) : null;
    }

    private static final class NotManagedMap extends AbstractMap<File, FileInformation> {
        public Set entrySet() {
            return Collections.EMPTY_SET;
        }
    }

    private class RefreshRequestRunnable implements Runnable {
        public void run() {
            List<RefreshRequest> toProcess;
            synchronized(refreshRequests) {
                toProcess = new ArrayList<RefreshRequest>(refreshRequests);
                refreshRequests.clear();
            }
            refresh(toProcess);              
        }
        
        private void refresh(List<RefreshRequest> requests) {
            for (RefreshRequest request : requests) {
                FileStatusCache.this.refreshNow(request.file, request.repositoryStatus, request.forceChangeEvent);
            }
        }
    }
    
    private class RefreshRequest {
        
        final File    file;
        final int     repositoryStatus;
        final boolean forceChangeEvent;

        RefreshRequest(File file, int repositoryStatus, boolean forceChangeEvent) {
            this.file = file;
            this.repositoryStatus = repositoryStatus;
            this.forceChangeEvent = forceChangeEvent;
        }
    }
}
