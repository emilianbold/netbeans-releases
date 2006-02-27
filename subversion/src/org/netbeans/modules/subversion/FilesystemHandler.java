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

import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler implements FileChangeListener, InterceptionListener {
        
    private static final RequestProcessor  eventProcessor = new RequestProcessor("CVS-Event", 1); // NOI18N

    private final Subversion svn;
    private final FileStatusCache   cache;
    private final Map savedMetadata = new HashMap();
    private static Thread ignoredThread;

    public FilesystemHandler(Subversion svn) {
        this.svn = svn;
        cache = svn.getStatusCache();
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
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDataCreated(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
//        new RuntimeException("new: " + fe.getFile().getPath()).printStackTrace();
        eventProcessor.post(new FileCreatedTask(FileUtil.toFile(fe.getFile())));
    }
    
    public void fileChanged(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        eventProcessor.post(new FileChangedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileDeleted(FileEvent fe) {
        // needed for external deletes; othewise, beforeDelete is quicker
        if (Thread.currentThread() == ignoredThread) return;
        eventProcessor.post(new FileDeletedTask(FileUtil.toFile(fe.getFile())));
    }

    public void fileRenamed(FileRenameEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
//        new RuntimeException("move: " +  fe.getFile().getPath()).printStackTrace();
        eventProcessor.post(new FileRenamedTask(fe));
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    // InterceptionListener implementation ---------------------------
    
    public void createSuccess(FileObject fo) {
        if (ignoringEvents()) return;
        if (fo.isFolder() && svn.isAdministrative(fo.getNameExt())) {
            // TODO: we need to delete all files created inside administrative folders
            File f = new File(FileUtil.toFile(fo), Subversion.INVALID_METADATA_MARKER);
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
        if (fo.isFolder()) {
            saveRecursively(FileUtil.toFile(fo));
        } else {
            FileObject parent = fo.getParent();
            if (svn.isAdministrative(parent.getName())) {
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
                if (svn.isAdministrative(file.getName())) {
                    saveMetadata(root);
                } else {
                    saveRecursively(file);
                }
            }
        }
    }

    public void deleteSuccess(FileObject fo) {
        if (ignoringEvents()) return;
        File deleted = FileUtil.toFile(fo);
        if (fo.isFolder()) {
            for (Iterator i = savedMetadata.keySet().iterator(); i.hasNext();) {
                File dir = (File) i.next();
                if (SvnUtils.isParentOrEqual(deleted, dir)) {
//                    CvsMetadata metadata = (CvsMetadata) savedMetadata.get(dir);
//                    MetadataAttic.setMetadata(dir, metadata);
                    i.remove();
                }
            }
            refreshRecursively(deleted);
        }
        fileDeletedImpl(deleted);
    }

    private void refreshRecursively(File file) {
/*
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
*/
    }

    public void deleteFailure(FileObject fo) {
        if (ignoringEvents()) return;
        if (fo.isFolder()) {
            File notDeleted = FileUtil.toFile(fo);
            for (Iterator i = savedMetadata.keySet().iterator(); i.hasNext();) {
                File dir = (File) i.next();
                if (SvnUtils.isParentOrEqual(notDeleted, dir)) {
                    if (!dir.exists()) {
//                        CvsMetadata metadata = (CvsMetadata) savedMetadata.get(dir);
//                        MetadataAttic.setMetadata(dir, metadata);
                    }
                    i.remove();
                }
            }
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
    
    private void fileCreatedImpl(File file) {
        if (file == null) return;
        int status = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();

        if (file.isDirectory()) {
/*
            CvsMetadata data = MetadataAttic.getMetadata(file);
            if (data != null) {
                try {
                    data.save(new File(file, CvsVersioningSystem.FILENAME_CVS));
                    MetadataAttic.setMetadata(file, null);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
*/
        }

        if ((status & FileInformation.STATUS_MANAGED) == 0) return;

        if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
/*
            StandardAdminHandler sah = new StandardAdminHandler();
            Entry entry = null;
            try {
                entry = sah.getEntry(file);
            } catch (IOException e) {
            }
            if (entry != null && !entry.isDirectory() && entry.isUserFileToBeRemoved()) {
                cvsUndoRemoveLocally(sah, file, entry);    
            }
*/
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
//        if (properties_changed) cache.directoryContentChanged(file.getParentFile());
        if (file.isDirectory()) cache.directoryContentChanged(file);
    }

    /**
     * If a regular file is deleted then update its Entries as if it has been removed.
     * 
     * @param file deleted file
     */ 
    private void fileDeletedImpl(File file) {
        if (file == null) return;

        try {
            ISVNClientAdapter client = Subversion.getInstance().getClient();
            client.remove(new File [] { file }, true);
        } catch (SVNClientException e) {
            // ignore; we do not know what to do here; does no harm
        }
        cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
//        if (properties_changed) cache.directoryContentChanged(file.getParentFile());
    }
    
    /**
     * The folder's metadata is about to be deleted. We have to save all metadata information.
     * 
     * @param dir
     */ 
    private void saveMetadata(File dir) {
        dir = FileUtil.normalizeFile(dir);
/*
        MetadataAttic.setMetadata(dir, null);
        if (savedMetadata.get(dir) != null) return;
        try {
            CvsMetadata data = CvsMetadata.readAndRemove(dir);
            savedMetadata.put(dir, data);
        } catch (IOException e) {
            // cannot read folder metadata, the folder is most probably not versioned
            return;
        }
*/
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
//            if (property_file_changed) cache.directoryContentChanged(file.getParentFile());
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
        
            File copied = FileUtil.toFile(newFile);
            File parent = FileUtil.toFile(newFile.getParent());
            File removed = new File(parent, oldName + oldExtension);

            if (removed == null) return;

            try {
                // XXX this is event handler files are already gone
                // but svn requires them to exist
                copied.renameTo(removed);                
                boolean force = true; // file with local chanegs must be forced
                ISVNClientAdapter client = Subversion.getInstance().getClient();
                client.move(removed, copied, force);
            } catch (SVNClientException e) {
                // ignore; we do not know what to do here; does no harm
                e.printStackTrace();
            }
            cache.refresh(removed, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            cache.refresh(copied, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);

        }
    }
}
