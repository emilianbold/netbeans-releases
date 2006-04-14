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

import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Handles events fired from the filesystem such as file/folder create/delete/move.
 * 
 * @author Maros Sandor
 */
class FilesystemHandler extends ProvidedExtensions implements FileChangeListener {
        
    private static final RequestProcessor  eventProcessor = new RequestProcessor("Subversion FS Monitor", 1); // NOI18N

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
        File file = FileUtil.toFile(fe.getFile());
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            eventProcessor.post(new FileCreatedTask(file));
        }
    }

    public void fileDataCreated(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        File file = FileUtil.toFile(fe.getFile());
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            eventProcessor.post(new FileCreatedTask(file));
        }
    }
    
    public void fileChanged(FileEvent fe) {
        if (Thread.currentThread() == ignoredThread) return;
        File file = FileUtil.toFile(fe.getFile());
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            eventProcessor.post(new FileChangedTask(file));
        }
    }

    public void fileDeleted(FileEvent fe) {
        // needed for external deletes; othewise, beforeDelete is quicker
        if (Thread.currentThread() == ignoredThread) return;
        File file = FileUtil.toFile(fe.getFile());
        if (file != null) {
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0) {
                // file is already deleted, cache contains null for up-to-date file
                // for deleted files it'd mean FILE_INFORMATION_UNKNOWN
                // on the other hand cache keeps all folders regardless their status
                File probe = file.getParentFile();
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                // XXX STATUS_VERSIONED instead
                if ((cache.getStatus(probe).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                    eventProcessor.post(new FileDeletedTask(file));
                }
            }
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        FileObject newFo = fe.getFile();
        File newFile = FileUtil.toFile(newFo);
        if (newFile != null) {
            cache.refresh(newFile, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            File parent = newFile.getParentFile();
            if (parent != null) {
                String name = fe.getName();
                String ext = fe.getExt();
                if (ext != null && "".equals(ext) == false) {  // NOI18N
                    name += "." + ext;  // NOI18N
                }
                File oldFile = new File(parent, name);
                if (oldFile.equals(newFile)) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Wrong (identity) rename event for " + newFile.getAbsolutePath());
                }
                cache.refresh(oldFile, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }
    
    // InterceptionListener implementation ---------------------------
    
    public void createSuccess(FileObject fo) {
        if (ignoringEvents()) return;
        File file = FileUtil.toFile(fo);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            if (fo.isFolder() && svn.isAdministrative(fo.getNameExt())) {
                // TODO: we need to delete all files created inside administrative folders
                File f = new File(file, Subversion.INVALID_METADATA_MARKER);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
                }
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
            File file = FileUtil.toFile(fo);
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                saveRecursively(file);
            }
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
        File file = FileUtil.toFile(fo);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            if (fo.isFolder()) {
                for (Iterator i = savedMetadata.keySet().iterator(); i.hasNext();) {
                    File dir = (File) i.next();
                    if (SvnUtils.isParentOrEqual(file, dir)) {
    //                    CvsMetadata metadata = (CvsMetadata) savedMetadata.get(dir);
    //                    MetadataAttic.setMetadata(dir, metadata);
                        i.remove();
                    }
                }
                refreshRecursively(file);
            }
        }
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
        File file = FileUtil.toFile(fo);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            if (fo.isFolder()) {
                for (Iterator i = savedMetadata.keySet().iterator(); i.hasNext();) {
                    File dir = (File) i.next();
                    if (SvnUtils.isParentOrEqual(file, dir)) {
                        if (!dir.exists()) {
    //                        CvsMetadata metadata = (CvsMetadata) savedMetadata.get(dir);
    //                        MetadataAttic.setMetadata(dir, metadata);
                        }
                        i.remove();
                    }
                }
                refreshRecursively(file);
            }
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
            client.removeNotifyListener(Subversion.getInstance().getLogger());
            client.remove(new File [] { file }, true);

            // fire event explicitly because the file is already gone
            // so svnClientAdapter does not fire ISVNNotifyListener event
            Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } catch (SVNClientException e) {
            // ignore; we do not know what to do here; does no harm
        }
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

    // BEGIN #73042 ProvidedExtensions ~~~~~~~~~~~~~~~~~~~~
    public ProvidedExtensions.IOHandler getRenameHandler(final File from, final String newName) {
        final File to = new File(from.getParentFile(), newName);
        return (implsRename(from, to)) ?  new ProvidedExtensions.IOHandler() {
            public void handle() throws IOException {
                renameImpl(from, to);
            }
        } : null;
    }

    public ProvidedExtensions.IOHandler getMoveHandler(final File from, final File to) {
        return (implsMove(from, to)) ?  new ProvidedExtensions.IOHandler() {
            public void handle() throws IOException {
                moveImpl(from, to);
            }
        } : null;
    }


    public boolean implsRename(File from, File to) {
        return implsMove(from, to);
    }

    public boolean implsMove(File srcFile, File to) {
        File destDir = to.getParentFile();
        if (srcFile != null && destDir != null) {
            FileInformation info = cache.getStatus(srcFile);
            if ((info.getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                return true;
            }
        }

        return false;
    }

    public  void svnMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        try {                        
            boolean force = true; // file with local changes must be forced
            ISVNClientAdapter client = Subversion.getInstance().getClient();
            client.removeNotifyListener(cache);  // do not fire events before MFS
            client.move(srcFile, dstFile, force);
        } catch (SVNClientException e) {
            IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath());
            ex.initCause(e);
            throw ex;
        }
    }


    public void renameImpl(File from, File to) throws IOException {
        moveImpl(from, to);
    }

    public void moveImpl(final File from, final File to) throws IOException {
        if (SwingUtilities.isEventDispatchThread()) {
            
            // Openide implemetation mistakenly calls FS from AWT
            // relax our asserts by reposting to non-AWT thread and wait
            // and print out warning so original gets fixed
            
            Exception ex = new IllegalThreadStateException("WARNING: above code access filesystem from AWT.\nImagine that Subversion's filesystem handler can connect to server over (slow) network.\nWorkarounding... (it may deadlocks however).");
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
            final Throwable innerT[] = new Throwable[1];
            Runnable outOfAwt = new Runnable() {
                public void run() {
                    try {
                        svnMoveImplementation(from, to);
                    } catch (Throwable t) {
                        innerT[0] = t;
                    }
                }
            };
            
            Subversion.getInstance().getRequestProccessor().post(outOfAwt).waitFinished();
            if (innerT[0] != null) {
                if (innerT[0] instanceof IOException) {
                    throw (IOException) innerT[0];
                } else if (innerT[0] instanceof RuntimeException) {
                    throw (RuntimeException) innerT[0];
                } else if (innerT[0] instanceof Error) {
                    throw (Error) innerT[0];
                } else {
                    throw new IllegalStateException("Unexpected exception class: " + innerT[0]);  // NOI18N
                }
            }
            
            // end of hack
            
        } else {
            svnMoveImplementation(from, to);
        }
    }
    // END #73042 InterceptionListener2 ~~~~~~~~~~~~~~~~~~~~

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

}
