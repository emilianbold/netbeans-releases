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
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler extends VCSInterceptor {
        
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
        return !ignoringEvents();
    }

    public void doDelete(File file) throws IOException {
        if (file.isDirectory() && hasMetadata(file)) {
            CvsVisibilityQuery.hideFolder(file);
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, true);
        } else if (org.netbeans.modules.versioning.system.cvss.util.Utils.isPartOfCVSMetadata(file)) {
            // medatada are never deleted
        } else {
            if (!file.delete()) {
                throw new IOException("Failed to delete file: " + file.getAbsolutePath());
            }
            fileDeletedImpl(file);
        }
    }

    public void afterDelete(File file) {
        refreshDeleted(file);
    }
    
    private void refreshDeleted(File file) {
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN, false);
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
    }

    /**
     * We handle directory renames that are managed by CVS.
     */
    public boolean beforeMove(File from, File to) {
        File destDir = to.getParentFile();
        return from != null && destDir != null && org.netbeans.modules.versioning.system.cvss.util.Utils.containsMetadata(from);
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
            to.mkdirs(); // make sure destination fodler is created even if the source folder is empty
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) {
                    if (fileName.equals(CvsVersioningSystem.FILENAME_CVS)) {
                        CvsVisibilityQuery.hideFolder(file.getParentFile());
                        continue;
                    }
                    File toFile = new File(to, fileName);
                    moveRecursively(affectedFiles, file, toFile);
                    affectedFiles.add(file);
                    affectedFiles.add(toFile);
                } else {
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

    public void afterMove(final File from, final File to) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                fileDeletedImpl(from);
                fileCreatedImpl(to);
            }
        });
    }
    
    public boolean beforeCreate(File file, boolean isDirectory) {
        if (ignoringEvents()) return false;
        return isDirectory && file.getName().equals(CvsVersioningSystem.FILENAME_CVS);
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

    public void afterCreate(final File file) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                fileCreatedImpl(file);
            }
        });
    }

    public void afterChange(final File file) {
        if (ignoringEvents()) return;
        Utils.post(new Runnable() {
            public void run() {
                cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
            }
        });
    }

    // private methods ---------------------------

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

        refreshDeleted(file);
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
