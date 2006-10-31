/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.openide.filesystems.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSystem;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Plugs into IDE filesystem and delegates file operations to registered versioning systems. 
 * 
 * @author Maros Sandor
 */
class FilesystemInterceptor extends ProvidedExtensions implements FileChangeListener {
    
    private VersioningManager master;

    // === LIFECYCLE =======================================================================================
    
    /**
     * Initializes the interceptor by registering it into master filesystem.
     * Registers listeners to all disk filesystems.
     * @param versioningManager
     */
    void init(VersioningManager versioningManager) {
        assert master == null;
        master = versioningManager;
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

    // ==================================================================================================
    // CHANGE
    // ==================================================================================================
    
    public void fileChanged(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe).afterChange();
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================

    private void removeFromDeletedFiles(File file) {
        synchronized(deletedFiles) {
            deletedFiles.remove(file);
        }
    }

    private void removeFromDeletedFiles(FileObject fo) {
        synchronized(deletedFiles) {
            if (deletedFiles.size() > 0) {
                deletedFiles.remove(FileUtil.toFile(fo));
            }
        }
    }
    
    public DeleteHandler getDeleteHandler(File file) {
        removeFromDeletedFiles(file);
        DelegatingInterceptor dic = getInterceptor(file, false);
        return dic.beforeDelete() ? dic : null;
    }

    public void fileDeleted(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe).afterDelete();
    }
        
    // ==================================================================================================
    // CREATE
    // ==================================================================================================
    
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {
        File file = FileUtil.toFile(parent);
        if (file == null) return;
        file = new File(file, name); 
        DelegatingInterceptor dic = getInterceptor(file, isFolder);
        dic.beforeCreate();
    }

    public void createSuccess(FileObject fo) {
        DelegatingInterceptor dic = getCreateInterceptor(fo);
        dic.beforeCreate();
    }

    public void fileFolderCreated(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe).afterCreate();
    }

    public void fileDataCreated(FileEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe).afterCreate();
    }
    
    // ==================================================================================================
    // MOVE
    // ==================================================================================================

    public IOHandler getMoveHandler(File from, File to) {
        DelegatingInterceptor dic = getInterceptor(from, to);
        return dic.beforeMove() ? dic : null;
    }
        
    public IOHandler getRenameHandler(File from, String newName) {
        File to = new File(from.getParentFile(), newName);
        return getMoveHandler(from, to);
    }

    public void fileRenamed(FileRenameEvent fe) {
        removeFromDeletedFiles(fe.getFile());
        getInterceptor(fe).afterMove();
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
        // not interested
    }

    private DelegatingInterceptor getInterceptor(FileEvent fe) {
        FileObject fo = fe.getFile();
        if (fo == null) return nullDelegatingInterceptor;
        File file = FileUtil.toFile(fo);
        if (file == null) return nullDelegatingInterceptor;
        
        VersioningSystem vs = master.getOwner(file);
        if (vs == null) return nullDelegatingInterceptor;

        if (fe instanceof FileRenameEvent) {
            FileRenameEvent fre = (FileRenameEvent) fe;
            File parent = file.getParentFile();
            if (parent != null) {
                String name = fre.getName();
                String ext = fre.getExt();
                if (ext != null && ext.length() > 0) {  // NOI18N
                    name += "." + ext;  // NOI18N
                }
                File from = new File(parent, name);
                return new DelegatingInterceptor(vs.getVCSInterceptor(), from, file, false);
            }
            return nullDelegatingInterceptor;
        } else {
            return new DelegatingInterceptor(vs.getVCSInterceptor(), file, null, false);
        }
    }

    private DelegatingInterceptor getCreateInterceptor(FileObject fo) {
        if (fo == null) return nullDelegatingInterceptor;
        return getInterceptor(FileUtil.toFile(fo), fo.isFolder());
    }
    
    private DelegatingInterceptor getInterceptor(File file, boolean isDirectory) {
        if (file == null) return nullDelegatingInterceptor;
        
        VersioningSystem vs = master.getOwner(file);
        if (vs == null) return nullDelegatingInterceptor;

        return new DelegatingInterceptor(vs.getVCSInterceptor(), file, null, isDirectory);
    }

    private DelegatingInterceptor getInterceptor(File from, File to) {
        if (from == null || to == null) return nullDelegatingInterceptor;
        
        VersioningSystem vs = master.getOwner(from);
        if (vs == null) return nullDelegatingInterceptor;

        return new DelegatingInterceptor(vs.getVCSInterceptor(), from, to, false);
    }
    
    private final DelegatingInterceptor nullDelegatingInterceptor = new DelegatingInterceptor() {
        public boolean beforeDelete() { return false; }
        public void doDelete() throws IOException {  }
        public void afterDelete() { }
        public boolean beforeMove() { return false; }
        public void doMove() throws IOException {  }
        public boolean beforeCreate() { return false; }
        public void doCreate() throws IOException {  }
        public void afterCreate() {  }
        public void afterChange() {  }
        public void afterMove() {  }
        public void handle() throws IOException {  }
        public boolean delete(File file) {  throw new UnsupportedOperationException();  }
    };
    
    /**
     * Delete interceptor: holds files and folders that we do not want to delete but must pretend that they were deleted.
     */ 
    private final Set<File> deletedFiles = new HashSet<File>(5);
    
    private class DelegatingInterceptor implements IOHandler, DeleteHandler {
        
        final VCSInterceptor  interceptor;
        final File            file;
        final File            to;
        private final boolean isDirectory;

        public DelegatingInterceptor() {
            this(null, null, null, false);
        }
        
        public DelegatingInterceptor(VCSInterceptor interceptor, File file, File to, boolean isDirectory) {
            this.interceptor = interceptor;
            this.file = file;
            this.to = to;
            this.isDirectory = isDirectory;
        }

        public boolean beforeDelete() {
            return interceptor.beforeDelete(file);
        }

        public void doDelete() throws IOException {
            interceptor.doDelete(file);
        }

        public void afterDelete() {
            interceptor.afterDelete(file);
        }

        public boolean beforeMove() {
            return interceptor.beforeMove(file, to);
        }

        public void doMove() throws IOException {
            interceptor.doMove(file, to);
        }

        public void afterMove() {
            interceptor.afterMove(file, to);
        }

        public boolean beforeCreate() {
            return interceptor.beforeCreate(file, isDirectory);
        }

        public void doCreate() throws IOException {
            interceptor.doCreate(file, isDirectory);
        }

        public void afterCreate() {
            interceptor.afterCreate(file);
        }

        public void afterChange() {
            interceptor.afterChange(file);
        }

        /**
         * We are doing MOVE here, inspite of the generic name of the method.
         * 
         * @throws IOException
         */
        public void handle() throws IOException {
            interceptor.doMove(file, to);
        }

        /**
         * This must act EXACTLY like java.io.File.delete(). This means:

         * 1.1  if the file is a file and was deleted, return true 
         * 1.2  if the file is a file and was NOT deleted because we want to keep it (is part of versioning metadata), also return true
         *      this is done this way to enable bottom-up recursive file deletion
         * 1.3  if the file is a file that should be deleted but the operation failed (the file is locked, for example), return false
         *  
         * 2.1  if the file is an empty directory that was deleted, return true 
         * 2.2  if the file is a NON-empty directory that was NOT deleted because it contains files that were NOT deleted in step 1.2, return true 
         * 2.3  if the file is a NON-empty directory that was NOT deleted because it contains some files that were not previously deleted, return false 
         * 
         * @param file file or folder to delete
         * @return true if the file was successfully deleted (event virtually deleted), false otherwise
         */
        public boolean delete(File file) {
            File [] children = file.listFiles();
            if (children != null) {
                synchronized(deletedFiles) {
                    for (File child : children) {
                        if (!deletedFiles.contains(child)) return false;
                    }
                }
            }
            try {
                interceptor.doDelete(file);
                synchronized(deletedFiles) {
                    if (file.isDirectory()) {
                        // the directory was virtually deleted, we can forget about its children
                        for (Iterator<File> i = deletedFiles.iterator(); i.hasNext(); ) {
                            File fakedFile = i.next();                             
                            if (file.equals(fakedFile.getParentFile())) {
                                i.remove();
                            }
                        }
                    }
                    if (file.exists()) {
                        deletedFiles.add(file);
                    } else {
                        deletedFiles.remove(file);
                    }
                }
                return true;
            } catch (IOException e) {
                // the interceptor failed to delete the file
                return false;
            }
        }
        
        VCSInterceptor getInterceptor() {
            return interceptor;
        }
    }
}
