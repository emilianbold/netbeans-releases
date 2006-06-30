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

import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler implements FileChangeListener, InterceptionListener {
        
    private static final RequestProcessor  eventProcessor = new RequestProcessor("CVS-Event", 1); // NOI18N

    private static final String METADATA_PATTERN = File.separator + CvsVersioningSystem.FILENAME_CVS;
    
    private final FileStatusCache   cache;
    private static Thread ignoredThread;

    public FilesystemHandler(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
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
     * @see http://javacvs.netbeans.org/nonav/issues/show_bug.cgi?id=68961
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
    static boolean ignoringEvents() {
        return ignoredThread == Thread.currentThread();
    }
    
    // FileChangeListener implementation ---------------------------
    
    public void fileFolderCreated(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        if (!shouldHandle(fe)) return;
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDataCreated(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        if (!shouldHandle(fe)) return;
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }
    
    public void fileChanged(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        if (!shouldHandle(fe)) return;
        eventProcessor.post(new FileChangedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDeleted(FileEvent fe) {
        // needed for external deletes; othewise, beforeDelete is quicker
        if (Thread.currentThread() == ignoredThread) return;
        if (!shouldHandle(fe)) return;
        eventProcessor.post(new FileDeletedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileRenamed(FileRenameEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        if (!shouldHandle(fe)) return;
        eventProcessor.post(new FileRenamedTask(fe));
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    // InterceptionListener implementation ---------------------------
    
    public void createSuccess(FileObject fo) {
        if (ignoringEvents()) return;
        if (!shouldHandle(fo)) return;
        if (fo.isFolder() && fo.getNameExt().equals(CvsVersioningSystem.FILENAME_CVS)) {
            File f = new File(FileUtil.toFile(fo), CvsLiteAdminHandler.INVALID_METADATA_MARKER);
            try {
                f.createNewFile();
            } catch (IOException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
            }
        }        
    }

    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        // not interested
    }
    
    public void createFailure(FileObject parent, String name, boolean isFolder) {
        // not interested
    }

    /**
     * We save all CVS metadata to be able to commit files that were
     * in that directory.
     * 
     * @param fo FileObject, we are only interested in files inside CVS directory
     */ 
    public void beforeDelete(FileObject fo) {
        if (ignoringEvents()) return;
        if (!shouldHandle(fo)) return;
        if (fo.isFolder()) {
            saveRecursively(FileUtil.toFile(fo));
        } else {
            FileObject parent = fo.getParent();
            if (CvsVersioningSystem.FILENAME_CVS.equals(parent.getName())) {
                if ((parent = parent.getParent()) == null) return;
                File matadataOwner = FileUtil.toFile(parent);
                saveMetadata(matadataOwner);
            }
        }
    }

    private void saveRecursively(File root) {
        File [] files = root.listFiles();
        if (files == null) return;   // invalid or deleted folder
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                if (CvsVersioningSystem.FILENAME_CVS.equals(file.getName())) {
                    saveMetadata(root);
                } else {
                    saveRecursively(file);
                }
            }
        }
    }

    public void deleteSuccess(FileObject fo) {
        if (ignoringEvents()) return;
        if (!shouldHandle(fo)) return;
        File deleted = FileUtil.toFile(fo);
        if (fo.isFolder()) {
            refreshRecursively(deleted);
        }
        fileDeletedImpl(deleted);
    }

    private void refreshRecursively(File file) {
        CvsMetadata data = MetadataAttic.getMetadata(file);
        if (data == null) return;
        Entry [] entries = data.getEntryObjects();
        for (int i = 0; i < entries.length; i++) {
            Entry entry = entries[i];
            if (entry.getName() == null) continue;
            if (entry.isDirectory()) {
                refreshRecursively(new File(file, entry.getName()));
            } else {
                cache.refreshCached(new File(file, entry.getName()), FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
        // not interested
    }

    public void deleteFailure(FileObject fo) {
        if (ignoringEvents()) return;
        if (!shouldHandle(fo)) return;
        if (fo.isFolder()) {
            File notDeleted = FileUtil.toFile(fo);
            CvsMetadata data = MetadataAttic.getMetadata(notDeleted);
            flushMetadata(notDeleted, data);
            refreshRecursively(notDeleted);
        }
    }

    // package private contract ---------------------------

    /**
     * Registers listeners to all disk filesystems.
     */ 
    void init() {
        Set filesystems = getRootFilesystems();
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.addFileChangeListener(this);
        }
    }

    /**
     * Unregisters listeners from all disk filesystems.
     */ 
    void shutdown() {
        Set filesystems = getRootFilesystems();
        for (Iterator i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = (FileSystem) i.next();
            fileSystem.removeFileChangeListener(this);
        }
    }

    private Set getRootFilesystems() {
        Set filesystems = new HashSet();
        File [] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(root));
            if (fo == null) continue;
            try {
                filesystems.add(fo.getFileSystem());
            } catch (FileStateInvalidException e) {
                // ignore invalid filesystems
            }
        }
        return filesystems;
    }
    
    // private methods ---------------------------

    
    /**
     * Determines whether a filesystem event should be handled.
     * 
     * @param fe event in question
     * @return true if this event should be handled by this versioning module, false otherwise
     */ 
    private boolean shouldHandle(FileEvent fe) {
        return shouldHandle(fe.getFile());
    }

    /**
     * Determines whether changes to a given FileObject should be handled.
     * 
     * @param fo FileObject that changed somehow
     * @return true if events coming from the given FileObject should be handled by this versioning module, false otherwise
     */ 
    private boolean shouldHandle(FileObject fo) {
        File file = FileUtil.toFile(fo);
        
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

        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
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

        if (file.isDirectory()) {
            CvsMetadata data = MetadataAttic.getMetadata(file);
            flushMetadata(file, data);
        }

        if ((status & FileInformation.STATUS_MANAGED) == 0) return;

        if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            StandardAdminHandler sah = new StandardAdminHandler();
            Entry entry = null;
            try {
                entry = sah.getEntry(file);
            } catch (IOException e) {
            }
            if (entry != null && !entry.isDirectory() && entry.isUserFileToBeRemoved()) {
                cvsUndoRemoveLocally(sah, file, entry);    
            }
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
        if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
        if (file.isDirectory()) cache.directoryContentChanged(file);
    }

    private void flushMetadata(File dir, CvsMetadata data) {
        if (data == null) return;
        try {
            // do not overwrite existing metadata on disk
            File metadataDir = new File(dir, CvsVersioningSystem.FILENAME_CVS);
            if (!metadataDir.exists()) {
                data.save(metadataDir);
            }
            MetadataAttic.setMetadata(dir, null);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
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
    
    /**
     * The folder's metadata is about to be deleted. We have to save all metadata information.
     * 
     * @param dir
     */ 
    private void saveMetadata(File dir) {
        dir = FileUtil.normalizeFile(dir);
        if (MetadataAttic.getMetadata(dir) != null) return;
        MetadataAttic.setMetadata(dir, null);
        try {
            CvsMetadata data = CvsMetadata.readAndRemove(dir);
            MetadataAttic.setMetadata(dir, data);
        } catch (IOException e) {
            // cannot read folder metadata, the folder is most probably not versioned
            return;
        }
    }

    /**
     * Handles the File Created event.
     */ 
    private final class FileCreatedTask implements Runnable {
        
        private final File file;

        FileCreatedTask(File file) {
            this.file = file;
        }
        
        public void run() {
            fileCreatedImpl(file);
        }
    }

    /**
     * Handles the File Changed event.
     */ 
    private final class FileChangedTask implements Runnable {
        
        private final File file;

        FileChangedTask(File file) {
            this.file = file;
        }
        
        public void run() {
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            if (file.getName().equals(CvsVersioningSystem.FILENAME_CVSIGNORE)) cache.directoryContentChanged(file.getParentFile());
        }
    }

    /**
     * Handles the File Deleted event.
     */
    private final class FileDeletedTask implements Runnable {

        private final File file;

        FileDeletedTask(File file) {
            this.file = file;
        }

        public void run() {
            fileDeletedImpl(file);
        }
    }

    /**
     * Handles the File Rename event.
     */ 
    private final class FileRenamedTask implements Runnable {
        
        private final FileRenameEvent event;

        public FileRenamedTask(FileRenameEvent event) {
            this.event = event;
        }

        public void run() {
            FileObject newFile = event.getFile();
            String oldName = event.getName();
            String oldExtension = event.getExt();
            if (oldExtension.length() > 0) oldExtension = "." + oldExtension; // NOI18N
        
            File parent = FileUtil.toFile(newFile.getParent());
            File removed = new File(parent, oldName + oldExtension);
        
            fileDeletedImpl(removed);
            fileCreatedImpl(FileUtil.toFile(newFile));
        }
    }
}
