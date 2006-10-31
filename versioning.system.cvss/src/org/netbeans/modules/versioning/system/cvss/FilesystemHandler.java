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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler extends VCSInterceptor {
        
    private static final String METADATA_PATTERN = File.separator + CvsVersioningSystem.FILENAME_CVS;

    private static final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "CVS(\\" + File.separatorChar + ".*|$)");
    
    // TODO: perform tasks asynchronously if possible
    private final RequestProcessor rp = new RequestProcessor("VCS-Interceptor", 1); // NOI18N            
    
    private final FileStatusCache   cache;
    private static Thread ignoredThread;

    public FilesystemHandler(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
    }

    /**
     * We save all CVS metadata to be able to commit files that were in that directory.
     * 
     * @param file File, we are only interested in files inside CVS directory
     */ 
    public boolean beforeDelete(File file) {
        if (ignoringEvents()) return false;
        return isPartOfCVSMetadata(file) || file.isDirectory() && hasMetadata(file);
    }

    public void doDelete(File file) throws IOException {
        if (file.isDirectory() && hasMetadata(file)) {
            new File(file, "CVS/.nb-removed").createNewFile();
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, true);
            return;
        }
        if (!isPartOfCVSMetadata(file)) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    public void afterDelete(File file) {
        if (ignoringEvents() || !shouldHandle(file)) return;
        fileDeletedImpl(file);
    }
    
    /**
     * We handle directory renames that are managed by CVS.
     */
    public boolean beforeMove(File from, File to) {
        File destDir = to.getParentFile();
        if (from != null && destDir != null && from.isDirectory()) {
            FileInformation info = cache.getStatus(from);
            return (info.getStatus() & FileInformation.STATUS_MANAGED) != 0;
        }
        return false;
    }
    
    /**
     * We only handle directories, file renames are examined ex post. Both directories share the same parent.
     * 
     * @param from source directory to be renamed
     * @param to new directory to be created 
     */
    public void doMove(File from, File to) throws IOException {
        List<File> affectedFiles = new ArrayList<File>();
        moveRecursively(affectedFiles, from, to);
        cvsRemoveRecursively(from);
        refresh(affectedFiles);
    }

    private void moveRecursively(List<File> affectedFiles, File from, File to) throws IOException {
        File [] files = from.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    if (fileName.equals("CVS")) {
                        new File(file, ".nb-removed").createNewFile();
                        continue;
                    }
                    File toFile = new File(to, fileName);
                    moveRecursively(affectedFiles, file, toFile);
                    affectedFiles.add(file);
                    affectedFiles.add(toFile);
                } else {
                    to.mkdirs();
                    File toFile = new File(to, fileName);
                    file.renameTo(toFile);
                    affectedFiles.add(file);
                    affectedFiles.add(toFile);
                }
            }
        }
    }

    private void cvsRemoveRecursively(File dir) {
        StandardAdminHandler sah = new StandardAdminHandler();
        Entry [] entries = null;
        try {
            entries = sah.getEntriesAsArray(dir);
        } catch (IOException e) {
            // the Entry is not available, continue with no Entry
        }
        
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry != null && !entry.isDirectory() && !entry.isUserFileToBeRemoved()) {
                    File file = new File(dir, entry.getName());
                    cvsRemoveLocally(sah, file, entry);
                }
            }
        }
        
        File [] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) cvsRemoveRecursively(file);
        }
    }

    public void afterMove(File from, File to) {
        if (ignoringEvents()) return;
        if (!shouldHandle(from) && !shouldHandle(to)) return;
        fileDeletedImpl(from);
        fileCreatedImpl(to);
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        if (ignoringEvents() || !shouldHandle(file)) return false;
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVS)) {
            if (file.isDirectory()) {
                File f = new File(file, CvsLiteAdminHandler.INVALID_METADATA_MARKER);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void doCreate(File file, boolean isDirectory) throws IOException {
        file.mkdir();
        File f = new File(file, CvsLiteAdminHandler.INVALID_METADATA_MARKER);
        try {
            f.createNewFile();
        } catch (IOException e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
        }
    }

    public void afterCreate(File file) {
        if (ignoringEvents() || !shouldHandle(file)) return;
        fileCreatedImpl(file);
    }

    public void afterChange(File file) {
        if (ignoringEvents() || !shouldHandle(file)) return;
        cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
    }

    // private methods ---------------------------

    /**
     * Determines whether changes to a given FileObject should be handled.
     * 
     * @param file File that changed somehow
     * @return true if events coming from the given FileObject should be handled by this versioning module, false otherwise
     */ 
    private boolean shouldHandle(File file) {
        
        // IMPLEMENTATION NOTE: 
        // Strictly speaking, we should NOT rely on FileStatusCache in this method because call to this method PRECEDES 
        // updates to the cache and thus in this moment status of files in the cache, particulary status of this file is 
        // most probably wrong and will be updated only once this call returns TRUE.
        // But since the cache is fast, we will use it for all files except those that do not exist.

        // also process events from CVS/ metadata dir, which would otherwise be reported as NOT_MANAGED by the cache
        String path = file.getAbsolutePath();

        int idx = path.lastIndexOf(METADATA_PATTERN);
        if (idx != -1) {
            if (idx == path.length() - 4) return true;
            if (path.charAt(idx + 4) == File.separatorChar) {
                return path.indexOf(File.separatorChar, idx + 5) == -1;
            }
        }

        for (;;) {
            if (file.exists()) break;
            file = file.getParentFile();
            if (file == null) return true;  // be on the safe side
        }
        int status = cache.getStatus(file).getStatus();
        return (status & FileInformation.STATUS_MANAGED) != 0;
    }

    private void fileCreatedImpl(File file) {
        if (file == null) return;
        int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();

        if ((status & FileInformation.STATUS_MANAGED) == 0) return;

        if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            StandardAdminHandler sah = new StandardAdminHandler();
            Entry entry = null;
            try {
                entry = sah.getEntry(file);
            } catch (IOException e) {
                // the Entry is not available, continue with no Entry
            }
            if (entry != null && !entry.isDirectory() && entry.isUserFileToBeRemoved()) {
                cvsUndoRemoveLocally(sah, file, entry);    
            }
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
        if (file.isDirectory()) cache.directoryContentChanged(file);
    }

    /**
     * If a regular file is deleted then update its Entries as if it has been removed.
     * 
     * @param file deleted file
     */ 
    private void fileDeletedImpl(File file) {
        if (file == null) return;
        
        StandardAdminHandler sah = new StandardAdminHandler();
        Entry entry = null;
        try {
            entry = sah.getEntry(file);
        } catch (IOException e) {
            // the Entry is not available, continue with no Entry
        }
        if (entry != null && !entry.isDirectory() && !entry.isUserFileToBeRemoved()) {
            cvsRemoveLocally(sah, file, entry);    
        }

        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
    }
    
    /**
     * Emulates the 'cvs remove' command by modifying Entries. We do this to avoid contacting the
     * server.
     * 
     * @param ah
     * @param file
     * @param entry
     */ 
    private void cvsRemoveLocally(AdminHandler ah, File file, Entry entry) {
        try {
            if (entry.isNewUserFile()) {
                ah.removeEntry(file);
            } else {
                entry.setRevision("-" + entry.getRevision()); // NOI18N
                entry.setConflict(Entry.DUMMY_TIMESTAMP);
                ah.setEntry(file, entry);
            }
        } catch (IOException e) {
            // failed to set/remove entry, there is no way to recover from this
        }
    }

    private void cvsUndoRemoveLocally(AdminHandler ah, File file, Entry entry) {
        entry.setRevision(entry.getRevision().substring(1));
        entry.setConflict(Entry.getLastModifiedDateFormatter().format(new Date(System.currentTimeMillis() - 1000)));
        try {
            ah.setEntry(file, entry);
        } catch (IOException e) {
            // failed to set entry, the file will be probably resurrected during update
        }
    }
    
    private void refresh(List<File> files) {
        for (File file : files) {
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, true);          
        }
    }

    private boolean hasMetadata(File file) {
        return new File(file, "CVS/Repository").canRead();
    }
    
    private boolean isPartOfCVSMetadata(File file) {
        return metadataPattern.matcher(file.getAbsolutePath()).matches();
    }
    
    /**
     * Ignores (internal) events from current thread. E.g.:
     * <pre>
     * try {
     *     FilesystemHandler.ignoreEvents(true);
     *     fo.createData(file.getName());
     * } finally {
     *     FilesystemHandler.ignoreEvents(false);
     * }
     * </pre>
     *
     * <p>It assumes that filesystem operations fire
     * synchronous events.
     * @see {http://javacvs.netbeans.org/nonav/issues/show_bug.cgi?id=68961}
     */
    static void ignoreEvents(boolean ignore) {
        if (ignore) {
            ignoredThread = Thread.currentThread();
        } else {
            ignoredThread = null;
        }
    }

    /**
     * @return true if filesystem events are ignored in current thread, false otherwise
     */ 
    private static boolean ignoringEvents() {
        return ignoredThread == Thread.currentThread();
    }
}
