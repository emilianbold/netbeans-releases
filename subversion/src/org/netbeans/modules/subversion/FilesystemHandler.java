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

import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.FileUtils;
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
class FilesystemHandler extends ProvidedExtensions implements FileChangeListener, ProvidedExtensions.DeleteHandler {
        
    private static final RequestProcessor  eventProcessor = new RequestProcessor("Subversion FS Monitor", 1); // NOI18N

    private final Subversion svn;
    private final FileStatusCache   cache;
    
    /**
     * Stores .svn folders that should be deleted ASAP.
     */ 
    private final Set<File> invalidMetadata = new HashSet<File>(5);
    
    /**
     * Delete interceptor: holds files and folders that we do not want to delete but must pretend that they were deleted.
     */ 
    private final Set<File> deletedFiles = new HashSet<File>(5);
    
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
    
    private void cleanupDeletedFiles(File createdFile) {
        for (Iterator<File> i = deletedFiles.iterator(); i.hasNext();) {
            File deleted = i.next();
            if (!deleted.exists() || createdFile.equals(deleted)) {
                i.remove();
            }
        }
    }
    
    /**
     * Removes invalid metadata from all known folders.
     */ 
    void removeInvalidMetadata() {
        synchronized(invalidMetadata) {
            for (File file : invalidMetadata) {
                SvnUtils.deleteRecursively(file);
            }
            invalidMetadata.clear();
        }
    }
    
    // FileChangeListener implementation ---------------------------
    
    public void fileFolderCreated(FileEvent fe) {
        File file = FileUtil.toFile(fe.getFile());
        cleanupDeletedFiles(file);
        if (Thread.currentThread() == ignoredThread) return;
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            eventProcessor.post(new FileCreatedTask(file));
        }
    }

    public void fileDataCreated(FileEvent fe) {
        File file = FileUtil.toFile(fe.getFile());
        cleanupDeletedFiles(file);
        if (Thread.currentThread() == ignoredThread) return;
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
                if ((cache.getStatus(probe).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
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
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Wrong (identity) rename event for " + newFile.getAbsolutePath()); // NOI18N
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
        try {
            Diagnostics.println("[createSuccess " + fo); // NOI18N
            if (fo.isFolder() && svn.isAdministrative(fo.getNameExt())) {
                // TODO: we need to delete all files created inside folders
                File file = FileUtil.toFile(fo);
                File f = new File(file, Subversion.INVALID_METADATA_MARKER);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "Unable to create marker: " + f.getAbsolutePath()); // NOI18N
                }
                invalidMetadata.add(file);
            }
        } finally {
            Diagnostics.println("]createSuccess " + fo); // NOI18N
        }
    }

    // XXX depends on #75168
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {        
        File file = FileUtil.toFile(parent);
        if (file != null) {
            try {
                file = new File(file, name);
                Diagnostics.println("[beforeCreate " + file); // NOI18N
                if (file.exists() == false) {
                    int status = cache.getStatus(file).getStatus();
                    if (status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                        try {
                            SvnClient client = Subversion.getInstance().getClient(true);
                            client.revert(file, false);
                            file.delete();
                        } catch (SVNClientException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                }
            } finally {
                Diagnostics.println("]beforeCreate " + file); // NOI18N
            }
        }
        

    }
    
    public void createFailure(FileObject parent, String name, boolean isFolder) {
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

    private Set<FileSystem> getRootFilesystems() {
        Set<FileSystem> filesystems = new HashSet<FileSystem>();
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
            ISVNClientAdapter client = Subversion.getInstance().getClient(false);
            client.remove(new File [] { file }, true);

            // fire event explicitly because the file is already gone
            // so svnClientAdapter does not fire ISVNNotifyListener event
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } catch (SVNClientException e) {
            // ignore; we do not know what to do here; does no harm
        }
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

    private boolean hasMetadata(File file) {
        return new File(file, ".svn/entries").canRead() || new File(file, "_svn/entries").canRead();
    }
    
    /**
     * Creates a handler object that will handle deletion of this file and all its children.
     * 
     * @param file file to delete
     * @return a handler or null if this file deletion is not to be handled by this module
     */
    public DeleteHandler getDeleteHandler(File file) {
        if (SvnUtils.isPartOfSubversionMetadata(file)) return this;
        // calling cache results in SOE, we must check manually
        if (file.isFile()) return null;
        if (hasMetadata(file)) return this; 
        return null;
    }
    
    /**
     * This interceptor ensures that subversion metadata is NOT deleted. 
     * 
     * @param file file to delete
     * @return true if the deletion was successful, false otherwise
     */ 
    public boolean delete(File file) {
        boolean isMetadata = SvnUtils.isPartOfSubversionMetadata(file);
        synchronized(deletedFiles) {
            if (file.isDirectory()) {
                if (!isMetadata && !hasMetadata(file)) return file.delete();
                File [] children = file.listFiles();
                if (children == null) return true;
                for (int i = 0; i < children.length; i++) {
                    File child = children[i];
                    if (!deletedFiles.contains(child)) return false;
                }
                if (!isMetadata) {
                    remove(file);
                }
                // once we 'delete' a directory, we can forget about its children, can we not?
                for (File child : children) {
                    deletedFiles.remove(child);
                }
                deletedFiles.add(file);
                return true;
            }
            if (isMetadata) {
                deletedFiles.add(file);
                return true;
            } else {
                return file.delete();
            }
        }
    }
    
    private boolean remove(File file) {
        try {
            ISVNClientAdapter client = Subversion.getInstance().getClient(false);
            // funny thing is, the command will delete all files recursively
            client.remove(new File [] { file }, true);
            return true;
        } catch (SVNClientException e) {
            return false;
        }
    }

    public boolean implsRename(File from, File to) {
        return implsMove(from, to);
    }

    public boolean implsMove(File srcFile, File to) {
        File destDir = to.getParentFile();
        if (srcFile != null && destDir != null) {
            FileInformation info = cache.getStatus(srcFile);
            if ((info.getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                return Subversion.getInstance().isManaged(to);
            }
            // else XXX handle file with saved administative
            // right now they have old status in cache but is it guaranteed?
        }

        return false;
    }

    public  void svnMoveImplementation(final File srcFile, final File dstFile) throws IOException {
        try {                        
            boolean force = true; // file with local changes must be forced
            ISVNClientAdapter client = Subversion.getInstance().getClient(false);
            
            File tmpMetadata = null;
            try {
                // prepare destination, it must be under Subversion control
                removeInvalidMetadata();

                File parent;
                if (dstFile.isDirectory()) {
                    parent = dstFile;
                } else {
                    parent = dstFile.getParentFile();
                }

                if (parent != null) {
                    int status = cache.getStatus(parent).getStatus();
                    assert Subversion.getInstance().isManaged(parent);  // see implsMove above

                    if ((FileInformation.STATUS_VERSIONED & status) == 0) {
                        addDirectories(parent);
                    }
                }

                // perform
                int retryCounter = 6;
                while (true) {
                    try {
                        client.move(srcFile, dstFile, force);
                        break;
                    } catch (SVNClientException e) {
                        // svn: Working copy '/tmp/co/svn-prename-19/AnagramGame-pack-rename/src/com/toy/anagrams/ui2' locked
                        if (e.getMessage().endsWith("' locked") && retryCounter > 0) { // NOI18N
                            // XXX HACK AWT- or FS Monitor Thread performs
                            // concurrent operation
                            try {
                                Thread.sleep(107);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                            retryCounter--;
                            continue;
                        }

                        // XXX loosing file history is less harm than raising IOException
                        // that completelly breaks clients (namely refactoring can not handle IOEx)
                        if (srcFile.renameTo(dstFile)) {
                            ErrorManager.getDefault().annotate(e, "Relaxing Subversion rename error...."); // NOI18N
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e); // NOI18N
                            break;
                        } else {
                            IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath()); // NOI18N
                            ex.initCause(e);
                            throw ex;
                        }
                    }
                }
            } finally {
                if (tmpMetadata != null) {
                    FileUtils.deleteRecursively(tmpMetadata);
                }
            }
        } catch (SVNClientException e) {
            IOException ex = new IOException("Subversion failed to rename " + srcFile.getAbsolutePath() + " to: " + dstFile.getAbsolutePath()); // NOI18N
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * Seeks versioned root and then adds all folders
     * under Subversion (so it contains metadata),
     */
    private void addDirectories(File dir) throws SVNClientException  {
        File parent = dir.getParentFile();
        if (parent != null) {
            int status = cache.getStatus(parent).getStatus();
            if ((FileInformation.STATUS_VERSIONED & status) == 0) {
                addDirectories(parent);  // RECURSION
            }
            ISVNClientAdapter client = Subversion.getInstance().getClient(false);
            client.addDirectory(dir, false);
            cache.refresh(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        } else {
            throw new SVNClientException("Reached FS root, but it's still not Subversion versioned!"); // NOI18N
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
            
            Exception ex = new IllegalThreadStateException("WARNING: above code access filesystem from AWT.\nImagine that Subversion's filesystem handler can connect to server over (slow) network.\nWorkarounding... (it may deadlocks however)."); // NOI18N
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
            
            Subversion.getInstance().getRequestProcessor().post(outOfAwt).waitFinished();
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
