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

import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler implements FileChangeListener, InterceptionListener {
        
    private static final RequestProcessor  eventProcessor = new RequestProcessor("CVS-Event", 1);

    private final FileStatusCache   cache;
    
    public FilesystemHandler(CvsVersioningSystem cvs) {
        cache = cvs.getStatusCache();
    }
    
    // FileChangeListener implementation ---------------------------
    
    public void fileFolderCreated(FileEvent fe) {
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDataCreated(FileEvent fe) {
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }
    
    public void fileChanged(FileEvent fe) {
        eventProcessor.post(new FileChangedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDeleted(FileEvent fe) {
        eventProcessor.post(new FileDeletedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileRenamed(FileRenameEvent fe) {
        eventProcessor.post(new FileRenamedTask(fe));
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    // InterceptionListener implementation ---------------------------
    
    /**
     * We save all CVS metadata to be able to commit files that were
     * in that directory.
     * 
     * @param fo FileObject, we are only interested in files inside CVS directory
     */ 
    public void beforeDelete(FileObject fo) {
        if (fo.isFolder()) return;
        if (!(fo = fo.getParent()).getName().equals(CvsVersioningSystem.FILENAME_CVS)) return;
        File file = FileUtil.toFile(fo.getParent());
        saveMetadata(file);
    }

    public void deleteSuccess(FileObject fo) {
        // not interested
    }

    public void deleteFailure(FileObject fo) {
        // not interested
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
        entry.setRevision("-" + entry.getRevision());
        entry.setConflict(Entry.DUMMY_TIMESTAMP);
        try {
            ah.setEntry(file, entry);
        } catch (IOException e) {
            // failed to set entry, the file will be probably resurrected during update
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
            if (oldExtension.length() > 0) oldExtension = "." + oldExtension;
        
            File parent = FileUtil.toFile(newFile.getParent());
            File removed = new File(parent, oldName + oldExtension);
        
            fileDeletedImpl(removed);
            fileCreatedImpl(FileUtil.toFile(newFile));
        }

    }
}
