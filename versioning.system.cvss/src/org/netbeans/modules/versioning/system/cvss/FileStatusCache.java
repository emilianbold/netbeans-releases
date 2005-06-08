/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.spi.VersioningListener;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.lib.cvsclient.admin.Entry;

import java.io.*;
import java.util.*;

/**
 * Central part of CVS status management, deduces and caches statuses of files under version control.
 * 
 * @author Maros Sandor
 */
public class FileStatusCache {

    /**
     * Indicates that status of a file changed and listeners SHOULD check new status 
     * values if they are interested in this file.
     */
    public static final Object EVENT_FILE_STATUS_CHANGED = new Object();

    /**
     * A folder maps to this special Map IFF the folder itself is not managed and also does not contain any managed files/folders.
     */ 
    static final Map NOT_MANAGED_MAP = new NotManagedMap();
        
    private static final int STATUS_MISSING =  
            FileInformation.STATUS_VERSIONED_NEWINREPOSITORY | 
            FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY; 
    
    public static final int REPOSITORY_STATUS_UNKNOWN   = 0;
    public static final int REPOSITORY_STATUS_UPDATED   = 'U';
    public static final int REPOSITORY_STATUS_PATCHED   = 'P';
    public static final int REPOSITORY_STATUS_MODIFIED  = 'M';
    public static final int REPOSITORY_STATUS_CONFLICT  = 'C';
    public static final int REPOSITORY_STATUS_MERGEABLE = 'G';
    public static final int REPOSITORY_STATUS_REMOVED   = 'R';
    public static final int REPOSITORY_STATUS_REMOVED_REMOTELY   = 'Y';
    public static final int REPOSITORY_STATUS_UPTODATE  = 65536;

    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true); 
    private static final FileInformation FILE_INFORMATION_UPTODATE = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true); 
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    private final CvsVersioningSystem   cvs;
    private final CvsLiteAdminHandler   sah;

    private static final boolean TURBO_ENABLED = "turbo".equalsIgnoreCase(System.getProperty("netbeans.experimental.FileStatusCache"));  // NOI18N

    /**
     * Holds three kinds of information: what folders we have scanned, what files we have found
     * and what statuses of these files are.
     * If a directory is not found as a key in the map, we have not scanned it yet.
     * If it has been scanned, it maps to a Set of files that were found somehow out of sync with the
     * repository (have any other status then up-to-date). In case all files are up-to-date, it maps
     * to Collections.EMPTY_MAP. Entries in this map are created as directories are scanne, are never removed and
     * are updated by the refresh method.
     */
    private final Map       scannedFolders;         // File => Map<File,FileInformation>

    private final Object    scanLock = new Object();

    private Turbo           turbo;
    
    private int             serialVersion = 0;

    /**
     * Identifies attribute that holds information about all non STATUS_VERSIONED_UPTODATE files.
     *
     * <p>Key type: File identifying existing folder
     * TODO allow non-existing folders to cover deep virtual structures
     *
     * <p>Value type: Map&lt;String (file name), FileInformation>
     */
    public static final String FILE_STATUS_MAP = "org.netbeans.modules.versioning.system.cvss-FILE_STATUS_MAP";  // NOI18N

    FileStatusCache(CvsVersioningSystem cvsVersioningSystem, HashMap cache) {
        scannedFolders = cache == null ? new HashMap() : cache;
        this.cvs = cvsVersioningSystem;
        sah = (CvsLiteAdminHandler) cvs.getAdminHandler();

        if (TURBO_ENABLED) {
            System.out.println("Turbo is active, netbeans.experimental.FileStatusCache property honored.");  // NOI18N
            turbo = Turbo.createCustom(new CustomProviders() {
                private final Set provider = Collections.singleton(new CacheTurboProvider());
                public Iterator providers() {
                    return provider.iterator();
                }
            }, 1, 100);
        }
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
        Set files = getScannedFiles(dir).keySet();
        return (File[]) files.toArray(new File[files.size()]);
    }

    /**
     * Behaves identically to listFiles method EXCEPT that it does not trigger file scanning. If this directory
     * is not yet scanned, it throws an exception;
     *  
     * @param dir directory to list
     * @return File [] files in this directory
     * @throws InformationUnavailableException if this directory was not scanned yet
     */ 
    public File [] listFilesCached(File dir) throws InformationUnavailableException {
        synchronized(onFolderInfp()) {
            if (getFolderInfo(dir) == null) {
                if (isNotManagedByDefault(dir)) {
                    return new File[0];
                } else {
                    throw new InformationUnavailableException();
                }
            }
        }
        return listFiles(dir);
    }
    
    /**
     * Determines the CVS status of a file. This method accesses disk and may block for a long period of time.
     * 
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */ 
    public FileInformation getStatus(File file) {
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
        return file.exists() ? FILE_INFORMATION_UPTODATE : FILE_INFORMATION_UNKNOWN;
    }

    /**
     * Determines the CVS status of a file. This method fails fast if the requested information is not readily available.
     * 
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @throws InformationUnavailableException if status of the file is not readily available
     * @see FileInformation
     */ 
    public FileInformation getStatusCached(File file) throws InformationUnavailableException {
        synchronized(onFolderInfp()) {
            if (getFolderInfo(file.getParentFile()) == null) throw new InformationUnavailableException();
        }
        return getStatus(file);
    }
    
    /**
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     * 
     * @param file
     * @param repositoryStatus
     */ 
    public FileInformation refresh(File file, int repositoryStatus) {
        File dir = file.getParentFile();
        Map files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) return FILE_INFORMATION_NOTMANAGED;
        FileInformation current = (FileInformation) files.get(file);
        FileInformation fi = createFileInformation(file, repositoryStatus);
        if (fi.equals(current)) return fi;
        // do not include uptodate files into cache, missing directories must be included
        if (current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
            return fi;
        }
        synchronized(onFolderInfp()) {
            if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
                files.remove(file);
                removeFolderInfo(file); // remove mapping in case of directories
            }
            else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
                files.remove(file);
            } else {
                files.put(file, fi);
            }
        }
        if (file.isDirectory() && needRecursiveRefresh(fi, current)) {
            File [] content = listFiles(file);
            for (int i = 0; i < content.length; i++) {
                refresh(content[i], REPOSITORY_STATUS_UNKNOWN);
            }
        }
        fireFileStatusChanged(file);
        return fi;
    }

    private boolean needRecursiveRefresh(FileInformation fi, FileInformation current) {
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED || 
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) return true;
        // we probably do not need this since file may become 'not managed' and vice versa only if configuration of 
        // versioning roots changes and this is handled by refreshRoots()
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
     * Scans given directory and performs two tasks: 1) refreshes all cached file statuses, 2) removes from cache
     * all files having one of the {@link STATUS_MISSING} status.  
     *  
     * @param dir directory to cleanup
     */ 
    public void clearVirtualDirectoryContents(File dir, boolean recursive) {
        synchronized(onFolderInfp()) {
            Map files = getFolderInfo(dir);
            if (files == null) {
               return;
            }
            Set set = new HashSet(files.keySet());
            for (Iterator i = set.iterator(); i.hasNext();) {
                File file = (File) i.next();
                if (recursive && file.isDirectory()) {
                    clearVirtualDirectoryContents(file, true);
                }
                FileInformation fi = refresh(file, REPOSITORY_STATUS_UNKNOWN);
                if ((fi.getStatus() & STATUS_MISSING) != 0) {
                    files.keySet().remove(file);
                }
            }
        }
    }
    
    // --- Package private contract ------------------------------------------
    
    /**
     * Returns this cache's serial number. Serial number of the cache increases every time before 
     * the {@link #EVENT_FILE_STATUS_CHANGED} is fired. If the number is the same across calls to this method, you can
     * be sure that nothing changed in cache (or at least, nobody knows it yet). Serial versions exist to support
     * consistent caching of results obtained from the cache.
     * Later we can replace this mechanism by some direct notification of friend clients. 
     * 
     * @return int cache serial number
     */ 
    int getSerialVersion() {
        return serialVersion;
    }

    void directoryContentChanged(File dir) {
        // TODO: implement
        fireFileStatusChanged(dir);                
    }
    
    /**
     * Used for simple serialization of cache.
     * 
     * @param oos stream to write to
     */
    void writeCache(ObjectOutputStream oos) throws IOException {
        synchronized(scannedFolders) {
            oos.writeObject(scannedFolders);
        }
    }

    /**
     * Cleans up the cache by removing or correcting entries that are no longer valid or correct.
     * TODO: implement full cleanup, may also speculatively delete some entries to limit cache size 
     */ 
    void cleanUp() {
        synchronized(onFolderInfp()) {
            Set toRefresh = new HashSet();
            for (Iterator i = scannedFolders.values().iterator(); i.hasNext();) {
                Map map = (Map) i.next();
                Set files = map.keySet();
                for (Iterator j = files.iterator(); j.hasNext();) {
                    File file = (File) j.next();
                    FileInformation info = (FileInformation) map.get(file);
                    if ((info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                        toRefresh.add(file);
                    }
                }
            }
            for (Iterator i = toRefresh.iterator(); i.hasNext();) {
                File file = (File) i.next();
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }
        
    // --- Private methods ---------------------------------------------------

    /** Access data structure, allows to swap impl. */
    private void putFolderInfo(File dir, Map files) {
        if (TURBO_ENABLED) {
            turbo.writeEntry(dir, FILE_STATUS_MAP, files);
        } else {
            synchronized(onFolderInfp()) {
                assert !dir.getName().equals(CvsVersioningSystem.FILENAME_CVS);
                scannedFolders.put(dir, files);
            }
        }
    }

    private void removeFolderInfo(File dir) {
        if (TURBO_ENABLED) {
            turbo.writeEntry(dir, FILE_STATUS_MAP, null);
        } else {
            synchronized(onFolderInfp()) {
                scannedFolders.remove(dir);
            }
        }
    }
    
    /** Access data structure, allows to swap impl. */
    private Map getFolderInfo(File dir) {
        if (TURBO_ENABLED) {
            return (Map) turbo.readEntry(dir, FILE_STATUS_MAP);
        } else {
            assert Thread.holdsLock(scannedFolders);
            return (Map) scannedFolders.get(dir);
        }
    }

    /** Access data structure lock, allows to swap impl. */
    private Object onFolderInfp() {
        if (TURBO_ENABLED) {
            return turbo;
        } else {
            return scannedFolders;
        }
    }

    private Map getScannedFiles(File dir) {
        Map files;
        synchronized(onFolderInfp()) {
            files = getFolderInfo(dir);
            if (files != null) return files;
        }
        if (isNotManagedByDefault(dir)) {
            return NOT_MANAGED_MAP; 
        }
        synchronized(scanLock) {
            synchronized(onFolderInfp()) {
                files = getFolderInfo(dir);
                if (files != null) return files;
            }
            files = scanFolder(dir);    // must not execute while holding the lock, it may take long to execute
            putFolderInfo(dir, files);
        }
        return files;
    }

    private boolean isNotManagedByDefault(File dir) {
        return !dir.exists() && MetadataAttic.getMetadata(dir) == null || dir.getName().equals(CvsVersioningSystem.FILENAME_CVS);
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
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInformation fi = createFileInformation(file, REPOSITORY_STATUS_UNKNOWN);
            // directories are always in cache for listFiles() to work
            if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                folderFiles.put(file, fi);
            }
        }

        try {
            Entry [] entries = sah.getEntriesAsArray(dir);
            outter : for (int i = 0; i < entries.length; i++) {
                Entry entry = entries[i];
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    if (file.getName().equals(entry.getName())) {
                        continue outter;
                    }
                }
                File file = new File(dir, entries[i].getName());
                FileInformation fi = createFileInformation(file, REPOSITORY_STATUS_UNKNOWN);
                folderFiles.put(file, fi);
            }
        } catch (IOException e) {
            // bad entries, ignore them
        }
        return folderFiles;
    }

    /**
     * Examines a file or folder and computes its CVS status. 
     * 
     * @param file file/folder to examine
     * @param repositoryStatus status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(File file, int repositoryStatus) {
        FileInformation fi = null;
        Entry entry = null;
        if (!cvs.isManaged(file)) {
            return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
        }
        if (cvs.isIgnored(file)) {
            return file.isDirectory() ? FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
        }
        try {
            entry = sah.getEntry(file);
        } catch (IOException e) {
            // no Entry available for this file, it is not versioned
        }
        if (entry != null) {
            fi = createVersionedFileInformation(entry, file, repositoryStatus);            
        } else {
            fi = createMissingEntryFileInformation(file, repositoryStatus);            
        }
        return fi;
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
            return FILE_INFORMATION_UPTODATE_DIRECTORY;
        }
        if (entry.isNewUserFile()) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, entry, false);
        } else if (entry.isUserFileToBeRemoved()) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, entry, false);
        } else {
            if (!file.exists()) {
                return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY, entry, false);                
            }
            if (repositoryStatus == REPOSITORY_STATUS_UPTODATE) {
                if (!entryTimestampMatches(entry,  file)) {
                    entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(file.lastModified())));
                    try {
                        sah.setEntry(file, entry);
                    } catch (IOException e) {
                        e.printStackTrace();
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
                if (file.exists()) {
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
        throw new IllegalArgumentException("Unknown repository status: " + (char)repositoryStatus);
    }

    private boolean isLocalConflict(Entry entry, File file) {
        return file.exists() && entry.hadConflicts() && entryTimestampMatches(entry, file);
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
        // Working directory roots (aka managed roots). We already know that cvs.isManaged(file) is true
        if (isDirectory && parentStatus == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
             return FILE_INFORMATION_UPTODATE_DIRECTORY;
        }
        if (repositoryStatus == REPOSITORY_STATUS_UNKNOWN || repositoryStatus == '?') {
            if (file.exists()) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                return new FileInformation(FileInformation.STATUS_UNKNOWN, false);
            }                    
        } else if (repositoryStatus == REPOSITORY_STATUS_UPDATED) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_NEWINREPOSITORY, isDirectory);
        } else if (repositoryStatus == REPOSITORY_STATUS_UPTODATE) {
            // server marks this file as uptodate and it does not have an entry, the file is probably listed in CVSROOT/cvsignore
            if (getStatus(file.getParentFile()).getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, isDirectory);
            }
        } else if (repositoryStatus == REPOSITORY_STATUS_REMOVED_REMOTELY) {
            if (file.exists()) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, isDirectory);
            } else {
                return FILE_INFORMATION_UNKNOWN;
            }
        }
        throw new IllegalArgumentException("Unknown repository status: " + (char)repositoryStatus);
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
    
    private void fireFileStatusChanged(File file) {
        serialVersion++;
        listenerSupport.fireVersioningEvent(EVENT_FILE_STATUS_CHANGED, file);
    }

    private static final class NotManagedMap extends AbstractMap implements Serializable {
        public Set entrySet() {
            return Collections.EMPTY_SET;
        }
        
        private Object readResolve() {
            return NOT_MANAGED_MAP;
        }
    }
    
    public static class InformationUnavailableException extends Exception {
        
    }
}
